package com.iris.dss.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.Modul;

@Repository("modulRepository")
public interface ModulRepository extends JpaRepository<Modul, Long>  {
	
	Modul findByModulCode(String modulCode);
	List<Modul> findAll();
	List<Modul> findByStatus(int status);
}
