package com.focuszone.utils;

public class Event<T> {
    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        } else {
            handled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }

    public boolean isHandled() {
        return handled;
    }
}
