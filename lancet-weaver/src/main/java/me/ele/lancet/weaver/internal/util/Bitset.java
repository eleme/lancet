package me.ele.lancet.weaver.internal.util;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by gengwanpeng on 17/5/10.
 */
public class Bitset {

    private static final int[] EMPTY = new int[0];

    private int[] bits;

    int next = 0;
    private Consumer<Bitset> initializer;

    public Bitset() {
        bits = EMPTY;
    }

    public void tryAdd(String intLike, int index) {
        int res = 0;
        while (index < intLike.length()) {
            int e = intLike.charAt(index) - '0';
            if (e < 0 || e > 9) return;
            res = res * 10 + e;
            index++;
        }
        add(res);
    }

    private void add(int bit) {
        ensureCapacity(bit);
        bits[bit >> 5] |= (1 << (bit & 31));
        if (bit == next) {
            moveToNext();
        }
    }

    private void moveToNext() {
        int i = next;
        int[] localBits = bits;
        while (((localBits[i >> 5] >> (i & 31)) & 1) != 0) {
            i++;
        }
        next = i;
    }

    private void ensureCapacity(int bit) {
        if (bit > Integer.MAX_VALUE - 31) {
            throw new OutOfMemoryError();
        }
        bit = (bit >> 5) + 1;
        if (bits == EMPTY) {
            bits = new int[minPow(bit)];
        } else if (bit > bits.length) {
            int bl = bits.length << 1;
            bits = Arrays.copyOf(bits, bit <= bl ? bl : minPow(bit));
        }
    }

    private static int minPow(int i) {
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        return i + 1;
    }

    public int consume() {
        init();
        int i = next;
        add(i);
        return i;
    }

    private void init() {
        if (bits == EMPTY) {
            initializer.accept(this);
        }
    }

    public void setInitializer(Consumer<Bitset> initializer) {
        this.initializer = initializer;
    }
}
