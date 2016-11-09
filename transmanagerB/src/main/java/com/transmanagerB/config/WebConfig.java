package com.transmanagerB.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.transmanagerB.logger.LoggerInterceptor;

/**
 * http://www.namooz.com/2015/11/10/spring-boot-thymeleaf-4-reload-contents-without-restarting-tomcat-server/
 * @author user
 *
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
	
	@Value("${activemq.name}")
	public String activemqName;

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
			"classpath:/META-INF/resources/", "classpath:/resources/",
			"classpath:/static/", "classpath:/public/" };
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(new LoggerInterceptor());
    	super.addInterceptors(registry);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	if (!registry.hasMappingForPattern("/**")) {
    		System.out.println("### addResourceHandlers start");
    		registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    		System.out.println("### addResourceHandlers end");
    	}
    	// media 재생시 사용 pro ffmpeg.destpath
    	registry.addResourceHandler("/home/tvingadmin/ffmpeg/out/**").addResourceLocations("file:///home/tvingadmin/ffmpeg/out/");    	
    	super.addResourceHandlers(registry);
    }
 
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
	 
}
