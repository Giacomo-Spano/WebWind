package Wind;

import com.google.android.gcm.server.Message;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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

        Alarm alarm = new Alarm();
        LOGGER.info("sendAlarm spotID=" + alarm.spotID);
        //SendPushMessages sp = new SendPushMessages();
        //sp.init();
        Message notification = new Message.Builder()
                // .collapseKey(collapsekey) // se c'? gi? un messaggio con lo
                // stesso collapskey e red id allora l'ultimo sostituir? il
                // precedente
                // .timeToLive(3).delayWhileIdle(true) // numero di secondi per
                // i quali il messagio rimane in coda (default 4 week)
                .addData("title", "titolox")
                //.addData("message", "allarme"+spot)
                .addData("spotID", "" + alarm.spotID)
                .addData("startDate", "" + alarm.startDate)
                .addData("startTime", "" + alarm.startTime)
                .addData("lastRingTime", "" + alarm.lastRingTime)
                .addData("endDate", "" + alarm.endDate)
                .addData("endTime", "" + alarm.endTime)
                .addData("avspeed", "" + alarm.avspeed)
                .addData("speed", "" + alarm.speed)

                /*addData("curspeed", "" + speed)
                .addData("curavspeed", "" + avspeed)
                .addData("curlocalTime", "" + currentTime)
                .addData("curlocalDate", "" + currentDate)
                .addData("curspotId", "" + spotId)*/

                .addData("notificationtype", AlarmModel.NotificationType_Alarm)
                .build();

        //Core.sendPushNotification(notification);


        /// ---- end


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
