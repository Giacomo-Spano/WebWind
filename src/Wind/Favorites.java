package Wind;

import sun.rmi.runtime.Log;
import windalarm.meteodata.PullData;
import windalarm.meteodata.Spot;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Favorites {

    private static final Logger LOGGER = Logger.getLogger(Favorites.class.getName());

    public Favorites() {
    }

    public List<Long> getFavorites(long userid) {

        List<Long> list = new ArrayList<Long>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM favorites WHERE userid=" + userid + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                long spotid = rs.getLong("spotid");
                list.add(spotid);
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

    public int insert(String personid, long spotid) {

        LOGGER.info("insert: personid=" + personid + "spotid=" + spotid);
        int lastid;

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "INSERT INTO favorites (spotid, personid)" +
                    " VALUES (" + spotid + "," + personid + ") ";
            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }
            stmt.close();
            conn.close();
            return lastid;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return 0;
        }
    }

    public static long delete(String personid, long spotid) {

        int deletedItems = 0;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "DELETE FROM favorites WHERE personid='" + personid + "' AND spotid=" + spotid + ";";
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
}
