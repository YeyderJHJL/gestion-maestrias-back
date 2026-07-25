package com.claudecoders.masters.grade;

import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.enrollment.Enrollment;
import com.claudecoders.masters.enrollment.EnrollmentService;
import com.claudecoders.masters.course.CourseService;
import com.claudecoders.masters.grade.dto.GradeRequest;
import com.claudecoders.masters.grade.dto.GradeResponse;
import com.claudecoders.masters.grade.dto.GradeUpdateRequest;
import com.claudecoders.masters.grade.dto.GradeBulkRowRequest;
import com.claudecoders.masters.grade.dto.GradeBulkRowResult;
import com.claudecoders.masters.grade.dto.GradeBulkResponse;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.auditlog.AuditContext;
import com.claudecoders.masters.enrollment.Enrollment;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class GradeService {
	private final GradeRepository gradeRepository;
	private final EnrollmentService enrollmentService;
	private final StateService stateService;
	private final CourseService courseService;
	private final TransactionTemplate transactionTemplate;

	private static final String GRADE_ENTITY_TYPE = "GRADE";
	private static final String STATE_CODE_REGISTERED = "REGISTERED";

	public GradeService(
			GradeRepository gradeRepository,
			EnrollmentService enrollmentService,
			StateService stateService,
			CourseService courseService,
			PlatformTransactionManager transactionManager
	) {
		this.gradeRepository = gradeRepository;
		this.enrollmentService = enrollmentService;
		this.stateService = stateService;
		this.courseService = courseService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	@Transactional(readOnly = true)
	public List<GradeResponse> findAll(UUID enrollmentId, UUID courseId, UUID studentId, UUID currentUserId) {
			courseService.checkAccess(courseId, currentUserId);
			return gradeRepository.findAllWithFilters(enrollmentId, courseId, studentId)
							.stream()
							.map(this::toResponse)
							.toList();
	}
	@Transactional(readOnly = true)
	public GradeResponse findById(UUID id, UUID currentUserId) {
		Grade grade = findEntity(id);
		courseService.checkAccess(grade.getEnrollment().getCourse().getId(), currentUserId);
		return toResponse(findEntity(id));
	}

	@Transactional
	public GradeResponse create(GradeRequest request, UUID currentUserId) {
		Grade grade = new Grade();
		Enrollment enrollment = enrollmentService.getReference(request.enrollmentId());
		courseService.checkAccess(enrollment.getCourse().getId(), currentUserId);
		applyRequest(grade, request);
		return toResponse(gradeRepository.save(grade));
	}

	@Transactional
	public GradeResponse update(UUID id, GradeUpdateRequest request, UUID currentUserId) {
		Grade grade = findEntity(id);
		courseService.checkAccess(grade.getEnrollment().getCourse().getId(), currentUserId);

		AuditContext.setReason(request.reason());

		try {
			applyRequest(grade, request);
			Grade saved = gradeRepository.saveAndFlush(grade);
			return toResponse(saved);
		} finally {
			AuditContext.clear();
		}
	}

	@Transactional
	public void delete(UUID id, String reason, UUID currentUserId) {
		Grade grade = findEntity(id);
		courseService.checkAccess(grade.getEnrollment().getCourse().getId(), currentUserId);

		AuditContext.setReason(reason);

		try {
			gradeRepository.delete(grade);
			gradeRepository.flush();
		} finally {
			AuditContext.clear();
		}
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

	private void applyRequest(Grade grade, GradeUpdateRequest request) {
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

	public GradeBulkResponse createBulk(UUID courseId, List<GradeBulkRowRequest> rows, UUID currentUserId) {
		courseService.checkAccess(courseId, currentUserId);

		List<GradeBulkRowResult> results = new ArrayList<>();
		Set<String> seenCuis = new HashSet<>();
		int imported = 0;
		int rejected = 0;

		for (int i = 0; i < rows.size(); i++) {
			GradeBulkRowRequest row = rows.get(i);
			int rowNumber = i + 2;
			String cui = row.cui();

			if (!seenCuis.add(cui)) {
				rejected++;
				results.add(new GradeBulkRowResult(
						rowNumber, cui, GradeBulkRowResult.Status.REJECTED,
						null, List.of("CUI duplicado dentro del archivo.")));
				continue;
			}

			try {
				UUID gradeId = transactionTemplate.execute(status -> importRow(courseId, cui, row.value()));
				imported++;
				results.add(new GradeBulkRowResult(
						rowNumber, cui, GradeBulkRowResult.Status.IMPORTED, gradeId, List.of()));
			} catch (RuntimeException ex) {
				rejected++;
				results.add(new GradeBulkRowResult(
						rowNumber, cui, GradeBulkRowResult.Status.REJECTED,
						null, List.of(rootMessage(ex))));
			}
		}

		return new GradeBulkResponse(results.size(), imported, rejected, results);
	}

	private UUID importRow(UUID courseId, String cui, Integer value) {
		if (value == null || value < 0 || value > 20) {
			throw new BusinessException("La nota debe estar entre 0 y 20.");
		}

		Enrollment enrollment = enrollmentService.findByCourseAndCui(courseId, cui)
				.orElseThrow(() -> new BusinessException("El estudiante no se encuentra matriculado en este curso."));

		if (gradeRepository.findByEnrollment_Id(enrollment.getId()).isPresent()) {
			throw new BusinessException("Ya existe una nota registrada para este estudiante en este curso.");
		}

		State state = resolveInitialState();

		Grade grade = new Grade();
		grade.setEnrollment(enrollment);
		grade.setState(state);
		grade.setValue(value.shortValue());

		AuditContext.setReason("Importación masiva de notas");
		try {
			return gradeRepository.save(grade).getId();
		} finally {
			AuditContext.clear();
		}
	}

	private State resolveInitialState() {
		return stateService.findByEntityTypeAndCode(GRADE_ENTITY_TYPE, STATE_CODE_REGISTERED);
	}

	private static String rootMessage(Throwable ex) {
		Throwable cur = ex;
		while (cur.getCause() != null && cur.getCause() != cur) {
			cur = cur.getCause();
		}
		return cur.getMessage() == null ? ex.getClass().getSimpleName() : cur.getMessage();
	}

}
