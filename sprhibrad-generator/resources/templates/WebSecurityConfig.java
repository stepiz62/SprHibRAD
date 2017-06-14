
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;

import com.sprhibrad.framework.configuration.ShrAuthenticationFailureHandler;
import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	ShrAuthenticationFailureHandler shAuthenticationFailureHandler;

	@Autowired
	private PasswordEncoder passwordEncoder;
		
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.jdbcAuthentication()
			.dataSource(dataSource)
§
	       	.passwordEncoder(passwordEncoder);	  
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		 http
		  .authorizeRequests()
		  	.antMatchers("/*.js", "/*.jpg", "/*.css", "/resources/**", "/login", "/index", "/hello", "/sessionexp", "/noaccess").permitAll()
§
		  	.anyRequest().authenticated()
		   		.and()
		   	.formLogin().
			   		failureHandler(shAuthenticationFailureHandler)
			   		.loginPage("/login")
			   		.permitAll()
			   		.defaultSuccessUrl("/", true)
		   		.and()
            .logout()                     
	                .permitAll()         
                .and()
	       	.requiresChannel()
	           		.antMatchers("/**").requiresSecure()
	           .and()
	     	.exceptionHandling()
	     	.accessDeniedHandler(new AccessDeniedHandlerImpl() {
				@Override
				public void handle(HttpServletRequest request,
						HttpServletResponse response,
						AccessDeniedException accessDeniedException)
						throws IOException, ServletException {
					if (accessDeniedException instanceof CsrfException)
						setErrorPage("/sessionexp");
					else {
						request.setAttribute("error", ((ShrResourceBundleMessageSource) messageSource).msgOrKey("message.accessDenied") );
						setErrorPage("/noaccess");
					}
					super.handle(request, response, accessDeniedException);
				}
			});
	}
}