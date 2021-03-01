package com.iris.dss.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.SystemSetting;

@Repository("systemSettingRepository")
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer> {
	SystemSetting findById(int id);
}
