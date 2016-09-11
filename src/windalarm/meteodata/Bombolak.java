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

    /*public Bombolak() {
        super(AlarmModel.Spot_Sorico);
        webcamUrl = "http://bomboklat.it/wdmeteo/webcam.php";
        mImageName = "spot-" + id + ".jpg";
        name = "Sorico (Lago di como)";
        sourceUrl = "http://bomboklat.it";
    }*/

    public Bombolak() {
        super();
    }

    public MeteoStationData getMeteoData() {

        //String htmlResultString = getHTMLPage("http://bomboklat.it/wdmeteo/meteo.php");
        String htmlResultString = getHTMLPage(meteodataUrl);
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        meteoStationData.sampledatetime = Core.getDate();

        // speed
        String txt = htmlResultString;
        String keyword = "<span class='windspd'>";
        int start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());

        keyword = " kts";
        int end = txt.indexOf(keyword);
        txt = txt.substring(0, end);

        String[] split = txt.split(" ");

        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        meteoStationData.speed = MeteoStationData.knotsToKMh(Double.valueOf(split[1].trim()));// * 1.85200; // convert knots to km/h

        meteoStationData.direction = split[0].trim();
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        // temperature
        txt = htmlResultString;
        keyword = "Temperatura:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "<td class='meteotextbig'>";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "&deg;C";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end).trim();
        meteoStationData.temperature = Double.valueOf(txt.trim());

        // pressure
        meteoStationData.pressure = -1.0;

        // humidity
        txt = htmlResultString;
        txt = htmlResultString;
        keyword = "Umidit";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "<td class='meteotextbig'>";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "%";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        meteoStationData.humidity = Double.valueOf(txt.trim());

        // rain rate
        meteoStationData.rainrate = -1.0;

        // average speed
        meteoStationData.averagespeed = Core.getAverage(id);

        // date
        txt = htmlResultString;
        keyword = "Ultimo aggiornamento:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "class='meteotextyellow'>";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "</span>";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
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
