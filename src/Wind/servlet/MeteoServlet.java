package Wind.servlet;

import Wind.Core;
import Wind.Favorites;
import Wind.RequestLog;
import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.PullData;
import windalarm.meteodata.Spot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by giacomo on 05/07/2015.
 */
public class MeteoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MeteoServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String spotIdstr = request.getParameter("spotid");
        long spotid = Long.valueOf(spotIdstr);
        String userid = request.getParameter("userid");
        String deletekey = request.getParameter("remove");

        LOGGER.info("doPost: postfavorite spotid=" + spotid + "userid=" + userid);

        if (deletekey != null && deletekey.equals("true")) {

            Favorites favorites = new Favorites();
            favorites.delete(userid, spotid);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");

        } else {

            Favorites favorites = new Favorites();
            favorites.insert(userid, spotid);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String lastdata = request.getParameter("lastdata");
        String spotlist = request.getParameter("requestspotlist");
        String history = request.getParameter("history");
        String log = request.getParameter("log");
        String spot = request.getParameter("spot");
        String fullinfo = request.getParameter("fullinfo");
        String userid = request.getParameter("userid");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (spotlist != null && spotlist.equals("true")) {

            RequestLog req = new RequestLog();
            req.insert("authcode", "spotlist", userid, "");

            boolean fullInfoRequest = false;
            if (fullinfo != null && fullinfo.equals("true"))
                fullInfoRequest = true;

            String str = getSpotListJson(fullInfoRequest, userid);
            out.print(str);

        } else if (lastdata != null && lastdata.equals("true")) { // Last meteo data

            RequestLog req = new RequestLog();
            req.insert("authcode", "lastdata", userid, "");

            String str = getLastData(spot);
            //response.setHeader("Length", "" + str.length());
            out.println(str);

        } else if (log != null && log.equals("true") && spot != null) { // Historical data

            LOGGER.info("log REQUEST");
            String strStartDate = request.getParameter("start");
            String strEndDate = request.getParameter("end");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");

            Date start = null, end = null;
            try {
                if (strStartDate != null)
                    start = df.parse(strStartDate);
                if (strEndDate != null)
                    end = df.parse(strEndDate);
            } catch (ParseException e) {
                e.printStackTrace();
                out.println("error");
                out.close();
            }
            if (start == null || end == null) {
                out.println("error");
                out.close();
            }

            String str = getLogJson(spot, start, end);
            out.print(str);

        } else {
            out.println("no data");
        }
        out.close();

        //RequestLog rl = new RequestLog();
        //rl.insert(authcode, type, user, params);

    }

    private String getLogJson(String spot, Date start, Date end) {
        List<MeteoStationData> list = Core.getHistory(Integer.valueOf(spot), start, end);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");

        String str = "\n{";

        String date = "";
        String speed = "";
        String avspeed = "";
        String direction = "";
        String trend = "";
        String temperature = "";
        int count = 0;

        for (MeteoStationData element : list) {

            if (count++ != 0) {
                date += ";";
                speed += ";";
                avspeed += ";";
                direction += ";";
                trend += ";";
                temperature += ";";
            }

            date += df.format(element.datetime);
            speed += element.speed;
            avspeed += element.averagespeed;
            direction += element.directionangle;
            if (element.trend == null) // TODO non so perchè in qualche caso è null
                element.trend = 0.0;
            trend += element.trend;
            temperature += element.temperature;
        }
        str += "\"date\" : \"";
        str += date;
        str += "\"";

        str += ",";
        str += "\"speed\" : \"";
        str += speed;
        str += "\"";

        str += ",";
        str += "\"avspeed\" : \"";
        str += avspeed;
        str += "\"";

        str += ",";
        str += "\"trend\" : \"";
        str += trend;
        str += "\"";

        str += ",";
        str += "\"direction\" : \"";
        str += direction;
        str += "\"";

        str += ",";
        str += "\"temperature\" : \"";
        str += temperature;
        str += "\"";

        str += "}";
        return str;
    }

    private String getLastData(String spotList) {
        ArrayList<MeteoStationData> mdList = new ArrayList<MeteoStationData>();

        if (spotList == null) {

            ArrayList<PullData> sl = Core.getSpotList();
            for (int i = 0; i < sl.size(); i++) {
                MeteoStationData md = Core.getLastfromID(sl.get(i).getSpotId());
                if (md != null)
                    mdList.add(md);
            }
        } else {

            String[] l = spotList.split(",");
            if (l != null) {
                for (int i = 0; i < l.length; i++) {
                    int id = Integer.valueOf(l[i]);
                    MeteoStationData md = Core.getLastfromID(id);
                    if (md != null)
                        mdList.add(md);
                }
            }
        }
        String str = "{\"meteodata\" : [";

        if (mdList != null) {
            LOGGER.info("md=" + mdList.toString());

            for (int i = 0; i < mdList.size(); i++) {

                MeteoStationData md = mdList.get(i);
                Spot spotInfo = Core.getSpotFromID(md.spotID);
                md.spotName = spotInfo.getName();
                md.source = spotInfo.getSourceUrl();
                md.webcamurl = spotInfo.getWebcamUrl(1);
                md.webcamurl2 = spotInfo.getWebcamUrl(2);
                md.webcamurl3 = spotInfo.getWebcamUrl(3);
                String jsonText = mdList.get(i).toJson();
                LOGGER.info("jsonText meteodata " + jsonText);

                str += jsonText;
                if (i != mdList.size() - 1)
                    str += ",";
            }
        }
        str += "] }";
        return str;
    }

    private String getSpotListJson(boolean fullInfoRequest, String userid) {
        String str = "{\"spotlist\" : [";

        List<PullData> sl = Core.getSpotList();

        int count = 0;
        for (Spot spot : sl) {

            if (count++ != 0)
                str += ",";

            str += "{\"spotname\" : \"" + spot.getName() + "\",";
            str += "\"sourceurl\" : " + "\"" + spot.getSourceUrl() + "\",";
            str += "\"id\" : " + "\"" + spot.getSpotId() + "\",";
            str += "\"webcamurl\" : " + "\"" + spot.getWebcamUrl(1) + "\",";
            str += "\"webcamurl2\" : " + "\"" + spot.getWebcamUrl(2) + "\",";
            str += "\"webcamurl3\" : " + "\"" + spot.getWebcamUrl(3) + "\"";

            if (fullInfoRequest) {
                MeteoStationData meteoStationData = Core.getLastfromID(spot.getSpotId());
                if (meteoStationData != null) {
                    str += ",";
                    SimpleDateFormat df = new SimpleDateFormat("DD/MM/YYY HH:mm:ss");
                    str += "\"speed\" : " + meteoStationData.speed + ",";
                    str += "\"avspeed\" : " + meteoStationData.averagespeed + ",";
                    str += "\"direction\" : " + "\"" + meteoStationData.direction + "\",";
                    str += "\"directionangle\" : " + meteoStationData.directionangle + ",";
                    str += "\"datetime\" : " + "\"" + meteoStationData.sampledatetime + "\"";
                }
            }
            str += "}";
        }
        str += "] ";

        Favorites f = new Favorites();
        List<Long> favorites = f.getFavorites(userid);

        str += ", \"favorites\" : \"";
        if (favorites != null) {
            count = 0;
            for (Long spot : favorites) {
                if (count++ != 0)
                    str += ",";
                str += spot;
            }
        }
        str += "\" ";

        str += " }";
        return str;
    }
}
