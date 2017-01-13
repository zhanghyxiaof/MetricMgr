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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
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
	
	@RequestMapping(value = "generateDescribe")
	public String generateDescribe(HttpServletRequest request) {
		System.out.println("generateDescribe is called!");
		/*map.put("welcomeStr", welcomeStr);
		map.put("metricMap", generateMetricMap("V4V", "6.4"));*/
		String[] checkedTags = request.getParameterValues("checkbox");
		String userJob = request.getParameter("userJob");
		List<String> checkMetricList = generateCheckMetricList("V4V", "6.4", checkedTags, userJob);
		changeDescribeFile("./src/main/resources/describe.xml", "./src/main/resources/new_describe.xml", checkMetricList);
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
	
	@RequestMapping(value = "admin")
	public String uploadDiscribeFile(Map<String, Object> map) {
		return "admin";
	}
	
	@RequestMapping(value = "uploadDescribe")
	public void processUpload(HttpServletRequest request) throws IOException {
		BufferedInputStream fileIn = new BufferedInputStream(request.getInputStream()); 
		String fn = request.getParameter("fileName"); 				     
		byte[] buf = new byte[1024];
		
		File file = new File("src/main/resources/" + fn); 		   
		BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file)); 		   
		while (true) { 
			int bytesIn = fileIn.read(buf, 0, 1024);       
		    System.out.println(bytesIn); 
		    if (bytesIn == -1) { 
		        break; 
		    } 
		    else { 
		    	fileOut.write(buf, 0, bytesIn); 
		    } 
		} 		   
		fileOut.flush(); 
		fileOut.close(); 
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
	
	public synchronized void changeDescribeFile(String originalDescribeFile, String changedDescribeFile, List<String> checkMetricList) {
		try (FileInputStream istm = new FileInputStream(originalDescribeFile)) {
			Scanner scanner = new Scanner(new InputStreamReader(istm, "UTF-8"));
			try {
				String resourceKind = "";
				String resourceGroup = "";
				String metricName = "";
				
				File outfile = new File(changedDescribeFile);
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
							metricName = getKey(lineArr);
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
	
	private String[] changeDefaultMonitored(String[] lineArr, boolean ismonitored) {
		for (int i=0; i<lineArr.length; i++) {
			String[] pair = lineArr[i].split("=");
			if (pair.length > 1) {
				if (pair[0].equals("defaultmonitored")) {
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
	
	public void updateTagWeights(Metric metric, String tag){
		metric.getTagMap().put(tag, metric.getTagMap().get(tag)+1);
		saveMetricToDB(metric);
	}
	
	public void uodateJobWeights(Metric metric, String userJob){
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
			uodateJobWeights(metric, userJob);
		}
		
//		metricList = metricRepo.findByAdapterKindAndAdapterVersion(adapterKind, adapterVersion);
//		for (Metric metric : metricList){
//			if (metric.getMetricName().equals("fec_rate")){
//				System.out.println(metric.getTagMap().get("remotefx_network"));
//			}
//		}
		
		return checkMetricList;
	}
}
