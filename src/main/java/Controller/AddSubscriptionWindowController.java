package Controller;

import Model.Dota.DotaDatabase;
import Model.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import Model.TelegramBot.Subscription;

public class AddSubscriptionWindowController {

    private DotaDatabase ddb;

    @FXML
    private void initialize() {
        ddb = Main.ddb;
    }

    @FXML
    private void handleAddBtAction(ActionEvent ae) {
        try {
            String code = generatePassword(10);
            if (isReusableCbx.isSelected()) {
                if (!tillTf.getText().isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date parsedDate = dateFormat.parse(tillTf.getText());
                    ddb.createSubscription(new Subscription(code, new Timestamp(parsedDate.getTime())));
                }
            } else {
                if (timeTf.getText().length() > 0) {
                    ddb.createSubscription(new Subscription(code, Integer.valueOf(timeTf.getText())));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleIsReusableCbxAction(ActionEvent ae) {
        if(isReusableCbx.isSelected()){
            tillTf.setEditable(true);
            tillTf.setVisible(true);
            timeTf.setEditable(false);
            timeTf.setVisible(false);
        }
        else{
            tillTf.setEditable(false);
            tillTf.setVisible(false);
            timeTf.setEditable(true);
            timeTf.setVisible(true);
        }
    }

    @FXML
    private void handleSwitchToAddMatchesMenuAction(ActionEvent ae) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/AddToDbWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setTitle("esport stats");
            stage.setScene(scene);
            stage.show();
            Stage stagetmp = (Stage) addBt.getScene().getWindow();
            stagetmp.hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSwitchToMainMenuAction(ActionEvent ae) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/MainWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 830, 240);
            Stage stage = new Stage();
            stage.setTitle("esport stats");
            stage.setScene(scene);
            stage.show();
            Stage stagetmp = (Stage) addBt.getScene().getWindow();
            stagetmp.hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generatePassword(int length){
        String saltchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            int index = (int) (rnd.nextFloat() * saltchars.length());
            sb.append(saltchars.charAt(index));
        }
        return sb.toString();
    }


    @FXML
    TextField timeTf;
    @FXML
    TextField tillTf;
    @FXML
    Button addBt;
    @FXML
    MenuItem switchToMainMenu;
    @FXML
    MenuItem switchToAddMatchesMenu;
    @FXML
    CheckBox isReusableCbx;

}
