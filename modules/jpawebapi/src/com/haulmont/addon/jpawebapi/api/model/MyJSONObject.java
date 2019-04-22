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

package com.haulmont.addon.jpawebapi.api.model;

import com.google.common.base.Strings;

import java.io.PrintWriter;
import java.util.*;

public class MyJSONObject implements MyJSON {

    protected final String _id;
    protected final boolean _ref;
    protected final Map<String, Object> _values;

    public MyJSONObject() {
        this(null, false);
    }

    public MyJSONObject(Object id, boolean ref) {
        _id = id != null ? id.toString() : null;
        _ref = ref;
        _values = new LinkedHashMap<>();
    }

    public void set(String key, Object value) {
        _values.put(key, value);
    }

    public void write(PrintWriter writer) {
        writer.println(toString());
    }

    public String toString() {
        return asString(0).toString();
    }

    @Override
    public StringBuilder asString(int indent) {
        StringBuilder buf = new StringBuilder().append(OBJECT_START);
        if (!Strings.isNullOrEmpty(_id)) {
            buf.append(encodeField(_ref ? REF_MARKER : ID_MARKER, ior(), 0));
            if (_values.entrySet().size() > 0)
                buf.append(FIELD_SEPARATOR).append(NEWLINE);
        }
        if (_ref) {
            return buf.append(OBJECT_END);
        }
        StringBuilder tab = newIndent(indent + 1);
        int i = 0;
        for (Map.Entry<String, Object> e : _values.entrySet()) {
            buf.append(tab).append(encodeField(e.getKey(), e.getValue(), indent + 1));
            if (i++ < _values.entrySet().size() - 1)
                buf.append(FIELD_SEPARATOR).append(NEWLINE);
        }
        buf.append(NEWLINE)
                .append(newIndent(indent))
                .append(OBJECT_END);
        return buf;
    }

    protected static StringBuilder encodeField(String field, Object value, int indent) {
        return new StringBuilder()
                .append(quoteFieldName(field))
                .append(VALUE_SEPARATOR)
                .append(quoteFieldValue(value, indent));
    }

    protected static StringBuilder newIndent(int indent) {
        char[] tabs = new char[indent * 4];
        Arrays.fill(tabs, SPACE);
        return new StringBuilder().append(tabs);
    }

    String ior() {
        return _id;
    }

    protected static StringBuilder quoteFieldName(String s) {
        return new StringBuilder().append(QUOTE).append(s).append(QUOTE);
    }

    protected static StringBuilder quoteFieldValue(Object o, int indent) {
        if (o == null) return new StringBuilder(NULL_LITERAL);
        if (o instanceof Number) return new StringBuilder(o.toString());
        if (o instanceof MyJSON) return ((MyJSON) o).asString(indent);
        return quoted(o.toString());
    }

    protected static StringBuilder quoted(Object o) {
        if (o == null) return new StringBuilder(NULL_LITERAL);
        String escaped = o.toString().replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return new StringBuilder().append(QUOTE).append(escaped).append(QUOTE);
    }

    public static class Array implements MyJSON {

        protected List<Object> members = new ArrayList<>();

        public void add(Object o) {
            members.add(o);
        }

        public String toString() {
            return asString(0).toString();
        }

        @Override
        public StringBuilder asString(int indent) {
            StringBuilder buf = new StringBuilder().append(ARRAY_START);
            StringBuilder tab = MyJSONObject.newIndent(indent + 1);
            for (Object o : members) {
                if (buf.length() > 1) buf.append(MEMBER_SEPARATOR);
                buf.append(NEWLINE).append(tab);
                if (o instanceof MyJSON)
                    buf.append(((MyJSON) o).asString(indent + 1));
                else
                    buf.append(o);
            }
            buf.append(NEWLINE)
                    .append(MyJSONObject.newIndent(indent))
                    .append(ARRAY_END);

            return buf;
        }
    }

    public static class KVMap implements MyJSON {

        protected Map<Object, Object> _entries = new LinkedHashMap<>();

        public void put(Object k, Object v) {
            _entries.put(k, v);
        }

        public String toString() {
            return asString(0).toString();
        }

        @Override
        public StringBuilder asString(int indent) {
            StringBuilder buf = new StringBuilder().append(ARRAY_START);
            StringBuilder tab = MyJSONObject.newIndent(indent + 1);
            for (Map.Entry<Object, Object> e : _entries.entrySet()) {
                if (buf.length() > 1) buf.append(MEMBER_SEPARATOR);
                buf.append(NEWLINE).append(tab);
                Object key = e.getKey();
                if (key instanceof MyJSON) {
                    buf.append(((MyJSON) key).asString(indent + 1));
                } else {
                    buf.append(key);
                }
                buf.append(VALUE_SEPARATOR);
                Object value = e.getValue();
                if (value instanceof MyJSON) {
                    buf.append(((MyJSON) value).asString(indent + 2));
                } else {
                    buf.append(value);
                }

            }
            buf.append(NEWLINE)
                    .append(MyJSONObject.newIndent(indent))
                    .append(ARRAY_END);
            return buf;
        }
    }
}
