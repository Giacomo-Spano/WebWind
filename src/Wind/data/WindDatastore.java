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
package Wind.data;


import Wind.AlarmModel;
import Wind.Core;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static void updateAlarmLastRingDate(int deviceId, long alarmId, Date date) {

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
                    + "deviceid=" + deviceId + ";";

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

    public static void updateAlarmSnoozeMinutes(long alarmId, int snoozeMminutes) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql = "UPDATE alarms SET snoozeminutes=" + snoozeMminutes + " WHERE id=" + alarmId + ";";

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

    public long saveAlarm(Alarm alarm) {

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

            String sql = "INSERT INTO alarms (id, deviceId, startdate,starttime,enddate,endtime,lastringdate,lastringtime,snoozeminutes,spotid,speed,avspeed,enabled,direction,mo,tu,we,th,fr,sa,su )" +
                    " VALUES ("
                    + alarm.id + ","
                    + alarm.deviceId + ","
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
                    + "deviceid=" + alarm.deviceId + ","
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


    public static long deleteAlarm(int id) {

        int deletedItems = 0;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "DELETE FROM alarms WHERE id=" + id + ";";
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

    public static List<Alarm> getAlarmsFromDeviceID(long deviceId,long spotId) {

        logger.info("deviceId=" + deviceId);
        List<Alarm> registeredAlarms = new ArrayList<Alarm>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM alarms WHERE deviceid=" + deviceId;
            if (spotId != -1) {
                sql += " AND spotid=" + spotId;
            }
            sql += ";";
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


    private static Alarm getAlarmFromResultSet(ResultSet rs/*Entity entityAlarm*/) {

        Alarm alarm = new Alarm();

        try {
            alarm.id = rs.getInt("id");
            alarm.deviceId = rs.getInt("deviceId");
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

    public static List<Alarm> getAlarms() {

        List<Alarm> alarms = new ArrayList<Alarm>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM alarms";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Alarm alarm = getAlarmFromResultSet(rs);
                alarms.add(alarm);
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
        return alarms;
    }

    public static List<Alarm> sendActiveAlarm(Double speed, Double avspeed, Date currentDate, long spotId, int windid) {

        logger.info("GETACTIVEALARM: speed=" + speed+",avspeed=" + avspeed+",currentDate=" + currentDate+",spotId=" + spotId);

        List<Alarm> registeredAlarms = new ArrayList<Alarm>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");

            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM alarms WHERE spotid=" + spotId +
                    " AND " + "speed<=" + speed +
                    " AND " + "avspeed<=" + avspeed +
                    " AND " + "enabled=true " +
                    " AND startdate <= '" + sdf.format(currentDate) + "'" +
                    " AND enddate >= '" + sdf.format(currentDate) + "'" +
                    " AND starttime <= '" + stf.format(currentDate) + "'" +
                    " AND endtime >= '" + stf.format(currentDate) + "'";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Alarm alarm = getAlarmFromResultSet(rs);

                Date today = Core.getDate();
                SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyy");
                SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
                SimpleDateFormat datetimeFmt = new SimpleDateFormat("dd/MM/yyy HH:mm");
                if (alarm.lastRingDate != null && alarm.lastRingTime != null) {

                    String strLastRingDateTime = dateFmt.format(alarm.lastRingDate) + " " + timeFmt.format(alarm.lastRingTime);
                    Date lastRingDate = datetimeFmt.parse(strLastRingDateTime);

                    Long minutesTimeDifference = (today.getTime() - lastRingDate.getTime()) / 1000 / 60;
                    if (minutesTimeDifference < 30) { // allarme già suonato da meno di 30 minuti

                        if (alarm.snoozeMinutes == 0) continue; // se non è impostato snoozetime non suonare

                        if (minutesTimeDifference < alarm.snoozeMinutes) {
                            continue;   // non suonare se non è ancora finito lo snooze time
                        }
                    }


                }
                AlarmModel.sendAlarm(alarm.deviceId,alarm,speed,avspeed,currentDate,spotId,windid);

                logger.info("ALARM ACTIVE: alarm.deviceId=" + alarm.deviceId +",alarm.speed=" + alarm.speed+",alarm.avspeed=" + alarm.avspeed+",alarm.direction=" + alarm.direction
                    +",alarm.id=" + alarm.id+",alarm.startTime=" + alarm.startTime.toString()+",alarm.endTime=" + alarm.endTime.toString()+",alarm.startDate=" + alarm.startDate.toString()
                    +",alarm.endDate=" + alarm.endDate.toString()+",alarm.spotId=" + alarm.spotID);

                //registeredAlarms.add(alarm);
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

    public static void purgeDB() {

        List<Alarm> alarms = new ArrayList<Alarm>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "INSERT INTO wind_backup (UserName,Password)\n" +
                    "SELECT UserName,Password FROM Table1 WHERE UserName='X' AND Password='X'";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Alarm alarm = getAlarmFromResultSet(rs);
                alarms.add(alarm);
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
        return ;
    }


}
