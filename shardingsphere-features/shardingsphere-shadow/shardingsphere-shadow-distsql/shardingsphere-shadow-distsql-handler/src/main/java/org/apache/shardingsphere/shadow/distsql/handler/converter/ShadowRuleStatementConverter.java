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

package org.apache.shardingsphere.shadow.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Shadow rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRuleStatementConverter {
    
    /**
     * Convert shadow rule segments to shadow rule configuration.
     *
     * @param rules shadow rule statements
     * @return shadow rule configuration
     */
    public static ShadowRuleConfiguration convert(final Collection<ShadowRuleSegment> rules) {
        // FIXME because the defined final attribute will be removed, here is just for the new object
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("removed", Collections.singletonList("removed"), Collections.singletonList("removed"));
        shadowRuleConfiguration.setShadowAlgorithms(getShadowAlgorithms(rules));
        shadowRuleConfiguration.setDataSources(getDataSource(rules));
        shadowRuleConfiguration.setTables(getTables(rules));
        return shadowRuleConfiguration;
    }
    
    private static Map<String, ShadowTableConfiguration> getTables(final Collection<ShadowRuleSegment> rules) {
        return rules.stream().flatMap(each -> each.getShadowTableRules().entrySet().stream()).collect(Collectors.toMap(Entry::getKey, ShadowRuleStatementConverter::buildShadowTableConfiguration));
    }
    
    private static ShadowTableConfiguration buildShadowTableConfiguration(final Entry<String, Collection<ShadowAlgorithmSegment>> entry) {
        return new ShadowTableConfiguration(entry.getValue().stream().map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList()));
    }
    
    private static Map<String, ShadowDataSourceConfiguration> getDataSource(final Collection<ShadowRuleSegment> rules) {
        return rules.stream().collect(Collectors.toMap(ShadowRuleSegment::getRuleName, each -> new ShadowDataSourceConfiguration(each.getSource(), each.getShadow())));
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> getShadowAlgorithms(final Collection<ShadowRuleSegment> rules) {
        return rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .collect(Collectors.toMap(ShadowAlgorithmSegment::getAlgorithmName, each -> new ShardingSphereAlgorithmConfiguration(each.getAlgorithmName(), each.getAlgorithmSegment().getProps())));
    }
}
