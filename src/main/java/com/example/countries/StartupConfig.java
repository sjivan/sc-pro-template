package com.example.countries;

import com.isomorphic.base.ISCInit;
import com.isomorphic.base.Init;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class StartupConfig {

    @Bean
    public ServletContextInitializer servletInitializer() {
        return servletContext -> {
            Init initServlet = new Init();
            ServletConfig initConfig = new CustomServletConfig("Init", servletContext);
            try {
                initServlet.init(initConfig);
                ISCInit.go();
            } catch (ServletException e) {
                throw new RuntimeException("Failed to initialize Init servlet", e);
            }
        };
    }

    private static class CustomServletConfig implements ServletConfig {
        private final String servletName;
        private final ServletContext servletContext;
        private final Map<String, String> initParams;

        public CustomServletConfig(String servletName, ServletContext servletContext) {
            this.servletName = servletName;
            this.servletContext = servletContext;
            this.initParams = new HashMap<>();
        }

        @Override
        public String getServletName() {
            return servletName;
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }

        @Override
        public String getInitParameter(String name) {
            return initParams.get(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(initParams.keySet());
        }
    }
}
