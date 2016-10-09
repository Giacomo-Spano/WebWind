package windalarm.meteodata;

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

    public Windfinder() {
        super();
    }

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
        //String time = findBetweenKeywords(htmlResultString, "Report from local weather station at ", "local time.");
        String time = findBetweenKeywords(htmlResultString, "<span id=\"last-update\">", "</span>");
        String date = findBetweenKeywords(htmlResultString, "<div class=\"weathertable__header\">", "</div>");
        if (time == null || date == null)
            meteoStationData.datetime = meteoStationData.sampledatetime;
        else
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
            LOGGER.info("unparsable data: spotName=" + name);
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
