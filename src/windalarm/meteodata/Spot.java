package windalarm.meteodata;

import java.util.Date;

/**
 * Created by giacomo on 13/09/2015.
 */
public class Spot {
    public String name;
    public int ID;
    public String sourceUrl;
    public String webcamUrl;
    public String webcamUrl2;
    public String webcamUrl3;
    public Boolean offline = false;


    public Spot(String name, int ID,String sourceUrl, String webcamUrl, String webcamUrl2, String webcamUrl3) {
        this.name = name;
        this.ID = ID;
        this.sourceUrl = sourceUrl;
        this.webcamUrl = webcamUrl;
        this.webcamUrl2 = webcamUrl2;
        this.webcamUrl3 = webcamUrl3;
    }
}
