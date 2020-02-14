package com.xxl.sso.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author xuxueli 2018-04-08 21:49:41
 */
@SpringBootApplication
public class XxlClientApplication {

	public static void main(String[] args) {
        SpringApplication.run(XxlClientApplication.class, args);
	}
}