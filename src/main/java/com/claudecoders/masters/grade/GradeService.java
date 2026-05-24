package com.claudecoders.masters.grade;

import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.enrollment.Enrollment;
import com.claudecoders.masters.enrollment.EnrollmentService;
import com.claudecoders.masters.grade.dto.GradeRequest;
import com.claudecoders.masters.grade.dto.GradeResponse;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateService;
import com.claudecoders.masters.student.Student;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GradeService {

	private final GradeRepository gradeRepository;
	private final EnrollmentService enrollmentService;
	private final StateService stateService;

	public GradeService(
			GradeRepository gradeRepository,
			EnrollmentService enrollmentService,
			StateService stateService
	) {
		this.gradeRepository = gradeRepository;
		this.enrollmentService = enrollmentService;
		this.stateService = stateService;
	}

	@Transactional(readOnly = true)
	public List<GradeResponse> findAll() {
		return gradeRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public GradeResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public GradeResponse create(GradeRequest request) {
		Grade grade = new Grade();
		applyRequest(grade, request);
		return toResponse(gradeRepository.save(grade));
	}

	@Transactional
	public GradeResponse update(UUID id, GradeRequest request) {
		Grade grade = findEntity(id);
		applyRequest(grade, request);
		return toResponse(gradeRepository.save(grade));
	}

	@Transactional
	public void delete(UUID id) {
		gradeRepository.delete(findEntity(id));
	}

	private Grade findEntity(UUID id) {
		return gradeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Grade", id));
	}

	private void applyRequest(Grade grade, GradeRequest request) {
		Enrollment enrollment = enrollmentService.getReference(request.enrollmentId());
		State state = stateService.getReference(request.stateId());
		grade.setEnrollment(enrollment);
		grade.setState(state);
		grade.setValue(request.value());
	}

	private GradeResponse toResponse(Grade grade) {
		Enrollment enrollment = grade.getEnrollment();
		Student student = enrollment.getStudent();
		Course course = enrollment.getCourse();
		State state = grade.getState();
		return new GradeResponse(
				grade.getId(),
				enrollment.getId(),
				student.getId(),
				student.getUser().getEmail(),
				course.getId(),
				course.getCode(),
				course.getName(),
				state.getId(),
				state.getCode(),
				state.getName(),
				grade.getValue(),
				grade.getCreatedAt(),
				grade.getUpdatedAt()
		);
	}
}
