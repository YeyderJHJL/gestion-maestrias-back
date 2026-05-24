package com.claudecoders.masters.promotion;

import com.claudecoders.masters.program.Program;
import com.claudecoders.masters.program.ProgramService;
import com.claudecoders.masters.promotion.dto.PromotionRequest;
import com.claudecoders.masters.promotion.dto.PromotionResponse;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final ProgramService programService;

	public PromotionService(PromotionRepository promotionRepository, ProgramService programService) {
		this.promotionRepository = promotionRepository;
		this.programService = programService;
	}

	@Transactional(readOnly = true)
	public List<PromotionResponse> findAll() {
		return promotionRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public PromotionResponse findById(Integer id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public PromotionResponse create(PromotionRequest request) {
		Promotion promotion = new Promotion();
		applyRequest(promotion, request);
		return toResponse(promotionRepository.save(promotion));
	}

	@Transactional
	public PromotionResponse update(Integer id, PromotionRequest request) {
		Promotion promotion = findEntity(id);
		applyRequest(promotion, request);
		return toResponse(promotionRepository.save(promotion));
	}

	@Transactional
	public void delete(Integer id) {
		promotionRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Promotion getReference(Integer id) {
		return findEntity(id);
	}

	private Promotion findEntity(Integer id) {
		return promotionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Promotion", id));
	}

	private void applyRequest(Promotion promotion, PromotionRequest request) {
		Program program = programService.getReference(request.programId());
		promotion.setProgram(program);
		promotion.setName(request.name());
		promotion.setPeriod(request.period());
		promotion.setYear(request.year());
	}

	private PromotionResponse toResponse(Promotion promotion) {
		return new PromotionResponse(
				promotion.getId(),
				promotion.getProgram().getId(),
				promotion.getProgram().getName(),
				promotion.getName(),
				promotion.getPeriod(),
				promotion.getYear(),
				promotion.getCreatedAt(),
				promotion.getUpdatedAt()
		);
	}
}
