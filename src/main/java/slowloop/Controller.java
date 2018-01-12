package slowloop;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.*;


public class Controller{

    public TextField city, country;
    public Text description, temp, time, popAmount, pop, tempTitle, timeTitle, date,
    tempDescr, title, popEstimate, untilSunsetSunrise, timeUntilSunsetSunrise;
    public Rectangle border;
    public Button button;
    public BorderPane bPane;
    public UserInterface ui;
    public String cityName, countryN;
    public Alert alert;
    public ProgressIndicator progress;

    public void enterPressed(KeyEvent ke) throws Exception{
        final KeyEvent event = ke;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if(event.getCode() == KeyCode.ENTER){
                    cityName = ui.city(city.getText().replace(" ", "+"));
                    countryN = ui.city(country.getText().replace(" ", "+"));
                    progress.setVisible(true);

                    ui.getLongLat(cityName, countryN);

                    if (ui.wikiConnect(cityName).equals("") || !(ui.locationType.equalsIgnoreCase("locality"))
                            && !(ui.wikiConnect(cityName).contains(ui.longName)) || ui.locationType.equals("")
                            ||  !(ui.countryName.equalsIgnoreCase(countryN.replace("+", "_")))) {

                        alertBox();

                    }else{
                        search(ui);
                        progress.setVisible(false);
                    }
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public void searchButton(ActionEvent ae) throws Exception{

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                cityName = ui.city(city.getText().replace(" ", "+"));
                countryN = ui.city(country.getText().replace(" ", "+"));
                progress.setVisible(true);

                ui.getLongLat(cityName, countryN);

                if (ui.wikiConnect(cityName).equals("") || !(ui.locationType.equalsIgnoreCase("locality"))
                        && !(ui.wikiConnect(cityName).contains(ui.longName)) || ui.locationType.equals("")
                        ||  !(ui.countryName.equalsIgnoreCase(countryN.replace("+", "_")))) {

                    alertBox();

                }else{
                    search(ui);
                    progress.setVisible(false);
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public void alertBox(){
        Platform.runLater(new Runnable() {
            public void run() {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error!");
                alert.setHeaderText(null);

                if(country.getText().equals("") && city.getText().equals("")){
                    alert.setContentText("Please enter a city and country.");
                    progress.setVisible(false);
                    alert.showAndWait();
                }
                else if(city.getText().equals("")){
                    alert.setContentText("Please enter a city.");
                    progress.setVisible(false);
                    alert.showAndWait();
                }
                else if(country.getText().equals("")){
                    alert.setContentText("Please enter a country.");
                    progress.setVisible(false);
                    alert.showAndWait();
                }
                else{
                    alert.setContentText("Cannot find location.");
                    progress.setVisible(false);
                    alert.showAndWait();
                }
            }
        });
    }

    public void search(UserInterface text) throws Exception{
        UserInterface ui = text;

        ui.getTimeZone();
        ui.weather();
        ui.cityPopulation();

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                titleAndDescription();
                tempAndPopulation();
                timeAndSunsetSunrise();
                return null;
            }
        };
        new Thread(task).start();
    }

    public void titleAndDescription() throws Exception{
        //City name
        title.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 30));

        //If length of the city name is longer than 13 characters then start on a new line
        if(cityName.length() > 13){
            StringBuilder longCityName = new StringBuilder();
            int count = 0;

            for(char letter : cityName.toCharArray()){
                if(Character.toString(letter).equals("_") && count == cityName.lastIndexOf("_")){
                    longCityName.append("\n");
                    continue;
                }
                if(count == 8 && !(cityName.contains("_"))){
                    longCityName.append(letter + "-" + "\n");
                }
                else{
                    longCityName.append(letter);
                }
                count++;
            }
            title.setText(longCityName.toString().replace("_", " "));
            title.setFill(Color.WHITE);
        }else{
            title.setText(cityName.replace("_", " "));
            title.setFill(Color.WHITE);
        }

        //border
        border.setHeight(50);
        border.setWidth(1);
        border.setFill(Color.WHITE);

        //City description
        ui.cityDescription(cityName, description);
    }

    public void tempAndPopulation() throws Exception{

        //City temperature
        tempTitle.setText("WEATHER");
        ui.setTemp(temp);
        temp.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 40));
        tempDescr.setText(ui.tempDescription);

        //City population
        pop.setText("POPULATION");
        popAmount.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 40));
        ui.getPopulationNumber(popAmount);
        popEstimate.setText(ui.popEstimate);
    }

    public void timeAndSunsetSunrise(){

        //Sunrise/sunset countdown
        timeUntilSunsetSunrise.setText("");
        timeUntilSunsetSunrise.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 40));
        untilSunsetSunrise.setText("");

        //Time
        timeTitle.setText("TIME & DATE");
        time.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 40));
        time.setText("");

        //Timer task for sunrise/sunset countdown and time/date
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    ui.getTime(time);
                    ui.date(date);
                    ui.dayLightHours(timeUntilSunsetSunrise, untilSunsetSunrise);

                } catch (Exception e) {
                }
            }
        };
        new Timer().schedule(task, 100, 1000);
    }

    public void deselect(MouseEvent me) {
        bPane.requestFocus();
    }

    public void initialize () throws IOException{
        ui = new UserInterface();
        progress.setVisible(false);

        city.setPromptText("Enter city");
        city.setFocusTraversable(false);
        country.setPromptText("Enter country");
        country.setFocusTraversable(false);

    }
}
