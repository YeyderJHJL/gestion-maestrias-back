package com.claudecoders.masters.semester;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, Integer> {

	boolean existsByYearAndCode(Integer year, String code);
}
