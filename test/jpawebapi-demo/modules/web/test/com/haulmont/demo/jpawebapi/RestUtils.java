/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.demo.jpawebapi;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class RestUtils {


    protected static WebConversation webConversation;
    protected String sessionId;

    protected void service(String url) throws IOException, SAXException {
        JSONObject content = new JSONObject();
        content.put("service", "demo_PortalTestService");
        content.put("method", "finAllUsers");

        WebResponse response = POST(url, content.toString(), "application/json;charset=UTF-8");
        JSONObject responseObject = new JSONObject(response.getText());
        JSONArray resultObject = responseObject.getJSONArray("result");

        assertNotNull(resultObject);
        assertEquals(2, resultObject.length());
    }

    protected void query(String url) throws IOException, SAXException {
        JSONObject content = new JSONObject();
        content.put("entity", "sec$User");
        content.put("query", "select c from sec$User c");

        WebResponse response = POST(url, content.toString(), "application/json;charset=UTF-8");
        JSONArray responseObject = new JSONArray(response.getText());
        assertEquals(2, responseObject.length());
    }

    protected void logoutTest(String urlBase) throws IOException, SAXException, JSONException {
        String session = login(urlBase + "/login?u=admin&p=admin&l=ru");
        GET(urlBase + "/logout?session=" + session);
        try {
            query(urlBase + "/query?s=" + session);
        } catch (Exception e) {
            session = null;
        }
        assertNull(session);
    }

    protected WebResponse GET(String url) throws IOException, SAXException {
        GetMethodWebRequest request = new GetMethodWebRequest(url);
        request.setHeaderField("Accept", "charset=UTF-8");
        return webConversation.sendRequest(request);

    }

    protected WebResponse POST(String url, String s, String contentType) throws IOException, SAXException {
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
        return webConversation.sendRequest(new PostMethodWebRequest(url, is, contentType));
    }

    protected String login(String url) throws JSONException, IOException, SAXException {
        WebResponse response = GET(url);
        return response.getText();
    }

    protected void logout(String url) throws JSONException {
        if (sessionId == null)
            return;
        try {
            GET(url);
        } catch (Exception e) {
            System.out.println("Error on logout: " + e);
        }
    }
}
