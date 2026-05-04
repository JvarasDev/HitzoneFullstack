package cl.hitzone.fullstack.Service;

import cl.hitzone.fullstack.Model.User;
import cl.hitzone.fullstack.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import cl.hitzone.fullstack.Dto.UserRegistrationDTO;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public User registerUser(UserRegistrationDTO dto) {
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        if(userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Este nombre de usuario ya está en uso");
        }
        
        //transformar dto a Entidad
        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setEmail(dto.getEmail());

        //Mas adelante aqui aplicaremos la encriptacion Bcrypt
        newUser.setPassword(dto.getPassword());

        return userRepository.save(newUser);

    }
}
