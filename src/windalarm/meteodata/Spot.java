package windalarm.meteodata;

/**
 * Created by giacomo on 13/09/2015.
 */
public class Spot {
    public String name;
    public int ID;
    public String sourceUrl;
    public String webcamUrl;
    public Boolean offline = false;

    public Spot(String name, int ID,String sourceUrl, String webcamUrl) {
        this.name = name;
        this.ID = ID;
        this.sourceUrl = sourceUrl;
        this.webcamUrl = webcamUrl;
    }
}
