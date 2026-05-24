package com.claudecoders.masters.program;

import com.claudecoders.masters.program.dto.ProgramRequest;
import com.claudecoders.masters.program.dto.ProgramResponse;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramService {

	private final ProgramRepository programRepository;

	public ProgramService(ProgramRepository programRepository) {
		this.programRepository = programRepository;
	}

	@Transactional(readOnly = true)
	public List<ProgramResponse> findAll() {
		return programRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProgramResponse findById(Integer id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public ProgramResponse create(ProgramRequest request) {
		Program program = new Program();
		applyRequest(program, request);
		return toResponse(programRepository.save(program));
	}

	@Transactional
	public ProgramResponse update(Integer id, ProgramRequest request) {
		Program program = findEntity(id);
		applyRequest(program, request);
		return toResponse(programRepository.save(program));
	}

	@Transactional
	public void delete(Integer id) {
		programRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Program getReference(Integer id) {
		return findEntity(id);
	}

	private Program findEntity(Integer id) {
		return programRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Program", id));
	}

	private void applyRequest(Program program, ProgramRequest request) {
		program.setName(request.name());
	}

	private ProgramResponse toResponse(Program program) {
		return new ProgramResponse(
				program.getId(),
				program.getName(),
				program.getCreatedAt(),
				program.getUpdatedAt()
		);
	}
}
