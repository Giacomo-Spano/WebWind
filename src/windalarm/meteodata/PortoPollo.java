package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;


public class PortoPollo extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public PortoPollo() {
        super();
    }

    public MeteoStationData getMeteoData() {

        LOGGER.info("getMeteoData: spotName=" + name);

        String htmlResultString = getHTMLPage(meteodataUrl);
        if (htmlResultString == null)
            return null;


        MeteoStationData meteoStationData = new MeteoStationData();
        // sample date time
        meteoStationData.sampledatetime = Core.getDate();

        //speed
        String speed = findBetweenKeywords(htmlResultString, "id=\"vWindCurrent\" >", "Kts</span>");
        if (speed != null)
            meteoStationData.speed = Double.valueOf(speed);

        //average speed
        String avspeed = findBetweenKeywords(htmlResultString, "id=\"vWindAverage\" >", "Kts</span>");
        if (avspeed != null)
            meteoStationData.averagespeed = Core.getAverage(id);

        // direction
        String direction = findBetweenKeywords(htmlResultString, "id=\"vWindDirection\" >", "</strong>");
        direction = findBetweenKeywords(direction, "(", ")");
        if (direction != null) {
            meteoStationData.direction = getDirection(direction);
            meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);
        }

        //datetime
        meteoStationData.datetime = meteoStationData.sampledatetime;

        // temperature
        String temperature = findBetweenKeywords(htmlResultString, "id=\"vTemperatureActual\" >", "&deg;C</span>");
        if (temperature != null)
            meteoStationData.temperature = Double.valueOf(temperature);;

        //pressure
        String pressure = findBetweenKeywords(htmlResultString, "id=\"vPressure\" >", "hpa</span>");
        if (pressure != null)
            meteoStationData.pressure = Double.valueOf(pressure);

        // rainrate
        String rainrate = findBetweenKeywords(htmlResultString, "id=\"vRainDaily\" >", "mm</span>");
        if (rainrate != null)
            meteoStationData.rainrate = Double.valueOf(rainrate);

        // humidity
        String humidity = findBetweenKeywords(htmlResultString, "id=\"vHumidity\" >", "%</span>");
        if (humidity != null)
            meteoStationData.humidity = Double.valueOf(humidity);



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
