package com.claudecoders.masters.auditlog;

import com.claudecoders.masters.shared.audit.Auditable;
import com.claudecoders.masters.shared.security.CurrentUserProvider;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AuditHibernateListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

	private final ApplicationEventPublisher publisher;
	private final CurrentUserProvider currentUserProvider;
	private final PersistenceUnitUtil persistenceUnitUtil;

	public AuditHibernateListener(
			EntityManagerFactory entityManagerFactory,
			ApplicationEventPublisher publisher,
			CurrentUserProvider currentUserProvider
	) {
		this.publisher = publisher;
		this.currentUserProvider = currentUserProvider;
		this.persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();

		SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
		EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
		registry.appendListeners(EventType.POST_INSERT, this);
		registry.appendListeners(EventType.POST_UPDATE, this);
		registry.appendListeners(EventType.POST_DELETE, this);
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		if (!(event.getEntity() instanceof Auditable auditable)) {
			return;
		}
		Map<String, Object> newValues = valuesFromState(auditable, event.getPersister(), event.getState(), null);
		publish(auditable, AuditAction.CREATED, Map.of(), newValues);
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (!(event.getEntity() instanceof Auditable auditable)) {
			return;
		}
		int[] dirtyProperties = event.getDirtyProperties();
		Map<String, Object> oldValues = valuesFromState(
				auditable,
				event.getPersister(),
				event.getOldState(),
				dirtyProperties
		);
		Map<String, Object> newValues = valuesFromState(
				auditable,
				event.getPersister(),
				event.getState(),
				dirtyProperties
		);
		publish(auditable, AuditAction.UPDATED, oldValues, newValues);
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if (!(event.getEntity() instanceof Auditable auditable)) {
			return;
		}
		Map<String, Object> oldValues = valuesFromState(
				auditable,
				event.getPersister(),
				event.getDeletedState(),
				null
		);
		publish(auditable, AuditAction.DELETED, oldValues, Map.of());
	}

	@Override
	public boolean requiresPostCommitHandling(EntityPersister persister) {
		return false;
	}

	private void publish(
			Auditable auditable,
			AuditAction action,
			Map<String, Object> oldValues,
			Map<String, Object> newValues
	) {
		if (oldValues.isEmpty() && newValues.isEmpty()) {
			return;
		}
		currentUserProvider.currentUserId()
				.ifPresent(actorUserId -> publisher.publishEvent(new AuditEvent(
						actorUserId,
						auditable.getAuditType(),
						auditable.getAuditId(),
						action,
						oldValues,
						newValues
				)));
	}

	private Map<String, Object> valuesFromState(
			Auditable auditable,
			EntityPersister persister,
			Object[] state,
			int[] propertyIndexes
	) {
		if (state == null) {
			return Map.of();
		}

		Set<String> allowedFields = new HashSet<>(auditable.auditFields());
		allowedFields.removeAll(auditable.auditHiddenFields());
		if (allowedFields.isEmpty()) {
			return Map.of();
		}

		String[] propertyNames = persister.getPropertyNames();
		Map<String, Object> values = new LinkedHashMap<>();
		if (propertyIndexes == null) {
			for (int i = 0; i < propertyNames.length; i++) {
				addValue(values, allowedFields, propertyNames[i], state[i]);
			}
			return values;
		}

		for (int propertyIndex : propertyIndexes) {
			addValue(values, allowedFields, propertyNames[propertyIndex], state[propertyIndex]);
		}
		return values;
	}

	private void addValue(Map<String, Object> values, Set<String> allowedFields, String fieldName, Object value) {
		if (allowedFields.contains(fieldName)) {
			values.put(fieldName, normalizeValue(value));
		}
	}

	private Object normalizeValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Auditable auditable) {
			return auditable.getAuditId();
		}
		try {
			Object identifier = persistenceUnitUtil.getIdentifier(value);
			return identifier == null ? value : identifier;
		} catch (IllegalArgumentException ignored) {
			return value;
		}
	}
}
