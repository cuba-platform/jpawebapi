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

package com.haulmont.addon.jpawebapi.api;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.sys.AppContext;
import org.apache.commons.text.StringTokenizer;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class holds an information about services that allowed to be invoked with REST API.
 * Configuration is loaded from {@code *-rest-services.xml} files.
 */
@Component(RestServicePermissions.NAME)
public class RestServicePermissions {

    public static final String NAME = "jpawebapi_RestServicePermissions";

    public static final String CUBA_REST_SERVICES_CONFIG_PROP_NAME = "cuba.restServicesConfig";

    private static final Logger log = LoggerFactory.getLogger(RestServicePermissions.class);

    @Inject
    protected Resources resources;

    protected Map<String, Set<String>> serviceMethods = new ConcurrentHashMap<>();

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Checks whether method of service is allowed to be invoked with REST API
     */
    public boolean isNotPermitted(String serviceName, String methodName) {
        lock.readLock().lock();
        try {
            checkInitialized();
            Set<String> methods = serviceMethods.get(serviceName);
            return methods == null || !methods.contains(methodName);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        String configName = AppContext.getProperty(CUBA_REST_SERVICES_CONFIG_PROP_NAME);
        StringTokenizer tokenizer = new StringTokenizer(configName);
        for (String location : tokenizer.getTokenArray()) {
            Resource resource = resources.getResource(location);
            if (resource.exists()) {
                try (InputStream stream = resource.getInputStream()) {
                    loadConfig(Dom4j.readDocument(stream).getRootElement());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to initialize REST Service Permissions", e);
                }
            } else {
                log.warn("Resource " + location + " not found, ignore it");
            }
        }
    }

    protected void loadConfig(Element rootElem) {
        for (Element serviceElem : rootElem.elements("service")) {
            String serviceName = serviceElem.attributeValue("name");

            for (Element methodElem : serviceElem.elements("method")) {
                String methodName = methodElem.attributeValue("name");
                addServiceMethod(serviceName, methodName);
            }
        }
    }

    protected void addServiceMethod(String serviceName, String methodName) {
        Set<String> methods = serviceMethods.computeIfAbsent(serviceName, k -> new HashSet<>());
        methods.add(methodName);
    }

    public List<ServiceInfo> getServiceInfos() {
        lock.readLock().lock();
        try {
            checkInitialized();
            List<ServiceInfo> infos = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : serviceMethods.entrySet()) {
                infos.add(new ServiceInfo(entry.getKey(), new HashSet<>(entry.getValue())));
            }
            return infos;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    public ServiceInfo getServiceInfo(String serviceName) {
        lock.readLock().lock();
        try {
            checkInitialized();
            Set<String> methods = serviceMethods.get(serviceName);
            if (methods == null) {
                return null;
            }
            return new ServiceInfo(serviceName, methods);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Class stores an information about the service permitted for REST API
     */
    public static class ServiceInfo {

        protected String service;
        protected Set<String> methods;

        public ServiceInfo(String service, Set<String> methods) {
            this.service = service;
            this.methods = methods;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public Set<String> getMethods() {
            return methods;
        }

        public void setMethods(Set<String> methods) {
            this.methods = methods;
        }
    }
}
