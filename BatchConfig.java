package com.techprimers.security.jwtsecurity.config;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.tap.task.MyTaskOne;
import com.tap.task.MyTaskThree;
import com.tap.task.MyTaskTwo;

@Configuration
@EnableBatchProcessing
@DisallowConcurrentExecution
@Import(AdditionalBatchConfiguration.class)
public class BatchConfig {
   
    @Autowired
    private JobBuilderFactory jobs;
 
    @Autowired
    private StepBuilderFactory steps;
     
    @Bean
    public Step stepOne(){
        return steps.get("stepOne")
                .tasklet(new MyTaskOne())
                .build();
    }
     
    @Bean
    public Step stepTwo(){
        return steps.get("stepTwo")
                .tasklet(new MyTaskTwo())
                .build();
    }  
     
    @Bean
    public Step stepThree(){
        return steps.get("stepThree")
                .tasklet(new MyTaskThree())
                .build();
    }
    
    @Bean
    @Primary
    public Job demoJob(){
        return jobs.get("demoJob")
                .incrementer(new RunIdIncrementer())
                .start(stepOne())
                .next(stepTwo())
                .build();
    }
    
    @Bean
    public Job demoJob1(){
        return jobs.get("demoJob1")
                .incrementer(new RunIdIncrementer())
                .start(stepTwo())
                //.next(stepTwo())
                .build();
    }
    
    
    @Bean
    public Job PartitionJob() {
      return jobs.get("PartitionJob")
    		  .incrementer(new RunIdIncrementer())
          .start(masterStep()).on("FAILED").end() //.to(stepThree())
          .from(masterStep()).on("*").to(stepOne())
          
           .from(stepOne()).on("FAILED").end()
           .end()
           .build();
    }
    
    @Bean
    public Step masterStep() {
      return steps.get("masterStep").partitioner(stepTwo().getName(), rangePartitioner())
          .partitionHandler(masterSlaveHandler()).build();
    }

    @Bean
    public PartitionHandler masterSlaveHandler() {
      TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
      handler.setGridSize(10);
      handler.setTaskExecutor(taskExecutor());
      handler.setStep(stepTwo());
      try {
        handler.afterPropertiesSet();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return handler;
    }
    
    @Bean
    public RangePartitioner rangePartitioner() {
      return new RangePartitioner();
    }
    
    @Bean
    public SimpleAsyncTaskExecutor taskExecutor() {
      return new SimpleAsyncTaskExecutor();
    }
    
}