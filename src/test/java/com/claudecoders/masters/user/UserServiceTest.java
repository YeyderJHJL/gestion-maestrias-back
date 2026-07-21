package com.claudecoders.masters.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.payment.PaymentRepository;
import com.claudecoders.masters.program.ProgramRepository;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.security.UserAccountService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentRepository;
import com.claudecoders.masters.teacher.TeacherRepository;
import com.claudecoders.masters.user.dto.UserCreateRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private TeacherRepository teacherRepository;

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private StoredFileService storedFileService;

	@Mock
	private ProgramRepository programRepository;

	@Mock
	private PaymentRepository paymentRepository;

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService(
				userRepository,
				new UserAccountService(userRepository),
				teacherRepository,
				studentRepository,
				storedFileService,
				programRepository,
				paymentRepository
		);
	}

	@Test
	void updateRejectsAnEmailAssignedToAnotherUser() {
		UUID userId = UUID.randomUUID();
		User user = userWithId(userId);
		String email = "existing@unsa.edu.pe";
		UserCreateRequest request = new UserCreateRequest(
				email,
				"Juan",
				"Quispe",
				"71234567",
				UserRole.STUDENT,
				true,
				null,
				new UserCreateRequest.StudentProfileRequest(2020, null, null, "2024001", "PAG-001", "951234567")
		);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmail(email)).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> userService.update(userId, request));

		assertEquals("Ya existe un usuario con el correo " + email, exception.getMessage());
	}

	@Test
	void updateRejectsACuiAssignedToAnotherStudent() {
		UUID userId = UUID.randomUUID();
		User user = userWithId(userId);
		Student student = new Student();
		student.setCui("2023001");
		student.setPaymentCode("PAG-OLD");
		UserCreateRequest request = new UserCreateRequest(
				"juan.quispe@unsa.edu.pe",
				"Juan",
				"Quispe",
				"71234567",
				UserRole.STUDENT,
				true,
				null,
				new UserCreateRequest.StudentProfileRequest(2020, null, null, "2024001", "PAG-001", "951234567")
		);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(studentRepository.findByUser_Id(userId)).thenReturn(Optional.of(student));
		when(studentRepository.existsByCui("2024001")).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> userService.update(userId, request));

		assertEquals("Ya existe un estudiante con el CUI 2024001", exception.getMessage());
	}

	private User userWithId(UUID id) {
		User user = new User();
		user.setId(id);
		return user;
	}
}
