package br.com.gilliardcarvalho.todolist.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByUsername(String username);

}
