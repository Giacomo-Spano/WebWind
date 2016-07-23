package windalarm.meteodata;

//import com.google.appengine.repackaged.org.joda.time.DateTimeZone;

//import Wind.HomeServlet;

import Wind.AlarmModel;
import Wind.Core;
import sun.awt.image.ToolkitImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

public class CML extends PullData {

    public static final String Dervio = "Lecco/dervio";
    public static final String Gera = "Como/geralario";
    public static final String Abbadia = "Lecco/abbadia";
    public static final String Dongo = "Como/dongo";
    public static final String Gravedona = "Como/gravedona";

    protected String mSpotUrl;

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public CML(int spotID) {
        super(spotID);

        switch (mSpotID) {
            case AlarmModel.Spot_Abbadia:
                mSpotUrl = Abbadia;
                mWebcamUrl = "http://www.abbadiameteo.it/foscam/FI9805W_00626E4EA7A8/snap/webcam.php";
                mName = "Abbadia Lariana (Lecco)";
                break;
            case AlarmModel.Spot_Gera:
                mSpotUrl = Gera;
                mWebcamUrl = "http://www.solemio.nl/solemio.jpg";
                mName = "Gera Lario (Como)";
                break;
            case AlarmModel.Spot_Dervio:
                mSpotUrl = Dervio;
                mWebcamUrl = "http://www.wcv.it/webcam03/currenth.jpg";
                mName = "Dervio (Lecco)";
                break;
            case AlarmModel.Spot_Dongo:
                mSpotUrl = Dongo;
                mWebcamUrl = "http://www.skiffsailing.it/webcam/video.jpg";
                mName = "Dongo (Como)";
                break;
            case AlarmModel.Spot_Gravedona:
                mSpotUrl = Gravedona;
                mWebcamUrl = "http://rete.centrometeolombardo.com/Como/gravedona/public/gravedona.jpg";
                mName = "Gravedona (Como)";
                break;

        }

        mImageName = "spot-" + mSpotID + ".jpg";
    }

    @Override
    MeteoStationData getMeteoData() {

        LOGGER.info("getMeteoData: spotName=" + mName);

        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        meteoStationData.sampledatetime = Core.getDate();
        meteoStationData.datetime = meteoStationData.sampledatetime;

        MeteoStationData lastMd = new MeteoStationData();
        lastMd = lastMd.getLastMeteoStationData(mSpotID);

        if (lastMd != null) {
            long minutes = (meteoStationData.sampledatetime.getTime() - lastMd.sampledatetime.getTime());
            minutes /= 1000;
            minutes /= 60;
            if(minutes < 5 ) {
                LOGGER.info("CML last item less then 5  minutes difference: skip ");
                return null;
            }
        }

        String address = "http://rete.centrometeolombardo.com/@spot@/immagini/v.png";
        address = address.replace("@spot@", mSpotUrl);
        String value = "";
        value = getTextFromImage(address);
        LOGGER.info("value=" + value);

        char character = 176;// �
        String tt = new String(character + "c");
        value = value.replace(tt, "C");
        value = value.replace("ob","%");
        value = value.replace("°b","%");

        String keyword = new String("C");
        int end = value.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(value + " not found " + keyword);
        String str = value.substring(0,end);
        meteoStationData.temperature = Double.valueOf(str);
        value = value.substring(end+keyword.length());

        keyword = "%";
        end = value.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(value + " not found " + keyword);
        str = value.substring(0,end);
        meteoStationData.humidity = Double.valueOf(str);
        value = value.substring(end+keyword.length());

        keyword = new String("C");
        end = value.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(value + " not found " + keyword);
        str = value.substring(0,end);
        value = value.substring(end+keyword.length());

        keyword = "km/h";
        end = value.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(value + " not found " + keyword);
        str = value.substring(0,end);
        meteoStationData.speed = Double.valueOf(str);
        value = value.substring(end+keyword.length());

        int i = 0;
        while (i < value.length() && !Character.isDigit(value.charAt(i))) i++;
        str = value.substring(0,i);
        meteoStationData.direction = str;
        meteoStationData.direction = meteoStationData.direction.toUpperCase();
        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);
        value = value.substring(i);

