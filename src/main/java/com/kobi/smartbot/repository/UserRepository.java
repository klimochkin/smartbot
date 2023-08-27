package com.kobi.smartbot.repository;

import com.kobi.smartbot.repository.entity.UserJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserJpa, Long> {
    Optional<UserJpa> findByUserIdAndSource(Long userId, String source);
}
