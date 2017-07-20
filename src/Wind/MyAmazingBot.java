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

import java.util.ArrayList;
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
                sendMeteodata(command , update.getMessage().getChatId().toString());

                sendPhoto(update.getMessage().getChatId());
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

    private void sendMeteodata(String spotname, String chatid) {

        Spot spot = Core.getSpotFromShortName(spotname.replace("/",""));
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
    }

    private void sendPhoto(long chatid) {

        java.io.File file = new java.io.File("C:\\Users\\gs163400\\Downloads\\IMG_20140518_155544.jpg");

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
        // TODO
        return "giacomouserbot";
    }

    @Override
    public String getBotToken() {
        // TODO
        return "368348893:AAG41vFx8ScaFyQoufL-W4gcBDgfpPx9tFo";
    }
}