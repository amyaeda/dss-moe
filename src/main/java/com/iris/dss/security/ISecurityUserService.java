package com.iris.dss.security;

public interface ISecurityUserService {

    String validatePasswordResetToken(long id, String token);
    
    String validatePasswordResetToken(String token);

}
