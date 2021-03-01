package com.iris.dss.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.iris.dss.AuthenticationException.CustomAuthenticationProvider;
import com.iris.dss.AuthenticationException.CustomOAuthRequestProcessingFilter;
import com.iris.dss.authentication.handler.CustomAuthenticationFailureHandler;
import com.iris.dss.authentication.handler.CustomLogoutSuccessHandler;
import com.iris.dss.authentication.handler.CustomizeAuthenticationSuccessHandler;
import com.iris.dss.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CustomAuthenticationProvider custAuthProvider;

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public CustomOAuthRequestProcessingFilter customUserPasswordAuthFilter() throws Exception {
		CustomOAuthRequestProcessingFilter filter = 
				new CustomOAuthRequestProcessingFilter(authenticationManagerBean(),	custAuthProvider);
		filter.setAuthenticationManager(authenticationManagerBean());
		filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
		filter.setAuthenticationFailureHandler(failureHandler());
		filter.setAuthenticationSuccessHandler(successHandler());
		return filter;
	}

	@Bean
	public LogoutSuccessHandler logoutSuccessHandler() {
		return new CustomLogoutSuccessHandler();
	}

	@Bean
	public CustomizeAuthenticationSuccessHandler successHandler() {
		CustomizeAuthenticationSuccessHandler handler = new CustomizeAuthenticationSuccessHandler();
		return handler;
	}

	@Bean
	public SimpleUrlAuthenticationFailureHandler failureHandler() {
		CustomAuthenticationFailureHandler handler = new CustomAuthenticationFailureHandler();
		return handler;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(custAuthProvider)
		.userDetailsService(userDetailsService())
		.passwordEncoder(passwordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		  .authorizeRequests()
		  .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
		  .antMatchers("/", "/forgetPassword*", "/resetPassword*", "/emailError*").permitAll()
		  .antMatchers("/changePassword*").permitAll()		  
		  //.antMatchers("/api/1/**").permitAll()
		  .antMatchers("/invalidSession*").anonymous()
		  .antMatchers("/updatePassword*").hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
		  .antMatchers("/savePassword*").permitAll() //.hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
		  .anyRequest().authenticated()
		  .antMatchers("/login**").permitAll()
		  .antMatchers("/error**").permitAll()
		.and()
		  .formLogin()
		  .loginPage("/login")
		  .defaultSuccessUrl("/home")
		  .failureHandler(new CustomAuthenticationFailureHandler()).permitAll()
		  .usernameParameter("username")
		  .passwordParameter("password")
		.and()
		  .addFilterBefore(customUserPasswordAuthFilter(), UsernamePasswordAuthenticationFilter.class)
		  .logout()
		    .logoutUrl("/logout")
		    .logoutSuccessUrl("/login?logout")
		    .logoutSuccessHandler(logoutSuccessHandler())
		    .clearAuthentication(true)
		    .invalidateHttpSession(true)
		    .deleteCookies("JSESSIONID")
		    .permitAll();

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
		web.ignoring().antMatchers("/api/1/**");
		//web.ignoring().antMatchers("/changePassword*");		
		//web.ignoring().antMatchers("/updatePassword.html*");
		web.ignoring().antMatchers("/savePassword*");
	}

}
