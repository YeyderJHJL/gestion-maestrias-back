package com.claudecoders.masters.user;

import com.claudecoders.masters.promotion.Promotion;
import com.claudecoders.masters.promotion.PromotionService;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.security.UserAccountService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentRepository;
import com.claudecoders.masters.teacher.Teacher;
import com.claudecoders.masters.teacher.TeacherRepository;
import com.claudecoders.masters.user.dto.StudentProfileResponse;
import com.claudecoders.masters.user.dto.TeacherProfileResponse;
import com.claudecoders.masters.user.dto.UserCreateRequest;
import com.claudecoders.masters.user.dto.UserRequest;
import com.claudecoders.masters.user.dto.UserProfileResponse;
import com.claudecoders.masters.user.dto.UserResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserAccountService userAccountService;
	private final TeacherRepository teacherRepository;
	private final StudentRepository studentRepository;
	private final PromotionService promotionService;

	public UserService(
			UserRepository userRepository,
			UserAccountService userAccountService,
			TeacherRepository teacherRepository,
			StudentRepository studentRepository,
			PromotionService promotionService
	) {
		this.userRepository = userRepository;
		this.userAccountService = userAccountService;
		this.teacherRepository = teacherRepository;
		this.studentRepository = studentRepository;
		this.promotionService = promotionService;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return userRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public UserResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public UserProfileResponse create(UserCreateRequest request) {
		validateCreateRequest(request);
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException("Ya existe un usuario con el correo " + request.email());
		}

		User user = new User();
		applyCreateRequest(user, request);
		User savedUser = userRepository.save(user);

		TeacherProfileResponse teacher = null;
		StudentProfileResponse student = null;
		if (request.role() == UserRole.TEACHER) {
			teacher = createTeacher(savedUser, request.teacher());
		} else if (request.role() == UserRole.STUDENT) {
			student = createStudent(savedUser, request.student());
		}

		return toProfileResponse(savedUser, student, teacher);
	}

	@Transactional
	public UserResponse create(UserRequest request) {
		User user = new User();
		applyRequest(user, request);
		return toResponse(userRepository.save(user));
	}

	@Transactional
	public User createEntity(UserRequest request) {
		User user = new User();
		applyRequest(user, request);
		return userRepository.save(user);
	}

	@Transactional
	public UserResponse update(UUID id, UserRequest request) {
		User user = findEntity(id);
		userAccountService.evictUser(user.getGoogleSub());
		applyRequest(user, request);
		return toResponse(userRepository.save(user));
	}

	@Transactional
	public void delete(UUID id) {
		User user = findEntity(id);
		userAccountService.evictUser(user.getGoogleSub());
		userRepository.delete(user);
	}

	@Transactional(readOnly = true)
	public User getReference(UUID id) {
		return findEntity(id);
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public boolean existsByDni(String dni) {
		return dni != null && !dni.isBlank() && userRepository.existsByDni(dni);
	}

	private User findEntity(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User", id));
	}

	private void validateCreateRequest(UserCreateRequest request) {
		switch (request.role()) {
			case TEACHER -> {
				if (request.teacher() == null) {
					throw new BusinessException("Debe enviar los datos de docente");
				}
				if (request.student() != null) {
					throw new BusinessException("Un docente no puede incluir datos de estudiante");
				}
			}
			case STUDENT -> {
				if (request.student() == null) {
					throw new BusinessException("Debe enviar los datos de estudiante");
				}
				if (request.teacher() != null) {
					throw new BusinessException("Un estudiante no puede incluir datos de docente");
				}
			}
			default -> {
				if (request.teacher() != null || request.student() != null) {
					throw new BusinessException("Solo docentes y estudiantes pueden incluir datos de perfil");
				}
			}
		}
	}

	private void applyCreateRequest(User user, UserCreateRequest request) {
		user.setEmail(request.email());
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setDni(request.dni());
		user.setRole(request.role());
		user.setActive(request.active() == null || request.active());
	}

	private void applyRequest(User user, UserRequest request) {
		user.setEmail(request.email());
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setDni(request.dni());
		user.setRole(request.role());
		user.setActive(request.active() == null || request.active());
	}

	private TeacherProfileResponse createTeacher(User user, UserCreateRequest.TeacherProfileRequest request) {
		Teacher teacher = new Teacher();
		teacher.setUser(user);
		teacher.setCategory(request.category());
		teacher.setRegime(request.regime());
		teacher.setAcademicDegree(request.academicDegree());
		teacher.setSpecialty(request.specialty());
		teacher.setType(request.type());
		teacher.setPhone(request.phone());
		return toTeacherProfileResponse(teacherRepository.save(teacher));
	}

	private StudentProfileResponse createStudent(User user, UserCreateRequest.StudentProfileRequest request) {
		Promotion promotion = promotionService.getReference(request.promotionId());
		Student student = new Student();
		student.setUser(user);
		student.setPromotion(promotion);
		student.setCui(request.cui());
		student.setPaymentCode(request.paymentCode());
		student.setPhone(request.phone());
		return toStudentProfileResponse(studentRepository.save(student));
	}

	private UserProfileResponse toProfileResponse(
			User user,
			StudentProfileResponse student,
			TeacherProfileResponse teacher
	) {
		return new UserProfileResponse(
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getDni(),
				user.getRole(),
				user.getActive(),
				user.getCreatedAt(),
				user.getUpdatedAt(),
				student,
				teacher
		);
	}

	private StudentProfileResponse toStudentProfileResponse(Student student) {
		Promotion promotion = student.getPromotion();
		return new StudentProfileResponse(
				student.getId(),
				promotion.getId(),
				promotion.getName(),
				student.getCui(),
				student.getPaymentCode(),
				student.getPhone(),
				student.getCreatedAt(),
				student.getUpdatedAt()
		);
	}

	private TeacherProfileResponse toTeacherProfileResponse(Teacher teacher) {
		return new TeacherProfileResponse(
				teacher.getId(),
				teacher.getCategory(),
				teacher.getRegime(),
				teacher.getAcademicDegree(),
				teacher.getSpecialty(),
				teacher.getType(),
				teacher.getPhone(),
				teacher.getCreatedAt(),
				teacher.getUpdatedAt()
		);
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getDni(),
				user.getRole(),
				user.getActive(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
