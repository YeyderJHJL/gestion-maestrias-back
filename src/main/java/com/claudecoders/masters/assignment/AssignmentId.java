package com.claudecoders.masters.assignment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AssignmentId implements Serializable {

	@Column(name = "id_course", nullable = false)
	private UUID courseId;

	@Column(name = "id_teacher", nullable = false)
	private UUID teacherId;

	public AssignmentId() {
	}

	public AssignmentId(UUID courseId, UUID teacherId) {
		this.courseId = courseId;
		this.teacherId = teacherId;
	}

	public UUID getCourseId() {
		return courseId;
	}

	public void setCourseId(UUID courseId) {
		this.courseId = courseId;
	}

	public UUID getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(UUID teacherId) {
		this.teacherId = teacherId;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof AssignmentId that)) {
			return false;
		}
		return Objects.equals(courseId, that.courseId) && Objects.equals(teacherId, that.teacherId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(courseId, teacherId);
	}
}
