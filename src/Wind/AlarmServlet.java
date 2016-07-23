package Wind;


//import com.google.appengine.repackaged.org.joda.time.LocalTime;
import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by giacomo on 14/06/2015.
 */
public class AlarmServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AlarmServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // to accept the json data

        String jsonData = request.getParameter("json");
        String regId = request.getParameter("regId");
        String deletekey = request.getParameter("delete");
        String ringkey = request.getParameter("ring");
        String snoozekey = request.getParameter("snooze");

        String strAlarmId = request.getParameter("alarmId");
        int alarmid;
        if (strAlarmId != null)
            alarmid = Integer.valueOf(strAlarmId);
        else
            alarmid = 0;



        try {
            if (deletekey != null) {

                LOGGER.info("delete regId " + regId + ";id " + alarmid);
                WindDatastore.deleteAlarm(regId, alarmid);

            } else if (ringkey != null) {

                AlarmLog al = new AlarmLog();
                al.insert("ring enable",alarmid,regId,0);

                LOGGER.info("update ring date regId " + regId + ";id " + alarmid);

                Date date = Core.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                //String date = request.getParameter("date");
                //String time = request.getParameter("time");

                //try {
                    WindDatastore.updateAlarmLastRingDate(regId,Integer.valueOf(alarmid),date/*sdf.parse(date + " " + time)*/);
                /*} catch (ParseException e) {
                    e.printStackTrace();
                }*/

            } else if (snoozekey != null) {

                int snoozeMinutes = Integer.valueOf(request.getParameter("minutes"));
                AlarmLog al = new AlarmLog();
                al.insert("snooze alarm",alarmid,regId,snoozeMinutes);
                LOGGER.info("snooze regId " + regId + ";id " + alarmid + ";snooze minutes="+snoozeMinutes);
                WindDatastore.updateAlarmSnoozeMinutes(regId,Integer.valueOf(alarmid),snoozeMinutes);

            } else {

                JSONObject json = new JSONObject(jsonData);
                Alarm alarm = new Alarm(json);
                Core.windDatastore.saveAlarm(regId, alarm);

                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.println(jsonData);
                out.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("JSONException=" + e.toString());


            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            //response.getWriter().write(e.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            String regId = request.getParameter("regId");
            String Id = request.getParameter("Id");
            String test = request.getParameter("testalarm");

            if (test != null && test.equals("true")) {

                LOGGER.info("testing alarm");
                /*LocalTime localTime = AlarmModel.getCurrentTime();
                Date localDate = AlarmModel.getCurrentDate();
                AlarmModel.evaluateAlarms(20.0, 20.0, localTime, localDate, 0);*/

            } else {

                List<Alarm> alarms = WindDatastore.getAlarmsFromRegId(regId);
                if (alarms == null) {
                    LOGGER.info("regId " + regId + " not found");
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
