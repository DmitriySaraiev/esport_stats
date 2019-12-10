package Model.Dota;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DotabuffParser{

    private Document mainDoc;
    private ArrayList<Document> gameDocList;
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";
    private static final String refferer = "http://www.google.com";
    
    public DotabuffParser(String url) {
        try {
            mainDoc = Jsoup.connect(url).userAgent(userAgent).referrer(refferer).get();
            gameDocList = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DotabuffParser() {
        super();
    }
    
    protected int[] parseGameScore(Document document) {
        int[] score = new int[2];
        score[0] = Integer.valueOf(document.select("span.the-radiant.score").text());
        score[1] = Integer.valueOf(document.select("span.the-dire.score").text());
        return score;
    }
    
    protected int[] parseMatchScore() {
        int[] score = new int[2];
        Element element = mainDoc.selectFirst("span.mini-score");
        if(element == null){
            score[0] = 0;
            score[1] = 0;
        }
        else{
            String scoreString = element.text().replaceAll("\\D*", "");
            score[0] = Character.getNumericValue(scoreString.charAt(0));
            score[1] = Character.getNumericValue(scoreString.charAt(1));
        }
        return score;
    }

    protected DotaTournament parseTournament() {
        Element element = mainDoc.selectFirst("span.league-text-full");
        return new DotaTournament(element.text());
    }



    protected ArrayList<DotaTeam> parseTeams() {
        ArrayList<DotaTeam> teams = new ArrayList<>();
        Elements elements = mainDoc.select("span.team-text-full");
        for (int i = 0; i < 2; i++) {
            if(elements.get(i).text().contains("Team Σ"))
                teams.add(new DotaTeam("NoNameTeam"));
            else
                teams.add(new DotaTeam(elements.get(i).text()));
        }
        return teams;
    }

    protected ArrayList<String> parsePicks(Document doc) {
        int pickCount = 10; //5*2
        ArrayList<String> pickList = new ArrayList<>();
        Elements elements = doc.select("div.pick").select("img");
        for (int i = 0; i < pickCount; i++)
            pickList.add(elements.get(i).attr("alt"));
        return pickList;
    }

    protected ArrayList<String> parseBans(Document doc) {
        int banCount = 12;  //6*2
        ArrayList<String> banList = new ArrayList<>();
        Elements elements = doc.select("div.ban").select("img");
        for (int i = 0; i < banCount; i++)
            banList.add(elements.get(i).attr("alt"));
        return banList;
    }

    /*protected DotaGame parseGame(int gameNumber, DotaMatch match) {
        Document doc = gameDocList.get(gameNumber);
        boolean isTeam1Radiant = false;
        Elements radiantTeam = doc.select("section.radiant").select("span.team-text.team-text-full");
        if (radiantTeam.first().text().equals(match.getTeam1().getName())) {
            isTeam1Radiant = true;
        }
        boolean isWinnerRadiant = isWinnerTeamRadiant(doc);
        HashMap<String, DotaPlayerPerformance> team1PlayersAndPerformances = new HashMap<>();
        HashMap<String, DotaPlayerPerformance> team2PlayersAndPerformances = new HashMap<>();

        for (DotaPlayer dotaPlayer : match.getTeam1PlayerList()) {
            if (!dotaPlayer.getMatchNick().contains("…")) //it's not a "..." it's a "…"
                team1PlayersAndPerformances.put(dotaPlayer.getMatchNick(), null);
            else {
                team1PlayersAndPerformances.put(dotaPlayer.getNick(), null);
                dotaPlayer.setMatchNick(dotaPlayer.getNick());
            }
        }
        for (DotaPlayer dotaPlayer : match.getTeam2PlayerList()) {
            if (!dotaPlayer.getMatchNick().contains("…"))     //it's not a "..." it's a "…"
                team2PlayersAndPerformances.put(dotaPlayer.getMatchNick(), null);
            else {
                team2PlayersAndPerformances.put(dotaPlayer.getNick(), null);
                dotaPlayer.setMatchNick(dotaPlayer.getNick());
            }
        }
        int[] gameScore = parseGameScore(doc);
        ArrayList<String> picks = parsePicks(doc);
        ArrayList<String> bans = parseBans(doc);
        ArrayList<String> team1Picks = new ArrayList<>();
        ArrayList<String> team2Picks = new ArrayList<>();
        ArrayList<String> team1Bans = new ArrayList<>();
        ArrayList<String> team2Bans = new ArrayList<>();
        ArrayList<DotaPlayerPerformance> team1Performance = new ArrayList<>();
        ArrayList<DotaPlayerPerformance> team2Performance = new ArrayList<>();
        ArrayList<DotaPlayerPerformance> teamsPerformances = parcePlayerPerformances(doc);
        if (isTeam1Radiant) {
            for (int i = 0; i < 5; i++) {
                team1Picks.add(picks.get(i));
                team2Picks.add(picks.get(i + 5));
                team1Performance.add(teamsPerformances.get(i));
                team2Performance.add(teamsPerformances.get(i + 5));
            }
            for (int i = 0; i < 6; i++) {
                team1Bans.add(bans.get(i));
                team2Bans.add(bans.get(i + 6));
            }
            for (DotaPlayerPerformance dotaPlayerPerformance : team1Performance)
                team1PlayersAndPerformances.put(dotaPlayerPerformance.getPlayerNick(), dotaPlayerPerformance);
            for (DotaPlayerPerformance dotaPlayerPerformance : team2Performance)
                team2PlayersAndPerformances.put(dotaPlayerPerformance.getPlayerNick(), dotaPlayerPerformance);
        } else {
            for (int i = 0; i < 5; i++) {
                team1Picks.add(picks.get(i + 5));
                team2Picks.add(picks.get(i));
                team1Performance.add(teamsPerformances.get(i + 5));
                team2Performance.add(teamsPerformances.get(i));
            }
            for (int i = 0; i < 6; i++) {
                team1Bans.add(bans.get(i + 6));
                team2Bans.add(bans.get(i));
            }
            for (DotaPlayerPerformance dotaPlayerPerformance : team1Performance)
                team1PlayersAndPerformances.put(dotaPlayerPerformance.getPlayerNick(), dotaPlayerPerformance);
            for (DotaPlayerPerformance dotaPlayerPerformance : team2Performance)
                team2PlayersAndPerformances.put(dotaPlayerPerformance.getPlayerNick(), dotaPlayerPerformance);
        }

        double duration = parseGameDuration(doc);
        return new DotaGame(gameScore[0], gameScore[1], gameDocList.get(gameNumber).location(), "", match,
                team1Picks, team2Picks, team1Bans, team2Bans,
                match.getTeam1PlayerList(), match.getTeam2PlayerList(),
                team1PlayersAndPerformances, team2PlayersAndPerformances,
                duration, isTeam1Radiant, isWinnerRadiant);
    }*/

    protected DotaGame parseGameLimited(int gameNumber, DotaMatch match) {
        Document doc = gameDocList.get(gameNumber);
        boolean isTeam1Radiant = false;
        Elements radiantTeam = doc.select("section.radiant").select("span.team-text.team-text-full");
        if (radiantTeam.isEmpty())
            isTeam1Radiant = true;
        else
            if(radiantTeam.first().text().equals(match.getTeam1().getName())) {
            isTeam1Radiant = true;
        }
        boolean isWinnerRadiant = isWinnerTeamRadiant(doc);
        int[] gameScore = parseGameScore(doc);
        ArrayList<String> picks = parsePicks(doc);
        ArrayList<String> team1Picks = new ArrayList<>();
        ArrayList<String> team2Picks = new ArrayList<>();
        if (isTeam1Radiant) {
            for (int i = 0; i < 5; i++) {
                team1Picks.add(picks.get(i));
                team2Picks.add(picks.get(i + 5));
            }
        } else {
            for (int i = 0; i < 5; i++) {
                team1Picks.add(picks.get(i + 5));
                team2Picks.add(picks.get(i));
            }
        }
        double duration = parseGameDuration(doc);
        return new DotaGame(gameScore[0], gameScore[1], gameDocList.get(gameNumber).location(), match,
                team1Picks, team2Picks,
                isTeam1Radiant, isWinnerRadiant, duration);
    }

    public DotaMatch parseMatchLimited() throws HttpStatusException {
        DotaMatch match = new DotaMatch();
        ArrayList<DotaTeam> teams = parseTeams();
        match.setTeam1(teams.get(0));
        match.setTeam2(teams.get(1));
        ArrayList<String> gamesLinkList = parseGameLinks();
        try {
            for (String url : gamesLinkList) {
                gameDocList.add(Jsoup.connect(url).referrer(refferer).userAgent(userAgent).get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<DotaGame> gamesList = new ArrayList<>();
        for (int i = 0; i < gamesLinkList.size(); i++) {
            gamesList.add(parseGameLimited(i, match));
        }
        match.setGameList(gamesList);
        match.setTournament(parseTournament());
        int[] matchScore = parseMatchScore();
        match.setScoreTeam1(matchScore[0]);
        match.setScoreTeam2(matchScore[1]);
        match.setDateAndTime(parseDateAndTime());
        match.setLink(mainDoc.location());
        return match;
    }

    private double parseGameDuration(Document document) {
        double duration = 0;
        Element durationElement = document.select("dl").select("dd").get(3);
        if(!durationElement.text().contains(":"))
            durationElement = document.select("dl").select("dd").get(4);
        String[] durationStringArray = durationElement.text().split(":");
        if (durationStringArray.length == 3) {
            duration = Double.valueOf(durationStringArray[0]) * 60 + Double.valueOf(durationStringArray[1]) + (Double.valueOf(durationStringArray[2]) / 100);
        } else if (durationStringArray.length == 2) {
            duration = Double.valueOf(durationStringArray[0]) + (Double.valueOf(durationStringArray[1]) / 100);
        } else if (durationStringArray.length == 1) {
            duration = (Double.valueOf(durationStringArray[0]) / 100);
        }
        return duration;
    }

    private String parseDateAndTime() {
        return mainDoc.selectFirst("time").attr("datetime").replace("T", " ").replaceAll("\\+.*", "");
    }


    private ArrayList<String> parseGameLinks() {
        ArrayList<String> gamelist = new ArrayList<>();
        Elements elements = mainDoc.select("div.match-link").select("a");
        for (Element element : elements)
            gamelist.add(element.attr("abs:href"));
        return gamelist;
    }

    private ArrayList<String> parsePlayersLinks() {
        ArrayList<String> playersLinkList = new ArrayList<>();
        Elements playerLinks = gameDocList.get(0).select("a.link-type-player");
        if (playerLinks.size() == 0) {
            playerLinks = gameDocList.get(0).select("a.esports-link.player-link");
            Elements elementstmp = new Elements();
            for (int i = 0; i < playerLinks.size(); i++) {
                if (i % 2 == 0)
                    elementstmp.add(playerLinks.get(i));
            }
            playerLinks = elementstmp;
        }
        for (Element element : playerLinks) {
            playersLinkList.add(element.attr("abs:href"));
        }
        return playersLinkList;
    }

    public ArrayList<String> parseAllTournamentMatchesLinks(String url, int start, int end) {  //start from 1 end is included
        ArrayList<String> tournamentMatchesLinkList = new ArrayList<>();
        boolean isFirstPage = true;
        int startCounter = 0;
        int numberOflinksParsed  = 0;
        int numberOflinksNeeded = 1000000;
        if(end != -1)
            numberOflinksNeeded = end - start + 1;
        try {
            Document document;
            if (start > 20) {
                document = Jsoup.connect(url + "?page=" + (start / 20 + 1)).userAgent(userAgent).referrer(refferer).get();
                start = start - ((start / 20) * 20);
            } else
                document = Jsoup.connect(url).userAgent(userAgent).referrer(refferer).get();
            while (true) {
                Elements links = document.select("a[title]");
                for (Element link : links) {
                    if (link.attr("rel").equals("")) {
                        if (isFirstPage)
                            if (++startCounter < start)
                                continue;
                        tournamentMatchesLinkList.add(link.attr("abs:href"));
                        numberOflinksParsed++;
                        if (numberOflinksParsed == numberOflinksNeeded)
                            return tournamentMatchesLinkList;
                    }
                }
                Elements nextPage = document.select("span.next");
                if (!nextPage.isEmpty()) {
                    isFirstPage = false;
                    String nextUrl = nextPage.select("a").attr("abs:href");
                    document = Jsoup.connect(nextUrl).referrer(refferer).userAgent(userAgent).get();
                } else
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tournamentMatchesLinkList;
    }

    private ArrayList<String> parseMatchNicks() {
        ArrayList<String> matchNickList = new ArrayList<>();
        Element linkToFirstGame = mainDoc.selectFirst("div.match-link").selectFirst("a");
        Document documentWithMatchNicks = gameDocList.get(0);
        Elements matchNicks = documentWithMatchNicks.select("a.link-type-player");
        if (matchNicks.isEmpty())
            matchNicks = documentWithMatchNicks.select("span.player-text.player-text-full");
        for (Element element : matchNicks) {
            matchNickList.add(element.text());
        }
        return matchNickList;

    }

    private DotaPlayer parsePlayer(String url, String matchNick) {
        try {

            Document doc = Jsoup.connect(url).userAgent(userAgent).referrer(refferer).get();
            Element element = doc.selectFirst("div.header-content-title").selectFirst("h1");
            String[] names = element.toString().replaceAll("\\s[<a].*", "").replaceAll("\n", "").replaceAll("<h1>", "").replaceAll("</h1>","").split("<small>");
            if (names.length == 2) {
                if (names[1].indexOf("Overview") == 0)
                    return new DotaPlayer(names[0], matchNick, "no name");
                else
                    return new DotaPlayer(names[0], matchNick, names[1]);
            } else
                return new DotaPlayer(names[0], matchNick, "no name");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DotaPlayer("default", "default", "default");
    }

    /*private ArrayList<DotaPlayerPerformance> parcePlayerPerformances(Document doc) {
        String url = doc.location() + "/farm";
        ArrayList<DotaPlayerPerformance> playerPerformances = new ArrayList<>();
        try {

            Document doc2 = Jsoup.connect(url).referrer(refferer).userAgent(userAgent).get();
            int shift1 = 0;
            Elements nicks = doc.select("a.link-type-player");
            if (nicks.size() == 0) {
                nicks = doc.select("a.esports-player");
                Elements tmpElements = new Elements();
                for (int i = 0; i < nicks.size(); i++) {
                    if (i % 2 == 0)
                        tmpElements.add(nicks.get(i));
                }
                nicks = tmpElements;
            }
            Elements kda = doc.select("td.r-group-1");
            Elements networthElements = doc2.select("td.r-group-1");
            Elements lh_dn = doc.select("td.r-tab.r-group-2");
            Elements gpm_xpm = doc2.select("td.r-group-2");
            Elements dmg_heal_strdmg = doc.select("td.r-group-3");
            Elements obs = doc.select("td.r-group-4").select("span.color-item-observer-ward");
            Elements sntr = doc.select("td.r-group-4").select("span.color-item-sentry-ward");
            Elements items_timings = doc.select("td.tf-pl.r-group-4");
            String playerNick;
            int kills;
            int deaths;
            int assists;
            int networth;
            int lastHits;
            int denies;
            int gpm;
            int xpm;
            int heroDamage;
            int healing;
            int structureDamage;
            int observerWards;
            int sentryWards;
            ArrayList<String> itembuild = new ArrayList<>();
            ArrayList<Integer> itembuildTiming = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                if (i >= 5)
                    shift1 = 1;
                playerNick = nicks.get(i).text();
                if (playerNick.charAt(playerNick.length() - 1) == ' ')
                    playerNick = playerNick.substring(0, playerNick.length() - 1);
                kills = Integer.valueOf(kda.get((i + shift1) * 4).text().replace("-", "0"));
                deaths = Integer.valueOf(kda.get((i + shift1) * 4 + 1).text().replace("-", "0"));
                assists = Integer.valueOf(kda.get((i + shift1) * 4 + 2).text().replace("-", "0"));
                networth = Integer.valueOf(networthElements.get((i + shift1) * 3).text().replace(",", ""));
                lastHits = Integer.valueOf(lh_dn.get((i + shift1) * 6).text().replace("-", "0").replace(".", "").replace("k","000"));
                denies = Integer.valueOf(lh_dn.get((i + shift1) * 6 + 2).text().replace("-", "0"));
                gpm = Integer.valueOf(gpm_xpm.get((i + shift1) * 3).text().replaceAll("\\s.*", "").replace(",", ""));
                xpm = Integer.valueOf(gpm_xpm.get((i + shift1) * 3 + 1).text().replaceAll("\\s.*", "").replace(",", ""));
                heroDamage = Integer.valueOf(dmg_heal_strdmg.get((i + shift1) * 3).text().replace("k", "00").replace(".", ""));
                healing = Integer.valueOf(dmg_heal_strdmg.get((i + shift1) * 3 + 1).text().replace("-", "0").replace("k", "00").replace(".", ""));
                structureDamage = Integer.valueOf(dmg_heal_strdmg.get((i + shift1) * 3 + 2).text().replace("-", "0").replace("k", "00").replace(".", ""));
                observerWards = Integer.valueOf(obs.get(i + shift1).text().replace("-", "0"));
                sentryWards = Integer.valueOf(sntr.get(i + shift1).text().replace("-", "0"));
                Elements itemElements = items_timings.get(i).select("img");
                Elements timingElements = items_timings.get(i).select("span.overlay-text.bottom.left");
                for (Element element : itemElements)
                    itembuild.add(element.attr("title"));
                for (Element element : timingElements)
                    itembuildTiming.add(Integer.valueOf(element.text().replace("m", "")));
                playerPerformances.add(new DotaPlayerPerformance(playerNick, kills, deaths, assists, networth, lastHits, denies, gpm, xpm, heroDamage, healing,
                        structureDamage, observerWards, sentryWards, new ArrayList<>(itembuild), new ArrayList<>(itembuildTiming)));
                itembuild.clear();
                itembuildTiming.clear();
            }
            return playerPerformances;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerPerformances;
    }*/

    private boolean isTeam1Radiant(ArrayList<String> gamesLinkList, DotaMatch dotaMatch) {
        Document document = gameDocList.get(0);
        String team1 = document.selectFirst("section.radiant").selectFirst("span.team-text.team-text-full").text();
        return dotaMatch.getTeam1().getName().contains(team1) || team1.contains(dotaMatch.getTeam1().getName());

    }

    public String[] parseLivePicks(String url, int gameNumber) {              //Parse from dota2.ru
        String[] picks = new String[2];
        StringBuilder radiantPickSb = new StringBuilder();
        StringBuilder direPickSb = new StringBuilder();
        try {
            Document document = Jsoup.connect(url).userAgent(userAgent).referrer(refferer).validateTLSCertificates(false).get();
            Elements pickElements = document.select("div.esport-match-view-map-single-side-picks-single-info");
            for (int i = (gameNumber - 1) * 10; i < (gameNumber - 1) * 10 + 5; i++) {
                if (i == (gameNumber - 1) * 10 + 5 - 1) {
                    radiantPickSb.append("'").append(pickElements.get(i).text().replaceAll("'", "''")).append("'");
                    direPickSb.append("'").append(pickElements.get(i + 5).text().replaceAll("'", "''")).append("'");
                } else {
                    radiantPickSb.append("'").append(pickElements.get(i).text().replaceAll("'", "''")).append("', ");
                    direPickSb.append("'").append(pickElements.get(i + 5).text().replaceAll("'", "''")).append("', ");
                }
            }
            picks[0] = radiantPickSb.toString();
            picks[1] = direPickSb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picks;
    }

    private boolean isWinnerTeamRadiant(Document gameDocument){
            Element winnerElement = gameDocument.selectFirst("section.radiant").selectFirst("span.victory-icon");
            return (winnerElement != null);
    }

    /*public void saveHeroImages() {
        try {
            Document document = Jsoup.connect("https://dota2-ru.gamepedia.com/Dota_2_%D0%92%D0%B8%D0%BA%D0%B8").get();
            Elements elements = document.select("div.heroentry").select("a");
            ArrayList<String> links = new ArrayList<>();
            ArrayList<String> heronames = new ArrayList<>();
            for (Element element : elements) {
                Element linkElement = element.select("img").first();
                links.add(linkElement.attr("src"));
                heronames.add(element.attr("title").replaceAll(" ", "_"));
            }
            BufferedImage image;
            for (int i = 0; i < links.size(); i++) {
                URL url = new URL(links.get(i));
                image = ImageIO.read(url);
                if (image != null) {
                    File file = new File("F:\\Java_projects\\esportStats\\res\\img\\dota\\heroes\\" + heronames.get(i) + ".png");
                    if (!file.exists())
                        file.createNewFile();
                    ImageIO.write(image, "png", file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*public void saveAbilityImages() {
        try {
            Document document = Jsoup.connect("https://dota2-ru.gamepedia.com/Dota_2_%D0%92%D0%B8%D0%BA%D0%B8").get();
            ArrayList<String> heroesLinks = new ArrayList<>();
            ArrayList<String> imagesLinks = new ArrayList<>();
            ArrayList<String> abilityNames = new ArrayList<>();
            Elements elements = document.select("div.heroentry").select("a");
            for (Element element : elements)
                heroesLinks.add(element.attr("abs:href"));
            for (String url : heroesLinks) {
                document = Jsoup.connect(url).ignoreContentType(true).get();
                Elements activeAbilities = document.select("div.ico_active").select("img");
                Elements passiveAbilities = document.select("div.ico_passive").select("img");
                for (Element element : activeAbilities) {
                    imagesLinks.add(element.attr("src"));
                    abilityNames.add(element.attr("alt").replaceAll("icon", "").replaceAll(".png", ""));
                }
                for (Element element : passiveAbilities) {
                    imagesLinks.add(element.attr("src"));
                    abilityNames.add(element.attr("alt").replaceAll("icon", ""));
                }
            }
            BufferedImage image;
            for (int i = 0; i < imagesLinks.size(); i++) {
                URL url = new URL(imagesLinks.get(i));
                image = ImageIO.read(url);
                if (image != null) {
                    File file = new File("F:\\Java_projects\\esportStats\\res\\img\\dota\\abilities\\" + abilityNames.get(i) + ".png");
                    if (!file.exists())
                        file.createNewFile();
                    ImageIO.write(image, "png", file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
