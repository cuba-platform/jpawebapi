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

package com.haulmont.addon.jpawebapi.api.controller;

import com.haulmont.addon.jpawebapi.api.config.JpaWebApiConfig;
import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileTypesHelper;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.core.sys.remoting.discovery.ServerSelector;
import com.haulmont.cuba.security.app.UserSessionService;
import com.haulmont.cuba.security.global.NoUserSessionException;
import com.haulmont.cuba.security.global.UserSession;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Handles file download requests to the portal client.
 * <br> This controller is deployed in Spring context defined by {@code cuba.dispatcherSpringContextConfig}
 * app property.
 */
@Controller
public class RestFileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(RestFileDownloadController.class);

    @Inject
    protected DataService dataService;

    @Inject
    protected UserSessionService userSessionService;

    @Resource(name = ServerSelector.NAME)
    protected ServerSelector serverSelector;

    @Inject
    protected ClientConfig clientConfig;

    @Inject
    protected JpaWebApiConfig jpaWebApiConfig;

    protected String fileDownloadContext;

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ModelAndView download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserSession userSession = getSession(request, response);
        if (userSession == null) {
            error(response);
            return null;
        }

        AppContext.setSecurityContext(new SecurityContext(userSession));
        try {
            UUID fileId;
            try {
                fileId = UUID.fromString(request.getParameter("f"));
            } catch (Exception e) {
                log.error(e.toString());
                error(response);
                return null;
            }

            FileDescriptor fd = dataService.load(new LoadContext<>(FileDescriptor.class).setId(fileId));
            if (fd == null) {
                log.warn("Unable to find file with id " + fileId);
                error(response);
                return null;
            }

            String fileName = URLEncodeUtils.encodeUtf8(fd.getName());

            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Type", getContentType(fd));
            response.setHeader("Pragma", "no-cache");

            boolean attach = Boolean.valueOf(request.getParameter("a"));

            attach = resolveAttachmentValue(attach, fd);

            response.setHeader("Content-Disposition", (attach ? "attachment" : "inline")
                    + "; filename=" + fileName);

            writeResponse(response, userSession, fd);

        } finally {
            AppContext.setSecurityContext(null);
        }
        return null;
    }

    protected void writeResponse(HttpServletResponse response, UserSession userSession, FileDescriptor fd)
            throws IOException {

        if (fileDownloadContext == null) {
            fileDownloadContext = clientConfig.getFileDownloadContext();
        }

        InputStream is = null;
        ServletOutputStream os = response.getOutputStream();
        try {
            Object context = serverSelector.initContext();
            String selectedUrl = serverSelector.getUrl(context);
            if (selectedUrl == null) {
                log.debug("Unable to download file: no available server URLs");
                error(response);
            }
            while (selectedUrl != null) {
                String url = selectedUrl + fileDownloadContext
                        + "?s=" + userSession.getId()
                        + "&f=" + fd.getId().toString();

                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);

                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    int httpStatus = httpResponse.getStatusLine().getStatusCode();
                    if (httpStatus == HttpStatus.SC_OK) {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        if (httpEntity != null) {
                            is = httpEntity.getContent();
                            IOUtils.copy(is, os);
                            os.flush();
                            break;
                        } else {
                            log.debug("Unable to download file from " + url + "\nHttpEntity is null");
                            selectedUrl = failAndGetNextUrl(context, response);
                        }
                    } else {
                        log.debug("Unable to download file from " + url + "\n" + httpResponse.getStatusLine());
                        selectedUrl = failAndGetNextUrl(context, response);
                    }
                } catch (IOException ex) {
                    log.debug("Unable to download file from " + url + "\n" + ex);
                    selectedUrl = failAndGetNextUrl(context, response);
                } finally {
                    IOUtils.closeQuietly(is);
                    httpClient.getConnectionManager().shutdown();
                }
            }
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    protected boolean resolveAttachmentValue(Boolean attachmentRequestParameterValue, FileDescriptor fileDescriptor) {
        if (BooleanUtils.isTrue(attachmentRequestParameterValue)) {
            return true;
        }

        String extension = fileDescriptor.getExtension();
        if (StringUtils.isEmpty(extension)) {
            // No extension - just download
            return true;
        } else {
            // Check if file is allowed to be opened inline
            List<String> inlineEnabledFileExtensions = jpaWebApiConfig.getInlineEnabledFileExtensions();
            return !inlineEnabledFileExtensions.contains(StringUtils.lowerCase(extension));
        }
    }

    @Nullable
    protected String failAndGetNextUrl(Object context, HttpServletResponse response) throws IOException {
        serverSelector.fail(context);
        String url = serverSelector.getUrl(context);
        if (url != null)
            log.debug("Trying next URL");
        else
            error(response);
        return url;
    }

    protected UserSession getSession(HttpServletRequest request, HttpServletResponse response) {
        UUID sessionId;
        try {
            sessionId = UUID.fromString(request.getParameter("s"));
        } catch (Exception e) {
            return null;
        }

        AppContext.setSecurityContext(new SecurityContext(sessionId));
        try {
            return userSessionService.getUserSession(sessionId);
        } catch (NoUserSessionException e) {
            return null;
        } finally {
            AppContext.setSecurityContext(null);
        }
    }

    protected String getContentType(FileDescriptor fd) {
        if (StringUtils.isEmpty(fd.getExtension())) {
            return FileTypesHelper.DEFAULT_MIME_TYPE;
        }

        return FileTypesHelper.getMIMEType("." + fd.getExtension().toLowerCase());
    }

    protected void error(HttpServletResponse response) throws IOException {
        if (!response.isCommitted())
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
