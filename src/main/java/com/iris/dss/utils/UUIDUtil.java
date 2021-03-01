package com.iris.dss.utils;

import java.util.UUID;

public final class UUIDUtil {

	public static String getId() {
		return UUID.randomUUID().toString();
	}
}
