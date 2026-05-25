package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.audit.Auditable;
import com.claudecoders.masters.shared.audit.BaseEntity;
import com.claudecoders.masters.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "teachers")
@SQLDelete(sql = "UPDATE teachers SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Teacher extends BaseEntity implements Auditable {

	private static final Set<String> AUDIT_FIELDS = Set.of(
			"user",
			"category",
			"regime",
			"academicDegree",
			"specialty",
			"type",
			"phone"
	);

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_user", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "category", columnDefinition = "teacher_category")
	private TeacherCategory category;

	@Column(name = "regime", length = 100)
	private String regime;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "academic_degree", columnDefinition = "academic_degree")
	private AcademicDegree academicDegree;

	@Column(name = "specialty", length = 255)
	private String specialty;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "type", nullable = false, columnDefinition = "teacher_type")
	private TeacherType type;

	@Column(name = "phone", length = 20)
	private String phone;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public TeacherCategory getCategory() {
		return category;
	}

	public void setCategory(TeacherCategory category) {
		this.category = category;
	}

	public String getRegime() {
		return regime;
	}

	public void setRegime(String regime) {
		this.regime = regime;
	}

	public AcademicDegree getAcademicDegree() {
		return academicDegree;
	}

	public void setAcademicDegree(AcademicDegree academicDegree) {
		this.academicDegree = academicDegree;
	}

	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public TeacherType getType() {
		return type;
	}

	public void setType(TeacherType type) {
		this.type = type;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public UUID getAuditId() {
		return id;
	}

	@Override
	public Set<String> auditFields() {
		return AUDIT_FIELDS;
	}
}
