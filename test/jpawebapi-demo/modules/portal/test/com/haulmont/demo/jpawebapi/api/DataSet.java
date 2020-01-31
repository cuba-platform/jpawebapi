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

package com.haulmont.demo.jpawebapi.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DataSet {

    private List<UUID> idPool = new ArrayList<>();

    public UUID createEntityId() {
        UUID result = UUID.randomUUID();
        addEntityId(result);
        return result;
    }

    public void addEntityId(UUID uuid) {
        if (uuid != null)
            idPool.add(uuid);
    }

    public List<UUID> getIdPool() {
        return idPool;
    }

    public void evictFromPool(UUID id) {
        idPool.remove(id);
    }

    public void cleanup(Connection conn) throws SQLException {
        deleteInstances(conn, "JPADEMO_DRIVER", idPool);
    }

    private void deleteInstances(Connection conn, String tableName, Collection<UUID> ids) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from " + tableName + " where id = ?");
        try {
            for (UUID uuid : ids) {
                String param = uuid.toString();
                stmt.setString(1, param);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

}
