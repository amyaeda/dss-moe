package com.iris.dss;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;

import com.iris.dss.schedulers.EmailTaskScheduler;
import com.iris.dss.service.LoginAttemptService;

@EnableAutoConfiguration
@EnableScheduling
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DssMoeApplication extends SpringBootServletInitializer {
	
	private long maxUploadSizeInMb = 30L * 1024L * 1024L; // 10 MB
	
	public static void main(String[] args) {
		SpringApplication.run(DssMoeApplication.class, args);
	}
	
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(maxUploadSizeInMb));
        factory.setMaxRequestSize(DataSize.ofBytes(maxUploadSizeInMb));
        return factory.createMultipartConfig();
    }
    
    @Bean
    EmailTaskScheduler emailScheduler() {
    	EmailTaskScheduler scheduler = new EmailTaskScheduler();
    	return scheduler;
    }    
    
	@Value("${dss.account.locked.duration}")
	private long lockedDuration;
	@Value("${dss.login.max.attempt}")
	private int maxAttempt;
		
	@Bean
	LoginAttemptService loginAttemptService() {
		LoginAttemptService loginAttemptService = new LoginAttemptService();
		loginAttemptService.init(lockedDuration, maxAttempt);
		return loginAttemptService;
	}

}
