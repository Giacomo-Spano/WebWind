package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.AlarmModel;
import Wind.Core;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class gvlnifollonica extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /*public gvlnifollonica() {
        super(AlarmModel.Spot_Follonica);
        webcamUrl = "http://www.gvlnifollonica.it/gvcam.jpg";
        mImageName = "spot-" + id + ".jpg";
        name = "Follonica";
        sourceUrl = "http://www.gvlnifollonica.it";
    }*/

    public gvlnifollonica() {
        super();
    }

    public MeteoStationData getMeteoData() {

        String htmlResultString = getHTMLPage(meteodataUrl);
        //String htmlResultString = getHTMLPage("http://www.gvlnifollonica.it/meteo/meteo.txt");
        if (htmlResultString == null)
            return null;
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        meteoStationData.sampledatetime = Core.getDate();

        try {
            JSONObject json = new JSONObject(htmlResultString);

            if (json.has("last_measure_time")) {

                String txt = json.getString("last_measure_time");
                String date = txt.substring(1, 11);
                String time = txt.substring(12, 20);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                try {
                    meteoStationData.datetime = formatter.parse(date + " " + time);
                    long difference = Core.getDate().getTime() - meteoStationData.sampledatetime.getTime();
                    if (difference/1000/60 >  60)
                        meteoStationData.offline = true;
                    else
                        meteoStationData.offline = false;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (json.has("wind_gust")) {
                meteoStationData.speed = json.getDouble("wind_gust");
                meteoStationData.speed = (double) (Math.round(meteoStationData.speed * 10)) / 10;
            }

            if (json.has("wind_ave")) {
                meteoStationData.averagespeed = json.getDouble("wind_ave");
                meteoStationData.averagespeed = (double) (Math.round(meteoStationData.averagespeed * 10)) / 10;
            }

            if (json.has("wind_dir_code"))
                meteoStationData.direction = json.getString("wind_dir_code");

            meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

            if (json.has("rain_rate"))
                meteoStationData.rainrate = (double) (Math.round(json.getDouble("rain_rate") * 10)) / 10;

            if (json.has("temp_out"))
                meteoStationData.temperature = (double) (Math.round(json.getDouble("temp_out") * 10)) / 10;

            if (json.has("hum_out"))
                meteoStationData.humidity = (double) (Math.round(json.getDouble("hum_out") * 10)) / 10;

            if (json.has("rel_pressure")) {
                meteoStationData.pressure = (double) (Math.round(json.getDouble("rel_pressure") * 10)) / 10;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        /*meteoStationData.spotName = AlarmModel.getSpotName(AlarmModel.Spot_Follonica);
        meteoStationData.spotID = id;*/
        //meteoStationData.source = "gvlnifollonica";

        return meteoStationData;
    }
}
