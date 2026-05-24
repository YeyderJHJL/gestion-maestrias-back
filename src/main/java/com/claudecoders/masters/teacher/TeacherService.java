package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.teacher.dto.TeacherRequest;
import com.claudecoders.masters.teacher.dto.TeacherResponse;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRole;
import com.claudecoders.masters.user.UserService;
import java.util.List;
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
	public TeacherResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public TeacherResponse create(TeacherRequest request) {
		Teacher teacher = new Teacher();
		applyRequest(teacher, request);
		return toResponse(teacherRepository.save(teacher));
	}

	@Transactional
	public TeacherResponse update(UUID id, TeacherRequest request) {
		Teacher teacher = findEntity(id);
		applyRequest(teacher, request);
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
			throw new BusinessException("User must have TEACHER role");
		}
		teacher.setUser(user);
		teacher.setCategory(request.category());
		teacher.setRegime(request.regime());
		teacher.setAcademicDegree(request.academicDegree());
		teacher.setSpecialty(request.specialty());
		teacher.setType(request.type());
		teacher.setPhone(request.phone());
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
