package Model.Dota;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PickSearcher {

    private DotaDatabase ddb;
    private ArrayList<String> heroList;
    private static final String basicQuery = "SELECT g.team1_pick1, g.team1_pick2, g.team1_pick3, g.team1_pick4, " +
            "g.team1_pick5, g.team2_pick1, g.team2_pick2, g.team2_pick3, g.team2_pick4, g.team2_pick5, " +
            "g.pick1_string, g.pick2_string, g.team1_score, g.team2_score, g.link, g.is_team1_radiant, " +
            "g.is_winner_radiant, g.duration, m.patch, m.team1_name, m.team2_name, m.patch " +
            "FROM game g INNER JOIN esport_db_dota.match m " +
            "ON g.match_link = m.link ";
    private static final String whereQuerry = "WHERE (m.patch >= 7.189 AND ((";
    private static final String orderByQuerry = "" +
            "ORDER BY patch ASC";
    private HashMap<String, CellStyle> styleMap;
    private XSSFWorkbook book;
    private double patch;

    public PickSearcher(ArrayList<String> heroList, DotaDatabase ddb) {
        initializeDataMap();
        this.heroList = heroList;
        this.ddb = ddb;
        book = new XSSFWorkbook();
        this.patch = ddb.getPatch();
    }

    public HashMap<String, ArrayList<DotaGame>> getData() {
        HashMap<String, ArrayList<DotaGame>> dataMap = initializeDataMap();
        ResultSet rs;
        boolean isTeam1Left;
        rs = ddb.findMatchesByPick(getSqlQuerryFor2v1());
        try {
            while (rs.next()) {
                isTeam1Left = true;
                int counter1 = 0;
                int counter2 = 0;
                int team1Score;
                int team2Score;
                boolean isTeam1radiant = rs.getInt("is_team1_radiant") == 1;
                boolean isWinnerRadiant = rs.getInt("is_winner_radiant") == 1;
                boolean isLeftWinner;
                float patch = Float.valueOf(rs.getString("patch"));
                String link = rs.getString("link");
                double duration = Double.parseDouble(rs.getString("duration"));
                String team1Name = rs.getString("team1_name");
                String team2Name = rs.getString("team2_name");
                ArrayList<String> team1Pick = new ArrayList<>();
                ArrayList<String> team2Pick = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    team1Pick.add(rs.getString("team1_pick" + i));
                    team2Pick.add(rs.getString("team2_pick" + i));
                }
                for (int i = 0; i < 5; i++) {
                    if (heroList.indexOf(team1Pick.get(i)) >= 0 && heroList.indexOf(team1Pick.get(i)) < 5)
                        counter1++;
                    if (heroList.indexOf(team2Pick.get(i)) >= 5 && heroList.indexOf(team2Pick.get(i)) < 10)
                        counter2++;
                }
                if ((counter1 <= 1 && counter2 <= 1) || (counter1 == 0 || counter2 == 0)) {
                    isTeam1Left = false;
                    counter1 = 0;
                    counter2 = 0;
                    for (int i = 0; i < 5; i++) {
                        if (heroList.indexOf(team2Pick.get(i)) >= 0 && heroList.indexOf(team2Pick.get(i)) < 5)
                            counter1++;
                        if (heroList.indexOf(team1Pick.get(i)) >= 5 && heroList.indexOf(team1Pick.get(i)) < 10)
                            counter2++;
                    }
                }
                if (isTeam1Left) {
                    if ((isWinnerRadiant && isTeam1radiant || (!isWinnerRadiant && !isTeam1radiant)))
                        isLeftWinner = true;
                    else
                        isLeftWinner = false;
                } else {
                    if ((!isWinnerRadiant && !isTeam1radiant) || (isWinnerRadiant && isTeam1radiant))
                        isLeftWinner = false;
                    else
                        isLeftWinner = true;
                }
                if ((isLeftWinner && isTeam1radiant) || (!isLeftWinner && isTeam1radiant)) {
                    team1Score = rs.getInt("team1_score");
                    team2Score = rs.getInt("team2_score");
                } else {
                    team1Score = rs.getInt("team2_score");
                    team2Score = rs.getInt("team1_score");
                }
                if (counter1 < counter2) {
                    int tmp = counter1;
                    counter1 = counter2;
                    counter2 = tmp;
                }
                ArrayList gameList = dataMap.get(String.valueOf(counter1) + String.valueOf(counter2));
                if (gameList == null)
                    continue;
                if (isTeam1Left)
                    gameList.add(new DotaGame(team1Score, team2Score, link, team1Pick, team2Pick, isTeam1radiant, isWinnerRadiant,
                            duration, patch, team1Name, team2Name, isLeftWinner));
                else
                    gameList.add(new DotaGame(team2Score, team1Score, link, team2Pick, team1Pick, isTeam1radiant, isWinnerRadiant,
                            duration, patch, team2Name, team1Name, isLeftWinner));
                ddb.createStatement();
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataMap;
    }

    public File searchByPicksToExcel(HashMap<String, ArrayList<DotaGame>> data, long chatId) {

        File fdir = new File("users/" + chatId);
        fdir.mkdirs();
        File f = new File("users/" + chatId + "/stats.xlsx");
        createStylesForExcelDocument();
        Sheet sheet = book.createSheet("Esport Stats");
        HashMap<String, int[]> heroesWinrate = new HashMap<>();

        int leftWinsCounter;
        int rightWinsCounter;

        for (int i = 0; i < heroList.size(); i++) {
            heroesWinrate.put(heroList.get(i), new int[]{0, 0, 0, 0});
        }
        int rowCounter = 0;
        int cellCounter = 0;
        int leftWins = 0;
        int rightWins = 0;
        ArrayList<String> entryList = new ArrayList<>();
        entryList.add("55");
        entryList.add("54");
        entryList.add("53");
        entryList.add("52");
        entryList.add("51");
        entryList.add("44");
        entryList.add("43");
        entryList.add("42");
        entryList.add("41");
        entryList.add("33");
        entryList.add("32");
        entryList.add("31");
        entryList.add("22");
        entryList.add("21");
        Row row = sheet.createRow(rowCounter++);
        Cell cell;
        for (int i = 0; i < 11; i++) {
            cell = row.createCell(cellCounter++);
            if (i == 5)
                continue;
            if (i < 5)
                cell.setCellValue("Пик " + (i + 1));
            else
                cell.setCellValue("Пик " + (i));
        }
        cell = row.createCell(cellCounter++);
        cell.setCellValue("Команда 1");
        cell = row.createCell(cellCounter++);
        cell.setCellValue("Команда 2");
        cell = row.createCell(cellCounter++);
        cell.setCellValue("Счет 1");
        cell = row.createCell(cellCounter++);
        cell.setCellValue("Счет 2");
        cell = row.createCell(cellCounter++);
        cell.setCellValue("Ссылка");
        cell = row.createCell(cellCounter++);
        cell.setCellValue("Продолжительность");
        cell = row.createCell(cellCounter++);
        cell.setCellValue("Патч");

        for (String entryString : entryList) {
            leftWinsCounter = 0;
            rightWinsCounter = 0;
            ArrayList dotaGameArrayList = data.get(entryString);
            if (dotaGameArrayList.size() != 0) {
                int[][][] heroVsHeroWinrateAllMatrix = new int[5][5][2];
                int[][][] heroVsHeroWinrateThisPatchMatrix = new int[5][5][2];
                if (dotaGameArrayList.size() == 0)
                    continue;
                cellCounter = 0;
                row = sheet.createRow(rowCounter++);
                cell = row.createCell(cellCounter);
                cell.setCellStyle(styleMap.get("vsStyle"));
                cell.setCellValue(entryString.charAt(0) + " vs " + entryString.charAt(1));
                for (DotaGame game : (ArrayList<DotaGame>) dotaGameArrayList) {
                    boolean isPatch = game.getPatch() >= (patch - 0.0001);
                    ArrayList<String> matchedHeroLeftList = new ArrayList<>();
                    ArrayList<String> matchedHeroRightList = new ArrayList<>();
                    cellCounter = 0;
                    row = sheet.createRow(rowCounter++);
                    for (String pick : game.getTeam1Picks()) {
                        cell = row.createCell(cellCounter++);
                        if (heroList.indexOf(pick) >= 0 && heroList.indexOf(pick) < 5) {
                            matchedHeroLeftList.add(pick);
                            cell.setCellStyle(styleMap.get("matchedHeroStyle"));
                            int[] wins = heroesWinrate.get(pick);
                            wins[0]++;
                            if (game.isLeftWinner()) {
                                wins[1]++;
                            }
                            if (isPatch) {
                                wins[2]++;
                                if (game.isLeftWinner()) {
                                    wins[3]++;
                                }
                            }
                        }
                        cell.setCellValue(pick);
                    }
                    cell = row.createCell(cellCounter++);
                    cell.setCellStyle(styleMap.get("vsStyle"));
                    for (String pick : game.getTeam2Picks()) {
                        cell = row.createCell(cellCounter++);
                        if (heroList.indexOf(pick) >= 5 && heroList.indexOf(pick) < 10) {
                            matchedHeroRightList.add(pick);
                            cell.setCellStyle(styleMap.get("matchedHeroStyle"));
                            int[] wins = heroesWinrate.get(pick);
                            wins[0]++;
                            if (!game.isLeftWinner()) {
                                wins[1]++;
                            }
                            if (isPatch) {
                                wins[2]++;
                                if (!game.isLeftWinner()) {
                                    wins[3]++;
                                }
                            }
                        }
                        cell.setCellValue(pick);
                    }
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(game.getTeam1Name());
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(game.getTeam2Name());
                    cell = row.createCell(cellCounter++);
                    if (game.isLeftWinner())
                        cell.setCellStyle(styleMap.get("scoreStyle"));
                    cell.setCellValue(game.getScoreTeam1());
                    cell = row.createCell(cellCounter++);
                    if (!game.isLeftWinner())
                        cell.setCellStyle(styleMap.get("scoreStyle"));
                    cell.setCellValue(game.getScoreTeam2());
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(game.getLink());
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(game.getDuration() + "m");
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(game.getPatch());
                    if (game.isLeftWinner)
                        leftWins++;
                    else
                        rightWins++;
                    for (int i = 0; i < matchedHeroLeftList.size(); i++) {
                        for (int j = 0; j < matchedHeroRightList.size(); j++) {
                            if (game.isLeftWinner) {
                                heroVsHeroWinrateAllMatrix[heroList.indexOf(matchedHeroLeftList.get(i))][heroList.indexOf(matchedHeroRightList.get(j)) - 5][1]++;
                                if (isPatch)
                                    heroVsHeroWinrateThisPatchMatrix[heroList.indexOf(matchedHeroLeftList.get(i))][heroList.indexOf(matchedHeroRightList.get(j)) - 5][1]++;
                            }
                            heroVsHeroWinrateAllMatrix[heroList.indexOf(matchedHeroLeftList.get(i))][heroList.indexOf(matchedHeroRightList.get(j)) - 5][0]++;
                            if (isPatch)
                                heroVsHeroWinrateThisPatchMatrix[heroList.indexOf(matchedHeroLeftList.get(i))][heroList.indexOf(matchedHeroRightList.get(j)) - 5][0]++;
                        }
                    }
                    if (game.isLeftWinner) {
                        leftWinsCounter++;
                    } else {
                        rightWinsCounter++;
                    }
                }

                cellCounter = 12;
                row = sheet.createRow(rowCounter++);
                cell = row.createCell(cellCounter++);
                cell.setCellValue("Итого");
                cell = row.createCell(cellCounter++);
                cell.setCellStyle(styleMap.get("scoreStyle"));
                cell.setCellValue(leftWins);
                cell = row.createCell(cellCounter++);
                cell.setCellStyle(styleMap.get("scoreStyle"));
                cell.setCellValue(rightWins);
                leftWins = 0;
                rightWins = 0;
                CellStyle cellStyle1;
                CellStyle cellStyle2;
                CellStyle cellStyle3;
                row = sheet.createRow(rowCounter++);
                cellCounter = 1;
                cell = row.createCell(cellCounter++);
                cell.setCellValue("Побед");
                cell = row.createCell(cellCounter++);
                cell.setCellValue("Поражений");
                cell = row.createCell(cellCounter++);
                cell.setCellValue("Винрейт");
                cellCounter = 7;
                cell = row.createCell(cellCounter++);
                cell.setCellValue("Побед");
                cell = row.createCell(cellCounter++);
                cell.setCellValue("Поражений");
                cell = row.createCell(cellCounter++);
                cell.setCellValue("Винрейт");
                for (int i = 0; i < 5; i++) {
                    switch (i) {
                        case 0:
                            cellStyle1 = styleMap.get("topLeftBorderStyle");
                            cellStyle2 = styleMap.get("topBorderStyle");
                            cellStyle3 = styleMap.get("topRightBorderStyle");
                            break;
                        case 4:
                            cellStyle1 = styleMap.get("bottomLeftBorderStyle");
                            cellStyle2 = styleMap.get("bottomBorderStyle");
                            cellStyle3 = styleMap.get("bottomRightBorderStyle");
                            break;
                        default:
                            cellStyle1 = styleMap.get("leftBorderStyle");
                            cellStyle2 = book.createCellStyle();
                            cellStyle3 = styleMap.get("rightBorderStyle");
                            break;
                    }
                    row = sheet.createRow(rowCounter++);
                    cellCounter = 0;
                    int[] wins;
                    wins = heroesWinrate.get(heroList.get(i));
                    cell = row.createCell(cellCounter++);
                    if (wins[0] != 0)
                        cell.setCellValue(heroList.get(i));
                    cell.setCellStyle(cellStyle1);
                    float winrateAll;
                    float winrateThisPatch;
                    if (wins[1] == 0) {
                        winrateAll = 0;
                        winrateThisPatch = 0;
                    } else {
                        winrateAll = (((float) wins[1] / (float) wins[0]) * 100);
                        winrateThisPatch = (wins[3] == 0) ? 0 : (((float) wins[3] / (float) wins[2]) * 100);
                    }
                    cell = row.createCell(cellCounter++);
                    if (wins[0] != 0) {
                        String winrateString;
                        if (wins[2] == 0)
                            winrateString = "--";
                        else
                            winrateString = String.valueOf(wins[3]);
                        cell.setCellValue(wins[1] + " (" + winrateString + ")");
                    }
                    cell.setCellStyle(cellStyle2);
                    cell = row.createCell(cellCounter++);
                    if (wins[0] != 0) {
                        String winrateString;
                        if (wins[2] == 0)
                            winrateString = "--";
                        else
                            winrateString = String.valueOf(wins[2] - wins[3]);
                        cell.setCellValue((wins[0] - wins[1] + " (" + winrateString + ")"));
                    }
                    cell.setCellStyle(cellStyle2);
                    cell = row.createCell(cellCounter++);
                    if (wins[0] != 0) {
                        String winrateString;
                        if (winrateThisPatch == 0)
                            winrateString = "0";
                        else
                            winrateString = String.format("%.1f", winrateThisPatch);
                        cell.setCellValue(String.format("%.1f", winrateAll) + " (" + winrateString + ")");
                    }
                    cell.setCellStyle(cellStyle3);
                    cellCounter = 6;
                    wins = heroesWinrate.get(heroList.get(i + 5));
                    cell = row.createCell(cellCounter++);
                    cell.setCellStyle(cellStyle1);

                    if (wins[0] != 0)
                        cell.setCellValue(heroList.get(i + 5));
                    if (wins[1] == 0) {
                        winrateAll = 0;
                        winrateThisPatch = 0;
                    } else {
                        winrateAll = (((float) wins[1] / (float) wins[0]) * 100);
                        winrateThisPatch = (((float) wins[3] / (float) wins[2]) * 100);
                    }
                    cell = row.createCell(cellCounter++);
                    cell.setCellStyle(cellStyle2);
                    if (wins[0] != 0) {
                        String winrateString;
                        if (wins[2] == 0)
                            winrateString = "--";
                        else
                            winrateString = String.valueOf(wins[3]);
                        cell.setCellValue(wins[1] + " (" + winrateString + ")");
                    }
                    cell = row.createCell(cellCounter++);
                    cell.setCellStyle(cellStyle2);
                    if (wins[0] != 0) {
                        String winrateString;
                        if (wins[2] == 0)
                            winrateString = "--";
                        else
                            winrateString = String.valueOf(wins[2] - wins[3]);
                        cell.setCellValue((wins[0] - wins[1] + " (" + winrateString + ")"));
                    }
                    cell = row.createCell(cellCounter++);
                    cell.setCellStyle(cellStyle3);
                    if (wins[0] != 0) {
                        String winrateString;
                        if (wins[2] == 0)
                            winrateString = "--";
                        else if (winrateThisPatch == 0)
                            winrateString = "0";
                        else
                            winrateString = String.format("%.1f", winrateThisPatch);
                        cell.setCellValue(String.format("%.1f", winrateAll) + " (" + winrateString + ")");
                    }
                }

                cellCounter = 7;
                rowCounter++;
                row = sheet.createRow(rowCounter++);
                for (int i = 0; i < 5; i++) {
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(heroList.get(i + 5));
                    cell.setCellStyle(styleMap.get("allBorderStyle"));
                }
                for (int i = 0; i < heroVsHeroWinrateAllMatrix[0].length; i++) {
                    cellCounter = 6;
                    row = sheet.createRow(rowCounter++);
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(heroList.get(i));
                    cell.setCellStyle(styleMap.get("allBorderStyle"));
                    for (int j = 0; j < heroVsHeroWinrateAllMatrix[0].length; j++) {
                        int allGames = heroVsHeroWinrateAllMatrix[i][j][0];
                        int wonGames = heroVsHeroWinrateAllMatrix[i][j][1];
                        cell = row.createCell(cellCounter++);
                        cell.setCellStyle(styleMap.get("allBorderStyle"));
                        if (allGames == 0)
                            cell.setCellValue("");
                        else
                            cell.setCellValue((String.format("%.1f", ((float) wonGames / (float) allGames) * 100)) + "% (" + wonGames + "-" + (allGames - wonGames + ")"));
                        cell.setCellStyle(styleMap.get("allBorderStyle"));
                    }
                }


                rowCounter++;
                row = sheet.createRow(rowCounter++);
                cellCounter = 7;
                for (int i = 0; i < 5; i++) {
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(heroList.get(i + 5));
                    cell.setCellStyle(styleMap.get("allBorderStyle"));
                }
                for (int i = 0; i < heroVsHeroWinrateThisPatchMatrix[0].length; i++) {
                    cellCounter = 6;
                    row = sheet.createRow(rowCounter++);
                    cell = row.createCell(cellCounter++);
                    cell.setCellValue(heroList.get(i));
                    cell.setCellStyle(styleMap.get("allBorderStyle"));
                    for (int j = 0; j < heroVsHeroWinrateThisPatchMatrix[0].length; j++) {
                        int allGames = heroVsHeroWinrateThisPatchMatrix[i][j][0];
                        int wonGames = heroVsHeroWinrateThisPatchMatrix[i][j][1];
                        cell = row.createCell(cellCounter++);
                        cell.setCellStyle(styleMap.get("allBorderStyle"));
                        if (allGames == 0)
                            cell.setCellValue("");
                        else
                            cell.setCellValue((String.format("%.1f", ((float) wonGames / (float) allGames) * 100)) + "% (" + wonGames + "-" + (allGames - wonGames + ")"));
                        cell.setCellStyle(styleMap.get("allBorderStyle"));
                    }
                }

                for (int i = 0; i < heroList.size(); i++) {
                    heroesWinrate.put(heroList.get(i), new int[]{0, 0, 0, 0});
                }
            }

        }
        /*DotaCounters dotaCounters = new DotaCounters();
        int[][] counterMatrix = dotaCounters.getCounterMatrix();
        ArrayList<String> dotaHeroes = dotaCounters.getHeroList();
        rowCounter += 2;
        int counterRowNumber = rowCounter;
        for (int i = 0; i < 5; i++) {
            sheet.createRow(rowCounter++);
        }
        rowCounter = counterRowNumber;
        for (int i = 0; i < 5; i++) {
            cellCounter = 0;
            if (counterMatrix[dotaHeroes.indexOf(heroList.get(i))][dotaHeroes.indexOf(dotaHeroes.get(i + 5))] == 1) {
                row = sheet.getRow(rowCounter++);
                cell = row.createCell(cellCounter++);
                cell.setCellValue(heroList.get(i));
                cell = row.createCell(cellCounter++);
                cell.setCellValue("counters");
                cell = row.createCell(cellCounter++);
                cell.setCellValue(heroList.get(i + 5));
            }
        }
        rowCounter = counterRowNumber;
        for (int i = 0; i < 5; i++) {
            cellCounter = 6;
            if (counterMatrix[dotaHeroes.indexOf(heroList.get(i + 5))][dotaHeroes.indexOf(heroList.get(i))] == 1) {
                row = sheet.getRow(rowCounter++);
                cell = row.createCell(cellCounter++);
                cell.setCellValue(heroList.get(i + 5));
                cell = row.createCell(cellCounter++);
                cell.setCellValue("counters");
                cell = row.createCell(cellCounter++);
                cell.setCellValue(heroList.get(i));
            }
        }*/
        for (int i = 0; i < 13; i++) {
            if (i == 5)
                continue;
            sheet.setColumnWidth(i, 3000);
        }
        sheet.setColumnWidth(5, 200);
        sheet.createFreezePane(0, 1);
        try {
            book.write(new FileOutputStream(f));
            book.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void createStylesForExcelDocument() {
        styleMap = new HashMap<>();

        CellStyle topBorderStyle = book.createCellStyle();
        CellStyle topLeftBorderStyle = book.createCellStyle();
        CellStyle topRightBorderStyle = book.createCellStyle();
        CellStyle bottomBorderStyle = book.createCellStyle();
        CellStyle bottomLeftBorderStyle = book.createCellStyle();
        CellStyle bottomRightBorderStyle = book.createCellStyle();
        CellStyle leftBorderStyle = book.createCellStyle();
        CellStyle rightBorderStyle = book.createCellStyle();
        CellStyle allBorderStyle = book.createCellStyle();

        topBorderStyle.setBorderTop(BorderStyle.THIN);
        topLeftBorderStyle.setBorderTop(BorderStyle.THIN);
        topLeftBorderStyle.setBorderLeft(BorderStyle.THIN);
        topRightBorderStyle.setBorderTop(BorderStyle.THIN);
        topRightBorderStyle.setBorderRight(BorderStyle.THIN);
        bottomBorderStyle.setBorderBottom(BorderStyle.THIN);
        bottomLeftBorderStyle.setBorderBottom(BorderStyle.THIN);
        bottomLeftBorderStyle.setBorderLeft(BorderStyle.THIN);
        bottomRightBorderStyle.setBorderBottom(BorderStyle.THIN);
        bottomRightBorderStyle.setBorderRight(BorderStyle.THIN);
        leftBorderStyle.setBorderLeft(BorderStyle.THIN);
        rightBorderStyle.setBorderRight(BorderStyle.THIN);
        allBorderStyle.setBorderRight(BorderStyle.THIN);
        allBorderStyle.setBorderLeft(BorderStyle.THIN);
        allBorderStyle.setBorderTop(BorderStyle.THIN);
        allBorderStyle.setBorderBottom(BorderStyle.THIN);

        topBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        leftBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        rightBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        bottomBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        topLeftBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        topRightBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        bottomLeftBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        bottomRightBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        allBorderStyle.setTopBorderColor(IndexedColors.BLUE.index);
        allBorderStyle.setBottomBorderColor(IndexedColors.BLUE.index);
        allBorderStyle.setLeftBorderColor(IndexedColors.BLUE.index);
        allBorderStyle.setRightBorderColor(IndexedColors.BLUE.index);

        CellStyle vsStyle = book.createCellStyle();
        CellStyle scoreStyle = book.createCellStyle();
        CellStyle matchedHeroStyle = book.createCellStyle();
        CellStyle winnerScoreStyle = book.createCellStyle();
        vsStyle.setFillForegroundColor(IndexedColors.RED.index);
        vsStyle.setFillPattern(FillPatternType.BRICKS);
        scoreStyle.setFillForegroundColor(IndexedColors.AQUA.index);
        scoreStyle.setFillPattern(FillPatternType.BRICKS);
        Font redFont = book.createFont();
        redFont.setColor(Font.COLOR_RED);
        matchedHeroStyle.setFont(redFont);
        winnerScoreStyle.setFillForegroundColor(IndexedColors.GREEN.index);
        vsStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styleMap.put("vsStyle", vsStyle);
        styleMap.put("scoreStyle", scoreStyle);
        styleMap.put("matchedHeroStyle", matchedHeroStyle);
        styleMap.put("winnerScoreStyle", winnerScoreStyle);
        styleMap.put("topBorderStyle", topBorderStyle);
        styleMap.put("bottomBorderStyle", bottomBorderStyle);
        styleMap.put("topLeftBorderStyle", topLeftBorderStyle);
        styleMap.put("topRightBorderStyle", topRightBorderStyle);
        styleMap.put("bottomLeftBorderStyle", bottomLeftBorderStyle);
        styleMap.put("bottomRightBorderStyle", bottomRightBorderStyle);
        styleMap.put("leftBorderStyle", leftBorderStyle);
        styleMap.put("rightBorderStyle", rightBorderStyle);
        styleMap.put("allBorderStyle", allBorderStyle);
    }

    /*private String getSqlQuerryFor5v5() {
        return basicQuery +           //5v5
                whereQuerry +
                "LOCATE(" + radiantPick[0] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[1] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[2] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[3] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[4] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[0] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[1] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[2] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[3] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[4] + ",g.pick2_string) > 0)" +
                "OR(" +
                "LOCATE(" + radiantPick[0] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[1] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[2] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[3] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[4] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[0] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[1] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[2] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[3] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[4] + ",g.pick1_string) > 0) ) )";
    }

    private String getSqlQuerryFor4v4() {
        int[][] ar1 = {{0, 1, 2, 3}, {0, 1, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4}};
        int[][] ar2 = {{0, 1, 2, 3}, {0, 1, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][3]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][3]]).append(", g.pick1_string) > 0 ");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }
        }
        sqlQuerryBuilder.append(orderByQuerry);

        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor3v3() {
        int[][] ar1 = {{0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4}, {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}};
        int[][] ar2 = {{0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4}, {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][2]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][2]]).append(", g.pick1_string) > 0");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }

        }
        sqlQuerryBuilder.append(orderByQuerry);
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor2v2() {
        int[][] ar1 = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {1, 3}, {1, 4}, {2, 3}, {2, 4}};
        int[][] ar2 = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {1, 3}, {1, 4}, {2, 3}, {2, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick1_string) > 0");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }

        }
        return sqlQuerryBuilder.toString();
    }*/

    private String getSqlQuerryFor1v1() {
        int[][] ar1 = {{0}, {1}, {2}, {3}, {4}};
        int[][] ar2 = {{0}, {1}, {2}, {3}, {4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE('").append(heroList.get(ar1[i][0])).append("', g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(heroList.get(ar1[i][0])).append("', g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(heroList.get(ar2[j][0])).append("', g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE('").append(heroList.get(ar1[i][0])).append("', g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(heroList.get(ar2[j][0])).append("', g.pick1_string) > 0");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }
        }
        sqlQuerryBuilder.append(orderByQuerry);
        return sqlQuerryBuilder.toString();
    }

    /*private String getSqlQuerryFor5v4() {
        int[][] ar2 = {{0, 1, 2, 3}, {0, 1, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar2.length; i++) {
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[0]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[1]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[2]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[3]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[4]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][0]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][1]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][2]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][3]]).append(", g.pick2_string) > 0");
            sqlQuerryBuilder.append(") OR (");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[0]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[1]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[2]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[3]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[4]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][0]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][1]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][2]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][3]]).append(", g.pick1_string) > 0");
            if (i != ar2.length - 1 || i != ar2.length - 1)
                sqlQuerryBuilder.append(") OR (");
            else
                sqlQuerryBuilder.append(") ) )");
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor5v3() {
        int[][] ar2 = {{0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4}, {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar2.length; i++) {
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[0]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[1]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[2]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[3]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[4]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][0]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][1]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][2]]).append(", g.pick2_string) > 0");
            sqlQuerryBuilder.append(") OR (");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[0]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[1]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[2]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[3]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[4]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][0]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][1]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][2]]).append(", g.pick1_string) > 0");
            if (i != ar2.length - 1 || i != ar2.length - 1)
                sqlQuerryBuilder.append(") OR (");
            else
                sqlQuerryBuilder.append(") ) )");
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor5v2() {
        int[][] ar2 = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {1, 3}, {1, 4}, {2, 3}, {2, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar2.length; i++) {
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[0]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[1]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[2]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[3]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[4]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][0]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][1]]).append(", g.pick2_string) > 0");
            sqlQuerryBuilder.append(") OR (");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[0]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[1]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[2]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[3]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[4]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][0]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[i][1]]).append(", g.pick1_string) > 0");
            if (i != ar2.length - 1 || i != ar2.length - 1)
                sqlQuerryBuilder.append(") OR (");
            else
                sqlQuerryBuilder.append(") ) )");
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor4v3() {
        int[][] ar1 = {{0, 1, 2, 3}, {0, 1, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4}};
        int[][] ar2 = {{0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4}, {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][2]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][2]]).append(", g.pick1_string) > 0 ");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor4v2() {
        int[][] ar1 = {{0, 1, 2, 3}, {0, 1, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4}};
        int[][] ar2 = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {1, 3}, {1, 4}, {2, 3}, {2, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick1_string) > 0");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor3v2() {
        int[][] ar1 = {{0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4}, {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}};
        int[][] ar2 = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {1, 3}, {1, 4}, {2, 3}, {2, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][1]]).append(", g.pick1_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][1]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][1]]).append(", g.pick1_string) > 0");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor3v1() {
        int[][] ar1 = {{0, 1, 2}, {0, 1, 3}, {0, 1, 4}, {0, 2, 3}, {0, 2, 4}, {0, 3, 4}, {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}};
        int[][] ar2 = {{0}, {1}, {2}, {3}, {4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery + whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][0]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][0]]).append(", g.pick1_string) > 0");
                if (i != ar1.length - 1 || j != ar2.length - 1)
                    sqlQuerryBuilder.append(") OR (");
                else
                    sqlQuerryBuilder.append(") ) )");
            }
        }
        return sqlQuerryBuilder.toString();
    }*/

    private String getSqlQuerryFor2v1() {
        ArrayList<String> tmpHeroList = new ArrayList<>(heroList.size());
        for (int i = 0; i < heroList.size(); i++) {
            if (heroList.get(i).contains("'")) {
                String newHero = heroList.get(i).replaceAll("'", "''");
                tmpHeroList.add(newHero);
            } else
                tmpHeroList.add(heroList.get(i));
        }
        int[][] ar1 = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}, {1, 3}, {1, 4}, {2, 3}, {2, 4}};
        int[][] ar2 = {{0}, {1}, {2}, {3}, {4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][0])).append("', g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][1])).append("', g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar2[j][0] + 5)).append("', g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][0])).append("', g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][1])).append("', g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar2[j][0] + 5)).append("', g.pick1_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][0] + 5)).append("', g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][1] + 5)).append("', g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar2[j][0])).append("', g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][0] + 5)).append("', g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar1[i][1] + 5)).append("', g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE('").append(tmpHeroList.get(ar2[j][0])).append("', g.pick1_string) > 0");
                if (i == ar1.length - 1 && j == ar2.length - 1)
                    sqlQuerryBuilder.append(") ) )");
                else
                    sqlQuerryBuilder.append(") OR (");
            }
        }
        return sqlQuerryBuilder.toString();
    }

    /*private String getSqlQuerryFor5v1() {
        int[][] ar1 = {{0}, {1}, {2}, {3}, {4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[0]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[1]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[2]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[3]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[4]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick2_string) > 0");
            sqlQuerryBuilder.append(") OR (");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[0]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[1]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[2]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[3]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[4]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0");
            if (i != ar1.length - 1)
                sqlQuerryBuilder.append(") OR (");
            else
                sqlQuerryBuilder.append(") ) )");
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor4v1() {
        int[][] ar1 = {{0, 1, 2, 3}, {0, 1, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4}};
        int[][] ar2 = {{0}, {1}, {2}, {3}, {4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            for (int j = 0; j < ar2.length; j++) {
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar2[j][0]]).append(", g.pick1_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][3]]).append(", g.pick1_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][0]]).append(", g.pick2_string) > 0");
                sqlQuerryBuilder.append(") OR (");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][3]]).append(", g.pick2_string) > 0 AND ");
                sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar2[j][0]]).append(", g.pick1_string) > 0");
                if (i == ar1.length - 1 && j == ar2.length - 1)
                    sqlQuerryBuilder.append(") ) )");
                else
                    sqlQuerryBuilder.append(") OR (");
            }
        }
        return sqlQuerryBuilder.toString();
    }

    private String getSqlQuerryFor5v0() {
        return basicQuery +
                whereQuerry +
                "LOCATE(" + radiantPick[0] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[1] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[2] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[3] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + radiantPick[4] + ",g.pick1_string) > 0)" +
                "OR(" +
                "LOCATE(" + radiantPick[0] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[1] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[2] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[3] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + radiantPick[4] + ",g.pick2_string) > 0)" +
                "OR(" +
                "LOCATE(" + direPick[0] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[1] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[2] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[3] + ",g.pick1_string) > 0 AND " +
                "LOCATE(" + direPick[4] + ",g.pick1_string) > 0)" +
                "OR(" +
                "LOCATE(" + direPick[0] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[1] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[2] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[3] + ",g.pick2_string) > 0 AND " +
                "LOCATE(" + direPick[4] + ",g.pick2_string) > 0) ) )";
    }

    private String getSqlQuerryFor4v0() {
        int[][] ar1 = {{0, 1, 2, 3}, {0, 1, 3, 4}, {0, 1, 2, 4}, {0, 2, 3, 4}, {1, 2, 3, 4}};
        StringBuilder sqlQuerryBuilder = new StringBuilder(basicQuery +
                whereQuerry);
        for (int i = 0; i < ar1.length; i++) {
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick1_string) > 0)");
            sqlQuerryBuilder.append(" OR (");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick1_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][3]]).append(", g.pick1_string) > 0)");
            sqlQuerryBuilder.append(" OR (");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(radiantPick[ar1[i][3]]).append(", g.pick2_string) > 0)");
            sqlQuerryBuilder.append(" OR (");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][0]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][1]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][2]]).append(", g.pick2_string) > 0 AND ");
            sqlQuerryBuilder.append("LOCATE(").append(direPick[ar1[i][3]]).append(", g.pick2_string) > 0");
            if (i != ar1.length - 1)
                sqlQuerryBuilder.append(") OR (");
            else
                sqlQuerryBuilder.append(") ) )");
        }
        return sqlQuerryBuilder.toString();
    }*/

    private HashMap<String, String> generetaQuerries() {
        HashMap<String, String> sqlQuerries = new HashMap<>();
        /*sqlQuerries.put("5v5", getSqlQuerryFor5v5());
        sqlQuerries.put("5v4", getSqlQuerryFor5v4());
        sqlQuerries.put("5v3", getSqlQuerryFor5v3());
        sqlQuerries.put("5v2", getSqlQuerryFor5v2());
        sqlQuerries.put("5v1", getSqlQuerryFor5v1());
        sqlQuerries.put("5v0", getSqlQuerryFor5v0());
        sqlQuerries.put("4v4", getSqlQuerryFor4v4());
        sqlQuerries.put("4v3", getSqlQuerryFor4v3());
        sqlQuerries.put("4v2", getSqlQuerryFor4v2());
        sqlQuerries.put("4v1", getSqlQuerryFor4v1());*/
        //sqlQuerries.put("4v0", getSqlQuerryFor4v0());
        /*sqlQuerries.put("3v3", getSqlQuerryFor3v3());
        sqlQuerries.put("3v2", getSqlQuerryFor3v2());
        sqlQuerries.put("3v1", getSqlQuerryFor3v1());
        sqlQuerries.put("2v2", getSqlQuerryFor2v2());*/
        sqlQuerries.put("2v1", getSqlQuerryFor2v1());
        //sqlQuerries.put("1v1", getSqlQuerryFor1v1());
        return sqlQuerries;
    }

    private HashMap<String, ArrayList<DotaGame>> initializeDataMap() {
        HashMap<String, ArrayList<DotaGame>> dataMap = new HashMap<>();
        //dataMap.put("11", new ArrayList<>());
        dataMap.put("21", new ArrayList<>());
        dataMap.put("22", new ArrayList<>());
        dataMap.put("31", new ArrayList<>());
        dataMap.put("32", new ArrayList<>());
        dataMap.put("33", new ArrayList<>());
        dataMap.put("41", new ArrayList<>());
        dataMap.put("42", new ArrayList<>());
        dataMap.put("43", new ArrayList<>());
        dataMap.put("44", new ArrayList<>());
        dataMap.put("51", new ArrayList<>());
        dataMap.put("52", new ArrayList<>());
        dataMap.put("53", new ArrayList<>());
        dataMap.put("54", new ArrayList<>());
        dataMap.put("55", new ArrayList<>());
        return dataMap;
    }


}
