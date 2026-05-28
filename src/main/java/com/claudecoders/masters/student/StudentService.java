package com.claudecoders.masters.student;

import com.claudecoders.masters.promotion.Promotion;
import com.claudecoders.masters.promotion.PromotionService;
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
	private final PromotionService promotionService;

	public StudentService(
			StudentRepository studentRepository,
			UserService userService,
			PromotionService promotionService
	) {
		this.studentRepository = studentRepository;
		this.userService = userService;
		this.promotionService = promotionService;
	}

	@Transactional(readOnly = true)
	public List<StudentResponse> findAll() {
		return studentRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public StudentResponse findById(UUID id) {
		return toResponse(findEntity(id));
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
	public void delete(UUID id) {
		studentRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public StudentResponse findByUserId(UUID userId) {
		return studentRepository.findByUser_Id(userId)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Student for user", userId));
	}

	@Transactional(readOnly = true)
	public Student getReference(UUID id) {
		return findEntity(id);
	}

	private Student findEntity(UUID id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Student", id));
	}

	private void applyRequest(Student student, StudentRequest request) {
		User user = userService.getReference(request.userId());
		if (user.getRole() != UserRole.STUDENT) {
			throw new BusinessException("User must have STUDENT role");
		}
		Promotion promotion = promotionService.getReference(request.promotionId());
		student.setUser(user);
		student.setPromotion(promotion);
		student.setCui(request.cui());
		student.setPaymentCode(request.paymentCode());
		student.setPhone(request.phone());
	}

	private StudentResponse toResponse(Student student) {
		User user = student.getUser();
		Promotion promotion = student.getPromotion();
		return new StudentResponse(
				student.getId(),
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				promotion.getId(),
				promotion.getName(),
				student.getCui(),
				student.getPaymentCode(),
				student.getPhone(),
				student.getCreatedAt(),
				student.getUpdatedAt()
		);
	}
}
