/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.apache.ignite.plugin.extensions.communication;

import java.nio.*;
import java.util.*;

/**
 * TODO
 */
public interface MessageReader {
    public void setBuffer(ByteBuffer buf);

    public byte readByte(String name);

    public short readShort(String name);

    public int readInt(String name);

    public long readLong(String name);

    public float readFloat(String name);

    public double readDouble(String name);

    public char readChar(String name);

    public boolean readBoolean(String name);

    public byte[] readByteArray(String name);

    public short[] readShortArray(String name);

    public int[] readIntArray(String name);

    public long[] readLongArray(String name);

    public float[] readFloatArray(String name);

    public double[] readDoubleArray(String name);

    public char[] readCharArray(String name);

    public boolean[] readBooleanArray(String name);

    public MessageAdapter readMessage(String name);

    public <T> Collection<T> readCollection(String name, Class<T> itemCls);

    public <K, V> Map<K, V> readMap(String name, Class<K> keyCls, Class<V> valCls);

    public boolean isLastRead();
}
