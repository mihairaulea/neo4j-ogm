/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session.request.strategy;

import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.session.Utils;

import java.util.Collection;

/**
 * @author Luanne Misquitta
 */
public class DeleteRelationshipStatements implements DeleteStatements {
    public Statement delete(Long id) {
        return new CypherQuery("MATCH (n)-[r]->() WHERE ID(r) = { id } DELETE r", Utils.map("id", id));
    }

    public Statement deleteAll(Collection<Long> ids) {
        return new CypherQuery("MATCH (n)-[r]->() WHERE id(r) in { ids } DELETE r", Utils.map("ids", ids));
    }

    public Statement purge() {
        return new CypherQuery("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map());
    }

    public Statement deleteByType(String type) {
        return new CypherQuery(String.format("MATCH (n)-[r:`%s`]-() DELETE r", type), Utils.map());
    }
}
