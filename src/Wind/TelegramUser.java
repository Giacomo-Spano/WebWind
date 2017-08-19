package Wind;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gs163400 on 22/07/2017.
 */
public class TelegramUser {

    long chatid;
    String firstName;
    String lastName;
    String userName;
    String unit;

    public int insert(long userId, String firstName, String lastName, String userName) {

        if (userId <= 0) return 0;
        if (firstName == null) firstName = "";
        if (userName == null) userName = "";
        if (lastName == null) lastName = "";

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
            sql = "INSERT INTO telegramusers (lastupdate, chatid, firstname, lastname, username)" +
                    " VALUES (" + strdate + ",'" + userId + "','" + firstName + "','" + lastName + "','" + userName  + "')" +
                    " ON DUPLICATE KEY UPDATE lastupdate=" + strdate + ",firstname='" + firstName + "'," +
                    "lastname='" + lastName + "',username='" + userName + "'" + ";";

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

    public boolean setUnit(long userId, String unit) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "UPDATE telegramusers SET unit='" + unit + "' WHERE chatid=" + userId;

            Statement stmt = conn.createStatement();
            stmt.execute(sql);
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


    public boolean read(long chatid) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM telegramusers WHERE chatid='"+chatid+"'";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                userName = rs.getString("username");
                firstName = rs.getString("firstname");
                lastName = rs.getString("lastname");
                unit = rs.getString("unit");
                rs.close();
                stmt.close();
                conn.close();

            } else {
                rs.close();
                stmt.close();
                conn.close();
                return false;
            }

        } catch (SQLException se) {
            se.printStackTrace();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

