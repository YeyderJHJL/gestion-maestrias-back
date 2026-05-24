package com.claudecoders.masters.state;

import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StateService {

	private final StateRepository stateRepository;

	public StateService(StateRepository stateRepository) {
		this.stateRepository = stateRepository;
	}

	@Transactional(readOnly = true)
	public State getReference(Integer id) {
		return stateRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("State", id));
	}
}
