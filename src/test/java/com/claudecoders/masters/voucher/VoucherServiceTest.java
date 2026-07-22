package com.claudecoders.masters.voucher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.claudecoders.masters.file.StoredFile;
import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.payment.Payment;
import com.claudecoders.masters.payment.PaymentService;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentService;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.voucher.dto.VoucherPaymentRequest;
import com.claudecoders.masters.voucher.dto.VoucherRequest;
import com.claudecoders.masters.voucher.dto.VoucherResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

	@Mock
	private VoucherRepository voucherRepository;

	@Mock
	private PaymentService paymentService;

	@Mock
	private StateService stateService;

	@Mock
	private StoredFileService storedFileService;

	@Mock
	private StudentService studentService;

	private VoucherService voucherService;

	@BeforeEach
	void setUp() {
		voucherService = new VoucherService(
				voucherRepository,
				paymentService,
				stateService,
				storedFileService,
				studentService
		);
	}

	@Test
	void createLinksAllPaymentsWhenTheirTotalMatchesTheDeclaredAmount() {
		Student student = student();
		Payment firstPayment = payment(student, 1);
		Payment secondPayment = payment(student, 2);
		State state = new State();
		state.setId(1);
		state.setCode("UPLOADED");
		state.setName("Cargado");
		VoucherRequest request = new VoucherRequest(
				new BigDecimal("840.00"),
				List.of(new VoucherPaymentRequest(firstPayment.getId()), new VoucherPaymentRequest(secondPayment.getId())),
				state.getId(),
				UUID.randomUUID(),
				null,
				"OP-123456"
		);

		when(paymentService.getReference(firstPayment.getId())).thenReturn(firstPayment);
		when(paymentService.getReference(secondPayment.getId())).thenReturn(secondPayment);
		when(stateService.getReference(state.getId())).thenReturn(state);
		when(storedFileService.getReference(any(), any())).thenReturn(new StoredFile());
		when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

		VoucherResponse response = voucherService.create(request);

		assertEquals(new BigDecimal("840.00"), response.declaredAmount());
		assertEquals(2, response.payments().size());
		assertEquals(List.of(firstPayment.getId(), secondPayment.getId()),
				response.payments().stream().map(payment -> payment.paymentId()).toList());
	}

	@Test
	void createRejectsADifferentDeclaredAmount() {
		Student student = student();
		Payment payment = payment(student, 1);
		VoucherRequest request = new VoucherRequest(
				new BigDecimal("249.00"),
				List.of(new VoucherPaymentRequest(payment.getId())),
				1,
				UUID.randomUUID(),
				null,
				"OP-123456"
		);
		when(paymentService.getReference(payment.getId())).thenReturn(payment);

		BusinessException exception = assertThrows(BusinessException.class, () -> voucherService.create(request));

		assertEquals("El monto declarado debe coincidir con la suma de los pagos seleccionados", exception.getMessage());
		verify(voucherRepository, never()).save(any());
	}

	@Test
	void createRejectsPaymentsFromDifferentStudents() {
		Payment firstPayment = payment(student(), 1);
		Payment secondPayment = payment(student(), 2);
		VoucherRequest request = new VoucherRequest(
				new BigDecimal("500.00"),
				List.of(new VoucherPaymentRequest(firstPayment.getId()), new VoucherPaymentRequest(secondPayment.getId())),
				1,
				UUID.randomUUID(),
				null,
				"OP-123456"
		);
		when(paymentService.getReference(firstPayment.getId())).thenReturn(firstPayment);
		when(paymentService.getReference(secondPayment.getId())).thenReturn(secondPayment);

		BusinessException exception = assertThrows(BusinessException.class, () -> voucherService.create(request));

		assertEquals("Todos los pagos de un voucher deben pertenecer al mismo estudiante", exception.getMessage());
	}

	private Student student() {
		User user = new User();
		user.setFirstName("Ana");
		user.setLastName("Paredes");
		user.setEmail("ana@unsa.edu.pe");
		Student student = new Student();
		student.setId(UUID.randomUUID());
		student.setUser(user);
		student.setPaymentCode("PAG-001");
		return student;
	}

	private Payment payment(Student student, int number) {
		Payment payment = new Payment();
		payment.setId(UUID.randomUUID());
		payment.setStudent(student);
		payment.setPaymentNumber(number);
		payment.setConcept(number == 1 ? "Matrícula" : "Pensión N° " + number);
		payment.setAmount(new BigDecimal("420.00"));
		return payment;
	}
}
