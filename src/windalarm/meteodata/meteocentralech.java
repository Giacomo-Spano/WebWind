package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

import Wind.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class meteocentralech {


    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static final String LUGANO = "svizzera/meteo-lugano/details/S067700";
    public static final String ZURIGO = "svizzera/meteo-zurigo/details/N-3518042";
    public static final String VALMADRERA = "italia/meteo-valmadrera/details/N-200564";

    public ArrayList<CHMeteoForecast> getMeteoData(String path) {

        String htmlResultString = "";
        MeteoStationData meteoStationData = new MeteoStationData();

        ArrayList<CHMeteoForecast> fList = new ArrayList<CHMeteoForecast>();

        String address = "http://www.meteocentrale.ch/it/europa/@path@/";
        //                                        italia/meteo-valmadrera/details/N-200564/
        //"http://www.meteocentrale.ch/it/europa/svizzera/meteo-lugano/details/S067700/"; lugano
        //http://www.meteocentrale.ch/it/europa/svizzera/meteo-zurigo/details/N-3518042/ zurigo

        try {
            address = address.replace("@path@", path);
            URL jsonurl = new URL(address);

            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(false);
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestMethod("GET");
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            BufferedReader rd = new BufferedReader(reader);
            String line = "";
            htmlResultString = "";
            while ((line = rd.readLine()) != null) {
                htmlResultString += line;
            }
            reader.close();

            int res = connection.getResponseCode();
            if (res == HttpURLConnection.HTTP_OK) {
                // OK
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
                Calendar cal = Calendar.getInstance();
                meteoStationData.sampledatetime = Core.getDate();
                LOGGER.info("time in rome=" + meteoStationData.sampledatetime);
                LOGGER.info("hour in rome=" + cal.get(Calendar.HOUR_OF_DAY));

                String txt1 = htmlResultString;
                String keyword1 = "";

                for (int k = 0; k < CHMeteoForecast.NDAYS; k++) {

                    CHMeteoForecast f = new CHMeteoForecast();

                    keyword1 = "<table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" class=\"detail-table\" id=\"detail-table-\">" + k;
                    int start = txt1.indexOf(keyword1);
                    txt1 = txt1.substring(start + keyword1.length());

                    String keyword2 = "";

                    for (int i = 0; i < CHMeteoForecast.NRANGES; i++) {

                        f.date[i] = dateFormat.format(cal.getTime());

                        keyword2 = "<td class=\"time\"><div>";
                        start = txt1.indexOf(keyword2);
                        txt1 = txt1.substring(start + keyword2.length());
                        keyword2 = "</div>";
                        String txt2 = txt1.substring(0, txt1.indexOf(keyword2));
                        f.time[i] = txt2;
                    }
                    for (int i = 0; i < CHMeteoForecast.NRANGES; i++) {

                        keyword2 = "<td title=\"Temperatura\">";
                        start = txt1.indexOf(keyword2);
                        txt1 = txt1.substring(start + keyword2.length());
                        keyword2 = "C</td>";
                        String txt2 = txt1.substring(0, txt1.indexOf(keyword2) - 2);
                        f.temperature[i] = Double.valueOf(txt2.trim());
                    }
                    for (int i = 0; i < CHMeteoForecast.NRANGES; i++) {

                        keyword2 = "<td title=\"Raffiche\">";
                        start = txt1.indexOf(keyword2);
                        txt1 = txt1.substring(start + keyword2.length());
                        keyword2 = "km/h</td>";
                        String txt2 = txt1.substring(0, txt1.indexOf(keyword2));
                        f.windmax[i] = Double.valueOf(txt2.trim());
                    }
                    for (int i = 0; i < CHMeteoForecast.NRANGES; i++) {

                        keyword2 = "<div class=\"mm_detail_";
                        start = txt1.indexOf(keyword2);
                        txt1 = txt1.substring(start + keyword2.length());
                        keyword2 = "\" title=\"vento";
                        String txt2 = txt1.substring(0, txt1.indexOf(keyword2));
                        f.direction[i] = txt2;
                    }
                    for (int i = 0; i < CHMeteoForecast.NRANGES; i++) {

                        keyword2 = "<td title=\"Vento medio\">";
                        start = txt1.indexOf(keyword2);
                        txt1 = txt1.substring(start + keyword2.length());
                        keyword2 = "km/h</td>";
                        String txt2 = txt1.substring(0, txt1.indexOf(keyword2));
                        f.wind[i] = Double.valueOf(txt2.trim());
                    }
                    for (int i = 0; i < CHMeteoForecast.NRANGES; i++) {

                        keyword2 = " relativa\">";
                        start = txt1.indexOf(keyword2);
                        txt1 = txt1.substring(start + keyword2.length());
                        keyword2 = "%</td>";
                        String txt2 = txt1.substring(0, txt1.indexOf(keyword2));
                        f.humidity[i] = Double.valueOf(txt2.trim());
                    }
                    for (int i = 0; i < CHMeteoForecast.NRANGES; i++) {

                        keyword2 = "<td title=\"Pressione atmosferica\">";
                        start = txt1.indexOf(keyword2);
                        if (start == -1) {
                            f.pressure[i] = 0.0;
                            continue;
                        }
                        txt1 = txt1.substring(start + keyword2.length());
                        keyword2 = "hPa</td>";
                        String txt2 = txt1.substring(0, txt1.indexOf(keyword2));
                        f.pressure[i] = Double.valueOf(txt2);

                    }

                    fList.add(f);
                    cal.add(Calendar.DAY_OF_YEAR, 1/*daysToAdd*/);

                }

            } else {
                // Server returned HTTP error code.
                LOGGER.info("Server returned HTTP error code" + res);
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return fList;
    }
}
