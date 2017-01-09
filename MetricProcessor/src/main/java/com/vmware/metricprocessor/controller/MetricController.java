package com.vmware.metricprocessor.controller;

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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.vmware.metricprocessor.pojo.Metric;
import com.vmware.metricprocessor.repository.MetricRepository;

/**
 * 
 * @author hy
 *
 */
@Controller
public class MetricController {

	@Autowired
	private MetricRepository metricRepo;

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
	
	@RequestMapping(value = "list")
	public String getMetricMap(Map<String, Object> map) {
		map.put("welcomeStr", welcomeStr);
		map.put("metricMap", generateMetricMap("V4V", "6.4"));
		return "list";
	}
	
	@RequestMapping(value = "download")
	public String processDownload(Map<String, Object> map) {
		System.out.println("download is called!");
		map.put("welcomeStr", welcomeStr);
		map.put("metricMap", generateMetricMap("V4V", "6.4"));
		return "download";
	}

	@RequestMapping(value = "example")
	public ModelAndView example(HttpServletRequest request) {
		return new ModelAndView("index");
	}

	/**
	 * Read the adapter describe file and save each metric as a structured
	 * object in mongo
	 * 
	 * @param adapterKind
	 *            the adapter kind which the describe file belongs to
	 * @param adapterVersion
	 *            the version of the adapter
	 * @param describeFile
	 *            the describeFile full name
	 */
	public synchronized void loadDescribeFile(String adapterKind, String adapterVersion, String describeFile) {
		// System.out.println(System.getProperty("user.dir"));
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
							// reset resource group for new resource kind since
							// some metrics
							// do not have resource group
							resourceGroup = "";
							break;
						case "<ResourceGroup":
							resourceGroup = getKey(lineArr);
							break;
						case "<ResourceAttribute":
							System.out.println("insert metric...");
							metricName = getKey(lineArr);
							Map<String, Double> tagMap = new HashMap<>();
							Metric metric = new Metric(adapterKind, adapterVersion, resourceKind, resourceGroup,
									metricName, tagMap);
							if (!resourceGroup.isEmpty()) {
								tagMap.put(resourceGroup, 1.0);
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
	 * 
	 * @param metric
	 *            the metric object to be saved
	 */
	public void saveMetricToDB(Metric metric) {
		metricRepo.save(metric);
	}

	public void deleteAllMetrics() {
		metricRepo.deleteAll();
	}

	/**
	 * get the key attribute from the array
	 * 
	 * @param lineArr
	 *            the string collections processed by split() method for each
	 *            line in describe
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
	 * 
	 * @param adapterKind
	 * @param adapterVersion
	 * @return the whole metric map
	 */
	public Map<String, List<Metric>> generateMetricMap(String adapterKind, String adapterVersion) {
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
}
