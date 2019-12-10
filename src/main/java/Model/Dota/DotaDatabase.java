package Model.Dota;

import Model.Main;
import Model.PasswordManager;
import Model.TelegramBot.Subscription;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

public class DotaDatabase {

    private PasswordManager passwordManager;
    private String remoteServerUrl;
    private String connectionURLRemote;
    private String connectionURLLocal = "jdbc:mysql://localhost/esport_db_dota?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true";
    private String user; 
    private String password; 
    
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public DotaDatabase(PasswordManager passwordManager) {
        this.passwordManager = passwordManager;
        try {
            initializeConfidentialInfo();
            initializeConnection();
            keepConnectionAlive();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeConfidentialInfo(){
        remoteServerUrl = passwordManager.getServerIP();
        user = passwordManager.getDbLogin();
        password = passwordManager.getDbPassword();
        connectionURLRemote = "jdbc:mysql://" + remoteServerUrl +"/esport_db_dota?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true";
    }
    
    private void initializeConnection() throws SQLException{
            if (Main.isServer)
                connection = DriverManager.getConnection(connectionURLLocal, user, password);
            else
                connection = DriverManager.getConnection(connectionURLRemote, user, password);
            statement = connection.createStatement();
    }

    private void keepConnectionAlive() {
        Runnable keepConnectionLiveTask = () -> {
            try {
                checkConnection();
            }
            catch (SQLException e){e.printStackTrace();}
            catch (InterruptedException e){e.printStackTrace();}
        };
        Thread connectionCheckThread = new Thread(keepConnectionLiveTask);
        connectionCheckThread.start();
    }

    private void checkConnection() throws SQLException, InterruptedException{
        while(true){
            System.out.println("checking connection");
            if (connection.isClosed()) {
                System.out.println("connection closed");
                connection = DriverManager.getConnection(connectionURLLocal, user, password);
            }
            if(statement == null || statement.isClosed()){
                System.out.println("statement closed");
                statement = connection.createStatement();
            }
            System.out.println(statement.execute("SELECT * FROM info"));  //execute simple querry every 5 hours to keep connection alive
            Thread.sleep(1000 * 60 * 60 * 5);
        }
    }
    
    public void createStatement() {
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createMatch(DotaMatch match) {
        try {
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM(" +
                    "SELECT * FROM esport_db_dota.match WHERE match.link LIKE '" + match.getLink() + "')AS subquerry");
            resultSet.last();
            if (resultSet.getInt(1) == 0) {
                for (DotaGame game : match.getGameList())
                    createGame(game, match.getLink());
                statement.executeUpdate("INSERT INTO esport_db_dota.match (" +
                        "date_and_time, tournament, team1_name, team2_name, team1_score, team2_score, link, patch) VALUES ('" +
                        match.getDateAndTime() + "', '" +
                        match.getTournament().getName().replaceAll("'","''") + "', '" +
                        match.getTeam1().getName().replaceAll("'","''") + "', '" +
                        match.getTeam2().getName().replaceAll("'","''") + "', '" +
                        match.getScoreTeam1() + "', '" +
                        match.getScoreTeam2() + "', '" +
                        match.getLink() + "', '" +
                        getPatch() + "'" +
                        ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createGame(DotaGame game, String matchLink) {
        try {
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM(" +
                    "SELECT * FROM esport_db_dota.game WHERE game.link LIKE '" + game.getLink() + "')AS subquerry");
            resultSet.last();
            if (resultSet.getInt(1) == 0) {
                int is_team_radiant = game.isTeam1Radiant() ? 1 : 0;
                StringBuilder team1pick = new StringBuilder();
                StringBuilder team2pick = new StringBuilder();
                for (int i = 0; i < game.getTeam1Picks().size(); i++) {
                    if (i < game.getTeam1Picks().size() - 1) {
                        team1pick.append(game.getTeam1Picks().get(i).replaceAll("'", "''")).append(", ");
                        team2pick.append(game.getTeam2Picks().get(i).replaceAll("'", "''")).append(", ");
                    } else {
                        team1pick.append(game.getTeam1Picks().get(i).replaceAll("'", "''"));
                        team2pick.append(game.getTeam2Picks().get(i).replaceAll("'", "''"));
                    }
                }
                int isWinnerRadiant = game.isWinnerRadiant() ? 1 : 0;
                statement.executeUpdate("INSERT INTO esport_db_dota.game (team1_score, team2_score, link, match_link, " +
                        "duration, is_team1_radiant, " +
                        "team1_pick1, team1_pick2, team1_pick3, team1_pick4, team1_pick5, " +
                        "team2_pick1, team2_pick2, team2_pick3, team2_pick4, team2_pick5, " +
                        "pick1_string, pick2_string, " +
                        "is_winner_radiant" +
                        ") VALUES ('" +
                        game.getScoreTeam1() + "', '" + game.getScoreTeam2() + "', '" + game.getLink() + "', '" + matchLink + "', '" +
                        game.getDuration() + "', '" + is_team_radiant + "', '" +
                        game.getTeam1Picks().get(0).replaceAll("'", "''") + "', '" + game.getTeam1Picks().get(1).replaceAll("'", "''") + "', '" +
                        game.getTeam1Picks().get(2).replaceAll("'", "''") + "', '" + game.getTeam1Picks().get(3).replaceAll("'", "''") + "', '" +
                        game.getTeam1Picks().get(4).replaceAll("'", "''") + "', '" +
                        game.getTeam2Picks().get(0).replaceAll("'", "''") + "', '" + game.getTeam2Picks().get(1).replaceAll("'", "''") + "', '" +
                        game.getTeam2Picks().get(2).replaceAll("'", "''") + "', '" + game.getTeam2Picks().get(3).replaceAll("'", "''") + "', '" +
                        game.getTeam2Picks().get(4).replaceAll("'", "''") + "', '" +
                        team1pick + "', '" + team2pick + "', '" + isWinnerRadiant + "'" +
                        ");");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public ResultSet findMatchesByPick(String sqlQuerry) {
        try {
            return statement.executeQuery(sqlQuerry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public ResultSet getUserById(long id){
        try {
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM(" +
                    "SELECT * FROM esport_db_dota.telegram_user WHERE esport_db_dota.telegram_user.id = " + id + ") as subquerry");
            resultSet.last();
            if (resultSet.getInt(1) == 0) {
                statement.executeUpdate("INSERT INTO esport_db_dota.telegram_user (id, registration_date) VALUES " +
                        "('" + id + "', '" + new Timestamp(System.currentTimeMillis())+"')");
            }
            resultSet = statement.executeQuery("SELECT * FROM esport_db_dota.telegram_user WHERE telegram_user.id = " + id);
        }
        catch (SQLException e){e.printStackTrace();}
        return resultSet;
    }

    public Timestamp getSubscriptionUntil(long userId) {
        try {
            resultSet = statement.executeQuery(
                    "SELECT  esport_db_dota.telegram_user.subscription_until " +
                            "FROM esport_db_dota.telegram_user " +
                            "WHERE telegram_user.id = " + userId);
            resultSet.last();
            Timestamp timestamp = resultSet.getTimestamp(1);
            return timestamp;
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return new Timestamp(System.currentTimeMillis());
    }

    public void incrimentUsesAll(long userId){
        try {
            statement.executeUpdate(
                    "UPDATE  esport_db_dota.telegram_user " +
                            "SET uses_all = uses_all+1 " +
                            " WHERE telegram_user.id = " + userId);
        }
        catch (SQLException e){e.printStackTrace();}
    }

    public int getFreeUsesLeft(long userId) {
        try {
            resultSet = statement.executeQuery(
                    "SELECT  esport_db_dota.telegram_user.free_uses_left " +
                            "FROM esport_db_dota.telegram_user " +
                            "WHERE telegram_user.id = " + userId);
            resultSet.last();
            int usesLeft = resultSet.getInt(1);
            return usesLeft;
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public void setFreeUsesLeft(int uses, long userId){
        try {
            statement.executeUpdate(
                    "UPDATE  esport_db_dota.telegram_user " +
                            "SET free_uses_left = " + uses +
                            " WHERE telegram_user.id = " + userId);
        }
        catch (SQLException e){e.printStackTrace();}
    }


    public Timestamp addTimeToSubscription(long userId, Timestamp tillDate) {
        try {
            resultSet = statement.executeQuery(
                    "SELECT  esport_db_dota.telegram_user.subscription_until " +
                            "FROM esport_db_dota.telegram_user " +
                            "WHERE telegram_user.id = " + userId);
            resultSet.last();
            Timestamp timestamp = resultSet.getTimestamp(1);
            if (timestamp == null || timestamp.before(new Timestamp(System.currentTimeMillis()))) {
                statement.executeUpdate(
                        "UPDATE  esport_db_dota.telegram_user " +
                                "SET subscription_until = '" + tillDate + "' " +
                                "WHERE telegram_user.id = " + userId);
                return tillDate;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return new Timestamp(System.currentTimeMillis());
    }

    public Timestamp addTimeToSubscription(long userId, int hours) {
        try {
            resultSet = statement.executeQuery(
                    "SELECT  esport_db_dota.telegram_user.subscription_until " +
                    "FROM esport_db_dota.telegram_user " +
                    "WHERE telegram_user.id = " + userId);
            resultSet.last();
            Timestamp timestamp = resultSet.getTimestamp(1);
            Calendar calendar = Calendar.getInstance();
            if(timestamp == null || timestamp.before(new Timestamp(System.currentTimeMillis())) ){
                calendar.add(Calendar.HOUR, hours);
                timestamp = new Timestamp(calendar.getTimeInMillis());
            }
            else if (timestamp.after(new Timestamp(System.currentTimeMillis() ) ) ){
                timestamp.setTime(timestamp.getTime() + (1000 * 60 * 60 * hours) );
            }
            statement.executeUpdate(
                    "UPDATE  esport_db_dota.telegram_user " +
                    "SET subscription_until = '" + timestamp + "' " +
                    "WHERE telegram_user.id = " + userId);
            return timestamp;
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return new Timestamp(System.currentTimeMillis());
    }

    public void createUserSubscription(long chatId, Subscription subscription){
        try{
            statement.executeUpdate("INSERT  INTO esport_db_dota.user_subscription " +
                    "(code, enter_date, expiration_date, user_id)" +
                    "VALUES ('"+
                    subscription.getCode()+"', '" +
                    new Timestamp(System.currentTimeMillis()) + "', '" +
                    subscription.getTillDate()  + "', " +
                    chatId + ")");
        }
        catch (SQLException e){e.printStackTrace();}
    }

    public void createUserSearchQuery(long chatId, ArrayList<String> heroList, String teams){
        StringBuilder leftPickSb = new StringBuilder();
        StringBuilder rightPickSb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            leftPickSb.append(heroList.get(i) + ", ");
            rightPickSb.append(heroList.get(i+5) + ", ");
        }
        leftPickSb.append(heroList.get(4));
        rightPickSb.append(heroList.get(9));
        try{
            statement.executeUpdate("INSERT  INTO esport_db_dota.user_search_query " +
                    "(pick1, pick2, pick3, pick4, pick5, pick6, pick7, pick8, pick9, pick10, " +
                    "left_pick_string, right_pick_string, user_id, time, teams)" +
                    "VALUES ('"+
                    heroList.get(0)+"', '" +
                    heroList.get(1)+"', '" +
                    heroList.get(2)+"', '" +
                    heroList.get(3)+"', '" +
                    heroList.get(4)+"', '" +
                    heroList.get(5)+"', '" +
                    heroList.get(6)+"', '" +
                    heroList.get(7)+"', '" +
                    heroList.get(8)+"', '" +
                    heroList.get(9)+"', '" +
                    leftPickSb.toString()+"', '" +
                    rightPickSb.toString()+"', " +
                    chatId + ", '" +
                    new Timestamp(System.currentTimeMillis()) +"', '" +
                    teams +
                    "')" );
        }
        catch (SQLException e){e.printStackTrace();}
    }

    public void createSubscription(Subscription subscription){
        try{
            if(subscription.isReusable()){
                statement.executeUpdate("INSERT INTO esport_db_dota.subscription " +
                        "(code, tillDate, isReusable) " +
                        "VALUES ('" +
                        subscription.getCode()+"', '" +
                        subscription.getTillDate()+"', " +
                        1 + ")"  );
            }
            else
                statement.executeUpdate("INSERT INTO esport_db_dota.subscription " +
                        "(code, hours, isReusable) " +
                        "VALUES ('" +
                        subscription.getCode()+"', " +
                        subscription.getHours()+", " +
                        0 + ")"  );
        }
        catch(SQLException e){e.printStackTrace();}
    }

    public Subscription getSubscriptionByCode(String code){
        Subscription subscription;
        try {
            resultSet = statement.executeQuery("SELECT * FROM esport_db_dota.subscription " +
                    "WHERE code='" + code + "'");
            if(resultSet.next()){
                if(resultSet.getInt("isReusable") == 1)
                    subscription = new Subscription(resultSet.getString("code"),
                            new Timestamp(resultSet.getDate("tillDate").getTime()));
                else {
                    subscription = new Subscription(resultSet.getString("code"), resultSet.getInt("hours"));
                    statement.executeUpdate("DELETE FROM subscription WHERE code = '" + code + "'");
                }
                return subscription;
            }
        }
        catch (SQLException e){e.printStackTrace();}
        return new Subscription();
    }

    public ResultSet getAllGames(){
        ResultSet rs = null;
        try {
            rs = statement.executeQuery("SELECT * FROM esport_db_dota.game");
        }
        catch (SQLException e){e.printStackTrace();}
        return rs;
    }

    public double getPatch(){
        double patch = 0.0;
        try{
            resultSet = statement.executeQuery("SELECT patch FROM esport_db_dota.info");
            resultSet.last();
            patch = resultSet.getDouble("patch");
        }
        catch(SQLException e){e.printStackTrace();}
        return patch;
    }

}
