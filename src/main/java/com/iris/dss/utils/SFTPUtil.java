package com.iris.dss.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * 
 * @author 
 *
 */
public class SFTPUtil {

	static JSch jsch = new JSch();
	static Session session = null;
	
	String username = "dssadmin";
	String password = "123456";
	String ipAddress = "172.16.253.109";
	String enableStrictHostKeyChecking = "no";
	
	public SFTPUtil(String username, String password, String ipAddress, String enableStrictHostKeyChecking) throws JSchException {
		this.username = username;
		this.password = password;
		this.ipAddress = ipAddress;
		this.enableStrictHostKeyChecking = enableStrictHostKeyChecking;
	}
	
	public void openSession() throws JSchException {
		session = jsch.getSession(username, ipAddress, 22);
		session.setConfig("StrictHostKeyChecking", enableStrictHostKeyChecking);
		session.setPassword(password);
		session.connect();
	}
	
	public void closeSession() {
		if(session!=null && session.isConnected()) { 
			session.disconnect();
		}
	}
	
	public boolean upload(String from, String to) throws Exception {		
		try {
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.put(from, to);  
			sftpChannel.exit();
			return true;
		} catch (JSchException e) {
			e.printStackTrace();  
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			
		}
		return false;

	}
	 
	public boolean download(String from, String to) throws Exception {	
		try {		
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.get(from, to);  
			sftpChannel.exit();		
			return true;
		} catch (JSchException e) {
			e.printStackTrace();  
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			
		}
		return false;

	}
	
	public static void main(String[] args) throws Exception {

		String username = "dssadmin";
		String password = "123456";
		String ipAddress = "172.16.253.109";
		String enableStrictHostKeyChecking = "no";
		
		SFTPUtil util = new SFTPUtil(username, password, ipAddress, enableStrictHostKeyChecking);
		
		util.openSession();
		
		for(int i=0; i< 100; i++) {
//			System.out.println( "Download file: " + 
//					util.download( "/home/dssadmin/documents/original/sample.pdf", 
//							"c:/Users/trlok/Desktop/sftp/sample.downloaded_"+ i +".pdf" ) );

			System.out.println(
					"Upload file: " + 			
							util.upload( "c:/Users/trlok/Desktop/sftp/sample.downloaded_"+ i +".pdf", 
									"/home/dssadmin/documents/signed/sample.downloaded_"+ i +".pdf" ) );

		}
		
		util.closeSession();
	}

}
