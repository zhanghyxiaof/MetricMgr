package com.vmware.main;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

/**
 * This is the main class for the start and logic of metric processor project
 * @author hy
 *
 */
////@RestController
@Controller
@EnableAutoConfiguration
public class MetricMain implements CommandLineRunner {

	@Autowired
	private MetricRepository metricRepo;
	@Autowired
	private AdapterRepository adapterRepo;

	@RequestMapping("/")
	public final String home() {
		return "index";
	}

	// from application.properties and default value
	@Value("${application.welcomeStr:Welcome!}")
	private String welcomeStr;

	@RequestMapping(value = "helloJsp")
	public String helloJsp(Map<String, Object> map) {
		map.put("welcomeStr", welcomeStr);
		map.put("metricMap", generateMetricMap("V4V", "6.4"));

		return "helloJsp";
	}

	@RequestMapping(value = "example")
	public ModelAndView example(HttpServletRequest request) {
		return new ModelAndView("index");
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MetricMain.class, args);

	}

	public void run(String... args) throws Exception {
		System.out.println("start mongo test......");
		metricRepo.deleteAll();

		// fetch all customers
		System.out.println("test get metric():");
		System.out.println("-------------------------------");
		System.out.println();
		loadDescribeFile("V4V", "6.4", "./src/main/resources/describe.xml");
		List<Metric> metricList = metricRepo.findByResourceKind("ViewPod");
		System.out.println(metricList.size());
		System.out.println(metricRepo.findAll().size());
		for (int i = 0; i < 10; i++) {
			Metric metric = metricList.get(i);
			System.out.println(metric);
		}

	}

	/**
	 * Read the adapter describe file and save each metric as a structured object in mongo
	 * @param adapterKind the adapter kind which the describe file belongs to
	 * @param adapterVersion the version of the adapter
	 * @param describeFile the describeFile full name
	 */
	private synchronized void loadDescribeFile(String adapterKind, String adapterVersion, String describeFile) {
		//System.out.println(System.getProperty("user.dir"));
		try (FileInputStream stm = new FileInputStream(describeFile)) {
			Scanner scanner = new Scanner(new InputStreamReader(stm, "UTF-8"));
			try {
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
							// reset resource group for new resource kind since some metrics
							// do not have resource group
							resourceGroup = "";
							break;
						case "<ResourceGroup":
							resourceGroup = getKey(lineArr);
							break;
						case "<ResourceAttribute":
							System.out.println("insert metric...");
							metricName = getKey(lineArr);
							Metric metric = new Metric(adapterKind, resourceKind, resourceGroup, metricName, null);
							if (!resourceGroup.isEmpty()) {
								Map<String, Double> tagMap = new HashMap<>();
								tagMap.put(resourceGroup, 1.0);
								metric.setTagMap(tagMap);
							}
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
	}

	/**
	 * Persist the metric to DB
	 * @param metric the metric object to be saved
	 */
	private void saveMetricToDB(Metric metric) {
		metricRepo.save(metric);
	}

	/**
	 * get the key attribute from the array
	 * @param lineArr the string collections processed by split() method for each line in describe
	 * @return the key attribute
	 */
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

	/**
	 * generate the whole metric map based on the specified describe version
	 * @param adapterKind
	 * @param adapterVersion
	 * @return the whole metric map
	 */
	private Map<String, List<Metric>> generateMetricMap(String adapterKind, String adapterVersion) {
		Map<String, List<Metric>> map = new HashMap<>();
		List<Metric> metricList = metricRepo.findByAdapterKindAndAdapterVersion(adapterKind, adapterVersion);
		for (Metric metric : metricList) {
			for (Entry<String, Double> entry : metric.getTagMap().entrySet()) {
				String tag = entry.getKey();
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
	 * get all stored adapterKinds
	 * @return a list of adapterKinds
	 */
	private List<String> getAllAdapterKinds() {
		List<Adapter> adapterList = adapterRepo.findAll();
		List<String> adapterKindList = new ArrayList<>();
		for (Adapter adapter : adapterList) {
			adapterKindList.add(adapter.getAdapterKind());
		}
		return adapterKindList;
	}

	/**
	 * get all stored versions for the selected adapterKind
	 * @param adapterKind
	 * @return a list of adapter versions
	 */
	private List<String> getAllAdapterVersionsByAdapterKind(String adapterKind) {
		Adapter adapter = adapterRepo.findByAdapterKind(adapterKind);
		List<String> versionList = new ArrayList<>();
		if (adapter != null) {
			versionList = adapter.getVersionList();
		}
		return versionList;
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
