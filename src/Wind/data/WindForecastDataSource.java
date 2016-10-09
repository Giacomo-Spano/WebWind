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

    public WindForecast getForecast(long spotId, String source) {

        WindForecast f = new WindForecast(spotId);
        f.spotId = spotId;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM forecast WHERE spotid=" + spotId + " AND source='" + source + "';";
            ResultSet rs = stmt.executeQuery(sql);


            if (rs.next()) {
                //f.initDate = rs.getDate("startdate");
                f.id = rs.getInt("id");
                f.sourceId = rs.getString("sourceid");
                f.sourceSpotName = rs.getString("sourcespotname");
                f.lat = rs.getDouble("lat");
                f.lon = rs.getDouble("lon");
                f.lastUpdate = rs.getDate("lastupdate");
                rs.close();
                //stmt.close();
            } else {
                rs.close();
                stmt.close();
                conn.close();
                return null;
            }

            sql = "SELECT * FROM windforecastdata WHERE forecastid=" + f.id + ";";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {

                String strDate = rs.getString("datetime");
                if (strDate != null) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = df.parse(strDate);
                    f.datetimes.add(date);
                    double value = rs.getDouble("speed");
                    f.speeds.add(value);
                    value = rs.getDouble("direction");
                    f.speedDirs.add(value);
                    value = rs.getDouble("maxspeed");
                    f.maxSpeeds.add(value);
                    value = rs.getDouble("temperature");
                    f.temperatures.add(value);
                    value = rs.getDouble("maxtemperature");
                    f.maxtemperatures.add(value);
                    value = rs.getDouble("mintemperature");
                    f.mintemperatures.add(value);
                    int intval = rs.getInt("humidity");
                    f.humidities.add(intval);
                    String strval = rs.getString("weather");
                    f.weathers.add(strval);
                    strval = rs.getString("weatherdescription");
                    f.weatherdescriptions.add(strval);
                    strval = rs.getString("icon");
                    f.icons.add(strval);
                    intval = rs.getInt("cloudPercentage");
                    f.cloudPercentages.add(intval);
                    value = rs.getDouble("pressure");
                    f.pressures.add(value);
                    value = rs.getDouble("windchill");
                    f.windchills.add(value);
                    value = rs.getDouble("rain");
                    f.rains.add(value);


                }
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
            conn.setAutoCommit(false);

            String sql;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //String lastupdate = "'" + df.format(Core.getDate()) + "'";
            String lastupdate = "'" + df.format(forecast.lastUpdate) + "'";
            int oldforecastid = -1;
            int forecastid = -1;

            // create statement
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // delete previous forecast (if exist)
            sql = "SELECT id FROM forecast WHERE spotid=" + forecast.spotId + " AND source='" + forecast.source + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                forecastid = rs.getInt(1);

                stmt.clearBatch();
                sql = "DELETE FROM windforecastdata WHERE forecastid=" + forecastid + ";";
                stmt.addBatch(sql);

                sql = "UPDATE forecast SET sourcespotname='" + forecast.sourceSpotName + "', sourceid='" + forecast.sourceId + "',lastupdate=" + lastupdate
                        + "WHERE id=" + forecastid + ";";
                stmt.addBatch(sql);

                stmt.executeBatch();

            } else {
                sql = "INSERT INTO forecast (id, spotid, sourceid, sourcespotname, source, lastupdate, lat, lon )" +
                        " VALUES (0," + forecast.spotId + ",'" + forecast.sourceId + "','" + forecast.sourceSpotName + "','" + forecast.source + "','" + lastupdate + "',"
                        + forecast.lat + "," + forecast.lon + ") "
                        + ";";
                stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    forecastid = rs.getInt(1);

                }
                // cancella i vecchi record se per caso ce ne sono di sbagliati
                stmt.clearBatch();
                sql = "DELETE FROM windforecastdata WHERE forecastid=" + forecastid + ";";
                stmt.addBatch(sql);
                stmt.executeBatch();
            }

            stmt.clearBatch();
            for (int i = 0; i < forecast.datetimes.size(); i++) {

                String strDatetime = "";
                if (forecast.datetimes.size() > i ) {
                    Date datetime = forecast.datetimes.get(i);
                    strDatetime = "'" + df.format(datetime) + "'";
                }

                double speed = -1;
                if (forecast.speeds.size() > i )
                    speed = forecast.speeds.get(i);

                double maxspeed = -1;
                if (forecast.maxSpeeds.size() > i )
                    maxspeed = forecast.maxSpeeds.get(i);

                double direction = -1;
                if (forecast.speedDirs.size() > i )
                    direction = forecast.speedDirs.get(i);

                double temmperature = -1;
                if (forecast.temperatures.size() > i )
                    temmperature = forecast.temperatures.get(i);

                double maxtemperature = -1;
                if (forecast.maxtemperatures.size() > i )
                    maxtemperature = forecast.maxtemperatures.get(i);

                double mintemperature = -1;
                if (forecast.mintemperatures.size() > i )
                    mintemperature = forecast.mintemperatures.get(i);

                int humidity = -1;
                if (forecast.humidities.size() > i )
                    humidity = forecast.humidities.get(i);

                String weather = "''";
                if (forecast.weathers.size() > i )
                    weather = "'" + forecast.weathers.get(i) + "'";

                String weatherdescription = "''";
                if (forecast.weatherdescriptions.size() > i )
                    weatherdescription = "'" + forecast.weatherdescriptions.get(i) + "'";

                String icon = "''";
                if (forecast.icons.size() > i )
                    icon = "'" + forecast.icons.get(i) + "'";

                int cloudpercentage = -1;
                if (forecast.cloudPercentages.size() > i )
                    cloudpercentage = forecast.cloudPercentages.get(i);

                double pressure = -1;
                if (forecast.pressures.size() > i )
                    pressure = forecast.pressures.get(i);

                double windchill = -1;
                if (forecast.windchills.size() > i )
                    windchill = forecast.windchills.get(i);

                double rain = -1;
                if (forecast.rains.size() > i )
                    rain = forecast.rains.get(i);



                sql = "INSERT INTO windforecastdata (forecastid, datetime, speed, maxspeed, direction, temperature, " +
                        "maxtemperature, mintemperature, humidity, weather, weatherdescription, icon, cloudpercentage," +
                        " pressure, windchill, rain )" +
                        " VALUES (" + forecastid + ","
                        + strDatetime + ","
                        + speed + ","
                        + maxspeed + ","
                        + direction + ","
                        + temmperature + ","

                        + maxtemperature + ","
                        + mintemperature + ","
                        + humidity + ","
                        + weather + ","
                        + weatherdescription + ","
                        + icon + ","
                        + cloudpercentage + ","
                        + pressure + ","
                        + windchill + ","
                        + rain
                        + ");";

                stmt.addBatch(sql);
            }
            stmt.executeBatch();
            conn.commit();
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
