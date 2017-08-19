package Wind;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gs163400 on 22/07/2017.
 */
public class SpotZones {

    public static SpotZone getFromName(String name) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM spotzones WHERE name='"+name+"'";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                SpotZone zone = new SpotZone();
                zone.name = rs.getString("name");
                zone.id = rs.getInt("id");
                zone.father = rs.getInt("father");
                zone.spotlist = Core.getSpotListFromZone(zone.id);
                rs.close();
                stmt.close();
                conn.close();
                return zone;
            }
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static SpotZone getFromId(int id) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM spotzones WHERE id='"+id+"'";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                SpotZone zone = new SpotZone();
                zone.name = rs.getString("name");
                zone.id = rs.getInt("id");
                zone.father = rs.getInt("father");
                zone.spotlist = Core.getSpotListFromZone(zone.id);

                rs.close();
                stmt.close();
                conn.close();
                return zone;
            }
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static List<SpotZone> getChildren(int id) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM spotzones WHERE father='"+id+"'";
            ResultSet rs = stmt.executeQuery(sql);

            List<SpotZone> list = new ArrayList<>();
            while (rs.next()) {
                SpotZone zone = new SpotZone();
                zone.name = rs.getString("name");
                zone.id = rs.getInt("id");
                zone.father = rs.getInt("father");
                zone.spotlist = Core.getSpotListFromZone(zone.id);
                list.add(zone);
            }
            rs.close();
            stmt.close();
            conn.close();
            return list;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

