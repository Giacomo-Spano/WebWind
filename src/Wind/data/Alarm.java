package Wind.data;

/*import com.google.appengine.repackaged.org.joda.time.LocalTime;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatterBuilder;*/

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Alarm {

    private static final Logger LOGGER = Logger.getLogger(Alarm.class.getName());

    public long spotID;
    public int deviceId; // id di registrazione
    public String personid;
    public Date startDate;
    public Date endDate;
    public double speed;
    public double avspeed;
    public Date startTime;
    public Date endTime;
    public boolean enabled;
    public String direction;
    public long id; //progressivo allarma
    public Boolean mo;
    public Boolean tu;
    public Boolean we;
    public Boolean th;
    public Boolean fr;
    public Boolean sa;
    public Boolean su;

    public Date lastRingDate;
    public Date lastRingTime;
    public int snoozeMinutes;

    public Alarm() {

    }

    public Alarm(JSONObject jObject) throws org.json.JSONException {

        LOGGER.info("startDate=" + jObject.getString("startDate"));
        LOGGER.info("startTime=" + jObject.getString("startTime"));

        if (jObject.has("startTime")) {
            deviceId = jObject.getInt("deviceId");
        }

        if (jObject.has("personid")) {
            personid = jObject.getString("personid");
        }

        speed = jObject.getDouble("speed");
        avspeed = jObject.getDouble("avspeed");

        DateFormat tf = new SimpleDateFormat("HH:mm");
        if (jObject.has("startTime")) {
            try {
                startTime = tf.parse(jObject.getString("startTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (jObject.has("endTime")) {
            try {
                endTime = tf.parse(jObject.getString("endTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        if (jObject.has("startDate")) {
            try {
                startDate = df.parse(jObject.getString("startDate"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (jObject.has("endDate")) {
            try {
                endDate = df.parse(jObject.getString("endDate"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        enabled = jObject.getBoolean("enabled");
        direction = jObject.getString("direction");
        id = jObject.getLong("id");

        mo = jObject.getBoolean("mo");
        tu = jObject.getBoolean("tu");
        we = jObject.getBoolean("we");
        th = jObject.getBoolean("th");
        fr = jObject.getBoolean("fr");
        sa = jObject.getBoolean("sa");
        su = jObject.getBoolean("su");

        spotID = jObject.getInt("spotId");

        if (jObject.has("snoozeminutes"))
            snoozeMinutes = jObject.getInt("snoozeminutes");
        else
            snoozeMinutes = 0;

        if (jObject.has("lastringdate")) {
            try {
                lastRingDate = tf.parse(jObject.getString("lastringdate"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (jObject.has("lastringtime")) {
            try {
                lastRingTime = df.parse(jObject.getString("lastringtime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String toJson() {

        JSONObject obj = new JSONObject();

        try {
            obj.put("deviceId", deviceId);
            obj.put("personid", personid);
            obj.put("speed", speed);
            obj.put("avspeed", avspeed);

            DateFormat tf = new SimpleDateFormat("HH:mm");
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            if (startDate != null)
                obj.put("startDate", df.format(startDate));
            if (endDate != null)
                obj.put("endDate", df.format(endDate));
            if (startTime != null)
                obj.put("startTime", tf.format(startTime));
            if (endTime != null)
                obj.put("endTime", tf.format(endTime));
            if (lastRingDate != null)
                obj.put("lastRingDate", df.format(lastRingDate));
            if (lastRingTime != null)
                obj.put("lastRingTime", tf.format(lastRingTime));

            obj.put("spotid", "" + spotID);
            obj.put("enabled", enabled);
            obj.put("direction", direction);
            obj.put("id", "" + id);
            obj.put("mo", mo);
            obj.put("tu", tu);
            obj.put("we", we);
            obj.put("th", th);
            obj.put("fr", fr);
            obj.put("sa", sa);
            obj.put("su", su);

            obj.put("snoozeminutes", snoozeMinutes);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return obj.toString();

    }
}


