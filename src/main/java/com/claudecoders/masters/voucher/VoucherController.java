package com.claudecoders.masters.voucher;

import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.voucher.dto.VoucherRequest;
import com.claudecoders.masters.voucher.dto.VoucherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vouchers", description = "Voucher management")
@RestController
@RequestMapping("/vouchers")
public class VoucherController {

	private final VoucherService voucherService;

	public VoucherController(VoucherService voucherService) {
		this.voucherService = voucherService;
	}

	@Operation(summary = "List vouchers")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR},
			description = "List all vouchers (only ADMIN and COORDINATOR can access)")
	@GetMapping
	public ApiResponse<List<VoucherResponse>> findAll() {
		return ApiResponse.ok(voucherService.findAll());
	}

	@Operation(summary = "List my vouchers",
			description = "Returns the authenticated student's vouchers with the state of each one")
	@Authorize(roles = {UserRole.STUDENT},
			description = "List the authenticated student's own vouchers (only STUDENT can access)")
	@GetMapping("/my")
	public ApiResponse<List<VoucherResponse>> findMy() {
		return ApiResponse.ok(voucherService.findMy());
	}

	@Operation(summary = "Get voucher by id")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR, UserRole.STUDENT},
			description = "Get voucher by id (only ADMIN, COORDINATOR and STUDENT can access)")
	@GetMapping("/{id}")
	public ApiResponse<VoucherResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(voucherService.findById(id));
	}

	@Operation(summary = "Create voucher")
	@Authorize(roles = {UserRole.ADMIN, UserRole.STUDENT},
			description = "Create new voucher (only ADMIN and STUDENT can access)")
	@PostMapping
	public ResponseEntity<ApiResponse<VoucherResponse>> create(@Valid @RequestBody VoucherRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(voucherService.create(request), "Voucher created"));
	}

	@Operation(summary = "Update voucher")
	@Authorize(roles = {UserRole.ADMIN},
			description = "Update voucher (only ADMIN can access)")
	@PutMapping("/{id}")
	public ApiResponse<VoucherResponse> update(@PathVariable UUID id, @Valid @RequestBody VoucherRequest request) {
		return ApiResponse.ok(voucherService.update(id, request), "Voucher updated");
	}

	@Operation(summary = "Delete voucher")
	@Authorize(roles = {UserRole.ADMIN},
			description = "Delete voucher (only ADMIN can access)")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		voucherService.delete(id);
		return ApiResponse.ok(null, "Voucher deleted");
	}
}
