/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.demo.jpawebapi.api;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.demo.jpawebapi.core.app.PortalTestService;
import com.meterware.httpunit.*;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataServiceControllerFT {

    private static final String DB_URL = "jdbc:hsqldb:hsql://localhost:9010/jpademo";
    private static final String DB_LOGIN = "sa";
    private static final String DB_PASSWORD = "";

    private static final String URI_BASE = "http://localhost:8080/";
    private static final String apiPath = "app-portal/api";

    private static final String userLogin = "admin";
    private static final String userPassword = "admin";

    private static WebConversation webConversation;

    private static Connection conn;
    private static DataSet dataSet;

    private static String firstId;
    private static String secondId;
    private static String thirdId;
    private static String forthId;

    private String sessionId;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws ClassNotFoundException, SQLException {
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);

        webConversation = new WebConversation();
        dataSet = new DataSet();
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        conn = DriverManager.getConnection(DB_URL, DB_LOGIN, DB_PASSWORD);

        prepareDb();
    }

    @Before
    public void beforeEach() throws Exception {
        sessionId = login(userLogin, userPassword);
    }

    @After
    public void afterEach() {
        logout();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        dataSet.cleanup(conn);

        if (conn != null) {
            conn.close();
        }
    }

    private static void prepareDb() throws SQLException {
        thirdId = dataSet.createEntityId().toString();
        forthId = dataSet.createEntityId().toString();

        for (UUID id : dataSet.getIdPool()) {
            executePrepared("insert into JPADEMO_DRIVER(id, FIRST_NAME, version) values (?,?,?)",
                    id.toString(),
                    "TestFirstName",
                    1
            );
        }
    }

    /********************************************************************************8
     *
     * /api/commit SECTION
     *
     */

    @Test
    public void test_commit_new_instance_JSON() throws Exception {
        firstId = UUID.randomUUID().toString();
        String json = prepareFile("new_driver.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-jpademo_Driver-" + firstId)
        );
        WebResponse response = POST(apiPath + "/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("jpademo_Driver-" + firstId, res.getJSONObject(0).get("id"));

        response = GET(apiPath + "/find.json?e=jpademo_Driver-" + firstId + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject entity = new JSONObject(response.getText());
        assertEquals("Test_First_Name", entity.getString("firstName"));
    }

    @Test
    public void test_commit_new_instance_XML() throws Exception {
        secondId = UUID.randomUUID().toString();
        String xml = prepareFile("new_driver.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-jpademo_Driver-" + secondId
        ));

        WebResponse response = POST(apiPath + "/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);
        assertFieldValueEquals("Test_First_Name", instanceEl, "firstName");
        assertFieldValueEquals("Test_Last_Name", instanceEl, "lastName");
        assertFieldValueEquals("12", instanceEl, "age");

        response = GET(apiPath + "/find.xml?e=jpademo_Driver-" + secondId + "&s=" + sessionId,
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        instanceEl = (Element) instanceNodes.get(0);
        assertFieldValueEquals("Test_First_Name", instanceEl, "firstName");
        assertFieldValueEquals("Test_Last_Name", instanceEl, "lastName");
    }

    @Test
    public void test_commit_remove_instance_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/find.json?e=jpademo_Driver-" + firstId + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());

        String json = prepareFile("remove_driver.json",
                MapUtils.asMap(
                        "$ENTITY-TO_BE_REPLACED_ID$",
                        "jpademo_Driver-" + firstId
                )
        );

        response = POST(apiPath + "/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("jpademo_Driver-" + firstId, res.getJSONObject(0).getString("id"));
        assertFalse(res.getJSONObject(0).isNull("version"));

        try {
            GET(apiPath + "/find.json?e=jpademo_Driver-" + firstId + "&s=" + sessionId,
                    "text/xml;charset=UTF-8");
            fail();
        } catch (HttpNotFoundException e) {
        }
    }

    @Test
    public void test_commit_remove_instance_XML() throws Exception {
        WebResponse response = GET(apiPath + "/find.xml?e=jpademo_Driver-" + secondId + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());

        String xml = prepareFile("remove_driver.xml",
                MapUtils.asMap(
                        "$ENTITY-TO_BE_REPLACED_ID$",
                        "jpademo_Driver-" + secondId,
                        "$TO_BE_REPLACED_ID$",
                        secondId
                )
        );
        response = POST(apiPath + "/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instanceEl = (Element) instanceElements.get(0);
        assertFieldValueEquals("2", instanceEl, "version");
        assertFieldValueEquals("Test_First_Name", instanceEl, "firstName");
        assertFieldValueEquals("admin", instanceEl, "deletedBy");

        exception.expect(HttpNotFoundException.class);
        GET(apiPath + "/find.xml?e=jpademo_Driver-" + secondId + "&s=" + sessionId,
                "charset=UTF-8");
    }

    /********************************************************************************8
     *
     * /api/find SECTION
     *
     */

    @Test
    public void test_find_driver_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/find.json?e=jpademo_Driver-" + thirdId + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject entity = new JSONObject(response.getText());
        assertEquals("jpademo_Driver-" + thirdId, entity.getString("id"));
    }

    @Test
    public void test_find_driver_XML() throws Exception {
        WebResponse response = GET(apiPath + "/find.xml?e=jpademo_Driver-" + thirdId + "&s=" + sessionId,
                "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        org.dom4j.Element instanceEl = (org.dom4j.Element) instanceNodes.get(0);
        assertEquals("jpademo_Driver-" + thirdId, instanceEl.attributeValue("id"));
    }

    /********************************************************************************8
     *
     * /api/service GET SECTION
     *
     */

    @Test
    public void test_get_driver_JSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "findEntityById", thirdId);

        JSONObject responseObject = new JSONObject(response.getText());
        JSONObject resultObject = responseObject.getJSONObject("result");
        assertNotNull(resultObject);
        assertEquals("jpademo_Driver-" + thirdId, resultObject.getString("id"));
    }

    @Test
    public void test_get_driver_XML() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("xml", "findEntityById", thirdId);
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        Element instances = rootElement.element("instances");
        List<Element> instanceList = Dom4j.elements(instances, "instance");
        assertEquals(1, instanceList.size());
        Element instance = instanceList.get(0);
        assertEquals("jpademo_Driver-" + thirdId, instance.attributeValue("id"));
    }

    @Test
    public void test_get_driver_list_JSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "finAllEntities");

        JSONObject responseObject = new JSONObject(response.getText());
        JSONArray resultArray = responseObject.getJSONArray("result");
        assertNotNull(resultArray);
        assertEquals(dataSet.getIdPool().size(), resultArray.length());

        List<String> entitiesIdentifiersFromDB = new ArrayList<>();
        for (int i = 0; i < resultArray.length(); i++) {
            entitiesIdentifiersFromDB.add(resultArray.getJSONObject(i).getString("id"));
        }

        for (UUID id : dataSet.getIdPool()) {
            assertTrue(entitiesIdentifiersFromDB.contains("jpademo_Driver-" + id.toString()));
        }
    }

    @Test
    public void test_get_driver_list_XML() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("xml", "finAllEntities");

        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        Element instancesEl = rootElement.element("instances");
        List<Element> instanceList = Dom4j.elements(instancesEl, "instance");
        assertEquals(dataSet.getIdPool().size(), instanceList.size());

        List<String> entitiesIdentifiersFromDB =
                instanceList.stream().map(i -> i.attributeValue("id")).collect(Collectors.toList());

        for (UUID id : dataSet.getIdPool()) {
            assertTrue(entitiesIdentifiersFromDB.contains("jpademo_Driver-" + id.toString()));
        }
    }

    /********************************************************************************8
     *
     * /api/service POST SECTION
     *
     */

    @Test
    public void test_service_post_JSON() throws IOException, SAXException, JSONException {
        Map<String, String> replacements = new HashMap<>();
        String newFirstName = "NewTestFirstName";
        replacements.put("$ENTITY-TO_BE_REPLACED_ID$", thirdId);
        replacements.put("$NEW_FIRST_NAME$", newFirstName);

        WebResponse response = invokeServiceMethodPost("update_driver_first_name_service_post.json", replacements, "application/json;charset=UTF-8");
        JSONObject responseObject = new JSONObject(response.getText());
        JSONObject resultObject = responseObject.getJSONObject("result");
        assertNotNull(resultObject);
        assertEquals("jpademo_Driver-" + thirdId, resultObject.getString("id"));
        assertEquals(newFirstName, resultObject.getString("firstName"));
    }

    @Test
    public void test_service_post_XML() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String newFirstName = "NewTestFirstName";
        replacements.put("$ENTITY-TO_BE_REPLACED_ID$", forthId);
        replacements.put("$NEW_FIRST_NAME$", newFirstName);

        WebResponse response = invokeServiceMethodPost("update_driver_first_name_service_post.xml", replacements, "text/xml;charset=UTF-8");

        Document document = Dom4j.readDocument(response.getText());
        Element instanceEl = (Element) document.selectSingleNode("result/instances/instance");
        assertNotNull(instanceEl);

        String newfirstName = fieldValue(instanceEl, "firstName");
        assertEquals("jpademo_Driver-" + forthId, instanceEl.attributeValue("id"));
        assertEquals(newFirstName, newfirstName);
    }

    @Test
    public void test_service_collection_post_JSON() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String newfirstName = "NewTestFirstNames";
        replacements.put("$ENTITY_ID_1$", thirdId);
        replacements.put("$ENTITY_ID_2$", forthId);
        replacements.put("$NEW_FIRST_NAME$", newfirstName);

        WebResponse response = invokeServiceMethodPost("update_driver_first_names_service_post.json", replacements, "application/json;charset=UTF-8");
        JSONObject responseObject = new JSONObject(response.getText());
        JSONArray resultArray = responseObject.getJSONArray("result");
        assertEquals(2, resultArray.length());

        JSONObject entity1 = resultArray.getJSONObject(0);
        assertEquals("jpademo_Driver-" + thirdId, entity1.getString("id"));
        assertEquals(newfirstName, entity1.getString("firstName"));

        JSONObject entity2 = resultArray.getJSONObject(1);
        assertEquals("jpademo_Driver-" + forthId, entity2.getString("id"));
        assertEquals(newfirstName, entity2.getString("firstName"));
    }

    @Test
    public void test_service_collection_post_XML() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String newfirstName = "NewTestFirstNames";
        replacements.put("$ENTITY_ID_1$", thirdId);
        replacements.put("$ENTITY_ID_2$", forthId);
        replacements.put("$NEW_FIRST_NAME$", newfirstName);

        WebResponse response = invokeServiceMethodPost("update_driver_first_names_service_post.xml", replacements, "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instances = document.selectNodes("result/instances/instance");
        assertEquals(2, instances.size());

        Element entityEl1 = (Element) instances.get(0);
        String elastName1 = fieldValue(entityEl1, "firstName");
        assertEquals("jpademo_Driver-" + thirdId, entityEl1.attributeValue("id"));
        assertEquals(newfirstName, elastName1);

        Element entityEl2 = (Element) instances.get(1);
        String elastName2 = fieldValue(entityEl2, "firstName");
        assertEquals("jpademo_Driver-" + forthId, entityEl2.attributeValue("id"));
        assertEquals(newfirstName, elastName2);
    }

    /********************************************************************************8
     *
     * /api/query SECTION
     *
     */

    @Test
    public void test_get_query_JSON() throws Exception {
        String url = apiPath + "/query.json?e=jpademo_Driver&q=select c from jpademo_Driver c where c.id = :id&id=" + thirdId + "&s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray entities = new JSONArray(response.getText());
        assertEquals(1, entities.length());
        assertEquals("jpademo_Driver-" + thirdId, ((JSONObject) entities.get(0)).getString("id"));
    }

    @Test
    public void test_post_query_JSON() throws Exception {
        String url = apiPath + "/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "jpademo_Driver");
        content.put("query", "select c from jpademo_Driver c where c.id = :id");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "id");
        params.put("value", thirdId);
        content.put("params", Collections.singletonList(params));

        JSONObject jsonObject = new JSONObject(content);
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray entities = new JSONArray(response.getText());
        assertEquals(1, entities.length());
        assertEquals("jpademo_Driver-" + thirdId, ((JSONObject) entities.get(0)).getString("id"));
    }


    @Test
    public void test_get_query_XML() throws IOException, SAXException {
        String url = apiPath + "/query.xml?e=jpademo_Driver&q=select c from jpademo_Driver c where c.id = :id&id=" + thirdId + "&s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);
        assertEquals("jpademo_Driver-" + thirdId, instanceEl.attributeValue("id"));
    }

    @Test
    public void test_post_query_XML() throws IOException, SAXException {
        String url = apiPath + "/query?s=" + sessionId;
        String xml = prepareFile("query-entity.xml",
                MapUtils.asMap(
                        "$ENTITY-TO_BE_REPLACED_ID$", "jpademo_Driver",
                        "$QUERY-TO_BE_REPLACED_ID$", "select c from jpademo_Driver c where c.id = :id",
                        "$PARAM_NAME-TO_BE_REPLACED$", "id",
                        "$PARAM_VALUE-TO_BE_REPLACED$", thirdId
                )
        );

        WebResponse response = POST(url, xml, "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);
        assertEquals("jpademo_Driver-" + thirdId, instanceEl.attributeValue("id"));
    }

    /********************************************************************************8
     *
     * /api/deployViews SECTION
     *
     */

    @Test
    public void test_deployViews() throws Exception {
        String xml = prepareFile("new_views.xml", Collections.EMPTY_MAP);
        POST(apiPath + "/deployViews?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");

        WebResponse response = GET(apiPath + "/find.xml?e=jpademo_Driver-" + thirdId + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());

        Document document = Dom4j.readDocument(response.getText());
        Element entity = (Element) document.selectSingleNode("instances/instance");
        assertFieldValueEquals("TestFirstName", entity, "firstName");

        response = GET(apiPath + "/find.xml?e=jpademo_Driver-" + thirdId + "-test.minimal&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());
        document = Dom4j.readDocument(response.getText());
        entity = (Element) document.selectSingleNode("instances/instance");
        assertNull(entity.selectSingleNode("field[@name='firstName']"));
    }


    /********************************************************************************8
     *
     * /api/printDomain SECTION
     *
     */

    @Test
    public void test_printDomain() throws IOException, SAXException {
        WebResponse response = GET(apiPath + "/printDomain?" + "s=" + sessionId,
                "text/html;charset=UTF-8");
        String txt = response.getText();
        assertTrue(txt.contains("<h1>Domain model description</h1>"));
    }

    private String login(String login, String password) throws JSONException, IOException, SAXException {
        JSONObject loginJSON = new JSONObject();
        loginJSON.put("username", login);
        loginJSON.put("password", password);
        loginJSON.put("locale", "ru");

        WebResponse response = POST(apiPath + "/login",
                loginJSON.toString(), "application/json;charset=UTF-8");
        return response.getText();
    }

    private void logout() throws JSONException {
        if (sessionId == null)
            return;
        try {
            GET(apiPath + "/logout?session=" + sessionId, "charset=UTF-8");
        } catch (Exception e) {
            System.out.println("Error on logout: " + e);
        }
    }

    private WebResponse POST(String uri, String s, String contentType) {
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());

        WebResponse webResponse;
        try {
            webResponse = webConversation.sendRequest(new PostMethodWebRequest(URI_BASE + uri, is, contentType));
        } catch (IOException | SAXException e) {
            throw new RuntimeException("An error occurred while performing POST request", e);
        }

        return webResponse;
    }

    private WebResponse GET(String uri, String acceptedFormat) throws IOException, SAXException {
        GetMethodWebRequest request = new GetMethodWebRequest(URI_BASE + uri);
        request.setHeaderField("Accept", acceptedFormat);
        return webConversation.sendRequest(request);
    }

    private static void executePrepared(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }

    private WebResponse invokeServiceMethodGet(String type, String methodName, String... params) throws IOException, SAXException {
        String serviceName = PortalTestService.NAME;
        StringBuilder sb = new StringBuilder();
        sb.append(apiPath);
        sb.append("/service.").append(type);
        sb.append("?s=").append(sessionId)
                .append("&service=").append(serviceName)
                .append("&method=").append(methodName);
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            sb.append("&param").append(i).append("=").append(param);
        }
        return GET(sb.toString(), "text/html;charset=UTF-8");
    }

    private WebResponse invokeServiceMethodPost(String fileName, Map<String, String> replacements, String type) throws IOException, SAXException {
        StringBuilder sb = new StringBuilder();
        sb.append(apiPath);
        sb.append("/service")
                .append("?s=").append(sessionId);
        String fileContent = getFileContent(fileName, replacements);
        return POST(sb.toString(), fileContent, type);
    }

    private String prepareFile(String fileName, Map<String, String> replacements) throws IOException {
        InputStream is = getClass().getResourceAsStream(fileName);
        String fileContent = IOUtils.toString(is);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            fileContent = fileContent.replace(entry.getKey(), entry.getValue());
        }
        return fileContent;
    }

    private String getFileContent(String fileName, Map<String, String> replacements) throws IOException {
        InputStream is = getClass().getResourceAsStream(fileName);
        String fileContent = IOUtils.toString(is);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            fileContent = fileContent.replace(entry.getKey(), entry.getValue());
        }
        return fileContent;
    }

    private String fieldValue(Element instanceEl, String propertyName) {
        return instanceEl.selectSingleNode("field[@name = '" + propertyName + "']").getText();
    }

    private void assertFieldValueEquals(String value, Element instanceEl, String fieldName) {
        Element fieldEl = (Element) instanceEl.selectSingleNode("field[@name='" + fieldName + "']");
        assertNotNull(fieldEl);
        assertEquals(value, fieldEl.getText());
    }
}
