package windalarm.meteodata;


import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class Colico extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Colico() {
        super(AlarmModel.Spot_Colico);
        mWebcamUrl = "http://www.wcv.it/webcam02/currenth.jpg";
        mImageName = "spot-" + mSpotID + ".jpg";
        mName = "Colico";
    }

    public MeteoStationData getMeteoData() {

        String htmlResultString = getHTMLPage("http://web.tiscali.it/meteocolico/");
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        meteoStationData.sampledatetime = Core.getDate();
        //LOGGER.info("time in rome=" + meteoStationData.sampledatetime);
        //LOGGER.info("hour in rome=" + cal.get(Calendar.HOUR_OF_DAY));

        // speed
        String txt = htmlResultString;
        String keyword = "Il vento soffia verso:";
        int start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "alla velocita' di&nbsp;";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "&nbsp; km/h</b>";
        int end = txt.indexOf(keyword);
        txt = txt.substring(0, end);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        meteoStationData.speed = Double.valueOf(txt.trim());

        // direction
        txt = htmlResultString;
        keyword = "Il vento soffia verso:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<b>";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = ",";
        end = txt.indexOf(keyword);
        txt = txt.substring(0, end);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        meteoStationData.direction = txt.trim();
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        // temperature
        txt = htmlResultString;
        txt = htmlResultString;
        keyword = "Temperatura percepita:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<b>";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "C";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        meteoStationData.temperature = Double.valueOf(txt.trim());

        // pressure
        txt = htmlResultString;
        txt = htmlResultString;
        keyword = "Pressione:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<b>";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "&nbsp; mb&nbsp;";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        meteoStationData.pressure = Double.valueOf(txt.trim());

        // humidity
        txt = htmlResultString;
        txt = htmlResultString;
        keyword = "Umidita' max:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<b>";
        start = txt.indexOf(keyword, start);
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
        txt = htmlResultString;
        txt = htmlResultString;
        keyword = "Intensita' della pioggia:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<b>";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "&nbsp; mm/h";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        meteoStationData.rainrate = Double.valueOf(txt.trim());

        // average speed
        txt = htmlResultString;
        txt = htmlResultString;
        keyword = "Vento medio:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<b>";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "km/h";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        meteoStationData.averagespeed = Double.valueOf(txt.trim().replace(',', '.'));

        // date
        txt = htmlResultString;
        keyword = "Stazione Meteo loc. Campera";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = "<font size=\"4\" face=\"Arial, Helvetica, sans-serif\" color=\"#000000\">";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "</font>";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        txt = txt.trim();
        txt = txt.replace("\t","");
        /*String str = txt.substring(0,10);
        String date = txt.trim();
        str = txt.substring(11,16);
        String time = txt.trim();*/

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            meteoStationData.datetime = formatter.parse(txt);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return meteoStationData;
    }
}
