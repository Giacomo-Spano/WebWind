package windalarm.meteodata;


import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class WVC extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public WVC() {
        super(AlarmModel.Spot_Valmadrera);
        mWebcamUrl = "http://www.wcv.it/webcam05/currenth.jpg";
        mImageName = "spot-" + mSpotID + ".jpg";
        mName = "Valmadrera";
    }

    public MeteoStationData getMeteoData() {

        String htmlResultString = getHTMLPage("http://www.wcv.it/news.php");
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
        //cal.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        meteoStationData.sampledatetime = Core.getDate();//dateFormat.format(cal.getTime());
        //LOGGER.info("time in rome=" + meteoStationData.sampledatetime);
        //LOGGER.info("hour in rome=" + cal.get(Calendar.HOUR_OF_DAY));
        //DateTimeZone timeZone = DateTimeZone.forID( "Europe/Rome" );
        //meteoStationData.sampledatetime = dateFormat.setTimeZone(timeZone);

        String txt = htmlResultString;
        String keyword = "Velocit&agrave; del vento:";
        int start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<strong>";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "</strong>";
        int end = txt.indexOf("</strong>");
        txt = txt.substring(0, end);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        meteoStationData.speed = Double.valueOf(txt.trim().replace(',', '.'));

        txt = htmlResultString;
        start = txt.indexOf("Temperatura:");
        start = txt.indexOf("<strong>", start);
        txt = txt.substring(start + 8);
        end = txt.indexOf("</strong>");
        txt = txt.substring(0, end);
        meteoStationData.temperature = Double.valueOf(txt.trim());

        txt = htmlResultString;
        start = txt.indexOf("Pressione:");
        start = txt.indexOf("<strong>", start);
        txt = txt.substring(start + 8);
        end = txt.indexOf("</strong>");
        txt = txt.substring(0, end);
        meteoStationData.pressure = Double.valueOf(txt.trim());

        txt = htmlResultString;
        start = txt.indexOf("Umidit&agrave;:");
        start = txt.indexOf("<strong>", start);
        txt = txt.substring(start + 8);
        end = txt.indexOf("</strong>");
        txt = txt.substring(0, end);
        meteoStationData.humidity = Double.valueOf(txt.trim());

        txt = htmlResultString;
        start = txt.indexOf("Rain Rate:");
        start = txt.indexOf("<strong>", start);
        txt = txt.substring(start + 8);
        end = txt.indexOf("</strong>");
        txt = txt.substring(0, end);
        meteoStationData.rainrate = Double.valueOf(txt.trim().replace(',', '.'));

        txt = htmlResultString;
        start = txt.indexOf("Velocit&agrave; del vento:");
        start = txt.indexOf("img src=\"ws2files/", start);
        txt = txt.substring(start + 18);
        end = txt.indexOf(".gif\"></div>");
        txt = txt.substring(0, end);
        meteoStationData.direction = txt.trim();

        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        txt = htmlResultString;
        keyword = "Velocit&agrave; media:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<span class=\"avgvento\">";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + 23);
        keyword = "</span>";
        end = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        meteoStationData.averagespeed = Double.valueOf(txt.trim().replace(',', '.'));

        txt = htmlResultString;
        start = txt.indexOf("Dati aggiornati il ");
        start = txt.indexOf("<strong>", start);
        txt = txt.substring(start + 8);
        end = txt.indexOf("</strong>");
        txt = txt.substring(0, end);
        String date = txt.trim();

        txt = htmlResultString;
        start = txt.indexOf("alle ore ");
        start = txt.indexOf("<strong>", start);
        txt = txt.substring(start + 8);
        end = txt.indexOf("</strong>");
        txt = txt.substring(0, end);
        txt = txt.trim();
        txt = txt.replace(".", ":");
        String time = txt;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            meteoStationData.datetime = formatter.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return meteoStationData;
    }
}
