package Model;

import Controller.AddToDbWindowController;
import Model.Dota.*;
import Model.LiveGames.LiveGameParser;
import Model.LiveGames.LiveGamePool;
import Model.TelegramBot.EsportStatsTelegramBot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    public static final boolean isServer = false;
    public static DotaDatabase ddb;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream("/fxml/AddToDbWindow.fxml"));
        primaryStage.setTitle("esport stats");
        primaryStage.setScene(new Scene(root, 600 , 400));
        primaryStage.setOnHidden(e -> AddToDbWindowController.stop());
        primaryStage.show();
    }
    
    private static void initialize(){
        ddb = new DotaDatabase(new PasswordManager());
        if(isServer) {
            ApiContextInitializer.init();
            TelegramBotsApi telegram = new TelegramBotsApi();
            EsportStatsTelegramBot bot = new EsportStatsTelegramBot(ddb);
            try {
                telegram.registerBot(bot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        initialize();
        launch(args);
    }

}