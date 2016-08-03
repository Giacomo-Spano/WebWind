package Wind;

import com.google.android.gcm.server.Message;
import windalarm.meteodata.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Core {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());

    public static String  APP_DNS_OPENSHIFT = "jbossews-giacomohome.rhcloud.com";
    protected static String appDNS_envVar;
    protected static String mysqlDBHost_envVar;
    protected static String mysqlDBPort_envVar;
    protected static String tmpDir_envVar;

    private static final String USER = "root";
    private static final String PASS = "giacomo";
    private static String DB_URL = "jdbc:mysql://127.0.0.1:3306/winddb";
    private Devices mDevices = new Devices();


    public static String getUser() {
        if (appDNS_envVar.equals(APP_DNS_OPENSHIFT))
            return "adminzdVX5dl";
        else
            return "root";
    }
    public static String getPassword() {
        if (appDNS_envVar.equals(APP_DNS_OPENSHIFT))
            return "eEySMcJ6WCj4";
        else
            return "giacomo";
    }
    public static String getDbUrl() {
        if (appDNS_envVar.equals(APP_DNS_OPENSHIFT)) {
            return "jdbc:mysql://" + mysqlDBHost_envVar + ":" + mysqlDBPort_envVar + "/" + "jbossews";
        }
        else
            return "jdbc:mysql://127.0.0.1:3306/winddb";
    }

    public static String getTmpDir() {
        if (appDNS_envVar.equals(APP_DNS_OPENSHIFT)) {
            return tmpDir_envVar;
        }
        else
            return "c:\\scratch";
    }

    private static AlarmModel alarmModel = new AlarmModel();
    public  static WindDatastore windDatastore = new WindDatastore();


    public static ArrayList<Spot> getSpotList() {
        return alarmModel.getSpotList();
    }

    public Core() {

        appDNS_envVar = System.getenv("OPENSHIFT_APP_DNS");
        mysqlDBHost_envVar = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
        mysqlDBPort_envVar = System.getenv("OPENSHIFT_MYSQL_DB_PORT");

        tmpDir_envVar = System.getenv("OPENSHIFT_TMP_DIR");

    }

    public static void sendPushNotification(int deviceId, Message notification) {

        LOGGER.info("sendPushNotification");
        //new PushNotificationThread(type,title,description,value).start();
        new PushNotificationThread(deviceId, notification).start();

        LOGGER.info("sendPushNotification sent");
    }

    public static int addDevice(Device device) {

        Devices devices = new Devices();
        return devices.insert(device);
    }

    public static void removeDevice(String regId) {

        Devices devices = new Devices();
        devices.delete(regId);
    }

    public static List<Device> getDevicesFromDeviceId(int deviceID) {
        Devices devices = new Devices();
        return devices.getDeviceFromDeviceId(deviceID);
    }

    public static Date getDate() {

        //LOGGER.info("getDate");
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

        final String dateInString;// = df.format(date);

        if (appDNS_envVar.equals(APP_DNS_OPENSHIFT)) {


            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY, 6); //minus number would decrement the hours
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

    public static double getAverage(int spotID) {

        return 0;
        //return alarmModel.getAverage(spotID);
    }

    public static double getTrend(int spotID) {
        return alarmModel.getTrend(spotID);
    }

    /*public static ArrayList<MeteoStationData> getLast() {

        return alarmModel.getLast();
    }*/

    public static MeteoStationData getLastfromID(int id) {

        return alarmModel.getLastfromID(id);
    }


    public static List<MeteoStationData> getHistory(int spotID) {
        return alarmModel.getHistory(spotID,100);
    }

    public void init() {

        PullData d;

        d = new VassilikiWindguru();
        alarmModel.addSpotData(d);

        d = new ClubVentos();
        alarmModel.addSpotData(d);

        d = new Colico();
        alarmModel.addSpotData(d);

        d = new Bombolak();
        alarmModel.addSpotData(d);

        d = new WVC();
        alarmModel.addSpotData(d);

        d = new CML(AlarmModel.Spot_Abbadia);
        alarmModel.addSpotData(d);

        d = new CML(AlarmModel.Spot_Dervio);
        alarmModel.addSpotData(d);

        d = new CML(AlarmModel.Spot_Dongo);
        alarmModel.addSpotData(d);

        d = new CML(AlarmModel.Spot_Gravedona);
        alarmModel.addSpotData(d);

        d = new CML(AlarmModel.Spot_Gera);
        alarmModel.addSpotData(d);


        d = new Windfinder(AlarmModel.Spot_VassilikiPort);
        alarmModel.addSpotData(d);

        d = new gvlnifollonica();
        alarmModel.addSpotData(d);

        //d = new Vassiliki();
        //alarmModel.addSpotData(d);

        d = new Windfinder(AlarmModel.Spot_Scarlino);
        alarmModel.addSpotData(d);

        d = new Windfinder(AlarmModel.Spot_Dakhla);
        alarmModel.addSpotData(d);

        alarmModel.getHistoricalMeteoData();
    }

    public static Spot getSpotFromID(int id) {

        int index = alarmModel.getIndexFromID(id);
        if (index < 0) return null;
        return alarmModel.getSpotList().get(index);
    }


    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";

    public void updateMeteoData() {

        alarmModel.pullData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();

        try {
            meteocentralech m = new meteocentralech();
            ArrayList<Forecast> list = m.getMeteoData(meteocentralech.LUGANO);
            sendForecastToWorksheet(list, dateFormat.format(cal.getTime()), "Lugano");
        } catch (Exception e) {
            LOGGER.severe("cannot get data for meteo svizzera lugano");
        }
        try {
            meteocentralech m = new meteocentralech();
            ArrayList<Forecast> list = m.getMeteoData(meteocentralech.ZURIGO);
            sendForecastToWorksheet(list, dateFormat.format(cal.getTime()), "Zurigo");
        } catch (Exception e) {
            LOGGER.severe("cannot get data for meteo zurigo");
        }
        try {
            meteocentralech m = new meteocentralech();
            ArrayList<Forecast> list = m.getMeteoData(meteocentralech.VALMADRERA);
            sendForecastToWorksheet(list, dateFormat.format(cal.getTime()), "Valmadrera");
        } catch (Exception e) {
            LOGGER.severe("cannot get data for meteo valmadrera");
        }


        /*CML cml = new CML();
        try {
            LOGGER.info("DEBUG - getMeteoData");
            MeteoStationData mdAbbadia = cml.getMeteoData("Abbadia", CML.Abbadia);
            LOGGER.info("DEBUG - sendData");
            sendData(mdAbbadia, AlarmModel.Spot_Abbadia);

        } catch (Exception e) {
            LOGGER.severe("cannot get data for abbadia" + e.toString());
        }*/
        /*try {
            MeteoStationData mdAbbadia = cml.getMeteoData(AlarmModel.getSpotName(AlarmModel.Spot_Gera), CML.Gera);
            sendData(mdAbbadia, AlarmModel.Spot_Gera);

        } catch (Exception e) {
            LOGGER.severe("cannot get data for gera" + e.toString());
        }
        try {
            MeteoStationData mdAbbadia = cml.getMeteoData(AlarmModel.getSpotName(AlarmModel.Spot_Dongo), CML.Dongo);
            sendData(mdAbbadia, AlarmModel.Spot_Dongo);

        } catch (Exception e) {
            LOGGER.severe("cannot get data for dongo" + e.toString());
        }
        try {
            MeteoStationData mdAbbadia = cml.getMeteoData(AlarmModel.getSpotName(AlarmModel.Spot_Dervio), CML.Dervio);
            sendData(mdAbbadia, AlarmModel.Spot_Dervio);

        } catch (Exception e) {
            LOGGER.severe("cannot get data for dervio" + e.toString());
        }*/

        alarmModel.evaluate();

    }

    public static void sendData(MeteoStationData meteoData, int spotID) {
        //mdList.add(meteoData);
        meteoData.insert();
        alarmModel.Add(meteoData, spotID);

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
    public boolean sendForecastToWorksheet(ArrayList<Forecast> list, String sampledate, String spotname) {

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
}
