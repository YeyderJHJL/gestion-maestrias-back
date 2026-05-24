package com.claudecoders.masters.assignment;

import com.claudecoders.masters.assignment.dto.AssignmentRequest;
import com.claudecoders.masters.assignment.dto.AssignmentResponse;
import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.course.CourseService;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.teacher.Teacher;
import com.claudecoders.masters.teacher.TeacherService;
import com.claudecoders.masters.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentService {

	private final AssignmentRepository assignmentRepository;
	private final CourseService courseService;
	private final TeacherService teacherService;

	public AssignmentService(
			AssignmentRepository assignmentRepository,
			CourseService courseService,
			TeacherService teacherService
	) {
		this.assignmentRepository = assignmentRepository;
		this.courseService = courseService;
		this.teacherService = teacherService;
	}

	@Transactional(readOnly = true)
	public List<AssignmentResponse> findAll() {
		return assignmentRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public AssignmentResponse findById(UUID courseId, UUID teacherId) {
		return toResponse(findEntity(courseId, teacherId));
	}

	@Transactional
	public AssignmentResponse create(AssignmentRequest request) {
		if (assignmentRepository.existsByCourse_IdAndTeacher_Id(request.courseId(), request.teacherId())) {
			throw new BusinessException("Assignment already exists for this course and teacher");
		}
		Course course = courseService.getReference(request.courseId());
		Teacher teacher = teacherService.getReference(request.teacherId());
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		assignment.setTeacher(teacher);
		assignment.setAssignmentDate(request.assignmentDate());
		return toResponse(assignmentRepository.save(assignment));
	}

	@Transactional
	public AssignmentResponse update(UUID courseId, UUID teacherId, AssignmentRequest request) {
		if (!courseId.equals(request.courseId()) || !teacherId.equals(request.teacherId())) {
			throw new BusinessException("Assignment course and teacher cannot be changed");
		}
		Assignment assignment = findEntity(courseId, teacherId);
		assignment.setAssignmentDate(request.assignmentDate());
		return toResponse(assignmentRepository.save(assignment));
	}

	@Transactional
	public void delete(UUID courseId, UUID teacherId) {
		assignmentRepository.delete(findEntity(courseId, teacherId));
	}

	private Assignment findEntity(UUID courseId, UUID teacherId) {
		return assignmentRepository.findByCourse_IdAndTeacher_Id(courseId, teacherId)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment", "%s/%s".formatted(courseId, teacherId)));
	}

	private AssignmentResponse toResponse(Assignment assignment) {
		Course course = assignment.getCourse();
		Teacher teacher = assignment.getTeacher();
		User user = teacher.getUser();
		return new AssignmentResponse(
				assignment.getId(),
				course.getId(),
				course.getCode(),
				course.getName(),
				teacher.getId(),
				user.getEmail(),
				"%s %s".formatted(user.getFirstName(), user.getLastName()),
				assignment.getAssignmentDate(),
				assignment.getCreatedAt(),
				assignment.getUpdatedAt()
		);
	}
}
