package com.claudecoders.masters.student;

import com.claudecoders.masters.file.FilePurpose;
import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.student.dto.StudentRequest;
import com.claudecoders.masters.student.dto.StudentResponse;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

	private final StudentRepository studentRepository;
	private final UserService userService;
	private final StoredFileService storedFileService;

	public StudentService(
			StudentRepository studentRepository,
			UserService userService,
			StoredFileService storedFileService
	) {
		this.studentRepository = studentRepository;
		this.userService = userService;
		this.storedFileService = storedFileService;
	}

	@Transactional(readOnly = true)
	public List<StudentResponse> findAll() {
		return studentRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<StudentResponse> search(Integer yearPromotion, StudentStatus status, String search) {
		String normalized = (search == null || search.isBlank()) ? null : search.trim();
		return studentRepository.search(yearPromotion, status, normalized).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<StudentResponse> findByPromotion(Integer yearPromotion) {
		return studentRepository.findByYearPromotionOrderByUser_LastNameAscUser_FirstNameAsc(yearPromotion).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public StudentResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional(readOnly = true)
	public StudentResponse findByUserId(UUID userId) {
		return studentRepository.findByUser_Id(userId)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Student for user", userId));
	}

	@Transactional
	public StudentResponse create(StudentRequest request) {
		Student student = new Student();
		applyRequest(student, request);
		return toResponse(studentRepository.save(student));
	}

	@Transactional
	public StudentResponse update(UUID id, StudentRequest request) {
		Student student = findEntity(id);
		applyRequest(student, request);
		return toResponse(studentRepository.save(student));
	}

	@Transactional
	public StudentResponse changeStatus(UUID id, StudentStatus status, Boolean active) {
		Student student = findEntity(id);
		if (status != null) {
			student.setStatus(status);
			studentRepository.save(student);
		}
		if (active != null) {
			userService.setActive(student.getUser().getId(), active);
		}
		return toResponse(findEntity(id));
	}

	@Transactional
	public void delete(UUID id) {
		studentRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Student getReference(UUID id) {
		return findEntity(id);
	}

	@Transactional(readOnly = true)
	public Student getReferenceByUserId(UUID userId) {
		return studentRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Student for user", userId));
	}

	private Student findEntity(UUID id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Student", id));
	}

	private void applyRequest(Student student, StudentRequest request) {
		User user = userService.getReference(request.userId());
		if (user.getRole() != UserRole.STUDENT) {
			throw new BusinessException("El usuario debe tener rol ESTUDIANTE");
		}
		boolean cuiChanged = !request.cui().equals(student.getCui());
		boolean paymentCodeChanged = !request.paymentCode().equals(student.getPaymentCode());
		if (cuiChanged && studentRepository.existsByCui(request.cui())) {
			throw new BusinessException("Ya existe un estudiante con el CUI " + request.cui());
		}
		if (paymentCodeChanged && studentRepository.existsByPaymentCode(request.paymentCode())) {
			throw new BusinessException("Ya existe un estudiante con el codigo de pago " + request.paymentCode());
		}
		student.setUser(user);
		student.setYearPromotion(request.yearPromotion());
		student.setStatus(request.status() == null ? StudentStatus.REGULAR : request.status());
		student.setReactualizationFile(request.reactualizationFileId() == null
				? null
				: storedFileService.getReference(request.reactualizationFileId(), FilePurpose.REACTUALIZATION));
		student.setCui(request.cui());
		student.setPaymentCode(request.paymentCode());
		student.setPhone(request.phone());
	}

	private StudentResponse toResponse(Student student) {
		User user = student.getUser();
		return new StudentResponse(
				student.getId(),
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getDni(),
				student.getYearPromotion(),
				student.getStatus(),
				storedFileService.toSummary(student.getReactualizationFile()),
				student.getCui(),
				student.getPaymentCode(),
				student.getPhone(),
				student.getCreatedAt(),
				student.getUpdatedAt()
		);
	}
}
