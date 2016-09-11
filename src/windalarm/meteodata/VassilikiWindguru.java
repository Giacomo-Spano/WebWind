package windalarm.meteodata;


import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.logging.Logger;

public class VassilikiWindguru extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /*public VassilikiWindguru() {
        super(AlarmModel.Spot_VassilikiWindguru);
        webcamUrl = "http://images.webcams.travel/preview/1323285421.jpg";
        mImageName = "spot-" + id + ".jpg";
        name = "Vassiliki wguru";
        sourceUrl = "https://beta.windguru.cz/station/63";
    }*/

    public VassilikiWindguru() {
        super();
    }

    public MeteoStationData getMeteoData() {

        String htmlResultString = getHTMLPage(meteodataUrl);
        //String htmlResultString = getHTMLPage("https://beta.windguru.cz/station/63");
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        meteoStationData.sampledatetime = Core.getDate();

        // speed
        String txt = htmlResultString;
        String keyword = "<span class=\"wgs_wind_avg_value\">";
        int start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start/* + keyword.length()*/);

        keyword = "</span>";
        int end = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
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
        keyword = "Umidita':";
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
