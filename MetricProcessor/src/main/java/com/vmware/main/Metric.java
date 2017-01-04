package com.vmware.main;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;


public class Metric {

    @Id
    public String id;

    public String adapterKind;
    public String resourceKind;
    public String resourceGroup;
    public String metricName;
    public List<String> tagList = new ArrayList<String>();

    public Metric() {}

    public Metric(String adapterKind, String resourceKind, String resourceGroup, String metricName, List<String> tagList) {
        this.adapterKind = adapterKind;
        this.resourceKind = resourceKind;
        this.resourceGroup = resourceGroup;
        this.metricName = metricName;
        this.tagList = tagList;
    }

    @Override
    public String toString() {
    	String tagStr="";
    	for (String tag : tagList) {
    		tagStr += tag + ";";
    	}
        return String.format(
                "Metric[id=%s, adapterKind='%s', resourceKind='%s', metricName='%s', tags='%s']",
                id, adapterKind, resourceKind, metricName, tagStr);
    }

}

