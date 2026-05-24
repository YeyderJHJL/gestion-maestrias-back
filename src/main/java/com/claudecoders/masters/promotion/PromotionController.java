package com.claudecoders.masters.promotion;

import com.claudecoders.masters.promotion.dto.PromotionRequest;
import com.claudecoders.masters.promotion.dto.PromotionResponse;
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

@Tag(name = "Promotions", description = "Promotion management")
@RestController
@RequestMapping("/promotions")
public class PromotionController {

	private final PromotionService promotionService;

	public PromotionController(PromotionService promotionService) {
		this.promotionService = promotionService;
	}

	@Operation(summary = "List promotions")
	@GetMapping
	public ApiResponse<List<PromotionResponse>> findAll() {
		return ApiResponse.ok(promotionService.findAll());
	}

	@Operation(summary = "Get promotion by id")
	@GetMapping("/{id}")
	public ApiResponse<PromotionResponse> findById(@PathVariable Integer id) {
		return ApiResponse.ok(promotionService.findById(id));
	}

	@Operation(summary = "Create promotion")
	@PostMapping
	public ResponseEntity<ApiResponse<PromotionResponse>> create(@Valid @RequestBody PromotionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(promotionService.create(request), "Promotion created"));
	}

	@Operation(summary = "Update promotion")
	@PutMapping("/{id}")
	public ApiResponse<PromotionResponse> update(
			@PathVariable Integer id,
			@Valid @RequestBody PromotionRequest request
	) {
		return ApiResponse.ok(promotionService.update(id, request), "Promotion updated");
	}

	@Operation(summary = "Delete promotion")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Integer id) {
		promotionService.delete(id);
		return ApiResponse.ok(null, "Promotion deleted");
	}
}
