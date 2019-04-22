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

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetadataObject;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributes;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils;
import com.haulmont.cuba.core.entity.BaseGenericIdEntity;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;

import java.util.*;

public class ConverterHelper {

    public static final Comparator<MetaProperty> PROPERTY_COMPARATOR = Comparator.comparing(MetadataObject::getName);

    public static List<MetaProperty> getOrderedProperties(MetaClass metaClass) {
        List<MetaProperty> result = new ArrayList<>(metaClass.getProperties());
        result.sort(PROPERTY_COMPARATOR);
        return result;
    }

    public static List<MetaProperty> getActualMetaProperties(MetaClass metaClass, Entity entity) {
        List<MetaProperty> result = new ArrayList<>(metaClass.getProperties());
        if (entity instanceof BaseGenericIdEntity
                && ((BaseGenericIdEntity) entity).getDynamicAttributes() != null) {
            Collection<CategoryAttribute> dynamicAttributes
                    = AppBeans.get(DynamicAttributes.NAME, DynamicAttributes.class).getAttributesForMetaClass(metaClass);
            for (CategoryAttribute dynamicAttribute : dynamicAttributes) {
                result.add(DynamicAttributesUtils.getMetaPropertyPath(metaClass, dynamicAttribute).getMetaProperty());
            }
        }

        result.sort(PROPERTY_COMPARATOR);

        return result;
    }

    public static void fetchDynamicAttributes(Entity entity){
        if (entity instanceof BaseGenericIdEntity) {
            LoadContext<BaseGenericIdEntity> loadContext = new LoadContext<>(entity.getMetaClass());
            loadContext.setId(entity.getId()).setLoadDynamicAttributes(true);
            DataService dataService = AppBeans.get(DataService.NAME, DataService.class);
            BaseGenericIdEntity reloaded = dataService.load(loadContext);
            if (reloaded != null) {
                //noinspection unchecked
                ((BaseGenericIdEntity) entity).setDynamicAttributes(reloaded.getDynamicAttributes());
            } else {
                ((BaseGenericIdEntity) entity).setDynamicAttributes(new HashMap<>());
            }
        }
    }
}
