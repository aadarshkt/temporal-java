package org.aadarshkt.temporaljava.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "tasks")
public class Task {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false)
	private UUID executionId;

	// TODO: Check what is the purpose of this field. if it is not required let's remove it.
	@Column(nullable = false, length = 100)
	private String refId;

	@Column(nullable = false, length = 100)
	private String action;

	@Setter
    @Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskStatus status = TaskStatus.PENDING;

	@Setter
    @Column(nullable = false)
	private int attemptCount = 0;

	@Column(nullable = false)
	private final int maxRetries = 3;

	@Setter
    @Column(columnDefinition = "text")
	private String lastError;

	@Setter
    @JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode dependencies;

	@Setter
    @Column(nullable = false)
	private int inDegree = 0;

	@Setter
    @Column(nullable = false)
	private boolean skipHint = false;

	@Setter
    @Column(length = 100)
	private String workerId;

	@Setter
    @Column(nullable = false)
	private int version = 1;

	@Setter
    @JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode input;

	@Setter
    @JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode output;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	protected Task() {
	}

	public static Task newTask(UUID executionId, String refId, String action) {
		Task task = new Task();
		task.id = UUID.randomUUID();
		task.executionId = executionId;
		task.refId = refId;
		task.action = action;
		task.status = TaskStatus.PENDING;
		task.version = 1;
		task.createdAt = Instant.now();
		return task;
	}

	public boolean canRetry(int maxRetry) {
		return this.attemptCount < maxRetry;
	}

}
