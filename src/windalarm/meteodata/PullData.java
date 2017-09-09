package windalarm.meteodata;

import Wind.AlarmModel;
import Wind.Core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giacomo on 13/08/2015.
 */
public abstract class PullData extends Spot {

    public static final String FORECAST_WINDFINDER = "Windfinder";
    public static final String FORECAST_WINDGURU = "Windguru";
    public static final String FORECAST_OPENWEATHERMAP = "OpenWeathermap";

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public Boolean offline = false;
    public Date lastHighWindNotificationSentDate;
    public Date lastWindIncreaseNotificationSentDate;

    abstract MeteoStationData getMeteoData();

    public PullData() {

    }

    public void pull() {

        try {
            MeteoStationData md = getMeteoData();
            if (md != null) {
                md.spotName = name;
                md.spotID = id;

                // evaluate wind trend
                long minutes = 30;
                Date startTime = new Date(md.datetime.getTime() - (minutes * AlarmModel.ONE_MINUTE_IN_MILLIS));
                md.trend = Core.getTrend(id, startTime, md.datetime);

                // normalizza il simbolo della direzione
                md.direction = md.getSymbolFromAngle(md.directionangle);

                // metti offline se ultima lettura più vecchia di 60 minuti
                long difference = md.datetime.getTime() - md.sampledatetime.getTime();
                if (difference / 1000 / 60 > 60)
                    offline = true;
                else
                    offline = false;

                if (Core.sendData(md, id)) {
                    offline = false;
                } //  Non metto offline tru se senddata ritorna zero perchè potrebbe essere un dato duplicato.
            }
        } catch (Exception e) {
            LOGGER.severe("cannot getFromName data for spot " + name + "(" + id + ")");
            offline = true;
        }

        try {
            String filepath = System.getenv("tmp") + "/";
            if (webcamUrl != "") {

                //  filepath = System.getProperty("java.io.tmpdir") + "/" + imageName + ".jpg";
                Core.getImage(webcamUrl, filepath + id + "webcam.jpg");
            }
            if (webcamUrl2 != "") {
                Core.getImage(webcamUrl2, filepath + id + "webcam2.jpg");
            }
            if (webcamUrl3 != "") {
                Core.getImage(webcamUrl3, filepath + id + "webcam3.jpg");
            }
        } catch (Exception e) {
            LOGGER.severe("cannot getFromName webcam for spot " + name + "(" + id + ")");
        }
    }

    public String getHTMLPage(String url) {
        return getHTMLPage(url,false);
    }

    public String getHTMLPage(String url, boolean withNewLine) {

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
                if (withNewLine)
                    htmlResultString += "\n";
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

    /*public static void getWebcamImage(String imagepath, String imageName) {

        try {
            BufferedImage bufferedImage;

            URL imageUrl = new URL(imagepath);
            System.setProperty("java.io.tmpdir", Core.getTmpDir());
            bufferedImage = ImageIO.read(imageUrl);

            // create a blank, RGB, same width and height, and a white background
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

            String path = Core.getDataDir() + "/" + imageName + ".jpeg";
            ImageIO.write(newBufferedImage, "jpg", new File(path));

        } catch (MalformedURLException ex) {
            LOGGER.severe("Debug - MalformedURLException error: " + ex.toString());
            return;
        } catch (IOException ioe) {
            LOGGER.severe("Debug - IOException error: " + ioe.toString());
            return;
        }
    }*/

    protected String findBetweenKeywords(String txt, String startKeyword, String endKeyword) {

        int start = txt.indexOf(startKeyword);
        if (start == -1)
            return null;
        txt = txt.substring(start + startKeyword.length());
        int end = txt.indexOf(endKeyword);
        if (end == -1)
            return null;
        txt = txt.substring(0, end);
        txt = txt.trim();
        if (txt.equals(""))
            return null;

        return txt;
    }

    protected String leftOfKeywords(String txt, String endKeyword) {

        int end = txt.indexOf(endKeyword);
        if (end == -1)
            return null;
        txt = txt.substring(0, end);
        txt = txt.trim();
        if (txt.equals(""))
            return null;

        return txt;
    }

    protected String rightOfKeywords(String txt, String startKeyword) {

        int start = txt.indexOf(startKeyword);
        if (start == -1)
            return null;
        txt = txt.substring(start + startKeyword.length());

        return txt;
    }
}
