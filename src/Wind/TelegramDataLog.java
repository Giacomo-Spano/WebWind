package Wind;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gs163400 on 22/07/2017.
 */
public class TelegramDataLog {

    protected String getStrDate() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = Core.getDate();
        String strDate = "'" + df.format(date) + "'";
        return strDate;
    }

    public void writelog(String event, long userid, String message) {

        try {

            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "INSERT INTO telegramdatalog (event, date, userid, message) VALUES ('" + event + "'," + getStrDate() + ",'" + userid + "','" + message + "');";
            stmt.executeUpdate(sql);

            // Extract data from result set
            // Clean-up environment
            //rs.close();
            stmt.close();

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }
}
