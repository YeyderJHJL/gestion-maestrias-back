package com.claudecoders.masters.promotion;

import com.claudecoders.masters.promotion.dto.PromotionRequest;
import com.claudecoders.masters.promotion.dto.PromotionResponse;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.shared.enums.UserRole;
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

@Tag(name = "Promotions", description = "Promotion management")
@RestController
@RequestMapping("/promotions")
public class PromotionController {

	private final PromotionService promotionService;

	public PromotionController(PromotionService promotionService) {
		this.promotionService = promotionService;
	}

	@Operation(summary = "List promotions")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR }, 
		description = "List all promotions (only ADMIN and COORDINATOR can access)")
	@GetMapping
	public ApiResponse<List<PromotionResponse>> findAll() {
		return ApiResponse.ok(promotionService.findAll());
	}

	@Operation(summary = "Get promotion by id")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER }, 
		description = "Get promotion by id (only ADMIN, COORDINATOR and TEACHER can access)")
	@GetMapping("/{id}")
	public ApiResponse<PromotionResponse> findById(@PathVariable Integer id) {
		return ApiResponse.ok(promotionService.findById(id));
	}

	@Operation(summary = "Create promotion")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Create a new promotion (only ADMIN can access)")
	@PostMapping
	public ResponseEntity<ApiResponse<PromotionResponse>> create(@Valid @RequestBody PromotionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(promotionService.create(request), "Promotion created"));
	}

	@Operation(summary = "Update promotion")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Update promotion information (only ADMIN can access)")
	@PutMapping("/{id}")
	public ApiResponse<PromotionResponse> update(
			@PathVariable Integer id,
			@Valid @RequestBody PromotionRequest request
	) {
		return ApiResponse.ok(promotionService.update(id, request), "Promotion updated");
	}

	@Operation(summary = "Delete promotion")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Delete a promotion (only ADMIN can access)")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Integer id) {
		promotionService.delete(id);
		return ApiResponse.ok(null, "Promotion deleted");
	}
}
