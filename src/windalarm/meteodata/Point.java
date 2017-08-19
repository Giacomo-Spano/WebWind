package windalarm.meteodata;

/**
 * Created by giaco on 08/08/2017.
 */
public class Point {

    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }


    @Override
    public String toString() {
        return ("(" + x + "," + y + ")");
    }
}