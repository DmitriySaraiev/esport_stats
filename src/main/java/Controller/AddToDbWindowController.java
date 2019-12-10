package Controller;

import Model.Dota.DotaDatabase;
import Model.Dota.DotaMatch;
import Model.Dota.DotabuffParser;
import Model.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jsoup.HttpStatusException;

import java.io.*;
import java.util.ArrayList;

public class AddToDbWindowController {

    private DotaDatabase db;
    private DotabuffParser dotaParser;

    @FXML
    private void initialize() {
        db = Main.ddb;

        dotaParser = new DotabuffParser();
        try {
            File f = new File("\\info.txt");
            f.createNewFile();
            BufferedReader reader = new BufferedReader(new FileReader(f));
            linkTf.setText(reader.readLine());
            numberTf1.setText(reader.readLine());
        }
        catch (FileNotFoundException e){e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
    }

    public static void stop(){
        System.exit(0);
    }

    @FXML
    private void handleOkButtonAction(ActionEvent ae){
        try {
            ta.clear();
            FileWriter writer = new FileWriter("\\info.txt");
            writer.write(linkTf.getText());
            writer.write(System.getProperty("line.separator"));
            ArrayList<String> linkList;
            if(numberTf2.getText().isEmpty())
                linkList = dotaParser.parseAllTournamentMatchesLinks(linkTf.getText(), Integer.valueOf(numberTf1.getText() ), -1);
            else
                linkList = dotaParser.parseAllTournamentMatchesLinks(linkTf.getText(), Integer.valueOf(numberTf1.getText()), Integer.valueOf(numberTf2.getText() ) );
            createDotaMatches(db, dotaParser, linkList);
            writer.write(numberTf1.getText());
            writer.close();
        }
        catch (IOException e) {e.printStackTrace();}
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDotaMatches(DotaDatabase ddb, DotabuffParser dotabuffParser, ArrayList<String> linkList){
        for (String link : linkList) {
            ta.appendText(String.valueOf(link)+ ". ");
            try {
                dotabuffParser = new DotabuffParser(link);
                DotaMatch match = dotabuffParser.parseMatchLimited();
                ddb.createMatch(match);
                ta.appendText(match.getTeam1().getName()+" vs " + match.getTeam2().getName() + " " + match.getLink() + "\n");
                int counter = Integer.valueOf(numberTf1.getText());
                numberTf1.setText(String.valueOf(++counter));
                Thread.sleep(1000);
            }
            catch (HttpStatusException e){
                ta.appendText("\nHttpStatusException");
                break;
            }
            catch (NullPointerException e) {
                ta.appendText("\nNullPointerException");
                continue;
            } catch (IndexOutOfBoundsException e) {
                ta.appendText("\nIndexOutOfBoundsException");
                e.printStackTrace();
                try {
                    File f = new File("\\info.txt");
                    f.createNewFile();
                    FileWriter writer = new FileWriter("\\info.txt");
                    writer.write(linkTf.getText());
                    writer.write(System.getProperty("line.separator"));
                    writer.write(numberTf1.getText());
                    writer.close();
                }
                catch (IOException e1) {e1.printStackTrace();}
                break;
            }
            catch (InterruptedException e){
                ta.appendText("\nInterruptedException");
            }
        }
    }

    @FXML
    TextField linkTf;
    @FXML
    TextField numberTf1;
    @FXML
    TextField numberTf2;
    @FXML
    TextArea ta;
    @FXML
    Button okBt;
    @FXML
    MenuItem switchToMainMenu;
    @FXML
    MenuItem switchToAddSubscriptionMenu;
}
