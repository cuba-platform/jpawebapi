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

package com.haulmont.addon.jpawebapi.sys;

import com.haulmont.addon.jpawebapi.api.config.JpaWebApiConfig;
import com.haulmont.cuba.core.sys.AbstractWebAppContextLoader;
import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.FrameworkServlet;

import javax.inject.Inject;
import javax.servlet.*;
import java.util.EnumSet;

@Component(JpaWebApiServletInitializer.NAME)
public class JpaWebApiServletInitializer {

    public static final String NAME = "jpawebapi_JpaWebApiServletInitializer";

    protected static final String SERVLET_NAME = "jpawebapi";

    @Inject
    protected ServletRegistrationManager servletRegistrationManager;
    @Inject
    protected JpaWebApiConfig jpaWebApiConfig;

    @EventListener
    protected void init(ServletContextInitializedEvent event) {
        String servletMapping = jpaWebApiConfig.getServletMapping();
        ApplicationContext appCtx = event.getApplicationContext();
        Servlet servlet = servletRegistrationManager.createServlet(appCtx,
                JpaWebApiDispatcherServlet.class.getName());

        ServletContext servletCtx = event.getSource();
        try {
            servlet.init(new AbstractWebAppContextLoader.CubaServletConfig(SERVLET_NAME, servletCtx));
        } catch (ServletException e) {
            throw new RuntimeException(
                    String.format("An error occurred while initializing '%s' servlet", SERVLET_NAME), e);
        }

        ServletRegistration.Dynamic servletRegistration = servletCtx.addServlet(SERVLET_NAME, servlet);
        servletRegistration.setLoadOnStartup(4);
        servletRegistration.addMapping(servletMapping);

        DelegatingFilterProxy springSecurityFilterChain = new DelegatingFilterProxy();
        springSecurityFilterChain.setContextAttribute(FrameworkServlet.SERVLET_CONTEXT_PREFIX
                + SERVLET_NAME);
        springSecurityFilterChain.setTargetBeanName("springSecurityFilterChain");

        FilterRegistration.Dynamic springSecurityFilterChainReg =
                servletCtx.addFilter("jpaWebApiSpringSecurityFilterChain", springSecurityFilterChain);

        springSecurityFilterChainReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),
                true, servletMapping);
    }
}
