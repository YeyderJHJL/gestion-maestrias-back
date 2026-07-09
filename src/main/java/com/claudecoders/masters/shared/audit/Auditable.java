package com.claudecoders.masters.shared.audit;

import java.util.Set;
import org.hibernate.Hibernate;

public interface Auditable {

	Object getAuditId();

	default String getAuditType() {
		return Hibernate.getClass(this).getSimpleName();
	}

	Set<String> auditFields();

	default Set<String> auditHiddenFields() {
		return Set.of("createdAt", "updatedAt", "deletedAt");
	}
}
