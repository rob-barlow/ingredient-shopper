package com.glendas.shopper.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.glendas.shopper.security.AdminSessionFilter;
import com.glendas.shopper.security.ProblemAuthenticationEntryPoint;
import com.glendas.shopper.service.AuthService;

/**
 * HTTP security for the whole API (ADR-006, 004 AC-8).
 *
 * <ul>
 *   <li>{@code /v1/admin/**}, {@code /v1/auth/logout} and {@code /v1/auth/me} need the admin.
 *       Without a valid token: <b>401</b>, enforced here on the server, whatever the UI shows.</li>
 *   <li>Everything else is public.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private static final String[] ADMIN_ONLY = {"/v1/admin/**", "/v1/auth/logout", "/v1/auth/me"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService) throws Exception {
        http
            // Stateless JSON API with bearer tokens: no server-side sessions (Article V),
            // and no CSRF protection needed because nothing relies on cookies.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .addFilterBefore(new AdminSessionFilter(authService), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(errors -> errors.authenticationEntryPoint(new ProblemAuthenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ADMIN_ONLY).hasAuthority(AdminSessionFilter.ROLE_ADMIN)
                .anyRequest().permitAll());
        return http.build();
    }

    /** Checks the admin password against ADMIN_PASSWORD_BCRYPT. Accepts $2a$, $2b$ and $2y$ hashes. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** The app's clock, as a bean so tests can use a fixed time. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * No users. This stops Spring Boot creating a default user with a generated password.
     * The admin is checked by AuthService instead.
     */
    @Bean
    UserDetailsService noUsers() {
        return new InMemoryUserDetailsManager();
    }

    /** Lets the Blazor frontend, served from another origin, call the API (plan §6, CORS). */
    @Bean
    CorsConfigurationSource corsConfigurationSource(ShopperProperties properties) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(properties.cors().allowedOrigins());
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
