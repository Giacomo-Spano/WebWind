package windalarm.meteodata;

import Wind.AlarmModel;
import Wind.Core;
import com.sun.xml.internal.ws.api.model.MEP;

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
        String txt = rightOfKeywords(htmlResultString,"current-wind-speed");
        String speed = findBetweenKeywords(txt, ">", "<");
        if (speed == null)
            return null;
        meteoStationData.speed = Double.valueOf(speed);
        meteoStationData.speed = MeteoStationData.knotsToKMh(meteoStationData.speed);

        //average speed
        meteoStationData.averagespeed = Core.getAverage(id);

        // direction
        txt = rightOfKeywords(htmlResultString,"current-wind__dir");
        String direction = findBetweenKeywords(txt, ">", "<");
        direction = direction.trim();
        meteoStationData.direction = direction;
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);
        if (meteoStationData.directionangle == -1) {
            meteoStationData.directionangle = 0.0;
            LOGGER.severe("Cannod find direction " + direction);
        }

        //datetime

        txt = rightOfKeywords(htmlResultString,"data-spotmeta-update");
        String time = findBetweenKeywords(txt, ">", "<");
        txt = rightOfKeywords(htmlResultString,"weathertable__headline");
        String date = findBetweenKeywords(txt, ">", "<");
        if (date == null)
            return null;
        meteoStationData.datetime = getDate(date, time);

        // temperature
        txt = rightOfKeywords(htmlResultString,"current-temp-value");
        String temperature = findBetweenKeywords(txt, ">", "<");
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

        int idx = date.indexOf(",")+1;
        String month = date.substring(idx,idx+4).trim();
        String day = date.substring(idx+5).trim();
        Date yd = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(yd);
        int year = cal.get(Calendar.YEAR);
        String fulldate = day + "-" + month + "-" + year + " " + time;

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ITALIAN);

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
