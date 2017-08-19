package windalarm.meteodata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Class InterpolationSearch **/
public class InterpolationSearch
{
    /** interpolationSearch function **/
    public MeteoStationData linearInterpolation(List<MeteoStationData> sortedArray, Date datetime)
    {
        int low = 0;
        int high = sortedArray.size() - 1;

        MeteoStationData p0 = sortedArray.get(low);
        MeteoStationData p1 = sortedArray.get(high);
        if (p0.datetime.getTime() > datetime.getTime())
            return p0;
        if (p1.datetime.getTime() < datetime.getTime())
            return p1;
        if (p0.datetime.getTime() == p1.datetime.getTime())
            return p0;

        int lcursor = low;
        while (sortedArray.get(lcursor).datetime.getTime() <= datetime.getTime() && lcursor <= high)
        {
            p0 = sortedArray.get(lcursor);
            lcursor++;
        }
        int hcursor = high;
        while (sortedArray.get(hcursor).datetime.getTime() >= datetime.getTime() && hcursor >= low)
        {
            p1 = sortedArray.get(hcursor);
            hcursor--;
        }

        if (p1.datetime.getTime() - p0.datetime.getTime() == 0)
            return p0;

        MeteoStationData md = new MeteoStationData();
        double val = p0.speed + (datetime.getTime() - p0.datetime.getTime()) * (p1.speed - p0.speed) / (p1.datetime.getTime() - p0.datetime.getTime());
        md.speed = round(val);

        val = p0.averagespeed + (datetime.getTime() - p0.datetime.getTime()) * (p1.averagespeed - p0.averagespeed) / (p1.datetime.getTime() - p0.datetime.getTime());
        md.averagespeed = round(val);

        val = p0.temperature + (datetime.getTime() - p0.datetime.getTime()) * (p1.temperature - p0.temperature) / (p1.datetime.getTime() - p0.datetime.getTime());
        md.temperature = round(val);

        val = p0.directionangle + (datetime.getTime() - p0.datetime.getTime()) * (p1.directionangle - p0.directionangle) / (p1.datetime.getTime() - p0.datetime.getTime());
        md.directionangle = round(val);
        //md.direction = md.di

        val = p0.trend + (datetime.getTime() - p0.datetime.getTime()) * (p1.trend - p0.trend) / (p1.datetime.getTime() - p0.datetime.getTime());
        md.trend = round(val);

        md.datetime = datetime;
        return md;
    }

    public static double round(double val)
    {
        return Math.floor(val * 100) / 100;
    }

    public List<MeteoStationData> getInterpolatedArray(List<MeteoStationData> sortedArray, Date startDate, Date endDate, int points) {

        long start = startDate.getTime();
        long end = endDate.getTime();
        if (start > end) return null;
        if (points <= 0) return null;
        if (sortedArray == null || sortedArray.size() == 0) return null;
        int step = (int) (end - start) / (points - 1) / 1000 / 60;

        List<MeteoStationData> interpolaterdArray = new ArrayList<>();
        Date current = startDate;
        while (current.getTime() <= endDate.getTime()) {
            MeteoStationData p = linearInterpolation(sortedArray,current);
            interpolaterdArray.add(p);
            Calendar cal = Calendar.getInstance();
            cal.setTime(current);
            cal.add(Calendar.MINUTE,step);
            current = cal.getTime();
        }
        return interpolaterdArray;
    }
}