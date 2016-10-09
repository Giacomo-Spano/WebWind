package Wind.data;

import java.util.Date;
import java.util.logging.Logger;

public class Location {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public String id;
    public String name;
    public double lat;
    public double lon;
    public String countryCode;
    public String country;

    public Location() {

    }
}
