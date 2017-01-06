package com.vmware.main;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdapterRepository extends MongoRepository<Adapter, String> {

    public Adapter findByAdapterKind(String adapterKind);
}