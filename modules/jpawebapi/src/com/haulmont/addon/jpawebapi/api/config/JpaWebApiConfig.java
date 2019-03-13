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

package com.haulmont.addon.jpawebapi.api.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInt;

/**
 * JPA WEB API config.
 */
@Source(type = SourceType.APP)
public interface JpaWebApiConfig extends Config {

    @Property("cuba.trustedClientPassword")
    String getTrustedClientPassword();

    @Property("cuba.rest.productionMode")
    @DefaultBoolean(false)
    boolean getProductionMode();

    /**
     * @return api version which used for initialization of
     * {@link com.haulmont.addon.jpawebapi.api.converter.ConversionFactory}
     */
    @Property("cuba.rest.apiVersion")
    @DefaultInt(2)
    int getRestApiVersion();

    /**
     * Default mappings are:
     * <ul>
     *     <li>
     *         {@code ${app_context}/dispatch/api/${apiPath}} - for Web module
     *     </li>
     *     <li>
     *         {@code ${portal_context}/api/${apiPath}} - for Portal module
     *     </li>
     * </ul>
     *
     * @return path which used for mapping with 'jpawebapi' servlet
     */
    @Property("jpawebapi.mapping.url")
    String getServletMapping();
}
