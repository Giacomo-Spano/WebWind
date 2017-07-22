package windalarm.meteodata;


import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class WCVold extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public WCVold() {
        super();
    }


    public MeteoStationData getMeteoData() {

        LOGGER.info("getMeteoData: spotName=" + name);

        String htmlResultString = getHTMLPage(meteodataUrl/*"http://www.wcv.it/news.php"*/);
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        meteoStationData.sampledatetime = Core.getDate();//dateFormat.format(cal.getTime());


        // date
        String txt = rightOfKeywords(htmlResultString, "Dati aggiornati il");
        String val = findBetweenKeywords(txt, "<strong>", "</strong>");
        String date = "";
        if (val != null)
            date = val.trim();

        // time
        txt = rightOfKeywords(htmlResultString, "alle ore");
        val = findBetweenKeywords(txt, "<strong>", "</strong>");
        String time = "";
        if (val != null) {
            val = val.replace(".",":");
            time = val.trim();
        }

        // direction
        txt = rightOfKeywords(htmlResultString, "direzione");
        val = findBetweenKeywords(txt, "<span class=\"td_value\">", "</span>");
        val = findBetweenKeywords(val, "http://www.wcv.it/ws2files/", ".png");
        if (val != null) {
            meteoStationData.direction = val.trim();
            meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);
        }
        //speed
        txt = rightOfKeywords(txt, "attuale");
        val = findBetweenKeywords(txt, "<span class=\"td_value\">", "</span>");
        if (val != null)
            meteoStationData.speed = Double.valueOf(val.trim());

        //average speed
        txt = rightOfKeywords(txt, "media");
        val = findBetweenKeywords(txt, "<span class=\"td_value\">", "</span>");
        if (val != null)
            meteoStationData.averagespeed = Double.valueOf(val.trim());

        // temperature
        txt = rightOfKeywords(txt, "temp");
        val = findBetweenKeywords(txt, "<span class=\"td_value\">", "</span>");
        if (val != null)
            meteoStationData.temperature = Double.valueOf(val.trim());

        // humidity
        txt = rightOfKeywords(txt, "umidit");
        val = findBetweenKeywords(txt, "<span class=\"td_value\">", "</span>");
        if (val != null)
            meteoStationData.humidity = Double.valueOf(val.trim());

        // pressure
        txt = rightOfKeywords(txt, "pressione");
        val = findBetweenKeywords(txt, "<span class=\"td_value\">", "</span>");
        if (val != null)
            meteoStationData.pressure = Double.valueOf(val.trim());

        // pioggia
        txt = rightOfKeywords(txt, "pioggia");
        val = findBetweenKeywords(txt, "<span class=\"td_value\">", "</span>");
        if (val != null)
            meteoStationData.rainrate = Double.valueOf(val.trim());


        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            meteoStationData.datetime = formatter.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

       long difference = meteoStationData.datetime.getTime() - meteoStationData.sampledatetime.getTime();
        if (difference / 1000 / 60 > 60)
           offline = true;
        else
           offline = false;

        return meteoStationData;
    }
}
