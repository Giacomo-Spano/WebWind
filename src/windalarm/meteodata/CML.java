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
import java.util.TimeZone;
import java.util.logging.Logger;

public class CML extends PullData {

    public static final String Dervio = "Lecco/dervio";
    public static final String Gera = "Como/geralario";
    public static final String Abbadia = "Lecco/abbadia";
    public static final String Dongo = "Como/dongo";

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
        }

        mImageName = "spot-" + mSpotID + ".jpg";
    }

    @Override
    MeteoStationData getMeteoData() {
        //return null;
        //}

        //public MeteoStationData getMeteoData(String name, String spot) {

        String htmlResultString = "";
        MeteoStationData meteoStationData = new MeteoStationData();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Calendar cal = Calendar.getInstance();
        meteoStationData.sampledatetime = Core.getDate();
        meteoStationData.datetime = meteoStationData.sampledatetime;
        LOGGER.info("xtime in rome=" + meteoStationData.sampledatetime);
        LOGGER.info("xhour in rome=" + cal.get(Calendar.HOUR_OF_DAY));

        String address = "http://rete.centrometeolombardo.com/@spot@/immagini/@image@.png";
        // http://www.centrometeolombardo.com/content.asp?contentid=6228&ContentType=Stazioni

        //<img src="http://rete.centrometeolombardo.com/Lecco/abbadia/immagini/4.png" width="338" height="40">


        address = address.replace("@spot@", mSpotUrl);
        LOGGER.info("xaddress=" + address);
        String value = "";

        String image = address.replace("@image@", "4");
        LOGGER.info("ximage=" + image);
        value = getTextFromImage(image);
        LOGGER.info("value" + value);
        String[] split = value.split("km/h");
        meteoStationData.speed = Double.valueOf(split[0]);
        meteoStationData.direction = split[1];
        meteoStationData.direction = meteoStationData.direction.toUpperCase();

        meteoStationData.directionangle = meteoStationData.getAngleFromDirectionSymbol(meteoStationData.direction);

        image = address.replace("@image@", "1");
        value = getTextFromImage(image);

        char character = 176;// �
        String tt = new String(character + "c");
        value = value.replace(tt, "");
        LOGGER.info("temoperature" + value);
        meteoStationData.temperature = Double.valueOf(value);

        image = address.replace("@image@", "2");
        value = getTextFromImage(image);
        //value = value.replace("ob","");
        value = value.replace("%", "");
        //value = value.replace(" ",""); // carattere strano
        LOGGER.info("humidity" + value);
        meteoStationData.humidity = Double.valueOf(value);

        image = address.replace("@image@", "5");
        value = getTextFromImage(image);
        value = value.replace("hPa", "");
        value = value.replace(" ", ""); // carattere strano
        LOGGER.info("pressure" + value);
        meteoStationData.pressure = Double.valueOf(value);

        image = address.replace("@image@", "7");
        value = getTextFromImage(image);
        value = value.replace("mm/h", "");
        value = value.replace(" ", ""); // carattere strano
        LOGGER.info("rainrate" + value);
        meteoStationData.rainrate = Double.valueOf(value);

        meteoStationData.spotName = mName;

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

            /*final Image oldImage = ImagesServiceFactory.makeImage(baos.toByteArray());
            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            Transform resize = ImagesServiceFactory.makeRotate(0);
            OutputSettings os = new OutputSettings(ImagesService.OutputEncoding.JPEG);
            Image newImage = imagesService.applyTransform(resize, oldImage, os);
            byte[] newImageData = newImage.getImageData();*/

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
        LOGGER.info("reponse_data=" + reponse_data);
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
