package Wind.data;

import Wind.Core;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Users {

    private static final Logger LOGGER = Logger.getLogger(Users.class.getName());

    public Users() {
    }

    public int insert(String personId, String personName, String personEmail, String authCode) {

        int lastid;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            Date date = Core.getDate();
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:m:ss");
            String strdate = "'" + df.format(date) + "'";

            String sql;
            sql = "INSERT INTO users (date, personid, personname, personemail, authcode)" +
                    " VALUES (" + strdate + ",\"" + personId + "\",\"" + personName + "\",\"" + personEmail + "\",\"" + authCode  + "\")" +
                    " ON DUPLICATE KEY UPDATE lastupdate=" + strdate + ",personName=\"" + personName + "\""
                            + ",personemail=\"" + personEmail + "\",authcode=\"" + authCode + "\"" + ";";

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = 1;
            }
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return -1;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return -1;
        }
        return lastid;
    }
}
