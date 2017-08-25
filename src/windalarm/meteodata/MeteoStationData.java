package windalarm.meteodata;

//import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;

import Wind.Core;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

/**
 * Created by giacomo on 07/06/2015.
 */
public class MeteoStationData {

    private static final Logger LOGGER = Logger.getLogger(MeteoStationData.class.getName());

    public long id;
    public boolean offline;
    public Double speed;
    public Double averagespeed = -1.0;
    public String direction;
    public Double directionangle;
    public Double trend;
    public java.util.Date datetime;
    public Double temperature;
    public Double pressure;
    public Double humidity;
    public Double rainrate;
    public java.util.Date sampledatetime;
    public String spotName = "name";
    public String source = "source";
    public long spotID = -1;
    public String webcamurl = "";
    public String webcamurl2 = null;
    public String webcamurl3 = null;
    private ArrayList<List<String>> symbolList = new ArrayList<List<String>>();

    public MeteoStationData() {

        List<String> symbols;
        symbols = asList("E", "EAST", "Est"); // 0
        symbolList.add(symbols);
        symbols = asList("NEE", "ENE", "Est-NordEst", "Est-Nord-Est");//1
        symbolList.add(symbols);
        symbols = asList("NE", "EN", "Nord-Est");//2
        symbolList.add(symbols);
        symbols = asList("NNE", "ENE", "Nord-Nord-Est", "Nord-NordEst");//3
        symbolList.add(symbols);
        symbols = asList("N", "Nord");//4
        symbolList.add(symbols);
        symbols = asList("NWN", "NNW", "NON", "NNO", "NordOvest-Nord", "Nord-Ovest-Nord");//5
        symbolList.add(symbols);
        symbols = asList("NW", "WN", "NO", "ON", "Nord-Ovest");//6
        symbolList.add(symbols);
        symbols = asList("WNW", "NWW", "ONO", "NON", "Ovest-NordOvest", "Ovest-Nord-Ovest");//7
        symbolList.add(symbols);
        symbols = asList("W", "O", "Ovest");//8
        symbolList.add(symbols);
        symbols = asList("WSW", "SWW", "OSO", "SSO", "Sud-SudOvest", "Sud-Sud-Ovest");//9
        symbolList.add(symbols);
        symbols = asList("SW", "WS", "SO", "OS", "Sud-Ovest");//10
        symbolList.add(symbols);
        symbols = asList("SSW", "SWS", "OSO", "SOS", "SudOvest-Sud", "Sud-Ovest-Sud");//11
        symbolList.add(symbols);
        symbols = asList("S", "Sud");//12
        symbolList.add(symbols);
        symbols = asList("SSE", "SEE", "Sud-Sud-Est");//13
        symbolList.add(symbols);
        symbols = asList("SE", "ES", "Sud-Est"); // 14
        symbolList.add(symbols);
        symbols = asList("ESE", "SEE", "Est-Sud-Est"); // 15
        symbolList.add(symbols);
    }

    public double getAngleFromDirectionSymbol(String symbol) {

        if (symbol == null || symbol.equals("")) {
            return -1.0;
        }

        for (int i = 0; i < symbolList.size(); i++) {
            for (int k = 0; k < symbolList.get(i).size(); k++) {
                if (symbolList.get(i).get(k).equalsIgnoreCase(symbol))
                    return i * 22.5;
            }
        }
        LOGGER.severe("cannot decode wind direction " + symbol);
        return -1;
    }

    public String toJson() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("windid", id);
            obj.put("speed", speed);
            obj.put("avspeed", averagespeed);
            obj.put("direction", direction);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
            if (datetime != null)
                obj.put("datetime", dateFormat.format(datetime));
            if (sampledatetime != null)
                obj.put("sampledatetime", dateFormat.format(sampledatetime));
            obj.put("temperature", temperature);
            obj.put("pressure", pressure);
            obj.put("humidity", humidity);
            obj.put("rainrate", rainrate);
            obj.put("spotname", spotName);
            obj.put("directionangle", directionangle);
            obj.put("trend", trend);
            obj.put("spotid", spotID);
            if (webcamurl != null)
                obj.put("webcamurl", webcamurl);
            if (webcamurl2 != null)
                obj.put("webcamurl2", webcamurl2);
            if (webcamurl3 != null)
                obj.put("webcamurl3", webcamurl3);
            if (source != null)
                obj.put("source", source);

