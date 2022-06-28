/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.strings.InternalByteArray;

@ValueType
public final class Bytes {
    public final byte[] array;
    public final int offset;
    public final int length;

    public Bytes(byte[] array, int offset, int length) {
        assert offset >= 0 && length >= 0 && offset + length <= array.length;
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public Bytes(byte[] array) {
        this(array, 0, array.length);
    }

    public static Bytes from(InternalByteArray bytes) {
        return new Bytes(bytes.getArray(), bytes.getOffset(), bytes.getLength());
    }

    public static Bytes fromRange(InternalByteArray bytes, int start, int end) {
        assert 0 <= start && start <= end && end <= bytes.getLength();
        return new Bytes(bytes.getArray(), bytes.getOffset() + start, end - start);
    }

    public static Bytes fromRange(byte[] array, int start, int end) {
        assert 0 <= start && start <= end && end <= array.length;
        return new Bytes(array, start, end - start);
    }

    /** Just like {@link #fromRange(byte[], int, int)}, but will clamp the length to stay within the bounds. */
    public static Bytes fromRangeClamped(InternalByteArray byteArray, int start, int length) {
        int clampedLength = Math.min(byteArray.getLength() - start, length);

        assert 0 <= start && (start + clampedLength <= byteArray.getLength());

        return new Bytes(byteArray.getArray(), start + byteArray.getOffset(), clampedLength);
    }

    /** Returns the end offset, equal to {@link #offset} + {@link #length}. */
    public int end() {
        return offset + length;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public byte get(int i) {
        return array[offset + i];
    }
}
