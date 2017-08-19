package Wind;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import com.vdurmont.emoji.EmojiParser;
import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.PullData;
import windalarm.meteodata.Spot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Math.toIntExact;

/**
 * Created by gs163400 on 19/07/2017.
 */
public class MyAmazingBot extends TelegramLongPollingBot {

    private String smile_emoji = EmojiParser.parseToUnicode(":smiley: some text");
    private String share_number_emoji = EmojiParser.parseToUnicode(":phone: share your number");
    private String money_emoji = EmojiParser.parseToUnicode(":moneybag:");
    private String wind_emoji = EmojiParser.parseToUnicode(":blowing_wind:");
    private String star_emoji = EmojiParser.parseToUnicode(":star:");

    private String button_MeteoStations = "Stazioni meteo";
    private String button_Back = "Indietro";
    private String button_FoehnDiagram = "Diagramma del Foehn";
    private String button_Settings = "Impostazioni";
    private String button_Kmh = "Km/h";
    private String button_Knots = "Knots";

    private String settings_knots = "knots";
    private String settings_kmh = "kmh";

    //private int windUnit = settings_kmh;

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

    public void sendResponse(String message, String chatid) {

        TelegramDataLog datalog = new TelegramDataLog();
        datalog.writelog("response", Long.parseLong(chatid), message);

        SendMessage response = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatid)
                .setParseMode(ParseMode.HTML)
                .setReplyMarkup(getMainKeyboard())
                .setText(message);

