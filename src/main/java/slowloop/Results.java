package slowloop;

import java.util.List;

public class Results {

    List<AddressComponents> address_components;
    Geometry geometry;
    List<String> types;

    public static class Geometry{

        Location location;

        public static class Location{

            Double lat;
            Double lng;
        }
    }
}
