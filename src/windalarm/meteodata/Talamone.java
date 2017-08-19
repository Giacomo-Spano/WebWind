package windalarm.meteodata;


import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

public class Talamone extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Talamone() {
        super();
    }


    public MeteoStationData getMeteoData() {

        LOGGER.info("getMeteoData: spotName=" + name);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

        String htmlResultString = getHTMLPage(meteodataUrl);
        if (htmlResultString == null)
            return null;

        String[] split = htmlResultString.replace("|", ";").split(";");

        MeteoStationData meteoStationData = new MeteoStationData();
        // sample date time
        meteoStationData.sampledatetime = Core.getDate();

        //speed
        String speed = split[26];
        if (speed == null) {
            return null;
        }
        meteoStationData.speed = Double.valueOf(speed.trim());
        meteoStationData.speed = MeteoStationData.knotsToKMh(meteoStationData.speed);

        //average speed
        meteoStationData.averagespeed = Core.getAverage(id);

        // direction
        String direction = split[25];
        if (direction == null) {
            return null;
        }
        meteoStationData.direction = direction;
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(direction.trim());

        //datetime
        String date = split[2];
        date = date.replace(" del ", " ");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        try {
            meteoStationData.datetime = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }


        // temperature
        String temperature =  split[5];
        if (temperature != null) {
            meteoStationData.temperature = Double.valueOf(temperature.trim());
        }

        //pressure
        String pressure = split[23];
        if (pressure != null) {
            meteoStationData.pressure = Double.valueOf(pressure.trim());
        }

        // humidity
        String humidity = split[19];
        if (humidity != null) {
            meteoStationData.humidity = Double.valueOf(humidity.trim());
        }

        // rainrate
        String rainrate = split[32];
        if (rainrate != null) {
            meteoStationData.rainrate = Double.valueOf(rainrate.trim());
        }


        return meteoStationData;
    }
}
