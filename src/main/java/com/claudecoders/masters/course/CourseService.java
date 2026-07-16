package com.claudecoders.masters.course;

import com.claudecoders.masters.course.dto.CourseRequest;
import com.claudecoders.masters.course.dto.CourseResponse;
import com.claudecoders.masters.enrollment.EnrollmentRepository;
import com.claudecoders.masters.shared.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

	private static final String ENROLLED_STATE_CODE = "ENROLLED";

	private final CourseRepository courseRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final UserService userService;

	public CourseService(
			CourseRepository courseRepository,
			EnrollmentRepository enrollmentRepository,
			UserService userService
	) {
		this.courseRepository = courseRepository;
		this.enrollmentRepository = enrollmentRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<CourseResponse> findAll() {
		return courseRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public CourseResponse findById(UUID id, UUID userId) {
		Course course = findEntity(id);
		checkAccess(course.getId(), userId);
		return toResponse(course);
	}

	@Transactional
	public CourseResponse create(CourseRequest request) {
		Course course = new Course();
		applyRequest(course, request);
		return toResponse(courseRepository.save(course));
	}

	@Transactional
	public CourseResponse update(UUID id, CourseRequest request) {
		Course course = findEntity(id);
		applyRequest(course, request);
		return toResponse(courseRepository.save(course));
	}

	@Transactional
	public void delete(UUID id) {
		courseRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Course getReference(UUID id) {
		return findEntity(id);
	}

	private void checkAccess(UUID courseId, UUID userId) {
		User user = userService.getReference(userId);

		if (user.getRole() == UserRole.STUDENT) {
			boolean enrolled = enrollmentRepository
					.existsByStudent_User_IdAndCourse_IdAndState_Code(userId, courseId, ENROLLED_STATE_CODE);
			if (!enrolled) {
				throw new AccessDeniedException("No tienes una matrícula activa en este curso");
			}
		}
	}

	private Course findEntity(UUID id) {
		return courseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Course", id));
	}

	private void applyRequest(Course course, CourseRequest request) {
		if (request.endDate().isBefore(request.startDate())) {
			throw new BusinessException("La fecha de fin del curso debe ser posterior o igual a la fecha de inicio");
		}
		course.setCode(request.code());
		course.setName(request.name());
		course.setStartDate(request.startDate());
		course.setEndDate(request.endDate());
		course.setObservations(request.observations());
	}

	private CourseResponse toResponse(Course course) {
		return new CourseResponse(
				course.getId(),
				course.getCode(),
				course.getName(),
				course.getStartDate(),
				course.getEndDate(),
				course.getObservations(),
				course.getCreatedAt(),
				course.getUpdatedAt()
		);
	}
}
