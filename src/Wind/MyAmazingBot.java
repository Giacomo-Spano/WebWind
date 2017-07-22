package Wind;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.vdurmont.emoji.EmojiParser;
import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.Spot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by gs163400 on 19/07/2017.
 */
public class MyAmazingBot extends TelegramLongPollingBot {

    private String smile_emoji = EmojiParser.parseToUnicode(":smiley: some text");
    private String share_number_emoji = EmojiParser.parseToUnicode(":phone: share your number");
    private String money_emoji = EmojiParser.parseToUnicode(":moneybag:");

    public void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(smile_emoji);
        keyboardFirstRow.add(smile_emoji);
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardButton shareNumBtn = new KeyboardButton(share_number_emoji);
        shareNumBtn.setRequestContact(true);
        shareNumBtn.setRequestLocation(false);
        keyboardSecondRow.add(shareNumBtn);
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public void sendResponse(String message,String chatid)
    {
        SendMessage response = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatid)
                .setText(message);

        try {
            sendMessage(response); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().isCommand()){
            String command = update.getMessage().getEntities().get(0).getText();
            if(command.equalsIgnoreCase("/start"/*START_COMMAND*/)){ // "/start" string
                sendResponse("Hi, Nice to meet you!" , update.getMessage().getChatId().toString());
            } else if (command.equalsIgnoreCase("/valma"/*START_COMMAND*/) || command.equalsIgnoreCase("/valmadrera"/*START_COMMAND*/)){ // "/valma" string
                sendMeteodata(command , update.getMessage().getChatId());

                //sendPhoto(update.getMessage().getChatId());

            }
        } else if (update.hasMessage() && update.getMessage().hasText()) { // We check if the update has a message and the message has text
            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText("risposta: " + smile_emoji + update.getMessage().getText());

            setButtons(message);

            try {
                sendMessage(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMeteodata(String spotname, long chatid) {

        Spot spot = Core.getSpotFromShortName(spotname.replace("/",""));
        if (spot == null) return;
        MeteoStationData md = Core.getLastMeteoData(spot.getSpotId());
        String message = "" + spot.getName() + "\n" +
                "Dati aggiornati il: " + md.datetime + "\n" +
                "Direzione: " + md.direction + "\n" +
                "Velocità vento: " + md.speed + "km/h\n" +
                "Velocità media: " + md.averagespeed + "km/h\n" +
                "Temperatura: " + md.temperature + "°C\n" +
                "Pressione: " + md.pressure + "HPa\n" +
                "Umidità: " + md.humidity + "%\n" +
                "Rainrate: " + md.rainrate + "mm\n" ;
        SendMessage response = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatid)
                .setText(message)
                ;

        try {
            sendMessage(response); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        String filepath = getData(spot.getSpotId());
        sendPhoto(filepath,chatid);
    }

    private void sendPhoto(String filepath, long chatid) {

        //java.io.File file = new java.io.File("C:\\Users\\gs163400\\Downloads\\IMG_20140518_155544.jpg");
        java.io.File file = new java.io.File(filepath);

         SendPhoto photo = new SendPhoto() // Create a SendMessage object with mandatory fields
                .setNewPhoto(file)
                .setCaption("prova")
                 .setChatId(chatid)
                ;

        try {
            sendPhoto(photo); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        if (Core.isProduction())
            return "robinwindbot";
        else
            return "giacomouserbot";
    }

    @Override
    public String getBotToken() {
        if (Core.isProduction())
            return "319387737:AAGOIJTMCfaxgX0V6gxWsZ1RlXR2rikIrFg";
        else
            return "368348893:AAG41vFx8ScaFyQoufL-W4gcBDgfpPx9tFo";
    }


    private String getData(long spotid) {

        Date endTime = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        cal.add(Calendar.HOUR, -3);
        Date startTime = cal.getTime();

        List<MeteoStationData> list = Core.getHistory(spotid, startTime, endTime, 0);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm");

        if (list == null || list.size() <= 0)
            return "";

        long min = list.get(0).datetime.getTime();
        long max = list.get(list.size()-1).datetime.getTime();

        String xserie = "";
        String speed = "";
        String avspeed = "";
        String direction = "";
        String time = "";
        int count = 0;
        for (MeteoStationData md : list) {

            double x = (md.datetime.getTime() - min) * 100 / (max - min);

            if (count > 0) xserie += ",";
            xserie += x;

            if (count > 0) speed += ",";
            speed += md.speed;

            if (count > 0) avspeed += ",";
            avspeed += md.averagespeed;

            if (count > 0) direction += ",";
            direction += md.directionangle;

            if (count++ % 5 == 0) {
                time += "%7C" + df.format(md.datetime);
            }

        }

        String str = "";
        str = xserie + "|" + speed + "|" + avspeed;

        //String filepath = System.getenv("tmp") + "/" + "chartx" + ".jpg";
        String filepath = Core.getTmpDir() + "/" + "chartx" + ".jpg";
        Core.getImage("https://image-charts.com/chart?cht=lxy&chs=700x125&chd=t:1,2,3,4|10,50,4,44|1,2,3,4|3,33,3,10&chxt=x,y&chxl=0:%7C1%7C2v%7C3%7C4:1:%7C1%7C2v%7C3%7C4:",filepath);


        return filepath;
        }
}