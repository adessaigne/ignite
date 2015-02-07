/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal;

import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.plugin.extensions.communication.*;

import java.io.*;
import java.nio.*;

/**
 * Job siblings request.
 */
public class GridJobSiblingsRequest extends MessageAdapter {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private IgniteUuid sesId;

    /** */
    @GridDirectTransient
    private Object topic;

    /** */
    private byte[] topicBytes;

    /**
     * Empty constructor required by {@link Externalizable}.
     */
    public GridJobSiblingsRequest() {
        // No-op.
    }

    /**
     * @param sesId Session ID.
     * @param topic Topic.
     * @param topicBytes Serialized topic.
     */
    public GridJobSiblingsRequest(IgniteUuid sesId, Object topic, byte[] topicBytes) {
        assert sesId != null;
        assert topic != null || topicBytes != null;

        this.sesId = sesId;
        this.topic = topic;
        this.topicBytes = topicBytes;
    }

    /**
     * @return Session ID.
     */
    public IgniteUuid sessionId() {
        return sesId;
    }

    /**
     * @return Topic.
     */
    public Object topic() {
        return topic;
    }

    /**
     * @return Serialized topic.
     */
    public byte[] topicBytes() {
        return topicBytes;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneCallsConstructors"})
    @Override public MessageAdapter clone() {
        GridJobSiblingsRequest _clone = new GridJobSiblingsRequest();

        clone0(_clone);

        return _clone;
    }

    /** {@inheritDoc} */
    @Override protected void clone0(MessageAdapter _msg) {
        GridJobSiblingsRequest _clone = (GridJobSiblingsRequest)_msg;

        _clone.sesId = sesId;
        _clone.topic = topic;
        _clone.topicBytes = topicBytes;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("all")
    @Override public boolean writeTo(ByteBuffer buf) {
        commState.setBuffer(buf);

        if (!commState.typeWritten) {
            if (!commState.putByte(null, directType()))
                return false;

            commState.typeWritten = true;
        }

        switch (commState.idx) {
            case 0:
                if (!commState.putGridUuid("sesId", sesId))
                    return false;

                commState.idx++;

            case 1:
                if (!commState.putByteArray("topicBytes", topicBytes))
                    return false;

                commState.idx++;

        }

        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("all")
    @Override public boolean readFrom(ByteBuffer buf) {
        commState.setBuffer(buf);

        switch (commState.idx) {
            case 0:
                sesId = commState.getGridUuid("sesId");

                if (!commState.lastRead())
                    return false;

                commState.idx++;

            case 1:
                topicBytes = commState.getByteArray("topicBytes");

                if (!commState.lastRead())
                    return false;

                commState.idx++;

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridJobSiblingsRequest.class, this);
    }
}
