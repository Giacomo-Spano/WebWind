package windalarm.meteodata;


import Wind.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Logger;

public class WCV extends PullData {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public WCV() {
        super();
    }


    public MeteoStationData getMeteoData() {

        LOGGER.info("getMeteoData: spotName=" + name);

        MeteoStationData md = new MeteoStationData();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        md.sampledatetime = Core.getDate();//dateFormat.format(cal.getTime());

        SimpleDateFormat todayFormat = new SimpleDateFormat("yyyyMMdd");
        String htmlResultString = getHTMLPage(meteodataUrl+todayFormat.format(md.sampledatetime) + ".txt",true);
        if (htmlResultString == null)
            return null;

        String line = "";
        Scanner scanner = new Scanner(htmlResultString);
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
        }
        scanner.close();
        if (line != null && !line.equals("")) {
            // process the line
            String[] fields = line.split(",");
            int id = Integer.parseInt(fields[0]);
            md.speed = Double.parseDouble(fields[1]);
            md.averagespeed = Double.parseDouble(fields[2]);
            //md.directionangle = Double.parseDouble(fields[3]);
            md.direction = fields[4];
            md.directionangle = md.getAngleFromDirectionSymbol(md.direction);
            String time = fields[5];
            md.temperature = Double.parseDouble(fields[6]);
            md.rainrate = Double.parseDouble(fields[7]);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            try {
                md.datetime = md.sampledatetime;
                Date date = timeFormat.parse(time);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY,cal2.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE,cal2.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND,0);
                md.datetime = cal.getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            MeteoStationData lastMeteoData = Core.getLastMeteoData(getSpotId());

            if (lastMeteoData != null) {

                String lastTime = timeFormat.format(lastMeteoData.datetime);
                if (lastTime.equals(time))
                    return null;
            }

        }


       long difference = md.datetime.getTime() - md.sampledatetime.getTime();
        if (difference / 1000 / 60 > 60)
           offline = true;
        else
           offline = false;

        return md;
    }
}
