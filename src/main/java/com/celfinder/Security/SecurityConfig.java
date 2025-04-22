package com.celfinder.Security;

import com.celfinder.Procesos.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers("/", "/usuarios/**", "/resources/**", "/css/**", "/js/**", "/ventas/listar").permitAll()
                // Endpoints accesibles para usuarios autenticados (ROLE_USER o superior)
                .requestMatchers("/menu", "/comparar/**", "/compararmedia/**", "/ventas/historial-solicitudes-vendedor", "/ventas/notificaciones").hasRole("USER")
                // Formulario de solicitud de vendedor accesible para usuarios autenticados
                .requestMatchers("/admin/solicitar-vendedor").hasRole("USER")
                // Endpoints de administración restringidos a ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Publicación de teléfonos restringida a ROLE_VENDEDOR
                .requestMatchers("/ventas/publicar").hasRole("VENDEDOR")
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/usuarios/login")
                .loginProcessingUrl("/usuarios/login")
                .defaultSuccessUrl("/menu", true)
                .failureUrl("/usuarios/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}