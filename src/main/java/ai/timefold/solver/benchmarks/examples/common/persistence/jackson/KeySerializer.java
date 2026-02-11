package ai.timefold.solver.benchmarks.examples.common.persistence.jackson;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializes a child of {@link AbstractPersistable} to a JSON map key
 * using {@link JacksonUniqueIdGenerator}.
 *
 * @param <E> The type must have a {@link com.fasterxml.jackson.annotation.JsonIdentityInfo} annotation with
 *        {@link JacksonUniqueIdGenerator} as its generator.
 */
public final class KeySerializer<E extends AbstractPersistable> extends ValueSerializer<E> {

    private final ObjectIdGenerator<String> idGenerator = new JacksonUniqueIdGenerator();

    @Override
    public void serialize(E persistable, JsonGenerator jsonGenerator, SerializationContext ctxt) throws JacksonException {
        Object jsonId = ctxt.findObjectId(persistable, idGenerator)
                .generateId(persistable);
        jsonGenerator.writeName(jsonId.toString());
    }
}
