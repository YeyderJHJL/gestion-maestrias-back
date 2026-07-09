package com.claudecoders.masters.voucher;

import com.claudecoders.masters.file.StoredFile;
import com.claudecoders.masters.payment.Payment;
import com.claudecoders.masters.shared.audit.Auditable;
import com.claudecoders.masters.shared.audit.BaseEntity;
import com.claudecoders.masters.state.State;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;

@Entity
@Table(name = "vouchers")
@SQLDelete(sql = "UPDATE vouchers SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Voucher extends BaseEntity implements Auditable {

	private static final Set<String> AUDIT_FIELDS = Set.of(
			"payment",
			"state",
			"file",
			"observation"
	);

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_payment", nullable = false)
	private Payment payment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_state", nullable = false)
	private State state;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_file", nullable = false)
	private StoredFile file;

	@Column(name = "observation", columnDefinition = "TEXT")
	private String observation;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public StoredFile getFile() {
		return file;
	}

	public void setFile(StoredFile file) {
		this.file = file;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
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
