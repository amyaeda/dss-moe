package com.iris.dss.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class LoginAttemptService {

	private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
	
	private LoadingCache<String, Integer> attemptsCache;
	private int maxAttempt;
	private long lockedDuration;
	
	public long getLockedDuration() {
		return this.lockedDuration;
	}
	
	public int getMaxAttempt() {
		return this.maxAttempt;
	}
	
	public void init(long lockedDuration, int maxAttempt) 
	{
		log.info(">init> lockedDuration=" + lockedDuration + ", maxAttempt="+ maxAttempt);
		this.maxAttempt = maxAttempt;
		this.lockedDuration = lockedDuration;
		attemptsCache = CacheBuilder.newBuilder().
				expireAfterWrite(lockedDuration, TimeUnit.SECONDS)
				.build(new CacheLoader<String, Integer>() 
				{ 
					public Integer load(String key){ return 0; } 
				});
	}

	public void loginSucceeded(String key) {
		log.info("Login for" + key + " success.");
		attemptsCache.invalidate(key);
	}

	public void loginFailed(String key) {
		int attempts = 0;
		try {
			attempts = attemptsCache.get(key);
		} catch (ExecutionException e) {
			attempts = 0;
		}
		attempts++;
		attemptsCache.put(key, attempts);
	}

	public boolean isBlocked(String key) {
		try {			
			return attemptsCache.get(key) >= this.maxAttempt;
		} catch (ExecutionException e) {
			return false;
		}
	}
}
