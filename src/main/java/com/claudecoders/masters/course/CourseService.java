package com.claudecoders.masters.course;

import com.claudecoders.masters.course.dto.CourseRequest;
import com.claudecoders.masters.course.dto.CourseResponse;
import com.claudecoders.masters.program.Program;
import com.claudecoders.masters.program.ProgramService;
import com.claudecoders.masters.promotion.Promotion;
import com.claudecoders.masters.promotion.PromotionService;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

	private final CourseRepository courseRepository;
	private final ProgramService programService;
	private final PromotionService promotionService;

	public CourseService(
			CourseRepository courseRepository,
			ProgramService programService,
			PromotionService promotionService
	) {
		this.courseRepository = courseRepository;
		this.programService = programService;
		this.promotionService = promotionService;
	}

	@Transactional(readOnly = true)
	public List<CourseResponse> findAll() {
		return courseRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public CourseResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public CourseResponse create(CourseRequest request) {
		Course course = new Course();
		applyRequest(course, request);
		return toResponse(courseRepository.save(course));
	}

	@Transactional
	public CourseResponse update(UUID id, CourseRequest request) {
		Course course = findEntity(id);
		applyRequest(course, request);
		return toResponse(courseRepository.save(course));
	}

	@Transactional
	public void delete(UUID id) {
		courseRepository.delete(findEntity(id));
	}

	@Transactional(readOnly = true)
	public Course getReference(UUID id) {
		return findEntity(id);
	}

	private Course findEntity(UUID id) {
		return courseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Course", id));
	}

	private void applyRequest(Course course, CourseRequest request) {
		if (request.endDate().isBefore(request.startDate())) {
			throw new BusinessException("Course end date must be on or after start date");
		}
		Program program = programService.getReference(request.programId());
		Promotion promotion = promotionService.getReference(request.promotionId());
		if (!promotion.getProgram().getId().equals(program.getId())) {
			throw new BusinessException("Promotion does not belong to the selected program");
		}
		course.setProgram(program);
		course.setPromotion(promotion);
		course.setCode(request.code());
		course.setName(request.name());
		course.setType(request.type());
		course.setStartDate(request.startDate());
		course.setEndDate(request.endDate());
		course.setObservations(request.observations());
		course.setSyllabusUrl(request.syllabusUrl());
	}

	private CourseResponse toResponse(Course course) {
		Program program = course.getProgram();
		Promotion promotion = course.getPromotion();
		return new CourseResponse(
				course.getId(),
				program.getId(),
				program.getName(),
				promotion.getId(),
				promotion.getName(),
				course.getCode(),
				course.getName(),
				course.getType(),
				course.getStartDate(),
				course.getEndDate(),
				course.getObservations(),
				course.getSyllabusUrl(),
				course.getCreatedAt(),
				course.getUpdatedAt()
		);
	}
}
