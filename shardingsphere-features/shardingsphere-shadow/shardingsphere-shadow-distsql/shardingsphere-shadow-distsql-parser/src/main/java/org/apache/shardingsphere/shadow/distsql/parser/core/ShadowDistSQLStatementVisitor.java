/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shadow.distsql.parser.core;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.AlgorithmPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CreateShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShadowAlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShadowRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShadowTableRuleContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for shadow dist SQL.
 */
public final class ShadowDistSQLStatementVisitor extends ShadowDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    @Override
    public ASTNode visitCreateShadowRule(final CreateShadowRuleContext ctx) {
        List<ShadowRuleSegment> shadowRuleSegments = ctx.shadowRuleDefinition().stream().map(this::visit).map(each -> (ShadowRuleSegment) each).collect(Collectors.toList());
        return new CreateShadowRuleStatement(shadowRuleSegments);
    }
    
    @Override
    public ASTNode visitShadowRuleDefinition(final ShadowRuleDefinitionContext ctx) {
        Map<String, Collection<ShadowAlgorithmSegment>> shadowAlgorithms = ctx.shadowTableRule().stream()
                .collect(Collectors.toMap(each -> getText(each.tableName()), each -> visitShadowAlgorithms(each.shadowAlgorithmDefinition())));
        return new ShadowRuleSegment(getText(ctx.ruleName()), getText(ctx.source()), getText(ctx.shadow()), shadowAlgorithms);
    }
    
    @Override
    public ASTNode visitShadowAlgorithmDefinition(final ShadowAlgorithmDefinitionContext ctx) {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment(getText(ctx.shadowAlgorithmType()), getAlgorithmProperties(ctx.algorithmProperties()));
        String algorithmName = null != ctx.algorithmName() ? getText(ctx.algorithmName()) : createAlgorithmName(getText(((ShadowTableRuleContext) ctx.getParent()).tableName()), algorithmSegment);
        return new ShadowAlgorithmSegment(algorithmName, algorithmSegment);
    }
    
    private Properties getAlgorithmProperties(final AlgorithmPropertiesContext ctx) {
        Properties result = new Properties();
        ctx.algorithmProperty().forEach(each -> result.put(new IdentifierValue(each.key.getText()).getValue(), new StringLiteralValue(each.value.getText()).getValue()));
        return result;
    }
    
    private static String getText(final ParserRuleContext ctx) {
        return new IdentifierValue(ctx.getText()).getValue();
    }
    
    private Collection<ShadowAlgorithmSegment> visitShadowAlgorithms(final List<ShadowAlgorithmDefinitionContext> ctxs) {
        return ctxs.stream().map(this::visit).map(each -> (ShadowAlgorithmSegment) each).collect(Collectors.toList());
    }
    
    private String createAlgorithmName(final String tableName, final AlgorithmSegment algorithmSegment) {
        return (tableName + "_" + algorithmSegment.getName()).toLowerCase();
    }
}
