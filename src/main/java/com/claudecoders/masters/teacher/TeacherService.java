package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.teacher.dto.TeacherBulkRequest;
import com.claudecoders.masters.teacher.dto.TeacherPatchRequest;
import com.claudecoders.masters.teacher.dto.TeacherRequest;
import com.claudecoders.masters.teacher.dto.TeacherImportResult;
import com.claudecoders.masters.teacher.dto.TeacherImportRowResult;
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
import java.util.ArrayList;
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
	public TeacherImportResult createBulk(List<TeacherBulkRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      throw new BusinessException("Debe enviar al menos un docente");
    }

    List<TeacherImportRowResult> results = new ArrayList<>();
    int imported = 0;
    int rejected = 0;

    Set<String> emailsInBatch = new HashSet<>();
    Set<String> dnisInBatch = new HashSet<>();

    for (int i = 0; i < requests.size(); i++) {
      TeacherBulkRequest request = requests.get(i);
      int row = i + 1;
      String email = request.email() != null ? request.email().trim().toLowerCase(Locale.ROOT) : null;

      try {
        if (email == null || !emailsInBatch.add(email)) {
          throw new BusinessException("Correo duplicado en el lote");
        }

        if (userService.existsByEmail(request.email())) {
          throw new BusinessException("Ya existe un usuario con el correo '%s'".formatted(request.email()));
        }
				
        String dni = blankToNull(request.dni());
        if (dni != null) {
          if (!dnisInBatch.add(dni)) {
            throw new BusinessException("DNI duplicado en el lote");
        	}
          if (userService.existsByDni(dni)) {
            throw new BusinessException("Ya existe un usuario con el DNI '%s'".formatted(dni));
          }
        }

        TeacherResponse teacher = createFromBulkRequest(request);
        results.add(new TeacherImportRowResult(row, "SUCCESS", request.email(), null, teacher));
        imported++;

      } catch (BusinessException ex) {
        results.add(new TeacherImportRowResult(row, "REJECTED", request.email(), ex.getMessage(), null));
        rejected++;
      }
    }

    return new TeacherImportResult(imported, rejected, results);
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

	@Transactional(readOnly = true)
	public Teacher getEntityByUserId(UUID userId) {
		return teacherRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher for user", userId));
	}

	@Transactional
	public TeacherResponse changeStatus(UUID id, boolean active) {
		Teacher teacher = findEntity(id);
		User user = teacher.getUser();
		if (user.getActive() != null && user.getActive() == active) {
			return toResponse(teacher);
		}
		userService.setActive(user.getId(), active);
		return toResponse(findEntity(id));
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
		teacher.setUniversity(request.university());
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
		teacher.setUniversity(request.university());
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
		if (request.university() != null) {
			teacher.setUniversity(request.university());
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
				teacher.getUniversity(),
				teacher.getCreatedAt(),
				teacher.getUpdatedAt()
		);
	}
}
