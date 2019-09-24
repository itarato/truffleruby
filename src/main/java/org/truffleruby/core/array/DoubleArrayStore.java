/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.array;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.truffleruby.core.array.ArrayStoreLibrary.ArrayAllocator;

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = double[].class)
@GenerateUncached
public class DoubleArrayStore {

    @ExportMessage
    public static double read(double[] store, long index) {
        return store[(int) index];
    }

    @ExportMessage
    public static boolean acceptsValue(double[] store, Object value) {
        return value instanceof Double;
    }

    @ExportMessage
    static class AcceptsAllValues {

        @Specialization
        static boolean acceptsZeroValues(double[] store, ZeroLengthArrayStore otherStore) {
            return true;
        }

        @Specialization
        static boolean acceptsDoubleValues(double[] store, double[] otherStore) {
            return true;
        }

        @Specialization
        static boolean acceptsDelegateValues(double[] store, DelegatedArrayStorage otherStore,
                @CachedLibrary("store") ArrayStoreLibrary stores) {
            return stores.acceptsAllValues(store, otherStore.storage);
        }

        @Specialization
        static boolean acceptsOtherValues(double[] store, Object otherStore) {
            return false;
        }
    }

    @ExportMessage
    public static boolean isMutable(double[] store) {
        return true;
    }

    @ExportMessage
    public static boolean isPrimitive(double[] store) {
        return true;
    }

    @ExportMessage
    public static String toString(double[] store) {
        return "double[]";
    }

    @ExportMessage
    public static void write(double[] store, long index, Object value) {
        store[(int) index] = (double) value;
    }

    @ExportMessage
    public static long capacity(double[] store) {
        return store.length;
    }

    @ExportMessage
    public static double[] expand(double[] store, long newCapacity) {
        double[] newStore = new double[(int) newCapacity];
        System.arraycopy(store, 0, newStore, 0, store.length);
        return newStore;
    }

    @ExportMessage
    static class CopyContents {

        @Specialization
        static void copyContents(double[] srcStore, long srcStart, double[] destStore, long destStart, long length) {
            System.arraycopy(srcStore, (int) srcStart, destStore, (int) destStart, (int) length);
        }

        @Specialization
        static void copyContents(double[] srcStore, long srcStart, Object destStore, long destStart, long length,
                @CachedLibrary(limit = "5") ArrayStoreLibrary destStores) {
            for (long i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStore[(int) (srcStart + i)]);
            }
        }
    }

    @ExportMessage
    public static double[] copyStore(double[] store, long length) {
        return ArrayUtils.grow(store, (int) length);
    }

    @ExportMessage
    @TruffleBoundary
    public static void sort(double[] store, long size) {
        Arrays.sort(store, 0, (int) size);
    }

    @ExportMessage
    public static Iterable<Object> getIterable(double[] store, long from, long length) {
        return () -> new Iterator<Object>() {

            private int n = (int) from;

            @Override
            public boolean hasNext() {
                return n < from + length;
            }

            @Override
            public Object next() throws NoSuchElementException {
                if (n >= from + length) {
                    throw new NoSuchElementException();
                }

                final Object object = store[n];
                n++;
                return object;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

        };
    }

    @ExportMessage
    static class GeneralizeForValue {

        @Specialization
        static ArrayAllocator generalize(double[] store, double newValue) {
            return DOUBLE_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(double[] store, Object newValue) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }
    }

    @ExportMessage
    static class GeneralizeForStore {

        @Specialization
        static ArrayAllocator generalize(double[] store, int[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(double[] store, long[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(double[] store, double[] newStore) {
            return DOUBLE_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(double[] store, Object[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(double[] store, Object newStore,
                @CachedLibrary(limit = "3") ArrayStoreLibrary newStores) {
            return newStores.generalizeForStore(newStore, store);
        }
    }

    @ExportMessage
    public static ArrayAllocator allocator(double[] store) {
        return DOUBLE_ARRAY_ALLOCATOR;
    }

    public static final ArrayAllocator DOUBLE_ARRAY_ALLOCATOR = new DoubleArrayAllocator();

    private static class DoubleArrayAllocator extends ArrayAllocator {

        @Override
        public double[] allocate(long capacity) {
            return new double[(int) capacity];
        }

        @Override
        public boolean accepts(Object value) {
            return value instanceof Double;
        }

        @Override
        public boolean specializesFor(Object value) {
            return value instanceof Double;
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return (double) value == 0.0;
        }
    }
}
