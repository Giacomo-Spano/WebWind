package Wind.data;

import Wind.Core;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Locations {

    private static final Logger LOGGER = Logger.getLogger(Locations.class.getName());

    public Locations() {
    }

    public List<Location> getLocations(String source, String filter) {

        LOGGER.info(" getLocations " + source);

        List<Location> list = new ArrayList<Location>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            //sql = "SELECT * FROM city WHERE name like %" + filter + "%;";

            // SELECT * FROM city AS locations INNER JOIN country_code on locations.country=country_code.code
            // WHERE locations.name like '%foll%' OR country_code.country like '%ita%'
            //filter = "foll";

            sql = "SELECT * FROM city AS locations INNER JOIN country_code on locations.country=country_code.code" +
                    " WHERE locations.name like '%" + filter + "%'" +
                    " OR country_code.country like '%" + filter + "%'";


            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Location location = new Location();
                location.id = rs.getString("id");
                location.name = rs.getString("name");
                location.lat = rs.getDouble("lat");
                location.lon = rs.getDouble("lon");
                location.countryCode = rs.getString("code");
                location.country = rs.getString("country_code.country");
                list.add(location);
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

}
