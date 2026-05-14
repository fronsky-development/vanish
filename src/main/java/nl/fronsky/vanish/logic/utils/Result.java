/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

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
        return new Result<>(null, exception);
    }

    /**
     * Retrieves the value associated with this object.
     *
     * @return the value associated with this object
     */
    @Override
    public T value() {
        return value;
    }

    /**
     * Retrieves the exception associated with this object.
     *
     * @return the exception associated with this object
     */
    @Override
    public Exception exception() {
        return exception;
    }

    /**
     * Checks if the operation was successful.
     *
     * @return {@code true} if the operation was successful; {@code false} otherwise
     */
    public boolean success() {
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
