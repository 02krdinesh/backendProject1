package com.example.charityManagementSystem.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfigSecurity {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    public ConfigSecurity(
            JwtFilter jwtFilter,
            @Qualifier(value = "firstUserService") UserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    // @Bean("firstUserService")
    // private UserDetailsService firstUserService(){
    // new MyUserDetailService();
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // Donor public APIs
                                "/donor/public/register",
                                "/donor/public/login",
                                "/donor/public/check-email-verification",
                                "/donor/public/send-verification-email",
                                "/donor/public/verify-email",
                                "/donor/public/send-reset-password-email",
                                "/donor/public/reset-password",

                                // Needy public APIs
                                "/needy/public/register",
                                "/needy/public/login",
                                "/needy/public/check-email-verification-status",
                                "/needy/public/send-verification-email",
                                "/needy/public/verify-email-token",
                                "/needy/public/send-reset-password-email",
                                "/needy/public/reset-password",
                                "/needy/public/account-verification-status",

                                // Payment public APIs
                                "/payments/public/stripe/checkOutSession",
                                "/payments/public/stripe/handlePayment",
                            "/payments/public/stripe/sendRegistrationMail", 
                        
                        
                               //admin
                               "/admin/public/login")

                        .permitAll()
                        .anyRequest().authenticated())
                // .httpBasic(Customizer.withDefaults()) --> you can remove this line for
                // disabling
                // basic authentication
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

        /*
         * return http
         * .csrf(csrf -> csrf.disable())
         * .authorizeHttpRequests(auth -> auth
         * .requestMatchers(
         * "/api/public/register",
         * "/api/public/login",
         * "/api/public/verify-email",
         * "/api/public/verifyResetPass",
         * "/api/public/sendResetPasswordMail"
         * ).permitAll()
         * .anyRequest().authenticated()
         * )
         * //.httpBasic(Customizer.withDefaults()) --> you can remove this line for
         * disabling
         * // basic authentication
         * .sessionManagement(session ->
         * session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
         * )
         * .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
         * .build();
         * 
         */

    }

    // @Bean
    // public UserDetailsService userDetailsService() {
    //
    // UserDetails user1 = User
    // .withDefaultPasswordEncoder()
    // .username("kiran")
    // .password("k@123")
    // .roles("USER")
    // .build();
    //
    // UserDetails user2 = User
    // .withDefaultPasswordEncoder()
    // .username("harsh")
    // .password("h@123")
    // .roles("ADMIN")
    // .build();
    // return new InMemoryUserDetailsManager(user1, user2);
    // }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}

/*
 * 
 * @FunctionalInterface
 * interface MyInterface {
 * void func(int i);
 * }
 * 
 * public class Main {
 * public static void main(String[] args) {
 * MyInterface obj = new MyInterface() {
 * 
 * @Override
 * public void func(int i) {
 * System.out.println("Number: " + i);
 * }
 * };
 * obj.func(5);
 * }
 * }
 * 
 * 
 * 
 * 
 * MyInterface obj = (i) -> {System.out.println(i);};
 * obj.func();
 * 
 * 
 */