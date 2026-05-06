package cl.hitzone.fullstack.config.security;

import cl.hitzone.fullstack.Model.User;
import cl.hitzone.fullstack.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de UserDetailsService requerida por Spring Security.
 *
 * Spring Security necesita saber cómo obtener un usuario desde alguna fuente
 * de datos (BD, memoria, LDAP, etc.) para poder verificar identidades.
 * Esta clase es ese "puente" entre nuestra entidad User y el sistema de seguridad.
 *
 * Se usa principalmente en el JwtAuthFilter para cargar el usuario
 * después de validar el token JWT.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Carga el usuario desde la base de datos usando el username.
     * Si no existe, lanzamos UsernameNotFoundException que Spring Security
     * maneja internamente devolviendo 401.
     *
     * Convertimos nuestra entidad User a un UserDetails de Spring Security
     * usando el builder que provee la librería — sin roles por ahora,
     * los agregaremos cuando implementemos el sistema de roles (Role.java ya existe).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado en la base de datos: " + username));

        // Construimos el UserDetails que Spring Security entiende.
        // Por ahora sin roles/authorities — se agregarán junto con Role.java.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())   // la password ya viene encriptada (BCrypt)
                .authorities("ROLE_USER")       // rol base — se expandirá con Role.java
                .build();
    }
}
