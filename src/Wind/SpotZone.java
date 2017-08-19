package Wind;

import windalarm.meteodata.Spot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giaco on 17/08/2017.
 */
public class SpotZone {

    public String name;
    public int id;
    public int father;
    public List<Spot> spotlist = new ArrayList<>();
}
