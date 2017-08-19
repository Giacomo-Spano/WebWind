package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.Core;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;


public class WindfinderForecast extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    private String windfinderid = "";
    private long spotId = -1;

    public WindfinderForecast(int spotid, String windfinderid) {
        this.windfinderid = windfinderid;
        this.spotId = spotid;
    }

    @Override
    MeteoStationData getMeteoData() {
        return null;
    }

    public WindfinderForecast() {
        super();
    }

    public WindForecast getForecastData() {

        LOGGER.info("getMeteoData: spotName=" + name);

        String htmlResultString = getHTMLPage("https://uk.windfinder.com/forecast/" + windfinderid /*marina_di_scarlino*/);
        if (htmlResultString == null)
            return null;

        WindForecast wf = new WindForecast(spotId);
        wf.source = FORECAST_WINDFINDER;
        wf.sourceId = windfinderid;

        String str = rightOfKeywords(htmlResultString, "<div class=\"weathertable forecast-day forecast forecast-day-8\">");
        String strBlock = str;

        while (strBlock != null) {

            String strDate = findBetweenKeywords(strBlock, "<div class=\"weathertable__header\">", "</div>");
            if (strDate == null)
                break;

            for (int i = 0; i < 8; i++) {

                String strHour = findBetweenKeywords(strBlock, "<div class=\"data-time weathertable__cell\">", "</div>");
                strHour = findBetweenKeywords(strHour, "<span class=\"value\">", "</span>");
                Date date = getDate(strDate, "" + strHour + ":00");
                wf.datetimes.add(date);

                String strDirection = findBetweenKeywords(strBlock, "<div class=\"cell-wind-2", "</div>");
                strDirection = findBetweenKeywords(strDirection, "title=\"", "&deg;");
                Double speedDir = Double.valueOf(strDirection);
                wf.speedDirs.add(speedDir);

                String strSpeed = findBetweenKeywords(strBlock, "<div class=\"speed\">", "</div>");
                strSpeed = findBetweenKeywords(strSpeed, "<span class=\"units-ws\">", "</span>");
                Double speed = Double.valueOf(strSpeed);
                wf.speeds.add(speed);

                String strMaxSpeed = findBetweenKeywords(strBlock, "<span class=\"units-ws\">", "</span>");
                Double maxSpeed = Double.valueOf(strMaxSpeed);
                wf.maxSpeeds.add(maxSpeed);

                String strTemperature = findBetweenKeywords(strBlock, "<span class=\"units-ws\">", "</span>");
                Double temperature = Double.valueOf(strMaxSpeed);
                wf.temperatures.add(temperature);


                strBlock = rightOfKeywords(strBlock, "<div class=\"data-time weathertable__cell\">");
            }


            str = rightOfKeywords(str, "<div class=\"weathertable forecast-day forecast forecast-day-8\">");
            if (str != null)
                strBlock = rightOfKeywords(str, "<div class=\"weathertable__header\">");
            else
                break;
        }

        return wf;
    }


    private Date getDate(String date, String time) {


        String dayofweek = date.substring(0, 3);
        int idx = date.indexOf(",") + 1;
        String month = date.substring(idx, idx + 4).trim();
        String day = date.substring(idx + 5).trim();
        Date yd = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(yd);
        int year = cal.get(Calendar.YEAR);
        //int month = cal.getFromName(Calendar.MONTH);
        //int day = cal.getFromName(Calendar.DAY_OF_MONTH);

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

    private String getDirection(String direction) {
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
