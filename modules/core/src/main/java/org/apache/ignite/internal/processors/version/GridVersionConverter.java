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

package org.apache.ignite.internal.processors.version;

import org.apache.ignite.internal.direct.*;

import java.nio.*;

/**
 * Version converter.
 */
public abstract class GridVersionConverter {
    /** State. */
    protected final GridTcpCommunicationMessageState commState = new GridTcpCommunicationMessageState();

    /**
     * Writes delta between two versions.
     *
     * @param buf Buffer to write to.
     * @return Whether delta was fully written.
     */
    public abstract boolean writeTo(ByteBuffer buf);

    /**
     * Reads delta between two versions.
     *
     * @param buf Buffer to read from.
     * @return Whether delta was fully read.
     */
    public abstract boolean readFrom(ByteBuffer buf);
}
