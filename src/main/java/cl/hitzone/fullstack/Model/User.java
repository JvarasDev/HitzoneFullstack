package cl.hitzone.fullstack.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "users")
@Getter // Reemplaza a @Data
@Setter // Reemplaza a @Data
@AllArgsConstructor
@NoArgsConstructor
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "este campo no puede estar vacio.")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    //validamos el minimo de caracteres y el maximo
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    // con esto hacemos que la contraseña sea mas segura al pedir un caracter mayusculas y minusculas...etc
    @Column(nullable = false, length = 255) // o length = 60 para BCrypt
    private String password;


    //validacion del correo electronico del usuario
    @NotBlank(message = "El campo email no puede estar vacio.")
    @Email(message = "El campo email debe tener formato de correo [@ y DOM].")
    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now();
    }
}
