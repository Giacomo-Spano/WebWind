package windalarm.meteodata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
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
    public String source = null;
    public String sourceId;
    public String sourceSpotName;
    public Date lastUpdate;
    public Double lon;
    public Double lat;
    public List<Date> datetimes;
    public List<Double> speeds;
    public List<Double> maxSpeeds;
    public List<Double> speedDirs;
    public List<Double> temperatures;

    public List<Double> maxtemperatures;
    public List<Double> mintemperatures;
    public List<Integer> humidities;
    public List<String> weathers;
    public List<String> weatherdescriptions;
    public List<String> icons;
    public List<Integer> cloudPercentages;
    public List<Double> pressures;
    public List<Double> rains;
    public List<Double> windchills; // non usato

    public String sourceSpotCountry;
    public long id;



    public WindForecast(long spotId, String sourceId, String source) {
        this.sourceId = sourceId;
        this.spotId = spotId;
        this.source = source;
        init();
    }

    private void init() {
        datetimes = new ArrayList<Date>();
        speeds = new ArrayList<Double>();
        speedDirs = new ArrayList<Double>();
        maxSpeeds = new ArrayList<Double>();
        temperatures = new ArrayList<Double>();
        maxtemperatures = new ArrayList<Double>();
        mintemperatures = new ArrayList<Double>();

        humidities = new ArrayList<Integer>();
        weathers = new ArrayList<String>();
        weatherdescriptions = new ArrayList<String>();
        icons = new ArrayList<String>();
        cloudPercentages = new ArrayList<Integer>();
        pressures = new ArrayList<Double>();
        windchills = new ArrayList<Double>();
        rains = new ArrayList<Double>();
    }

    public WindForecast(long spotId) {
        this.spotId = spotId;
        init();
    }

    public WindForecast() {
        init();
    }

    public String toJson() {

        int count;
        String str = "{ ";

        if (sourceSpotName != null) {
            str += "\"name\" : \"" + sourceSpotName + "\", ";
        }

        if (lastUpdate != null) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            str += "\"lastupdate\" : \"" + df.format(lastUpdate) + "\", ";
        }

        if (lon != null) {
            str += "\"lon\" : \"" + lon + "\", ";
        }

        if (lat != null) {
            str += "\"lat\" : \"" + lat + "\", ";
        }

        if (datetimes != null && datetimes.size() > 0) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            str += "\"datetimes\" : [";
            count = 1;
            for (Date d : datetimes) {
                if (count++ > 1)
                    str += ",";
                str += "\"" + df.format(d) + "\"";
            }
            str += "],";
        }

        if (speeds != null && speeds.size() > 0) {
            str += "\"speeds\" : [";
            count = 1;
            for (Double h : speeds) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "],";
        }

        if (speedDirs != null && speedDirs.size() > 0) {
            str += "\"speedDirs\" : [";
            count = 1;
            for (Double h : speedDirs) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "],";
        }

        if (maxSpeeds != null && maxSpeeds.size() > 0) {
            str += "\"maxSpeeds\" : [";
            count = 1;
            for (Double h : maxSpeeds) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "],";
        }

        if (temperatures != null && temperatures.size() > 0) {
            str += "\"temperatures\" : [";
            count = 1;
            for (Double h : temperatures) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "],";
        }

        if (maxtemperatures != null && maxtemperatures.size() > 0) {
            str += "\"maxtemperatures\" : [";
            count = 1;
            for (Double h : maxtemperatures) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "],";
        }

        if (mintemperatures != null && mintemperatures.size() > 0) {
            str += "\"mintemperatures\" : [";
            count = 1;
            for (Double h : mintemperatures) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "],";
        }

        if (humidities != null && humidities.size() > 0) {
            str += "\"humidities\" : [";
            count = 1;
            for (Integer h : humidities) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "],";
        }

        if (weathers != null && weathers.size() > 0) {
            str += "\"weathers\" : [";
            count = 1;
            for (String h : weathers) {
                if (count++ > 1)
                    str += ",";
                str += "\"" + h + "\"";
            }
            str += "],";
        }

        if (weatherdescriptions != null && weatherdescriptions.size() > 0) {
            str += "\"weatherdescriptions\" : [";
            count = 1;
            for (String h : weatherdescriptions) {
                if (count++ > 1)
                    str += ",";
                str += "\"" + h + "\"";
            }
            str += "],";
        }

        if (icons != null && icons.size() > 0) {
            str += "\"icons\" : [";
            count = 1;
            for (String h : icons) {
                if (count++ > 1)
                    str += ",";
                str += "\"" + h + "\"";
            }
            str += "],";
        }

        if (pressures != null && pressures.size() > 0) {
            str += "\"pressures\" : [";
            count = 1;
            for (Double p : pressures) {
                if (count++ > 1)
                    str += ",";
                str += p;
            }
            str += "],";
        }

        if (windchills != null && windchills.size() > 0) {
            str += "\"windchills\" : [";
            count = 1;
            for (Double w : windchills) {
                if (count++ > 1)
                    str += ",";
                str += w;
            }
            str += "],";
        }

        if (rains != null && rains.size() > 0) {
            str += "\"rains\" : [";
            count = 1;
            for (Double r : rains) {
                if (count++ > 1)
                    str += ",";
                str += r;
            }
            str += "],";
        }

        if (cloudPercentages != null && cloudPercentages.size() > 0) {
            str += "\"cloudPercentages\" : [";
            count = 1;
            for (Integer h : cloudPercentages) {
                if (count++ > 1)
                    str += ",";
                str += h;
            }
            str += "]";
        }

        str += "}";
        return str;
    }


}
