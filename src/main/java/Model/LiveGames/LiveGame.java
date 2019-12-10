package Model.LiveGames;

import java.util.ArrayList;
import java.util.Objects;

public class LiveGame {

    private ArrayList<String> picks;
    private String teams;

    public LiveGame(ArrayList<String> picks, String teams) {
        this.picks = picks;
        this.teams = teams;
    }

    public LiveGame(String teams) {
        this.teams = teams;
    }

    public LiveGame() {
    }

    public ArrayList<String> getPicks() {
        return picks;
    }

    public String getTeams() {
        return teams;
    }

    public void setPicks(ArrayList<String> picks) {
        this.picks = picks;
    }

    public void setTeams(String teams) {
        this.teams = teams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiveGame liveGame = (LiveGame) o;
        return getPicks().equals(liveGame.getPicks()) &&
                getTeams().equals(liveGame.getTeams());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPicks(), getTeams());
    }
}
