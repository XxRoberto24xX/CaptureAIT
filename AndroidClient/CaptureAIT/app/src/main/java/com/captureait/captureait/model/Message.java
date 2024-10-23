package com.captureait.captureait.model;

/**
 * Represents a message with type, name, and code attributes which can be an invitation to a game or a friend request.
 */
public class Message {

    /** The type of the message. */
    private String type;

    /** The sender name. */
    private String name;

    /** The code of the message (if needed). */
    private String code;

    /**
     * Constructs a new Message object with specified type, name, and code.
     *
     * @param type The type of the message.
     * @param name The sender name.
     * @param code The code of the message (if needed).
     */
    public Message(String type, String name, String code){
        this.type = type;
        this.name = name;
        this.code = code;
    }

    /**
     * Returns the type of the message.
     *
     * @return The type of the message.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the name associated with the message.
     *
     * @return The name associated with the message.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the code of the message.
     *
     * @return The code of the message.
     */
    public String getCode() {
        return code;
    }
}
