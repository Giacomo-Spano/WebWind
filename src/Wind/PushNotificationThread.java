package Wind;

import com.google.android.gcm.server.Message;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class PushNotificationThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(PushNotificationThread.class.getName());

    int deviceId;
    String type;
    String title;
    String description;
    String value;
    Message notification;

    /*public PushNotificationThread(String type, String title, String description, String value) {
        super("str");

        this.type = type;
        this.title = title;
        this.description = description;
        this.value = value;
    }*/

    public PushNotificationThread(int deviceId, Message notification) {
        super("str");
        this.notification = notification;
        this.deviceId = deviceId;
    }

    public void run() {

        LOGGER.info("PushNotificationThread type=" + type + "title=" + title + "value=" + value);
        SendPushMessages sp = new SendPushMessages(notification);
        //SendPushMessages sp = new SendPushMessages(type, title, description, value);
        List<Device> devices = Core.getDevicesFromDeviceId(deviceId);
        sp.send(devices);
        LOGGER.info("PushNotificationThread type=" + type + "title=" + title + "value=" + value);
    }
}

