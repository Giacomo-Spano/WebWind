package Wind.notification;

import Wind.Core;
import Wind.data.Device;
import com.google.android.gcm.server.Message;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class PushNotificationThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(PushNotificationThread.class.getName());

    JSONObject notification, data;
    List<Device> devices;

    public PushNotificationThread(int deviceId, JSONObject notification, JSONObject data) {
        super("PushNotificationThread");

        devices = new ArrayList<Device>();

        this.notification = notification;
        this.data = data;
        addDevice(deviceId);
    }

    public PushNotificationThread(List<Device> devices, JSONObject notification, JSONObject data) {
        super("str");

        this.notification = notification;
        this.data = data;
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

        //SendPushMessages sp = new SendPushMessages(notification);
        SendPushMessages sp = new SendPushMessages("type", "title", "description", "0");
        //List<Device> devices = Core.getDevicesFromDeviceId(deviceId);


        //String tokenId1 = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
        String tokenId  = devices.get(0).regId;
        String server_key ="AAAAfybHUqA:APA91bFlcdoIQsQWU2sGvK5z1LywrqbiknlfwngyQWoUbNfLhEXxLSJOpU1unq8V2qHJDPg3Z6q60vZkLTfp68eK34SanqM8rYhP3fV3qAjt_q5rs-p_czwFEiTFtICWMVBtVjB2nB70" ;
        String message = "Welcome alankit Push Service.";

        /*putIds= new ArrayList<>();
        putIds.add(tokenId1);
        putIds.add(tokenId);*/
   /* for Group*/
        //FCM.send_FCM_NotificationMulti(putIds,tokenId,server_key,message);

    /*for indevidual*/
        /*Map<Object, Object> jsonRequest = new HashMap();
        jsonRequest.put("registration_ids", tokenId);  //
        Map<String, String> payload = notification.getData();
        if(!payload.isEmpty()) {
            jsonRequest.put("data", payload);
        }*/

        //String requestBody = JSONValue.toJSONString(jsonRequest);

        FCM.send_FCM_Notification( tokenId,server_key,notification,data);
        //sp.send(devices);
    }
}

