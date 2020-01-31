/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.demo.jpawebapi.core.app;

import com.haulmont.cuba.security.entity.User;
import com.haulmont.demo.jpawebapi.core.entity.Driver;

import java.util.List;
import java.util.UUID;

/**
 * Service is used in functional tests
 */
public interface PortalTestService {

    String NAME = "demo_PortalTestService";

    void emptyMethod();

    Driver findEntityById(UUID id);

    List<Driver> finAllEntities();

    List<User> finAllUsers();

    Driver updateFirstName(UUID id, String newFirstName);

    List<Driver> updateFirstNames(List<Driver> entities, String newFirstName);

}