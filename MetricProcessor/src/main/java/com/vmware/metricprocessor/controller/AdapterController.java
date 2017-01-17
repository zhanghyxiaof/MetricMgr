package com.vmware.metricprocessor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vmware.metricprocessor.pojo.Adapter;
import com.vmware.metricprocessor.pojo.Metric;
import com.vmware.metricprocessor.repository.AdapterRepository;

/**
 * This is the main class for the start and logic of metric processor project
 * 
 * @author hy
 *
 */
//// @RestController
@Controller
public class AdapterController {

	@Autowired
	private AdapterRepository adapterRepo;
	
	public List<Adapter> generateAdapterList() {
		List<Adapter> adapterList = adapterRepo.findAll();
		return adapterList;
	}
	
	public void deleteAllAdapters() {
		adapterRepo.deleteAll();
	}

	/**
	 * get all stored adapterKinds
	 * 
	 * @return a list of adapterKinds
	 */
	public List<String> getAllAdapterKinds() {
		List<Adapter> adapterList = adapterRepo.findAll();
		List<String> adapterKindList = new ArrayList<>();
		for (Adapter adapter : adapterList) {
			adapterKindList.add(adapter.getAdapterKind());
		}
		return adapterKindList;
	}

	/**
	 * get all stored versions for the selected adapterKind
	 * 
	 * @param adapterKind
	 * @return a list of adapter versions
	 */
	public List<String> getAllAdapterVersionsByAdapterKind(String adapterKind) {
		Adapter adapter = adapterRepo.findByAdapterKind(adapterKind);
		List<String> versionList = new ArrayList<>();
		if (adapter != null) {
			versionList = adapter.getVersionList();
		}
		return versionList;
	}

	public void insertAdapter(Adapter adapter) {
		adapterRepo.save(adapter);
	}

	public void removeAdapter(Adapter adapter) {
		adapterRepo.delete(adapter);
	}

	/**
	 * Append a new support version number to the existing adapter
	 * 
	 * @param adapterKind
	 * @param adapterVersion
	 */
	public void addNewAdapterVersion(String adapterKind, String adapterVersion) {
		Adapter adapter = adapterRepo.findByAdapterKind(adapterKind);
		if (adapter != null && !adapter.getVersionList().contains(adapterVersion)) {
			adapter.getVersionList().add(adapterVersion);
			insertAdapter(adapter);
		}
	}
	
	public void addNewAdapterKind(String adapterKind){
		List<String> adapterKindList = getAllAdapterKinds();
		if (adapterKindList.contains(adapterKind)){
			return;
		}
		Adapter adapter = new Adapter();
		adapter.setAdapterKind(adapterKind);
		List<String> versionList = new ArrayList<>();
		adapter.setVersionList(versionList);
		insertAdapter(adapter);
	}
	
	public void addTestAdapterData(){
		addNewAdapterKind("V4V");
		addNewAdapterVersion("V4V","6.1");
		addNewAdapterVersion("V4V","6.2");
		addNewAdapterVersion("V4V","6.3");
		addNewAdapterVersion("V4V","6.4");
		
		addNewAdapterKind("V4PA");
		addNewAdapterVersion("V4PA","6.1");
		
		addNewAdapterKind("V4PA7X");
		addNewAdapterVersion("V4PA7X","6.2");
		addNewAdapterVersion("V4PA7X","6.3");
		addNewAdapterVersion("V4PA7X","6.4");
		
		addNewAdapterKind("VMWARE");
		addNewAdapterVersion("VMWARE","6.0");
		addNewAdapterVersion("VMWARE","6.1");
		addNewAdapterVersion("VMWARE","6.2");
		addNewAdapterVersion("VMWARE","6.3");
		addNewAdapterVersion("VMWARE","6.4");
	}

}
