package Model.Dota;

public class DotaPlayer {

    private String nick;
    private String matchNick;
    private String name;

    public DotaPlayer(String nick, String matchNick, String name) {
        this.nick = nick;
        this.matchNick = matchNick;
        this.name = name;
    }

    public DotaPlayer(String nick, String matchNick) {
        this.nick = nick;
        this.matchNick = matchNick;
    }

    public String getNick() {
        return nick;
    }

    public String getMatchNick() {
        return matchNick;
    }

    public String getName() {
        return name;
    }

    public void setMatchNick(String matchNick) {
        this.matchNick = matchNick;
    }

    @Override
    public String toString() {
        return "DotaPlayer{" +
                "nick='" + nick + '\'' +
                ", matchNick='" + matchNick + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
