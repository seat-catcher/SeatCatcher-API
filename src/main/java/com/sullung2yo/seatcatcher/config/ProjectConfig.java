package com.sullung2yo.seatcatcher.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = "com.sullung2yo.seatcatcher")
@RequiredArgsConstructor
@EnableScheduling
public class ProjectConfig {
}
