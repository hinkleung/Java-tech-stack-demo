package com.bfxy.esjob.config;

import org.springframework.context.annotation.Bean;

import com.bfxy.esjob.annotation.JobTraceInterceptor;

//@Configuration
public class TraceJobConfiguration {

	@Bean
	public JobTraceInterceptor jobTraceInterceptor() {
		System.err.println("init --------------->");
		return new JobTraceInterceptor();
	}
	
}
