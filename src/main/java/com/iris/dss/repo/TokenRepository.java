package com.iris.dss.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.Token;

@Repository("tokenRepository")
public interface TokenRepository extends JpaRepository<Token, Integer>  {
	
	Token findById(int id);
	Token findByStatus(int status);
	Token findBySha256ChecksumAndStatus(String sha256Checksum, int status);
	
	 @Query(value = "SELECT * FROM token WHERE status in (0 ,1) ", nativeQuery = true)
	 List<Token> queryfindAllStatusExceptDeletedFile();
	 
	 List<Token> findAllByStatus(int status);
}
