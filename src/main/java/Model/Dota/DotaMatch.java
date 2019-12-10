package Model.Dota;

import java.util.ArrayList;

public class DotaMatch {

    private ArrayList<DotaGame> gameList;
    private DotaTeam team1;
    private DotaTeam team2;
    private ArrayList<DotaPlayer> team1PlayerList;
    private ArrayList<DotaPlayer> team2PlayerList;
    private int scoreTeam1;
    private int scoreTeam2;
    private DotaTournament tournament;
    private String dateAndTime;
    private String link;

    public DotaMatch() {
    }

    public DotaMatch(ArrayList<DotaGame> gameList, DotaTeam team1, DotaTeam team2, ArrayList<DotaPlayer> team1PlayerList,
                     ArrayList<DotaPlayer> team2PlayerList, int scoreTeam1, int scoreTeam2, DotaTournament tournament, String dateAndTime,
                     String link) {
        this.gameList = gameList;
        this.team1 = team1;
        this.team2 = team2;
        this.team1PlayerList = team1PlayerList;
        this.team2PlayerList = team2PlayerList;
        this.scoreTeam1 = scoreTeam1;
        this.scoreTeam2 = scoreTeam2;
        this.tournament = tournament;
        this.dateAndTime = dateAndTime;
        this.link = link;
    }

    public void setGameList(ArrayList<DotaGame> gameList) {
        this.gameList = gameList;
    }

    public void setTeam1(DotaTeam team1) {
        this.team1 = team1;
    }

    public void setTeam2(DotaTeam team2) {
        this.team2 = team2;
    }

    public void setTeam1PlayerList(ArrayList<DotaPlayer> team1PlayerList) {
        this.team1PlayerList = team1PlayerList;
    }

    public void setTeam2PlayerList(ArrayList<DotaPlayer> team2PlayerList) {
        this.team2PlayerList = team2PlayerList;
    }

    public void setScoreTeam1(int scoreTeam1) {
        this.scoreTeam1 = scoreTeam1;
    }

    public void setScoreTeam2(int scoreTeam2) {
        this.scoreTeam2 = scoreTeam2;
    }

    public void setTournament(DotaTournament tournament) {
        this.tournament = tournament;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ArrayList<DotaGame> getGameList() {
        return gameList;
    }

    public DotaTeam getTeam1() {
        return team1;
    }

    public DotaTeam getTeam2() {
        return team2;
    }

    public ArrayList<DotaPlayer> getTeam1PlayerList() {
        return team1PlayerList;
    }

    public ArrayList<DotaPlayer> getTeam2PlayerList() {
        return team2PlayerList;
    }

    public int getScoreTeam1() {
        return scoreTeam1;
    }

    public int getScoreTeam2() {
        return scoreTeam2;
    }

    public DotaTournament getTournament() {
        return tournament;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "DotaMatch{" +
                "gameList=" + gameList +
                ", team1=" + team1 +
                ", team2=" + team2 +
                ", team1PlayerList=" + team1PlayerList +
                ", team2PlayerList=" + team2PlayerList +
                ", scoreTeam1=" + scoreTeam1 +
                ", scoreTeam2=" + scoreTeam2 +
                ", tournament=" + tournament +
                ", dateAndTime='" + dateAndTime + '\'' +
                '}';
    }
}
