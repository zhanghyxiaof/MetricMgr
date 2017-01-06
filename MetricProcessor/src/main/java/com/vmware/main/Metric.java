package com.vmware.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.data.annotation.Id;


public class Metric {

    @Id
    private String id;

    private String adapterVersion;
    private String adapterKind;
    private String resourceKind;
    private String resourceGroup;
    private String metricName;
    private Map<String, Double> tagMap = new HashMap<String, Double>();

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAdapterVersion() {
		return adapterVersion;
	}

	public void setAdapterVersion(String adapterVersion) {
		this.adapterVersion = adapterVersion;
	}

	public String getAdapterKind() {
		return adapterKind;
	}

	public void setAdapterKind(String adapterKind) {
		this.adapterKind = adapterKind;
	}

	public String getResourceKind() {
		return resourceKind;
	}

	public void setResourceKind(String resourceKind) {
		this.resourceKind = resourceKind;
	}

	public String getResourceGroup() {
		return resourceGroup;
	}

	public void setResourceGroup(String resourceGroup) {
		this.resourceGroup = resourceGroup;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public Map<String, Double> getTagMap() {
		return tagMap;
	}

	public void setTagMap(Map<String, Double> tagMap) {
		this.tagMap = tagMap;
	}

	public Metric() {}

    public Metric(String adapterKind, String resourceKind, String resourceGroup, String metricName, Map<String, Double> tagMap) {
        this.adapterKind = adapterKind;
        this.resourceKind = resourceKind;
        this.resourceGroup = resourceGroup;
        this.metricName = metricName;
        this.tagMap = tagMap;
    }

    @Override
    public String toString() {
    	String tagStr="";
    	for (Entry<String, Double> entry : tagMap.entrySet()) {
    		tagStr += entry.getKey() + ":" + entry.getValue() + ";";
    	}
        return String.format(
                "Metric[id=%s, adapterKind='%s', resourceKind='%s', metricName='%s', tags='%s']",
                id, adapterKind, resourceKind, metricName, tagStr);
    }

}

