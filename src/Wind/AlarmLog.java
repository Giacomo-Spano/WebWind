package Wind;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class AlarmLog {

    private static final Logger LOGGER = Logger.getLogger(AlarmLog.class.getName());

    public AlarmLog() {
    }

    public boolean insert(String command, long alarmid, int deviceid, int snoozeminute,int windid) {
        return insert(command, alarmid, deviceid, 0.0, 0.0,-1,snoozeminute,windid);
    }

    public boolean insert(String command, long alarmid, int deviceid, Double speed, Double avspeed, long spotId, int snoozeminute, long windid) {

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            java.util.Date date = Core.getDate();
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:m:ss");
            String strStartDate = "'" + df.format(date) + "'";

            String sql;
            sql = "INSERT INTO alarmlog (deviceid, date, alarmid, command, speed, avspeed, spotid, snoozeminutes, windid)" +
                    " VALUES (" + deviceid + "," + strStartDate + "," + alarmid + ",'" + command + "'," + speed + "," + avspeed + "," + spotId + "," + snoozeminute+ "," + windid + ") " ;

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return false;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
