package windalarm.meteodata;

import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class Bombolak extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Bombolak() {
        super();
    }

    public MeteoStationData getMeteoData() {

        String htmlResultString = getHTMLPage(meteodataUrl);
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        meteoStationData.sampledatetime = Core.getDate();

        // speed & direction
        String txt = findBetweenKeywords(htmlResultString,"<span class='windspd'>","</span");

        String[] split = txt.split(" ");

        meteoStationData.direction = split[0].trim();
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        String keyword = " kts";
        if (split[1].indexOf(keyword) != -1) {
            txt = leftOfKeywords(split[1],keyword).trim();
            meteoStationData.speed = MeteoStationData.knotsToKMh(Double.valueOf(txt));// * 1.85200; // convert knots to km/h
        } else {
            meteoStationData.speed = 0.0;
        }

        // temperature
        txt = findBetweenKeywords(htmlResultString,"Temperatura:","&deg;C");
        txt = rightOfKeywords(txt,"<td class='meteotextbig'>");
        meteoStationData.temperature = Double.valueOf(txt.trim());

        // pressure
        meteoStationData.pressure = -1.0;

        // humidity
        txt = findBetweenKeywords(htmlResultString,"Umidit","%");
        txt = rightOfKeywords(txt,"<td class='meteotextbig'>");
        meteoStationData.humidity = Double.valueOf(txt.trim());

        // rain rate
        meteoStationData.rainrate = -1.0;

        // average speed
        meteoStationData.averagespeed = Core.getAverage(id);

        // date
        txt = findBetweenKeywords(htmlResultString,"Ultimo aggiornamento:","</span>");
        txt = rightOfKeywords(txt,"class='meteotextyellow'>");
        txt = txt.trim();
        txt = txt.replace("\t","");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            meteoStationData.datetime = formatter.parse(txt);
            long difference = Core.getDate().getTime() - meteoStationData.sampledatetime.getTime();
            if (difference/1000/60 >  60)
                meteoStationData.offline = true;
            else
                meteoStationData.offline = false;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return meteoStationData;
    }
}
