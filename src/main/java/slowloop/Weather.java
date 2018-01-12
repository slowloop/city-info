package slowloop;

import java.util.List;

public class Weather {


    public List<CityWeather> weather;
    public Main main;
    public Sys sys;
    public String name;


    public static class Main{
        public double temp;
        public double pressure;
        public int humidity;
        public double temp_min;
        public double temp_max;
        public double sea_level;
        public double grnd_level;
    }

    public static class Sys{
        public int type;
        public int id;
        public double message;
        public String country;
        public int sunrise;
        public int sunset;
    }
}

