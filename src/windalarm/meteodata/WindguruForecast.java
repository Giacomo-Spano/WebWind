package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;


public class WindguruForecast extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private String source = FORECAST_WINDGURU;
    private String sourceId = null;
    private long spotId = -1;

    public WindguruForecast(int spotid, String sourceId) {
        this.sourceId = sourceId;
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

        String htmlResultString = getHTMLPage("http://www.windguru.cz/it/index.php?sc=" + sourceId/*meteodataUrl*/);
        if (htmlResultString == null)
            return null;

        String str = findBetweenKeywords(htmlResultString, "var wg_fcst_tab_data_1 = ", ";");

        JSONObject jobj = null;
        try {
            jobj = new JSONObject(str);
            WindForecast f = fromJson(jobj);
            f.source = source;
            f.sourceId =sourceId;
            f.spotId = spotId;
            return f;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public WindForecast fromJson(JSONObject obj) {

        WindForecast wf = new WindForecast();
        try {
            if (obj.has("id_spot"))
                sourceId = "" + obj.getInt("id_spot");
            else
                return null;

            if (obj.has("spot"))
                wf.sourceSpotName = obj.getString("spot");

            if (obj.has("utc_offset")) {
                int utc_offset = obj.getInt("utc_offset");


                if (obj.has("fcst")) {
                    JSONObject fcst = obj.getJSONObject("fcst");
                    if (fcst.has("3")) {
                        JSONObject json = fcst.getJSONObject("3");

                        Date initDate;
                        if (json.has("initdate")) {
                            String s = json.getString("initdate");
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            initDate = df.parse(s);


                            if (json.has("hours")) {
                                JSONArray jarray = json.getJSONArray("hours");
                                for (int n = 0; n < jarray.length(); n++) {
                                    Integer hour = jarray.getInt(n);
                                    Date end = new Date();
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(initDate);
                                    cal.add(Calendar.HOUR_OF_DAY, hour);
                                    wf.datetimes.add(cal.getTime());
                                }
                            }

                            if (json.has("WINDSPD")) {
                                JSONArray jarray = json.getJSONArray("WINDSPD");
                                for (int n = 0; n < jarray.length(); n++) {
                                    Double value = jarray.getDouble(n);
                                    wf.speeds.add(value);
                                }
                            }

                            if (json.has("WINDDIR")) {
                                JSONArray jarray = json.getJSONArray("WINDDIR");
                                for (int n = 0; n < jarray.length(); n++) {
                                    Double value = jarray.getDouble(n);
                                    wf.speedDirs.add(value);
                                }
                            }

                            if (json.has("TMP")) {
                                JSONArray jarray = json.getJSONArray("TMP");
                                for (int n = 0; n < jarray.length(); n++) {
                                    Double value = jarray.getDouble(n);
                                    wf.temperatures.add(value);
                                }
                            }
                        } else {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return wf;
    }
}
