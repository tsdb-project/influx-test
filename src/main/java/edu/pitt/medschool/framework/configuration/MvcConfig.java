/**
 * 
 */
package edu.pitt.medschool.framework.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import edu.pitt.medschool.framework.interceptor.HttpHandlerInterceptor;

/**
 * @author Isolachine
 *
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    HttpHandlerInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("user/login");
    }
}
