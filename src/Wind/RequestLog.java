package Wind;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class RequestLog {

    private static final Logger LOGGER = Logger.getLogger(RequestLog.class.getName());

    public RequestLog() {
    }

    public boolean insert(String authcode, String type, int user, String params) {

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            Date date = Core.getDate();
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:m:ss");
            String strStartDate = "'" + df.format(date) + "'";

            String sql;
            sql = "INSERT INTO alarmlog (authcode, type, date, user, params)" +
                    " VALUES ('" + authcode + "','" + type + "'," + strStartDate + "," + user + ",'" + params + "') " ;

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return false;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
