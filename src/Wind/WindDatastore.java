/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package Wind;


import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


/**
 * Simple implementation of a data store using standard Java collections.
 * <p/>
 * This class is thread-safe but not persistent (it will lost the data when the
 * app is restarted) - it is meant just as an example.
 */
public class WindDatastore {

    private static String alarmName = "AlarmName";
    private static String notificationName = "Notifications";
    private static String deviceName = "WindDevice";
    private static final List<Alarm> programIds = new ArrayList<Alarm>();
    private static final Logger logger = Logger.getLogger(WindDatastore.class.getName());

    public WindDatastore() {

    }


    public static void updateAlarmLastRingDate(String regId, long alarmId, Date date) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            String strLastRingDate = "'" + df.format(date) + "'";
            DateFormat tf = new SimpleDateFormat("HH:mm:ss");
            String strLastRingTime = "'" + tf.format(date) + "'";

            String sql = "UPDATE alarms SET "
                    + "lastringdate=" + strLastRingDate + ","
                    + "lastringtime=" + strLastRingTime + ","
                    + "snoozeminutes=0 "
                    + " WHERE "
                    + "id='" + alarmId + "' AND "
                    + "regid='" + regId + "';";

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.close();

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            ;
            logger.severe(se.toString());
            return;

        } catch (Exception e) {
            //Handle errors for Class.forName
            logger.severe(e.toString());
            return;
        }
    }

    public static void updateAlarmSnoozeMinutes(String regId, long alarmId, int snoozeMminutes) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql = "UPDATE alarms SET snoozeminutes=" + snoozeMminutes + ";";

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            ;
            logger.severe(se.toString());
            return;

        } catch (Exception e) {
            //Handle errors for Class.forName
            logger.severe(e.toString());
            return;
        }
    }

    public long saveAlarm(String regId, Alarm alarm) {

        long lastid;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            String strStartDate = "'" + df.format(alarm.startDate) + "'";
            String strEndDate = "'" + df.format(alarm.endDate) + "'";
            String strLastRingDate;
            if (alarm.lastRingDate != null)
                strLastRingDate = "'" + df.format(alarm.lastRingDate) + "'";
            else
                strLastRingDate = "null";
            DateFormat tf = new SimpleDateFormat("HH:mm:ss");
            String strStartTime = "'" + tf.format(alarm.startTime) + "'";
            String strEndTime = "'" + tf.format(alarm.endTime) + "'";
            String strLastRingTime;
            if (alarm.lastRingTime != null)
                strLastRingTime = "'" + tf.format(alarm.lastRingTime) + "'";
            else
                strLastRingTime = "null";


            String sql = "INSERT INTO alarms (id, regid, startdate,starttime,enddate,endtime,lastringdate,lastringtime,snoozeminutes,spotid,speed,avspeed,enabled,direction,mo,tu,we,th,fr,sa,su )" +
                    " VALUES ("
                    + alarm.id + ","
                    + "'" + regId + "'" + ","
                    + strStartDate + ","
                    + strStartTime + ","
                    + strEndDate + ","
                    + strEndTime + ","
                    + strLastRingDate + ","
                    + strLastRingTime + ","
                    + alarm.snoozeMinutes + ","
                    + alarm.spotID + ","
                    + alarm.speed + ","
                    + alarm.avspeed + ","
                    + alarm.enabled + ","
                    + "'" + alarm.direction + "'" + ","
                    + alarm.mo + ","
                    + alarm.tu + ","
                    + alarm.we + ","
                    + alarm.th + ","
                    + alarm.fr + ","
                    + alarm.sa + ","
                    + alarm.su
                    + ") "
                    + "ON DUPLICATE KEY UPDATE "
                    + "id=" + alarm.id + ","
                    + "regid=" + "'" + regId + "'" + ","
                    + "startdate=" + strStartDate + ","
                    + "starttime=" + strStartTime + ","
                    + "enddate=" + strEndDate + ","
                    + "endtime=" + strEndTime + ","
                    + "lastringdate=" + strLastRingDate + ","
                    + "lastringtime=" + strLastRingTime + ","
                    + "snoozeminutes=" + alarm.snoozeMinutes + ","
                    + "spotid=" + alarm.spotID + ","
                    + "speed=" + alarm.speed + ","
                    + "avspeed=" + alarm.avspeed + ","
                    + "enabled=" + alarm.enabled + ","
                    + "direction=" + "'" + alarm.direction + "'" + ","
                    + "mo=" + alarm.mo + ","
                    + "tu=" + alarm.tu + ","
                    + "we=" + alarm.we + ","
                    + "th=" + alarm.th + ","
                    + "fr=" + alarm.fr + ","
                    + "sa=" + alarm.sa + ","
                    + "su=" + alarm.su;

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = alarm.id;
            }

            stmt.close();

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return 0;
        }

        //read(); // reload data
        return lastid;
    }

    public static List<Alarm> getAlarms() {

        /*Key alarmKey = KeyFactory.createKey("WindAlarm", "alarmName");
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();

        Query query = new Query("Alarm", alarmKey);
        List<Entity> alarms = datastore.prepare(query).asList(
                FetchOptions.Builder.withLimit(5));

        programIds.removeAll(programIds);
        // regIds.

        for (int i = 0; i < alarms.size(); i++) {

            Alarm alarm = new Alarm();
            alarm.regId = (String) alarms.get(i).getProperty("regId");
            alarm.startTime = (LocalTime) alarms.get(i).getProperty("startTime");
            alarm.endTime = (LocalTime) alarms.get(i).getProperty("endTime");
            alarm.startDate = (Date) alarms.get(i).getProperty("startDate");
            alarm.endDate = (Date) alarms.get(i).getProperty("endDate");
            alarm.speed = (Double) alarms.get(i).getProperty("speed");
            alarm.avspeed = (Double) alarms.get(i).getProperty("avspeed");
            alarm.enabled = (Boolean) alarms.get(i).getProperty("enabled");
            alarm.direction = (String) alarms.get(i).getProperty("direction");
            alarm.id = (Long) alarms.get(i).getProperty("id");

            alarm.mo = (Boolean) alarms.get(i).getProperty("mo");
            alarm.tu = (Boolean) alarms.get(i).getProperty("tu");
            alarm.we = (Boolean) alarms.get(i).getProperty("we");
            alarm.th = (Boolean) alarms.get(i).getProperty("th");
            alarm.fr = (Boolean) alarms.get(i).getProperty("fr");
            alarm.sa = (Boolean) alarms.get(i).getProperty("sa");
            alarm.su = (Boolean) alarms.get(i).getProperty("su");

            programIds.add(alarm);
        }
*/
        return new ArrayList<Alarm>(programIds);
    }

    public static Alarm getAlarm(String regId, String Id) {

        /*logger.info("regId=" + regId + ";Id=" + Id);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter regIdFilter = new Query.FilterPredicate("regId", Query.FilterOperator.EQUAL, regId);
        //Query.Filter IdFilter = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, Id);
        //Query.Filter filter = Query.CompositeFilterOperator.and(regIdFilter, IdFilter);

        Query query = new Query(alarmName)
                .setFilter(regIdFilter);
        List<Entity> alarms = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

        if (alarms.size() > 0) {
            //Entity entityAlarm = alarms.get(0);
            Alarm alarm = getWindAlarmProgram(alarms.get(0));

            return alarm;
        } else {
            return null;
        }*/
        return null;
    }

    public static long deleteAlarm(String regId, int id) {

        int deletedItems = 0;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "DELETE FROM alarms WHERE id=" + id + " AND regid='" + regId + "'";
            Statement stmt = conn.createStatement();
            deletedItems = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.close();
            conn.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        return deletedItems;
    }

    public static List<Alarm> getAlarmsFromRegId(String regId) {

        logger.info("regId=" + regId);
        List<Alarm> registeredAlarms = new ArrayList<Alarm>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM alarms WHERE regid='" + regId + "'";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                Alarm alarm = new Alarm();

                alarm.id = rs.getInt("id");
                alarm.startDate = rs.getDate("startdate");
                alarm.endDate = rs.getDate("enddate");
                alarm.startTime = rs.getTime("starttime");
                alarm.endTime = rs.getTime("endtime");
                alarm.spotID = rs.getInt("spotid");
                alarm.speed = rs.getDouble("speed");
                alarm.avspeed = rs.getDouble("avspeed");
                alarm.enabled = rs.getBoolean("enabled");
                alarm.direction = rs.getString("direction");
                alarm.mo = rs.getBoolean("mo");
                alarm.tu = rs.getBoolean("tu");
                alarm.we = rs.getBoolean("we");
                alarm.th = rs.getBoolean("th");
                alarm.fr = rs.getBoolean("fr");
                alarm.sa = rs.getBoolean("sa");
                alarm.su = rs.getBoolean("su");
                alarm.lastRingTime = rs.getDate("lastringtime");
                alarm.lastRingDate = rs.getDate("lastringdate");

                registeredAlarms.add(alarm);
            }
            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return registeredAlarms;
    }


    private static Alarm getWindAlarmProgram(ResultSet rs/*Entity entityAlarm*/) {

        Alarm alarm = new Alarm();

        try {
            alarm.id = rs.getInt("id");
            alarm.regId = rs.getString("regId");

            alarm.startDate = rs.getDate("startdate");
            alarm.endDate = rs.getDate("enddate");
            alarm.startTime = rs.getTime("starttime");
            alarm.endTime = rs.getTime("endtime");
            alarm.spotID = rs.getInt("spotid");
            alarm.speed = rs.getDouble("speed");
            alarm.avspeed = rs.getDouble("avspeed");
            alarm.enabled = rs.getBoolean("enabled");
            alarm.direction = rs.getString("direction");
            alarm.mo = rs.getBoolean("mo");
            alarm.tu = rs.getBoolean("tu");
            alarm.we = rs.getBoolean("we");
            alarm.th = rs.getBoolean("th");
            alarm.fr = rs.getBoolean("fr");
            alarm.sa = rs.getBoolean("sa");
            alarm.su = rs.getBoolean("su");
            alarm.lastRingTime = rs.getTime("lastringtime");
            alarm.lastRingDate = rs.getDate("lastringdate");
            alarm.snoozeMinutes = rs.getInt("snoozeminutes");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alarm;
    }

    public static List<Alarm> getActiveAlarm(Double speed, Double avspeed, Date currentTime, Date currentDate, long spotId) {

        logger.info("GETACTIVEALARM: speed=" + speed+",avspeed=" + avspeed+",currentTime=" + currentTime+",currentDate=" + currentDate+",spotId=" + spotId);

        List<Alarm> registeredAlarms = new ArrayList<Alarm>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");

            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM alarms WHERE spotid=" + spotId + " AND " + "speed<=" + speed + " AND " + "enabled=true " +
                    "AND startdate <= '" + sdf.format(currentDate) + "' " +
                    "AND enddate >= '" + sdf.format(currentDate) + "' " +
                    "AND starttime <= '" + stf.format(currentDate) + "' " +
                    "AND endtime >= '" + stf.format(currentDate) + "' ";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                Alarm alarm = getWindAlarmProgram(rs);

                Date today = Core.getDate();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

                if (alarm.lastRingDate != null && alarm.lastRingTime != null) {

                    if (fmt.format(today).equals(fmt.format(alarm.lastRingDate))) {

                        if (alarm.snoozeMinutes == 0)   // non suonare se ha già
                            // suonato oggi ed è stato stoppato (snooze == 0)
                            continue;

                        Calendar cal = Calendar.getInstance(); // creates calendar
                        cal.setTime(alarm.lastRingTime); // sets calendar time/date
                        cal.add(Calendar.MINUTE, alarm.snoozeMinutes); // adds one hour
                        Date snoozeTime = cal.getTime(); // returns new date object, one hour in the future

                        if (timeIsBefore(today, snoozeTime)) // non suonare se non è finito lo snooze time
                            continue;
                    }
                }

                logger.info("ALARM ACTIVE: alarm.regId=" + alarm.regId+",alarm.speed=" + alarm.speed+",alarm.avspeed=" + alarm.avspeed+",alarm.direction=" + alarm.direction
                    +",alarm.id=" + alarm.id+",alarm.startTime=" + alarm.startTime.toString()+",alarm.endTime=" + alarm.endTime.toString()+",alarm.startDate=" + alarm.startDate.toString()
                    +",alarm.endDate=" + alarm.endDate.toString()+",alarm.spotId=" + alarm.spotID);

                registeredAlarms.add(alarm);
            }
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        logger.info("FOUND " + registeredAlarms.size()+ " active alarms");

        return registeredAlarms;
    }

    public static boolean timeIsBefore(Date d1, Date d2) {
        DateFormat f = new SimpleDateFormat("HH:mm:ss.SSS");
        return f.format(d1).compareTo(f.format(d2)) < 0;
    }

    /*public static void saveNotification(String regId, NotificationSettings notificationSettings) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key deviceKey = KeyFactory.createKey(notificationName, "regId" + regId);
        Entity device;

        try {
            device = datastore.get(deviceKey);
        } catch (EntityNotFoundException e) {
            device = new Entity(deviceKey);
        }

        device.setProperty("regId", regId);
        device.setProperty("windIncrease", notificationSettings.windIncrease);
        device.setProperty("windChangeDirection", notificationSettings.windChangeDirection);

        logger.info("device=" + device);

        datastore.put(device);
    }*/

    /*public static List<NotificationSettings> getNotificationSettingsFromRegId(String regId) {

        logger.info("regId=" + regId);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter regIdFilter = new Query.FilterPredicate("regId", Query.FilterOperator.EQUAL, regId);
        Query query = new Query(notificationName)
                .setFilter(regIdFilter);

        PreparedQuery pq = datastore.prepare(query);
        List<NotificationSettings> registeredAlarms = new ArrayList<NotificationSettings>();
        for (Entity result : pq.asIterable()) {

            NotificationSettings alarm = getNotificationSettings(result);
            registeredAlarms.add(alarm);
        }
        return registeredAlarms;
    }*/

    /*private static NotificationSettings getNotificationSettings(Entity entityAlarm) {
        NotificationSettings notificationSettings = new NotificationSettings();


        //notificationSettings.regId = (String) entityAlarm.getProperty("regId");
        notificationSettings.windIncrease = (Boolean) entityAlarm.getProperty("windIncrease");
        notificationSettings.windChangeDirection = (Boolean) entityAlarm.getProperty("windChangeDirection");

        return notificationSettings;
    }*/

    /*public static List<Alarm> getActiveNotifications(Double speed, Double avspeed, LocalTime currentTime, Date currentDate) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter speedFilter = new Query.FilterPredicate("speed", Query.FilterOperator.LESS_THAN_OR_EQUAL, speed);

        Query query = new Query(alarmName)
                .setFilter(speedFilter);

        // Use PreparedQuery interface to retrieve results
        PreparedQuery pq = datastore.prepare(query);

        List<Alarm> registeredAlarms = new ArrayList<Alarm>();
        for (Entity result : pq.asIterable()) {

            Alarm alarm = getWindAlarmProgram(result);

            logger.info("alarm.regId=" + alarm.regId);
            logger.info("alarm.speed=" + alarm.speed);
            logger.info("alarm.avspeed=" + alarm.avspeed);
            logger.info("alarm.direction=" + alarm.direction);
            logger.info("alarm.id=" + alarm.id);
            logger.info("alarm.startTime=" + alarm.startTime.toString());
            logger.info("alarm.endTime=" + alarm.endTime.toString());
            logger.info("alarm.startDate=" + alarm.startDate.toString());
            logger.info("alarm.endDate=" + alarm.endDate.toString());
            logger.info("alarm.lastringtime=" + alarm.lastRingDate.toString());

            if (alarm.avspeed >= avspeed) {
                logger.info("speed to low");
                continue;
            }
            if (alarm.startTime.compareTo(currentTime) > 0) {
                logger.info("startTime too late");
                continue;
            }
            if (alarm.endTime.compareTo(currentTime) < 0) {
                logger.info("endTime too early");
                continue;
            }
            if (alarm.startDate.compareTo(currentDate) > 0) {
                logger.info("startDate too late");
                continue;
            }
            if (alarm.endDate.compareTo(currentDate) < 0) {
                logger.info("endDate to early");
                continue;
            }
            logger.info("currentDate=" + currentDate.toString());
            logger.info("alarm.lastRingDate=" + alarm.lastRingDate.toString());

            if (alarm.lastRingDate.equals(currentDate)) {
                logger.info("già suonato oggi");
                continue;
            }
            registeredAlarms.add(alarm);
        }

        //List<Entity> alarms = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

        logger.info("alarms size=" + registeredAlarms.size());


        logger.info("registeredAlarms=" + registeredAlarms);
        return registeredAlarms;
    }*/

}
