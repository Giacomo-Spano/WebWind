package Wind;

import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.Spot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
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
        String fullinfo = request.getParameter("fullinfo");

        String authcode = request.getHeader("user");

        String type = "";
        int user = 1;
        String params = "params";

        LOGGER.info("lastdata " + lastdata);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();



        if (spotlist != null && spotlist.equals("true")) {

            RequestLog req = new RequestLog();
            req.insert(authcode,"spotlist",0,"");

            LOGGER.info("spotlist");
            out.print("{\"spotlist\" : [");

            //MeteoStationData md = new MeteoStationData();
            //List<MeteoStationData> mdList = md.getLastMeteoStationData();
            //Iterator<MeteoStationData> iterator = mdList.iterator();

            List<Spot> sl = Core.getSpotList();
            Iterator<Spot> iterator = sl.iterator();

            String str = "";
            int count = 0;
            while (iterator.hasNext()) {

                Spot sp = iterator.next();
                MeteoStationData meteoStationData = Core.getLastfromID(sp.ID);
                if (meteoStationData == null)
                    continue;

                if (count++ != 0)
                    str += ",";

                str += "{\"spotname\" : \"" + sp.name + "\",";
                str += "\"sourceurl\" : " + "\"" + sp.sourceUrl + "\",";
                str += "\"webcamurl\" : " + "\"" + sp.webcamUrl + "\",";


                if (fullinfo != null && fullinfo.equals("true")) {
                    SimpleDateFormat df = new SimpleDateFormat("DD/MM/YYY HH:mm:ss");
                    str += "\"speed\" : " + meteoStationData.speed + ",";
                    str += "\"avspeed\" : " + meteoStationData.averagespeed + ",";
                    str += "\"direction\" : " + "\"" + meteoStationData.direction + "\",";
                    str += "\"directionangle\" : " + meteoStationData.directionangle + ",";
                    str += "\"datetime\" : " + "\"" + meteoStationData.sampledatetime + "\",";
                }
                str += "\"id\" : " + meteoStationData.spotID + "}";
            }


            str += "] }";
            response.setHeader("Length",""+str.length());
            out.print(str);
            //out.println("] }");

        } else if (lastdata != null && lastdata.equals("true")) { // Last meteo data

            RequestLog req = new RequestLog();
            req.insert(authcode,"lastdata",0,"");

            ArrayList<MeteoStationData> mdList = new ArrayList<MeteoStationData>();

            if (spot == null) {

                ArrayList<Spot> sl = Core.getSpotList();
                for (int i = 0; i < sl.size(); i++) {
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
           String str="{\"meteodata\" : [";

            if (mdList != null) {
                LOGGER.info("md=" + mdList.toString());

                for (int i = 0; i < mdList.size(); i++) {

                    MeteoStationData md = mdList.get(i);
                    Spot spotInfo = Core.getSpotFromID(md.spotID);
                    md.spotName = spotInfo.webcamUrl;
                    md.source = spotInfo.sourceUrl;
                    md.webcamurl = spotInfo.webcamUrl;
                    String jsonText = mdList.get(i).toJson();
                    LOGGER.info("jsonText meteodata " + jsonText);

                    str += jsonText;
                    if (i != mdList.size() - 1)
                        str +=",";
                }
            }
            str += "] }";
            //response.setContentLength(str.length());
            response.setHeader("Length",""+str.length());
            out.println(str);


        } else if (history != null && history.equals("true") && spot != null) { // Historical data

            LOGGER.info("history");

            RequestLog req = new RequestLog();
            req.insert(authcode,"history",0,"");

            Date end = Core.getDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.add(Calendar.HOUR_OF_DAY, -6); //minus number would decrement the hours
            //Date start = cal.getTime();
            //MeteoStationData md = new MeteoStationData();
            //List<MeteoStationData> list = md.getHistory(Integer.valueOf(spot), start, end);
            List<MeteoStationData> list = Core.getHistory(Integer.valueOf(spot),200);

            LOGGER.info("md=" + list.toString());



            int length = 999;
            response.setHeader("Length",""+length);
            //response.setContentLength(length);
            out.println(" ");

            String str = "{\"meteodata\" : [";

            for (int i = 0; i < list.size(); i++) {
                //String jsonText = list.get(i).toJson();
                String jsonText = list.get(i).toSpeedHistoryJson();
                LOGGER.info("jsonText history" + jsonText);

                str += jsonText;
                if (i != list.size() - 1)
                    str += ",";

                out.print(str);
                str = "";
            }

            str += "] }";
            out.println(str);


        } else {
            out.println("no data");
        }
        out.close();

        RequestLog rl = new RequestLog();
        rl.insert(authcode,type,user,params);

    }
}
