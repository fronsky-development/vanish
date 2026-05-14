/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.tasks;

public interface ITask {
    /**
     * Runs the task.
     */
    void run();

    /**
     * Disables the task.
     */
    void disable();

    /**
     * Retrieves the delay before the task starts executing.
     *
     * @return the delay before the task starts executing, in milliseconds
     */
    long getDelay();

    /**
     * Retrieves the period between subsequent executions of the task.
     *
     * @return the period between subsequent executions of the task, in milliseconds,
     * or {@code -1} if the task is not meant to repeat
     */
    long getPeriod();

    /**
     * Checks if the task is designed to run asynchronously.
     *
     * @return {@code true} if the task is meant to run asynchronously; {@code false} otherwise
     */
    boolean isAsync();
}
