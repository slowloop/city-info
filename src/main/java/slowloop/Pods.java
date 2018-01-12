package slowloop;

import java.util.List;

public class Pods {

    public String title;
    public String scanner;
    public String id;
    public int position;
    public boolean error;
    public int numsubpods;
    public List<Subpods> subpods;

    public static class Subpods{

        public String title;
        public Img img;
        public String plaintext;

        public static class Img{
            public String src;
            public String alt;
            public String title;
            public int width;
            public int height;
        }
    }
}
