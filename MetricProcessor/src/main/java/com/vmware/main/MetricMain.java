package com.vmware.main;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.mongodb.MongoClient;

////@RestController
@Controller

@EnableAutoConfiguration
public class MetricMain implements CommandLineRunner {

	@Autowired
	private MetricRepository repository;
	
    @RequestMapping("/")
    String home() {
        return "Hello My World!";
    }

    //from application.properties and default value
  	@Value("${application.hello:Hello Angel}")
  	private String hello;

  	@RequestMapping("/helloRestJsp")
  	public String helloRestJsp(Map<String, Object> map) {
  		System.out.println("HelloController.helloJsp().hello=" + hello);
  		map.put("hello", hello);
  		return "helloJsp";
  	}
  	
  	@RequestMapping(value="helloJsp")
  	public String helloJsp(Map<String, Object> map) {
  		System.out.println("HelloController.helloJsp().hello=" + hello);
  		
  		map.put("hello", hello);
  		map.put("metricMap", generateMetricMap());
  		
  		return "helloJsp";
  	}
    
  	@RequestMapping(value="example")
  	public ModelAndView example(HttpServletRequest request) {
  		return new ModelAndView("index");
  	}
  	
    public static void main(String[] args) throws Exception {
        SpringApplication.run(MetricMain.class, args);
        
    }
    
    
    public void run(String... args) throws Exception {
		System.out.println("start mongo test......");
		repository.deleteAll();

		// save a couple of customers
		List<String> tagList = new ArrayList<String>();
		tagList.add("cpu");
		tagList.add("ui");
		repository.save(new Metric("V4V", "Session", "cpu", "cpu_usage", tagList));
		repository.save(new Metric("V4V", "Session", "cpu", "cpu_available", tagList));

		// fetch all customers
		System.out.println("test get metric():");
		System.out.println("-------------------------------");
		// repository.findMetric("V4V", "Session", "cpu", "cpu_usage")
		for (Metric metric : repository.findByAdapterKind("V4V")) {
			System.out.println(metric);
		}
		System.out.println();
		loadDescribeFile("test");
		List<Metric> metricList = repository.findByResourceKind("ViewPod");
		System.out.println(metricList.size());
		System.out.println(repository.findAll().size());
		for (int i=0; i<10; i++) {
			Metric metric = repository.findAll().get(i);
			System.out.println(metric);
		}

	}

	private synchronized String loadDescribeFile(String describeFile) {
		describeFile = "./src/main/resources/describe.xml";
		StringBuilder config = new StringBuilder();
		System.out.println(System.getProperty("user.dir"));

		try (FileInputStream stm = new FileInputStream(describeFile)) {
			Scanner scanner = new Scanner(new InputStreamReader(stm, "UTF-8"));
			try {
				String adapterKind = "";
				String resourceKind = "";
				String resourceGroup = "";
				String metricName = "";
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					String[] lineArr = line.trim().split(" ");
					if (lineArr.length > 1) {
						switch (lineArr[0]) {
						case "<AdapterKind":
							adapterKind = getKey(lineArr);
							break;
						case "<ResourceKind":
							resourceKind = getKey(lineArr);
							break;
						case "<ResourceGroup":
							resourceGroup = getKey(lineArr);
							break;
						case "<ResourceAttribute":
							System.out.println("insert metric...");
							metricName = getKey(lineArr);
							Metric metric = new Metric(adapterKind, resourceKind, resourceGroup, metricName, null);
							saveMetricToDB(metric);
							break;
						default:
							System.out.println("skip line...");
						}
					}
				}
				System.out.println("All metrics are loaded to database...");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				scanner.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(config.toString().substring(config.toString().length() - 100));
		return config.toString();
	}

	private void saveMetricToDB(Metric metric) {
		repository.save(metric);
	}
	private String getKey(String[] lineArr) {
		for (String element : lineArr) {
			String[] pair = element.split("=");
			if (pair.length > 1) {
				if (pair[0].equals("key")) {
					return pair[1].replace("\"", "");
				}
			}
		}
		return "";
	}
	private Map<String, List<Metric>> generateMetricMap() {
		Map<String, List<Metric>> map = new HashMap<>();
		List<Metric> metricList = repository.findAll();
		for (Metric metric : metricList) {
			for (String tag : metric.tagList) {
				if (map.get(tag) == null) {
					List<Metric> list = new ArrayList<>();
					map.put(tag, list);
				}
				map.get(tag).add(metric);
			}
		}
		return map;
	}
    /**
   	 * DB connection Factory
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
   		SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongoClient, "test");

   		return simpleMongoDbFactory;
   	}

   	@Bean
   	public MongoTemplate mongoTemplate() throws Exception {
   		return new MongoTemplate(mongoDbFactory());
   	}

}
