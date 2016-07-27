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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
            Devices devices =  new Devices();
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
        //String deviceId = getParameter(req, "deviceId");


        if (registeruser != null && registeruser.equals("true")) {
            String authCode = getParameter(req, "authcode");

        } else {
            String regId = getParameter(req, "regId");
            LOGGER.info("RegisterServlet regid=" + regId);

            Device device = new Device();
            device.regId = regId;
            device.id = 0;
            device.name = getParameter(req, "name");
            device.date = Core.getDate();
            //device.deviceId = deviceId;
            Core.addDevice(device);
        }

        setSuccess(resp);
    }

}
