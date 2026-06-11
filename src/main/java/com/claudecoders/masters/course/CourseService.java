package com.claudecoders.masters.course;

import com.claudecoders.masters.course.dto.CourseRequest;
import com.claudecoders.masters.course.dto.CourseResponse;
import com.claudecoders.masters.file.StoredFileService;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

	private final CourseRepository courseRepository;
	private final StoredFileService storedFileService;

	public CourseService(
			CourseRepository courseRepository,
			StoredFileService storedFileService
	) {
		this.courseRepository = courseRepository;
		this.storedFileService = storedFileService;
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
			throw new BusinessException("La fecha de fin del curso debe ser posterior o igual a la fecha de inicio");
		}
		course.setCode(request.code());
		course.setName(request.name());
		course.setStartDate(request.startDate());
		course.setEndDate(request.endDate());
		course.setObservations(request.observations());
		course.setSyllabusFile(request.syllabusFileId() == null ? null : storedFileService.getReference(request.syllabusFileId()));
	}

	private CourseResponse toResponse(Course course) {
		return new CourseResponse(
				course.getId(),
				course.getCode(),
				course.getName(),
				course.getStartDate(),
				course.getEndDate(),
				course.getObservations(),
				storedFileService.toSummary(course.getSyllabusFile()),
				course.getCreatedAt(),
				course.getUpdatedAt()
		);
	}
}
