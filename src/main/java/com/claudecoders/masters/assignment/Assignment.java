package com.claudecoders.masters.assignment;

import com.claudecoders.masters.course.Course;
import com.claudecoders.masters.shared.audit.BaseEntity;
import com.claudecoders.masters.teacher.Teacher;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "assignments")
@SQLDelete(sql = "UPDATE assignments SET deleted_at = CURRENT_TIMESTAMP WHERE id_course = ? AND id_teacher = ?")
@SQLRestriction("deleted_at IS NULL")
public class Assignment extends BaseEntity {

	@EmbeddedId
	private AssignmentId id = new AssignmentId();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("courseId")
	@JoinColumn(name = "id_course", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("teacherId")
	@JoinColumn(name = "id_teacher", nullable = false)
	private Teacher teacher;

	@Column(name = "assignment_date", nullable = false)
	private LocalDate assignmentDate = LocalDate.now();

	public AssignmentId getId() {
		return id;
	}

	public void setId(AssignmentId id) {
		this.id = id;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public LocalDate getAssignmentDate() {
		return assignmentDate;
	}

	public void setAssignmentDate(LocalDate assignmentDate) {
		this.assignmentDate = assignmentDate;
	}
}
