package com.vmware.metricprocessor.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vmware.metricprocessor.pojo.Metric;

public interface MetricRepository extends MongoRepository<Metric, String> {

	public List<Metric> findByAdapterKind(String adapterKind);

	public List<Metric> findByResourceKind(String resourceKind);

	public List<Metric> findByAdapterVersion(String adapterVersion);

	public List<Metric> findByAdapterKindAndAdapterVersion(String adapterKind, String adapterVersion);
	// public List<Metric> findMetric(String adapterKind, String resourceKind,
	// String resourceGroup, String metricName);
	public List<Metric> deleteByAdapterKindAndAdapterVersion(String adapterKind, String adapterVersion);
}
