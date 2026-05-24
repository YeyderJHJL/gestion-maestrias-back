package com.claudecoders.masters.program;

import com.claudecoders.masters.program.dto.ProgramRequest;
import com.claudecoders.masters.program.dto.ProgramResponse;
import com.claudecoders.masters.shared.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Programs", description = "Program management")
@RestController
@RequestMapping("/programs")
public class ProgramController {

	private final ProgramService programService;

	public ProgramController(ProgramService programService) {
		this.programService = programService;
	}

	@Operation(summary = "List programs")
	@GetMapping
	public ApiResponse<List<ProgramResponse>> findAll() {
		return ApiResponse.ok(programService.findAll());
	}

	@Operation(summary = "Get program by id")
	@GetMapping("/{id}")
	public ApiResponse<ProgramResponse> findById(@PathVariable Integer id) {
		return ApiResponse.ok(programService.findById(id));
	}

	@Operation(summary = "Create program")
	@PostMapping
	public ResponseEntity<ApiResponse<ProgramResponse>> create(@Valid @RequestBody ProgramRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(programService.create(request), "Program created"));
	}

	@Operation(summary = "Update program")
	@PutMapping("/{id}")
	public ApiResponse<ProgramResponse> update(@PathVariable Integer id, @Valid @RequestBody ProgramRequest request) {
		return ApiResponse.ok(programService.update(id, request), "Program updated");
	}

	@Operation(summary = "Delete program")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Integer id) {
		programService.delete(id);
		return ApiResponse.ok(null, "Program deleted");
	}
}
