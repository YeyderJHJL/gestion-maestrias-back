package com.claudecoders.masters.shared.seed;

import com.claudecoders.masters.program.Program;
import com.claudecoders.masters.program.ProgramRepository;
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

	@Value("${app.seed.admin-email:#{null}}")
	private String adminEmail;

	public DatabaseSeeder(ProgramRepository programRepository, UserRepository userRepository) {
		this.programRepository = programRepository;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		seedPrograms();
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
			programRepository.save(program);
			log.info("Seeded program: {}", name);
		}
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
		// google_sub will be overwritten on the admin's first OAuth login
		admin.setGoogleSub("pending-" + adminEmail);
		admin.setFirstName("Admin");
		admin.setLastName("Sistema");
		admin.setRole(UserRole.ADMIN);
		admin.setActive(true);
		userRepository.save(admin);
		log.info("Seeded admin user: {}", adminEmail);
	}
}
