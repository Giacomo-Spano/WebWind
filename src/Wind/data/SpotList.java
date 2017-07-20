package Wind.data;

import Wind.Core;
import windalarm.meteodata.PullData;
import windalarm.meteodata.Spot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class SpotList {

    private static final Logger LOGGER = Logger.getLogger(SpotList.class.getName());

    public SpotList() {
    }

    public List<PullData> getSpotList() {

        LOGGER.info(" getSpotList");

        List<PullData> list = new ArrayList<PullData>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM spot WHERE enabled=1;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                PullData spot = getSpotFromResultSet(rs);
                if (spot != null)
                    list.add(spot);
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

    private PullData getSpotFromResultSet(ResultSet rs) throws SQLException {

        String className = rs.getString("classname");

        try {
            Class clazz = Class.forName(className);
            PullData spot = (PullData) clazz.newInstance();

            if (rs.getBoolean("id") == false)
                return null;

            spot.setSpotId(rs.getInt("id"));
            spot.setName(rs.getString("name"));
            spot.setShortName(rs.getString("shortname"));
            spot.setMeteodataUrl(rs.getString("meteodataurl"));
            spot.setSourceUrl(rs.getString("sourceurl"));
            spot.setWebcamUrl(1,rs.getString("webcamurl"));
            spot.setWebcamUrl(2,rs.getString("webcamurl2"));
            spot.setWebcamUrl(3,rs.getString("webcamurl3"));
            spot.setWindguruId(rs.getString("windguruid"));
            spot.setWindfinderId(rs.getString("windfinderid"));
            spot.setOpenweathermapId(rs.getString("openweathermapid"));

            return spot;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Spot getSpotFromId(int id) {

        LOGGER.info("getSpotFromId: " + id);

        Spot spot = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM spot WHERE id=" + id + " LIMIT 1;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                spot = getSpotFromResultSet(rs);
            }
            rs.close();
            stmt.close();
            conn.close();
            return spot;

        } catch (SQLException se) {
            se.printStackTrace();
            LOGGER.info("error=" + se.toString());
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("error=" + e.toString());
            return null;
        }
    }
}
