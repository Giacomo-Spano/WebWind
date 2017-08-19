package Wind;

import Wind.data.*;
import com.google.android.gcm.server.Message;
/*import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.DateTimeZone;
import com.google.appengine.repackaged.org.joda.time.LocalTime;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatterBuilder;*/

import org.json.JSONException;
import org.json.JSONObject;
import windalarm.meteodata.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by giacomo on 25/06/2015.
 */
public class AlarmModel {

    public static final String NotificationType_Alarm = "Alarm";
    public static final String NotificationType_Info = "info";
    public static final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
    private static final Logger LOGGER = Logger.getLogger(AlarmModel.class.getName());

    private ArrayList<PullData> mSpotDataList = new ArrayList<PullData>();
    private ArrayList<List<MeteoStationData>> meteoHistory;// = new ArrayList<ArrayList<MeteoStationData>>();
    public AlarmModel() {
        meteoHistory = new ArrayList<List<MeteoStationData>>();
    }

    public String getSpotName(int spotId) {

        for (Spot spot : mSpotDataList) {
            if (spot.getSpotId() == spotId)
                return spot.getName();
        }
        return "";
    }

    public void addSpotData(PullData spot) {

        mSpotDataList.add(spot);

        // inizializza i dati storici

        // TODO Questa roba non serve più credo, ora legge tutto da db
        Date end = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.HOUR_OF_DAY, -6); //minus number would decrement the datetimes
        Date start = cal.getTime();
        MeteoStationData md = new MeteoStationData();
        List<MeteoStationData> list = md.getHistory(Long.valueOf(spot.getSpotId()), start, end,-1L,-1);
        meteoHistory.add(list);
    }

    public ArrayList<PullData> getSpotList() {

        return mSpotDataList;
    }


    public void pullData() {


        for (PullData spot : mSpotDataList) {
            spot.pull();
        }
    }

    public void pullForecastData() {

        for (PullData spot : mSpotDataList) {
            if (spot.getOpenweathermapid() != null) {

                OpenWeatherForecast owf = new OpenWeatherForecast(spot.getSpotId(),spot.getOpenweathermapid());
                WindForecast wf = owf.getMeteoForecastData();
                WindForecastDataSource f = new WindForecastDataSource();
                f.insert(wf);
            }
        }


        WindForecast wf;
        WindForecastDataSource f = new WindForecastDataSource();

        /*OpenWeatherForecast owf = new OpenWeatherForecast(1,"524901");
        wf = owf.getMeteoForecastData();
        f.insert(wf);*/

        WindfinderForecast wff = new WindfinderForecast(1,"marina_di_scarlino");
        wf = wff.getForecastData();
        f.insert(wf);

        WindguruForecast wgf = new WindguruForecast(1,"49162"); // porto pollo
        wf = wgf.getForecastData();
        f.insert(wf);

        wgf = new WindguruForecast(2,"49011"); // puntone
        wf = wgf.getForecastData();
        f.insert(wf);
    }

    public boolean Add(MeteoStationData md, long spotID) {

        int index = getIndexFromSpotId(spotID);
        if (index < 0) return false;

        meteoHistory.get(index).add(md);

        if (meteoHistory.get(index).size() > 1000)
            meteoHistory.get(index).remove(0);

        return true;
    }

    public void evaluate(int windid, MeteoStationData md) {

        Date localTime = Core.getDate();
        Date localDate = localTime;
        LOGGER.info("--->evaluate ALARMS " + localDate.toString());

        evaluateAlarms(md.speed, md.averagespeed, localDate, md.spotID, windid);
        evaluateNotifications(md);

        LOGGER.info("<---evaluate ALARMS");
    }

    public MeteoStationData getLastMeteodatafromSpotId(long spotid) {

        int index = getIndexFromSpotId(spotid);
        if (index < 0 || index >= meteoHistory.size()) return null;

        int len = meteoHistory.get(index).size();
        if (len == 0) return null;

        MeteoStationData md = meteoHistory.get(index).get(len - 1);

        Spot spot = Core.getSpotFromID(spotid);
        if (spot.getOffline())
            md.offline = true;

        return md;
    }

    public List<MeteoStationData> getLastFavorites(String personId) {
        MeteoStationData md = new MeteoStationData();
        List<MeteoStationData> list = md.getLastFavorites(personId);
        return list;
    }

    public int getIndexFromSpotId(long spotID) {

        for (int i = 0; i < mSpotDataList.size(); i++) {
            if (mSpotDataList.get(i).getSpotId() == spotID)
                return i;
        }
        return -1;
    }

    public List<MeteoStationData> getLastSamples(long spotID, int maxSampleData) {

        MeteoStationData md = new MeteoStationData();
        List<MeteoStationData> list = md.getLastSamples(spotID, maxSampleData);

        return list;
    }

    public List<MeteoStationData> getHistory(long spotID, Date startDate, Date endDate, long lastWindId, int maxpoint) {

        MeteoStationData md = new MeteoStationData();
        List<MeteoStationData> list = md.getHistory(spotID, startDate, endDate, lastWindId,maxpoint);

        return list;
    }

    public PullData getSpotInfoFromId(long id) {
        for (int i = 0; i < mSpotDataList.size(); i++) {
            if (mSpotDataList.get(i).getSpotId() == id)
                return mSpotDataList.get(i);
        }
        return null;
    }

    public void setLastHighWindNotificationDate(long id, Date date) {
        for (int i = 0; i < mSpotDataList.size(); i++) {
            if (mSpotDataList.get(i).getSpotId() == id)
                mSpotDataList.get(i).lastHighWindNotificationSentDate = date;
        }
    }

    public void setLastIncreaseWindNotificationDate(long id, Date date) {
        for (int i = 0; i < mSpotDataList.size(); i++) {
            if (mSpotDataList.get(i).getSpotId() == id)
                mSpotDataList.get(i).lastWindIncreaseNotificationSentDate = date;
        }
    }

    public void evaluateNotifications(MeteoStationData md) {

        PullData spotdata = getSpotInfoFromId(md.spotID);

        if (md.averagespeed > 18.0) {

            long differenceInMinutes = -1;
            if (spotdata.lastHighWindNotificationSentDate != null)
                differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(Core.getDate().getTime() - spotdata.lastHighWindNotificationSentDate.getTime());
            else
                differenceInMinutes = -1;
            if (differenceInMinutes == -1 || differenceInMinutes > 30) {

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:m");
                Message notification = new Message.Builder()
                        .addData("title", md.spotName)
                        .addData("message", df.format(md.datetime) + " " + md.spotName + " - Vento forte " + md.speed + "km/h " +
                                md.direction + "(" + md.averagespeed + "km/h) -" + md.id)
                        .addData("spotID", "" + md.spotID)
                        .addData("notificationtype", AlarmModel.NotificationType_Info)
                        .addData("spotName", md.spotName)
                        .build();

                //List<Device> devices = Core.getDevices();
                Devices d = new Devices();
                List<Device> devices = d.getDevicesWithFavorites(md.spotID);


                //Core.sendPushNotification(devices, notification);
                for (Device device : devices) {
                    //Core.sendPushNotification(device.id, notification);
                    AlarmLog al = new AlarmLog();
                    al.insert("sendhighwind", 0, device.id, md.speed, md.averagespeed, md.spotID, 0, md.id);

                }

                setLastHighWindNotificationDate(md.spotID, Core.getDate());

            }
        } else if (md.trend > 300.0) {

            long differenceInMinutes = -1;
            if (spotdata.lastWindIncreaseNotificationSentDate != null)
                differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(Core.getDate().getTime() - spotdata.lastWindIncreaseNotificationSentDate.getTime());
            if (differenceInMinutes == -1 || differenceInMinutes > 30) {

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:m");
                Message notification = new Message.Builder()
                        .addData("title", md.spotName)
                        .addData("message", df.format(md.datetime) + " " + md.spotName + " - Vento in forte aumento (" + md.trend + ") -" + md.id)
                        .addData("spotID", "" + md.spotID)
                        .addData("spotName", md.spotName)
                        .addData("notificationtype", AlarmModel.NotificationType_Info)
                        .build();

                //List<Device> devices = Core.getDevices();
                Devices d = new Devices();
                List<Device> devices = d.getDevicesWithFavorites(md.spotID);

                //Core.sendPushNotification(devices, notification);
                for (Device device : devices) {
                    //Core.sendPushNotification(device.id, notification);
                    AlarmLog al = new AlarmLog();
                    al.insert("sendtrend", 0, device.id, md.trend, 0.0, md.spotID, 0, md.id);
                }
                setLastIncreaseWindNotificationDate(md.spotID, Core.getDate());
            }
        }
    }


    public static void evaluateAlarms(Double speed, Double avspeed, Date localDate, long spotId, int windid) {
        LOGGER.info("LocalDate=" + localDate);
        List<Alarm> list = WindDatastore.sendActiveAlarm(speed, avspeed, localDate, spotId, windid);

        LOGGER.info("list.size=" + list.size());
    }

    public static void sendAlarm(int deviceId, Alarm alarm, Double speed, Double avspeed, Date currentDate, long spotId, int windid) {

        LOGGER.info("sendAlarm spotID=" + spotId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        AlarmLog al = new AlarmLog();
        al.insert("sendalarm", alarm.id, deviceId, speed, avspeed, spotId, 0, windid);
        WindDatastore.updateAlarmLastRingDate(alarm.deviceId, alarm.id, currentDate);

        JSONObject data = new JSONObject();
        try {
            data.put("title", "titolox");
            data.put("alarmId", "" + alarm.id);
            data.put("spotID", "" + alarm.spotID);
            data.put("spotName", "" + Core.getSpotFromID(alarm.spotID).getName());
            data.put("startDate", "" + alarm.startDate);
            data.put("startTime", "" + alarm.startTime);
            data.put("lastRingTime", "" + alarm.lastRingTime);
            data.put("endDate", "" + alarm.endDate);
            data.put("endTime", "" + alarm.endTime);
            data.put("avspeed", "" + alarm.avspeed);
            data.put("speed", "" + alarm.speed);
            data.put("curspeed", "" + speed);
            data.put("curavspeed", "" + avspeed);
            data.put("curDate", "" + dateFormat.format(currentDate));
            data.put("curspotId", "" + spotId);
            data.put("notificationtype", AlarmModel.NotificationType_Alarm);
            data.put("windid", "" + windid);

            Core.sendPushNotification(deviceId, null, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }


       /* Message notification = new Message.Builder()
                .addData("title", "titolox")
                .addData("alarmId", "" + alarm.id)
                .addData("spotID", "" + alarm.spotID)
                .addData("spotName", "" + Core.getSpotFromID(alarm.spotID).getName())
                .addData("startDate", "" + alarm.startDate)
                .addData("startTime", "" + alarm.startTime)
                .addData("lastRingTime", "" + alarm.lastRingTime)
                .addData("endDate", "" + alarm.endDate)
                .addData("endTime", "" + alarm.endTime)
                .addData("avspeed", "" + alarm.avspeed)
                .addData("speed", "" + alarm.speed)
                .addData("curspeed", "" + speed)
                .addData("curavspeed", "" + avspeed)
                .addData("curDate", "" + dateFormat.format(currentDate))
                .addData("curspotId", "" + spotId)
                .addData("notificationtype", AlarmModel.NotificationType_Alarm)
                .addData("windid", "" + windid)
                .build();*/
        //Core.sendPushNotification(deviceId, notification);
    }

    public boolean getHistoricalMeteoData() {// inizializza historical meteo data

        String htmlResultString = "";
        MeteoStationData meteoStationData = new MeteoStationData();


        String path = "https://script.google.com/macros/s/AKfycbzbIMcKfuLjkp-37tUslU5TEq26D_GG2GAkMtNZqL0xhJWfE-XT/exec";

        path += "?sampledata=100";
        /*if (HomeServlet.ProductionEnvironment)
            path += "&production=true";
        else
            path += "&production=false";*/


        return true;//callGET(path,Spot_Valmadrera);
    }

    protected double getAverage(long spotID) {

        int samples = 5;
        List<MeteoStationData> list = getLastSamples(spotID, samples);
        if (list == null || list.size() == 0)
            return 0;

        double average = 0;
        int count = 0;
        for (MeteoStationData md : list) {
            average += md.speed;
            count++;
        }
        average = average / count;
        average = Math.round(average * 10) / 10.0; // aarotondaad un solo decimale
        return average;
    }

    protected double getTrend(long spotID, Date startDate, Date endDate) {

        int samples = 5;
        List<MeteoStationData> list = getLastSamples(spotID, samples);
        if (list == null || list.size() < samples)
            return 0;

        MeteoStationData startMeteodata;
        MeteoStationData endMeteodata;
        endMeteodata = (MeteoStationData) list.get(samples - 1);
        startMeteodata = (MeteoStationData) list.get(0);

        double trend = 0.0;
        long timeDifference = (endMeteodata.datetime.getTime() - startMeteodata.datetime.getTime()) / ONE_MINUTE_IN_MILLIS;
        if (timeDifference == 0)
            return 0;
        trend = (endMeteodata.averagespeed - startMeteodata.averagespeed) / timeDifference;
        trend = Math.round(trend * 100);
        trend = 10 * trend;
        return trend;
    }

    private boolean callGET(String path, int spot) {

        path += "&spot=" + getSpotName(spot);

        String htmlResultString;
        boolean result;
        try {
            URL jsonurl = new URL(path);

            LOGGER.info("jsonurl=" + jsonurl);

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

                //String txt = htmlResultString;
                try {
                    JSONObject json = new JSONObject(htmlResultString);
                    org.json.JSONArray jsonArray = json.getJSONArray("meteodata");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jObject2 = jsonArray.getJSONObject(i);
                        MeteoStationData md = new MeteoStationData();
                        md.fromJson(jObject2);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (res == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                result = callGET(location, spot);
                // OK
                //result = true;
            } else {
                // Server returned HTTP error code.
                LOGGER.info("Server returned HTTP error code" + res);
                return false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
