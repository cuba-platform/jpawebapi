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

package com.haulmont.addon.jpawebapi.api.converter;

import com.haulmont.addon.jpawebapi.api.config.JpaWebApiConfig;
import com.haulmont.addon.jpawebapi.api.converter.impl.JSONConverter;
import com.haulmont.addon.jpawebapi.api.converter.impl.XMLConverter;
import org.springframework.stereotype.Component;

import javax.activation.MimeType;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConversionFactory {

    @Inject
    protected JSONConverter jsonConverter;

    @Inject
    protected XMLConverter xmlConverter;

    @Inject
    protected JpaWebApiConfig jpaWebApiConfig;

    protected List<Converter> converters = new ArrayList<>();

    protected int restApiVersion;

    @PostConstruct
    protected int init() {
        converters.add(jsonConverter);
        converters.add(xmlConverter);

        restApiVersion = jpaWebApiConfig.getRestApiVersion();

        return restApiVersion;
    }

    public Converter getConverter(MimeType requestedForm) {
        if (requestedForm != null) {
            for (Converter converter : getConverters()) {
                if (requestedForm.match(converter.getMimeType()) && converter.getApiVersions().contains(restApiVersion))
                    return converter;
            }
        }
        throw new RuntimeException("Converter not found");
    }

    protected List<Converter> getConverters() {
        return converters;
    }

    public Converter getConverter(String type) {
        for (Converter converter : getConverters()) {
            if (converter.getType().equals(type) && converter.getApiVersions().contains(restApiVersion))
                return converter;
        }
        throw new RuntimeException("Converter not found");
    }
}
