package org.aadarshkt.temporaljava.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false, length = 50)
	private String workflowType;

	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private WorkflowStatus status = WorkflowStatus.RUNNING;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "execution_id", referencedColumnName = "id", insertable = false, updatable = false)
	private List<Task> tasks;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected WorkflowExecution() {
	}

	public static WorkflowExecution newWorkflow(UUID userId, String workflowType) {
		WorkflowExecution workflow = new WorkflowExecution();
		workflow.id = UUID.randomUUID();
		workflow.userId = userId;
		workflow.workflowType = workflowType;
		workflow.status = WorkflowStatus.RUNNING;
		workflow.createdAt = Instant.now();
		return workflow;
	}

	public boolean isFinished() {
		return this.status == WorkflowStatus.COMPLETED || this.status == WorkflowStatus.FAILED;
	}
}

