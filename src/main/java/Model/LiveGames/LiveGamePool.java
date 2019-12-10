package Model.LiveGames;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LiveGamePool {

    private ConcurrentHashMap<String, LiveGame> liveGames;
    LiveGameParser liveGameParser;
    private boolean isMainThreadBusy = false;

    public LiveGamePool(){
        liveGames = new ConcurrentHashMap<>();
        liveGameParser = new LiveGameParser();
        Runnable poolTask = () -> {
            while(true) {
                try {
                    if (!isMainThreadBusy) {
                        updateLiveGames();
                        System.out.println("currently " + liveGames.size() + " games live");
                        for (Map.Entry entry : liveGames.entrySet()) {
                            LiveGame liveGame = (LiveGame) entry.getValue();
                            ArrayList<String> picks = liveGame.getPicks();
                            if (picks.size() < 10) {
                                updateLivePicks(entry.getKey().toString());
                            }
                        }
                    }
                    Thread.sleep(1000 * 60 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catch (NullPointerException e){
                    System.out.println("NullPointer");
                    e.printStackTrace();}
            }
        };
        Runnable ubuntuTask = () -> {
            try{
                Thread.sleep(1000 * 60 * 60 * 6);
                System.out.println("clearing chrome memory...");
                isMainThreadBusy = true;
                Runtime.getRuntime().exec("/bin/bash -c pkill chromedriver");   //sometimes chromedriver proceses aren't closed by themselves, kill chromedrivers every 6 hours
                isMainThreadBusy = false;
            }
            catch (IOException e){e.printStackTrace();}
            catch (InterruptedException e) {
                System.out.println("NullPointer");
                e.printStackTrace();
            }
            catch (NullPointerException e){e.printStackTrace();}
        };
        Thread poolThread = new Thread(poolTask);
        poolThread.start();
        Thread ubuntuThread = new Thread(ubuntuTask);
        ubuntuThread.start();
    }

    public void updateLiveGames(){
        HashMap<String, String> liveGameMap = liveGameParser.parseLiveGames();
        Iterator iterator = liveGames.entrySet().iterator();
        while (iterator.hasNext()) {                                  //удаляем завершенные игры
            Map.Entry<String, String> entry = (Map.Entry<String, String>)iterator.next();
            if(liveGameMap.get(entry.getKey()) == null)
                iterator.remove();
        }
        for(Map.Entry entry : liveGameMap.entrySet()){               //добавляем текущие игры
            if(liveGames.get(entry.getKey()) == null)
                liveGames.put(entry.getKey().toString(), new LiveGame(new ArrayList<>(), entry.getValue().toString()));
        }
    }

    private void updateLivePicks(String link) {
        Runnable pickTask = () -> {
            try {
                while (true) {
                    ArrayList<String> runnablePicks = liveGameParser.parseLiveGame(link);
                    if (runnablePicks.size() == 10) {
                        updateLiveGamePicks(link, runnablePicks);
                        break;
                    }
                    Thread.sleep(1000 * 60);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread pickThread = new Thread(pickTask);
        pickThread.start();
    }

    private void updateLiveGamePicks(String link, ArrayList<String> picks){
        LiveGame liveGame = liveGames.get(link);
        liveGame.setPicks(picks);
    }

    public ConcurrentHashMap<String, LiveGame> getLiveGames() {
        return liveGames;
    }
}
