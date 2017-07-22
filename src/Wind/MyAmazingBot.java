package Wind;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Message;
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

        if (!update.hasMessage())
            return;
        Message updateMessage = update.getMessage();

        TelegramDataLog datalog = new TelegramDataLog();
        TelegramUser  users = new TelegramUser();
        long userid = updateMessage.getChatId();
        Chat chat = updateMessage.getChat();
        String firstname = chat.getFirstName();
        String lastname = chat.getLastName();
        String username = chat.getUserName();
        users.insert(userid,firstname,lastname,username);

        if (updateMessage.isCommand()){


            String command = update.getMessage().getEntities().get(0).getText();
            if(command.equalsIgnoreCase("/start"/*START_COMMAND*/)){ // "/start" string

                datalog.writelog(command, updateMessage.getChatId(),updateMessage.getText());

                sendResponse("Hi, Nice to meet you!" , update.getMessage().getChatId().toString());
            } else if (command.equalsIgnoreCase("/valma"/*START_COMMAND*/)
                    || command.equalsIgnoreCase("/valma2"/*START_COMMAND*/)){ // "/valma" string

                datalog.writelog(command, updateMessage.getChatId(),updateMessage.getText());
                sendMeteodata(command , update.getMessage().getChatId());
                //sendPhoto(update.getMessage().getChatId());
            }
        } else if (updateMessage.hasText()) { // We check if the update has a message and the message has text
            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText("risposta: " + smile_emoji + update.getMessage().getText());

            setButtons(message);

            datalog.writelog("message", updateMessage.getChatId(),updateMessage.getText());


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

            TelegramDataLog datalog = new TelegramDataLog();
            datalog.writelog("sendmeteodata", chatid,message);
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

            TelegramDataLog datalog = new TelegramDataLog();
            datalog.writelog("sendphoto", chatid,filepath);

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
        String times = "";
        int count = 0;

        double maxspeed = 0;
        for (MeteoStationData md : list) {
            if (maxspeed < md.speed) maxspeed = md.speed;
            if (maxspeed < md.averagespeed) maxspeed = md.averagespeed;
        }

        for (MeteoStationData md : list) {

            double x = (md.datetime.getTime() - min) * 100 / (max - min);

            if (count > 0) xserie += ",";
            xserie += x;

            if (count > 0) speed += ",";
            speed += md.speed;

            if (count > 0) avspeed += ",";
            avspeed += md.averagespeed;

            if (count > 0) direction += ",";
            double val = md.directionangle * maxspeed / 360.0;
            val = Math.floor(val * 100) / 100;

            direction += val;
            count++;
        }

        Calendar calcounter = Calendar.getInstance();
        calcounter.setTime(startTime);
        Date cursor = startTime;
        while (endTime.after(cursor) || endTime.equals(cursor)) {
            times += "|" + df.format(cursor);
            calcounter.add(Calendar.MINUTE, 30);
            cursor = calcounter.getTime();

        }
        times += "|";

        String str = "";
        String chd = "&chd=t:" + xserie + "|" + speed + "|" + xserie + "|" + avspeed + "|" + xserie + "|" + direction;
        String chs = "&chs=600x300";
        String chg = "&chg=15,10";
        //String chxl = "&chxl=0:|Freezing|C|kk|kk|Hot|2:|S|E|N|O|";
        String chxl = "&chxl=0:" + times + "2:|km/h|"+ "3:|E|N|O|S|";
        String chxp = "&chxp=|2,50|";
        String chdl = "&chdl=vel|media|dir";
        //chdl=NASDAQ|FTSE100|DOW
        String chco = "&chco=FF0000,000000,00FF00";
        String chdlp= "&chdlp=t";
        String chma = "&chma=20,0,30,0";

        //String sourceimage = "https://chart.googleapis.com/chart?chxt=x,y,r&chds=a&cht=lxy&chco=FF0000,00FF00,0000FF&chd=t:0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|0.0,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,1.7,0.0,3.1,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,1.7,1.7,3.1,1.7,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,0.0,0.0,0.0|0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0&chs=700x250&chg=10,10&chxl=0:|Freezing|C|kk|kk|Hot|2:|S|E|N|O|";

        //String chd = "&chd=t:0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|0.0,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,1.7,0.0,3.1,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,1.7,1.7,3.1,1.7,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,0.0,0.0,0.0|0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0";

        String sourceimage = "https://chart.googleapis.com/chart?chxt=x,y,y,r&chds=a&cht=lxy" +
                chco +
                chdl +
                chdlp +
                chma +
                chd +
                chs +
                chg +
                chxl +
                chxp;
                //"&chs=700x250&chg=10,10&chxl=0:|Freezing|C|kk|kk|Hot|2:|S|E|N|O|";


        //String filepath = System.getenv("tmp") + "/" + "chartx" + ".jpg";
        String filepath = Core.getTmpDir() + "/" + "chartx" + ".jpg";
        Core.getImage(sourceimage,filepath);


        return filepath;
        }
}