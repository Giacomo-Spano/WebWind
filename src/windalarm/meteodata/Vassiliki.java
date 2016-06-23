package windalarm.meteodata;


import Wind.AlarmModel;
import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class Vassiliki extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Vassiliki() {
        super(AlarmModel.Spot_Vassiliki);
        //mImageName = AlarmModel.getSpotName(AlarmModel.Spot_Vassiliki) + ".jpg";
        mImageName = "spot-" + mSpotID + ".jpg";
        mName = "Vassiliki (Grecia)";
    }


    public MeteoStationData getMeteoData() {

        String htmlResultString = getHTMLPage("http://www.vasiliki-webcams-and-more.org/cam1/sequenz.php");
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        meteoStationData.sampledatetime = Core.getDate();
        LOGGER.info("time in rome=" + meteoStationData.sampledatetime);
        LOGGER.info("hour in rome=" + cal.get(Calendar.HOUR_OF_DAY));

        String txt = htmlResultString;
        //wetterdaten.push({timestamp: '2015-08-09 08:12:53',aussentemperatur: 31,windgeschwindigkeit: 7.1610810810811,windrichtung: 225});

        String keyword = "windgeschwindigkeit:";
        int start = txt.indexOf(keyword);
        if (start == -1)
            LOGGER.severe(txt + " not found " + keyword);
        txt = txt.substring(start + keyword.length());
        int end = txt.indexOf(",");
        txt = txt.substring(0, end);
        keyword = ",";
        meteoStationData.speed = Double.valueOf(txt.trim());
        meteoStationData.speed = MeteoStationData.knotsToKMh(meteoStationData.speed);

        meteoStationData.averagespeed = Core.getAverage(mSpotID);

        txt = htmlResultString;
        //<div id="gaugeWinddirValue">180�</div>
        keyword = "aussentemperatur:";
        start = txt.indexOf(keyword);
        txt = txt.substring(start + keyword.length());
        end = txt.indexOf(",");
        txt = txt.substring(0, end);
        meteoStationData.temperature = Double.valueOf(txt.trim());

        txt = htmlResultString;
        //<div id="gaugeWinddirValue">180�</div>
        keyword = "windrichtung:";
        start = txt.indexOf(keyword);
        txt = txt.substring(start + keyword.length());
        end = txt.indexOf("});");
        txt = txt.substring(0, end);
        //meteoStationData.temperature = Double.valueOf(txt.trim());
        meteoStationData.direction = getDirection(Double.valueOf(txt.trim()));
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        meteoStationData.pressure = 0.0;
        meteoStationData.humidity = 0.0;
        meteoStationData.rainrate = 0.0;

        txt = htmlResultString;
        //<a href="#" data-alt="0">10:42</a>
        keyword = "wetterdaten.push({timestamp: '";
        start = txt.indexOf(keyword);
        txt = txt.substring(start + keyword.length());
        end = txt.indexOf("',");
        txt = txt.substring(0, end);


        txt = htmlResultString;
        // <img name="bild" id="webcambild" src="ba20150815078000.jpg"
        keyword = "<img name=\"bild\" id=\"webcambild\" src=\"";
        start = txt.indexOf(keyword);
        txt = txt.substring(start + keyword.length());
        end = txt.indexOf("\"");
        txt = txt.substring(0, end);
        mWebcamUrl = "http://www.vasiliki-webcams-and-more.org/cam1/" + txt;


        String date = txt.substring(8, 10) + "/" + txt.substring(5, 7) + "/" + txt.substring(0, 4);
        String time = txt.substring(11, 16);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            meteoStationData.datetime = formatter.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*meteoStationData.spotName = mName;
        meteoStationData.spotID = mSpotID;*/

        return meteoStationData;
    }

    String direction[] = {"N", "NNW", "NW", "NWW", "W", "WSW", "SW", "SWS", "S", "SSE", "SE", "SEE", "E", "ENE", "NE", "NEN", "N"};

    private String getDirection(double deg) {

        return direction[(int) (deg / 22.5)];
    }
}
