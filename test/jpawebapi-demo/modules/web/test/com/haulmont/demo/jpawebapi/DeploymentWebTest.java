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

import com.meterware.httpunit.WebConversation;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class DeploymentWebTest extends RestUtils {

    private static final String URL_WEB = "http://localhost:8080/app/dispatch/api";

    @Before
    public void beforeEach() throws Exception {
        String url = URL_WEB + "/login?u=admin&p=admin&l=ru";
        webConversation = new WebConversation();
        sessionId = login(url);
    }

    @After
    public void afterEach() {
        String url = URL_WEB + "/logout?session=" + sessionId;
        logout(url);
    }

    @Test
    public void loginTest() {
        assertNotNull(sessionId);
    }

    @Test
    public void query() throws IOException, SAXException {
        String url = URL_WEB + "/query?s=" + sessionId;
        query(url);
    }

    @Test
    public void service() throws IOException, SAXException, JSONException {
        String url = URL_WEB + "/service?s=" + sessionId;
        service(url);
    }

    @Test
    public void logoutTest() throws IOException, SAXException, JSONException {
        logoutTest(URL_WEB);
    }
}
