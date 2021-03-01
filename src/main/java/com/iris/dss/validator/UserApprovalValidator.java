package com.iris.dss.validator;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iris.dss.model.UserApproval;
import com.iris.dss.model.UserApprovalForm;
import com.iris.dss.repo.UserApprovalRepository;
import com.iris.dss.service.CertificateFileService;

@Component
public class UserApprovalValidator implements Validator {
	
	@Autowired
	private UserApprovalRepository userApprovalRepository;
	
	@Autowired
	private CertificateFileService certificateFileService;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		//return clazz == UserForm.class;
		return UserApprovalForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserApprovalForm appUserForm = (UserApprovalForm) target;
		//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "usernameAdm", "message.username");
		
		if (!errors.hasFieldErrors("userId")) {
			//System.out.println("masuk no error1");
			if (appUserForm.getUserId() != null && appUserForm.getId() == 0) {
				UserApproval dbUser = userApprovalRepository.findByUserIdAndActive(appUserForm.getUserId(), 1);
				if (dbUser != null) {
					//System.out.println("masuk no error not null");
					errors.rejectValue("userId", "message.userId");}
			}
		}
		
		if(appUserForm.getFile()!=null) {
			//System.out.println("masuk no error2"+appUserForm.getFile());
			Date date;
			
			try {
				Map<String, String> validityDate = null;
				validityDate = certificateFileService.validity(appUserForm.getPassword(), appUserForm.getFile().getBytes());
				if (validityDate.containsKey("invalid")) {	
					errors.rejectValue("file", "message.file");
					
				}
				
				if(validityDate!=null) {
					if (validityDate.containsKey("validTo")) {
						
						date = new SimpleDateFormat("yyyy/MM/dd").parse(validityDate.get("validTo"));
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						
						java.util.Date dateToday= new Date();
						Calendar cal2 = Calendar.getInstance();
						//cal2.setTime(dateToday);
						
						Date dateValidTo = new Date(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
					    Date today = new Date(cal2.get(Calendar.YEAR),cal2.get(Calendar.MONTH),cal2.get(Calendar.DAY_OF_MONTH));
						
						boolean isExpiredDate = dateValidTo.before(today);
						  //System.out.println("\nisExpiredDate" +isExpiredDate);
						if(isExpiredDate==true) {
							System.out.println("expired true ");
							errors.rejectValue("file", "message.dateTo");
						}
					}
					
				}
				
			} catch (ParseException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
				errors.rejectValue("file", "message.file");
				e.printStackTrace();
			}
			
			catch (Exception e) {
				// TODO Auto-generated catch block
				errors.rejectValue("file", "Fail Tidak Sah");
				e.printStackTrace();
			}
			
		}
		
		
		
	}
	
}

	