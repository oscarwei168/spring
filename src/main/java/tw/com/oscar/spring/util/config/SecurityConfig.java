/**
 * SecurityConfig.java
 * Title: Oscar Wei Web Project
 * Copyright: Copyright(c)2015, oscarwei168
 *
 * @author Oscar Wei
 * @since 2015/8/6
 * <p>
 * H i s t o r y
 * 2015/8/6 Oscar Wei v1
 * + File created
 */
package tw.com.oscar.spring.util.config;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.LoggerListener;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import tw.com.oscar.spring.service.account.AccountService;
import tw.com.oscar.spring.util.annotations.Log;
import tw.com.oscar.spring.util.security.CustomLogoutSuccessHandler;
import tw.com.oscar.spring.util.security.CustomSuccessHandler;
import tw.com.oscar.spring.util.security.SecurityUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * <p>
 * Title: SecurityConfig.java<br>
 * </p>
 * <strong>Description:</strong> A spring security framework configuration<br>
 * This function include: - <br>
 * <p>
 * Copyright: Copyright (c) 2015<br>
 * </p>
 * <p>
 * Company: oscarwei168 Inc.
 * </p>
 *
 * @author Oscar Wei
 * @version v1, 2015/8/6
 * @since 2015/8/6
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
// mode = AdviceMode.ASPECTJ
// @ImportResource(value = "classpath:spring-ldap-context.xml")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Log
    Logger LOGGER;

    @Autowired
    UserDetailsService userService;

    @Autowired
    DataSource dataSource;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomSuccessHandler customSuccessHandler;

    @Autowired
    CustomLogoutSuccessHandler customLogoutSuccessHandler;

    /**
     * A bean for obtain TokenBasedRememberMeServices object
     *
     * @return a TokenBasedRememberMeServices object
     */
    @Bean
    public TokenBasedRememberMeServices rememberMeServices() {
        return new TokenBasedRememberMeServices("remember-me-key", userService);
    }

    /**
     * A bean used for obtains PersistentTokenRepository object to store remember-me token
     *
     * @return a PersistentTokenRepository object
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(this.dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }

    /**
     * A bean used for obtains BCryptPasswordEncoder object
     *
     * @return a BCryptPasswordEncoder object
     */
    @Bean
    public BCryptPasswordEncoder bcryptEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * A configuration for setting authentication and password encoding strategy
     *
     * @param auth a AuthenticationManagerBuilder object
     * @throws Exception throw exception when:<br>
     *                   <ul><li>if cannot find any entity info</li></ul>
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.eraseCredentials(true).userDetailsService(userService).passwordEncoder(bcryptEncoder());
    }

    /**
     * Ensuring that static contents(JavaScript, CSS, etc) is accessible from the login page without authentication
     *
     * @param web a WebSecurity object
     * @throws Exception throw exception when:<br>if any exception occurred
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * A configuration for uri mapping pattern
     *
     * @param http HttpSecurity object
     * @throws Exception throw exception when:<br>
     *                   <ul><li>if something wrong</li></ul>
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/*", "/index", "/resources/**").permitAll()
                .antMatchers("/secure/**").hasRole("ADMIN")
                .antMatchers("/oscar/**").access("hasRole('ADMIN') and hasRole('MANAGER')")
                //.anyRequest().authenticated()
                .anyRequest().anonymous()
                .and()
                .formLogin().loginPage("/login").loginProcessingUrl("/authenticate").defaultSuccessUrl("/index", true)
                .successHandler(authenticationSuccessHandler()).permitAll()
                .and()
                .logout().deleteCookies("JSESSIONID").invalidateHttpSession(true)
                //.logout().logoutUrl("/logout")
                //.logoutSuccessHandler(customLogoutSuccessHandler()).deleteCookies
                //("JSESSIONID").invalidateHttpSession(true)
                //.logoutSuccessUrl("/index").permitAll();
                .and()
                .rememberMe().rememberMeCookieName("remember-me").tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(1209600)
                .and().exceptionHandling().accessDeniedPage("/accessDenied")
                .and()
                .sessionManagement().maximumSessions(1);
        // invalidSessionUrl("/login?time=1")
        // .expiredUrl("/login?expired");
    }

    /**
     * A bean that will be customize original authentication success handler
     *
     * @return a AuthenticationSuccessHandler object
     */
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler() {

            /**
             * A handler while login successful
             *
             * @param request a HttpServletRequest object
             * @param response a HttpServletResponse object
             * @param authentication a Authentication object
             * @throws ServletException
             * @throws IOException
             */
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
                super.onAuthenticationSuccess(request, response, authentication);
                LOGGER.info("Login success...");
                SecurityUser user = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                LOGGER.info("Id: {}, Username : {}, Email: {}", user.getId(), user.getUsername(), user.getEmail());
            }
        };
    }

    /**
     * Automatically receives AuthenticationEvent messages
     *
     * @return a LoggerListener object
     */
    @Bean
    public LoggerListener loggerListener() {
        return new LoggerListener();
    }

}
