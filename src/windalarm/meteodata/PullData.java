package windalarm.meteodata;

import Wind.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created by giacomo on 13/08/2015.
 */
public abstract class PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected int mSpotID;// = AlarmModel.Spot_Valmadrera;
    protected String mName;
    protected String mWebcamUrl = "";
    protected String mImageName = "";
    protected String mSource = "";
    public Boolean offline = false;

    public int getSpotID() {
        return mSpotID;
    }
    public String getName() {
        return mName;
    }
    public String getWebcamUrl() {
        return mWebcamUrl;
    }
    public String getSourceUrl() {
        return mSource;
    }


    abstract MeteoStationData getMeteoData();

    public PullData(int spotID) {
        this.mSpotID = spotID;
        mName = "";//AlarmModel.getSpotName(spotID);
    }


    public void pull(){

        try {
            MeteoStationData md = getMeteoData();
            if (md != null) {
                md.spotName = mName;
                md.spotID = mSpotID;
                md.trend = Core.getTrend(mSpotID);
                Core.sendData(md, mSpotID);

                if (mWebcamUrl != "")
                    Core.sendImage(mWebcamUrl/*"http://www.wcv.it/webcam05/currenth.jpg"*/, mImageName);
            }

        } catch (Exception e) {
            LOGGER.severe("cannot get data for spot " + mName + "("+mSpotID+")");
        }

    }



    /*protected double getTrend() {

        ArrayList<MeteoStationData> list = AlarmModel.getHistory(mSpotID);

        double trend = 0;
        if (list == null || list.size() == 0)
            return 0;

        int samples = 5;
        if (samples > list.size())
            samples = list.size();

        double initialAverageSpeed = list.get(list.size() - samples).averagespeed;

        for (int i = 1; i < samples; i++) {
            trend += (list.get(list.size() - samples + i).averagespeed - initialAverageSpeed);
        }
        trend = Math.round(trend * 10) / 10;
        return trend;
    }*/
    /*protected double getAverage() {

        ArrayList<MeteoStationData> list = AlarmModel.getHistory(mSpotID);

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

            //LOGGER.info("jsonurl=" + jsonurl);

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
