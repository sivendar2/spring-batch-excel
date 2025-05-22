package com.siven.controller;

import com.siven.entity.Employee;
import com.siven.repository.EmployeeRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job importEmployeeJob;

    public JobController(JobLauncher jobLauncher, Job importEmployeeJob) {
        this.jobLauncher = jobLauncher;
        this.importEmployeeJob = importEmployeeJob;
    }

    @PostMapping("/import-employees")
    public String runJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis()) // Make it unique each time
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(importEmployeeJob, params);
            return "Job started with status: " + execution.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
            return "Job failed: " + e.getMessage();
        }
    }

    @Autowired
    private EmployeeRepository repository;

    @GetMapping
    public List<Employee> getAll() {
        List<Employee> empList =repository.findAll();
        return empList;
    }


    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getAllEmployees(Pageable pageable) {
        Page<Employee> page = repository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("employees", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("pageSize", page.getSize());

        return ResponseEntity.ok(response);
    }


}
