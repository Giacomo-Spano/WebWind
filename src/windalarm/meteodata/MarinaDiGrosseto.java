package windalarm.meteodata;


import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

public class MarinaDiGrosseto extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public MarinaDiGrosseto() {
        super();
    }


    public MeteoStationData getMeteoData() {

        LOGGER.info("getMeteoData: spotName=" + name);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

        String htmlResultString = getHTMLPage(meteodataUrl);
        if (htmlResultString == null)
            return null;

        MeteoStationData meteoStationData = new MeteoStationData();
        // sample date time
        meteoStationData.sampledatetime = Core.getDate();

        //speed
        String speed = rightOfKeywords(htmlResultString, "realtime-datas");
        if (speed != null) {
            //speed = findBetweenKeywords(htmlResultString, "<span class=\"temp\">", "</span>");
            //if (speed != null) {
                if (shortname.equals("torbole") || shortname.equals("rosignano")) {
                    speed = findBetweenKeywords(speed, "<strong>", "Km/h</strong>");
                    meteoStationData.speed = Double.valueOf(speed.trim());
                } else {
                    speed = findBetweenKeywords(speed, "<strong>", "kts</strong>");
                    speed = findBetweenKeywords(speed, "<span class=\"temp\">", "</span>");
                    meteoStationData.speed = MeteoStationData.knotsToKMh(Double.valueOf(speed.trim()));
                }

            //}
        }

        //average speed
        String avspeed = rightOfKeywords(htmlResultString, "Velocit&agrave;");
        avspeed = rightOfKeywords(avspeed, "<div align=\"center\">");
        avspeed = rightOfKeywords(avspeed, "<div align=\"center\">");
        if (avspeed != null) {
            if (shortname.equals("torbole") || shortname.equals("rosignano")) {
                avspeed = findBetweenKeywords(avspeed, "<strong>", "Km/h</strong>");
                meteoStationData.averagespeed = Double.valueOf(avspeed.trim());
            } else {
                avspeed = findBetweenKeywords(avspeed, "<strong>", "kts</strong>");
                meteoStationData.averagespeed = MeteoStationData.knotsToKMh(Double.valueOf(avspeed.trim()));
            }
        }

        // direction
        String direction = rightOfKeywords(htmlResultString, ">Direzione</td>");
        if (direction != null) {
            direction = findBetweenKeywords(direction, "<strong>", "</strong>");
            if (direction != null) {
                meteoStationData.direction = direction.trim();
                meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);
            }
        }

        //datetime
        String date = rightOfKeywords(htmlResultString, "Dati aggiornati il");
        if (date != null) {
            date = findBetweenKeywords(date, "<strong>", "</strong>");

            String time = rightOfKeywords(htmlResultString, "alle ore");
            if (time != null) {
                time = time.replace("ERR","");
                time = time.trim();

                time = findBetweenKeywords(time, "<strong>", "</strong>");

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH.mm", Locale.ENGLISH);
                try {
                    meteoStationData.datetime = df.parse(date + " " + time);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        // temperature
        String temperature = rightOfKeywords(htmlResultString, "Temperatura");
        if (temperature != null) {
            temperature = findBetweenKeywords(temperature, "<strong>", "&deg;C</strong>");
            if (temperature != null) {
                meteoStationData.temperature = Double.valueOf(temperature.trim());
            }
        }

        //pressure
        String pressure = rightOfKeywords(htmlResultString, "Pressione");
        if (pressure != null) {
            pressure = findBetweenKeywords(pressure, "<strong>", "hPa</strong>");
            if (pressure != null) {
                meteoStationData.pressure = Double.valueOf(pressure.trim());
            }
        }

        // humidity
        String humidity = rightOfKeywords(htmlResultString, "Umidit");
        if (humidity != null) {
            humidity = findBetweenKeywords(humidity, "<strong>", "%</strong>");
            if (humidity != null) {
                meteoStationData.humidity = Double.valueOf(humidity.trim());
            }
        }

        // rainrate
        String rainrate = rightOfKeywords(htmlResultString, "Rain Rate");
        if (rainrate != null) {
            rainrate = findBetweenKeywords(rainrate, "<strong>", "mm/h</strong>");
            if (rainrate != null) {
                meteoStationData.rainrate = Double.valueOf(rainrate.trim());
            }
        }


        long difference = meteoStationData.datetime.getTime() - meteoStationData.sampledatetime.getTime();
        if (difference / 1000 / 60 > 60)
           offline = true;
        else
           offline = false;

        return meteoStationData;
    }
}
