package com.iris.dss.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtil {
	static final String DATEFORMAT = "yyyyMMddHHmmss";

	public static String getFormattedTimeStamp(Date d) {
		SimpleDateFormat sdfDate = new SimpleDateFormat(DATEFORMAT);
		String strDate = sdfDate.format(d);
		return strDate;
	}
	
	public static String getFormattedTimeStampForPdfFooter(Date d) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = sdfDate.format(d);
		return strDate;
	}
	
	public static Date getFormattedStringAsDate(String expiry) throws ParseException {
		SimpleDateFormat sdfDate = new SimpleDateFormat(DATEFORMAT);
		return sdfDate.parse(expiry);
	}
	
	public static Date GetUTCdatetimeAsDate(Date d)
	{
		//note: doesn't check for null
		return StringDateToDate(GetUTCdatetimeAsString(d));
	}

	public static String GetUTCdatetimeAsString(Date d)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(d);

		return utcTime;
	}

	public static Date StringDateToDate(String StrDate)
	{
		Date dateToReturn = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);

		try
		{
			dateToReturn = (Date)dateFormat.parse(StrDate);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		return dateToReturn;
	}
}
