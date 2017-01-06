package com.vmware.metricprocessor.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vmware.metricprocessor.pojo.Adapter;

public interface AdapterRepository extends MongoRepository<Adapter, String> {

	public Adapter findByAdapterKind(String adapterKind);
}