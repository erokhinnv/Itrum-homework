package org.example;

import java.util.ArrayDeque;
import java.util.Deque;

public class MyStringBuilder {

    private static final class Snapshot {
        private final char[] value;
        private final int count;

        private Snapshot(char[] value, int count) {
            this.value = value;
            this.count = count;
        }
    }

    private void saveSnapshot() {
        history.push(new Snapshot(value.clone(), count));
    }

    public MyStringBuilder undo() {
        if (history.isEmpty()) {
            throw new IllegalStateException("История изменений пуста — отменять нечего");
        }
        Snapshot snapshot = history.pop();
        value = snapshot.value;
        count = snapshot.count;
        return this;
    }

    private void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > value.length) {
            expandCapacity(minimumCapacity);
        }
    }

    private void expandCapacity(int minimumCapacity) {
        int newCapacity = value.length * 2 + 2;
        if (newCapacity < minimumCapacity) {
            newCapacity = minimumCapacity;
        }
        if (newCapacity < 0) {
            throw new OutOfMemoryError();
        }
        char[] newValue = new char[newCapacity];
        System.arraycopy(value, 0, newValue, 0, count);
        value = newValue;
    }

    public MyStringBuilder append(String str) {
        if (str == null) {
            str = "null";
        }
        saveSnapshot();
        int len = str.length();
        ensureCapacity(count + len);
        str.getChars(0, len, value, count);
        count += len;
        return this;
    }

    public MyStringBuilder delete(int start, int end) {
        checkRange(start, end);
        if (end > count) {
            end = count;
        }
        saveSnapshot();
        int len = end - start;
        if (len > 0) {
            System.arraycopy(value, end, value, start, count - end);
            count -= len;
        }
        return this;
    }

    private void checkRange(int start, int end) {
        if (start < 0 || start > end || start > count) {
            throw new StringIndexOutOfBoundsException("start " + start + ", end " + end + ", length " + count);
        }
    }

    @Override
    public String toString() {
        return new String(value, 0, count);
    }

    private char[] value = new char[16];
    private int count = 0;
    private final Deque<Snapshot> history = new ArrayDeque<>();
}