package com.siven.service;

import com.siven.entity.Employee;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class EmployeeProcessor implements ItemProcessor<Employee, Employee> {
    @Override
    public Employee process(Employee item) {
        item.setName(item.getName().toUpperCase());
        return item;
    }
}
