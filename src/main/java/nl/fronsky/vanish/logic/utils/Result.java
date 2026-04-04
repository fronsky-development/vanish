/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

public class Result<T> {
    private final T value;
    private final Exception exception;

    public Result(T value, Exception exception) {
        this.value = value;
        this.exception = exception;
    }

    /**
     * Creates a successful Result object with a result value.
     *
     * @param result the result value to be encapsulated in the Result object
     * @param <T>    the type of the result value
     * @return a new Result object representing a successful operation with the specified result value
     */
    public static <T> Result<T> Ok(T result) {
        return new Result<T>(result, null);
    }

    /**
     * Creates a failed Result object with an exception.
     *
     * @param exception the exception to be encapsulated in the Result object
     * @param <T>       the type of the result value (irrelevant in this case)
     * @return a new Result object representing a failed operation with the specified exception
     */
    public static <T> Result<T> Fail(Exception exception) {
        return new Result<T>(null, exception);
    }

    /**
     * Retrieves the value associated with this object.
     *
     * @return the value associated with this object
     */
    public T Value() {
        return value;
    }

    /**
     * Retrieves the exception associated with this object.
     *
     * @return the exception associated with this object
     */
    public Exception Exception() {
        return exception;
    }

    /**
     * Checks if the operation was successful.
     *
     * @return {@code true} if the operation was successful; {@code false} otherwise
     */
    public boolean Success() {
        return exception == null;
    }

    /**
     * Checks if the exception associated with this object is of a specific type.
     *
     * @param exceptionClass the class representing the type of exception to check for
     * @return {@code true} if the exception associated with this object is an instance
     * of the specified type; {@code false} otherwise
     */
    public boolean isExceptionType(Class<? extends Exception> exceptionClass) {
        return exceptionClass.isInstance(exception);
    }
}
