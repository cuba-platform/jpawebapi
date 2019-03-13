[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

- [1. Overview](#overview)
- [2. Installation](#installation)
- [3. Describing Functions](#describing-functions)
  - [3.1. Login](#login)
  - [3.2. Logout](#logout)
  - [3.3. Loading a Persistent Object Instance From the Database by Identifier](#loading-by-identifier)
  - [3.4. Executing JPQL Query to Retrieve Data](#executing)
  - [3.5. Committing New and Modified Instances, Removal](#new-modified)
  - [3.6. Uploading Files to Storage](#uploading)
  - [3.7. Downloading Files from Storage](#downloading)
  - [3.8. Obtaining Data Model Description in HTML Format](#obtaining)
  - [3.9. Creating New Views on Server](#creating)
  - [3.10. Service Calls](#service-calls)
- [4. CORS Settings](#cors-settings)

# 1. Overview <a name="overview"></a>

The universal JPA web API of the platform enables loading and saving any entities defined in the application data model by sending simple HTTP requests. This provides an easy way to integrate with a wide range of third-party applications – from the JavaScript code executed in the browser to arbitrary systems running on Java, .NET, PHP or any other platform.

Key API features:
- Loading entity instances from the database by identifier or by JPQL query with parameters.
- Saving new and modified instances, deleting instances.
- Obtaining a description of the data model in HTML format.
- Data representation in JSON and XML formats.
- Middleware service calls.
- User authentication.

# 2. Installation <a name="installation"></a>

To install the component in your project, do the following steps:

1. Open your application in CUBA Studio.

2. Open **Project -> Properties** in the project tree.

3. On the **App components** pane click the **Plus** button next to **Custom components**.

4. Paste the add-on coordinates in the corresponding field as follows: `group:name:version`.

  - Artifact group: `com.haulmont.addon.jpawebapi`;
  - Artifact name: `jpawebapi-global`;
  - Version: `add-on version`.

Specify the add-on version compatible with the used version of the CUBA platform.

| Platform Version | Add-on Version |
|------------------|----------------|
|7.0.x             | 0.1-SNAPSHOT   |

5. Click **OK** to save the project properties.

After that the JPA web API functions will be available at the `{host:port}/app/dispatch/api` or `{host:port}/app-portal/api` URL.

API URL can be customized via `jpawebapi.mapping.url` app property for `portal` or `web` application module.

# 3. Describing Functions <a name="describing-functions"></a>

All functions require an authenticated user session, which means that you must perform the login first and then pass the resulting session identifier to subsequent requests.

## 3.1. Login <a name="login"></a>

Login can be performed either by  GET or POST request.

**GET request**

For GET request create the URL `{host:port}/app/dispatch/api/login` with the following parameters:

- **u** − user login   
- **p** − user password   
- **l** − user locale (optional)

For example:

`http://localhost:8080/app/dispatch/api/login?u=admin&p=admin&l=ru`

**POST request**

To perform login using POST, execute request by `{host:port}/app/dispatch/api/login` address, passing JSON (`Content-Type` header has the value `application/json`) or form (`Content-Type` header has the value `application/x-www-form-urlencoded`) in request body.

Example of the JSON format:

```json
{
"username" : "admin",
  "password" : "admin",
  "locale" : "en"
}
```

Example of the form:
```
    username: admin
    password: admin
    locale: en
```

The service will return `userSessionId` in the response body and status 200 or 401 if the authentication fails.

To log in through JPA web API, the user must have `cuba.restApi.enabled` specific [permission](https://doc.cuba-platform.com/manual-7.0/permissions.html). Notice that the user have the permission if there are no [roles](https://doc.cuba-platform.com/manual-7.0/roles.html) explicitly revoking it.

## 3.2. Logout <a name="logout"></a>

Logout can also be performed either by GET or POST request.

**GET request**

To perform logout using GET, construct the URL `{host:port}/app/dispatch/logout` with the session parameter containing the current session ID obtained by calling `login`.

For example:

`http://localhost:8080/app/dispatch/api/logout?session=64f7d59d-2cf5-acfb-f4d3-f55b7882da72`

**POST request**

To perform login using POST, send request to `{host:port}/app/dispatch/api/logout` URL, passing JSON (`Content-Type` header has the value `application/json`) or form (Content-Type header has the value `application/x-www-form-urlencoded`) in the request body.

Example of the JSON format:
```json
{
	"session" : "64f7d59d-2cf5-acfb-f4d3-f55b7882da72"
}
```

Example of the form:

```json
    session: 64f7d59d-2cf5-acfb-f4d3-f55b7882da72
```

The service will return status 200.

## 3.3. Loading a Persistent Object Instance From the Database by Identifier <a name="loading-by-identifier"></a>

To load an object, you should perform GET request `{host:port}/app/dispatch/api/find.{format}` with the following parameters:

- **e** − the description of the required object in `<entity-id>` or `<entity-id-view>` format (see [EntityLoadInfo](https://doc.cuba-platform.com/manual-7.0/link_to_screen.html)). For example, `sales$Order-43c61345-d23c-48fe-ab26-567504072f05-_local`. Thus, the format allows you to specify the required [view](https://doc.cuba-platform.com/manual-7.0/views.html) of the loaded object.

- **s** − current session identifier.

**format** element of the request specifies the result format. It takes two values: `xml` and `json`.

Example of the request, which returns the result in XML format:

`http://localhost:8080/app/dispatch/api/find.xml?e=sales$Order-60885987-1b61-4247-94c7-dff348347f93-orderWithCustomer&s=c38f6bf4-fae7-4ee6-a412-9d93ff243f23`

Example the request, which returns the result in JSON format:

`http://localhost:8080/app/dispatch/api/find.json?e=sales$Order-60885987-1b61-4247-94c7-dff348347f93-orderWithCustomer&s=c38f6bf4-fae7-4ee6-a412-9d93ff243f23`

## 3.4. Executing JPQL Query to Retrieve Data <a name="executing"></a>

To execute a query, the `{host:port}/app/dispatch/api/query.{format}` GET request should be performed with the following parameters:

- **e** − the name of the entity.
- **q** − a [JPQL](https://doc.cuba-platform.com/manual-7.0/glossary.html#jpql) data query. The request may contain parameters. Their values are provided as values of same-named parameters of the HTTP query.
- **s** − the identifier of the current session.
- **view** (optional) − the [view](https://doc.cuba-platform.com/manual-7.0/views.html), which should be used to load data.
- **max** (optional) − the maximum number of rows in a resulting dataset (similar to JPA `setMaxResults`).
- **first** (optional) − the number of the first row of a resulting dataset (similar to JPA `setFirstResult`).

**format** specifies the format of obtaining the result. It takes two values: `xml` or `json`.

Examples:

```
http://localhost:8080/app/dispatch/api/query.json?e=sales$Customer&q=select+c+from+sales$Customer+c&s=748e5d3f-1eaf-4b38-bf9d-8d838587367d&view=_local

http://localhost:8080/app/dispatch/api/query.json?e=sales$Customer&q=select+c+from+sales$Customer+c+where+c.name=:name&s=748e5d3f-1eaf-4b38-bf9d-8d838587367d&name=Smith
```
For each of the passed parameters, the type can be explicitly specified by adding the parameter of the same name and the `_type` suffix to the request. For example:

```
http://localhost:8080/app/dispatch/api/query.json?e=sales$Customer&q=select+c+from+sales$Customer+c+where+c.name=:name&s=748e5d3f-1eaf-4b38-bf9d-8d838587367d&name=Smith&name_type=string
```

Specifying parameter type is optional, however, it allows you to avoid parsing errors if the system cannot determine the type automatically. Normally, the type should be specified only for string parameters, which for some reason have more specific format types (dates, numbers, UUID), but must be interpreted as strings. The list of available types can be found in meta-model description (**Help > Data Model**) or by obtaining the [HTML-description of the model](#obtaining).

A query can also be executed using POST request to the `{host:port}/app/dispatch/api/query.{format}?s=<sessionId>` URL, where `<sessionId>` is the identifier of the current session. In this case, the query and its parameters are passed in the request body as follows.

Example POST request of JSON format, the `Content-Type` header must be set to `application/json`:

```
http://localhost:8080/app/dispatch/api/query.json?s=748e5d3f-1eaf-4b38-bf9d-8d838587367d
```

The request body:
```json
{
  "entity": "sales$Customer",
  "query": "select c from sales$Customer c where c.name=:name",
  "params": [
      {
        "name": "name",
        "value": "Smith"
      }
  ]
}
```
Example POST request of XML format, the `Content-Type` header must be set to `text/xml`:

```
http://localhost:8080/app/dispatch/api/query.xml?s=748e5d3f-1eaf-4b38-bf9d-8d838587367d
```

The request body:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<QueryRequest xmlns="http://schemas.haulmont.com/cuba/restapi-query-request-v2.xsd">
    <entity>sales$Customer</entity>
    <query>select c from sales$Customer c where c.name=:name</query>
    <params>
        <param>
            <name>name</name>
            <value>Smith</value>
        </param>
    </params>
</QueryRequest>
```
## 3.5. Committing New and Modified Instances, Removal <a name="new-modified"></a>

The commit function enables performing operations on objects passed to it and returning their new state. The format of the result depends on a format (JSON or XML) used for the request (the `Content-Type` header).

**JSON format**

`application/json` should be used as the value of the `Content-Type` header.

Creating a `Customer` entity with an automatically generated identifier:

```json
{
	"commitInstances": [
		{
			"id": "NEW-sales$Customer",
			"name": "Lawrence",
			"email": "lawrence@mail.com"
        }
    ]
}
```

Creating a `Customer` entity with a specified identifier:

```json
{
	"commitInstances": [
		{
			"id": "NEW-sales$Customer-b32a6412-d4d9-11e2-a20b-87b22b1460c7",
			"name": "Bradley",
			"email": "bradley@mail.com"
        }
    ]
}
```

Creating an `Order` entity, specifying a link to a new `Custome`r entity and filling the `Customer` entity with attributes:

```json
{
	"commitInstances": [
		{
			"id": "NEW-sales$Order",
			"amount": 15,
			"customer": {
            "id": "NEW-sales$Customer-b32e43e8-d4d9-11e2-8c8b-2b2939d67fff"
			}
        },
        {
			"id": "sales$Customer-b32e43e8-d4d9-11e2-8c8b-2b2939d67fff",
			"name": "Fletcher",
			"email": "fletcher@mail.com"
        }
    ]
}
```

Changing two `Customer` entities simultaneously:

```json
{
	"commitInstances": [
		{
			"id": "sales$Customer-b32e43e8-d4d9-11e2-8c8b-2b2939d67fff",
			"email": "fletcher@mail.com"
        },
        {
          "id": "sales$Customer-32261b09-b7f7-4b8c-88cc-6dee6fa8e6ab",
          "email": "lawrence@mail.com"
        }
    ]
}
```

Removing the `Customer` entity with [soft deletion](https://doc.cuba-platform.com/manual-7.0/soft_deletion.html) support:

```json
{
	"removeInstances": [
		{
			"id": "sales$Customer-b32e43e8-d4d9-11e2-8c8b-2b2939d67fff"
		}
    ],
    "softDeletion": "true"
}
```
- The `commitInstances` array contains created and modified entities.

 - When creating an entity, `id` or `NEW-<entityName>` should be specified as the value of the `NEW-<entityName>-<uuid>` field.

 - When changing an entity, `<entityName>-<uuid>` should be specified as the value of the id field.

  - Next, attribute names and values for created or modified entity should be provided in the list of elements, separated by commas.

   If any attribute should be set to `null` while editing the entity, you must specify the view that includes this attribute, in the identifier. For example:

   ```json
{
	"commitInstances": [
		{
			"id": "sales$Customer-b32a6412-d4d9-11e2-a20b-87b22b1460c7-customer-edit",
			"name": "John Doe",
			"channel": null
        }
    ]
}
```

   Here, the `customer-edit` view must contain the `channel` attribute, otherwise, the value will not change.

- The `removeInstances` array contains removed entities. When removing an entity, you must specify the value of the `id` field. Before deletion, `merge()` will be executed for the provided object, which enables checking if the version of the removed object has changed.

- The `softDeletion` field controls [soft deletion](https://doc.cuba-platform.com/manual-7.0/soft_deletion.html) mode.

The function is called by a POST request to `{host:port}/app/dispatch/api/commit?s=<sessionId>`. JSON is passed in the request body. The function returns a JSON objects array. For example, the following JSON objects array will be returned when the `email` field of the `Customer` entity is changed:

```json
[
  {"id":"sales$Customer-32261b09-b7f7-4b8c-88cc-6dee6fa8e6ab",
    "createTs":"2013-06-14T14:07:15.040",
    "createdBy":"admin",
    "deleteTs":null,
    "deletedBy":null,
    "email":"fletcher@mail.com",
    "name":"Fletcher",
    "updateTs":"2013-06-14T15:07:03.463",
    "updatedBy":"admin",
    "version":"3"
  }
]
```

**XML format**

`text/xml` should be used as the value of the `Content-Type` header.

XML format example:

```xml
<CommitRequest>
    <commitInstances>
        <instance id="sales$Order-9873c8a8-d4e7-11e2-85c0-33423bc08c84">
            <field name="date">2015-01-30</field>
            <field name="amount">3500.00</field>
            <reference name="customer" id="sales$Customer-32261b09-b7f7-4b8c-88cc-6dee6fa8e6ab"/>
        </instance>
    </commitInstances>
    <removeInstances>
        <instance id="sales$Customer-d67c10f0-4d28-4904-afca-4bc45654985d"/>
    </removeInstances>
    <softDeletion>true</softDeletion>
</CommitRequest>
```

XML document fields semantics is defined in http://schemas.haulmont.com/cuba/6.10/restapi-commit-v2.xsd scheme.

In case of an XML request, fields are set to null with the help of the `null="true"` attribute. In addition to that, the identifier must contain the [view](https://doc.cuba-platform.com/manual-6.10/views.html), which contains the attribute. For example:

```xml
<CommitRequest>
    <commitInstances>
        <instance id="Order-9873c8a8-d4e7-11e2-85c0-33423bc08c84">
            <field name="amount" null="true"/>
            <reference name="customer" null="true"/>
        </instance>
    </commitInstances>
</CommitRequest>
```

The function is called with a POST request to `{host:port}/app/dispatch/api/commit?s=<sessionId>`. XML is passed in the request body. The request returns array of XML objects similar to the one below:

```xml
<instances>
    <instance ...>
    <instance ...>
</instances>
```
The schema containing the description of the function call result is located at http://schemas.haulmont.com/cuba/6.10/restapi-instance-v2.xsd.

## 3.6. Uploading Files to Storage <a name="uploading"></a>

To upload a file to [FileStorage](https://doc.cuba-platform.com/manual-7.0/file_storage.html), use the POST request `{host:port}/app/dispatch/api/upload` with the following parameters:

- **s** − current session ID
- **name** - file name
- **ext** - file extension
- **size** - file size in bytes

File is sent in the request body.

## 3.7. Downloading Files from Storage <a name="downloading"></a>

To download a file from [FileStorage](https://doc.cuba-platform.com/manual-7.0/file_storage.html), use the GET request `{host:port}/app/dispatch/api/download` with the following parameters:

- **f** − the ID of the corresponding `FileDescriptor` instance
- **s** − current session ID

For example:

```
http://localhost:8080/app/dispatch/api/download?s=abbfb51c-715d-ced5-cc00-ee355278ea21&f=dbea7543-7761-3680-9b6c-c06f7fdb3738
```
## 3.8. Obtaining Data Model Description in HTML Format <a name="obtaining"></a>

The `/printDomain?s=<sessionId>` GET request allows a developer to obtain data model description. The service returns a simple HTML, which contains basic data types list and description of all meta-model entities, their attributes, and [views](https://doc.cuba-platform.com/manual-7.0/views.html) defined for entities.

## 3.9. Creating New Views on Server <a name="creating"></a>

The `/deployViews?s=<sessionId>` POST request enables loading to middleware descriptions of [view](https://doc.cuba-platform.com/manual-7.0/views.html) objects, required for the client. The view objects are sent as a standard XML description of a view used in the platform. XML is placed in the request body. For more information about the format, see [Views](https://doc.cuba-platform.com/manual-7.0/views.html).

## 3.10. Service Calls <a name="service-calls"></a>

[Service](https://doc.cuba-platform.com/manual-7.0/services.html) methods available for API calls are listed in a configuration file. The name of the file is defined by the [cuba.restServicesConfig](https://doc.cuba-platform.com/manual-7.0/app_properties_reference.html#cuba.rest.servicesConfig) property.

A sample JPA web API services configuration file:

```xml
<services xmlns="http://schemas.haulmont.com/cuba/restapi-service-v2.xsd">
    <service name="refapp_PortalTestService">
        <method name="findAllCars"/>
        <method name="updateCarVin"/>
    </service>
</services>
```

Service method call can be performed both by GET and POST requests. Additionally, POST requests allow passing entities or entity collections to the invoked method.

**Service Call by GET Request**

Request format:

```
{host:port}/app/dispatch/api/service.{format}?service=<serviceName>&method=<methodName>&view=<view>&param0=<value 0>&paramN=<value N>&param0_type=<type 0>&paramN_type=<type N>&s=<sessionId>
```

- **format** - defines the output format. Two values are accepted: xml or json.
- **service** - the name of the service called.
- **method** - the name of the method invoked.
- **param0 .. paramN** - parameter values of the method.
- **param0_type .. paramN_type** - parameter types of the method.
- **s** - the current session identifier.

If a service has a single method with the specified name and number of parameters, explicit parameter type definition is not required. In other cases, parameter type must be specified.

**Service Call by POST Request**

Request format:

```
{host:port}/app/dispatch/api/service?s=<sessionId>
```

**s** - the current session identifier.

JSON or XML with the description of the method call is passed in the request body.

**JSON format**

The `Content-Type` header value is `application/json`:

```json
{
      "service": "refapp_PortalTestService",
      "method": "updateCarVin",
      "view": "carEdit",
      "params": {
		"param0": {
			"id": "ref$Car-32261b09-b7f7-4b8c-88cc-6dee6fa8e6ab",
			"vin": "WV00001",
			"colour" : {
				"id": "ref$Colour-b32a6412-d4d9-11e2-a20b-87b22b1460c7",
				"name": "Red"
			},
			"driverAllocations": [
            {
              "id": "ref$DriverAllocation-b32e43e8-d4d9-11e2-8c8b-2b2939d67fff"
            },
            {
              "id": "NEW-ref$DriverAllocation"
            }
			]
        },
        "param1": "WV00001",
        "param0_type": "com.haulmont.refapp.core.entity.Car",
        "param1_type": "java.lang.String"
      }
}
```

Properties of the passed object:

- **service** - the name of the service called.
- **method** - the name of the method invoked.
- **param0 .. paramN** - method parameter values.
- **param0_type .. paramN_type** - method parameter types.

**XML format**

The `Content-Type` header value is `text/xml`:

```xml
<ServiceRequest xmlns="http://schemas.haulmont.com/cuba/restapi-service-v2.xsd">
    <service>refapp_PortalTestService</service>
    <method>updateCarVin</method>
    <view>carEdit</view>
    <params>
        <param name="param0">
            <instance id="ref$Car-32261b09-b7f7-4b8c-88cc-6dee6fa8e6ab">
                <field name="vin">WV00000</field>
                <reference name="colour">
                    <instance id="ref$Colour-b32a6412-d4d9-11e2-a20b-87b22b1460c7">
                        <field name="name">Red</field>
                    </instance>
                </reference>
                <collection name="driverAllocations">
                    <instance id="ref$DriverAllocation-b32e43e8-d4d9-11e2-8c8b-2b2939d67fff"/>
                    <instance id="NEW-ref$DriverAllocation"/>
                </collection>
            </instance>
        </param>
        <param name="param1">WV00001</param>
        <param name="param0_type">com.haulmont.refapp.core.entity.Car</param>
        <param name="param1_type">java.lang.String</param>
    </params>
</ServiceRequest>
```

The main elements of the passed document are:

- **service** - the name of the service called.
- **method** - the name of the method invoked.
- **param** - the value of the parameter method or the parameter type. The name of the parameter (`name` attribute) must have the format `param0 .. paramN` or `param0_type .. paramN_type`.

If a service has a single method with the specified name and number of parameters, explicit parameter type definition is not required. In other cases, parameter types must be specified.

`<param>` element may contain both plain text (for setting primitive type values) and nested `<instance>` elements for entities or `<instances>` for entity collections.

The XSD of the request is available at http://schemas.haulmont.com/cuba/6.10/restapi-service-v2.xsd.

**Supported Service Method Parameter Types**

- primitive Java types. `long`, `int`, `boolean`, etc. should be specified as the type name.
- primitive Java type wrappers. The full class name should be specified as the type name: `java.lang.Boolean`, `java.lang.Integer`, etc.
- string (`java.lang.String`).
- date (`java.util.Date`).
- UUID (`java.util.UUID`).
- BigDecimal (`java.math.BigDecimal`).
- entity (for POST requests only). The full class name should be specified as the type name, e.g. `com.haulmont.cuba.security.entity.User`.
- entity collections (for POST requests only). The full class or collection interface name should be specified as the type name, e.g. `java.util.List`.

**Service Call Result**

The result may be in JSON or XML, depending on the method call declaration. Currently, methods can return primitive data types, entities and entity collections.

**Example of a JSON result**

Primitive data type:

```json
{
  "result": "10"
}
```

Entity:

```json
{
  "result": {
    "id" : "ref$Colour-b32e43e8-d4d9-11e2-8c8b-2b2939d67fff",
    "name": "Red"
  }
}
```

**Example of XML result**

Primitive data type:

```xml
<result>
    10
</result>
```

Entity:

```xml
<result>
    <instance id="ref$Colour-b32a6412-d4d9-11e2-a20b-87b22b1460c7">
        <field name="name">Red</field>
    </instance>
</result>
```
The XSD of the result is available at http://schemas.haulmont.com/cuba/6.10/restapi-service-v2.xsd.

# 4. CORS Settings <a name="cors-settings"></a>

By default, all CORS requests to the JPA web API are allowed. To restrict the origins list you can define a global CORS configuration in the Spring configuration file.

```xml
<mvc:cors>
    <mvc:mapping path="/api/**" allowed-origins="http://host1, http://host2"/>
</mvc:cors>
```
