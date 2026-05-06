package cl.hitzone.fullstack.Service;

import cl.hitzone.fullstack.Model.User;
import cl.hitzone.fullstack.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cl.hitzone.fullstack.Dto.UserRegistrationDTO;

/**
 * Servicio de autenticación.
 *
 * Centraliza la lógica de negocio relacionada con usuarios:
 * - Registro de nuevos usuarios (con validaciones de unicidad)
 * - Autenticación (comparación de contraseñas con BCrypt)
 *
 * IMPORTANTE: No manejamos sesiones ni tokens aquí.
 * La generación del JWT queda en el controller para mantener
 * separadas las responsabilidades (SRP - Single Responsibility Principle).
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Inyectamos el PasswordEncoder definido en ConfigSecurity.
     * Usamos la interfaz (no BCryptPasswordEncoder directamente)
     * para facilitar cambiar el algoritmo en el futuro sin tocar este código.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * Validaciones previas al guardado:
     * 1. Email único — no pueden existir dos usuarios con el mismo email
     * 2. Username único — el nombre de usuario es un identificador único
     *
     * La contraseña se encripta con BCrypt antes de persistirla.
     * Nunca guardamos contraseñas en texto plano.
     */
    public User registerUser(UserRegistrationDTO dto) {

        // Verificamos que el email no esté ya registrado
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Verificamos que el username no esté ya en uso
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Este nombre de usuario ya está en uso");
        }

        // Mapeamos el DTO a la entidad User
        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setEmail(dto.getEmail());

        // Encriptamos la contraseña con BCrypt antes de guardar.
        // BCrypt genera un salt aleatorio cada vez, por lo que el mismo password
        // genera hashes distintos — completamente normal y esperado.
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));

        return userRepository.save(newUser);
    }

    /**
     * Autentica un usuario verificando username y contraseña.
     *
     * Usamos passwordEncoder.matches() para comparar la contraseña en texto plano
     * con el hash BCrypt almacenado. NUNCA comparamos strings directamente.
     *
     * Si las credenciales son incorrectas, lanzamos RuntimeException
     * que el controller captura y convierte en 401 Unauthorized.
     */
    public User authenticate(String username, String password) {

        // Buscamos al usuario por nombre de usuario
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        // matches(rawPassword, encodedPassword) — BCrypt hace la comparación segura
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return user;
    }
}
