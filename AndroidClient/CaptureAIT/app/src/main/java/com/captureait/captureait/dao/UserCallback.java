package com.captureait.captureait.dao;

/**
 * Callback interface for handling asynchronous responses related to user operations.
 */
public interface UserCallback {

    /**
     * Called when an operation is successful.
     */
    void onSuccess();

    /**
     * Called when an error occurs during an operation.
     *
     * @param error A string describing the error encountered
     */
    void onError(String error);
}
