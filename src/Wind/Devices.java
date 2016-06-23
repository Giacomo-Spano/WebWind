package Wind;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Devices {

    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());


    public Devices() {

    }

    /*public Device getFromId(int id) {
        Iterator<Device> iterator = mDeviceList.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            if (device.id == id)
                return device;
        }
        return null;
    }*/

    /*public void read() {

        LOGGER.info(" read devices");

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.DB_URL, Core.USER, Core.PASS);
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT id, name, regid, date FROM devices";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {


                Device device = new Device();
                device.id = rs.getInt("id");
                device.regId = rs.getString("regid");
                device.name = rs.getString("name");
                device.date = rs.getDate("date");

                mDeviceList.add(device);
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
    }*/

    public List<Device> getDevices(String regId) {

        LOGGER.info(" getDevices");

        List<Device> list = new ArrayList<Device>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT id, name, regid, date FROM devices";
            if (regId != null)
                sql += " WHERE regid='" + regId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Device device = new Device();
                device.id = rs.getInt("id");
                device.regId = rs.getString("regid");
                device.name = rs.getString("name");
                device.date = rs.getDate("date");

                list.add(device);
            }
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public int insert(Device device) {

        int lastid;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = "NULL";
            date = "'" + df.format((device.date)) + "'";

            String sql;
            sql = "INSERT INTO devices (id, regid, date, name)" +
                    " VALUES (" + device.id + ",\"" + device.regId + "\"," + date + ",\"" + device.name + "\") " +
                    "ON DUPLICATE KEY UPDATE id=" + device.id + ", regid=\"" + device.regId + "\", date=" + date + ", name=\"" + device.name + "\"";

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = device.id;
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
        return lastid;
    }

    public int delete(String regId) {

        int res = 0;
        try {

            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "DELETE FROM devices WHERE regid=" + "'" + regId + "'";
            Statement stmt = conn.createStatement();
            res = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.close();
            conn.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return res;
    }


}
