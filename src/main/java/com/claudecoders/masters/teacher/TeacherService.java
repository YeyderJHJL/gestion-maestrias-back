package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.teacher.dto.TeacherBulkRequest;
import com.claudecoders.masters.teacher.dto.TeacherPatchRequest;
import com.claudecoders.masters.teacher.dto.TeacherRequest;
import com.claudecoders.masters.teacher.dto.TeacherResponse;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.user.UserService;
import com.claudecoders.masters.user.dto.UserRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherService {

	private final TeacherRepository teacherRepository;
	private final UserService userService;

	public TeacherService(TeacherRepository teacherRepository, UserService userService) {
		this.teacherRepository = teacherRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<TeacherResponse> findAll() {
		return teacherRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TeacherResponse> search(
			TeacherCategory category,
			TeacherType type,
			AcademicDegree academicDegree,
			String search
	) {
		String normalized = (search == null || search.isBlank()) ? null : search.trim();
		return teacherRepository.search(category, type, academicDegree, normalized).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public TeacherResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional(readOnly = true)
	public TeacherResponse findByUserId(UUID userId) {
		return teacherRepository.findByUser_Id(userId)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher for user", userId));
	}

	@Transactional
	public TeacherResponse create(TeacherRequest request) {
		Teacher teacher = new Teacher();
		applyRequest(teacher, request);
		return toResponse(teacherRepository.save(teacher));
	}

	@Transactional
	public List<TeacherResponse> createBulk(List<TeacherBulkRequest> requests) {
		validateBulkRequests(requests);
		return requests.stream()
				.map(this::createFromBulkRequest)
				.toList();
	}

	@Transactional
	public TeacherResponse update(UUID id, TeacherRequest request) {
		Teacher teacher = findEntity(id);
		applyRequest(teacher, request);
		return toResponse(teacherRepository.save(teacher));
	}

	@Transactional
	public TeacherResponse patch(UUID id, TeacherPatchRequest request) {
		Teacher teacher = findEntity(id);
		applyPatch(teacher, request);
		return toResponse(teacherRepository.save(teacher));
	}

	@Transactional
	public void delete(UUID id) {
		teacherRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Teacher getReference(UUID id) {
		return findEntity(id);
	}

	private Teacher findEntity(UUID id) {
		return teacherRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher", id));
	}

	private void applyRequest(Teacher teacher, TeacherRequest request) {
		User user = userService.getReference(request.userId());
		if (user.getRole() != UserRole.TEACHER) {
			throw new BusinessException("El usuario debe tener rol DOCENTE");
		}
		teacher.setUser(user);
		teacher.setCategory(request.category());
		teacher.setRegime(request.regime());
		teacher.setAcademicDegree(request.academicDegree());
		teacher.setSpecialty(request.specialty());
		teacher.setType(request.type());
		teacher.setPhone(request.phone());
	}

	private TeacherResponse createFromBulkRequest(TeacherBulkRequest request) {
		User user = userService.createEntity(new UserRequest(
				request.email(),
				request.firstName(),
				request.lastName(),
				blankToNull(request.dni()),
				UserRole.TEACHER,
				true
		));

		Teacher teacher = new Teacher();
		teacher.setUser(user);
		teacher.setCategory(request.category());
		teacher.setRegime(request.regime());
		teacher.setAcademicDegree(request.academicDegree());
		teacher.setSpecialty(request.specialty());
		teacher.setType(request.type());
		teacher.setPhone(request.phone());
		return toResponse(teacherRepository.save(teacher));
	}

	private void validateBulkRequests(List<TeacherBulkRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			throw new BusinessException("Debe enviar al menos un docente");
		}

		Set<String> emails = new HashSet<>();
		Set<String> dnis = new HashSet<>();
		for (TeacherBulkRequest request : requests) {
			String email = request.email().trim().toLowerCase(Locale.ROOT);
			if (!emails.add(email)) {
				throw new BusinessException("El correo '%s' está repetido en la carga".formatted(request.email()));
			}
			if (userService.existsByEmail(request.email())) {
				throw new BusinessException("Ya existe un usuario con el correo '%s'".formatted(request.email()));
			}

			String dni = blankToNull(request.dni());
			if (dni != null && !dnis.add(dni)) {
				throw new BusinessException("El DNI '%s' está repetido en la carga".formatted(dni));
			}
			if (userService.existsByDni(dni)) {
				throw new BusinessException("Ya existe un usuario con el DNI '%s'".formatted(dni));
			}
		}
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private void applyPatch(Teacher teacher, TeacherPatchRequest request) {
		if (request.category() != null) {
			teacher.setCategory(request.category());
		}
		if (request.regime() != null) {
			teacher.setRegime(request.regime());
		}
		if (request.academicDegree() != null) {
			teacher.setAcademicDegree(request.academicDegree());
		}
		if (request.specialty() != null) {
			teacher.setSpecialty(request.specialty());
		}
		if (request.type() != null) {
			teacher.setType(request.type());
		}
		if (request.phone() != null) {
			teacher.setPhone(request.phone());
		}
	}

	private TeacherResponse toResponse(Teacher teacher) {
		User user = teacher.getUser();
		return new TeacherResponse(
				teacher.getId(),
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
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
}
