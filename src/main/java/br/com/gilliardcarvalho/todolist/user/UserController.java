package br.com.gilliardcarvalho.todolist.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity<Object> create(@RequestBody UserModel userModel) {

        if (userModel.getUsername() == null || userModel.getUsername().isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("erro", "O campo username é obrigatório.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<UserModel> existingUser = userRepository.findByUsername(userModel.getUsername());
        if (existingUser.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("erro", "Usuário já existe com esse username.");
            return ResponseEntity.status(409).body(response);
        }

        String passwordHashed = BCrypt.withDefaults()
            .hashToString(12, userModel.getPassword().toCharArray());

        userModel.setPassword(passwordHashed);

    
        UserModel userCreated = userRepository.save(userModel);
        return ResponseEntity.ok(userCreated);
    }
}

