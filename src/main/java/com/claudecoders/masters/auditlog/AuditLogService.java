package com.claudecoders.masters.auditlog;

import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Transactional
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public AuditLogService(
			AuditLogRepository auditLogRepository,
			UserRepository userRepository
	) {
		this.auditLogRepository = auditLogRepository;
		this.userRepository = userRepository;
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAuditEvent(AuditEvent event) {
		if (event.oldValues().isEmpty() && event.newValues().isEmpty()) {
			return;
		}

		User actor = userRepository.getReferenceById(event.actorUserId());
		AuditLog auditLog = new AuditLog();
		auditLog.setUser(actor);
		auditLog.setEntityType(event.entityType());
		auditLog.setEntityId(event.entityId());
		auditLog.setAction(event.action().name());
		auditLog.setOldValue(event.oldValues().isEmpty() ? null : objectMapper.valueToTree(event.oldValues()));
		auditLog.setNewValue(event.newValues().isEmpty() ? null : objectMapper.valueToTree(event.newValues()));
		auditLogRepository.save(auditLog);
	}
}
