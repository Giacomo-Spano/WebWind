package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;


public class Windfinder extends PullData {


    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    //protected String mSpotUrl;

    public Windfinder() {
        super();
    }
    /*public Windfinder(long mSpotID) {
        super(mSpotID);

        switch ((int) mSpotID) {
            case (int) AlarmModel.Spot_Scarlino:
                mSpotUrl = "marina_di_scarlino";
                webcamUrl = "http://www.meteoindiretta.it/get_webcam.php?src=http%3A%2F%2Fwww.parallelo43.it%2Fwebcam%2Fmarinascarlino.jpg&w=630";
                mImageName = "spot-" + mSpotID + ".jpg";
                name = "Marina di Scarlino (Toscana)";
                break;
            case (int) AlarmModel.Spot_VassilikiPort:
                mSpotUrl = "lefkada_port?fspot=vasiliki";
                webcamUrl = "http://images.webcams.travel/preview/1323285421.jpg";
                //mImageName = AlarmModel.getSpotName(AlarmModel.Spot_Vassiliki) + ".jpg";
                mImageName = "spot-" + mSpotID + ".jpg";
                name = "Vassiliki Port - Lefkada (Grecia)";
                break;
            case (int) AlarmModel.Spot_Dakhla:
                mSpotUrl = "dakhla";
                name = "Dakhla (Marocco)";
                break;
        }
        sourceUrl = "www.windfinder.it";
    }*/


    public MeteoStationData getMeteoData(/*String name, String spot*/ /*lake_como_colico*/) {

        LOGGER.info("getMeteoData: spotName=" + name);

        String htmlResultString = getHTMLPage(meteodataUrl);
        if (htmlResultString == null)
            return null;


        MeteoStationData meteoStationData = new MeteoStationData();
        // sample date time
        meteoStationData.sampledatetime = Core.getDate();

        //speed
        String speed = findBetweenKeywords(htmlResultString, "<span class=\"current__wind__speed\">", "<span class=\"current__wind__unit\">kts</span>");
        if (speed != null)
            meteoStationData.speed = Double.valueOf(speed);

        //average speed
        meteoStationData.averagespeed = Core.getAverage(id);

        // direction
        String direction = findBetweenKeywords(htmlResultString, "<span class=\"current__wind__dir\">", "</span>");
        meteoStationData.direction = getDirection(direction);
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        //datetime
        String time = findBetweenKeywords(htmlResultString, "Report from local weather station at ", "local time.");
        String date = findBetweenKeywords(htmlResultString, "<div class=\"weathertable__header\">", "</div>");
        meteoStationData.datetime = getDate(date, time);

        // temperature
        String temperature = findBetweenKeywords(htmlResultString, "<span class=\"current__temp__value\">", "<span class=\"current__temp__unit\">");
        meteoStationData.temperature = Double.valueOf(temperature);;

        //pressure
        meteoStationData.pressure = null;

        // humidity
        meteoStationData.humidity = null;

        // rainrate
        meteoStationData.rainrate = null;

        return meteoStationData;
    }

    private String findBetweenKeywords(String txt, String startKeyword, String endKeyword) {

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

    private Date getDate(String date, String time) {



        String dayofweek = date.substring(0,3);
        int idx = date.indexOf(",")+1;
        String month = date.substring(idx,idx+4).trim();
        String day = date.substring(idx+5).trim();
        Date yd = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(yd);
        int year = cal.get(Calendar.YEAR);
        //int month = cal.get(Calendar.MONTH);
        //int day = cal.get(Calendar.DAY_OF_MONTH);

        String fulldate = day + "-" + month + "-" + year + " " + time;

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm", Locale.ENGLISH);

        try {
            Date d = df.parse(fulldate);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDirection (String direction ) {
        String txt = direction;
        if (txt == null) {
            return "";
        } else {
            txt = txt.replaceAll(" ", "");
            txt = txt.replaceAll("-", "");
            txt = txt.trim();
            txt = txt.toUpperCase();
            txt = txt.replaceAll("NORTH", "N");
            txt = txt.replaceAll("SOUTH", "S");
            txt = txt.replaceAll("EAST", "E");
            txt = txt.replaceAll("WEST", "O");
            return txt;
        }
    }
}
