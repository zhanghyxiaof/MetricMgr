package com.vmware.main;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Adapter {
	@Id
	private String id;
	
	private String adapterKind;
	private List<String> versionList;
	
	public List<String> getVersionList() {
		return versionList;
	}
	public void setVersionList(List<String> versionList) {
		this.versionList = versionList;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAdapterKind() {
		return adapterKind;
	}
	public void setAdapterKind(String adapterKind) {
		this.adapterKind = adapterKind;
	}
	
}
