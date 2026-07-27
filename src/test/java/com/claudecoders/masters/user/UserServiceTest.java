package com.claudecoders.masters.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.payment.PaymentRepository;
import com.claudecoders.masters.program.ProgramRepository;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.security.UserAccountService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentRepository;
import com.claudecoders.masters.student.StudentService;
import com.claudecoders.masters.teacher.Teacher;
import com.claudecoders.masters.teacher.TeacherRepository;
import com.claudecoders.masters.teacher.TeacherService;
import com.claudecoders.masters.user.dto.UserCreateRequest;
import java.util.List;
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

	@Test
	void deleteSoftDeletesRoleProfilesBeforeDeletingTheUser() {
		UUID userId = UUID.randomUUID();
		User user = userWithId(userId);
		Teacher teacher = new Teacher();
		Student student = new Student();
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(teacherRepository.findByUser_Id(userId)).thenReturn(Optional.of(teacher));
		when(studentRepository.findByUser_Id(userId)).thenReturn(Optional.of(student));

		userService.delete(userId);

		verify(teacherRepository).delete(teacher);
		verify(studentRepository).delete(student);
		verify(userRepository).delete(user);
	}

	@Test
	void deleteStudentsInBulkSoftDeletesStudentsAndTheirUsers() {
		UUID firstStudentId = UUID.randomUUID();
		UUID secondStudentId = UUID.randomUUID();
		Student firstStudent = studentWithUser(UUID.randomUUID());
		Student secondStudent = studentWithUser(UUID.randomUUID());
		when(studentRepository.findById(firstStudentId)).thenReturn(Optional.of(firstStudent));
		when(studentRepository.findById(secondStudentId)).thenReturn(Optional.of(secondStudent));
		when(studentRepository.findByUser_Id(firstStudent.getUser().getId())).thenReturn(Optional.of(firstStudent));
		when(studentRepository.findByUser_Id(secondStudent.getUser().getId())).thenReturn(Optional.of(secondStudent));
		when(userRepository.findById(firstStudent.getUser().getId())).thenReturn(Optional.of(firstStudent.getUser()));
		when(userRepository.findById(secondStudent.getUser().getId())).thenReturn(Optional.of(secondStudent.getUser()));
		StudentService studentService = new StudentService(studentRepository, userService, storedFileService);

		studentService.deleteBulk(List.of(firstStudentId, secondStudentId));

		verify(studentRepository).delete(firstStudent);
		verify(studentRepository).delete(secondStudent);
		verify(userRepository).delete(firstStudent.getUser());
		verify(userRepository).delete(secondStudent.getUser());
	}

	@Test
	void deleteTeachersInBulkSoftDeletesTeachersAndTheirUsers() {
		UUID firstTeacherId = UUID.randomUUID();
		UUID secondTeacherId = UUID.randomUUID();
		Teacher firstTeacher = teacherWithUser(UUID.randomUUID());
		Teacher secondTeacher = teacherWithUser(UUID.randomUUID());
		when(teacherRepository.findById(firstTeacherId)).thenReturn(Optional.of(firstTeacher));
		when(teacherRepository.findById(secondTeacherId)).thenReturn(Optional.of(secondTeacher));
		when(teacherRepository.findByUser_Id(firstTeacher.getUser().getId())).thenReturn(Optional.of(firstTeacher));
		when(teacherRepository.findByUser_Id(secondTeacher.getUser().getId())).thenReturn(Optional.of(secondTeacher));
		when(userRepository.findById(firstTeacher.getUser().getId())).thenReturn(Optional.of(firstTeacher.getUser()));
		when(userRepository.findById(secondTeacher.getUser().getId())).thenReturn(Optional.of(secondTeacher.getUser()));
		TeacherService teacherService = new TeacherService(teacherRepository, userService);

		teacherService.deleteBulk(List.of(firstTeacherId, secondTeacherId));

		verify(teacherRepository).delete(firstTeacher);
		verify(teacherRepository).delete(secondTeacher);
		verify(userRepository).delete(firstTeacher.getUser());
		verify(userRepository).delete(secondTeacher.getUser());
	}

	private User userWithId(UUID id) {
		User user = new User();
		user.setId(id);
		return user;
	}

	private Student studentWithUser(UUID userId) {
		Student student = new Student();
		student.setUser(userWithId(userId));
		return student;
	}

	private Teacher teacherWithUser(UUID userId) {
		Teacher teacher = new Teacher();
		teacher.setUser(userWithId(userId));
		return teacher;
	}
}
