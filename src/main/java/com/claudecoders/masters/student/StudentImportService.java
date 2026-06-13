package com.claudecoders.masters.student;

import com.claudecoders.masters.payment.Payment;
import com.claudecoders.masters.payment.PaymentRepository;
import com.claudecoders.masters.program.Program;
import com.claudecoders.masters.program.ProgramRepository;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.student.dto.StudentImportResponse;
import com.claudecoders.masters.student.dto.StudentImportRowResult;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudentImportService {

	private static final int MIN_PROMOTION_YEAR = 2001;
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
	private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");
	private static final Pattern CUI_PATTERN = Pattern.compile("^\\d{6,12}$");

	private static final Map<String, String> COLUMN_ALIASES = Map.of(
			"email", "email",
			"correo", "email",
			"firstname", "firstName",
			"nombres", "firstName",
			"nombre", "firstName",
			"lastname", "lastName",
			"apellidos", "lastName",
			"apellido", "lastName"
	);

	private static final Map<String, String> EXTRA_ALIASES = Map.ofEntries(
			Map.entry("dni", "dni"),
			Map.entry("cui", "cui"),
			Map.entry("yearpromotion", "yearPromotion"),
			Map.entry("aniopromocion", "yearPromotion"),
			Map.entry("anopromocion", "yearPromotion"),
			Map.entry("promocion", "yearPromotion"),
			Map.entry("paymentcode", "paymentCode"),
			Map.entry("codigopago", "paymentCode"),
			Map.entry("codigodepago", "paymentCode"),
			Map.entry("phone", "phone"),
			Map.entry("telefono", "phone"),
			Map.entry("celular", "phone"),
			Map.entry("status", "status"),
			Map.entry("estado", "status")
	);

	private final StudentRepository studentRepository;
	private final UserRepository userRepository;
	private final ProgramRepository programRepository;
	private final PaymentRepository paymentRepository;
	private final TransactionTemplate transactionTemplate;

	public StudentImportService(
			StudentRepository studentRepository,
			UserRepository userRepository,
			ProgramRepository programRepository,
			PaymentRepository paymentRepository,
			PlatformTransactionManager transactionManager
	) {
		this.studentRepository = studentRepository;
		this.userRepository = userRepository;
		this.programRepository = programRepository;
		this.paymentRepository = paymentRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	public StudentImportResponse importFromExcel(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("El archivo Excel es obligatorio");
		}
		String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
		if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
			throw new BusinessException("El archivo debe tener extension .xlsx o .xls");
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

		try (InputStream in = file.getInputStream();
				Workbook workbook = WorkbookFactory.create(in)) {

			Sheet sheet = workbook.getSheetAt(0);
			if (sheet == null) {
				throw new BusinessException("El archivo Excel no contiene hojas");
			}
			Row headerRow = sheet.getRow(sheet.getFirstRowNum());
			if (headerRow == null) {
				throw new BusinessException("El archivo Excel no tiene fila de cabecera");
			}
			Map<String, Integer> columns = resolveColumns(headerRow);
			validateRequiredColumns(columns);

			DataFormatter formatter = new DataFormatter();
			int lastRow = sheet.getLastRowNum();
			for (int rowIdx = headerRow.getRowNum() + 1; rowIdx <= lastRow; rowIdx++) {
				Row row = sheet.getRow(rowIdx);
				if (row == null || isBlankRow(row, formatter)) {
					continue;
				}
				ParsedRow parsed = parseRow(row, columns, formatter);
				List<String> observations = new ArrayList<>();
				validateRow(parsed, observations, seenEmails, seenDnis, seenCuis, seenPaymentCodes);

				if (!observations.isEmpty()) {
					rejected++;
					results.add(new StudentImportRowResult(
							rowIdx + 1,
							parsed.email,
							parsed.dni,
							parsed.cui,
							parsed.yearPromotion,
							StudentImportRowResult.Status.REJECTED,
							null,
							observations
					));
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
							rowIdx + 1,
							parsed.email,
							parsed.dni,
							parsed.cui,
							parsed.yearPromotion,
							StudentImportRowResult.Status.IMPORTED,
							saved == null ? null : saved.getId(),
							List.of()
					));
				} catch (RuntimeException ex) {
					rejected++;
					results.add(new StudentImportRowResult(
							rowIdx + 1,
							parsed.email,
							parsed.dni,
							parsed.cui,
							parsed.yearPromotion,
							StudentImportRowResult.Status.REJECTED,
							null,
							List.of("Error al guardar: " + rootMessage(ex))
					));
				}
			}
		} catch (IOException | EncryptedDocumentException ex) {
			throw new BusinessException("No se pudo leer el archivo Excel: " + ex.getMessage());
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

	private Map<String, Integer> resolveColumns(Row headerRow) {
		Map<String, Integer> columns = new HashMap<>();
		Map<String, String> dictionary = new HashMap<>();
		dictionary.putAll(COLUMN_ALIASES);
		dictionary.putAll(EXTRA_ALIASES);

		DataFormatter formatter = new DataFormatter();
		for (Cell cell : headerRow) {
			String raw = formatter.formatCellValue(cell);
			String key = normalizeHeader(raw);
			String fieldName = dictionary.get(key);
			if (fieldName != null && !columns.containsKey(fieldName)) {
				columns.put(fieldName, cell.getColumnIndex());
			}
		}
		return columns;
	}

	private void validateRequiredColumns(Map<String, Integer> columns) {
		List<String> required = Arrays.asList(
				"email", "firstName", "lastName", "dni", "cui", "yearPromotion", "paymentCode");
		List<String> missing = required.stream().filter(c -> !columns.containsKey(c)).toList();
		if (!missing.isEmpty()) {
			throw new BusinessException("Faltan columnas obligatorias en el Excel: " + String.join(", ", missing));
		}
	}

	private ParsedRow parseRow(Row row, Map<String, Integer> columns, DataFormatter formatter) {
		ParsedRow parsed = new ParsedRow();
		parsed.email = trimToNull(readString(row, columns.get("email"), formatter));
		parsed.firstName = trimToNull(readString(row, columns.get("firstName"), formatter));
		parsed.lastName = trimToNull(readString(row, columns.get("lastName"), formatter));
		parsed.dni = trimToNull(readString(row, columns.get("dni"), formatter));
		parsed.cui = trimToNull(readString(row, columns.get("cui"), formatter));
		parsed.paymentCode = trimToNull(readString(row, columns.get("paymentCode"), formatter));
		parsed.phone = trimToNull(readString(row, columns.get("phone"), formatter));
		parsed.yearPromotion = readInteger(row, columns.get("yearPromotion"), formatter);

		Integer statusIdx = columns.get("status");
		if (statusIdx != null) {
			String statusValue = trimToNull(readString(row, statusIdx, formatter));
			if (statusValue != null) {
				try {
					parsed.status = StudentStatus.valueOf(statusValue.toUpperCase(Locale.ROOT));
				} catch (IllegalArgumentException ex) {
					parsed.statusError = "Estado no reconocido: " + statusValue;
				}
			}
		}
		return parsed;
	}

	private String readString(Row row, Integer index, DataFormatter formatter) {
		if (index == null) {
			return null;
		}
		Cell cell = row.getCell(index);
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.NUMERIC) {
			double numeric = cell.getNumericCellValue();
			if (numeric == Math.floor(numeric) && !Double.isInfinite(numeric)) {
				return Long.toString((long) numeric);
			}
		}
		return formatter.formatCellValue(cell);
	}

	private Integer readInteger(Row row, Integer index, DataFormatter formatter) {
		if (index == null) {
			return null;
		}
		Cell cell = row.getCell(index);
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == CellType.NUMERIC) {
			return (int) cell.getNumericCellValue();
		}
		String value = trimToNull(formatter.formatCellValue(cell));
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value.replace(",", "").trim());
		} catch (NumberFormatException ex) {
			try {
				return (int) Double.parseDouble(value);
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
	}

	private boolean isBlankRow(Row row, DataFormatter formatter) {
		for (Cell cell : row) {
			if (trimToNull(formatter.formatCellValue(cell)) != null) {
				return false;
			}
		}
		return true;
	}

	private static String normalizeHeader(String raw) {
		if (raw == null) {
			return "";
		}
		String lower = raw.toLowerCase(Locale.ROOT).trim();
		StringBuilder builder = new StringBuilder(lower.length());
		for (int i = 0; i < lower.length(); i++) {
			char c = lower.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				builder.append(switch (c) {
					case 'á' -> 'a';
					case 'é' -> 'e';
					case 'í' -> 'i';
					case 'ó' -> 'o';
					case 'ú', 'ü' -> 'u';
					case 'ñ' -> 'n';
					default -> c;
				});
			}
		}
		return builder.toString();
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
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
	}
}
