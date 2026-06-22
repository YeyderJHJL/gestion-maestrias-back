package com.claudecoders.masters.voucher;

import com.claudecoders.masters.file.StoredFile;
import com.claudecoders.masters.file.FilePurpose;
import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.payment.Payment;
import com.claudecoders.masters.payment.PaymentService;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.security.SecurityHelper;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateService;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentService;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.voucher.dto.VoucherRequest;
import com.claudecoders.masters.voucher.dto.VoucherResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoucherService {

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
	public List<VoucherResponse> findAll() {
		return voucherRepository.findAll().stream()
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
		return voucherRepository.findByPayment_Student_IdOrderByCreatedAtDesc(student.getId()).stream()
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
		Payment payment = paymentService.getReference(request.paymentId());
		State state = stateService.getReference(request.stateId());
		StoredFile file = storedFileService.getReference(request.fileId(), FilePurpose.PAYMENT_VOUCHER);
		voucher.setPayment(payment);
		voucher.setState(state);
		voucher.setFile(file);
		voucher.setObservation(request.observation());
	}

	private VoucherResponse toResponse(Voucher voucher) {
		State state = voucher.getState();
		Payment payment = voucher.getPayment();
		Student student = payment.getStudent();
		User user = student.getUser();
		return new VoucherResponse(
				voucher.getId(),
				payment.getId(),
				payment.getPaymentNumber(),
				payment.getConcept(),
				payment.getAmount(),
				payment.getPaymentDate(),
				"%s %s".formatted(user.getFirstName(), user.getLastName()),
				user.getEmail(),
				student.getPaymentCode(),
				state.getId(),
				state.getCode(),
				state.getName(),
				storedFileService.toSummary(voucher.getFile()),
				voucher.getObservation(),
				voucher.getCreatedAt(),
				voucher.getUpdatedAt()
		);
	}
}
