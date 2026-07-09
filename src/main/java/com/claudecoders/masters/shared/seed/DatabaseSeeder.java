package com.claudecoders.masters.shared.seed;

import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.course.CourseRepository;
import com.claudecoders.masters.program.Program;
import com.claudecoders.masters.program.ProgramRepository;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateRepository;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import com.claudecoders.masters.shared.enums.UserRole;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

	private final ProgramRepository programRepository;
	private final CourseRepository courseRepository;
	private final UserRepository userRepository;
	private final StateRepository stateRepository;

	@Value("${app.seed.admin-email:#{null}}")
	private String adminEmail;

	public DatabaseSeeder(
			ProgramRepository programRepository,
			CourseRepository courseRepository,
			UserRepository userRepository,
			StateRepository stateRepository
	) {
		this.programRepository = programRepository;
		this.courseRepository = courseRepository;
		this.userRepository = userRepository;
		this.stateRepository = stateRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		seedPrograms();
		seedCourses();
		seedStates();
		seedAdminUser();
	}

	private void seedPrograms() {
		seedProgram("MAESTRÍA EN INFORMÁTICA");

		// Example: add more programs following the same pattern:
		// seedProgram("MAESTRÍA EN ADMINISTRACIÓN");
		// seedProgram("MAESTRÍA EN EDUCACIÓN");
	}

	private void seedProgram(String name) {
		boolean exists = programRepository.findAll().stream()
				.anyMatch(p -> name.equalsIgnoreCase(p.getName()));
		if (!exists) {
			Program program = new Program();
			program.setName(name);
			program.setPensionCount(14);
			programRepository.save(program);
			log.info("Seeded program: {}", name);
		}
	}

	private void seedCourses() {
		seedCourse("SOCIEDAD DE LA INFORMACIÓN");
		seedCourse("MULTIMEDIA");
		seedCourse("HERRAMIENTAS PARA EL TRABAJO COLABORATIVO");
		seedCourse("REDES Y CONECTIVIDAD");
		seedCourse("E-PEDAGOGÍA");
		seedCourse("ANÁLISIS Y DISEÑO DE SOFTWARE");
		seedCourse("CREACIÓN DE CONTENIDOS, MOBILE LEARNING Y GAMIFICACIÓN EN EL AULA");
		seedCourse("METODOLOGÍA DE LA INVESTIGACIÓN");
		seedCourse("AMBIENTES EDUCATIVOS VIRTUALES");
		seedCourse("TÓPICOS DE INVESTIGACIÓN I");
		seedCourse("SEMINARIO DE TESIS I");
		seedCourse("NEUROCIENCIAS COGNOSCITIVAS EN INFORMÁTICA");
		seedCourse("TÓPICOS DE INVESTIGACIÓN II");
		seedCourse("SEMINARIO DE TESIS II");
	}

	private void seedCourse(String name) {
		String code = courseCode(name);
		boolean exists = courseRepository.findAll().stream()
				.anyMatch(course -> name.equalsIgnoreCase(course.getName()) || code.equalsIgnoreCase(course.getCode()));
		if (exists) {
			return;
		}

		LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 3, 1);
		Course course = new Course();
		course.setCode(code);
		course.setName(name);
		course.setStartDate(startDate);
		course.setEndDate(startDate.plusMonths(5).minusDays(1));
		courseRepository.save(course);
		log.info("Seeded course: {}", name);
	}

	private String courseCode(String name) {
		String code = normalize(name)
				.toUpperCase(Locale.ROOT)
				.replaceAll("[^A-Z0-9]+", "_")
				.replaceAll("^_+|_+$", "");
		return code.length() <= 100 ? code : code.substring(0, 100);
	}

	private String normalize(String value) {
		return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
	}

	private void seedStates() {
		seedState("VOUCHER", "UPLOADED", "Cargado", "Voucher cargado por el estudiante y pendiente de revisión");
		seedState("VOUCHER", "VALIDATED", "Validado", "Voucher validado por administración");
		seedState("VOUCHER", "OBSERVED", "Observado", "Voucher observado y pendiente de corrección");
		seedState("VOUCHER", "REJECTED", "Rechazado", "Voucher rechazado por administración");

		seedState("ENROLLMENT", "ENROLLED", "Matriculado", "Matrícula activa del estudiante en el curso");
		seedState("ENROLLMENT", "WITHDRAWN", "Retirado", "Matrícula retirada por el estudiante o administración");
		seedState("ENROLLMENT", "CANCELLED", "Anulado", "Matrícula anulada");

		seedState("GRADE", "REGISTERED", "Registrada", "Nota registrada");
		seedState("GRADE", "MODIFIED", "Modificada", "Nota modificada");
		seedState("GRADE", "CANCELLED", "Anulada", "Nota anulada");
	}

	private void seedState(String entityType, String code, String name, String description) {
		if (stateRepository.findByEntityTypeAndCode(entityType, code).isPresent()) {
			return;
		}
		State state = new State();
		state.setEntityType(entityType);
		state.setCode(code);
		state.setName(name);
		state.setDescription(description);
		stateRepository.save(state);
		log.info("Seeded state: {}/{}", entityType, code);
	}

	private void seedAdminUser() {
		if (adminEmail == null || adminEmail.isBlank()) {
			log.warn("app.seed.admin-email not set — skipping admin user seed");
			return;
		}
		if (userRepository.existsByEmail(adminEmail)) {
			return;
		}
		User admin = new User();
		admin.setEmail(adminEmail);
		admin.setFirstName("Admin");
		admin.setLastName("Sistema");
		admin.setRole(UserRole.ADMIN);
		admin.setActive(true);
		userRepository.save(admin);
		log.info("Seeded admin user: {}", adminEmail);
	}
}
