/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package Wind;

import windalarm.meteodata.MeteoStationData;
import windalarm.meteodata.Spot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * DebugServlet that registers a device, whose registration id is identified by
 * {@link #PARAMETER_REG_ID}.
 * <p/>
 * <p/>
 * The client app should call this servlet everytime it receives a
 * {@code com.google.android.c2dm.intent.REGISTRATION C2DM} intent without an
 * error or {@code unregistered} extra.
 */
public class RegisterServlet extends BaseServlet {

    private static final Logger LOGGER = Logger.getLogger(PushNotificationThread.class.getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            String regId = request.getParameter("regid");
            Devices devices = new Devices();
            PrintWriter out = response.getWriter();

            if (devices != null) {
                Device device = devices.getDeviceFromRegId(regId);
                response.setContentType("application/json");
                out.println("{\"id\" : \"" + device.id + "\" }");
            }

            out.close();

            setSuccess(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {

        String registeruser = getParameter(req, "registeruser");
        String registerdevice = getParameter(req, "registerdevice");

        if (registeruser != null && registeruser.equals("true")) {

            String personId = getParameter(req, "personId");
            String personName = getParameter(req, "personName");
            String personEmail = getParameter(req, "personEmail");
            String personPhoto = getParameter(req, "personPhoto");
            String authCode = getParameter(req, "authCode");

            int userid = Core.addUser(personId,personName,personEmail,authCode,personPhoto,authCode);

            resp.setContentType("application/json");
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                out.println("{\"id\" : \"" + userid + "\", \"personid\" : \"" + personId + "\" }");
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.close();

            setSuccess(resp);

        } else if (registerdevice != null && registerdevice.equals("true")) {
            String regId = getParameter(req, "regId");
            String personId = getParameter(req, "personId");
            String personName = getParameter(req, "personName");
            String personEmail = getParameter(req, "personEmail");
            String personPhoto = getParameter(req, "personPhoto");
            String authCode = getParameter(req, "authCode");

            int userid = Core.addUser(personId,personName,personEmail,authCode,personPhoto,authCode);

            Device device = new Device();
            device.regId = regId;
            device.id = 0;
            device.name = getParameter(req, "name");
            device.date = Core.getDate();
            device.personId = personId;
            int deviceId = Core.addDevice(device);

            resp.setContentType("application/json");
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                out.println("{\"deviceid\" : " + deviceId + ", \"userid\" : " + userid + ", \"spotlist\" : [");  // todo eliminare il tag spotlist

                ArrayList<Spot> spotlist = Core.getSpotList();
                Iterator<Spot> iterator = spotlist.iterator();
                String str = "";
                int count = 0;
                while (iterator.hasNext()) {
                    Spot spot = iterator.next();
                    if (count++ != 0)
                        str += ",";
                    str += "{\"spotname\" : \"" + spot.name + "\",";
                    str += "\"id\" : " + "\"" + spot.ID + "\",";
                    str += "\"sourceurl\" : " + "\"" + spot.sourceUrl + "\",";
                    str += "\"webcamurl\" : " + "\"" + spot.webcamUrl + "\"}";
                }
                str += "] }";
                out.println(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.close();

            setSuccess(resp);
        }
    }

}
