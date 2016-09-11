package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.AlarmModel;
import Wind.Core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;


public class Windfinder extends PullData {


    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    //protected String mSpotUrl;

    public Windfinder() {
        super();
    }
    /*public Windfinder(long mSpotID) {
        super(mSpotID);

        switch ((int) mSpotID) {
            case (int) AlarmModel.Spot_Scarlino:
                mSpotUrl = "marina_di_scarlino";
                webcamUrl = "http://www.meteoindiretta.it/get_webcam.php?src=http%3A%2F%2Fwww.parallelo43.it%2Fwebcam%2Fmarinascarlino.jpg&w=630";
                mImageName = "spot-" + mSpotID + ".jpg";
                name = "Marina di Scarlino (Toscana)";
                break;
            case (int) AlarmModel.Spot_VassilikiPort:
                mSpotUrl = "lefkada_port?fspot=vasiliki";
                webcamUrl = "http://images.webcams.travel/preview/1323285421.jpg";
                //mImageName = AlarmModel.getSpotName(AlarmModel.Spot_Vassiliki) + ".jpg";
                mImageName = "spot-" + mSpotID + ".jpg";
                name = "Vassiliki Port - Lefkada (Grecia)";
                break;
            case (int) AlarmModel.Spot_Dakhla:
                mSpotUrl = "dakhla";
                name = "Dakhla (Marocco)";
                break;
        }
        sourceUrl = "www.windfinder.it";
    }*/


    public MeteoStationData getMeteoData(/*String name, String spot*/ /*lake_como_colico*/) {

        LOGGER.info("getMeteoData: spotName=" + name);

        String htmlResultString = getHTMLPage(meteodataUrl);
        //String htmlResultString = getHTMLPage("https://uk.windfinder.com/forecast/" + mSpotUrl);
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
        //cal.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        meteoStationData.sampledatetime = Core.getDate();
        LOGGER.info("time in rome=" + meteoStationData.sampledatetime);
        LOGGER.info("hour in rome=" + cal.get(Calendar.HOUR_OF_DAY));
        //DateTimeZone timeZone = DateTimeZone.forID( "Europe/Rome" );
        //meteoStationData.sampledatetime = dateFormat.setTimeZone(timeZone);

        //<span id="current-windspeed">1</span>


        String txt = htmlResultString;
        String keyword = "<span class=\"current__wind__speed\">";
        int start = txt.indexOf(keyword);
        txt = txt.substring(start + keyword.length());
        int end = txt.indexOf("<span class=\"current__wind__unit\">kts</span>");
        txt = txt.substring(0, end);
        meteoStationData.speed = MeteoStationData.knotsToKMh(Double.valueOf(txt));// * 1.85200; // convert knots to km/h

        meteoStationData.averagespeed = Core.getAverage(id);

        txt = htmlResultString;
        keyword = "<span class=\"current__wind__dir\">";
        start = txt.indexOf(keyword);
        if (start == -1) {
            meteoStationData.direction = "";
            meteoStationData.directionangle = -1.0;
        } else {
            txt = txt.substring(start + keyword.length());
            end = txt.indexOf("</span>");
            txt = txt.substring(0, end);
            txt = txt.replaceAll(" ", "");
            txt = txt.replaceAll("-", "");
            txt = txt.trim();
            txt = txt.toUpperCase();
            txt = txt.replaceAll("NORTH", "N");
            txt = txt.replaceAll("SOUTH", "S");
            txt = txt.replaceAll("EAST", "E");
            txt = txt.replaceAll("WEST", "O");
            meteoStationData.direction = txt;
            meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);
        }

        meteoStationData.temperature = null;

        /*txt = htmlResultString;
        keyword = "spotmeta__notification";
        start = txt.indexOf(keyword);
        txt = txt.substring(start + keyword.length());
        end = txt.indexOf("ora locale.");
        txt = txt.substring(0, end);
        meteoStationData.temperature = Double.valueOf(txt);*/

        /*meteoStationData.spotName = name;
        meteoStationData.spotID = id;*/

        //String date = meteoStationData.sampledatetime.toString().substring(0,10);
        //String time = meteoStationData.sampledatetime.toString().substring(11,16);

        meteoStationData.datetime = meteoStationData.sampledatetime;


        return meteoStationData;
    }
}
