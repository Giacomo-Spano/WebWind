package Wind;

import Wind.data.*;
import Wind.notification.PushNotificationThread;
import com.google.android.gcm.server.Message;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import windalarm.meteodata.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Core {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());

    public static String APP_DNS_OPENSHIFT = "jbossews-giacomohome.rhcloud.com";
    public static String APP_DNS_OPENSHIFTTEST = "jbossewsbeta-giacomohome.rhcloud.com";
    //public static String APP_DNS_OPENSHIFTTEST = "jbossewstest-giacomohome.rhcloud.com";


    protected static String appDNS_envVar;
    protected static String mysqlDBHost_envVar;
    protected static String mysqlDBPort_envVar;
    protected static String tmpDir_envVar;
    protected static String dataDir_envVar;
    private static String version = "0.11";

    static  boolean production = false;

    public static boolean isProduction() {

        return production;
    }

    public static String getUser() {

        if (appDNS_envVar == null)
            return "root";

        if (appDNS_envVar.equals(APP_DNS_OPENSHIFT))
            return "adminzdVX5dl";
        else if (appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST))
            return "adminjNm7VUk";
            //return "adminw8ZVVu2";
        else
            //return "adminzdVX5dl";// production
            return "root";
    }

    public static String getPassword() {
        if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFT))
            return "eEySMcJ6WCj4";
        else if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST))
            return "xX1MAIXQLLHq";
            //return "MhbY-61ZlqU4";
        else
            //return "eEySMcJ6WCj4"; //production
            return "giacomo";
    }

    public static String getDbUrl() {

        if (appDNS_envVar == null)
            return "jdbc:mysql://127.0.0.1:3306/windalarm";

        if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFT)) { // production
            return "jdbc:mysql://" + mysqlDBHost_envVar + ":" + mysqlDBPort_envVar + "/" + "";
        } else if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST)) { // test
            return "jdbc:mysql://" + mysqlDBHost_envVar + ":" + mysqlDBPort_envVar + "/" + "windalarm";
            //return "jdbc:mysql://" + mysqlDBHost_envVar + ":" + mysqlDBPort_envVar + "/" + "jbossews";
        }
        //test
        return "jdbc:mysql://127.0.0.1:3306/windalarm";
        //return "jdbc:mysql://127.0.0.1:3307/jbossews"; // production
    }

    public static String getTmpDir() {
        /*if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFT)) {
            return tmpDir_envVar;
        } else if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST)) {
            return tmpDir_envVar;
        } else*/
            return System.getenv("tmp");
            //return System.getProperty("java.io.tmpdir");
    }

    public static String getDataDir() {
        if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFT)) {
            return dataDir_envVar;
        } else if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST)) {
            return dataDir_envVar;
        } else
            //return "c:\\scratch";
            return System.getProperty("java.io.tmpdir");
    }

    private static AlarmModel alarmModel = new AlarmModel();
    public static WindDatastore windDatastore = new WindDatastore();


    public static ArrayList<PullData> getSpotList() {
        return alarmModel.getSpotList();
    }

    public Core() {

        appDNS_envVar = System.getenv("OPENSHIFT_APP_DNS");
        mysqlDBHost_envVar = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
        mysqlDBPort_envVar = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
        tmpDir_envVar = System.getenv("OPENSHIFT_TMP_DIR");
        dataDir_envVar = System.getenv("OPENSHIFT_DATA_DIR");

        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\temp")  ||
                tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 7.0\\temp"))
            production = false;
        else
            production = true;
    }

    public static void sendPushNotification(int deviceId, Message notification) {

        LOGGER.info("sendPushNotification");
        //new PushNotificationThread(type,title,description,value).start();
        new PushNotificationThread(deviceId, notification).start();

        LOGGER.info("sendPushNotification sent");
    }

    public static void sendPushNotification(List<Device> devices, Message notification) {

        new PushNotificationThread(devices, notification).start();

    }

    public static int addDevice(Device device) {

        Devices devices = new Devices();
        return devices.insert(device);
    }

    public static int addUser(String personId, String personName, String personEmail, String authCode, String authcode, String name) {

        Users users = new Users();
        return users.insert(personId, personName, personEmail, authCode);
    }

    public static void removeDevice(String regId) {

        Devices devices = new Devices();
        devices.delete(regId);
    }

    public static Device getDevicesFromDeviceId(int deviceID) {
        Devices devices = new Devices();
        return devices.getDeviceFromDeviceId(deviceID);
    }

    public static List<Device> getDevices() {

        LOGGER.info("getDevices");
        Devices devices = new Devices();
        return devices.getDevices();
    }

    public static Date getDate() {

        //LOGGER.info("getDate");
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

        final String dateInString;// = df.format(date);

        if (appDNS_envVar != null && (appDNS_envVar.equals(APP_DNS_OPENSHIFT) || appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST))) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY, 6); //minus number would decrement the datetimes
            Date tzdate = cal.getTime();
            dateInString = df.format(tzdate);
        } else {
            dateInString = df.format(date);
        }

        //final String dateInString = df.format(date);

        Date newDate = null;
        try {

            newDate = df.parse(dateInString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    //-------------------------------------

    public static double getAverage(long spotID) {

        //return 0;
        return alarmModel.getAverage(spotID);
    }

    public static double getTrend(long spotID, Date startDate, Date endDate) {
        return alarmModel.getTrend(spotID, startDate, endDate);
    }



    public static List<MeteoStationData> getLastFavorites(String personid) {

        return alarmModel.getLastFavorites(personid);
    }

    public static List<MeteoStationData> getHistory(long spotID, Date start, Date end, long windId) {
        return alarmModel.getHistory(spotID, start, end, windId);
    }

    public static String getVersion() {
        return version;
    }


    public void init() {

        SpotList sl = new SpotList();
        List<PullData> list = sl.getSpotList();
        for (PullData spot : list) {
            alarmModel.addSpotData(spot);
        }

        instantiateBOT();
    }

    public void instantiateBOT() {
        // TODO Initialize Api Context
        ApiContextInitializer.init();

        // TODO Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // TODO Register our bot
        try {
            botsApi.registerBot(new MyAmazingBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static Spot getSpotFromID(long id) {

        int index = alarmModel.getIndexFromSpotId(id);
        if (index < 0) return null;
        return alarmModel.getSpotList().get(index);
    }

    public static MeteoStationData getLastMeteoData(long spotid) {

        return alarmModel.getLastMeteodatafromSpotId(spotid);
    }

    /*public static MeteoStationData getLastfromID(long id) {

        return alarmModel.getLastMeteodatafromSpotId(id);
    }*/


    public static Spot getSpotFromShortName(String shortname) {

        for (Spot spot : alarmModel.getSpotList()) {
            if (spot.getShortName().equalsIgnoreCase(shortname))
                return spot;
        }
        return null;
    }

    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

    public void updateMeteoData() {

        alarmModel.pullData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();

        /*
        try {
            meteocentralech m = new meteocentralech();
            ArrayList<CHMeteoForecast> list = m.getMeteoData(meteocentralech.LUGANO);
            sendForecastToWorksheet(list, dateFormat.format(cal.getTime()), "Lugano");
        } catch (Exception e) {
            LOGGER.severe("cannot get data for meteo svizzera lugano");
        }
        try {
            meteocentralech m = new meteocentralech();
            ArrayList<CHMeteoForecast> list = m.getMeteoData(meteocentralech.ZURIGO);
            sendForecastToWorksheet(list, dateFormat.format(cal.getTime()), "Zurigo");
        } catch (Exception e) {
            LOGGER.severe("cannot get data for meteo zurigo");
        }
        try {
            meteocentralech m = new meteocentralech();
            ArrayList<CHMeteoForecast> list = m.getMeteoData(meteocentralech.VALMADRERA);
            sendForecastToWorksheet(list, dateFormat.format(cal.getTime()), "Valmadrera");
        } catch (Exception e) {
            LOGGER.severe("cannot get data for meteo valmadrera");
        }*/
    }


    public void pullForecastData() {

        alarmModel.pullForecastData();

    }

    public static boolean getImage(String imagepath, String imageName) {

        try {
            BufferedImage bufferedImage;

            URL imageUrl = new URL(imagepath);
            System.setProperty("java.io.tmpdir", Core.getTmpDir());
            ImageIO.setUseCache(false);
            bufferedImage = ImageIO.read(imageUrl);

            // create a blank, RGB, same width and height, and a white background
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

            String path = imageName;

            //if(!Core.isProduction())
                //path = System.getenv("tmp") + "/" + imageName + ".jpg";
            //else
              //  tmpDir = System.getProperty("java.io.tmpdir");


            boolean res = ImageIO.write(newBufferedImage, "jpg", new File(path));
            return res;

        } catch (MalformedURLException ex) {
            LOGGER.severe("Debug - MalformedURLException error: " + ex.toString());
            return false;
        } catch (IOException ioe) {
            LOGGER.severe("Debug - IOException error: " + ioe.toString());
            return false;
        } catch (Exception e) {
            LOGGER.severe("Debug - Exception error: " + e.toString());
            return false;
        }
    }

    public static void sendData(MeteoStationData meteoData, long spotID) {
        //mdList.add(meteoData);
        int windId = meteoData.insert();
        alarmModel.Add(meteoData, spotID);
        alarmModel.evaluate(windId, meteoData);
        //sendToWorksheet(meteoData);
    }

    private boolean sendXivelydata(MeteoStationData data) {
        /*try {
            Xively xively = new Xively(HomeServlet.WindAlarmAPIKey, HomeServlet.WindAlarmfeedID);
            XivelyJson xjson = new XivelyJson();

            String webduinodatetime = data.date + "T" + data.time;
            xjson.addstream("webdatetime", "'" + webduinodatetime + "'");
            xjson.addstream("speed", data.speed.toString());
            xjson.addstream("averagespeed", data.averagespeed.toString());
            xjson.addstream("direction", data.direction);
            //xively.WriteMultipleDatapoints(xjson);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }*/
        return true;
    }

    public static boolean sendToWorksheet(MeteoStationData data) {

        boolean result;

        //String url = HomeServlet.WindAlarmGoogleScript;
        String url = "https://script.google.com/macros/s/AKfycbzbIMcKfuLjkp-37tUslU5TEq26D_GG2GAkMtNZqL0xhJWfE-XT/exec";// = "https://script.google.com/macros/s/AKfycbzbIMcKfuLjkp-37tUslU5TEq26D_GG2GAkMtNZqL0xhJWfE-XT/exec";
        //String url = "https://script.google.com/macros/s/AKfycbzbIMcKfuLjkp-37tUslU5TEq26D_GG2GAkMtNZqL0xhJWfE-XT/exec
        //https://script.google.com/macros/s/AKfycbzbIMcKfuLjkp-37tUslU5TEq26D_GG2GAkMtNZqL0xhJWfE-XT/exec


        String params = "";
        params += "speed=" + data.speed;
        params += "&avspeed=" + data.averagespeed;
        //params += "&date=" + data.date;
        //params += "&time=" + data.time;
        params += "&direction=" + data.direction;
        params += "&pressure=" + data.pressure;
        params += "&humidity=" + data.humidity;
        params += "&temperature=" + data.temperature;
        params += "&rainrate=" + data.rainrate;
        params += "&sampledatetime=" + data.sampledatetime;

        params += "&production=true";
        /*if (HomeServlet.ProductionEnvironment)
            params += "&production=true";
        else
            params += "&production=false";*/


        params += "&type=UPDATE";

        LOGGER.info("url=" + params);

        params += "&spot=" + data.spotName.trim(); // elimino gli spazi perch� alrimenti la get si blocca

        result = callget(url, params);
        return result;

    }

    public static boolean sendImage(String imageUrl, String name) {

        boolean result;

        /*String url = HomeServlet.WindAlarmImageGoogleScript;
        String params = "";
        params += "command=" + "SAVE";
        params += "&imageurl=" + imageUrl;
        params += "&imagename=" + name;
        if (HomeServlet.ProductionEnvironment)
            params += "&production=true";
        else
            params += "&production=false";

        LOGGER.info("url=" + params);

        result = callget(url, params);
        return result;*/

        return true;
    }

    //private static
    public boolean sendForecastToWorksheet(ArrayList<CHMeteoForecast> list, String sampledate, String spotname) {

       /* boolean result;

        String json = "%7B";//"{";
        json += "%22sampledate%22:%22" + sampledate + "%22";
        json += ",%22spotname%22:%22" + spotname + "%22";

        json += ",%22days%22:";
        json += "%5B";//"[";
        for (int i = 0; i < list.size(); i++) {

            if (i != 0)
                json += ",";
            json += list.get(i).toJson();


        }
        json += "%5D"; //"]";
        json += "%7D";//"}";

        String url = HomeServlet.WindAlarmForecastGoogleScript;
        //String url = HomeServlet.WindAlarmGoogleScript;
        String params = "";
        params += "type=forecast";
        params += "&json=" + json;
        if (HomeServlet.ProductionEnvironment)
            params += "&production=true";
        else
            params += "&production=false";

        result = callget(url, params);
        return result;*/

        return true;
    }


    private boolean callpost(String url, String urlParameters) {
        String htmlResultString;
        boolean result;
        try {
            if (!urlParameters.isEmpty())
                url += "?" + urlParameters;
            URL jsonurl = new URL(url);
            LOGGER.info("Xjsonurl=" + jsonurl);
            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(false);

            // Send post request
            connection.setDoOutput(false);
            /*if (!urlParameters.isEmpty()) {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
            }*/


            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            BufferedReader rd = new BufferedReader(reader);
            String line = "";
            htmlResultString = "";
            while ((line = rd.readLine()) != null) {
                htmlResultString += line;
            }
            //LOGGER.info("htmlResultString=" + htmlResultString);
            reader.close();
            int res = connection.getResponseCode();

            if (res == HttpURLConnection.HTTP_OK) {
                // OK
                result = true;
            } else if (res == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                result = callget(location, "");
                // OK
                //result = true;
            } else {
                // Server returned HTTP error code.
                LOGGER.severe("Server returned HTTP error code" + res);
                result = false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = false;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private static boolean callget(String url, String urlParameters) {
        String htmlResultString;
        boolean result;
        try {
            if (!urlParameters.isEmpty())
                url += "?" + urlParameters;
            URL jsonurl = new URL(url);

            //LOGGER.info("Xjsonurl=" + jsonurl);
            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);

            // Send post request
            /*connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();*/

            int res = connection.getResponseCode();


            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            BufferedReader rd = new BufferedReader(reader);
            String line = "";
            htmlResultString = "";
            while ((line = rd.readLine()) != null) {
                htmlResultString += line;
            }
            //LOGGER.info("htmlResultString=" + htmlResultString);
            reader.close();


            if (res == HttpURLConnection.HTTP_OK) {
                // OK
                result = true;
            } else if (res == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                result = callget(location, "");
                // OK
                //result = true;
            } else {
                // Server returned HTTP error code.
                LOGGER.severe("Server returned HTTP error code" + res);
                result = false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = false;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public void dbMaintenance() {


    }
}
