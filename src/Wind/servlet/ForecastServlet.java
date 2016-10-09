package Wind.servlet;

import Wind.data.WindForecastDataSource;
import Wind.data.RequestLog;
import windalarm.meteodata.OpenWeatherForecast;
import windalarm.meteodata.PullData;
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
        String forecastSource = request.getParameter("source");
        String city = request.getParameter("location");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (spot != null && forecastSource != null &&
                (forecastSource.equals(PullData.FORECAST_OPENWEATHERMAP) ||
                forecastSource.equals(PullData.FORECAST_WINDFINDER) ||
                forecastSource.equals(PullData.FORECAST_WINDGURU))) {

            RequestLog req = new RequestLog();
            req.insert("authcode", "forecast", userid, "");

            long spotId = Long.valueOf(spot);

            String str = getForecastJson(spotId, forecastSource);

            if (str == null) { //
                OpenWeatherForecast owf = new OpenWeatherForecast();
                WindForecast wf = owf.getMeteoForecastByCityId(""+spotId,/*"uk"*/null);
                str = wf.toJson();
            }

            out.print(str);

        } else if (city != null) {

            RequestLog req = new RequestLog();
            req.insert("authcode", "forecast", userid, "");

            OpenWeatherForecast owf = new OpenWeatherForecast();
            WindForecast wf = owf.getMeteoForecastByCityName(city,/*"uk"*/null);

            String str = wf.toJson();
            out.print(str);

        } else {

            out.println("no data");
        }
        out.close();
    }

    private String getForecastJson(long spotId, String source) {

        WindForecastDataSource forecastDBData = new WindForecastDataSource();
        WindForecast f = forecastDBData.getForecast(spotId,source);
        if (f == null) return null;

        String str = f.toJson();

        return str;
    }
}
