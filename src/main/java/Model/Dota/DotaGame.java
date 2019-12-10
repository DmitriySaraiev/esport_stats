package Model.Dota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DotaGame {

    private int scoreTeam1;
    private int scoreTeam2;
    private String link;
    private DotaMatch match;
    private ArrayList<String> team1Picks;
    private ArrayList<String> team2Picks;
    private boolean isTeam1Radiant;
    private boolean isWinnerRadiant;
    private double duration;
    private double patch;
    boolean isLeftWinner;
    private String team1Name;
    private String team2Name;


    public DotaGame() {
    }

    public DotaGame(int scoreTeam1, int scoreTeam2, String link, DotaMatch match,
                    ArrayList<String> team1Picks, ArrayList<String> team2Picks,

                    double duration, boolean isTeam1Radiant, boolean isWinnerRadiant) {
        this.scoreTeam1 = scoreTeam1;
        this.scoreTeam2 = scoreTeam2;
        this.link = link;
        this.match = match;
        this.team1Picks = team1Picks;
        this.team2Picks = team2Picks;
        this.duration = duration;
        this.isTeam1Radiant = isTeam1Radiant;
        this.isWinnerRadiant = isWinnerRadiant;
    }

    public DotaGame(int scoreTeam1, int scoreTeam2, String link, DotaMatch match,
                    ArrayList<String> team1Picks, ArrayList<String> team2Picks,
                    boolean isTeam1Radiant, boolean isWinnerRadiant, double duration) {
        this.scoreTeam1 = scoreTeam1;
        this.scoreTeam2 = scoreTeam2;
        this.link = link;
        this.match = match;
        this.team1Picks = team1Picks;
        this.team2Picks = team2Picks;
        this.isTeam1Radiant = isTeam1Radiant;
        this.isWinnerRadiant = isWinnerRadiant;
        this.duration = duration;
    }

    public DotaGame(int scoreTeam1, int scoreTeam2, String link, ArrayList<String> team1Picks, ArrayList<String> team2Picks,
                    boolean isTeam1Radiant, boolean isWinnerRadiant, double duration, double patch, String team1Name,
                    String team2Name, boolean isLeftWinner) {
        this.scoreTeam1 = scoreTeam1;
        this.scoreTeam2 = scoreTeam2;
        this.link = link;
        this.team1Picks = team1Picks;
        this.team2Picks = team2Picks;
        this.isTeam1Radiant = isTeam1Radiant;
        this.isWinnerRadiant = isWinnerRadiant;
        this.duration = duration;
        this.patch = patch;
        this.team1Name = team1Name;
        this.team2Name = team2Name;
        this.isLeftWinner = isLeftWinner;
    }

    public boolean isTeam1Radiant() {
        return isTeam1Radiant;
    }

    public int getScoreTeam1() {
        return scoreTeam1;
    }

    public int getScoreTeam2() {
        return scoreTeam2;
    }

    public String getLink() {
        return link;
    }

    public DotaMatch getMatch() {
        return match;
    }

    public ArrayList<String> getTeam1Picks() {
        return team1Picks;
    }

    public ArrayList<String> getTeam2Picks() {
        return team2Picks;
    }

    public double getDuration() {
        return duration;
    }

    public boolean isWinnerRadiant() {
        return isWinnerRadiant;
    }

    public double getPatch() {
        return patch;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public boolean isLeftWinner() {
        return isLeftWinner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DotaGame dotaGame = (DotaGame) o;
        return getScoreTeam1() == dotaGame.getScoreTeam1() &&
                getScoreTeam2() == dotaGame.getScoreTeam2() &&
                isTeam1Radiant() == dotaGame.isTeam1Radiant() &&
                isWinnerRadiant() == dotaGame.isWinnerRadiant() &&
                Double.compare(dotaGame.getDuration(), getDuration()) == 0 &&
                Double.compare(dotaGame.getPatch(), getPatch()) == 0 &&
                isLeftWinner() == dotaGame.isLeftWinner() &&
                getLink().equals(dotaGame.getLink()) &&
                getMatch().equals(dotaGame.getMatch()) &&
                Objects.equals(getTeam1Picks(), dotaGame.getTeam1Picks()) &&
                Objects.equals(getTeam2Picks(), dotaGame.getTeam2Picks()) &&
                Objects.equals(getTeam1Name(), dotaGame.getTeam1Name()) &&
                Objects.equals(getTeam2Name(), dotaGame.getTeam2Name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScoreTeam1(), getScoreTeam2(), getLink(), getMatch(), getTeam1Picks(), getTeam2Picks(), isTeam1Radiant(), isWinnerRadiant(), getDuration(), getPatch(), isLeftWinner(), getTeam1Name(), getTeam2Name());
    }

    @Override
    public String toString() {
        return "DotaGame{" +
                "scoreTeam1=" + scoreTeam1 +
                ", scoreTeam2=" + scoreTeam2 +
                ", link='" + link + '\'' +
                ", team1Picks=" + team1Picks +
                ", team2Picks=" + team2Picks +
                ", isTeam1Radiant=" + isTeam1Radiant +
                ", duration=" + duration +
                "\n\n" +
                '}';
    }
}
