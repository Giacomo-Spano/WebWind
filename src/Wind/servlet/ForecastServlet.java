package Wind.servlet;

import Wind.data.WindForecastDataSource;
import Wind.data.RequestLog;
import windalarm.meteodata.WindForecast;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giacomo on 05/07/2015.
 */
public class ForecastServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ForecastServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String spot = request.getParameter("spot");
        String userid = request.getParameter("userid");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (spot != null) {

            RequestLog req = new RequestLog();
            req.insert("authcode", "forecast", userid, "");

            long spotId = Long.valueOf(spot);


            String str = getForecastJson(spotId);
            out.print(str);

        } else {
            out.println("no data");
        }
        out.close();
    }

    private String getForecastJson(long spotId) {


        WindForecastDataSource forecastDBData = new WindForecastDataSource();

        WindForecast f = forecastDBData.getForecast(spotId);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String str = "{ ";

        str += "\"datetimes\" : [";
        int count = 1;
        for (Date d : f.datetimes) {
            if (count++ > 1)
                str += ",";
            str += df.format(d);
        }
        str += "],";

        str += "\"speeds\" : [";
        count = 1;
        for (Double h : f.speeds) {
            if (count++ > 1)
                str += ",";
            str += h;
        }
        str += "],";

        str += "\"directions\" : [";
        count = 1;
        for (Double h : f.speedDirs) {
            if (count++ > 1)
                str += ",";
            str += h;
        }
        str += "],";

        str += "\"temperatures\" : [";
        count = 1;
        for (Double h : f.temperatures) {
            if (count++ > 1)
                str += ",";
            str += h;
        }
        str += "]";

        str += "}";

        return str;
    }
}
