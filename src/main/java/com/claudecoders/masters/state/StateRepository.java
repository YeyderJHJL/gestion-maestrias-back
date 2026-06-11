package com.claudecoders.masters.state;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Integer> {

	Optional<State> findByEntityTypeAndCode(String entityType, String code);
}
