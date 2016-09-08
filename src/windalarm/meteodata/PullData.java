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
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giacomo on 13/08/2015.
 */
public abstract class PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected int mSpotID;// = AlarmModel.Spot_Valmadrera;
    protected String mName;
    protected String mWebcamUrl = null;
    protected String mWebcamUrl2 = null;
    protected String mWebcamUrl3 = null;
    protected String mImageName = "";
    protected String mImageName2 = "";
    private String mImageName3  = "";
    protected String mSource = "";
    public Boolean offline = false;
    public Date lastHighWindNotificationSentDate;
    public Date lastWindIncreaseNotificationSentDate;



    public int getSpotID() {
        return mSpotID;
    }
    public String getName() {
        return mName;
    }
    public String getWebcamUrl(int index) {
        switch (index) {
            case 1:
                return mWebcamUrl;
            case 2:
                return mWebcamUrl2;
            case 3:
                return mWebcamUrl3;
        }
        return null;
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


                long minutes = 30;
                Date startTime = new Date(md.datetime.getTime() - (minutes * AlarmModel.ONE_MINUTE_IN_MILLIS));
                md.trend = Core.getTrend(mSpotID, startTime, md.datetime);
                Core.sendData(md, mSpotID);

                if (mWebcamUrl != "")
                    Core.sendImage(mWebcamUrl, mImageName);
                if (mWebcamUrl2 != "")
                    Core.sendImage(mWebcamUrl2, mImageName2);
                if (mWebcamUrl3 != "")
                    Core.sendImage(mWebcamUrl3, mImageName3);
            }

        } catch (Exception e) {
            LOGGER.severe("cannot get data for spot " + mName + "("+mSpotID+")");
        }

    }

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
