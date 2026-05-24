package com.claudecoders.masters.enrollment;

import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.course.CourseService;
import com.claudecoders.masters.enrollment.dto.EnrollmentRequest;
import com.claudecoders.masters.enrollment.dto.EnrollmentResponse;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentService;
import com.claudecoders.masters.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final StudentService studentService;
	private final CourseService courseService;
	private final StateService stateService;

	public EnrollmentService(
			EnrollmentRepository enrollmentRepository,
			StudentService studentService,
			CourseService courseService,
			StateService stateService
	) {
		this.enrollmentRepository = enrollmentRepository;
		this.studentService = studentService;
		this.courseService = courseService;
		this.stateService = stateService;
	}

	@Transactional(readOnly = true)
	public List<EnrollmentResponse> findAll() {
		return enrollmentRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public EnrollmentResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public EnrollmentResponse create(EnrollmentRequest request) {
		Enrollment enrollment = new Enrollment();
		applyRequest(enrollment, request);
		return toResponse(enrollmentRepository.save(enrollment));
	}

	@Transactional
	public EnrollmentResponse update(UUID id, EnrollmentRequest request) {
		Enrollment enrollment = findEntity(id);
		applyRequest(enrollment, request);
		return toResponse(enrollmentRepository.save(enrollment));
	}

	@Transactional
	public void delete(UUID id) {
		enrollmentRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Enrollment getReference(UUID id) {
		return findEntity(id);
	}

	private Enrollment findEntity(UUID id) {
		return enrollmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
	}

	private void applyRequest(Enrollment enrollment, EnrollmentRequest request) {
		Student student = studentService.getReference(request.studentId());
		Course course = courseService.getReference(request.courseId());
		if (!student.getPromotion().getId().equals(course.getPromotion().getId())) {
			throw new BusinessException("Student and course must belong to the same promotion");
		}
		State state = stateService.getReference(request.stateId());
		enrollment.setStudent(student);
		enrollment.setCourse(course);
		enrollment.setState(state);
		enrollment.setEnrollmentDate(request.enrollmentDate());
		enrollment.setResolutionUrl(request.resolutionUrl());
		enrollment.setObservations(request.observations());
	}

	private EnrollmentResponse toResponse(Enrollment enrollment) {
		Student student = enrollment.getStudent();
		User user = student.getUser();
		Course course = enrollment.getCourse();
		State state = enrollment.getState();
		return new EnrollmentResponse(
				enrollment.getId(),
				student.getId(),
				user.getEmail(),
				"%s %s".formatted(user.getFirstName(), user.getLastName()),
				course.getId(),
				course.getCode(),
				course.getName(),
				state.getId(),
				state.getCode(),
				state.getName(),
				enrollment.getEnrollmentDate(),
				enrollment.getResolutionUrl(),
				enrollment.getObservations(),
				enrollment.getCreatedAt(),
				enrollment.getUpdatedAt()
		);
	}
}
