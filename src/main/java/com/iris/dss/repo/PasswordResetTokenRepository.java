package com.iris.dss.repo;

import java.util.Date;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.PasswordResetToken;
import com.iris.dss.model.User;

@Repository("passwordTokenRepository")
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    PasswordResetToken findByUser(User user);

    Stream<PasswordResetToken> findAllByExpiryDateLessThan(Date now);

    void deleteByExpiryDateLessThan(Date now);

	/*
	 * @Modifying
	 * 
	 * @Query("delete from PasswordResetToken t where t.expiryDate <= ?1") void
	 * deleteAllExpiredSince(Date now);
	 */
    
    //@Modifying
    @Query(value = "delete from PasswordResetToken t where t.expiryDate <= : now ", nativeQuery = true)
    void deleteAllExpiredSince(@Param("now") Date now);
}
