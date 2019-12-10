package Model.Dota;

public class DotaTournament {

    private String name;

    public DotaTournament(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DotaTournament{" +
                "name='" + name + '\'' +
                '}';
    }
}
