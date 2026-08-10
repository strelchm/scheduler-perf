package ru.strelchm.scheduler_perf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SchedulerPerfApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchedulerPerfApplication.class, args);
	}

}
