package com.iris.dss.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.Ipts;

@Repository("iptsRepository")
public interface IptsRepository extends JpaRepository<Ipts, Long>  {
	
	Ipts findByIptsCode(String iptsCode);
	List<Ipts> findAll();
	List<Ipts> findByStatus(int status);
	List<Ipts> findByJenisIpts(String jenisIpts);
}
