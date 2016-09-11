package windalarm.meteodata;

import Wind.AlarmModel;
import Wind.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giacomo on 13/08/2015.
 */
public abstract class PullData extends Spot {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    //protected int id;// = AlarmModel.Spot_Valmadrera;
    //protected String name;
    //protected String webcamUrl = null;
    //protected String webcamUrl2 = null;
    //protected String webcamUrl3 = null;
    protected String mImageName = "";
    protected String mImageName2 = "";
    private String mImageName3  = "";
    public Boolean offline = false;
    public Date lastHighWindNotificationSentDate;
    public Date lastWindIncreaseNotificationSentDate;



    abstract MeteoStationData getMeteoData();

    /*public PullData(long spotID) {
        this.id = spotID;
        name = "";//AlarmModel.getSpotName(spotID);
    }*/

    public PullData() {
        mImageName = "spot-" + id + ".jpg";
        mImageName2 = "spot-" + id + "-2.jpg";
    }

    /*public PullData(long id, String name, String webcamUrl, String webcamUrl2, String webcamUrl3, String sourceUrl) {
        this.id = id;
        this.name = name;
        webcamUrl = webcamUrl;
        webcamUrl2 = webcamUrl2;
        webcamUrl3 = webcamUrl3;
        sourceUrl = sourceUrl;
    }*/


    public void pull(){

        try {
            MeteoStationData md = getMeteoData();
            if (md != null) {
                md.spotName = name;
                md.spotID = id;


                long minutes = 30;
                Date startTime = new Date(md.datetime.getTime() - (minutes * AlarmModel.ONE_MINUTE_IN_MILLIS));
                md.trend = Core.getTrend(id, startTime, md.datetime);
                Core.sendData(md, id);

                if (webcamUrl != "")
                    Core.sendImage(webcamUrl, mImageName);
                if (webcamUrl2 != "")
                    Core.sendImage(webcamUrl2, mImageName2);
                if (webcamUrl3 != "")
                    Core.sendImage(webcamUrl3, mImageName3);
            }

        } catch (Exception e) {
            LOGGER.severe("cannot get data for spot " + name + "("+ id +")");
        }

    }

    /*protected double getAverage() {

        ArrayList<MeteoStationData> list = AlarmModel.getHistory(id);

        double average = 0;
        int samples = 5;
        if (samples > list.size())
            samples = list.size();

        for (int i = 0; i < samples; i++) {
            average += list.get(list.size() - 1 - i).speed;
        }
        average = average / samples;
        average = Math.round(average * 10) / 10;
        return average;
    }*/

    public String getHTMLPage(String url) {

        String htmlResultString = "";

        try {
            URL jsonurl = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(false);
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestMethod("GET");
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            BufferedReader rd = new BufferedReader(reader);
            String line = "";
            htmlResultString = "";
            while ((line = rd.readLine()) != null) {
                htmlResultString += line;
            }
            reader.close();

            int res = connection.getResponseCode();
            if (res == HttpURLConnection.HTTP_OK) {

                return htmlResultString;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }


}
