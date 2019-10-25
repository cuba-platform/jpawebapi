# CUBA JPA Web API Addon

[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/cuba-platform/jpawebapi.svg?branch=master)](https://travis-ci.org/cuba-platform/jpawebapi)
[![Documentation](https://img.shields.io/badge/documentation-online-03a9f4.svg)](https://github.com/cuba-platform/jpawebapi/wiki)

## Overview

The universal JPA web API enables to load and save any entities
defined in the application data model by sending simple HTTP requests. 

This provides an easy way to integrate with a wide range of third-party 
applications – from the JavaScript code executed in the browser to arbitrary 
systems running on Java, .NET, PHP or any other platform.

Key API features:

- Loading entity instances from the database by identifier or by JPQL query with parameters.
- Saving new and modified instances, deleting instances.
- Obtaining a description of the data model in HTML format.
- Data representation in JSON and XML formats.
- Middleware service calls.
- User authentication.

Read addon Wiki for an additional information: [link](https://github.com/cuba-platform/jpawebapi/wiki).

## Compatibility with platform versions

| Add-on        | Platform      |
|:------------- |:------------- |
| 1.0.0  | 7.1.x  |

## Installation

To install the component in your project, do the following steps:

1. Open **Project -> Properties** in the project tree.

2. On the **App components** pane click the **Plus** button next to **Custom components**.

3. Paste the addon coordinates in the corresponding field:

    `com.haulmont.addon.jpawebapi:jpawebapi-global:1.0.0`

4. Click **OK** to save the project properties.

After that the JPA Web API functionality will be available at:

- `{host:port}/app/dispatch/api/*` - for Web module
- `{host:port}/app-portal/api/*` - for Portal module

## Customization

JPA Web API URL can be customized via `jpawebapi.mapping.url` app property
both for `web` and `portal` application modules.

## Demo

1. Login as `admin / admin` with `GET` request:

```
http://localhost:8080/app/dispatch/api/login?u=admin&p=admin&l=ru
```

2. Load `Users` list using auth token:

```
http://localhost:8080/app/dispatch/api/query.json?e=sec$Role&q=select+r+from+sec$Role+r&s=b376f1d2-15d7-ea33-4048-2199de9721e7
```

![demo](./img/demo.jpg)
