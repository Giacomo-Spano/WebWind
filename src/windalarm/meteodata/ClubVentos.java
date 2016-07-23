package windalarm.meteodata;


import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class ClubVentos extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public ClubVentos() {
        super(AlarmModel.Spot_Jeri);
        mWebcamUrl = "";
        mImageName = "spot-" + mSpotID + ".jpg";
        mName = "Jericoacoara (Brasile)";
        mSource = "http://www.clubventos.com";
    }

    public MeteoStationData getMeteoData() {

        String htmlResultString = getHTMLPage("http://www.clubventos.com/wind.php");
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
        //String keyword = "Peak wind";   // non va bene, questa � la raffica max giornaliera
        String keyword = "Average wind";
        int start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = ": ";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "kts";
        int end = txt.indexOf(keyword);
        txt = txt.substring(0, end);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        meteoStationData.speed = MeteoStationData.knotsToKMh(Double.valueOf(txt.trim()));// * 1.85200; // convert knots to km/h

        // direction
        meteoStationData.direction = "";
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        // temperature
        txt = htmlResultString;
        keyword = "Temperature";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = ": ";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = " ";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end).trim();
        meteoStationData.temperature = Double.valueOf(txt.trim());

        // pressure
        meteoStationData.pressure = -1.0;

        // humidity
        meteoStationData.humidity = -1.0;

        // rain rate
        meteoStationData.rainrate = -1.0;

        // average speed
        txt = htmlResultString;
        keyword = "Average wind";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = ": ";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "kts";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end).trim();
        meteoStationData.averagespeed = Double.valueOf(txt.trim());

        // date
        txt = htmlResultString;
        keyword = "Jericoacoara Weather:";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = ": ";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = ".";
        end = txt.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(0, end);
        txt = txt.trim();
        //txt = txt.replace("\t","");
        /*String str = txt.substring(0,8);
        String date = txt.trim();
        str = txt.substring(10,17);
        String time = txt.trim();*/

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - K:mm a");
        try {
            meteoStationData.datetime = formatter.parse(txt);

            long difference = meteoStationData.datetime .getTime() - Core.getDate() .getTime();
            if (difference/1000/60 >  60)
                meteoStationData.offline = true;
            else
                meteoStationData.offline = false;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*meteoStationData.spotName = mName;
        meteoStationData.spotID = mSpotID;
        meteoStationData.trend = getTrend();*/

        return meteoStationData;
    }
}
