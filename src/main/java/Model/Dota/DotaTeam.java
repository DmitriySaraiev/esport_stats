package Model.Dota;

import java.awt.image.BufferedImage;

public class DotaTeam {

    private String name;
    private BufferedImage image;

    public DotaTeam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DotaTeam{" +
                "name='" + name + '\'' +
                ", image=" + image +
                '}';
    }
}
