package tw.com.oscar.spring.util.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 * Created by Oscar on 2015/2/28.
 */
//@Configuration
//@ImportResource(value = "classpath:spring-security-context.xml")
public class SecurityConfig {

//    @Bean
//    public UserService userService() {
//        return new UserService();
//    }
//
//    @Bean
//    public TokenBasedRememberMeServices rememberMeServices() {
//        return new TokenBasedRememberMeServices("remember-me-key", userService());
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new StandardPasswordEncoder();
    }
}