package Wind;

import com.google.android.gcm.server.Message;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 05/06/2016.
 */

public class DebugServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DebugServlet.class.getName());

    private String message;

    public void init() throws ServletException {
        // Do required initialization
        message = "Hello World xxxxx";
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {

        // --------------- send notification

        Date localTime = Core.getDate();
        Date localDate = Core.getDate();
        double speed = 29.0;
        double avspeed = 29.0;

        //WindDatastore.updateAlarmLastRingDate(regi,Integer.valueOf(alarmid),date/*sdf.parse(date + " " + time)*/);


        int spotId = 0;
        List<Alarm> list = WindDatastore.getActiveAlarm(speed,avspeed,localTime,localDate,spotId);
        if (list.size() > 0) {
            String registrationId = list.get(0).regId;
            Alarm alarm = list.get(0);
            AlarmModel.sendAlarm(registrationId, alarm, speed, avspeed, localTime, localDate, spotId);
        }




        // Set response content type
        response.setContentType("text/html");

        // Actual logic goes here.
        PrintWriter out = response.getWriter();
        out.println("<h1>" + message + "</h1>");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateInString = df.format(Core.getDate());
        out.println("Data = " + dateInString);
        out.println("\n");


        String envVar = System.getenv("OPENSHIFT_APP_DNS");
        out.println("OPENSHIFT_APP_DNS = " + envVar);
        out.println("DB user = " + Core.getUser());
        out.println("DB password = " + Core.getPassword());
        out.println("DB host = " + Core.getDbUrl());
        out.println("Temp dir = " + Core.getTmpDir());

    }

    public void destroy() {
        // do nothing.
    }



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {



        String authCode = req.getParameter("authCode");






    }
}
