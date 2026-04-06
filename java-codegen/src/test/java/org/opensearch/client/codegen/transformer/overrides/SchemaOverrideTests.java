/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.client.codegen.transformer.overrides;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opensearch.client.codegen.openapi.JsonPointer;
import org.opensearch.client.codegen.openapi.OpenApiSchema;

public class SchemaOverrideTests {

    @Test
    public void extendsSchema_notSet_returnsEmpty() {
        var override = SchemaOverride.builder().build();
        assertFalse(override.getExtendsSchema().isPresent());
    }

    @Test
    public void extendsSchema_set_returnsPointer() {
        var pointer = OpenApiSchema.COMPONENTS_SCHEMAS.append("_common.aggregations___AggregateBase");
        var override = SchemaOverride.builder().withExtendsSchema(pointer).build();

        assertTrue(override.getExtendsSchema().isPresent());
        assertEquals(pointer, override.getExtendsSchema().get());
    }

    @Test
    public void extendsSchema_doesNotAffectOtherFields() {
        var pointer = OpenApiSchema.COMPONENTS_SCHEMAS.append("_common.aggregations___AggregateBase");
        var override = SchemaOverride.builder()
            .withExtendsSchema(pointer)
            .withClassName("MyClass")
            .build();

        assertEquals(pointer, override.getExtendsSchema().get());
        assertEquals("MyClass", override.getClassName().orElseThrow());
        assertFalse(override.getMappedType().isPresent());
    }

    @Test
    public void extendsSchema_setToNull_returnsEmpty() {
        var override = SchemaOverride.builder().withExtendsSchema(null).build();
        assertFalse(override.getExtendsSchema().isPresent());
    }

    @Test
    public void overrides_aggregateBaseIntermediateTypes_allHaveExtendsSchema() {
        var aggregateBase = OpenApiSchema.COMPONENTS_SCHEMAS.append(
            "_common.aggregations" + OpenApiSchema.NAMESPACE_NAME_SEPARATOR + "AggregateBase"
        );

        String[] intermediateTypes = {
            "PercentilesAggregateBase",
            "SingleMetricAggregateBase",
            "StatsAggregateBase",
            "MultiBucketAggregateBase",
            "SingleBucketAggregateBase" };

        for (var typeName : intermediateTypes) {
            var pointer = OpenApiSchema.COMPONENTS_SCHEMAS.append(
                "_common.aggregations" + OpenApiSchema.NAMESPACE_NAME_SEPARATOR + typeName
            );
            var override = Overrides.OVERRIDES.getSchema(pointer);

            assertTrue(override.isPresent(), typeName + " should have an override");
            assertTrue(
                override.get().getExtendsSchema().isPresent(),
                typeName + " override should have extendsSchema set"
            );
            assertEquals(
                aggregateBase,
                override.get().getExtendsSchema().get(),
                typeName + " should extend AggregateBase"
            );
        }
    }
}
