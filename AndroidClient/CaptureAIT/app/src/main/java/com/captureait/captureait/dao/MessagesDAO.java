package com.captureait.captureait.dao;

import com.captureait.captureait.model.Message;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * DAO class which receives the messages information the server gives and saves it in different message object as cache
 */
public class MessagesDAO {
    /** The list of messages the player has. */
    private ArrayList<Message> messageArrayList;

    /**
     * Constructs a new MessagesDAO object with specified attributes.
     *
     * @param mailBox JSON array of messages.
     * @throws JSONException If there is an error parsing the JSON.
     */
    public MessagesDAO(JSONArray mailBox) throws JSONException {
        // Create the ArrayList instance
        messageArrayList = new ArrayList<>();

        // Fill the messages arrayList
        for (int i = 0; i < mailBox.length(); i++) {
            Message message = new Message(mailBox.getJSONObject(i).getString("type"), mailBox.getJSONObject(i).getString("name"), mailBox.getJSONObject(i).getString("code"));
            messageArrayList.add(message);
        }
    }

    /**
     * Returns the list of messages.
     *
     * @return The message array list.
     */
    public ArrayList<Message> getMessageArrayList() {
        synchronized (messageArrayList) {
            return messageArrayList;
        }
    }

    /**
     * Removes a message from the messagesArrayList and its duplicates if it has.
     *
     * @param type The type of the message.
     * @param userName The name of the user.
     * @param code The code of the message.
     */
    public void deleteMessage(String type, String userName, String code) {
        synchronized (messageArrayList) {
            for (int i = messageArrayList.size() - 1; i >= 0; i--) {
                if (messageArrayList.get(i).getType().equals(type) && messageArrayList.get(i).getName().equals(userName) && messageArrayList.get(i).getCode().equals(code)) {
                    messageArrayList.remove(i);
                }
            }
        }
    }

    /**
     * Adds a message to the list.
     *
     * @param type The type of the message.
     * @param userName The name of the user.
     * @param code The code of the message.
     */
    public void addMessage(String type, String userName, String code) {
        synchronized (messageArrayList) {
            messageArrayList.add(new Message(type, userName, code));
        }
    }
}
