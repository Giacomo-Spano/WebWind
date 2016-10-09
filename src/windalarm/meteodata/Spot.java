package windalarm.meteodata;

/**
 * Created by giacomo on 13/09/2015.
 */
public class Spot {
    protected String name;
    protected long id;
    protected String webcamUrl;
    protected String webcamUrl2;
    protected String webcamUrl3;
    private String sourceUrl = "";
    protected String meteodataUrl = "";
    protected Boolean offline = false;

    protected String openweathermapId;
    protected String windguruId;
    protected String windfinderId;

    public Spot() {
    }

    public Spot(String name, int id,String sourceUrl, String webcamUrl, String webcamUrl2, String webcamUrl3) {
        this.name = name;
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.webcamUrl = webcamUrl;
        this.webcamUrl2 = webcamUrl2;
        this.webcamUrl3 = webcamUrl3;
    }

    public long getSpotId() {
        return id;
    }

    public void setSpotId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebcamUrl(int index) {
        switch (index) {
            case 1:
                return webcamUrl;
            case 2:
                return webcamUrl2;
            case 3:
                return webcamUrl3;
        }
        return null;
    }

    public void setWebcamUrl(int index, String webcamUrl) {
        switch (index) {
            case 1:
                this.webcamUrl = webcamUrl;
            case 2:
                this.webcamUrl2 = webcamUrl;
            case 3:
                this.webcamUrl3 = webcamUrl;
        }
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public boolean getOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public void setMeteodataUrl(String meteodataUrl) {
        this.meteodataUrl = meteodataUrl;
    }

    public void setWindguruId(String windguruId) {
        this.windguruId = windguruId;
    }

    public void setWindfinderId(String windfinderId) {
        this.windfinderId = windfinderId;
    }

    public void setOpenweathermapId(String openweathermapId) {
        this.openweathermapId = openweathermapId;
    }

    public String getOpenweathermapid() {
        return openweathermapId;
    }
}
