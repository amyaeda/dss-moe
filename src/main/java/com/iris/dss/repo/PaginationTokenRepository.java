package com.iris.dss.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.Token;


@Repository
public interface PaginationTokenRepository extends PagingAndSortingRepository<Token, Integer>  {
	
	@Query(value = "SELECT * FROM token WHERE status in (0 ,1) ", nativeQuery = true)
	Page<Token> queryfindAllStatusExceptDeletedFile(Pageable pageable);
	
	/*
	 * @Query("SELECT count(*) FROM token WHERE status in (0 ,1) ") int
	 * totalrecord(Pageable pageable);
	 */
	
}
