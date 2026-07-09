package com.claudecoders.masters.program;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

	Optional<Program> findFirstByOrderByIdAsc();
}
