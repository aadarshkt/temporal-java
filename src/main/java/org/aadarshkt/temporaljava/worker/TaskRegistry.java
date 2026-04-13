package org.aadarshkt.temporaljava.worker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds all registered {@link TaskHandler} instances keyed by action name.
 * Equivalent to: type TaskRegistry map[string]TaskHandler
 */
public class TaskRegistry {

    private final Map<String, TaskHandler> handlers;

    private TaskRegistry(Map<String, TaskHandler> handlers) {
        this.handlers = Collections.unmodifiableMap(handlers);
    }

    /**
     * Look up a handler by action name.
     *
     * @param action the task action identifier
     * @return the matching {@link TaskHandler}, or empty if not registered
     */
    public Optional<TaskHandler> get(String action) {
        return Optional.ofNullable(handlers.get(action));
    }

    /**
     * Returns true when a handler is registered for the given action name.
     */
    public boolean contains(String action) {
        return handlers.containsKey(action);
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Wires up all built-in task handlers.
     * Equivalent to: func InitRegistry() TaskRegistry { ... }
     *
     * @return an initialised, immutable {@link TaskRegistry}
     */
    public static TaskRegistry init() {
        Map<String, TaskHandler> registry = new HashMap<>();

        registry.put("create_employee_profile", input -> {
            System.out.printf("Creating employee profile with payload: %s%n", new String(input));
            Thread.sleep(10_000);
            return """
                    {"status": "success", "employee_id": "EMP-12345", "profile_created": true}"""
                    .getBytes();
        });

        registry.put("setup_email_account", input -> {
            System.out.printf("Setting up email account with payload: %s%n", new String(input));
            Thread.sleep(10_000);
            return """
                    {"status": "success", "email": "john.doe@company.com", "mailbox_created": true}"""
                    .getBytes();
        });

        registry.put("assign_equipment", input -> {
            System.out.printf("Assigning equipment with payload: %s%n", new String(input));
            Thread.sleep(10_000);
            return """
                    {"status": "success", "laptop_id": "LT-789", "monitor_id": "MN-456", "assigned": true}"""
                    .getBytes();
        });

        registry.put("enroll_benefits", input -> {
            System.out.printf("Enrolling in benefits with payload: %s%n", new String(input));
            Thread.sleep(10_000);
            return """
                    {"status": "success", "health_plan": "Premium PPO", "401k_enrolled": true}"""
                    .getBytes();
        });

        registry.put("schedule_orientation", input -> {
            System.out.printf("Scheduling orientation with payload: %s%n", new String(input));
            Thread.sleep(10_000);
            return """
                    {"status": "success", "orientation_date": "2026-03-01", "calendar_invite_sent": true}"""
                    .getBytes();
        });

        // Test: always fails to simulate task errors
        registry.put("failing_task", input -> {
            System.out.printf("Failing task executed with payload: %s%n", new String(input));
            throw new RuntimeException("simulated task failure");
        });

        // Test: long-running task that respects thread interruption (analogous to ctx.Done())
        registry.put("timeout_task", input -> {
            System.out.printf("Long-running task started with payload: %s%n", new String(input));

            // Run for ~10 s but check for interruption every 100 ms
            for (int i = 0; i < 100; i++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException("timeout_task was cancelled");
                }
                Thread.sleep(100);
            }

            return """
                    {"status": "success", "completed": true}"""
                    .getBytes();
        });

        return new TaskRegistry(registry);
    }
}

