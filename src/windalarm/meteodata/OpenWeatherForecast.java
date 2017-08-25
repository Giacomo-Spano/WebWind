package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.Core;
import Wind.data.WindForecastDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;


public class OpenWeatherForecast extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private String openweathermapid = null;
    private long spotId = -1;

    public OpenWeatherForecast(long spotid, String openweathermapid) {
        this.openweathermapid = openweathermapid;
        this.spotId = spotid;
    }

    public OpenWeatherForecast() {
        super();
    }

    @Override
    MeteoStationData getMeteoData() {
        return null;
    }

    public WindForecast getMeteoForecastData() {

        LOGGER.info("getMeteoData: spotName=" + name);

        String APIKEY = "98d03ba3e2cdbfec512ed401f221b528";
        String url = "http://api.openweathermap.org/data/2.5/forecast?units=metric&mode=json";
        url+= "&lang=it";
        url += "&id=" + openweathermapid;
        url += "&appid="+APIKEY;

        return getWindForecast(url);

    }

    public WindForecast getMeteoForecastByCityName(String city, String country) {

        LOGGER.info("getMeteoData: spotName=" + name);

        String APIKEY = "98d03ba3e2cdbfec512ed401f221b528";
        String url = "http://api.openweathermap.org/data/2.5/forecast?units=metric&mode=json";
        url+= "&lang=it";
        url += "&q=" + city;
        if (country != null)
            url += "," + country;
        url += "&appid="+APIKEY;

        //api.openweathermap.org/data/2.5/weather?q=London,uk

        return getWindForecast(url);

    }

    public WindForecast getMeteoForecastByCityId(String cityId, String country) {

        LOGGER.info("getMeteoData: spotName=" + name);

        String APIKEY = "98d03ba3e2cdbfec512ed401f221b528";
        String url = "http://api.openweathermap.org/data/2.5/forecast?units=metric&mode=json";
        url+= "&lang=it";
        url += "&id=" + cityId;
        if (country != null)
            url += "," + country;
        url += "&appid="+APIKEY;

        //api.openweathermap.org/data/2.5/weather?q=London,uk

        return getWindForecast(url);

    }

    private WindForecast getWindForecast(String url) {
        String htmlResultString = getHTMLPage(url);
        if (htmlResultString == null)
            return null;

        JSONObject jobj = null;
        try {
            jobj = new JSONObject(htmlResultString);
            WindForecast f = fromJson(jobj);
            f.sourceId = openweathermapid;
            f.spotId = spotId;
            f.source = FORECAST_OPENWEATHERMAP;

            //WindForecastDataSource f = new WindForecastDataSource();
            //f.insert(forecastData);

            return f;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }

    public WindForecast fromJson(JSONObject obj) {

        WindForecast wf = new WindForecast();
        try {
            if (obj.has("city")) {
                JSONObject city = obj.getJSONObject("city");
                wf.sourceSpotName = city.getString("name");
                wf.sourceSpotCountry = city.getString("country");
                JSONObject jcoord = city.getJSONObject("coord");
                wf.lon = jcoord.getDouble("lon");
                wf.lat = jcoord.getDouble("lat");
                wf.lastUpdate = Core.getDate();

            } else
                return null;

            if (obj.has("list")) {
                JSONArray jarray = obj.getJSONArray("list");
                for (int n = 0; n < jarray.length(); n++) {
                    JSONObject j = jarray.getJSONObject(n);

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dt_txt = j.getString("dt_txt");
                    Date date = df.parse(dt_txt);
                    wf.datetimes.add(date);

                    JSONObject jmain = j.getJSONObject("main");
                    Double temperature = jmain.getDouble("temp");
                    wf.temperatures.add(temperature);
                    Double maxtemp = jmain.getDouble("temp_max");
                    wf.maxtemperatures.add(maxtemp);
                    Double mintemp = jmain.getDouble("temp_min");
                    wf.mintemperatures.add(mintemp);
                    int humidity = jmain.getInt("humidity");
                    wf.humidities.add(humidity);
                    Double pressure = jmain.getDouble("pressure");
                    wf.pressures.add(pressure);

                    JSONArray jweatherlist = j.getJSONArray("weather"); // perchè è un jarray e non un jobc??
                    JSONObject jweather = jweatherlist.getJSONObject(0);
                    String weather = jweather.getString("main");
                    wf.weathers.add(weather);
                    String weatherdescription = jweather.getString("description");
                    wf.weatherdescriptions.add(weatherdescription);
                    String icon = jweather.getString("icon");
                    wf.icons.add(icon);

                    JSONObject jcloud = j.getJSONObject("clouds");
                    int cloudPercentage = jcloud.getInt("all");
                    wf.cloudPercentages.add(cloudPercentage);

                    JSONObject jwind = j.getJSONObject("wind");
                    Double speed = jwind.getDouble("speed");
                    wf.speeds.add(speed);
                    Double deg = jwind.getDouble("deg");
                    wf.speedDirs.add(deg);


                    Double rain = 0.0;
                    if (j.has("rain")) {
                        JSONObject jrain = j.getJSONObject("rain");
                        if (jrain.has("3h"))
                            rain = jrain.getDouble("3h");
                    }
                    wf.rains.add(rain);

                }
            } else
                return null;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return wf;
    }
}
