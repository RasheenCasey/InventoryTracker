package com.Inventory.productsheet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**").permitAll()
				.requestMatchers("/inventory", "/api/**", "/ai/**").authenticated()
				.anyRequest().authenticated())
				.formLogin(login -> login
						.loginPage("/login")
						.defaultSuccessUrl("/inventory", true)
						.failureUrl("/login?error=true")
						.permitAll())
				.logout(logout -> logout
						.logoutSuccessUrl("/")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/ai/**"))
				.sessionManagement(session -> session
						.maximumSessions(10)
						.maxSessionsPreventsLogin(false));

		return http.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		UserDetails admin = User.withUsername("admin")
				.password(passwordEncoder().encode("admin123"))
				.roles("ADMIN", "USER")
				.build();

		UserDetails user = User.withUsername("user")
				.password(passwordEncoder().encode("user123"))
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(admin, user);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
