package Wind;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Devices {

    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public Devices() {
    }

    public List<Device> getDevices() {

        LOGGER.info(" getDeviceFromDeviceId");

        List<Device> list = new ArrayList<Device>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM devices;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Device device = new Device();
                device.id = rs.getInt("id");
                //device.deviceId = rs.getString("deviceid");
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

    public Device getDeviceFromDeviceId(int deviceId) {

        LOGGER.info("getDeviceFromDeviceId: " + deviceId);

        Device device = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM devices WHERE id=" + deviceId + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                device = new Device();
                device.id = rs.getInt("id");
                device.regId = rs.getString("regid");
                device.name = rs.getString("name");
                device.date = rs.getDate("date");
                LOGGER.info(" id=" + device.id + " regId=" + device.regId + " name=" + device.name);
            }
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            LOGGER.info("error=" + se.toString());
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("error=" + e.toString());
            return null;
        }
        return device;
    }

    public Device getDeviceFromRegId(String regId) {

        LOGGER.info(" getDevices");

        Device device = new Device();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM devices WHERE regid='" + regId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                device.id = rs.getInt("id");
                device.regId = rs.getString("regid");
                device.name = rs.getString("name");
                device.date = rs.getDate("date");
                //device.deviceId = rs.getString("deviceid");
            } else {
                device = null;
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
        return device;
    }

    public List<Device> getDevicesWithFavorites(long spotid) {

        // ritorna la lista di tutti i devices che sono associati ad utenti che abbiano
        // tra i favoriti lo spotId passato


        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT favorites.spotid, users.personid, devices.personid, devices.id, devices.regid, devices.name, devices.date " +
                    "FROM users " +
                    "INNER JOIN favorites ON users.personid = favorites.personid " +
                    "INNER JOIN devices ON devices.personid = favorites.personid " +
                    "WHERE favorites.spotid = " + spotid + ";";
            ResultSet rs = stmt.executeQuery(sql);

            List<Device> devices = new ArrayList<Device>();
            while (rs.next()) {
                Device device = new Device();
                device.id = rs.getInt("id");
                device.regId = rs.getString("regid");
                device.name = rs.getString("name");
                device.date = rs.getDate("date");
                device.personId = rs.getString("personid");
                devices.add(device);
            }
            rs.close();
            stmt.close();
            conn.close();
            return devices;

        } catch (SQLException se) {
            se.printStackTrace();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            sql = "DELETE FROM devices WHERE personid=" + "'" + device.personId + "' AND name=" + "'" + device.name + "';";
            Statement stmt = conn.createStatement();
            int res = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            /*sql = "INSERT INTO devices (id, regid, date, name, personid)" +
                    " VALUES (" + device.id + ",\"" + device.regId + "\"," + date + ",\"" + device.name + "\",\"" + device.personId + "\") " +
                    "ON DUPLICATE KEY UPDATE date=" + date + ", name=\"" + device.name + "\", personid=\"" + device.personId + "\";";
*/
            sql = "INSERT INTO devices (id, regid, date, name, personid)" +
                    " VALUES (" + device.id + ",\"" + device.regId + "\"," + date + ",\"" + device.name + "\",\"" + device.personId + "\") ;";

            //Statement stmt = conn.createStatement();
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
            LOGGER.severe(se.toString());
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            LOGGER.severe(e.toString());
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
