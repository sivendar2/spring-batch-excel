package com.siven.config;

import com.siven.entity.Employee;
import com.siven.partition.ExcelPartitioner;
import com.siven.service.EmployeeProcessor;
import com.siven.service.EmployeeWriter;
import com.siven.service.ExcelEmployeeReader;
import com.siven.service.PartitionedExcelEmployeeReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.SynchronizedItemWriter;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.SynchronizedItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }
    @Bean
    public Step slaveStep() {
        return new StepBuilder("slaveStep", jobRepository)
                .<Employee, Employee>chunk(100, transactionManager)
                .reader(partitionedReader())
                .processor(processor())
                .writer(writer()) // already thread-safe due to JPA batch insert
                .build();
    }

    @Bean
    public Step partitionedStep() {
        return new StepBuilder("partitionedStep", jobRepository)
                .partitioner(slaveStep().getName(), new ExcelPartitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(taskExecutor()) // multi-threaded partitioning
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);         // Number of threads
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);       // Task queue capacity
        executor.setThreadNamePrefix("batch-");
        executor.initialize();
        return executor;
    }
    public Step importStep() {
        return new StepBuilder("importStep", jobRepository)
                .<Employee, Employee>chunk(100, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(synchronizedWriter(writer()))  // 🔄 Use the synchronized writer
                .taskExecutor(taskExecutor())
                .throttleLimit(10)
                .build();
    }

    @Bean
    public SynchronizedItemWriter<Employee> synchronizedWriter(ItemWriter<Employee> delegateWriter) {
        return new SynchronizedItemWriter<>(delegateWriter);
    }

    @Bean
    public ExcelEmployeeReader reader() {
        return new ExcelEmployeeReader();
    }

    @Bean
    public EmployeeProcessor processor() {
        return new EmployeeProcessor();
    }

    @Bean
    public EmployeeWriter writer() {
        return new EmployeeWriter();
    }
    @Bean
    public Job importEmployeeJob() {
        return new JobBuilder("importEmployeeJob", jobRepository)
                .start(partitionedStep())
                .build();
    }

    @Bean
    @StepScope
    public PartitionedExcelEmployeeReader partitionedReader() {
        return new PartitionedExcelEmployeeReader();
    }
}
