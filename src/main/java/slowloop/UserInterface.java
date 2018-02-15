package slowloop;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.util.*;


public class UserInterface extends Application {

    public String longName, countryName, provinceName, tempDescription, temperature, pop, popEstimate,
            cityTitle, timeZone;
    private double longitude, latitude;
    public int dOffset, rOffset, sunsetSeconds, sunriseSeconds;
    public Text tempText;

    public void start(final Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/face.fxml"));
        Scene scene = new Scene(root, 500, 470);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Destination");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()  {
            public void handle(WindowEvent t) {
                System.exit(0);
            }
        });
        primaryStage.show();
    }

    //Grab longitude and latitude (and other geocode data) of the city using Google geocode API.
    public void getLongLat(String city, String country) throws IOException{

        InputStream in = new URL( "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                URLEncoder.encode(city, "UTF8") + "," + URLEncoder.encode(country, "UTF8") + "&key=API key goes here").openStream();
        StringBuilder json = new StringBuilder();

        try {
            for(String string : IOUtils.readLines(in, (String) null)){
                json.append(string);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }

        GoogleLocation location = new Gson().fromJson(json.toString(), GoogleLocation.class);

        longName = "";

        //Grab city name, province name, and country name
        try{
            for(int i = 0; i < location.results.get(0).address_components.size();i++){

                for(int a = 0; a < location.results.get(0).address_components.get(i).types.size(); a++){

                    if(location.results.get(0).address_components.get(i).types.get(a).equalsIgnoreCase("locality")||
                            location.results.get(0).address_components.get(i).types.get(a).equalsIgnoreCase("administrative_area_level_2") && longName.equals("")){

                        longName = (location.results.get(0).address_components.get(i).long_name).replace(" District", "")
                                .replace(" Territory", "").replace(" Precinct", "").replace(" Region", "");

                    }
                    if(location.results.get(0).address_components.get(i).types.get(a).equalsIgnoreCase("administrative_area_level_1")){

                        provinceName = location.results.get(0).address_components.get(i).long_name.replace(" ", "_").replaceAll("State_of_", "");

                    }
                    if(location.results.get(0).address_components.get(i).types.get(a).equalsIgnoreCase("country")){

                        countryName = location.results.get(0).address_components.get(i).long_name.replace(" ", "_");

                    }
                }
            }

            if(provinceName == null){
                provinceName = "";
            }
            if(countryName == null){
                countryName = "";
            }

            //Grab longitude and latitude for the city
            longitude = location.results.get(0).geometry.location.lng;
            latitude = location.results.get(0).geometry.location.lat;

            if(Double.toString(longitude) == null){
                longitude = 0;
            }
            if(Double.toString(latitude) == null){
                latitude = 0;
            }

        }catch (Exception e){
        }
    }

    //Grab timezone data using Google timezone API.
    public void getTimeZone() throws IOException{

        Timestamp time = new Timestamp(System.currentTimeMillis());
        InputStream tz = new URL( "https://maps.googleapis.com/maps/api/timezone/json?location=" + latitude + "," +
                longitude + "&timestamp=" + time.getTime()/1000 + "&key=API key goes here").openStream();
        StringBuilder jsonTz = new StringBuilder();

        try {
            for(String string : IOUtils.readLines(tz, (String) null)){
                jsonTz.append(string);
            }
        } finally {
            IOUtils.closeQuietly(tz);
        }

        GoogleTimeZone timeZ = new Gson().fromJson(jsonTz.toString(), GoogleTimeZone.class);

        try{
            dOffset = timeZ.dstOffset;
            rOffset = timeZ.rawOffset;
            timeZone = timeZ.timeZoneId;

            if(timeZone == null){
                timeZone = "";
            }
        }catch(Exception e){
        }
    }

    //Get the current time in the city.
    public void getTime(Text text) throws Exception{

        if(timeZone.equals("")){
            text.setText("n/a");
        }
        else{
            //get UTC time
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String time = new LocalTime().parse(sdf.format(date)).plusSeconds(rOffset).
                    plusSeconds(dOffset).toString();

            Date date1 = new SimpleDateFormat("H:mm:ss").parse(time);

            text.setText(new SimpleDateFormat("h:mm:ss a").format(date1));
        }
    }

    //Calculate how much time left until sunrise or sunset.
    public void dayLightHours(Text text, Text textTitle) throws Exception{

        if(sunsetSeconds == 0 || sunriseSeconds == 0){
            text.setText("n/a");
            textTitle.setText("");
        }
        else{
            long sunrise = (long)(sunriseSeconds)*1000;
            long sunset = (long)(sunsetSeconds)*1000;

            String daylightStatus;

            //convert sunrise date/time (milliseconds) to UTC
            Date sunriseMilliseconds = new Date(sunrise);
            SimpleDateFormat sunriseSDF = new SimpleDateFormat("H:mm:ss");
            sunriseSDF.setTimeZone(TimeZone.getTimeZone("UTC"));

            //convert UTC sunrise time to the city's time zone (get the city's sunrise time)
            LocalTime sunriseTime = new LocalTime().parse(sunriseSDF.format(sunriseMilliseconds)).plusSeconds(rOffset).plusSeconds(dOffset);

            //convert sunset date/time (milliseconds) to UTC
            Date sunsetMilliseconds = new Date(sunset);
            SimpleDateFormat sunsetSDF = new SimpleDateFormat("H:mm:ss");
            sunsetSDF.setTimeZone(TimeZone.getTimeZone("UTC"));

            //convert UTC sunset time to the city's time zone (get the city's sunset time)
            LocalTime sunsetTime = new LocalTime().parse(sunsetSDF.format(sunsetMilliseconds)).plusSeconds(rOffset).plusSeconds(dOffset);

            //convert the local time to UTC
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            //convert the UTC time to the city's time zone (get the city's current time)
            LocalTime localTime = new LocalTime().parse(sdf.format(date)).plusSeconds(rOffset).
                    plusSeconds(dOffset);

            if(localTime.compareTo(sunriseTime) == 1 && localTime.compareTo(sunsetTime) != 1){
                LocalTime timeUntilSunset = sunsetTime.minusHours(localTime.getHourOfDay()).minusMinutes(localTime.getMinuteOfHour());
                daylightStatus = timeUntilSunset.getHourOfDay() + "h " + timeUntilSunset.getMinuteOfHour() + "min";

                textTitle.setText("COUNTDOWN TO SUNSET");
            }else{
                LocalTime timeUntilSunrise = sunriseTime.minusHours(localTime.getHourOfDay()).minusMinutes(localTime.getMinuteOfHour());
                daylightStatus = timeUntilSunrise.getHourOfDay() + "h " + timeUntilSunrise.getMinuteOfHour() + "min";

                textTitle.setText("COUNTDOWN TO SUNRISE");
            }
            if(localTime.compareTo(sunriseTime) == 0){
                textTitle.setText("THE SUN HAS RISEN");
                daylightStatus = "0h 0min";
            }
            if(localTime.compareTo(sunsetTime) == 0){
                textTitle.setText("THE SUN HAS SET");
                daylightStatus = "0h 0min";
            }
            text.setText(daylightStatus);
        }
    }

    //Get the current date in the city
    public void date(Text text) throws Exception{

        if(timeZone.equals("")){
            text.setText("");
        }else{
            //get UTC time
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            LocalTime local = new LocalTime().parse(sdf.format(date)).plusSeconds(rOffset).
                    plusSeconds(dOffset);

            Date date1 = new SimpleDateFormat("M:dd:y").parse(local.toDateTimeToday(DateTimeZone.forID(timeZone)).getMonthOfYear()
                    + ":" + local.toDateTimeToday(DateTimeZone.forID(timeZone)).getDayOfMonth() + ":" + local.toDateTimeToday(DateTimeZone.forID(timeZone)).getYear());

            text.setText(new SimpleDateFormat("MMM d").format(date1));
        }
    }

    //Grab city population data using the Wolfram Alpha API.
    public void cityPopulation() throws Exception{

        InputStream population = new URL("https://api.wolframalpha.com/v2/query?appid=API key goes here&input=population%20of%20"
                + URLEncoder.encode(longName, "UTF8").replace(" ", "%20") + "," +
                URLEncoder.encode(countryName, "UTF8").replace("_", "%20") + "&output=json").openStream();
        StringBuilder populationText = new StringBuilder();

        try {
            for(String string : IOUtils.readLines(population, (String) null)){

                populationText.append(string);
            }
        } finally {
            IOUtils.closeQuietly(population);
        }

        Wolfram wr = new Gson().fromJson(populationText.toString(), Wolfram.class);

        if(wr.queryresult.success != true){
            //If query result is not successful then pop value is null.
            pop = null;
        }
        else{
            //If more than one result is given then grab the relevant data.
            if(wr.queryresult.pods.get(1).subpods.get(0).plaintext.contains(countryName) && wr.queryresult.pods.get(1).subpods.get(0).plaintext.contains("|")){

                String wolframText = wr.queryresult.pods.get(1).subpods.get(0).plaintext
                        .substring(wr.queryresult.pods.get(1).subpods.get(0).plaintext.indexOf(countryName),
                                wr.queryresult.pods.get(1).subpods.get(0).plaintext.length());

                pop = wolframText.substring(countryName.length()+3, wolframText.length());
            }
            else{
                pop = wr.queryresult.pods.get(1).subpods.get(0).plaintext;

            }

            //If the population estimate date includes the full date (day, month, year) as opposed to just the year then remove the day to save space.
            if(pop.contains("Sunday") || pop.contains("Monday") || pop.contains("Tuesday") ||
                    pop.contains("Wednesday") ||pop.contains("Thursday") || pop.contains("Friday") || pop.contains("Saturday")){

                popEstimate = "(" + pop.substring(pop.indexOf(",")+2, pop.length());
            }
            else{
                popEstimate = pop.substring(pop.lastIndexOf("("), pop.length());
            }
        }
    }

    //Add a comma (if number is in the thousands, but below 100 thousand) or M (if number is in the millions) to the population value.
    //Add a K if the population is above 100 thousand but below 1 million
    public void getPopulationNumber(Text text) throws Exception{

        StringBuilder populationNum = new StringBuilder();
        int count = 0;

        if(!(pop.substring(0, pop.indexOf(" ")).contains("."))){

            try{
                if((Integer.parseInt(pop.substring(0, pop.indexOf(" "))) >= 1000)){

                    for(Character num : pop.substring(0, pop.indexOf(" ")).toCharArray()){
                        count++;

                        if(count == 4 && pop.substring(0, pop.indexOf(" ")).length() == 6){
                            populationNum.append(" K");
                            continue;
                        }
                        if(count == 3 && pop.substring(0, pop.indexOf(" ")).length() == 5){
                            populationNum.append("," + num);
                            continue;
                        }
                        if(count == 2 && pop.substring(0, pop.indexOf(" ")).length() == 4){
                            populationNum.append("," + num);
                        }
                        else{
                            if(count >= 4 && pop.substring(0, pop.indexOf(" ")).length() == 6){

                            }else{
                                populationNum.append(num);
                            }
                        }
                    }
                }

                text.setText(populationNum.toString());

            }catch(Exception e){
                //population is set to n/a and popEstimate is empty if the Wolfram Alpha API is unavailable for whatever reason.
                text.setText("n/a");
                popEstimate = "";
            }
        }else{
            if(pop.substring(0, pop.indexOf(" ")).length() == 5){
                text.setText(pop.substring(0, pop.indexOf(" ")-1) + " M");

            }else{
                text.setText(pop.substring(0, pop.indexOf(" ")) + " M");
            }
        }
    }

    //Grab the city's weather data and sunrise and sunset data using Open Weather Map API.
    public void weather() throws Exception{

        InputStream openWeather = new URL( "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon="
                + longitude + "&APPID=API key goes here").openStream();
        StringBuilder weather = new StringBuilder();

        try {
            for(String string : IOUtils.readLines(openWeather, (String) null)){
                weather.append(string);
            }

        } finally {
            IOUtils.closeQuietly(openWeather);
        }
        Weather weather1 = new Gson().fromJson(weather.toString(), Weather.class);

        try{
            temperature = Math.round((weather1.main.temp) - 273) + "° C";
            tempDescription = weather1.weather.get(0).description;
            sunsetSeconds = weather1.sys.sunset;
            sunriseSeconds = weather1.sys.sunrise;

            if(temperature == null){
                temperature = "";
            }
            if(tempDescription == null){
                tempDescription = "";
            }
            if(Integer.toString(sunsetSeconds) == null){
                sunsetSeconds = 0;
            }
            if(Integer.toString(sunriseSeconds) == null){
                sunriseSeconds = 0;
            }
        }catch(Exception e){
        }
    }

    //Set the temperature
    public void setTemp(Text text){
        tempText = text;
        tempText.setText(temperature);
        tempText.setFont(Font.loadFont(getClass().getResource("/fonts/SourceSansPro-SemiBold.ttf").toString(), 50));
    }

    //Properly capitalize the city name
    public String city(String city){

        boolean plus = false;
        StringBuilder aCity = new StringBuilder();
        int first = 0;
        int count = 0;

        try{
            for(char letter : city.toCharArray()){
                count++;

                if(Character.toString(letter).equalsIgnoreCase("+")){
                    aCity.append(letter);
                    plus = true;
                    continue;
                }
                if(plus == true){

                    //Don't capitalize words that shouldn't be capitalized.
                    if(Character.toString(letter).equalsIgnoreCase("d") && Character.toString(city.charAt(count+1)).equalsIgnoreCase("+") ||
                            Character.toString(letter).equalsIgnoreCase("l") && Character.toString(city.charAt(count+1)).equalsIgnoreCase("+")
                            || Character.toString(city.charAt(count)).equalsIgnoreCase("+")){

                        aCity.append(Character.toString(letter).toLowerCase());
                    }
                    else{
                        aCity.append(Character.toString(letter).toUpperCase());
                    }
                    plus = false;
                    continue;
                }
                if(first == 0){
                    aCity.append(Character.toString(letter).toUpperCase());
                    first++;
                }
                else {
                    aCity.append(Character.toString(letter).toLowerCase());
                }
            }
        }catch(Exception e){
        }

        return aCity.toString().replace("+", "_");
    }

    //Connect to the wiki page and grab the first relevant paragraph using jsoup and the wikiCrawl method.
    public String wikiConnect(){

        String description = "";

        try {
            List search = new ArrayList<String>();
            Collections.addAll(search,",_" + URLEncoder.encode(countryName, "UTF8"), ",_" + URLEncoder.encode(provinceName, "UTF8"), "_(city)", "");
            boolean linkFound = false;

            for(int i = 0; i < search.size(); i++){

                if(linkFound == false){
                    try{
                        Document doc = Jsoup.connect("http://en.wikipedia.org/wiki/" + city(URLEncoder.encode(longName, "UTF8").replace(" ", "+").replace("-", "+")) + search.get(i)).get();
                        Elements wikiPage = doc.select("p");

                        if (!(wikiPage.text().contains(countryName.replace("_", " "))) || wikiPage.text().contains("most commonly refers to:") || wikiPage.text().contains("may refer to:")) {

                            continue;
                        }else{

                            description = wikiCrawl(longName, wikiPage);
                            linkFound = true;
                        }
                    }catch(Exception e){
                        description = longName + " is a city in " + countryName + ".";
                        cityTitle = longName;
                    }
                }
            }
        }catch(Exception e){
        }

        return description;
    }

    //Crawl the wiki page to get a short description of the city.
    private String wikiCrawl(String city, Elements wiki){
        String cityDescription = "";
        int limit;
        boolean found = false;

        if(wiki.size() > 1){

            limit = 2;

        }else{
            limit = wiki.size();
        }

        try{
            for(int i = 0; i < limit; i++) {

                if (found == false && wiki.get(i).text().length() > city.length() && !(wiki.get(i).toString().contains("<p><a"))
                        && !(wiki.get(i).toString().contains("Motto")) && !(wiki.get(i).toString().contains("Sources")) && !(wiki.get(i).toString().contains("Nickname"))
                        && !(wiki.get(i).toString().contains("Coordinates")) && wiki.get(i).text().length() > 40) {

                    //Remove certain words and characters from the wiki description.
                    cityDescription = wiki.get(i).text().replaceAll("\\[[a-z]+]", "").replaceAll("\\[\\d*]", "")
                            .replaceAll("\\[note\\W[0-9+]]", "").replaceAll("\\W\\(\\Wlisten\\)", "")
                            .replaceAll("\\W\\(\\Wlisten\\W\\(help.info\\)\\)", "").replaceAll("\\W\\(\\Wpronunciation\\W\\(help.info\\)\\)", "").
                                    replaceAll("\\W\\Wlisten\\W\\(help.info\\)", "").replaceAll("\\W\\Wpronunciation\\W\\(help.info\\)", "");

                    cityTitle = city;
                    found = true;
                }
            }
        }catch(Exception e){
        }

        return cityDescription;
    }

    //Use only the first sentence of the paragraph grabbed using the wikiConnect method.
    public void cityDescription(Text description) throws Exception{

        StringBuilder wikiDescription = new StringBuilder();
        char[] wiki = wikiConnect().toCharArray();

        int count = 0;
        int a = 0;

        try{
            for(Character letter : wiki) {
                a++;

                if(Character.toString(letter).equals(".") && count != 1){
                    wikiDescription.append(letter);
                    continue;
                }
                else if (Character.toString(letter).matches("\\s") && count != 1 && wikiDescription.toString().endsWith(".")
                        && Character.toString(wiki[a]).equals(Character.toString(wiki[a]).toUpperCase())
                        && !(Character.toString(wiki[a-4]).equals("S")) && !(Character.toString(wiki[a-3]).equals("t"))
                        && !(Character.toString(wiki[a-5]).equals(" "))){

                    if(wikiDescription.toString().contains(")") || !(wikiDescription.toString().contains("("))){
                        wikiDescription.append(letter);
                        count++;
                    }else{
                        wikiDescription.append(letter);
                        continue;
                    }
                }
                else if(count != 1){
                    wikiDescription.append(letter);
                }
            }

            //If the length of the sentence is greater than 260 characters then just use up until the last comma in the
            //sentence. If necessary, loop this until below 260 characters.
            if(wikiDescription.toString().length() > 260){

                String shortDescription = wikiDescription.toString();
                boolean underLimit = false;

                while(underLimit != true){

                    String shorterDescription = shortDescription.substring(0, shortDescription.lastIndexOf(",")) + ".";

                    if(shorterDescription.length() > 260){

                        shortDescription = shorterDescription;

                    }else{
                        description.setText(shorterDescription);
                        underLimit = true;
                    }
                }
            }
            else{
                description.setText(wikiDescription.toString());
            }
        }catch(Exception e){
        }
    }
}

