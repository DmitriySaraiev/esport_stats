package Controller;

import Model.Dota.Heroes;
import Model.Dota.PickSearcher;
import Model.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainWindowController {

    private static ArrayList<String> heroList;
    ArrayList<TextField> heroeTfList;

    public static void stop(){
        System.exit(0);
    }

    @FXML
    private void initialize(){
        heroList = Heroes.getAllHeroes();
        heroeTfList = new ArrayList<>();
        heroeTfList.add(heroeTf1);
        heroeTfList.add(heroeTf2);
        heroeTfList.add(heroeTf3);
        heroeTfList.add(heroeTf4);
        heroeTfList.add(heroeTf5);
        heroeTfList.add(heroeTf6);
        heroeTfList.add(heroeTf7);
        heroeTfList.add(heroeTf8);
        heroeTfList.add(heroeTf9);
        heroeTfList.add(heroeTf10);
        for (int i = 0; i < heroeTfList.size(); i++) {
            TextField tf = heroeTfList.get(i);
            final int i2 = i;
            tf.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    List<String> res = search(getHeroFromShortName(newValue));
                    if (res.size() == 1) {
                        tf.setText(res.get(0));
                        if(i2<heroeTfList.size()-1)
                            heroeTfList.get(i2+1).requestFocus();
                    }
                }
            });
        }
    }

    private static List<String> search(String text) {
        List<String> res = new ArrayList<>();
        for(String s : heroList) {
            if(s.startsWith(text)) {
                res.add(s);
            }
        }
        return res;
    }

    private String getHeroFromShortName(String heroName){
        switch (heroName){
            case "aa":
                return Heroes.aa;
            case "shaker":
                return Heroes.shaker;
            case "bb":
                return Heroes.bb;
            case "et":
                return Heroes.et;
            case "es":
                return Heroes.es;
            case "pha":
                return Heroes.pa;
            case "sd":
                return Heroes.sd;
            case "sf":
                return Heroes.sf;
            case "ss":
                return Heroes.ss;
            case "pl":
                return Heroes.pl;
            case "pit":
                return Heroes.pitlord;
            case "uy":
                return Heroes.undying;
            case "np":
                return Heroes.np;
            case "cm":
                return Heroes.cm;
            case "wd":
                return Heroes.wd;
            case "od":
                return Heroes.od;
            case "wk":
                return Heroes.wk;
            case "ls":
                return Heroes.ls;
            case "ww":
                return Heroes.ww;
            case "dp":
                return Heroes.dp;
            case "dw":
                return Heroes.dw;
            case "tb":
                return Heroes.tb;
            case "dk":
                return Heroes.dk;
            case "sk":
                return Heroes.sk;
            case "ns":
                return Heroes.ns;
            case "sb":
                return Heroes.sb;
            case "ck":
                return Heroes.ck;
            case "am":
                return Heroes.am;
            case "ta":
                return Heroes.ta;
            case "bh":
                return Heroes.bh;
            case "mk":
                return Heroes.mk;
            case "wr":
                return Heroes.wr;
            case "ds":
                return Heroes.ds;
            case "lc":
                return Heroes.lc;
            case "ld":
                return Heroes.ld;
            case "sma":
                return Heroes.sky;
            default:
                char firstChar = heroName.charAt(0);
                return heroName.replace(firstChar, Character.toUpperCase(firstChar));
        }
    }

    @FXML
    private void handleOkButtonAction(ActionEvent ae){
        ArrayList<String> heroList = new ArrayList<>();
        for(TextField tf : heroeTfList)
            heroList.add(tf.getText());
        PickSearcher pickSearcher = new PickSearcher(heroList, Main.ddb);
        pickSearcher.searchByPicksToExcel(pickSearcher.getData(), 1L);
        finishLabel.setText("finished");
    }

    @FXML
    private void handleClearButtonAction(ActionEvent ae){
        for(TextField tf : heroeTfList)
            tf.clear();
        finishLabel.setText("");
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
            Stage stagetmp = (Stage) okBt.getScene().getWindow();
            stagetmp.hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSwitchToAddSubscriptionMenuAction(ActionEvent ae) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/AddSubscriptionWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 315, 355);
            Stage stage = new Stage();
            stage.setTitle("esport stats");
            stage.setScene(scene);
            stage.show();
            Stage stagetmp = (Stage) okBt.getScene().getWindow();
            stagetmp.hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    Button okBt;
    @FXML
    Button clearBt;
    @FXML
    TextField heroeTf1;
    @FXML
    TextField heroeTf2;
    @FXML
    TextField heroeTf3;
    @FXML
    TextField heroeTf4;
    @FXML
    TextField heroeTf5;
    @FXML
    TextField heroeTf6;
    @FXML
    TextField heroeTf7;
    @FXML
    TextField heroeTf8;
    @FXML
    TextField heroeTf9;
    @FXML
    TextField heroeTf10;
    @FXML
    Label finishLabel;
    @FXML
    MenuItem switchToAddMatchesMenu;
    @FXML
    MenuItem switchToAddSubscriptionMenu;
}
