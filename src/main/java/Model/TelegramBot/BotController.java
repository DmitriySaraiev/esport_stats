package Model.TelegramBot;

import Model.Dota.DotaDatabase;
import Model.Dota.PickSearcher;
import Model.Main;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class BotController {

    private DotaDatabase ddb;
    private long chatId;
    private Timestamp subscriptionUntill;
    private BotState botState;
    private int radiantHeroCounter = 1;
    private int direHeroCounter = 1;
    private boolean isPickingRadiantHero = true;
    private ArrayList<String> heroList;
    private ArrayList<String> leftHeroList;
    private ArrayList<String> rightHeroList;
    private String teams;

    public BotController(long chatId, DotaDatabase ddb) {
        this.ddb = ddb;
        this.chatId = chatId;
        try {
            ResultSet rs = ddb.getUserById(chatId);
            rs.next();
            Timestamp subscriptionUntill = rs.getTimestamp("subscription_until");
            this.subscriptionUntill = subscriptionUntill;
            teams = "";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        leftHeroList = new ArrayList<>();
        rightHeroList = new ArrayList<>();
        botState = BotState.START;
    }

    public void reset(){
        leftHeroList.clear();
        rightHeroList.clear();
        radiantHeroCounter = 1;
        direHeroCounter = 1;
        botState = BotState.START;
        teams = "";
    }

    public void mergeHeroLists(){
        heroList = new ArrayList<>(leftHeroList);
        heroList.addAll(rightHeroList);
    }

    public File createExcell(long chatId){
        ArrayList<String> heroeList = new ArrayList<>(leftHeroList);
        heroeList.addAll(rightHeroList);
        PickSearcher pickSearcher = new PickSearcher(heroeList, Main.ddb);
        return pickSearcher.searchByPicksToExcel(pickSearcher.getData(), chatId);
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public void setLeftHeroList(ArrayList<String> leftHeroList) {
        this.leftHeroList = leftHeroList;
    }

    public void setRightHeroList(ArrayList<String> rightHeroList) {
        this.rightHeroList = rightHeroList;
    }

    public void setHeroList(ArrayList<String> heroList) {
        this.heroList = heroList;
    }

    public ArrayList<String> getHeroList() {
        return heroList;
    }

    public int getRadiantHeroCounter() {
        return radiantHeroCounter;
    }

    public int getDireHeroCounter() {
        return direHeroCounter;
    }

    public void setRadiantHeroCounter(int radiantHeroCounter) {
        this.radiantHeroCounter = radiantHeroCounter;
    }

    public void setDireHeroCounter(int direHeroCounter) {
        this.direHeroCounter = direHeroCounter;
    }

    public boolean isPickingRadiantHero() {
        return isPickingRadiantHero;
    }

    public void setPickingRadiantHero(boolean pickingRadiantHero) {
        isPickingRadiantHero = pickingRadiantHero;
    }

    public Timestamp getSubscriptionUntill() {
        this.subscriptionUntill = ddb.getSubscriptionUntil(chatId);
        return subscriptionUntill;
    }

    public void setSubscriptionUntill(int hours) {
        this.subscriptionUntill = ddb.addTimeToSubscription(chatId, hours);
    }

    public void setSubscriptionUntill(Timestamp tillDate) {
        this.subscriptionUntill = ddb.addTimeToSubscription(chatId, tillDate);
    }

    public boolean isSubscriptionActive() {
        if(getSubscriptionUntill() == null)
            return false;
        else
            return getSubscriptionUntill().after(new Timestamp(System.currentTimeMillis()));
    }

    public ArrayList<String> getLeftHeroList() {
        return leftHeroList;
    }

    public ArrayList<String> getRightHeroList() {
        return rightHeroList;
    }

    public String getTeams() {
        return teams;
    }

    public void setTeams(String teams) {
        this.teams = teams;
    }
}
