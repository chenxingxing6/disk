package com.netpan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
@EnableAutoConfiguration
@ImportResource({"classpath:*.xml"})
@Configuration
public class DiskApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DiskApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DiskApplication.class);
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> servletServletRegistrationBean = new ServletRegistrationBean<>(dispatcherServlet);
        servletServletRegistrationBean.addUrlMappings("*.do");
        return servletServletRegistrationBean;
    }
}

