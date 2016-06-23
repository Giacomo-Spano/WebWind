package windalarm.meteodata;

/**
 * Created by giacomo on 19/07/2015.
 */
public class Forecast {

    public static int NDAYS = 5;
    public static int NRANGES = 7;
    String[] time = new String[NRANGES];
    Double[] temperature = new Double[NRANGES];
    Double[] windmax = new Double[NRANGES];
    String[] direction = new String[NRANGES];
    Double[] wind = new Double[NRANGES];
    Double[] humidity = new Double[NRANGES];
    Double[] pressure = new Double[NRANGES];
    String[] date = new String[NRANGES];
    //String source = "";

    public String toJson() {

        String json = "";

            for (int i = 0; i < NRANGES; i++) {

                if ( i!= 0)
                    json += ",";
                json += "%7B";
                json += "%22date%22:%22" + date[i] + "%22";
                json += ",%22time%22:%22" + time[i] + "%22";
                json += ",%22temperature%22:%22" + temperature[i] + "%22";
                json += ",%22windmax%22:%22" + windmax[i] + "%22";
                json += ",%22direction%22:%22" + direction[i] + "%22";
                json += ",%22wind%22:%22" + wind[i] + "%22";
                json += ",%22humidity%22:%22" + humidity[i] + "%22";
                json += ",%22pressure%22:%22" + pressure[i] + "%22";
                json += "%7D";

            }
        return json;
    }
}
