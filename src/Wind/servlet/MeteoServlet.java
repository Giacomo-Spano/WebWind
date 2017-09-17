package Wind.servlet;

import Wind.Core;
import Wind.User;
import Wind.data.Favorites;
import Wind.data.Location;
import Wind.data.Locations;
import Wind.data.RequestLog;
import org.json.JSONObject;
import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.PullData;
import windalarm.meteodata.Spot;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
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

        String tokenid = request.getParameter("tokenid");
        User user = new User();
        if (!Core.validateTokenId(tokenid,user)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            return;
        }
        String deletekey = request.getParameter("remove");
        String reorder = request.getParameter("reorder");
        String order = request.getParameter("spotlist");

        LOGGER.info("doPost: postfavorite spotid="  + "userid=" + user.personid);

        if (deletekey != null && deletekey.equals("true")) { // elimina preferito

            String spotIdstr = request.getParameter("spotid");
            long spotid = Long.valueOf(spotIdstr);
            Favorites favorites = new Favorites();
            favorites.delete(user.personid, spotid);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");

        } else if (reorder != null && reorder.equals("true") && order != null) { // riordina preferiti

            Favorites favorites = new Favorites();
            List<Long> list = favorites.getFavorites(user.personid);
            for(Long id : list) {
                favorites.delete(user.personid, id);
            }
            String[] neworder = order.split(",");
            for(String spot : neworder) {
                favorites.insert(user.personid, Integer.valueOf(spot));
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");

        } else { // aggiungi preferito
            String spotIdstr = request.getParameter("spotid");
            long spotid = Long.valueOf(spotIdstr);
            Favorites favorites = new Favorites();
            favorites.insert(user.personid, spotid);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
    }



    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String tokenid = request.getParameter("tokenid");
        User user = new User();
        if (!Core.validateTokenId(tokenid,user)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            return;
        }

        String lastdata = request.getParameter("lastdata");
        String favoriteslastdata = request.getParameter("favoriteslastdata");
        String spotlist = request.getParameter("requestspotlist");
        String log = request.getParameter("log");
        String spot = request.getParameter("spot");
        String forecastlocations = request.getParameter("forecastlocations");
        String filter = request.getParameter("filter");
        String fullinfo = request.getParameter("fullinfo");
        String webcamimage = request.getParameter("webcamimage");
        String windid = request.getParameter("lastwindid");

        if (webcamimage != null && webcamimage.equals("1")) { // webcamimage

            RequestLog req = new RequestLog();
            req.insert("authcode", "webcamimage" + webcamimage, user.personid, "");

            getWebcamImage(response,webcamimage);
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (spotlist != null && spotlist.equals("true")) {

            RequestLog req = new RequestLog();
            req.insert("authcode", "spotlist", user.personid, "");

            boolean fullInfoRequest = false;
            if (fullinfo != null && fullinfo.equals("true"))
                fullInfoRequest = true;

            String str = getSpotListJson(fullInfoRequest, user.personid);
            out.print(str);

        } else if (forecastlocations != null && forecastlocations.equals("openweathermap")) {

            RequestLog req = new RequestLog();
            req.insert("authcode", "forecastspotlist", user.personid, "");

            if (filter == null) filter = "";

            String str = getForecastSpotListJson(user.personid, forecastlocations, filter);
            out.print(str);

        } else if (lastdata != null && lastdata.equals("true")) { // Last meteo data

            RequestLog req = new RequestLog();
            req.insert("authcode", "lastdata", user.personid, "");

            String str = getLastData(spot);
            //response.setHeader("Length", "" + str.length());
            out.println(str);

        } else if (favoriteslastdata != null && favoriteslastdata.equals("true") && user.personid != null) { // Last meteo data

            RequestLog req = new RequestLog();
            req.insert("authcode", "lastdata", user.personid, "");

            long lastWindId = 0l;
            if (windid != null)
                lastWindId = Long.valueOf(windid);
            String str = getFavoritesLastData(user.personid,lastWindId);
            out.println(str);

        } else if (log != null && log.equals("true") && spot != null) { // Historical data

            LOGGER.info("log REQUEST");
            String strStartDate = request.getParameter("start");
            String strEndDate = request.getParameter("end");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
            String strLastWindId = request.getParameter("lastwindid");
            long lastWindId = 0l;
            if (strLastWindId != null)
                lastWindId = Long.valueOf(strLastWindId);

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

            String str = getLogJson(spot, start, end, lastWindId,10);
            out.print(str);

        } else {
            out.println("no data");
        }
        out.close();

        //RequestLog rl = new RequestLog();
        //rl.insert(authcode, type, user, params);

    }

    private void getWebcamImage(HttpServletResponse response, String webcamimage) {

        response.setContentType("image/jpeg");

        String pathToWeb = Core.getDataDir() + "/" + "webcam.jpeg";
        File f = new File(pathToWeb);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(f);
            OutputStream out = response.getOutputStream();
            ImageIO.write(bi, "jpg", out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLogJson(String spot, Date start, Date end, long lastWindId, int maxpoint) {
        List<MeteoStationData> list = Core.getHistory(Integer.valueOf(spot), start, end, lastWindId, maxpoint);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");

        String str = "\n{";

        String date = "";
        String speed = "";
        String avspeed = "";
        String direction = "";
        String trend = "";
        String temperature = "";
        String id = "";
        int count = 0;

        for (MeteoStationData element : list) {

            if (count++ != 0) {
                date += ";";
                speed += ";";
                avspeed += ";";
                direction += ";";
                trend += ";";
                temperature += ";";
                id += ";";
            }

            date += df.format(element.datetime);
            speed += element.speed;
            avspeed += element.averagespeed;
            direction += element.directionangle;
            if (element.trend == null) // TODO non so perchè in qualche caso è null
                element.trend = 0.0;
            trend += element.trend;
            temperature += element.temperature;
            id += element.id;
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

        str += ",";
        str += "\"id\" : \"";
        str += id;
        str += "\"";

        str += "}";
        return str;
    }

    private String getLastData(String spotList) {
        ArrayList<MeteoStationData> mdList = new ArrayList<MeteoStationData>();

        if (spotList == null) {

            ArrayList<PullData> sl = Core.getSpotList();
            for (int i = 0; i < sl.size(); i++) {
                MeteoStationData md = Core.getLastMeteoData(sl.get(i).getSpotId());
                if (md != null)
                    mdList.add(md);
            }
        } else {

            String[] l = spotList.split(",");
            if (l != null) {
                for (int i = 0; i < l.length; i++) {
                    int id = Integer.valueOf(l[i]);
                    MeteoStationData md = Core.getLastMeteoData(id);
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

    private String getFavoritesLastData(String personid, long lastWindId) {

        if (personid == null) return null;

        List<MeteoStationData> list;
        list = Core.getLastFavorites(personid);

        String str = "{\"meteodata\" : [";

        int count = 0;
        for (MeteoStationData md : list) {

            if (count++ != 0)
                str += ",";
            String jsonText = md.toJson();
            str += jsonText;
        }
        str += "] }";
        return str;
    }

    private String getForecastSpotListJson(String userid, String source, String filter) {

        Locations locations = new Locations();
        List<Location> list = locations.getLocations(source, filter);

        String str = "{\"locations\" : [";

        int count = 0;
        for (Location l : list) {

            if (count++ != 0)
                str += ",";

            str += "{\"name\" : \"" + l.name + "\",";
            str += "\"id\" : " + "\"" + l.id + "\",";
            str += "\"lat\" : " + "\"" + l.lat + "\",";
            str += "\"lon\" : " + "\"" + l.lon + "\",";
            str += "\"countrycode\" : " + "\"" + l.countryCode + "\"";

            str += " }";
        }
        str += "] ";

        str += " }";
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
                MeteoStationData meteoStationData = Core.getLastMeteoData(spot.getSpotId());
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
