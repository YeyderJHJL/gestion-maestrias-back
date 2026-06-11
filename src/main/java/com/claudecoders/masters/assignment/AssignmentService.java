package com.claudecoders.masters.assignment;

import com.claudecoders.masters.assignment.dto.AssignmentRequest;
import com.claudecoders.masters.assignment.dto.AssignmentResponse;
import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.course.CourseService;
import com.claudecoders.masters.semester.Semester;
import com.claudecoders.masters.semester.SemesterService;
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
	private final SemesterService semesterService;

	public AssignmentService(
			AssignmentRepository assignmentRepository,
			CourseService courseService,
			TeacherService teacherService,
			SemesterService semesterService
	) {
		this.assignmentRepository = assignmentRepository;
		this.courseService = courseService;
		this.teacherService = teacherService;
		this.semesterService = semesterService;
	}

	@Transactional(readOnly = true)
	public List<AssignmentResponse> findAll() {
		return assignmentRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public AssignmentResponse findById(UUID courseId, UUID teacherId, Integer semesterId) {
		return toResponse(findEntity(courseId, teacherId, semesterId));
	}

	@Transactional
	public AssignmentResponse create(AssignmentRequest request) {
		if (assignmentRepository.existsByCourse_IdAndTeacher_IdAndSemester_Id(
				request.courseId(),
				request.teacherId(),
				request.semesterId()
		)) {
			throw new BusinessException("Ya existe una asignacion para este curso, docente y semestre");
		}
		Course course = courseService.getReference(request.courseId());
		Teacher teacher = teacherService.getReference(request.teacherId());
		Semester semester = semesterService.getReference(request.semesterId());
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		assignment.setTeacher(teacher);
		assignment.setSemester(semester);
		assignment.setAssignmentDate(request.assignmentDate());
		return toResponse(assignmentRepository.save(assignment));
	}

	@Transactional
	public AssignmentResponse update(UUID courseId, UUID teacherId, Integer semesterId, AssignmentRequest request) {
		if (!courseId.equals(request.courseId()) || !teacherId.equals(request.teacherId()) || !semesterId.equals(request.semesterId())) {
			throw new BusinessException("No se puede cambiar el curso, docente ni semestre de la asignacion");
		}
		Assignment assignment = findEntity(courseId, teacherId, semesterId);
		assignment.setAssignmentDate(request.assignmentDate());
		return toResponse(assignmentRepository.save(assignment));
	}

	@Transactional
	public void delete(UUID courseId, UUID teacherId, Integer semesterId) {
		assignmentRepository.delete(findEntity(courseId, teacherId, semesterId));
	}

	@Transactional(readOnly = true)
	public List<AssignmentResponse> findByCourse(UUID courseId) {
		courseService.getReference(courseId);
		return assignmentRepository.findByCourse_IdOrderByAssignmentDateAsc(courseId)
					.stream()
					.map(this::toResponse)
					.toList();
	}

	@Transactional(readOnly = true)
	public List<AssignmentResponse> findByTeacher(UUID teacherId) {
    teacherService.getReference(teacherId);
    return assignmentRepository.findByTeacher_IdOrderByAssignmentDateAsc(teacherId)
            .stream()
            .map(this::toResponse)
            .toList();
	}

	private Assignment findEntity(UUID courseId, UUID teacherId, Integer semesterId) {
		return assignmentRepository.findByCourse_IdAndTeacher_IdAndSemester_Id(courseId, teacherId, semesterId)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment", "%s/%s/%s".formatted(courseId, teacherId, semesterId)));
	}

	private AssignmentResponse toResponse(Assignment assignment) {
		Course course = assignment.getCourse();
		Teacher teacher = assignment.getTeacher();
		Semester semester = assignment.getSemester();
		User user = teacher.getUser();
		return new AssignmentResponse(
				assignment.getId(),
				course.getId(),
				course.getCode(),
				course.getName(),
				teacher.getId(),
				user.getEmail(),
				"%s %s".formatted(user.getFirstName(), user.getLastName()),
				semester.getId(),
				semester.getYear(),
				semester.getCode(),
				assignment.getAssignmentDate(),
				assignment.getCreatedAt(),
				assignment.getUpdatedAt()
		);
	}
}
