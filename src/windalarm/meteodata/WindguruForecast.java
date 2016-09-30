package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.Core;
import Wind.data.WindForecastDataSource;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;


public class WindguruForecast extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private int windguruid = -1;
    private long spotId = -1;

    public WindguruForecast(int spotid, int windguruid) {
        this.windguruid = windguruid;
        this.spotId = spotid;
    }

    @Override
    MeteoStationData getMeteoData() {
        return null;
    }

    public WindguruForecast() {
        super();
    }

    public WindForecast getForecastData() {

        LOGGER.info("getMeteoData: spotName=" + name);



        //String htmlResultString = getHTMLPage("http://www.windguru.cz/it/index.php?sc=49162"/*meteodataUrl*/);
        String htmlResultString = getHTMLPage("http://www.windguru.cz/it/index.php?sc=" + windguruid/*meteodataUrl*/);
        if (htmlResultString == null)
            return null;


        // sample date time
        //forecastData.sampledatetime = Core.getDate();


        String str = findBetweenKeywords(htmlResultString, "var wg_fcst_tab_data_1 = ", ";");

        JSONObject jobj = null;
        try {
            jobj = new JSONObject(str);
            WindForecast forecastData = new WindForecast(spotId,windguruid);
            forecastData.fromJson(jobj);

            WindForecastDataSource f = new WindForecastDataSource();
            f.insert(forecastData);

            return forecastData;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;

    }



    private Date getDate(String date, String time) {



        String dayofweek = date.substring(0,3);
        int idx = date.indexOf(",")+1;
        String month = date.substring(idx,idx+4).trim();
        String day = date.substring(idx+5).trim();
        Date yd = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(yd);
        int year = cal.get(Calendar.YEAR);
        //int month = cal.get(Calendar.MONTH);
        //int day = cal.get(Calendar.DAY_OF_MONTH);

        String fulldate = day + "-" + month + "-" + year + " " + time;

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm", Locale.ENGLISH);

        try {
            Date d = df.parse(fulldate);
            return d;
        } catch (ParseException e) {
            LOGGER.info("unparsable data: spotName=" + name);
            e.printStackTrace();
        }
        return null;
    }

    private String getDirection (String direction ) {
        String txt = direction;
        if (txt == null) {
            return "";
        } else {
            txt = txt.replaceAll(" ", "");
            txt = txt.replaceAll("-", "");
            txt = txt.trim();
            txt = txt.toUpperCase();
            txt = txt.replaceAll("NORTH", "N");
            txt = txt.replaceAll("SOUTH", "S");
            txt = txt.replaceAll("EAST", "E");
            txt = txt.replaceAll("WEST", "O");
            return txt;
        }
    }
}
