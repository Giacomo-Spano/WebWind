package windalarm.meteodata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Giacomo Spanò on 28/09/2016.
 */
public class WindForecast {

    public long spotId = -1;
    public int windguruspotId;
    public String windguruSpotName;
    public List<Date> datetimes;
    public List<Double> speeds;
    public List<Double> speedDirs;
    public List<Double> temperatures;
    public int utc_offset;
    public Date initDate;

    public WindForecast(long spotId, int windguruid) {
        this.windguruspotId = windguruid;
        this.spotId = spotId;
        datetimes = new ArrayList<Date>();
        speeds = new ArrayList<Double>();
        speedDirs = new ArrayList<Double>();
        temperatures = new ArrayList<Double>();
    }

    public WindForecast(long spotId) {
        this.windguruspotId = -1;
        this.spotId = spotId;
        datetimes = new ArrayList<Date>();
        speeds = new ArrayList<Double>();
        speedDirs = new ArrayList<Double>();
        temperatures = new ArrayList<Double>();
    }

    public void fromJson(JSONObject obj) {

        try {
            if (obj.has("id_spot"))
                windguruspotId = obj.getInt("id_spot");

            if (obj.has("spot"))
                windguruSpotName = obj.getString("spot");

            if (obj.has("utc_offset"))
                utc_offset = obj.getInt("utc_offset");

            if (obj.has("fcst")) {
                JSONObject fcst = obj.getJSONObject("fcst");
                if (fcst.has("3")) {
                    JSONObject json = fcst.getJSONObject("3");

                    if (json.has("initdate")) {
                        String s = json.getString("initdate");
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        initDate = df.parse(s);
                    }

                    if (json.has("hours")) {
                        JSONArray jarray = json.getJSONArray("hours");
                        for(int n = 0; n < jarray.length(); n++) {
                            Integer hour = jarray.getInt(n);
                            Date end = new Date();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(initDate);
                            cal.add(Calendar.HOUR_OF_DAY, hour);
                            datetimes.add(cal.getTime());
                        }
                    }

                    if (json.has("WINDSPD")) {
                        JSONArray jarray = json.getJSONArray("WINDSPD");
                        for(int n = 0; n < jarray.length(); n++)
                        {
                            Double value = jarray.getDouble(n);
                            speeds.add(value);
                        }
                    }

                    if (json.has("WINDDIR")) {
                        JSONArray jarray = json.getJSONArray("WINDDIR");
                        for(int n = 0; n < jarray.length(); n++)
                        {
                            Double value = jarray.getDouble(n);
                            speedDirs.add(value);
                        }
                    }

                    if (json.has("TMP")) {
                        JSONArray jarray = json.getJSONArray("TMP");
                        for(int n = 0; n < jarray.length(); n++)
                        {
                            Double value = jarray.getDouble(n);
                            temperatures.add(value);
                        }
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