        try {

            sendMessage(response); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {


        if (update.hasMessage()) {
            Message updateMessage = update.getMessage();
            TelegramDataLog datalog = new TelegramDataLog();
            TelegramUser users = new TelegramUser();
            long userid = updateMessage.getChatId();
            Chat chat = updateMessage.getChat();
            String firstname = chat.getFirstName();
            String lastname = chat.getLastName();
            String username = chat.getUserName();
            users.insert(userid, firstname, lastname, username);
            if (updateMessage.isCommand()) {


                String command = update.getMessage().getEntities().get(0).getText();
                if (command.equalsIgnoreCase("/start"/*START_COMMAND*/)) { // "/start" string

                    datalog.writelog(command, updateMessage.getChatId(), updateMessage.getText());

                    sendResponse("Ciao, Benvenuto nel bot del vento per windsurfisti", update.getMessage().getChatId().toString());
                } else if (command.equalsIgnoreCase("/foehn")) {
                    sendFoehnDiagram(update.getMessage().getChatId());

                } /*else if (command.equalsIgnoreCase("/valma")
                        || command.equalsIgnoreCase("/sorico")
                        || command.equalsIgnoreCase("/abbadia")
                        || command.equalsIgnoreCase("/gera")
                        || command.equalsIgnoreCase("/dervio")
                        || command.equalsIgnoreCase("/dongo")
                        || command.equalsIgnoreCase("/gravedona")
                        || command.equalsIgnoreCase("/portopollo")
                        || command.equalsIgnoreCase("/cremia")) {

                    long chatid = updateMessage.getChatId();
                    String text = updateMessage.getText();
                    String spot = command.replace("/", "");
                    datalog.writelog(command, chatid, text);
                    sendLastThreeHoursMeteoData(chatid, spot);
                }*/
            } else if (updateMessage.hasText()) { // We check if the update has a message and the message has text

                String text = updateMessage.getText();
                long chatid = updateMessage.getChatId();

                Spot spot = Core.getSpotFromShortName(text);
                SpotZone spotzone = SpotZones.getFromName(text);

                if (spot != null) {
                    sendLastThreeHoursMeteoData(chatid, spot.getShortName());
                } else if (spotzone != null) {
                    sendMeteoStationKeyboard(chatid, spotzone.id);
                } else if (text.equals(button_Back)) {
                    sendMainKeyboard(chatid,updateMessage.getMessageId());
                } else if (text.equals(button_FoehnDiagram)) {
                    sendFoehnDiagram(chatid);
                } else if (text.equals(button_MeteoStations)) {
                    sendMeteoStationKeyboard(chatid,-1);
                } else if (text.equals(button_Settings)) {
                    sendSettingsKeyboard(chatid,updateMessage.getMessageId());
                } else if (text.equals(button_Kmh)) {
                    users.setUnit(userid,settings_kmh);
                    sendMainKeyboard(chatid,updateMessage.getMessageId());
                } else if (text.equals(button_Knots)) {
                    users.setUnit(userid,settings_knots);
                    sendMainKeyboard(chatid,updateMessage.getMessageId());
                } else {

                    /*String txt = "<b>prova</b>";
                    SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(update.getMessage().getChatId())
                        .setParseMode(ParseMode.HTML)
                        .setText("risposta: " + smile_emoji + update.getMessage().getText() + txt);

                    datalog.writelog("message", updateMessage.getChatId(), updateMessage.getText());


                try {
                    sendMessage(message); // Call method to send the message
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }*/
            }
            }
        } else if (update.hasCallbackQuery()) {

            // Set variables
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            String command = "";
            try {
                JSONObject json = new JSONObject(call_data);
                if (json.has("command")) {
                    command = json.getString("command");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Spot spot = Core.getSpotFromShortName(command);

            if (command.equals("update")) {
                try {
                    JSONObject json = new JSONObject(call_data);
                    if (json.has("spot") && json.has("hours")) {
                        command = json.getString("command");
                        String spotname = json.getString("spot");
                        int hours = json.getInt("hours");

                        Date endTime = Core.getDate();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endTime);
                        cal.add(Calendar.HOUR, -hours);
                        Date startTime = cal.getTime();
                        sendMeteodata(spotname, chat_id,startTime,endTime, "Ultime " + hours + " ore");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (command.equals("meteostation")) {

                InlineKeyboardMarkup markupInline = getMeteoStationsInlineKeyboardMarkup();
                EditMessageReplyMarkup new_messagereplymarkup = new EditMessageReplyMarkup()
                        .setChatId(chat_id)
                        .setMessageId(toIntExact(message_id))
                        .setReplyMarkup(markupInline);
                try {
                    editMessageReplyMarkup(new_messagereplymarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("last3hours") || command.equals("last6hours") || command.equals("last12hours")
                    || command.equals("last24hours")) {


                try {
                    JSONObject json = new JSONObject(call_data);
                    if (json.has("spot") && json.has("hours")) {
                        command = json.getString("command");
                        String spotname = json.getString("spot");
                        int hours = json.getInt("hours");

                        Date endTime = Core.getDate();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endTime);
                        cal.add(Calendar.HOUR, -hours);
                        Date startTime = cal.getTime();
                        sendMeteodata(spotname, chat_id,startTime,endTime, "Ultime " + hours + " ore");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (command.equals("foehn")) {

                sendFoehnDiagram(chat_id);
            } else if (spot != null) {

                //Spot spot = Core.getSpotFromShortName(command);
                if (spot != null) {

                    int hours = 3;
                    Date endTime = Core.getDate();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(endTime);
                    cal.add(Calendar.HOUR, -hours);
                    Date startTime = cal.getTime();
                    sendMeteodata(spot.getShortName(), chat_id, startTime, endTime, "Ultime " + hours + " ore");
                }
            } else {
                    //sendResponse(command,""+chat_id);
            }
        }
    }

    private void sendLastThreeHoursMeteoData(long chatid, String spot) {
        Date endTime = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);
        cal.add(Calendar.HOUR, -3);
        Date startTime = cal.getTime();
        sendMeteodata(spot, chatid, startTime,endTime, "Ultime " + 3 +" ore");
    }

    private InlineKeyboardMarkup getMeteoStationsInlineKeyboardMarkup() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        try {
            JSONObject json = new JSONObject();
            json.put("command","back");
            //rowInline.add(new InlineKeyboardButton().setText("Indietro").setCallbackData(json.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<PullData> list = Core.getSpotList();
        int count = 0; // 1 perchè c'è già il bottone iondietro
        for (Spot spot: list) {

            if (count != 0 && count % 2 == 0) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
            String shortname = spot.getShortName();
            //
            try {
                JSONObject json = new JSONObject();
                json.put("command",/*"/"+*/shortname);
                rowInline.add(new InlineKeyboardButton().setText(spot.getName()).setCallbackData(json.toString()));
                count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        // Set the keyboard to the markup
        rowsInline.add(rowInline);

        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private void sendMeteodata(String spotname, long chatid, Date startTime, Date endTime, String caption) {

        Spot spot = Core.getSpotFromShortName(spotname);
        if (spot == null) return;
        MeteoStationData md = Core.getLastMeteoData(spot.getSpotId());
        if (spot.getOffline() || md == null) {
            try {
                SendMessage responseError = new SendMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(chatid)
                        .setParseMode(ParseMode.HTML)
                        .setReplyMarkup(getMainKeyboard())
                        .setText("Stazione Meteo offline");


                sendMessage(responseError); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            return;
        }

        TelegramUser user = new TelegramUser();
        String unit = settings_kmh;
        if (user.read(chatid)) {
            unit = user.unit;
        }
        double low = 10;
        double medium = 16;
        double high = 25;
        String unitText = "Km/h";
        if (unit.equals(settings_knots)) {
            low = MeteoStationData.kmhToKnots(low);
            medium = MeteoStationData.kmhToKnots(medium);
            high = MeteoStationData.kmhToKnots(high);
            md.speed = MeteoStationData.kmhToKnots(md.speed);
            md.averagespeed = MeteoStationData.kmhToKnots(md.averagespeed);
            unitText = "Knots";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm");

        String score = "";
        if (md.speed >= low)
            score += star_emoji;
        if (md.speed >= medium)
            score += star_emoji;
        if (md.speed > high)
            score += star_emoji;


        String message = "<b>" + spot.getName() + score + "</b>\n" +
                "Dati aggiornati il: <b>" + dateFormat.format(md.datetime) + "</b>\n" +
                "Direzione: <b>" + md.direction + "</b>\n" +
                "Velocità vento: <b>" + md.speed + unitText + "</b>\n" +
                "Velocità media: <b>" + md.averagespeed + unitText + "</b>\n" +
                "Temperatura: <b>" + md.temperature + "°C</b>\n" /*+
                "Pressione: <b>" + md.pressure + "HPa</b>\n" +
                "Umidità: <b>" + md.humidity + "%</b>\n" +
                "Rainrate: <b>" + md.rainrate + "mm</b>\n"*/;
        SendMessage response = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatid)
                .setParseMode(ParseMode.HTML)
                .setReplyMarkup(getMainKeyboard())
                .setText(message);

        try {

            sendMessage(response); // Call method to send the message

            TelegramDataLog datalog = new TelegramDataLog();
            datalog.writelog("sendmeteodata", chatid, message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        String chartCaption = /*"Diagramma vento " + */spot.getName() + " - " + caption;
        String filepath = getData(spot.getSpotId(),startTime,endTime,unit,low,medium,high);
        if (filepath != null && !filepath.equals("")) {
            sendChart(filepath, chatid, chartCaption, 1, spot.getShortName(),spot.getSourceUrl());
        } else {
            try {
                SendMessage responseError = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(chatid)
                    .setParseMode(ParseMode.HTML)
                    .setText("impossibile creare grafico");

                sendMessage(responseError); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendHideKeyboard(Long chatId, Integer messageId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText("prova");

        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        sendMessage(sendMessage);
    }

    private void sendMainKeyboard(Long chatId, Integer messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(messageId);
        //replyKeyboardRemove.setSelective(true);
        sendMessage.setReplyMarkup(getMainKeyboard());
        sendMessage.setText("scegli");

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void sendSettingsKeyboard(Long chatId, Integer messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText("Scegli le impostazioni");
        //replyKeyboardRemove.setSelective(true);
        sendMessage.setReplyMarkup(getSettingsKeyboard());



        /*String answer = "Updated message text";
        EditMessageText new_message = new EditMessageText()
                //.setChatId(chatId)
                //.setMessageId(messageId)
                //.setMessageId(toIntExact(messageId))
                .setInlineMessageId(messageId.toString())
                .setText(answer);
        try {
            editMessageText(new_message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }*/


        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        //sendMainKeyboard(chatId,sendMessage.getReplyToMessageId());
    }

    private void sendMeteoStationKeyboard(Long chatId, int fatherid) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.enableMarkdown(true);
        //sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText("stazioni meteo");

        //ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        //replyKeyboardRemove.setSelective(true);
        sendMessage.setReplyMarkup(getMeteostationsKeyboard(fatherid));

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getMainKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(button_MeteoStations);
        row.add(button_FoehnDiagram);
        row.add(button_Settings);
        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(button_Kmh);
        row.add(button_Knots);
        //row.add(button_Settings);
        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getMeteostationsKeyboard(int fatherid) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        List<SpotZone> spotzones = SpotZones.getChildren(fatherid);

        int count = 0; // 1 perchè c'è già il bottone iondietro
        for (SpotZone spotzone : spotzones) {
            if (count != 0 && count % 3 == 0) {
                keyboard.add(row);
                row = new KeyboardRow();
            }
            row.add(spotzone.name);
            count++;
        }

        SpotZone father = SpotZones.getFromId(fatherid);
        if (father != null) {
            for (Spot spot : father.spotlist) {
                if (count != 0 && count % 3 == 0) {
                    keyboard.add(row);
                    row = new KeyboardRow();
                }
                String shortname = spot.getShortName();
                row.add(shortname);
                count++;
            }
        }
        keyboard.add(row);

        row = new KeyboardRow();
        row.add(button_Back);
        keyboard.add(row);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }


    private void sendFoehnDiagram(long chatid) {

        String filepath = Core.getTmpDir() + "/foehn.jpg";
        Core.getImage("http://www.meteocentrale.ch/uploads/pics/uwz-ch_foehn_it.png", filepath);
        sendPhoto(filepath, chatid, "Diagramma Foehn");
    }

    private void sendPhoto(String filepath, long chatid, String caption) {

        //java.io.File file = new java.io.File("C:\\Users\\gs163400\\Downloads\\IMG_20140518_155544.jpg");
        java.io.File file = new java.io.File(filepath);

        SendPhoto photo = new SendPhoto() // Create a SendMessage object with mandatory fields
                .setNewPhoto(file)
                .setCaption(caption)
                .setChatId(chatid);

        //button
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Update").setCallbackData("update_msg_text"));
        rowInline.add(new InlineKeyboardButton().setText("3 ore").setCallbackData("update_msg_text2"));
        rowInline.add(new InlineKeyboardButton().setText("6 ore").setCallbackData("update_msg_text3"));
        rowInline.add(new InlineKeyboardButton().setText("12").setCallbackData("update_msg_text4"));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText("24 ore").setCallbackData("update_msg_text"));
        rowInline2.add(new InlineKeyboardButton().setText("ieri").setCallbackData("update_msg_text2"));
        rowInline2.add(new InlineKeyboardButton().setText("settimana").setCallbackData("update_msg_text3"));
        rowInline2.add(new InlineKeyboardButton().setText("Update message text").setCallbackData("update_msg_text4"));

        // Set the keyboard to the markup
        rowsInline.add(rowInline);
        rowsInline.add(rowInline2);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        //photo.setReplyMarkup(markupInline);
        //

        try {
            sendPhoto(photo); // Call method to send the message

            TelegramDataLog datalog = new TelegramDataLog();
            datalog.writelog("sendphoto", chatid, filepath);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendChart(String filepath, long chatid, String caption, int parentMessageId, String spotShortName, String source) {

        java.io.File file = new java.io.File(filepath);

        SendPhoto photo = new SendPhoto() // Create a SendMessage object with mandatory fields
                .setNewPhoto(file)
                .setCaption(caption)
                .setChatId(chatid);

        //button
        JSONObject json = new JSONObject();
        try {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            json.put("command", "last3hours");
            json.put("spot", spotShortName);
            json.put("hours", 3);
            rowInline.add(new InlineKeyboardButton().setText("3 ore").setCallbackData(json.toString()));
            json.put("command", "last6hours");
            json.put("spot", spotShortName);
            json.put("hours", 6);
            rowInline.add(new InlineKeyboardButton().setText("6 ore").setCallbackData(json.toString()));
            json.put("command", "last12hours");
            json.put("spot", spotShortName);
            json.put("hours", 12);
            rowInline.add(new InlineKeyboardButton().setText("12 ore").setCallbackData(json.toString()));
            json.put("command", "last24hours");
            json.put("spot", spotShortName);
            json.put("hours", 24);
            rowInline.add(new InlineKeyboardButton().setText("24 ore").setCallbackData(json.toString()));

            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            json.put("command", "update");
            json.put("spot", spotShortName);
            json.put("hours", 3);
            rowInline2.add(new InlineKeyboardButton().setText("Aggiorna").setCallbackData(json.toString()));
            json.put("command", "link");
            //rowInline2.add(new InlineKeyboardButton().setText("Stazioni Meteo").setCallbackData(json.toString()));
            rowInline2.add(new InlineKeyboardButton().setUrl(source).setText(source.replace("http://","")).setCallbackData(json.toString()));

            /*json.put("command", "foehn");
            rowInline2.add(new InlineKeyboardButton().setText("Diagramma foehn").setCallbackData(json.toString()));
            */

            // Set the keyboard to the markup
            rowsInline.add(rowInline);
            rowsInline.add(rowInline2);
            // Add it to the message
            markupInline.setKeyboard(rowsInline);
            photo.setReplyMarkup(markupInline);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            sendPhoto(photo); // Call method to send the message

            TelegramDataLog datalog = new TelegramDataLog();
            datalog.writelog("sendphoto", chatid, filepath);

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

    private String getData(long spotid,Date startTime,Date endTime, String windUnit, double low, double medium, double high) {

        List<MeteoStationData> list = Core.getHistory(spotid, startTime, endTime, 0,40);

        if (windUnit.equals(settings_knots)) {
            for (MeteoStationData data : list) {
                data.speed = MeteoStationData.kmhToKnots(data.speed);
                data.averagespeed = MeteoStationData.kmhToKnots(data.averagespeed);
            }
        }

        SimpleDateFormat df = new SimpleDateFormat("HH:mm");

        if (list == null || list.size() <= 0)
            return "";

        long min = startTime.getTime();//list.getFromName(0).datetime.getTime();
        long max = endTime.getTime();//list.getFromName(list.size() - 1).datetime.getTime();
        if (min == max) return "";
        long duration = (max - min) / 1000 / 60; //durata espressa in minuti

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
        double defaultMaxSpeed = 36;
        if (windUnit.equals(settings_knots))
            defaultMaxSpeed = MeteoStationData.kmhToKnots(defaultMaxSpeed);
        if (maxspeed < defaultMaxSpeed)
            maxspeed = defaultMaxSpeed;

        for (MeteoStationData md : list) {

            double x = (md.datetime.getTime() - min) * 100 / (max - min);
            x = Math.floor(x * 100) / 100;

            if (count > 0) xserie += ",";
            xserie += x;

            if (count > 0) speed += ",";
            double val = md.speed * 100 / maxspeed;
            val = Math.floor(val * 100) / 100;
            speed += val;

            if (count > 0) avspeed += ",";
            val = md.averagespeed * 100 / maxspeed;
            val = Math.floor(val * 100) / 100;
            avspeed += val;

            if (count > 0) direction += ",";
            val = md.directionangle * 100 / 360.0;
            //val = Math.floor(val * 1) / 1;

            direction += (int) val;
            count++;
        }

        Calendar calcounter = Calendar.getInstance();
        calcounter.setTime(startTime);
        Date cursor = /*list.getFromName(0).datetime;*/startTime;

        int step = (int) duration / 6;//30; //intervallo temporale in minuti
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        cal.add(Calendar.MINUTE, step);
        double xstep = (cal.getTime().getTime() - min) * 100.0 / (max - min);
        xstep = Math.floor(xstep * 100) / 100;

        String xlabels = "";
        int i = 0;
        while (endTime.after(cursor) || endTime.equals(cursor)) {
            if (i++ != 0)
                xlabels +=",";
            double z = (calcounter.getTime().getTime() - min) * 100.0 / (max - min);
            z = Math.floor(z * 100) / 100;
            xlabels += "" + z;

            times += "|" + df.format(cursor);

            calcounter.add(Calendar.MINUTE, step);
            cursor = calcounter.getTime();
        }
        times += "|";

        String str = "";
        String chd = "&chd=t:" + xserie + "|" + speed + "|" + xserie + "|" + avspeed + "|" + xserie + "|" + direction;
        String chs = "&chs=600x300";
        //String chg = "&chg=15,10";
        //chg=<x_axis_step_size>,<y_axis_step_size>,<opt_dash_length>,<opt_space_length>,<opt_x_offset>,<opt_y_offset>
        //xstep = step * 100 / duration;
        String chg = "&chg=" + xstep + "," + 2 * 100 / maxspeed;
        //String chxl = "&chxl=0:|Freezing|C|kk|kk|Hot|2:|S|E|N|O|";
        String chxl = "&chxl=0:" + times + "1:|E|N|O|S|E|" + "3:|km/h|";
        if (windUnit.equals(settings_knots))
            chxl += "3:|knots|";
        else if (windUnit.equals(settings_kmh))
            chxl += "3:|km/h|";
        //String chxp = "&chxp=0,0,20,40,60,80,100";
        String chxp = "&chxp=0," + xlabels;//,0,20,40,60,80,100";
        chxp += "|3,100";

        String chxs = "&chxs=3,0000dd,13,-1,t,FF0000"; // - Axis label styles for the r-axis: text color, text size, left-aligned, with red tick marks.
        String chxtc = "&chxtc=1,10|3,-180"; // Axis tick lengths for the y- and r-axes. The first value specifies 10-pixel-long ticks, outside the axis. The second value specifies 180-pixel-long ticks inside the axis; the negative number means that the tick goes inside the axis, and the tick is cropped to fit inside the chart.

        String chdl = "&chdl=vel|media|dir";
        String chco = "&chco=FF0000,0000FF,00FF00"; //fff
        String chdlp = "&chdlp=t";
        String chma = "&chma=20,0,30,0";
        String chds = "&chds=a"; // auto scaling
        String chxr = "&chxr=0,0,100|1,0,360|2,0," + maxspeed + "|50";

        String chm = "&chm=H,000000,0," + 20 * maxspeed / 100 + ",1";


        double lowband = round(low/maxspeed);
        double mediumband = round((medium-low)/maxspeed);
        double highband = round((high-medium)/maxspeed);
        double veryhighband = round((maxspeed-high)/maxspeed);



        String chf = "&chf=c,ls,90"
                        + ",ebebff," + lowband
                        + ",ebffeb," + mediumband
                        + ",ffffeb," + highband
                        + ",ffebeb," + veryhighband;

        Double markervalue = 20 * 100 / maxspeed;
        //String chm = "&chm=r,ccffcc,0,0.0," + markervalue.toString();
        //String chm = "&chm=r,ccffcc,0,0.0,0.25|r,ffffcc,0,0.25,0.7|r,ffcccc,0,0.7,1.0";

        //String sourceimage = "https://chart.googleapis.com/chart?chxt=x,y,r&chds=a&cht=lxy&chco=FF0000,00FF00,0000FF&chd=t:0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|0.0,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,1.7,0.0,3.1,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,1.7,1.7,3.1,1.7,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,0.0,0.0,0.0|0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0&chs=700x250&chg=10,10&chxl=0:|Freezing|C|kk|kk|Hot|2:|S|E|N|O|";

        //String chd = "&chd=t:0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|0.0,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,1.7,0.0,3.1,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,1.7,1.7,3.1,1.7,1.7,0.0,0.0,0.0,0.0,1.7,0.0,1.7,0.0,0.0,0.0,0.0,1.7,0.0,0.0,0.0,0.0,0.0,0.0|0.0,2.0,4.0,7.0,9.0,11.0,16.0,18.0,20.0,22.0,25.0,27.0,30.0,31.0,34.0,36.0,39.0,40.0,43.0,45.0,48.0,50.0,52.0,54.0,57.0,59.0,61.0,63.0,66.0,68.0,69.0,72.0,75.0,77.0,80.0,81.0,84.0,86.0,89.0,90.0,93.0,95.0,98.0,100.0|1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,1.7,1.7,1.7,1.7,1.7,0.0,0.0,0.0,0.0,0.0,0.0,0.0";

        String sourceimage = "https://chart.googleapis.com/chart?chxt=x,y,r,r&cht=lxy" +
                chxr +
                chco +
                chdl +
                chdlp +
                chma +
                chd +
                chs +
                chg +
                chxl +
                //chm +
                chxs +
                chf +
                //chxtc +
                chxp;
        //"&chs=700x250&chg=10,10&chxl=0:|Freezing|C|kk|kk|Hot|2:|S|E|N|O|";


        //String filepath = System.getenv("tmp") + "/" + "chartx" + ".jpg";
        String filepath = Core.getTmpDir() + "/" + "chartx" + ".jpg";

        try{

            File file = new File(filepath);

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){

            e.printStackTrace();

        }


        Core.getImage(sourceimage, filepath);

        return filepath;
    }

    private double round(double val) {
        val = Math.round(val * 10.0);
        val = val / 10;
        return val;
    }
}