            obj.put("offline", offline);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return obj.toString();
    }

    public String toSpeedHistoryJson() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("speed", speed);
            obj.put("avspeed", averagespeed);
            obj.put("direction", direction);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
            if (datetime != null) obj.put("datetime", dateFormat.format(datetime));
            obj.put("directionangle", directionangle);
            obj.put("temperature", temperature);
            obj.put("trend", trend);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return obj.toString();
    }


    public String fromJson(JSONObject obj) {

        try {
            if (obj.has("id"))
                spotID = obj.getInt("id");
            if (obj.has("speed"))
                speed = obj.getDouble("speed");
            if (obj.has("avspeed"))
                averagespeed = obj.getDouble("avspeed");
            if (obj.has("direction"))
                direction = obj.getString("direction");
            if (obj.has("datetime")) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    datetime = formatter.parse(obj.getString("datetime"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (obj.has("temperature"))
                temperature = obj.getDouble("temperature");
            if (obj.has("pressure"))
                pressure = obj.getDouble("pressure");
            if (obj.has("humidity"))
                humidity = obj.getDouble("humidity");
            if (obj.has("rainrate"))
                rainrate = obj.getDouble("rainrate");
            if (obj.has("sampledatetime")) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    sampledatetime = formatter.parse(obj.getString("datetime"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (obj.has("directionangle"))
                directionangle = obj.getDouble("directionangle");
            if (obj.has("trend"))
                trend = obj.getDouble("trend");
            if (obj.has("spotid"))
                spotID = obj.getInt("spotid");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return obj.toString();
    }

    public static Double knotsToKMh(double knots) {

        double kmh = knots * 1.85200;
        kmh = Math.round(kmh * 10.0);
        kmh = kmh / 10;
        return kmh;
    }

    public static Double kmhToKnots(double kmh) {

        double knots = kmh / 1.85200;
        knots = Math.round(knots * 10.0);
        knots = knots / 10;
        return knots;
    }

    public int insert() {

        int lastid;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDatetime = "''";
            if (datetime == null) {
                LOGGER.severe("datetime null");
                return 0;
            }
            strDatetime = "'" + df.format(datetime) + "'";

            String strSampleDatetime = "''";
            if (sampledatetime == null) {
                LOGGER.severe("sampledatetime null");
                return 0;
            }
            strSampleDatetime = "'" + df.format(sampledatetime) + "'";

            // controlla se esiste già un record con la stessa data e ora
            MeteoStationData lastMd = getLastMeteoStationData(spotID);
            if (lastMd != null && datetime.compareTo(lastMd.datetime) == 0
                    && lastMd.speed.compareTo(speed) == 0
                    /*&& lastMd.averagespeed == averagespeed*/) { //non controllare avspeeed perchè è calcolata
                LOGGER.info("duplicate datetime: skip insert ");
                return 0;
            }

            String sql;
            sql = "INSERT INTO wind (spotid, datetime, sampledatetime, speed, averagespeed, direction, directionangle, temperature, humidity, pressure,trend)" +
                    " VALUES (" + spotID + "," + strDatetime + "," + strSampleDatetime + "," + speed + "," + averagespeed + ",'" + direction + "'," + directionangle + "," + temperature + "," + humidity + "," + pressure + "," + trend + ") ";

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

    public List<MeteoStationData> getHistory(Long spotId, Date startDate, Date endDate, Long lastWindId, int maxpoint) {

        List<MeteoStationData> list = new ArrayList<MeteoStationData>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
            String strStartDate = "'" + df.format(startDate) + "'";
            String strEndDate = "'" + df.format(endDate) + "'";

            String sql;
            sql = "SELECT * FROM wind WHERE spotid=" + spotId
                    + " AND datetime BETWEEN " + strStartDate + " and " + strEndDate ;
            /*if ((long) lastWindId != -1)
                sql += " AND id > " + lastWindId;*/
            sql += " ORDER BY datetime ASC;";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                MeteoStationData md = getMeteoStationDataFromResulset(rs);

                list.add(md);
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

        //return list;
        if (maxpoint <= 0 || list.size() < maxpoint)
            return list;

        InterpolationSearch interpolation = new InterpolationSearch();
        List<MeteoStationData> iList = interpolation.getInterpolatedArray(list,startDate,endDate,maxpoint);
        return iList;
    }
    public List<MeteoStationData> getLastFavorites(String personId) {

        List<MeteoStationData> list = new ArrayList<MeteoStationData>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            /*  SELECT * FROM
                (SELECT max(id) as maxid,spotid
                FROM wind GROUP BY spotid ORDER by id) as lastdata
                INNER JOIN wind ON wind.id = lastdata.maxid
                INNER JOIN spot ON lastdata.spotid = spot.id
                INNER JOIN favorites ON lastdata.spotid = favorites.spotid WHERE personid = '112171344340940317913';
            */
            sql = "SELECT * FROM\n" +
                    "(SELECT max(id) as maxid,spotid\n" +
                    "FROM wind GROUP BY spotid ORDER by id) as lastdata\n" +
                    "INNER JOIN wind ON wind.id = lastdata.maxid\n" +
                    "INNER JOIN spot ON lastdata.spotid = spot.id\n" +
                    "INNER JOIN favorites ON lastdata.spotid = favorites.spotid WHERE personid = '" + personId + "' ;";



            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                MeteoStationData md = getMeteoStationDataFromResulset(rs);
                list.add(md);
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

    public List<MeteoStationData> getLastSamples(long spotId, long nSamples) {

        List<MeteoStationData> list = new ArrayList<MeteoStationData>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM wind WHERE spotid=" + spotId
                    + " ORDER BY datetime DESC LIMIT " + nSamples + ";";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                MeteoStationData md = getMeteoStationDataFromResulset(rs);
                list.add(md);
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

    private MeteoStationData getMeteoStationDataFromResulset(ResultSet rs) throws SQLException {
        MeteoStationData md = new MeteoStationData();
        md.id = rs.getLong("id");
        md.datetime = rs.getTimestamp("datetime");
        md.spotID = rs.getInt("spotid");
        md.sampledatetime = rs.getTimestamp("sampledatetime");
        md.speed = rs.getDouble("speed");
        md.averagespeed = rs.getDouble("averagespeed");
        md.directionangle = rs.getDouble("directionangle");
        md.direction = rs.getString("direction");
        md.temperature = rs.getDouble("temperature");
        md.humidity = rs.getDouble("humidity");
        md.pressure = rs.getDouble("pressure");
        md.trend = rs.getDouble("trend");

        // queste colonne ci sono solo quando la query è in join con tabella spot e favorites
        if (hasColumn(rs,"name"))
            md.spotName = rs.getString("name");
        if (hasColumn(rs,"sourceurl"))
            md.source = rs.getString("sourceurl");
        if (hasColumn(rs,"webcamurl"))
            md.webcamurl = rs.getString("webcamurl");
        if (hasColumn(rs,"webcamurl2"))
            md.webcamurl2 = rs.getString("webcamurl2");
        if (hasColumn(rs,"webcamurl3"))
            md.webcamurl3 = rs.getString("webcamurl3");

        return md;
    }

    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    public MeteoStationData getLastMeteoStationData(long spotId) {

        MeteoStationData md = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM wind WHERE spotid=" + spotId + " ORDER BY sampledatetime DESC LIMIT 1;";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                md = getMeteoStationDataFromResulset(rs);
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
        return md;
    }
}