        keyword = "hPa";
        end = value.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(value + " not found " + keyword);
        str = value.substring(0,end);
        meteoStationData.pressure = Double.valueOf(str);
        value = value.substring(end+keyword.length());

        keyword = "mm";
        end = value.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(value + " not found " + keyword);
        str = value.substring(0,end);
        value = value.substring(end+keyword.length());

        keyword = "mm/h";
        end = value.indexOf(keyword);
        if (end == -1)
            LOGGER.severe(value + " not found " + keyword);
        str = value.substring(0,end);
        meteoStationData.rainrate = Double.valueOf(str);
        value = value.substring(end+keyword.length());

        meteoStationData.spotName = mName;

        LOGGER.info("meteoData=" + meteoStationData.toString());

        return meteoStationData;
    }


    public static String getTextFromImage(String imagepath) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "----WebKitFormBoundaryUxfW8zs5hNhHkS0V";
        String urlString = "http://ocr1.sc.isc.tohoku.ac.jp/cgi-bin/weocr/submit_e1.cgi";
        String reponse_data = "";
        try {
            // get the image and transform to jpg
            URL imageURL = new URL(imagepath);

            //----
            BufferedImage bufferedImage;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] newImageData;

            try {

                URL imageUrl = new URL(imagepath);
                System.setProperty("java.io.tmpdir", Core.getTmpDir());
                bufferedImage = ImageIO.read(imageUrl);

                // create a blank, RGB, same width and height, and a white background
                BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                        bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

                // write to jpeg file
                ImageIO.write(newBufferedImage, "jpg", baos);
                baos.flush();
                newImageData = baos.toByteArray();
                baos.close();

                System.out.println("Done");


                //--

                // prepare header
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();

                //conn.setReadTimeout(20000); //10 Sec
                //conn.setConnectTimeout(60000);  //60 Seconds
                //conn.setReadTimeout(60000);  //60 Seconds

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                String headerbytes = "";
                headerbytes += twoHyphens + boundary + lineEnd;
                headerbytes += "Content-Disposition: form-data; name=\"userfile\"; filename=\"prova.jpg\"" + lineEnd;
                headerbytes += "Content-Type: image/jpeg" + lineEnd + lineEnd;
                // prepare footer
                String footerbytes = "";
                footerbytes += lineEnd + twoHyphens + boundary + twoHyphens + lineEnd + lineEnd;
                int len = newImageData.length/*bytesAvailable*/ + headerbytes.length() + footerbytes.length();
                conn.setRequestProperty("Content-Length", "" + len);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(headerbytes);
                // write the image in the body
                dos.write(newImageData, 0, newImageData.length);
                // write footer
                dos.writeBytes(footerbytes);
                dos.flush();
                dos.close();

                //------------------ read the SERVER RESPONSE
                inStream = new DataInputStream(conn.getInputStream());
                //String reponse_data = "";
                String str;
                while ((str = inStream.readLine()) != null) {
                    reponse_data += str;
                }
                inStream.close();
            } catch (IOException e) {

                e.printStackTrace();

            }

        } catch (MalformedURLException ex) {
            LOGGER.severe("Debug - MalformedURLException error: " + ex.toString());
            return "0";
        } catch (IOException ioe) {
            LOGGER.severe("Debug - IOException error: " + ioe.toString());
            return "0";
        }
        //LOGGER.info("reponse_data=" + reponse_data);
        String tag = "Recognition result:<br><hr><pre>";
        int start = reponse_data.indexOf(tag);
        if (start == -1) {
            LOGGER.info("DEBUG - cannot find result");
            return "0";
        }
        String result = reponse_data.substring(start + tag.length());
        tag = "</pre><hr>";
        int end = result.indexOf(tag);
        result = result.substring(0, end);
        result = result.trim();
        result = result.replaceAll(" ", ""); // carattere strano
        result = result.replaceAll("ob", "%");
        result = result.replaceAll("o", "0");
        result = result.replaceAll("O", "0");
        result = result.replaceAll("g", "9");
        result = result.replaceAll("_", "");

        return result;
    }


}
