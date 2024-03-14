package ai.timefold.solver.benchmarks.examples.taskassigning.domain;

import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.AbstractKeyDeserializer;

final class CustomerKeyDeserializer extends AbstractKeyDeserializer<Customer> {

    public CustomerKeyDeserializer() {
        super(Customer.class);
    }

    @Override
    protected Customer createInstance(long id) {
        return new Customer(id);
    }
}
