package Model.Dota;

import java.util.ArrayList;

public class DotaPlayerPerformance {

    private String playerNick;
    private int kills;
    private int deaths;
    private int assists;
    private int networth;
    private int lastHits;
    private int denies;
    private int gpm;
    private int xpm;
    private int damage;
    private int healing;
    private int structureDamage;
    private int observerWards;
    private int sentryWards;
    private ArrayList<String> itembuild;
    private ArrayList<Integer> itembuildTiming;

    public DotaPlayerPerformance(String playerNick,int kills, int deaths, int assists, int networth, int lastHits, int denies, int gpm,
                                 int xpm, int damage, int healing, int structureDamage, int observerWards,
                                 int sentryWards, ArrayList<String> itembuild, ArrayList<Integer> itembuildTiming) {
        this.playerNick = playerNick;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.networth = networth;
        this.lastHits = lastHits;
        this.denies = denies;
        this.gpm = gpm;
        this.xpm = xpm;
        this.damage = damage;
        this.healing = healing;
        this.structureDamage = structureDamage;
        this.observerWards = observerWards;
        this.sentryWards = sentryWards;
        this.itembuild = itembuild;
        this.itembuildTiming = itembuildTiming;
    }

    public String getPlayerNick() {
        return playerNick;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public int getNetworth() {
        return networth;
    }

    public int getLastHits() {
        return lastHits;
    }

    public int getDenies() {
        return denies;
    }

    public int getGpm() {
        return gpm;
    }

    public int getXpm() {
        return xpm;
    }

    public int getDamage() {
        return damage;
    }

    public int getHealing() {
        return healing;
    }

    public int getStructureDamage() {
        return structureDamage;
    }

    public int getObserverWards() {
        return observerWards;
    }

    public int getSentryWards() {
        return sentryWards;
    }

    public ArrayList<String> getItembuild() {
        return itembuild;
    }

    public ArrayList<Integer> getItembuildTiming() {
        return itembuildTiming;
    }

    @Override
    public String toString() {
        return "DotaPlayerPerformance{" +
                "kills=" + kills +
                ", deaths=" + deaths +
                ", assists=" + assists +
                ", networth=" + networth +
                ", lastHits=" + lastHits +
                ", denies=" + denies +
                ", gpm=" + gpm +
                ", xpm=" + xpm +
                ", damage=" + damage +
                ", healing=" + healing +
                ", structureDamage=" + structureDamage +
                ", observerWards=" + observerWards +
                ", sentryWards=" + sentryWards +
                ", itembuild=" + itembuild +
                ", itembuildTiming=" + itembuildTiming +
                "\n" +
                '}';
    }
}
