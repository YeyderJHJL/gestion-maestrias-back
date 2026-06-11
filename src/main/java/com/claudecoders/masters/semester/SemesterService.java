package com.claudecoders.masters.semester;

import com.claudecoders.masters.semester.dto.SemesterRequest;
import com.claudecoders.masters.semester.dto.SemesterResponse;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemesterService {

	private final SemesterRepository semesterRepository;

	public SemesterService(SemesterRepository semesterRepository) {
		this.semesterRepository = semesterRepository;
	}

	@Transactional(readOnly = true)
	public List<SemesterResponse> findAll() {
		return semesterRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SemesterResponse findById(Integer id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public SemesterResponse create(SemesterRequest request) {
		if (semesterRepository.existsByYearAndCode(request.year(), request.code())) {
			throw new BusinessException("Ya existe un semestre con ese anio y codigo");
		}
		Semester semester = new Semester();
		applyRequest(semester, request);
		return toResponse(semesterRepository.save(semester));
	}

	@Transactional
	public SemesterResponse update(Integer id, SemesterRequest request) {
		Semester semester = findEntity(id);
		if ((!semester.getYear().equals(request.year()) || !semester.getCode().equals(request.code()))
				&& semesterRepository.existsByYearAndCode(request.year(), request.code())) {
			throw new BusinessException("Ya existe un semestre con ese anio y codigo");
		}
		applyRequest(semester, request);
		return toResponse(semesterRepository.save(semester));
	}

	@Transactional
	public void delete(Integer id) {
		semesterRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Semester getReference(Integer id) {
		return findEntity(id);
	}

	private Semester findEntity(Integer id) {
		return semesterRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Semester", id));
	}

	private void applyRequest(Semester semester, SemesterRequest request) {
		semester.setYear(request.year());
		semester.setCode(request.code());
	}

	private SemesterResponse toResponse(Semester semester) {
		return new SemesterResponse(
				semester.getId(),
				semester.getYear(),
				semester.getCode(),
				semester.getCreatedAt(),
				semester.getUpdatedAt()
		);
	}
}
