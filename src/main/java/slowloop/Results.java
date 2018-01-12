package slowloop;

import java.util.List;

public class Results {

    List<AddressComponents> address_components;
    String formatted_address;
    Geometry geometry;
    String place_id;
    List<String> types;

    public static class Geometry{

        Bounds bounds;
        Location location;
        String locationType;
        ViewPort viewport;

        public static class Bounds{

            Northeast northeast;
            Southwest southwest;

            public class Northeast{

                Double lat;
                Double lng;
            }
            public class Southwest{

                Double lat;
                Double lng;
            }
        }

        public static class Location{

            Double lat;
            Double lng;
        }

        public static class ViewPort{

            Northeast northeast;
            Southwest southwest;

            public class Northeast{

                Double lat;
                Double lng;
            }
            public class Southwest{

                Double lat;
                Double lng;
            }
        }

    }

}
