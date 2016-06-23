package Wind;

import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.Spot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giacomo on 05/07/2015.
 */
public class MeteoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MeteoServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String lastdata = request.getParameter("lastdata");
        String spotlist = request.getParameter("requestspotlist");
        String history = request.getParameter("history");
        String spot = request.getParameter("spot");

        LOGGER.info("lastdata " + lastdata);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();


        if (spotlist != null && spotlist.equals("true")) {

            LOGGER.info("spotlist");

            out.print("{\"spotlist\" : [");

            ArrayList<Spot> list = Core.getSpotList();

            String str ="";
            for(int i = 0; i < list.size(); i++) {

                Spot s = list.get(i);
                if (i != 0)
                    str += ",";

                str += "{\"spotname\" : \"" + s.name/* AlarmModel.Spot_name[i]*/ + "\",";
                str += "\"id\" : " + "\"" + s.ID/*i*/ + "\"}";

            }
            out.print(str);
            out.println("] }");

        } else if (lastdata != null && lastdata.equals("true")) {

            ArrayList<MeteoStationData> mdList = new ArrayList<MeteoStationData>();

            if (spot == null) {

                ArrayList<Spot> sl = Core.getSpotList();
                for (int i = 0; i < sl.size();i++) {
                    MeteoStationData md = Core.getLastfromID(sl.get(i).ID);
                    if (md != null)
                        mdList.add(md);
                }
            } else {


                String[] l = spot.split(",");
                if (l != null) {
                    for (int i = 0; i < l.length; i++) {
                        int id = Integer.valueOf(l[i]);
                        MeteoStationData md = Core.getLastfromID(id);
                        if (md != null)
                            mdList.add(md);
                    }
                }
            }

            //ArrayList<MeteoStationData> md = PullDataServlet.getLast();
            out.println("{\"meteodata\" : [");

            if (mdList != null) {
                LOGGER.info("md=" + mdList.toString());

                for (int i = 0; i < mdList.size(); i++) {
                    String jsonText = mdList.get(i).toJson();
                    LOGGER.info("jsonText meteodata " + jsonText);

                    out.println(jsonText);
                    if (i != mdList.size() - 1)
                        out.println(",");
                }
            }
            out.println("] }");
        } else if (history != null && history.equals("true") && spot != null){

            LOGGER.info("history");


            Date end = Core.getDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.add(Calendar.HOUR_OF_DAY, - 6); //minus number would decrement the hours
            Date start = cal.getTime();

            MeteoStationData md = new MeteoStationData();
            List<MeteoStationData> list = md.getHistory(Integer.valueOf(spot), start, end);



            //ArrayList<MeteoStationData> md = Core.getHistory(Integer.valueOf(spot));

            LOGGER.info("md=" + list.toString());

            out.println("{\"meteodata\" : [");

            for (int i = 0; i < list.size(); i++) {
                String jsonText = list.get(i).toJson();
                LOGGER.info("jsonText history" + jsonText);

                out.println(jsonText);
                if (i != list.size() - 1)
                    out.println(",");
            }
            out.println("] }");

        } else {
            out.println("no data");
        }
        out.close();


    }


}
