package com.example.countries;

import com.isomorphic.servlet.IDACall;
import com.isomorphic.servlet.DataSourceLoader;
import jakarta.servlet.Servlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CountriesApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CountriesApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean<Servlet> idaCallServlet() {
        ServletRegistrationBean<Servlet> bean = new ServletRegistrationBean<>(new IDACall(), "/isomorphic/IDACall/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<Servlet> dataSourceLoaderServlet() {
        ServletRegistrationBean<Servlet> bean = new ServletRegistrationBean<>(new DataSourceLoader(), "/isomorphic/DataSourceLoader/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
