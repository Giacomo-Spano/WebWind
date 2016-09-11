package windalarm.meteodata;


import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class ClubVentos extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /*public ClubVentos() {
        super(AlarmModel.Spot_Jeri);
        webcamUrl = "";
        mImageName = "spot-" + id + ".jpg";
        name = "Jericoacoara (Brasile)";
        sourceUrl = "http://www.clubventos.com";
    }*/

    public ClubVentos() {
        super();
    }

    public MeteoStationData getMeteoData() {

        //String htmlResultString = getHTMLPage("http://www.clubventos.com/wind.php");
        String htmlResultString = getHTMLPage(meteodataUrl);
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();


        // speed
        String txt = htmlResultString;

        String keyword = "Jericoacoara Weather: ";
        int start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = ".";
        int end = txt.indexOf(keyword);
        txt = txt.substring(0, end);

        // 12/05/15 - 2:40 PM
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - hh:mm a");
        try {
            meteoStationData.sampledatetime = dateFormat.parse(txt);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // speed
        txt = htmlResultString;
        keyword = "Average wind";
        start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        keyword = ": ";
        start = txt.indexOf(keyword, start);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        keyword = "kts";
        end = txt.indexOf(keyword);
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
        meteoStationData.pressure = null;

        // humidity
        meteoStationData.humidity = null;

        // rain rate
        meteoStationData.rainrate = null;

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


        meteoStationData.datetime = Core.getDate();
        long difference = meteoStationData.datetime.getTime() - meteoStationData.sampledatetime.getTime();
        if (difference / 1000 / 60 > 60)
            meteoStationData.offline = true;
        else
            meteoStationData.offline = false;

        return meteoStationData;
    }
}
