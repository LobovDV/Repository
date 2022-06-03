package com.bookshop.BookShopApp.config;

import com.bookshop.BookShopApp.security.JwtRequestFilter;
import com.bookshop.BookShopApp.security.Oauth2AuthenticationSuccessHandler;
import com.bookshop.BookShopApp.services.BookstoreUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JwtRequestFilter filter;
    private final LogoutHandler logoutHandler;
    private final Oauth2AuthenticationSuccessHandler successHandler;

    @Autowired
    public SecurityConfig(BookstoreUserDetailsService bookstoreUserDetailsService, JwtRequestFilter filter, LogoutHandler logoutHandler, Oauth2AuthenticationSuccessHandler successHandler) {
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.filter = filter;
        this.logoutHandler = logoutHandler;
        this.successHandler = successHandler;
    }

    @Bean
    PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(bookstoreUserDetailsService).passwordEncoder(getPasswordEncoder());  //
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement()
                .and().authorizeRequests()
                       .antMatchers("/my","/profile").authenticated() //.hasRole("USER")  //.authenticated()
                       .antMatchers("/**").permitAll()
                .and().formLogin()
                      .loginPage("/signin")
                      .failureUrl("/signin")
                .and().logout()
                       .logoutUrl("/logout")
                       .logoutSuccessUrl("/signin")
                       .addLogoutHandler(logoutHandler)
                       .deleteCookies("token")
                       .deleteCookies("refresh")
                       .clearAuthentication(true)
                .and().addFilterAfter(filter, UsernamePasswordAuthenticationFilter.class);

        http
                .oauth2Client()
                .and().oauth2Login()
                      .successHandler(successHandler)
                      .userInfoEndpoint();

    }

}
