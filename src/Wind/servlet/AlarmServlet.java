package Wind.servlet;


//import com.google.appengine.repackaged.org.joda.time.LocalTime;

import Wind.*;
import Wind.data.Alarm;
import Wind.data.AlarmLog;
import Wind.data.WindDatastore;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giacomo on 14/06/2015.
 */
public class AlarmServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AlarmServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String jsonData = request.getParameter("json");
        String deletekey = request.getParameter("delete");
        String ringkey = request.getParameter("ring");
        String snoozekey = request.getParameter("snooze");
        String strAlarmId = request.getParameter("alarmId");
        String test = request.getParameter("test");

        int alarmid;
        if (strAlarmId != null)
            alarmid = Integer.valueOf(strAlarmId);
        else
            alarmid = 0;

        try {
            if (deletekey != null) {
                String tokenid = request.getParameter("tokenid");
                User user = new User();
                if (!Core.validateTokenId(tokenid,user)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain");
                    return;
                }

                LOGGER.info("deleteAll alarm  id=" + alarmid);
                WindDatastore.deleteAlarm(alarmid);
            } else if (ringkey != null) { //
                AlarmLog al = new AlarmLog();
                al.insert("ring enable", alarmid, -1, 0, 0);
                LOGGER.info("update ring date alarmid " + alarmid);
            } else if (snoozekey != null) { // snooze
                int snoozeMinutes = Integer.valueOf(request.getParameter("minutes"));
                AlarmLog al = new AlarmLog();
                al.insert("snooze alarm", alarmid, -1, snoozeMinutes, 0);
                LOGGER.info("snooze alarmid " + alarmid + ";snooze minutes=" + snoozeMinutes);
                WindDatastore.updateAlarmSnoozeMinutes(alarmid, snoozeMinutes);
            } else if (test != null && test.equals("true")) { // test alarm
                String tokenid = request.getParameter("tokenid");
                User user = new User();
                if (!Core.validateTokenId(tokenid,user)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain");
                    return;
                }

                LOGGER.info("testing alarm");
                Date localDate = Core.getDate();
                double speed = 99.0;
                double avspeed = 99.0;
                int spotId = 0;
                List<Alarm> list = WindDatastore.getAlarms();
                Iterator<Alarm> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Alarm alarm = iterator.next();
                    if (alarm.id == alarmid) {
                        AlarmModel.sendAlarm(alarm.deviceId, alarm, speed, avspeed, localDate, spotId, 0);
                    }
                }
            } else { // save alarm
                String tokenid = request.getParameter("tokenid");
                User user = new User();
                if (!Core.validateTokenId(tokenid,user)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain");
                    return;
                }

                JSONObject json = new JSONObject(jsonData);
                Alarm alarm = new Alarm(json);
                Core.windDatastore.saveAlarm(alarm);
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.println(jsonData);
                out.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("JSONException=" + e.toString());


            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String tokenid = request.getParameter("tokenid");
        User user = new User();
        if (!Core.validateTokenId(tokenid,user)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            return;
        }

        try {

            String strDeviceId = request.getParameter("deviceId");
            long deviceId = Integer.valueOf(strDeviceId);
            String strSpotId = request.getParameter("spotId");
            long spotId;
            if (strSpotId != null)
                spotId = Integer.valueOf(strSpotId);
            else
                spotId = -1;

            List<Alarm> alarms = WindDatastore.getAlarmsFromDeviceID(deviceId,spotId);
            if (alarms == null) {
                LOGGER.info("deviceId " + deviceId + " not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{\"alarms\" : [");

            for (int i = 0; i < alarms.size(); i++) {
                String jsonText = alarms.get(i).toJson();
                LOGGER.info("jsonText " + jsonText);

                out.println(jsonText);
                if (i != alarms.size() - 1)
                    out.println(",");
            }
            out.println("] }");
            out.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
