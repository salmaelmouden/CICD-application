package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // Enables method-level security annotations like @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login").permitAll() // Public endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Only accessible by ADMIN role
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // Accessible by USER or ADMIN roles
                        .anyRequest().authenticated() // All other endpoints require authentication
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("http://localhost:3002/profile", true) // Redirect to React app
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("http://localhost:3002/") // Redirect to homepage on logout
                        .permitAll()
                );
        return http.build();
    }
}
