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

                    if (ui.wikiConnect().equals("") || ui.longName.equals("") || !(ui.countryName.equalsIgnoreCase(countryN.replace("+", "_")))) {

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

                if (ui.wikiConnect().equals("") || ui.longName.equals("") || !(ui.countryName.equalsIgnoreCase(countryN.replace("+", "_")))) {

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

        try{
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

        }catch (Exception e){

            //If the Wolfram Alpha API is unavailable then exclude it from search.
            //This prevents an infinite loading loop.
            ui.getTimeZone();
            ui.weather();

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
    }

    public void titleAndDescription() throws Exception{
        //City name
        title.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 30));

        //If length of the city name is longer than 13 characters then start on a new line
        if(ui.cityTitle.length() > 13){
            StringBuilder longCityName = new StringBuilder();
            int count = 0;

            try{
                for(char letter : ui.cityTitle.toCharArray()){

                    if(Character.toString(letter).equals(" ") && count == ui.cityTitle.lastIndexOf(" ")){

                        if(count > 13){
                            int position = 0;
                            boolean newLine = false;

                            for(char letters : longCityName.toString().toCharArray()){
                                position++;

                                if(Character.toString(letters).equals(" ") && position < count && position > 8 && newLine == false){

                                    longCityName.replace(position, position, "\n");
                                    count = 0;
                                    newLine = true;
                                }
                            }
                        }else{
                            longCityName.append("\n");
                            count = 0;
                            continue;
                        }
                    }
                    if(count == 8 && !(ui.cityTitle.contains(" "))){
                        longCityName.append(letter + "-" + "\n");
                        count = 0;
                    }
                    else{
                        longCityName.append(letter);
                    }
                    count++;
                }
            }catch(Exception e){
            }

            title.setText(longCityName.toString());
            title.setFill(Color.WHITE);
        }else{
            title.setText(ui.cityTitle);
            title.setFill(Color.WHITE);
        }

        //border
        border.setHeight(50);
        border.setWidth(1);
        border.setFill(Color.WHITE);

        //City description
        ui.cityDescription(description);
    }

    public void tempAndPopulation() throws Exception{

        //City temperature
        tempTitle.setText("WEATHER");
        ui.setTemp(temp);
        temp.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 40));
        tempDescr.setText(ui.tempDescription);

        if(ui.pop == null){
            //City population. If the pop variable in the UserInterface class is null then population text is set to "n/a".
            pop.setText("POPULATION");
            popAmount.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 40));
            popAmount.setText("n/a");
            popEstimate.setText("");

        }else{
            pop.setText("POPULATION");
            popAmount.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 40));
            ui.getPopulationNumber(popAmount);
            popEstimate.setText(ui.popEstimate);
        }
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
