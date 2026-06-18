package com.claudecoders.masters.student;

import com.claudecoders.masters.file.FilePurpose;
import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.payment.Payment;
import com.claudecoders.masters.payment.PaymentRepository;
import com.claudecoders.masters.program.Program;
import com.claudecoders.masters.program.ProgramRepository;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.student.dto.StudentBulkRequest;
import com.claudecoders.masters.student.dto.StudentImportResponse;
import com.claudecoders.masters.student.dto.StudentImportRowResult;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class StudentImportService {

	private static final int MIN_PROMOTION_YEAR = 2001;
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
	private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");
	private static final Pattern CUI_PATTERN = Pattern.compile("^\\d{6,12}$");

	private final StudentRepository studentRepository;
	private final UserRepository userRepository;
	private final ProgramRepository programRepository;
	private final PaymentRepository paymentRepository;
	private final StoredFileService storedFileService;
	private final TransactionTemplate transactionTemplate;

	public StudentImportService(
			StudentRepository studentRepository,
			UserRepository userRepository,
			ProgramRepository programRepository,
			PaymentRepository paymentRepository,
			StoredFileService storedFileService,
			PlatformTransactionManager transactionManager
	) {
		this.studentRepository = studentRepository;
		this.userRepository = userRepository;
		this.programRepository = programRepository;
		this.paymentRepository = paymentRepository;
		this.storedFileService = storedFileService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	public StudentImportResponse importFromJson(List<StudentBulkRequest> items) {
		if (items == null || items.isEmpty()) {
			throw new BusinessException("Debe enviar al menos un estudiante");
		}

		Program program = programRepository.findFirstByOrderByIdAsc()
				.orElseThrow(() -> new BusinessException("Debe existir un programa configurado para generar pagos"));

		List<StudentImportRowResult> results = new ArrayList<>();
		Set<String> seenEmails = new HashSet<>();
		Set<String> seenDnis = new HashSet<>();
		Set<String> seenCuis = new HashSet<>();
		Set<String> seenPaymentCodes = new HashSet<>();
		int imported = 0;
		int rejected = 0;

		for (int i = 0; i < items.size(); i++) {
			ParsedRow parsed = toParsedRow(items.get(i));
			int rowNumber = i + 1;
			List<String> observations = new ArrayList<>();
			validateRow(parsed, observations, seenEmails, seenDnis, seenCuis, seenPaymentCodes);

			if (!observations.isEmpty()) {
				rejected++;
				results.add(new StudentImportRowResult(
						rowNumber, parsed.email, parsed.dni, parsed.cui, parsed.yearPromotion,
						StudentImportRowResult.Status.REJECTED, null, observations));
				continue;
			}

			try {
				Student saved = transactionTemplate.execute(status -> persistStudent(parsed, program));
				seenEmails.add(parsed.email.toLowerCase(Locale.ROOT));
				if (parsed.dni != null) {
					seenDnis.add(parsed.dni);
				}
				seenCuis.add(parsed.cui);
				seenPaymentCodes.add(parsed.paymentCode);
				imported++;
				results.add(new StudentImportRowResult(
						rowNumber, parsed.email, parsed.dni, parsed.cui, parsed.yearPromotion,
						StudentImportRowResult.Status.IMPORTED,
						saved == null ? null : saved.getId(),
						List.of()));
			} catch (RuntimeException ex) {
				rejected++;
				results.add(new StudentImportRowResult(
						rowNumber, parsed.email, parsed.dni, parsed.cui, parsed.yearPromotion,
						StudentImportRowResult.Status.REJECTED, null,
						List.of("Error al guardar: " + rootMessage(ex))));
			}
		}

		return new StudentImportResponse(results.size(), imported, rejected, results);
	}

	private Student persistStudent(ParsedRow parsed, Program program) {
		User user = new User();
		user.setEmail(parsed.email);
		user.setFirstName(parsed.firstName);
		user.setLastName(parsed.lastName);
		user.setDni(parsed.dni);
		user.setRole(UserRole.STUDENT);
		user.setActive(true);
		User savedUser = userRepository.save(user);

		Student student = new Student();
		student.setUser(savedUser);
		student.setYearPromotion(parsed.yearPromotion);
		student.setStatus(parsed.status == null ? StudentStatus.REGULAR : parsed.status);
		student.setCui(parsed.cui);
		student.setPaymentCode(parsed.paymentCode);
		student.setPhone(parsed.phone);
		if (parsed.reactualizationFileId != null) {
			student.setReactualizationFile(storedFileService.getReference(
					parsed.reactualizationFileId,
					FilePurpose.REACTUALIZATION
			));
		}
		Student savedStudent = studentRepository.save(student);

		for (int number = 1; number <= program.getPensionCount(); number++) {
			Payment payment = new Payment();
			payment.setStudent(savedStudent);
			payment.setPaymentNumber(number);
			paymentRepository.save(payment);
		}
		return savedStudent;
	}

	private void validateRow(
			ParsedRow row,
			List<String> observations,
			Set<String> seenEmails,
			Set<String> seenDnis,
			Set<String> seenCuis,
			Set<String> seenPaymentCodes
	) {
		if (isBlank(row.firstName)) {
			observations.add("Nombres es obligatorio");
		}
		if (isBlank(row.lastName)) {
			observations.add("Apellidos es obligatorio");
		}

		if (isBlank(row.email)) {
			observations.add("Email es obligatorio");
		} else if (!EMAIL_PATTERN.matcher(row.email).matches()) {
			observations.add("Email tiene formato invalido");
		} else if (row.email.length() > 255) {
			observations.add("Email excede los 255 caracteres");
		} else if (seenEmails.contains(row.email.toLowerCase(Locale.ROOT))) {
			observations.add("Email duplicado dentro del archivo");
		} else if (userRepository.existsByEmail(row.email)) {
			observations.add("Email ya registrado");
		}

		if (isBlank(row.dni)) {
			observations.add("DNI es obligatorio");
		} else if (!DNI_PATTERN.matcher(row.dni).matches()) {
			observations.add("DNI debe contener 8 digitos");
		} else if (seenDnis.contains(row.dni)) {
			observations.add("DNI duplicado dentro del archivo");
		} else if (userRepository.existsByDni(row.dni)) {
			observations.add("DNI ya registrado");
		}

		if (isBlank(row.cui)) {
			observations.add("CUI es obligatorio");
		} else if (row.cui.length() > 20) {
			observations.add("CUI excede los 20 caracteres");
		} else if (!CUI_PATTERN.matcher(row.cui).matches()) {
			observations.add("CUI debe contener solo digitos (6 a 12)");
		} else if (seenCuis.contains(row.cui)) {
			observations.add("CUI duplicado dentro del archivo");
		} else if (studentRepository.existsByCui(row.cui)) {
			observations.add("CUI ya registrado");
		}

		if (row.yearPromotion == null) {
			observations.add("Promocion es obligatoria");
		} else if (row.yearPromotion < MIN_PROMOTION_YEAR) {
			observations.add("Promocion debe ser mayor o igual a " + MIN_PROMOTION_YEAR);
		} else if (row.yearPromotion > Year.now().getValue()) {
			observations.add("Promocion no puede ser futura");
		}

		if (isBlank(row.paymentCode)) {
			observations.add("Codigo de pago es obligatorio");
		} else if (row.paymentCode.length() > 100) {
			observations.add("Codigo de pago excede los 100 caracteres");
		} else if (seenPaymentCodes.contains(row.paymentCode)) {
			observations.add("Codigo de pago duplicado dentro del archivo");
		} else if (studentRepository.existsByPaymentCode(row.paymentCode)) {
			observations.add("Codigo de pago ya registrado");
		}

		if (row.phone != null && row.phone.length() > 20) {
			observations.add("Telefono excede los 20 caracteres");
		}
		if (row.statusError != null) {
			observations.add(row.statusError);
		}
	}

	private ParsedRow toParsedRow(StudentBulkRequest item) {
		ParsedRow parsed = new ParsedRow();
		parsed.email = trim(item.email());
		parsed.firstName = trim(item.firstName());
		parsed.lastName = trim(item.lastName());
		parsed.dni = trim(item.dni());
		parsed.cui = trim(item.cui());
		parsed.paymentCode = trim(item.paymentCode());
		parsed.phone = trim(item.phone());
		parsed.yearPromotion = item.yearPromotion();
		parsed.status = item.status();
		parsed.reactualizationFileId = item.reactualizationFileId();
		if (parsed.status == StudentStatus.REACTUALIZATION && parsed.reactualizationFileId == null) {
			parsed.statusError = "Estudiantes recursantes requieren reactualizationFileId";
		}
		return parsed;
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static String rootMessage(Throwable ex) {
		Throwable cur = ex;
		while (cur.getCause() != null && cur.getCause() != cur) {
			cur = cur.getCause();
		}
		return cur.getMessage() == null ? ex.getClass().getSimpleName() : cur.getMessage();
	}

	private static final class ParsedRow {
		String email;
		String firstName;
		String lastName;
		String dni;
		String cui;
		String paymentCode;
		String phone;
		Integer yearPromotion;
		StudentStatus status;
		String statusError;
		UUID reactualizationFileId;
	}
}
