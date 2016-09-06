package Wind;

import com.google.android.gcm.server.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class PushNotificationThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(PushNotificationThread.class.getName());

    Message notification;
    List<Device> devices;

    public PushNotificationThread(int deviceId, Message notification) {
        super("PushNotificationThread");

        devices = new ArrayList<Device>();

        this.notification = notification;
        addDevice(deviceId);
    }

    public PushNotificationThread(List<Device> devices, Message notification) {
        super("str");

        this.notification = notification;
        this.devices = devices;
    }

    public void addDevice(Device device) {

        devices.add(device);
    }
    public void addDevice(int deviceId) {

        LOGGER.info("addDevice deviceId" + deviceId);

        Device device = Core.getDevicesFromDeviceId(deviceId);
        devices.add(device);
    }

    public void run() {

        LOGGER.info("PushNotificationThread::run");

        for (Device device : devices) {
            LOGGER.info("deviceid=" + device.id + ", name=" + device.name + ", regId=" + device.regId);
        }

        SendPushMessages sp = new SendPushMessages(notification);
        //SendPushMessages sp = new SendPushMessages(type, title, description, value);
        //List<Device> devices = Core.getDevicesFromDeviceId(deviceId);
        sp.send(devices);

    }
}

