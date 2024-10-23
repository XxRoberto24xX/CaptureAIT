package com.captureait.captureait.controller;

/**
 * Listener for receiving notifications about events in the application.
 * Implement this interface to handle events from the central controller.
 */
public interface ActiveViewListener {
    /**
     * Called when the initialization process is finished.
     *
     * @param info Additional information about how the initialization process has ended.
     */
    void onInitializationFinished(String info);

    /**
     * Called when a server event occurs and the UI needs to be updated.
     *
     * @param updatedElement The identifier of the model element which has been updated.
     */
    void onUpdate(String updatedElement);
}
