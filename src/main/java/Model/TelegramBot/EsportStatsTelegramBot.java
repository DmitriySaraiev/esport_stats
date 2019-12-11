package Model.TelegramBot;

import Model.Dota.DotaDatabase;
import Model.Dota.Heroes;
import Model.LiveGames.LiveGame;
import Model.LiveGames.LiveGamePool;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EsportStatsTelegramBot extends TelegramLongPollingBot {

    LiveGamePool liveGamePool;
    private DotaDatabase ddb;
    private HashMap<Long, BotController> botControllerMap;
    private boolean isChangingHero = false;
    private int heroIndex = -1;
    private final long servetTimeShift = 1000L*60*60*3; //3 hours

    public EsportStatsTelegramBot(DotaDatabase ddb) {
        this.ddb = ddb;
        botControllerMap = new HashMap();
        liveGamePool = new LiveGamePool();
    }

    @Override
    public void onUpdateReceived(Update update) {

        SendMessage message = new SendMessage();
        long chatId;
        String text = "Похоже, что произошла ошибка. Если бот завис, пожалуйста, введите /start для перезапуска сессии";
        BotController botController;

        if (update.hasMessage())
            chatId = update.getMessage().getChatId();
        else
            chatId = update.getCallbackQuery().getMessage().getChatId();
        botController = getBotController(chatId);
        if (update.hasMessage() && update.getMessage().getText().equals("/start")){
            processStartCommand(text, chatId, botController);
        }

        processBotState(botController, text, chatId, message, update);

        message.setChatId(chatId).setText(text);
        try {
            if (!botController.getBotState().equals(BotState.CREATING_EXCEL)) {
                execute(message);
            } else {
                execute(message);
                if (!botController.isSubscriptionActive()) {
                    int freeUsesLeft = ddb.getFreeUsesLeft(chatId) - 1;
                    ddb.setFreeUsesLeft(freeUsesLeft, chatId);
                }
                ddb.incrimentUsesAll(chatId);
                ArrayList<String> heroList = new ArrayList<>(botController.getLeftHeroList());
                heroList.addAll(botController.getRightHeroList());
                ddb.createUserSearchQuery(chatId, heroList, botController.getTeams());
                sendExcel(chatId);
                botControllerMap.remove(chatId);
                botController.reset();
                text = "Выберите одну из опций:";
                message = new SendMessage(chatId, text);
                setOptionsReplyKeyboard(message);
                execute(message);
            }
        } catch (
                TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private BotController getBotController(long chatId) {
        BotController botController;
        if (!botControllerMap.containsKey(chatId))
            createBotController(chatId);
        botController = botControllerMap.get(chatId);
        return botController;
    }

    private void createBotController(long chatId){
        BotController botController = new BotController(chatId, ddb);
        botControllerMap.put(chatId, botController);
        System.out.println("botController created " + chatId);
        System.out.println("size = " + botControllerMap.size());
    }

    private void processBotState(BotController botController, String text, long chatId, SendMessage message, Update update){
        switch (botController.getBotState()) {
            case START:
                processStartState(text, chatId, botController, message, update);
                break;
            case SUBSCRIPTION_MODE:
                processSubscriptionModeState(text, chatId, botController, message, update);
                break;
            case ENTERING_SUBSCRIPTION_CODE:
                processEnteringSubscriptionCodeState(text, chatId, botController, message, update);
                break;
            case CHOOSING_ANALYSER_MODE:
                processChoosingAnalyzerModeState(text, chatId, botController, message, update);
                break;
            case PICKING_LIVE_GAME:
                processPickingLiveGameState(text, chatId, botController, message, update);
                break;
            case PICKING_FIRST_LETTER:
                processPickingFirstLetterState(text, chatId, botController, message, update);
                break;
            case PICKING_HERO:
                processPickingHeroState(text, chatId, botController, message, update);
                break;
            case CONFIRMING_LIVE_GAME:
                processConfirmingLiveGameState(text, chatId, botController, message, update);
                break;
            case CONFIRMING:
                processConfirmingState(text, chatId, botController, message, update);
                break;
            case PICKING_HERO_TO_CHANGE:
                processPickingHeroToChangeState(text, chatId, botController, message, update);
                break;
            case CREATING_EXCEL:
                break;
        }
    }

    private void processStartCommand(String text, long chatId, BotController botController){
        text = "Выберите одну из опций:";
        botControllerMap.remove(chatId);
        botControllerMap.put(chatId, new BotController(chatId, ddb));
        isChangingHero = false;
        botController = getBotController(chatId);
    }

    private void processStartState(String text, long chatId, BotController botController, SendMessage message, Update update){
        botController.reset();
        setOptionsReplyKeyboard(message);
        text = "Выберите одну из опций:";
        if (update.getMessage().getText().equals("Начать работу анализатора пиков")) {
            StringBuilder responceSb = new StringBuilder();
            int freeUsesLeft = ddb.getFreeUsesLeft(chatId);
            if (!botController.isSubscriptionActive() && freeUsesLeft != 0) {
                responceSb.append("У вас осталось " + freeUsesLeft + " бесплатных запросов\n\n");
            }
            else if(!botController.isSubscriptionActive() && freeUsesLeft == 0){
                text = "Ваша подписка истекла/не ативирована. У вас не осталось бесплатных запросов.";
                setOptionsReplyKeyboard(message);
                return;
            }
            ConcurrentHashMap<String, LiveGame> liveGameMap = liveGamePool.getLiveGames();
            responceSb.append("Список текущих игр:\n");
            for(Map.Entry entry : liveGameMap.entrySet()){
                LiveGame liveGame = (LiveGame)entry.getValue();
                responceSb.append(liveGame.getTeams()+"\n");
            }
            if(liveGameMap.size() > 0)
                text = responceSb.toString();
            else
                text = "Игр в лайве не было обнаружено, пожалуйста, введите пик вручную";
            setChooseAnalyserModeKeyboard(message, liveGameMap.size() > 0);
            botController.setBotState(BotState.CHOOSING_ANALYSER_MODE);
        } else if (update.getMessage().getText().equals("Моя подписка")) {
            removeReplyKeyboard(message);
            Timestamp subsr = botController.getSubscriptionUntill();
            if (subsr == null || subsr.before(new Timestamp(System.currentTimeMillis()))) {
                int freeUsesLeft = ddb.getFreeUsesLeft(chatId);
                if (freeUsesLeft != 0)
                    text = "Ваша подписка истекла/не ативирована.\nУ вас осталось " + freeUsesLeft + " бесплатных запросов";
                else
                    text = "Ваша подписка истекла/не ативирована";
                setSubscriptionModeKeyboard(message);
                botController.setBotState(BotState.SUBSCRIPTION_MODE);
            } else {
                text = "Ваша подписка активна до " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Timestamp(subsr.getTime() + servetTimeShift));
                setOptionsReplyKeyboard(message);
                botControllerMap.remove(chatId);
                botController.setBotState(BotState.START);
            }
        } else if (update.getMessage().getText().equals("Информация")) {
            text = "Всю необходимую информация вы найдете на канале @esportStatsDota2Channel";
        }
    }

    private void processSubscriptionModeState(String text, long chatId, BotController botController, SendMessage message, Update update){
        if (update.getMessage().getText().equals("Ввести код для активации подписки")) {
            text = "Пожалуйста, введите активационный код:";
            botController.setBotState(BotState.ENTERING_SUBSCRIPTION_CODE);
            removeReplyKeyboard(message);
        } else if (update.getMessage().getText().equals("К списку комманд")) {
            setOptionsReplyKeyboard(message);
            text = "Выберите одну из опций:";
            botControllerMap.remove(chatId);
            botController.setBotState(BotState.START);
        }
    }

    private void processEnteringSubscriptionCodeState(String text, long chatId, BotController botController, SendMessage message, Update update){
        Subscription subscription;
        subscription = ddb.getSubscriptionByCode(update.getMessage().getText());
        if (subscription.getCode() == null) {
            text = "Неправильный код!";
            botController.setBotState(BotState.SUBSCRIPTION_MODE);
            setSubscriptionModeKeyboard(message);
        } else {
            if (subscription.isReusable()) {
                if (subscription.getTillDate().before(new Timestamp(System.currentTimeMillis()))) {
                    text = "К сожалению, срок действия этого кода уже истек";
                    botControllerMap.remove(chatId);
                    botController.setBotState(BotState.START);
                    setOptionsReplyKeyboard(message);
                } else {
                    botController.setSubscriptionUntill(subscription.getTillDate());
                    ddb.createUserSubscription(chatId, subscription);
                }
            } else
                botController.setSubscriptionUntill(subscription.getHours());
            if (subscription.getTillDate().after(new Timestamp(System.currentTimeMillis()))) {
                text = "Ваш активационный код был успешно подтвержден. Ваша подписка активна до " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Timestamp(botController.getSubscriptionUntill().getTime() + servetTimeShift));
                ddb.createUserSubscription(chatId, subscription);
                botControllerMap.remove(chatId);
                botController.setBotState(BotState.START);
                setOptionsReplyKeyboard(message);
            }
        }
    }

    private void processChoosingAnalyzerModeState(String text, long chatId, BotController botController, SendMessage message, Update update){
        if (update.getMessage().getText().equals("Выбрать из списка текущих игр")){
            text = "Пожалуйста, выберите одну из предложеных игр:";
            setChooseLiveGameKeyboard(message);
            botController.setBotState(BotState.PICKING_LIVE_GAME);
        }
        else if(update.getMessage().getText().equals("Ввести пики вручную")){
            int freeUsesLeft = ddb.getFreeUsesLeft(chatId);
            if (botController.isSubscriptionActive()) {
                text = "Выберите первую букву имени героя сил света №1";
                setAlphabetReplyKeyboard(message, true);
                botController.setBotState(BotState.PICKING_FIRST_LETTER);
                return;
            }
            if (freeUsesLeft != 0) {
                text = "Ваша подписка истекла/не ативирована.\nУ вас осталось " + freeUsesLeft + " бесплатных запросов\nВыберите первую букву имени героя сил света №1";
                setAlphabetReplyKeyboard(message, true);
                botController.setBotState(BotState.PICKING_FIRST_LETTER);
            } else {
                text = "Ваша подписка истекла/не ативирована";
            }
        }
        else if(update.getMessage().getText().equals("Назад")){
            botControllerMap.remove(chatId);
            botController.setBotState(BotState.START);
            text = "Выберите одну из опций:";
            setOptionsReplyKeyboard(message);
        }
        else{
            text = "Пожалуйста, выберите одну из опций:";
        }
    }

    private void processPickingLiveGameState(String text, long chatId, BotController botController, SendMessage message, Update update){
        if(update.getMessage().getText().equals("Назад")){
            botController.setBotState(BotState.CHOOSING_ANALYSER_MODE);
            setChooseAnalyserModeKeyboard(message, true);
            StringBuilder responceSb = new StringBuilder();
            ConcurrentHashMap<String, LiveGame> liveGameMap = liveGamePool.getLiveGames();
            responceSb.append("Список текущих игр:\n");
            for(Map.Entry entry : liveGameMap.entrySet()){
                LiveGame liveGame = (LiveGame)entry.getValue();
                responceSb.append(liveGame.getTeams()+"\n");
            }
            if(liveGameMap.size() > 0)
                text = responceSb.toString();
            else
                text = "Игр в лайве не было обнаружено, пожалуйста, введите пик вручную";
            setChooseAnalyserModeKeyboard(message, liveGameMap.size() > 0);
        }
        else if (update.getMessage().getText().contains("____")){
            String linkNumber = update.getMessage().getText().split("____")[1];
            botController.setTeams(update.getMessage().getText().split("____")[0]);
            ArrayList<String> pickList = liveGamePool.getLiveGames().get("https://hawkbets.com/matches/"+linkNumber).getPicks();
            if(pickList.size() < 10){
                text = "Стадия пиков еще не завершена, пожалуйста, повторите попытку позже.";
                return;
            }
            botController.setLeftHeroList(new ArrayList<>(pickList.subList(0,5)));
            botController.setRightHeroList(new ArrayList<>(pickList.subList(5,10)));
            StringBuilder sb = new StringBuilder();
            sb.append("Вы выбрали " + botController.getTeams() + "\n");
            for (int i = 0; i < botController.getLeftHeroList().size(); i++) {
                sb.append(botController.getLeftHeroList().get(i));
                if (i == 4)
                    sb.append("\n");
                else
                    sb.append(", ");
            }
            for (int i = 0; i < botController.getRightHeroList().size(); i++) {
                sb.append(botController.getRightHeroList().get(i));
                if (i == 4)
                    sb.append("\n");
                else
                    sb.append(", ");
            }
            sb.append("\nВсе верно?");
            text = sb.toString();
            botController.setBotState(BotState.CONFIRMING_LIVE_GAME);
            setAgreeReplyKeyboard(message);
        }
        else{
            text = "Пожалуйста, выберите одну из опций:";
        }
    }

    private void processPickingFirstLetterState(String text, long chatId, BotController botController, SendMessage message, Update update) {
        if (update.getMessage().getText().equals("Сменить сторону выбора")) {
            botController.setPickingRadiantHero(!botController.isPickingRadiantHero());
            if (botController.isPickingRadiantHero())
                text = "Выберите первую букву имени героя сил света №" + botController.getRadiantHeroCounter();
            else
                text = "Выберите первую букву имени героя сил тьмы №" + (botController.getDireHeroCounter());
        } else if (update.getMessage().getText().matches("[A-Z]")) {
            removeReplyKeyboard(message);
            try {
                execute(message.setChatId(chatId).setText("Список героев "));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            text = "на выбранную букву:";
            botController.setBotState(BotState.PICKING_HERO);
            setHeroListInlineKeyboard(message, update.getMessage().getText());
        } else {
            text = "Пожалуйста, выберите букву:";
        }
    }

    private void processPickingHeroState(String text, long chatId, BotController botController, SendMessage message, Update update){
        if (isChangingHero) {
            if(heroIndex > 4){
                heroIndex -= 5;
                botController.getRightHeroList().remove(heroIndex);
                botController.getRightHeroList().add(heroIndex, update.getCallbackQuery().getData());
            }
            else {
                botController.getLeftHeroList().remove(heroIndex);
                botController.getLeftHeroList().add(heroIndex, update.getCallbackQuery().getData());
            }
            isChangingHero = false;
        } else {
            if(botController.isPickingRadiantHero())
                botController.getLeftHeroList().add(update.getCallbackQuery().getData());
            else
                botController.getRightHeroList().add(update.getCallbackQuery().getData());
            botController.setBotState(BotState.PICKING_FIRST_LETTER);
            if (botController.isPickingRadiantHero())
                botController.setRadiantHeroCounter(botController.getRadiantHeroCounter() + 1);
            else
                botController.setDireHeroCounter(botController.getDireHeroCounter() + 1);
        }
        if (botController.isPickingRadiantHero() ) {
            if(botController.getRadiantHeroCounter() <= 5)
                text = "Выберите первую букву имени героя сил света №" + botController.getRadiantHeroCounter();
            if (botController.getRadiantHeroCounter() == 6) {
                text = "Выберите первую букву имени героя сил тьмы №" + botController.getDireHeroCounter();
                botController.setPickingRadiantHero(false);
            }
        } else if (!botController.isPickingRadiantHero()) {
            if(botController.getDireHeroCounter() <= 5)
                text = "Выберите первую букву имени героя сил тьмы №" + (botController.getDireHeroCounter());
            if (botController.getDireHeroCounter()== 6) {
                text = "Выберите первую букву имени героя сил света №" + botController.getRadiantHeroCounter();
                botController.setPickingRadiantHero(true);
            }
        }
        boolean isChangingAllowed = false;
        if (botController.isPickingRadiantHero()) {
            if (botController.getDireHeroCounter() < 6)
                isChangingAllowed = true;
        } else if (!botController.isPickingRadiantHero()) {
            if (botController.getRadiantHeroCounter() < 6)
                isChangingAllowed = true;
        }
        setAlphabetReplyKeyboard(message, isChangingAllowed);
        if(botController.getRadiantHeroCounter() > 5 && botController.getDireHeroCounter() > 5) {
            botController.setBotState(BotState.CONFIRMING);
            setAgreeReplyKeyboard(message);
            StringBuilder sb1 = new StringBuilder();
            for (int i = 0; i < botController.getLeftHeroList().size(); i++) {
                sb1.append(botController.getLeftHeroList().get(i));
                if (i == 4)
                    sb1.append("\n");
                else
                    sb1.append(", ");
            }
            for (int i = 0; i < botController.getRightHeroList().size(); i++) {
                sb1.append(botController.getRightHeroList().get(i));
                if (i == 4)
                    sb1.append("\n");
                else
                    sb1.append(", ");
            }
            sb1.append("\nВсе верно?");
            text = sb1.toString();
        }
    }

    private void processConfirmingLiveGameState(String text, long chatId, BotController botController, SendMessage message, Update update) {
        if (update.getMessage().getText().equals("Да")) {
            botController.setBotState(BotState.CREATING_EXCEL);
            text = "Пожалуйста, подождите...";
            removeReplyKeyboard(message);
        } else if (update.getMessage().getText().equals("Нет")) {
            text = "Пожалуйста, выберите одну из предложеных игр:";
            setChooseLiveGameKeyboard(message);
            botController.setBotState(BotState.PICKING_LIVE_GAME);
        } else {
            text = "Пожалуйста, выберите ответ:";
        }
    }

    private void processConfirmingState(String text, long chatId, BotController botController, SendMessage message, Update update) {
        if (update.getMessage().getText().equals("Да")) {
            botController.setBotState(BotState.CREATING_EXCEL);
            text = "Пожалуйста, подождите...";
            removeReplyKeyboard(message);
        } else if (update.getMessage().getText().equals("Нет")) {
            botController.setBotState(BotState.PICKING_HERO_TO_CHANGE);
            text = "Выберите героя, которого вы желаете заменить";
            isChangingHero = true;
            ArrayList<String> heroList = new ArrayList<>(botController.getLeftHeroList());
            heroList.addAll(botController.getRightHeroList());
            botController.mergeHeroLists();
            botController.setRadiantHeroCounter(6);
            botController.setDireHeroCounter(6);
            setChangeHeroReplyKeyboard(message, heroList);
        } else {
            text = "Пожалуйста, выберите ответ:";
        }
    }

    private void processPickingHeroToChangeState(String text, long chatId, BotController botController, SendMessage message, Update update){
        heroIndex = botController.getHeroList().indexOf(update.getMessage().getText());
        botController.setBotState(BotState.PICKING_FIRST_LETTER);
        text = "Выберите первую букву имени героя";
        setAlphabetReplyKeyboard(message, false);
    }
    
    private synchronized void sendExcel(Long chatId) {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        File f = botControllerMap.get(chatId).createExcell(chatId);
        sendDocumentRequest.setDocument(f);
        sendDocumentRequest.setCaption("Статистика");
        try {
            execute(sendDocumentRequest);
            f.delete();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private synchronized void setHeroListInlineKeyboard(SendMessage sendMessage, String letter) {
        InlineKeyboardMarkup heroKeyboardMarkup = new InlineKeyboardMarkup();
        //sendMessage.setReplyMarkup(new ReplyKeyboardMarkup());
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
        int counter = 0;
        for (String hero : getHeroListByFirstletter(letter)) {
            if (counter++ > 2) {
                counter = 0;
                keyboard.add(keyboardRow);
                keyboardRow = new ArrayList<>();
            }
            keyboardRow.add(new InlineKeyboardButton().setText(hero).setCallbackData(hero));
        }
        keyboard.add(keyboardRow);
        heroKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(heroKeyboardMarkup);
    }

    private synchronized void setOptionsReplyKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup optionsKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Начать работу анализатора пиков"));
        keyboardRow2.add(new KeyboardButton("Моя подписка"));
        keyboardRow3.add(new KeyboardButton("Информация"));
        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);
        rowList.add(keyboardRow3);
        optionsKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(optionsKeyboardMarkup);
    }

    private synchronized void setSubscriptionModeKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup subscriptionModeKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Ввести код для активации подписки"));
        keyboardRow2.add(new KeyboardButton("К списку комманд"));
        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);
        subscriptionModeKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(subscriptionModeKeyboardMarkup);
    }

    private synchronized void setChangeHeroReplyKeyboard(SendMessage sendMessage, ArrayList<String> heroList) {
        ReplyKeyboardMarkup changeHeroKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        for (int i = 0; i < 5; i++) {
            keyboardRow1.add(heroList.get(i));
            keyboardRow2.add(heroList.get(i + 5));
        }
        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);
        changeHeroKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(changeHeroKeyboardMarkup);
    }

    private synchronized void setAgreeReplyKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup agreeKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Да"));
        keyboardRow1.add(new KeyboardButton("Нет"));
        rowList.add(keyboardRow1);
        agreeKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(agreeKeyboardMarkup);
    }

    private synchronized void setChooseAnalyserModeKeyboard(SendMessage sendMessage, boolean isLiveGamePresent) {
        ReplyKeyboardMarkup agreeKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        if(isLiveGamePresent){
            keyboardRow1.add(new KeyboardButton("Выбрать из списка текущих игр"));
            keyboardRow2.add(new KeyboardButton("Ввести пики вручную"));
            keyboardRow3.add(new KeyboardButton("Назад"));
        }
        else {
            keyboardRow1.add(new KeyboardButton("Ввести пики вручную"));
            keyboardRow2.add(new KeyboardButton("Назад"));
        }
        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);
        if(isLiveGamePresent)
            rowList.add(keyboardRow3);
        agreeKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(agreeKeyboardMarkup);
    }

    private synchronized void setAlphabetReplyKeyboard(SendMessage sendMessage, boolean isChangingAllowed) {
        ReplyKeyboardMarkup alphabetKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardRow keyboardRow4 = new KeyboardRow();
        KeyboardRow keyboardRow5 = new KeyboardRow();

        keyboardRow1.add(new KeyboardButton("A"));
        keyboardRow1.add(new KeyboardButton("B"));
        keyboardRow1.add(new KeyboardButton("C"));
        keyboardRow1.add(new KeyboardButton("D"));
        keyboardRow1.add(new KeyboardButton("E"));
        keyboardRow1.add(new KeyboardButton("F"));

        keyboardRow2.add(new KeyboardButton("G"));
        keyboardRow2.add(new KeyboardButton("H"));
        keyboardRow2.add(new KeyboardButton("I"));
        keyboardRow2.add(new KeyboardButton("J"));
        keyboardRow2.add(new KeyboardButton("K"));
        keyboardRow2.add(new KeyboardButton("L"));

        keyboardRow3.add(new KeyboardButton("M"));
        keyboardRow3.add(new KeyboardButton("N"));
        keyboardRow3.add(new KeyboardButton("O"));
        keyboardRow3.add(new KeyboardButton("P"));
        keyboardRow3.add(new KeyboardButton("Q"));
        keyboardRow3.add(new KeyboardButton("R"));

        keyboardRow4.add(new KeyboardButton("S"));
        keyboardRow4.add(new KeyboardButton("T"));
        keyboardRow4.add(new KeyboardButton("U"));
        keyboardRow4.add(new KeyboardButton("V"));
        keyboardRow4.add(new KeyboardButton("W"));
        keyboardRow4.add(new KeyboardButton("Z"));

        if (isChangingAllowed)
            keyboardRow5.add(new KeyboardButton("Сменить сторону выбора"));

        rowList.add(keyboardRow1);
        rowList.add(keyboardRow2);
        rowList.add(keyboardRow3);
        rowList.add(keyboardRow4);
        if (isChangingAllowed)
            rowList.add(keyboardRow5);
        alphabetKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(alphabetKeyboardMarkup);
    }

    private synchronized void setChooseLiveGameKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup chooseLiveGameKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        ConcurrentHashMap<String, LiveGame> liveGames = liveGamePool.getLiveGames();
        for(Map.Entry entry : liveGames.entrySet()){
            LiveGame liveGame = (LiveGame) entry.getValue();
            String[] linkAr = entry.getKey().toString().split("/");
            String linkNumber = linkAr[linkAr.length-1];
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(new KeyboardButton(liveGame.getTeams() +"  ____"+linkNumber));
            rowList.add(keyboardRow);
        }
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("Назад"));
        rowList.add(keyboardRow);
        chooseLiveGameKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(chooseLiveGameKeyboardMarkup);
    }

    private synchronized void removeReplyKeyboard(SendMessage sendMessage) {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        sendMessage.setReplyMarkup(keyboardRemove);
    }

    private ArrayList<String> getHeroListByFirstletter(String letter) {
        ArrayList<String> heroList = new ArrayList<>();
        switch (letter) {
            case "A":
                heroList.add(Heroes.aba);
                heroList.add(Heroes.alch);
                heroList.add(Heroes.aa);
                heroList.add(Heroes.am);
                heroList.add(Heroes.arc);
                heroList.add(Heroes.axe);
                break;
            case "B":
                heroList.add(Heroes.bane);
                heroList.add(Heroes.bat);
                heroList.add(Heroes.beast);
                heroList.add(Heroes.bs);
                heroList.add(Heroes.bh);
                heroList.add(Heroes.brew);
                heroList.add(Heroes.bb);
                heroList.add(Heroes.brood);
                break;
            case "C":
                heroList.add(Heroes.cent);
                heroList.add(Heroes.ck);
                heroList.add(Heroes.chen);
                heroList.add(Heroes.clinkz);
                heroList.add(Heroes.clock);
                heroList.add(Heroes.cm);
                break;
            case "D":
                heroList.add(Heroes.ds);
                heroList.add(Heroes.dw);
                heroList.add(Heroes.dazzle);
                heroList.add(Heroes.dp);
                heroList.add(Heroes.disruptor);
                heroList.add(Heroes.doom);
                heroList.add(Heroes.dk);
                heroList.add(Heroes.drow);
                break;
            case "E":
                heroList.add(Heroes.es);
                heroList.add(Heroes.shaker);
                heroList.add(Heroes.et);
                heroList.add(Heroes.ember);
                heroList.add(Heroes.ench);
                heroList.add(Heroes.enigma);
                break;
            case "F":
                heroList.add(Heroes.faceles);
                break;
            case "G":
                heroList.add(Heroes.grim);
                heroList.add(Heroes.gyro);
                break;
            case "H":
                heroList.add(Heroes.huskar);
                break;
            case "I":
                heroList.add(Heroes.invoker);
                heroList.add(Heroes.io);
                break;
            case "J":
                heroList.add(Heroes.jakiro);
                heroList.add(Heroes.jugg);
                break;
            case "K":
                heroList.add(Heroes.kotl);
                heroList.add(Heroes.kunkka);
                break;
            case "L":
                heroList.add(Heroes.lc);
                heroList.add(Heroes.lesh);
                heroList.add(Heroes.lich);
                heroList.add(Heroes.ls);
                heroList.add(Heroes.lina);
                heroList.add(Heroes.lion);
                heroList.add(Heroes.ld);
                heroList.add(Heroes.luna);
                heroList.add(Heroes.lycan);
                break;
            case "M":
                heroList.add(Heroes.magnus);
                heroList.add(Heroes.mars);
                heroList.add(Heroes.medusa);
                heroList.add(Heroes.meepo);
                heroList.add(Heroes.mirana);
                heroList.add(Heroes.mk);
                heroList.add(Heroes.morph);
                break;
            case "N":
                heroList.add(Heroes.naga);
                heroList.add(Heroes.np);
                heroList.add(Heroes.necr);
                heroList.add(Heroes.ns);
                heroList.add(Heroes.nyx);
                break;
            case "O":
                heroList.add(Heroes.ogre);
                heroList.add(Heroes.omni);
                heroList.add(Heroes.oracle);
                heroList.add(Heroes.od);
                break;
            case "P":
                heroList.add(Heroes.pango);
                heroList.add(Heroes.pa);
                heroList.add(Heroes.pl);
                heroList.add(Heroes.phoenix);
                heroList.add(Heroes.puck);
                heroList.add(Heroes.pudge);
                heroList.add(Heroes.pugna);
                break;
            case "Q":
                heroList.add(Heroes.qop);
                break;
            case "R":
                heroList.add(Heroes.razor);
                heroList.add(Heroes.riki);
                heroList.add(Heroes.rubick);
                break;
            case "S":
                heroList.add(Heroes.sk);
                heroList.add(Heroes.sd);
                heroList.add(Heroes.sf);
                heroList.add(Heroes.ss);
                heroList.add(Heroes.silencer);
                heroList.add(Heroes.sky);
                heroList.add(Heroes.slardar);
                heroList.add(Heroes.slark);
                heroList.add(Heroes.sniper);
                heroList.add(Heroes.spectre);
                heroList.add(Heroes.sb);
                heroList.add(Heroes.storm);
                heroList.add(Heroes.sven);
                break;
            case "T":
                heroList.add(Heroes.techies);
                heroList.add(Heroes.ta);
                heroList.add(Heroes.tb);
                heroList.add(Heroes.tide);
                heroList.add(Heroes.timber);
                heroList.add(Heroes.tinker);
                heroList.add(Heroes.tiny);
                heroList.add(Heroes.treant);
                heroList.add(Heroes.troll);
                heroList.add(Heroes.tusk);
                break;
            case "U":
                heroList.add(Heroes.pitlord);
                heroList.add(Heroes.undying);
                heroList.add(Heroes.ursa);
                break;
            case "V":
                heroList.add(Heroes.venge);
                heroList.add(Heroes.veno);
                heroList.add(Heroes.viper);
                heroList.add(Heroes.visage);
                break;
            case "W":
                heroList.add(Heroes.warlock);
                heroList.add(Heroes.weaver);
                heroList.add(Heroes.wr);
                heroList.add(Heroes.ww);
                heroList.add(Heroes.wd);
                heroList.add(Heroes.wk);
                break;
            /*case "X":
                break;
            case "Y":
                break;*/
            case "Z":
                heroList.add(Heroes.zeus);
                break;
        }
        return heroList;
    }

    private void replaceHero(String oldHero, String newHero, int heroIndex, BotController botController){
        if(botController.getLeftHeroList().indexOf(oldHero) == heroIndex){
            botController.getLeftHeroList().remove(heroIndex);
            botController.getLeftHeroList().add(heroIndex, newHero);
        }
        else if(botController.getRightHeroList().indexOf(oldHero) == heroIndex) {
            botController.getRightHeroList().remove(heroIndex);
            botController.getRightHeroList().add(heroIndex, newHero);
        }
    }


    @Override
    public String getBotUsername() {
        return "";
    }  //botName

    @Override
    public String getBotToken() {
        return "";
    } //botToken

}
