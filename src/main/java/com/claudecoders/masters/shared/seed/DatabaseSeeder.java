package com.claudecoders.masters.shared.seed;

import com.claudecoders.masters.program.Program;
import com.claudecoders.masters.program.ProgramRepository;
import com.claudecoders.masters.state.State;
import com.claudecoders.masters.state.StateRepository;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import com.claudecoders.masters.shared.enums.UserRole;
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
	private final UserRepository userRepository;
	private final StateRepository stateRepository;

	@Value("${app.seed.admin-email:#{null}}")
	private String adminEmail;

	public DatabaseSeeder(
			ProgramRepository programRepository,
			UserRepository userRepository,
			StateRepository stateRepository
	) {
		this.programRepository = programRepository;
		this.userRepository = userRepository;
		this.stateRepository = stateRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		seedPrograms();
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
