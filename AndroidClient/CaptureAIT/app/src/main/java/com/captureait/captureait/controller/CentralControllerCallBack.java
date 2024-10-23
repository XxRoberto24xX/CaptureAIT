package com.captureait.captureait.controller;

/* Interface used when the method the view calls is asynchronous */
/**
 * Listener for receiving callbacks about processes called to the central controller.
 * Implement this interface to handle the end status of the method called.
 */
public interface CentralControllerCallBack {
    /**
     * Called when the method finished without error.
     *
     * @param info Additional information about how the method processing has ended.
     */
    void onSuccess(String info);

    /**
     * Called when the method finished with error.
     *
     * @param error Exception launched with information about what error occurred.
     */
    void onError(String error);
}
