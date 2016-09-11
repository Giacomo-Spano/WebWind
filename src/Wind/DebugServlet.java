package Wind;

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

        Date localDate = Core.getDate();
        double speed = 88.0;
        double avspeed = 88.0;
        int spotId = 0;

        List<Alarm> list = WindDatastore.getAlarms();
        if (list.size() > 0) {
            int deviceId = list.get(0).deviceId;
            Alarm alarm = list.get(0);
            AlarmModel.sendAlarm(deviceId, alarm, speed, avspeed, localDate, spotId,0);
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

        out.println("\nVERSION = " + Core.getVersion());

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
