package com.iris.dss.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iris.dss.model.User;
import com.iris.dss.model.UserForm;
import com.iris.dss.repo.UserRepository;

@Component
public class UserValidator implements Validator {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		//return clazz == UserForm.class;
		return UserForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserForm appUserForm = (UserForm) target;
		//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "usernameAdm", "message.username");
		
		if (!errors.hasFieldErrors("usernameAdm")) {
			System.out.println("masuk no error");
			if (appUserForm.getUsernameAdm() != null && appUserForm.getId() == 0) {
				User dbUser = userRepository.findByUserName(appUserForm.getUsernameAdm());
				if (dbUser != null) {
					System.out.println("masuk no error not null");
					errors.rejectValue("usernameAdm", "message.username");}
			}
		}
		
	}
	
}

	