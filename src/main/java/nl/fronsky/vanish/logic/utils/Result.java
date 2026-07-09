/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import java.util.Objects;

/**
 * An immutable result of an operation that either succeeds with a value or fails with an exception.
 *
 * @param value     the value produced on success, or {@code null} on failure
 * @param exception the exception describing the failure, or {@code null} on success
 * @param <T>       the type of the success value
 */
public record Result<T>(T value, Exception exception) {

    /**
     * Creates a successful Result object with a result value.
     *
     * @param result the result value to be encapsulated in the Result object
     * @param <T>    the type of the result value
     * @return a new Result object representing a successful operation with the specified result value
     */
    public static <T> Result<T> ok(T result) {
        return new Result<>(result, null);
    }

    /**
     * Creates a failed Result object with an exception.
     *
     * @param exception the exception to be encapsulated in the Result object
     * @param <T>       the type of the result value (irrelevant in this case)
     * @return a new Result object representing a failed operation with the specified exception
     */
    public static <T> Result<T> fail(Exception exception) {
        return new Result<>(null, Objects.requireNonNull(exception, "exception must not be null"));
    }

    /**
     * Checks if the operation was successful.
     *
     * @return {@code true} if the operation was successful; {@code false} otherwise
     */
    public boolean success() {
        return exception == null;
    }
}
