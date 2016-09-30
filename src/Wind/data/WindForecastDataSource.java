package Wind.data;

import Wind.Core;
import windalarm.meteodata.WindForecast;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class WindForecastDataSource {

    private static final Logger LOGGER = Logger.getLogger(WindForecastDataSource.class.getName());

    public WindForecastDataSource() {
    }

    public WindForecast getForecast(long spotId) {

        WindForecast f = new WindForecast(spotId);
        f.spotId = spotId;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM forecast WHERE spotid=" + spotId + ";";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                f.initDate = rs.getDate("startdate");
                f.windguruspotId = rs.getInt("windguruid");
                f.windguruSpotName = rs.getString("windguruname");
                rs.close();
                //stmt.close();
            } else {
                rs.close();
                stmt.close();
                conn.close();
                return null;
            }

            sql = "SELECT * FROM windforecastdata WHERE spotid=" + spotId + ";";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date datetime = rs.getDate("datetime");
                f.datetimes.add(datetime);
                double value = rs.getDouble("speed");
                f.speeds.add(value);
                value = rs.getDouble("direction");
                f.speedDirs.add(value);
                value = rs.getDouble("temperature");
                f.temperatures.add(value);
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
        return f;
    }

    public int insert(WindForecast forecast) {

        int lastid;

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = "'" + df.format((forecast.initDate)) + "'";

            String lastupdate = "'" + df.format(Core.getDate()) + "'";

            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sql1 = "INSERT INTO forecast (spotid, startdate, windguruid, windguruname, lastupdate )" +
                    " VALUES (" + forecast.spotId + "," + date + "," + forecast.windguruspotId + ",'" + forecast.windguruSpotName + "'," + lastupdate +  ") " +
                    "ON DUPLICATE KEY UPDATE startdate=" + date + ", windguruid=" + forecast.windguruspotId + ", windguruname='" + forecast.windguruSpotName  + "'" +
                    ",lastupdate=" + lastupdate + ";";

            String sql2 = "DELETE FROM windforecastdata WHERE spotid=" + forecast.spotId;

            stmt.addBatch(sql1);
            stmt.addBatch(sql2);

            for (int i = 0; i < forecast.datetimes.size(); i++) {

                Date datetime = forecast.datetimes.get(i);
                String strDatetime = "'" + df.format(datetime) + "'";
                double speed = forecast.speeds.get(i);
                double direction = forecast.speedDirs.get(i);
                double temmperature = forecast.temperatures.get(i);

                sql = "INSERT INTO windforecastdata (spotid, datetime, speed, direction, temperature )" +
                        " VALUES (" + forecast.spotId + ","
                                    + strDatetime + ","
                                    + speed + ","
                                    + direction + ","
                                    + temmperature + ");";

                stmt.addBatch(sql);
            }

            conn.setAutoCommit(false);

            stmt.executeBatch();
            conn.commit();


            /*ResultSet rs  = stmt.executeQuery("select * from forecast");
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }*/
            stmt.close();
            conn.close();
            return 0;
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

        /*int deletedItems = 0;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "DELETE FROM favorites WHERE personid='" + personid + "'";
            if (spotid != -1)
                sql += " AND spotid=" + spotid + ";";
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
        return deletedItems;*/
        return 0;
    }
}
