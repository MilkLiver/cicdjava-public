package com.milkliver.deploytest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = { "com.milkliver.deploytest", "scheduleds", "utils" })
@EnableScheduling
public class MainConfiguration {

}
