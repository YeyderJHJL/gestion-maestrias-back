package com.claudecoders.masters.user;

import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.security.UserAccountService;
import com.claudecoders.masters.user.dto.UserRequest;
import com.claudecoders.masters.user.dto.UserResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserAccountService userAccountService;

	public UserService(UserRepository userRepository, UserAccountService userAccountService) {
		this.userRepository = userRepository;
		this.userAccountService = userAccountService;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return userRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public UserResponse findById(UUID id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public UserResponse create(UserRequest request) {
		User user = new User();
		applyRequest(user, request);
		return toResponse(userRepository.save(user));
	}

	@Transactional
	public UserResponse update(UUID id, UserRequest request) {
		User user = findEntity(id);
		userAccountService.evictUser(user.getGoogleSub());
		applyRequest(user, request);
		return toResponse(userRepository.save(user));
	}

	@Transactional
	public void delete(UUID id) {
		User user = findEntity(id);
		userAccountService.evictUser(user.getGoogleSub());
		userRepository.delete(user);
	}

	@Transactional(readOnly = true)
	public User getReference(UUID id) {
		return findEntity(id);
	}

	private User findEntity(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User", id));
	}

	private void applyRequest(User user, UserRequest request) {
		user.setEmail(request.email());
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setDni(request.dni());
		user.setRole(request.role());
		user.setActive(request.active() == null || request.active());
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getDni(),
				user.getRole(),
				user.getActive(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
