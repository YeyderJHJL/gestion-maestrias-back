package com.claudecoders.masters.enrollment;

import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.course.CourseService;
import com.claudecoders.masters.enrollment.dto.EnrollmentBulkRequest;
import com.claudecoders.masters.enrollment.dto.EnrollmentBulkResponse;
import com.claudecoders.masters.enrollment.dto.EnrollmentBulkRowResult;
import com.claudecoders.masters.enrollment.dto.EnrollmentRequest;
import com.claudecoders.masters.enrollment.dto.EnrollmentResponse;
import com.claudecoders.masters.file.FilePurpose;
import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.semester.Semester;
import com.claudecoders.masters.semester.SemesterService;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentService;
import com.claudecoders.masters.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final StudentService studentService;
	private final CourseService courseService;
	private final StateService stateService;
	private final StoredFileService storedFileService;
	private final SemesterService semesterService;
	private final TransactionTemplate transactionTemplate;

	public EnrollmentService(
			EnrollmentRepository enrollmentRepository,
			StudentService studentService,
			CourseService courseService,
			StateService stateService,
			StoredFileService storedFileService,
			SemesterService semesterService,
			PlatformTransactionManager transactionManager
	) {
		this.enrollmentRepository = enrollmentRepository;
		this.studentService = studentService;
		this.courseService = courseService;
		this.stateService = stateService;
		this.storedFileService = storedFileService;
		this.semesterService = semesterService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
		if (enrollmentRepository.existsByStudent_IdAndCourse_Id(request.studentId(), request.courseId())) {
			throw new BusinessException("El estudiante ya está matriculado en este curso");
		}
		Enrollment enrollment = new Enrollment();
		applyRequest(enrollment, request);
		return toResponse(enrollmentRepository.save(enrollment));
	}

	public EnrollmentBulkResponse createBulk(EnrollmentBulkRequest request) {
		List<EnrollmentBulkRowResult> results = new ArrayList<>();
		int enrolled = 0;
		int rejected = 0;
		List<UUID> studentIds = request.studentIds();
		for (int i = 0; i < studentIds.size(); i++) {
			UUID studentId = studentIds.get(i);
			int rowNumber = i + 1;
			EnrollmentRequest row = new EnrollmentRequest(
					studentId,
					request.courseId(),
					request.semesterId(),
					request.stateId(),
					request.enrollmentDate(),
					request.resolutionFileId(),
					request.observations()
			);
			try {
				EnrollmentResponse saved = transactionTemplate.execute(status -> create(row));
				enrolled++;
				results.add(new EnrollmentBulkRowResult(
						rowNumber, studentId, EnrollmentBulkRowResult.Status.ENROLLED,
						saved.id(), List.of()));
			} catch (RuntimeException ex) {
				rejected++;
				results.add(new EnrollmentBulkRowResult(
						rowNumber, studentId, EnrollmentBulkRowResult.Status.REJECTED,
						null, List.of(rootMessage(ex))));
			}
		}
		return new EnrollmentBulkResponse(results.size(), enrolled, rejected, results);
	}

	private static String rootMessage(Throwable ex) {
		Throwable cur = ex;
		while (cur.getCause() != null && cur.getCause() != cur) {
			cur = cur.getCause();
		}
		return cur.getMessage() == null ? ex.getClass().getSimpleName() : cur.getMessage();
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

	@Transactional(readOnly = true)
	public List<EnrollmentResponse> findByCourse(UUID courseId, UUID userId) {
    courseService.getReference(courseId);
		courseService.checkAccess(courseId, userId);
    return enrollmentRepository.findByCourse_IdOrderByEnrollmentDateAsc(courseId)
          .stream()
          .map(this::toResponse)
          .toList();
	}

	@Transactional(readOnly = true)
	public List<EnrollmentResponse> findByStudent(UUID studentId) {
		studentService.getReference(studentId); // valida que el estudiante exista
		return enrollmentRepository.findByStudent_IdOrderByEnrollmentDateAsc(studentId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<EnrollmentResponse> findByPromotion(Integer yearPromotion) {
		return enrollmentRepository.findByStudent_YearPromotionOrderByEnrollmentDateAsc(yearPromotion)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private Enrollment findEntity(UUID id) {
		return enrollmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
	}

	private void applyRequest(Enrollment enrollment, EnrollmentRequest request) {
		Student student = studentService.getReference(request.studentId());
		Course course = courseService.getReference(request.courseId());
		Semester semester = semesterService.getReference(request.semesterId());
		State state = stateService.getReference(request.stateId());
		enrollment.setStudent(student);
		enrollment.setCourse(course);
		enrollment.setSemester(semester);
		enrollment.setState(state);
		enrollment.setEnrollmentDate(request.enrollmentDate());
		enrollment.setResolutionFile(request.resolutionFileId() == null
				? null
				: storedFileService.getReference(request.resolutionFileId(), FilePurpose.ENROLLMENT_RESOLUTION));
		enrollment.setObservations(request.observations());
	}

	private EnrollmentResponse toResponse(Enrollment enrollment) {
		Student student = enrollment.getStudent();
		User user = student.getUser();
		Course course = enrollment.getCourse();
		Semester semester = enrollment.getSemester();
		State state = enrollment.getState();
		return new EnrollmentResponse(
				enrollment.getId(),
				student.getId(),
				user.getEmail(),
				"%s %s".formatted(user.getFirstName(), user.getLastName()),
				course.getId(),
				course.getCode(),
				course.getName(),
				semester.getId(),
				semester.getYear(),
				semester.getCode(),
				state.getId(),
				state.getCode(),
				state.getName(),
				enrollment.getEnrollmentDate(),
				storedFileService.toSummary(enrollment.getResolutionFile()),
				enrollment.getObservations(),
				enrollment.getCreatedAt(),
				enrollment.getUpdatedAt()
		);
	}

	@Transactional(readOnly = true)
	public Optional<Enrollment> findByCourseAndCui(UUID courseId, String cui) {
		return enrollmentRepository.findByCourse_IdAndStudent_Cui(courseId, cui);
	}
}
