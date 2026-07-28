package com.claudecoders.masters.voucher;

import com.claudecoders.masters.file.StoredFile;
import com.claudecoders.masters.file.FilePurpose;
import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.payment.Payment;
import com.claudecoders.masters.payment.PaymentService;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.security.SecurityHelper;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentService;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.voucher.dto.VoucherRequest;
import com.claudecoders.masters.voucher.dto.VoucherResponse;
import com.claudecoders.masters.voucher.dto.VoucherPaymentResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoucherService {

	private static final String VOUCHER_STATE_VALIDATED = "VALIDATED";

	private final VoucherRepository voucherRepository;
	private final PaymentService paymentService;
	private final StateService stateService;
	private final StoredFileService storedFileService;
	private final StudentService studentService;

	public VoucherService(
			VoucherRepository voucherRepository,
			PaymentService paymentService,
			StateService stateService,
			StoredFileService storedFileService,
			StudentService studentService
	) {
		this.voucherRepository = voucherRepository;
		this.paymentService = paymentService;
		this.stateService = stateService;
		this.storedFileService = storedFileService;
		this.studentService = studentService;
	}

	@Transactional(readOnly = true)
	public List<VoucherResponse> findAll(String search) {
		List<Voucher> vouchers = (search == null || search.isBlank())
				? voucherRepository.findAll()
				: voucherRepository.search(search.trim());
		return vouchers.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public VoucherResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional(readOnly = true)
	public List<VoucherResponse> findMy() {
		Student student = studentService.getReferenceByUserId(SecurityHelper.currentUserId());
		return voucherRepository.findByPaymentStudentIdOrderByCreatedAtDesc(student.getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public VoucherResponse create(VoucherRequest request) {
		Voucher voucher = new Voucher();
		applyRequest(voucher, request);
		return toResponse(voucherRepository.save(voucher));
	}

	@Transactional
	public VoucherResponse update(UUID id, VoucherRequest request) {
		Voucher voucher = findEntity(id);
		applyRequest(voucher, request);
		return toResponse(voucherRepository.save(voucher));
	}

	@Transactional
	public void delete(UUID id) {
		voucherRepository.delete(findEntity(id));
	}

	private Voucher findEntity(UUID id) {
		return voucherRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Voucher", id));
	}

	private void applyRequest(Voucher voucher, VoucherRequest request) {
		List<Payment> payments = new ArrayList<>();
		Set<UUID> paymentIds = new HashSet<>();
		UUID studentId = null;
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (var paymentRequest : request.payments()) {
			if (!paymentIds.add(paymentRequest.paymentId())) {
				throw new BusinessException("No puede incluir el mismo pago más de una vez en un voucher");
			}
			Payment payment = paymentService.getReference(paymentRequest.paymentId());
			if (payment.getAmount() == null) {
				throw new BusinessException("El pago " + payment.getPaymentNumber() + " no tiene un monto configurado");
			}
			UUID paymentStudentId = payment.getStudent().getId();
			if (studentId != null && !studentId.equals(paymentStudentId)) {
				throw new BusinessException("Todos los pagos de un voucher deben pertenecer al mismo estudiante");
			}
			studentId = paymentStudentId;
			totalAmount = totalAmount.add(payment.getAmount());
			payments.add(payment);
		}
		if (totalAmount.compareTo(request.declaredAmount()) != 0) {
			throw new BusinessException("El monto declarado debe coincidir con la suma de los pagos seleccionados");
		}
		State state = stateService.getReference(request.stateId());
		validatePreviousPayments(studentId, payments, state);
		StoredFile file = storedFileService.getReference(request.fileId(), FilePurpose.PAYMENT_VOUCHER);
		voucher.setDeclaredAmount(request.declaredAmount());
		voucher.setState(state);
		voucher.setFile(file);
		voucher.setObservation(request.observation());
		voucher.setOperationNumber(request.operationNumber());
		voucher.getPayments().removeIf(voucherPayment -> !paymentIds.contains(voucherPayment.getPayment().getId()));
		Set<UUID> existingPaymentIds = voucher.getPayments().stream()
				.map(voucherPayment -> voucherPayment.getPayment().getId())
				.collect(Collectors.toSet());
		payments.stream()
				.filter(payment -> !existingPaymentIds.contains(payment.getId()))
				.forEach(voucher::addPayment);
	}

	private void validatePreviousPayments(UUID studentId, List<Payment> payments, State state) {
		if (payments.isEmpty()) {
			return;
		}
		int minPaymentNumber = payments.stream()
				.map(Payment::getPaymentNumber)
				.min(Integer::compareTo)
				.orElseThrow();
		List<Payment> previousPayments = paymentService.findByStudentId(studentId).stream()
				.filter(payment -> payment.getPaymentNumber() < minPaymentNumber)
				.toList();

		List<Integer> unsubmittedPaymentNumbers = previousPayments.stream()
				.filter(payment -> paymentService.latestVoucherStateCode(payment.getId()) == null)
				.map(Payment::getPaymentNumber)
				.toList();
		if (!unsubmittedPaymentNumbers.isEmpty()) {
			throw new BusinessException(
					"Debe presentar primero el comprobante de los pagos anteriores N° " + joinNumbers(unsubmittedPaymentNumbers));
		}

		if (VOUCHER_STATE_VALIDATED.equals(state.getCode())) {
			List<Integer> unvalidatedPaymentNumbers = previousPayments.stream()
					.filter(payment -> !VOUCHER_STATE_VALIDATED.equals(paymentService.latestVoucherStateCode(payment.getId())))
					.map(Payment::getPaymentNumber)
					.toList();
			if (!unvalidatedPaymentNumbers.isEmpty()) {
				throw new BusinessException(
						"No puede validar este pago porque los pagos anteriores N° " + joinNumbers(unvalidatedPaymentNumbers)
								+ " aún no están validados");
			}
		}
	}

	private String joinNumbers(List<Integer> numbers) {
		return numbers.stream().map(String::valueOf).collect(Collectors.joining(", "));
	}

	private VoucherResponse toResponse(Voucher voucher) {
		State state = voucher.getState();
		List<VoucherPaymentResponse> payments = voucher.getPayments().stream()
				.map(voucherPayment -> {
					Payment payment = voucherPayment.getPayment();
					return new VoucherPaymentResponse(
							payment.getId(),
							payment.getPaymentNumber(),
							payment.getConcept(),
							payment.getAmount(),
							payment.getPaymentDate()
					);
				})
				.toList();
		Student student = voucher.getPayments().getFirst().getPayment().getStudent();
		User user = student.getUser();
		return new VoucherResponse(
				voucher.getId(),
				voucher.getDeclaredAmount(),
				payments,
				student.getId(),
				"%s %s".formatted(user.getFirstName(), user.getLastName()),
				user.getEmail(),
				student.getPaymentCode(),
				user.getDni(),
				student.getCui(),
				state.getId(),
				state.getCode(),
				state.getName(),
				storedFileService.toSummary(voucher.getFile()),
				voucher.getObservation(),
				voucher.getOperationNumber(),
				voucher.getCreatedAt(),
				voucher.getUpdatedAt()
		);
	}
}
