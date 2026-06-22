package com.claudecoders.masters.payment;

import com.claudecoders.masters.shared.audit.Auditable;
import com.claudecoders.masters.shared.audit.BaseEntity;
import com.claudecoders.masters.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;

@Entity
@Table(name = "payments")
@SQLDelete(sql = "UPDATE payments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Payment extends BaseEntity implements Auditable {

	private static final Set<String> AUDIT_FIELDS = Set.of(
			"student",
			"paymentNumber",
			"paymentDate",
			"concept",
			"amount"
	);

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_student", nullable = false)
	private Student student;

	@Column(name = "payment_number", nullable = false)
	private Integer paymentNumber;

	@Column(name = "payment_date")
	private LocalDate paymentDate;

	@Column(name = "concept", length = 255)
	private String concept;

	@Column(name = "amount", precision = 10, scale = 2)
	private BigDecimal amount;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Integer getPaymentNumber() {
		return paymentNumber;
	}

	public void setPaymentNumber(Integer paymentNumber) {
		this.paymentNumber = paymentNumber;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDate paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
