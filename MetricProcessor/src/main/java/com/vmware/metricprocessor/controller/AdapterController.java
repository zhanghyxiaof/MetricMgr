package com.vmware.metricprocessor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.vmware.metricprocessor.pojo.Adapter;
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
			adapterRepo.save(adapter);
		}
	}

}
