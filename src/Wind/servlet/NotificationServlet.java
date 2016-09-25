package Wind.servlet;

import Wind.AlarmModel;
import Wind.Core;
import Wind.data.Device;
import com.google.android.gcm.server.Message;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 05/06/2016.
 */

public class NotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());

    public void init() throws ServletException {
        // Do required initialization
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {


    }

    public void destroy() {
        // do nothing.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        LOGGER.info("NotificationServlet::doPost");

        String title    = request.getParameter("title");
        String message = request.getParameter("message");

        LOGGER.info("title:"+title);
        LOGGER.info("message:"+message);



        // Set response content type
        response.setContentType("text/html");
        // Actual logic goes here.
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println("<h1>" + "notification sent" + "</h1>");

            /*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String dateInString = df.format(Core.getDate());
            out.println("Data = " + dateInString);
            out.println("\n");

            String envVar = System.getenv("OPENSHIFT_APP_DNS");
            out.println("OPENSHIFT_APP_DNS = " + envVar);
            out.println("DB user = " + Core.getUser());
            out.println("DB password = " + Core.getPassword());
            out.println("DB host = " + Core.getDbUrl());
            out.println("Temp dir = " + Core.getTmpDir());
            */
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info("IOException:"+e.toString());
        }


        out.close();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        //response.setContentLength(0);

        // --------------- send notification
        List<Device> devices = Core.getDevices();
        sendNotification(devices,title,message);



    }

    public static void sendNotification(List<Device> devices, String title,String message) {

        LOGGER.info("sendNotification: ");
        for (Device device : devices ) {
            LOGGER.info("device: ");
            LOGGER.info("-id: " + device.id);
            LOGGER.info("-name: " + device.name);
            LOGGER.info("-regid: " + device.regId);
        }

        Message notification = new Message.Builder()
                .addData("title", title)
                .addData("message", message)
                .addData("notificationtype", AlarmModel.NotificationType_Info)
                .addData("spotName", "spotName")
                .build();
        Core.sendPushNotification(devices, notification);
    }
}
