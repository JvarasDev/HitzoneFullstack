package cl.hitzone.fullstack.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion central de Spring Security.
 * Define rutas publicas, estrategia STATELESS, filtro JWT, BCrypt y CORS.
 */
@Configuration
@EnableWebSecurity
public class ConfigSecurity {

    // Filtro JWT — intercepta cada request y valida el token
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // Nuestro UserDetailsService — carga usuarios desde la BD
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Cadena de filtros de seguridad.
     * Define que rutas son publicas y cuales requieren autenticacion JWT.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF porque JWT es stateless e inmune a CSRF
                .csrf(csrf -> csrf.disable())

                // CORS: permitimos requests desde el frontend (ver corsConfigurationSource)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Sin sesiones en el servidor — cada request se autentica con su token JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas por ruta
                .authorizeHttpRequests(auth -> auth
                        // Rutas publicas — no requieren token
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Todo lo demas requiere un JWT valido
                        .anyRequest().authenticated())

                // Proveedor que conecta BCrypt + UserDetailsService con Spring Security
                .authenticationProvider(authenticationProvider())

                // Nuestro filtro JWT va ANTES del filtro estandar de Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS — permite que el frontend (en otro puerto/dominio) haga requests al
     * backend.
     * Sin esto el navegador bloquea las llamadas por politica de mismo origen.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origenes permitidos en desarrollo local
        // En produccion: agregar "https://tu-dominio.com"
        config.setAllowedOrigins(List.of(
                "http://localhost:3000", // React / Vite default
                "http://localhost:5173", // Vite alternativo
                "http://localhost:4200" // Angular
        ));

        // Metodos HTTP habilitados para el frontend
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Headers permitidos — Authorization es clave para enviar el JWT
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Credenciales permitidas (cookies + JWT)
        config.setAllowCredentials(true);

        // Aplicamos la configuracion a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Proveedor DAO: conecta UserDetailsService y BCrypt con Spring Security.
     * En Spring Security 7.x el UserDetailsService se pasa en el constructor.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt: encripta contrasenas con salt aleatorio.
     * Resistente a ataques de fuerza bruta y rainbow tables.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager expuesto como Bean para uso futuro.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

}


    

    
    
    
    

    
    

    
    

    
    

    
    
