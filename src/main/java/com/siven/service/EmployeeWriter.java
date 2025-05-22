package com.siven.service;

import com.siven.entity.Employee;
import com.siven.repository.EmployeeRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@StepScope
public class EmployeeWriter implements ItemWriter<Employee> {

    @Autowired
    private EmployeeRepository repository;

    @Override
    public void write(Chunk<? extends Employee> chunk) throws Exception {
        // Convert chunk to List and save
        repository.saveAll(chunk.getItems());
    }
}
