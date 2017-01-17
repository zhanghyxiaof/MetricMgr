package com.vmware.metricprocessor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.vmware.metricprocessor.controller.AdapterController;
import com.vmware.metricprocessor.controller.MetricController;

/**
 * This is the main class to start metric processor project
 * @author hy
 *
 */
////@RestController
@ComponentScan(basePackages={"com.vmware.metricprocessor"})
@EnableAutoConfiguration
public class ProcessorMainStarter implements CommandLineRunner {
	
	@Autowired
	private MetricController metricController;
	@Autowired
	private AdapterController adapterController;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ProcessorMainStarter.class, args);

	}

	public void run(String... args) throws Exception {
		System.out.println("start mongo test......");
		metricController.deleteAllMetrics();
		System.out.println("test get metric():");
		System.out.println("-------------------------------");
		System.out.println();
		
		metricController.loadDescribeFile("V4V", "6.4", "./src/main/resources/describe.xml");
		
		System.out.println("start to add adapters");
		adapterController.deleteAllAdapters();
		adapterController.addTestAdapterData();
		System.out.println("add adapters successfully");
	}

	/**
	 * DB connection Factory.
	 * It is a workaround to use Mongo 3.4
	 * 
	 * @return a ready to use MongoDbFactory
	 */
	@Bean
	public MongoDbFactory mongoDbFactory() throws Exception {

		// Set credentials
		// MongoCredential credential = MongoCredential("admin", "test",
		// "admin".toCharArray());
		// ServerAddress serverAddress = new ServerAddress("localhost", 27017);

		// Mongo Client
		MongoClient mongoClient = new MongoClient("localhost", 27017);

		// Mongo DB Factory
		SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongoClient, "MetricDB");

		return simpleMongoDbFactory;
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongoDbFactory());
	}

}
