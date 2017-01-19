package com.vmware.metricprocessor.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.vmware.metricprocessor.pojo.Adapter;
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
	
	@Autowired
	private AdapterController adapterController;
	
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
		map.put("adapterList", adapterController.generateAdapterList());
		map.put("metricMap", generateAllMetricMap(adapterController.generateAdapterList()));
		return "list";
	}
	
//	@RequestMapping(value = "test")
//	public void test(Map<String, Object> map) {
//		map.put("test", welcomeStr);
//		return;
//	}
	
	@RequestMapping(value = "admin")
	public String produceAdminPage(Map<String, Object> map) {
		map.put("adapterList", adapterController.generateAdapterList());
		return "admin";
	}
	
	@RequestMapping(value = "generateDescribe")
	public String generateDescribe(HttpServletRequest request) {
		System.out.println("generateDescribe is called!");
		/*map.put("welcomeStr", welcomeStr);
		map.put("metricMap", generateMetricMap("V4V", "6.4"));*/
		String[] checkedTags = request.getParameterValues("checkbox");
		String userJob = request.getParameter("userJob");
		String adapterKind = request.getParameter("adapterKind");
		String adapterVersion = request.getParameter("adapterVersion");
		List<String> checkMetricList = generateCheckMetricList(adapterKind, adapterVersion, checkedTags, userJob);
		
		String propertiesFilePath = "./src/main/resources/" + adapterKind + "/" + adapterVersion + "/resources.properties";
		String describeFilePath = "./src/main/resources/" + adapterKind + "/" + adapterVersion + "/describe.xml";
		String newDescribeFilePath = "./src/main/resources/" + adapterKind + "/" + adapterVersion + "/new_describe.xml";
		
		Map<String, String> propertiesMap = loadProperties(propertiesFilePath);
		generateNewDescribeFile(describeFilePath, newDescribeFilePath, checkMetricList, propertiesMap);
		/*System.out.println(checkedTags[0]);*/
		return "download";
	}
	
	@RequestMapping(value = "downloadDescribe")
	public void downloadDiscribeFile(HttpServletResponse response) throws IOException {
		OutputStream outputStream = response.getOutputStream();
        //输出文件用的字节数组，每次向输出流发送1024个字节
        byte b[] = new byte[1024];
        //要下载的文件
        File fileload = new File("./src/main/resources/describe.xml");        
        //客服端使用保存文件的对话框
        response.setHeader("Content-disposition", "attachment;filename=describe.xml;");
        //通知客服文件的MIME类型
        response.setContentType("application/msword");
        //通知客服文件的长度
        long fileLength = fileload.length();
        String length = String.valueOf(fileLength);
        response.setHeader("Content_length", length);
        //读取文件，并发送给客服端下载
        FileInputStream inputStream = new FileInputStream(fileload);
        int n = 0;
        while((n=inputStream.read(b))!=-1){
            outputStream.write(b,0,n);
        }
        inputStream.close();
        outputStream.close();
	}
	
	@RequestMapping(value = "uploadDescribe")
	public void uploadDescribeFile(HttpServletRequest request) throws IOException {
		BufferedInputStream fileIn = new BufferedInputStream(request.getInputStream()); 
		String fn = request.getParameter("fileName");
		String adapterKind = request.getParameter("adapterKind");
		String adapterVersion = request.getParameter("adapterVersion");
		String FilePath = "./src/main/resources/" + adapterKind + "/" + adapterVersion + "/" + fn;
		byte[] buf = new byte[1024];

		File path = new File("./src/main/resources/" + adapterKind + "/" + adapterVersion); 
        if(!path.exists()) {  
        	path.mkdirs();
        }
		
		File file = new File(FilePath); 
		System.out.println(FilePath);
		BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file)); 	
		while (true) { 
			int bytesIn = fileIn.read(buf, 0, 1024);       
//		    System.out.println(bytesIn); 
		    if (bytesIn == -1) { 
		        break; 
		    } 
		    else { 
		    	fileOut.write(buf, 0, bytesIn); 
		    } 
		} 		   
		fileOut.flush(); 
		fileOut.close(); 
		System.out.println("upload file successfully!");
		
		loadPropertiesAndDescribe(adapterKind,adapterVersion);
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
	
	public synchronized void generateNewDescribeFile(String originalDescribeFilePath, String newDescribeFilePath, List<String> checkMetricList, Map<String, String> propertiesMap) {
		try (FileInputStream istm = new FileInputStream(originalDescribeFilePath)) {
			Scanner scanner = new Scanner(new InputStreamReader(istm, "UTF-8"));
			try {
				String resourceKind = "";
				String resourceGroup = "";
				String metricName = "";
				
				File outfile = new File(newDescribeFilePath);
				FileOutputStream ostm = new FileOutputStream(outfile);
				if (!outfile.exists()) {
					outfile.createNewFile();
				}
								
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					String[] lineArr = line.trim().split(" ");
					if (lineArr.length > 0) {
						switch (lineArr[0]) {
						case "<ResourceAttribute":
							metricName = propertiesMap.get(getNameKey(lineArr));
							if (checkMetricList.contains(metricName)){
								lineArr = changeDefaultMonitored(lineArr, true);
							}
							else {
								lineArr = changeDefaultMonitored(lineArr, false);
							}
							line = "";
							for (String element : lineArr){
								line = line + element + " ";
							}
							line += "\r\n";
							/*if (checkMetricList.contains(metricName)){
								System.out.println(line);
							}*/
							ostm.write(line.getBytes());
							break;
						default:
							line += "\r\n";
							ostm.write(line.getBytes());
						}
					}
				}
				System.out.println("New describe file is generated...");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				scanner.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isInteger(String str) {    
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
		return pattern.matcher(str).matches();    
	}  
	
	public void loadPropertiesAndDescribe(String adapterKind, String adapterVersion){
		String propertiesFilePath = "./src/main/resources/" + adapterKind + "/" + adapterVersion + "/resources.properties";
		String describeFilePath = "./src/main/resources/" + adapterKind + "/" + adapterVersion + "/describe.xml";
		File propertiesFile = new File(propertiesFilePath);
		File describeFile = new File(describeFilePath);
		if(!propertiesFile.exists() || !describeFile.exists()){
			return;
		}
		metricRepo.deleteByAdapterKindAndAdapterVersion(adapterKind, adapterVersion);
		Map<String, String> propertiesMap = loadProperties(propertiesFilePath);
		loadDescribeFile(adapterKind, adapterVersion, describeFilePath, propertiesMap);
	}
	
	public synchronized Map<String, String> loadProperties(String propertiesFilePath){
		Map<String, String> propertiesMap = new HashMap<>();
		try (FileInputStream istm = new FileInputStream(propertiesFilePath)) {
			Scanner scanner = new Scanner(new InputStreamReader(istm, "UTF-8"));
			try {
				String resourceKind = "";
				String resourceGroup = "";
				String metricName = "";
								
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					String[] lineArr = line.trim().split("=");
					if (lineArr.length > 1) {
						if (isInteger(lineArr[0])) {
							propertiesMap.put(lineArr[0], lineArr[1]);
						}
					}
				}
				System.out.println("Properties file is loaded");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				scanner.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return propertiesMap;
	}
	
	public synchronized void loadDescribeFile(String adapterKind, String adapterVersion, String describeFilePath, Map<String, String> propertiesMap) {
		// System.out.println(System.getProperty("user.dir"));
		try (FileInputStream stm = new FileInputStream(describeFilePath)) {
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
							resourceKind = propertiesMap.get(getNameKey(lineArr));
							// reset resource group for new resource kind since
							// some metrics
							// do not have resource group
							resourceGroup = "";
							break;
						case "<ResourceGroup":
							resourceGroup = propertiesMap.get(getNameKey(lineArr));
							break;
						case "<ResourceAttribute":
//							System.out.println("insert metric...");
							metricName = propertiesMap.get(getNameKey(lineArr));
							Map<String, Double> tagMap = new HashMap<>();
							Metric metric = new Metric(adapterKind, adapterVersion, resourceKind, resourceGroup,
									metricName, tagMap);
							if (resourceGroup != null && !resourceGroup.isEmpty()) {
								tagMap.put(resourceGroup, 1.0);
							}
							saveMetricToDB(metric);
							break;
						default:
//							System.out.println("skip line...");
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
	
	public void deleteMetricsByAdapterKindandVersion(String adapterKind, String adapterVersion){
		metricRepo.deleteByAdapterKindAndAdapterVersion(adapterKind, adapterVersion);
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
				if (pair[0].equalsIgnoreCase("key")) {
					return pair[1].replace("\"", "");
				}
			}
		}
		return "";
	}
	
	private String getNameKey(String[] lineArr) {
		for (String element : lineArr) {
			String[] pair = element.split("=");
			if (pair.length > 1) {
				if (pair[0].equalsIgnoreCase("namekey")) {
					return pair[1].replace("\"", "");
				}
			}
		}
		return "";
	}
	
	private String[] changeDefaultMonitored(String[] lineArr, boolean ismonitored) {
		for (int i=0; i<lineArr.length; i++) {
			String[] pair = lineArr[i].split("=");
			if (pair.length > 1) {
				if (pair[0].equalsIgnoreCase("defaultmonitored")) {
					if (ismonitored) {
						lineArr[i] = pair[0] + "=\"true\"";
					}
					else{
						lineArr[i] = pair[0] + "=\"false\"";
					}
				}
			}
		}
		return lineArr;
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
	
	public Map<String, Map<String, Map<String, List<Metric>>>> generateAllMetricMap(List<Adapter> adapterList){
		Map<String, Map<String, Map<String, List<Metric>>>> map = new HashMap<>();
		for(Adapter adapter : adapterList){
			Map<String, Map<String, List<Metric>>> tmpMap = new HashMap<>();			
			for (String version : adapter.getVersionList()){
				tmpMap.put(version, generateMetricMap(adapter.getAdapterKind(),version));
			}
			map.put(adapter.getAdapterKind(), tmpMap);
		}
		return map;
	}
	
	public void updateTagWeights(Metric metric, String tag){
		metric.getTagMap().put(tag, metric.getTagMap().get(tag)+1);
		saveMetricToDB(metric);
	}
	
	public void updateJobWeights(Metric metric, String userJob){
		if (metric.getTagMap().containsKey(userJob)){
			metric.getTagMap().put(userJob, metric.getTagMap().get(userJob)+1);
			saveMetricToDB(metric);
		}
	}
	
	public List<String> generateCheckMetricList(String adapterKind, String adapterVersion, String[] checkedTags, String userJob) {
		List<Metric> metricList = metricRepo.findByAdapterKindAndAdapterVersion(adapterKind, adapterVersion);
		List<String> checkMetricList = new ArrayList<>();
		for (Metric metric : metricList){
			for (Entry<String, Double> entry : metric.getTagMap().entrySet()){
				String tag = entry.getKey();
				if (Arrays.asList(checkedTags).contains(tag)){
					if (!checkMetricList.contains(metric.getMetricName())){
						checkMetricList.add(metric.getMetricName());
					}			
					updateTagWeights(metric, tag);
				}
			}
			updateJobWeights(metric, userJob);
		}
		
//		metricList = metricRepo.findByAdapterKindAndAdapterVersion(adapterKind, adapterVersion);
//		for (Metric metric : metricList){
//			if (metric.getMetricName().equals("Number of VMs")){
//				System.out.println(metric.getTagMap().get("Connections"));
//			}
//		}
		
		return checkMetricList;
	}
}
