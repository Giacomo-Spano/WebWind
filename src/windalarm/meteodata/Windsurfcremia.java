package windalarm.meteodata;

import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;


public class Windsurfcremia extends PullData {


    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Windsurfcremia() {
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

        //datetime
        String datetime = findBetweenKeywords(htmlResultString, "<caption>Conditions at local time ", "</caption>");
        //String date = findBetweenKeywords(htmlResultString, "<div class=\"weathertable__header\">", "</div>");
        meteoStationData.datetime = getDate(datetime);

        // temperature
        String temperature = findBetweenKeywords(htmlResultString, "<td>Temperature</td>", "</td>");
        temperature = findBetweenKeywords(temperature, "<td>", "&nbsp;&deg;C");
        temperature = temperature.replace(",",".");
        meteoStationData.temperature = Double.valueOf(temperature);

        //speed
        String speed = findBetweenKeywords(htmlResultString, "<td>Wind&nbsp;Speed&nbsp;(gust)</td>", "</td>");
        speed = findBetweenKeywords(speed, "<td>", "&nbsp;km/h");
        speed = speed.replace(",",".");
        meteoStationData.speed = Double.valueOf(speed);

        //average speed
        speed = findBetweenKeywords(htmlResultString, "<td>Wind&nbsp;Speed&nbsp;(avg)</td>", "</td>");
        speed = findBetweenKeywords(speed, "<td>", "&nbsp;km/h");
        speed = speed.replace(",",".");
        meteoStationData.speed = Double.valueOf(speed);

        // direction
        String direction = findBetweenKeywords(htmlResultString, "<td>Wind Bearing</td>", "</td>");
        direction = rightOfKeywords(direction, "&deg;");
        direction = direction.trim();
        meteoStationData.direction = direction;

        // directionangle
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        //pressure
        String pressure = findBetweenKeywords(htmlResultString, "<td>Barometer&nbsp;</td>", "</td>");
        pressure = findBetweenKeywords(pressure, "<td>", "&nbsp;mb");
        pressure = pressure.replace(",",".");
        meteoStationData.pressure = Double.valueOf(pressure);

        // humidity
        String humidity = findBetweenKeywords(htmlResultString, "<td>Humidity</td>", "</td>");
        humidity = findBetweenKeywords(humidity, "<td>", "%");
        humidity = humidity.replace(",",".");
        meteoStationData.humidity = Double.valueOf(humidity);

        // rainrate
        String rainrate = findBetweenKeywords(htmlResultString, "<td>Rainfall&nbsp;Rate</td>", "</td>");
        rainrate = findBetweenKeywords(rainrate, "<td>", "&nbsp;mm/hr");
        rainrate = rainrate.replace(",",".");
        meteoStationData.rainrate = Double.valueOf(rainrate);

        return meteoStationData;
    }



    private Date getDate(String datetime) {

        String keyword = "on";
        String time = leftOfKeywords(datetime,keyword);
        if (time == null)
            return null;
        time = time.trim();


        String date = rightOfKeywords(datetime,keyword);

        if (date == null)
            return null;
        date = date.trim();

        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm", Locale.ITALIAN);

        try {
            Date d = df.parse(date + " " + time);
            return d;
        } catch (ParseException e) {
            LOGGER.info("unparsable data: spotName=" + name);
            e.printStackTrace();
        }
        return null;
    }

    /*private String getDirection (String direction ) {
        String txt = direction;
        if (txt == null) {
            return "";
        } else {
            txt = txt.trim();
            txt = txt.toUpperCase();
            txt = txt.replaceAll("NORTH", "N");
            txt = txt.replaceAll("SOUTH", "S");
            txt = txt.replaceAll("EAST", "E");
            txt = txt.replaceAll("WEST", "O");
            return txt;
        }
    }*/
}
