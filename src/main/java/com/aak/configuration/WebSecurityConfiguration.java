package com.aak.configuration;

import com.aak.domain.ClientDetail;
import com.aak.user.MyAuthenticationFailureHandler;
import com.aak.user.MyAuthenticationSuccessHandle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsUtils;

@EnableWebSecurity
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return new JdbcUserDetails();
    }

    public ClientDetailsService clientDetailsServiceBean() throws  Exception{
        return new JdbcClientDetails();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/webjars/**","/resources/**","/imges/**","/css/**","/js/**");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/login","/logout.do","/oauth/getAuth","/oauth/revoke_token","/oauth/check_token","/oauth/authorize","/oauth/token","/outside/**","/user/*").permitAll()
                .antMatchers("/**").authenticated()
                .and()
                .formLogin()
                .loginProcessingUrl("/login.do")
                .failureForwardUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                //.defaultSuccessUrl("http://smart.cast.org.cn/")
                .successHandler(new MyAuthenticationSuccessHandle())
                .failureHandler(new MyAuthenticationFailureHandler())
                .loginPage("/login")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout.do"))
                .and()
                .userDetailsService(userDetailsServiceBean());
        http
                .cors()
                .and().csrf().ignoringAntMatchers("/outside/information");

        //add for bug
        //https://github.com/thymeleaf/thymeleaf-spring/issues/110
        //https://stackoverflow.com/a/53004917/5366876
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS);


        ///.and().csrf().disable();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceBean())
        .passwordEncoder(passwordEncoder());
    }


}
