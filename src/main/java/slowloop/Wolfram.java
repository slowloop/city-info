package slowloop;

import java.util.List;

public class Wolfram {

    public Result queryresult;

    public static class Result {

        public boolean success;
        public boolean error;
        public int numpods;
        public String datatypes;
        public String timedout;
        public String timedoutpods;
        public double timing;
        public double parsetiming;
        public boolean parsetimedout;
        public String recalculate;
        public String id;
        public String host;
        public String server;
        public String related;
        public String version;
        public List<Pods> pods;
    }
}
