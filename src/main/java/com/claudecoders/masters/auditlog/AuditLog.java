package com.claudecoders.masters.auditlog;

import com.claudecoders.masters.shared.audit.CreatedEntity;
import com.claudecoders.masters.user.User;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends CreatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_user", nullable = false)
	private User user;

	@Column(name = "entity_type", nullable = false, length = 100)
	private String entityType;

	@Column(name = "id_entity", nullable = false)
	private UUID entityId;

	@Column(name = "action", nullable = false, length = 100)
	private String action;

	@Column(name = "field_name", length = 100)
	private String fieldName;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "old_value", columnDefinition = "jsonb")
	private JsonNode oldValue;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "new_value", columnDefinition = "jsonb")
	private JsonNode newValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public JsonNode getOldValue() {
		return oldValue;
	}

	public void setOldValue(JsonNode oldValue) {
		this.oldValue = oldValue;
	}

	public JsonNode getNewValue() {
		return newValue;
	}

	public void setNewValue(JsonNode newValue) {
		this.newValue = newValue;
	}
}
