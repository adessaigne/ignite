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

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.cache.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.managers.communication.*;
import org.apache.ignite.internal.processors.cache.distributed.dht.atomic.*;
import org.apache.ignite.plugin.extensions.communication.*;
import org.apache.ignite.spi.*;
import org.apache.ignite.spi.communication.tcp.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.apache.ignite.testframework.junits.common.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import static org.apache.ignite.cache.CacheAtomicWriteOrderMode.*;
import static org.apache.ignite.cache.CacheDistributionMode.*;
import static org.apache.ignite.cache.CacheMode.*;
import static org.apache.ignite.cache.CacheWriteSynchronizationMode.*;

/**
 * Tests messages being sent between nodes in ATOMIC mode.
 */
public class GridCacheAtomicMessageCountSelfTest extends GridCommonAbstractTest {
    /** VM ip finder for TCP discovery. */
    private static TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

    /** Starting grid index. */
    private int idx;

    /** Partition distribution mode. */
    private CacheDistributionMode partDistMode;

    /** Write sync mode. */
    private CacheAtomicWriteOrderMode writeOrderMode;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();

        discoSpi.setIpFinder(ipFinder);

        cfg.setDiscoverySpi(discoSpi);

        CacheConfiguration cCfg = new CacheConfiguration();

        cCfg.setCacheMode(PARTITIONED);
        cCfg.setBackups(1);
        cCfg.setWriteSynchronizationMode(FULL_SYNC);
        cCfg.setDistributionMode(partDistMode);
        cCfg.setAtomicWriteOrderMode(writeOrderMode);

        if (idx == 0)
            cCfg.setDistributionMode(partDistMode);
        else
            cCfg.setDistributionMode(PARTITIONED_ONLY);

        idx++;

        cfg.setCacheConfiguration(cCfg);

        cfg.setCommunicationSpi(new TestCommunicationSpi());

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testPartitionedClock() throws Exception {
        checkMessages(PARTITIONED_ONLY, CLOCK);
    }

    /**
     * @throws Exception If failed.
     */
    public void testPartitionedPrimary() throws Exception {
        checkMessages(PARTITIONED_ONLY, PRIMARY);
    }

    /**
     * @throws Exception If failed.
     */
    public void testClientClock() throws Exception {
        checkMessages(CLIENT_ONLY, CLOCK);
    }

    /**
     * @throws Exception If failed.
     */
    public void testClientPrimary() throws Exception {
        checkMessages(CLIENT_ONLY, PRIMARY);
    }

    /**
     * @param distMode Distribution mode.
     * @param orderMode Write ordering mode.
     * @throws Exception If failed.
     */
    protected void checkMessages(CacheDistributionMode distMode,
        CacheAtomicWriteOrderMode orderMode) throws Exception {

        partDistMode = distMode;
        writeOrderMode = orderMode;

        startGrids(4);

        try {
            awaitPartitionMapExchange();

            TestCommunicationSpi commSpi = (TestCommunicationSpi)grid(0).configuration().getCommunicationSpi();

            commSpi.registerMessage(GridNearAtomicUpdateRequest.class);
            commSpi.registerMessage(GridDhtAtomicUpdateRequest.class);

            int putCnt = 15;

            int expNearCnt = 0;
            int expDhtCnt = 0;

            for (int i = 0; i < putCnt; i++) {
                ClusterNode locNode = grid(0).localNode();

                if (writeOrderMode == CLOCK) {
                    if (cache(0).affinity().isPrimary(locNode, i) || cache(0).affinity().isBackup(locNode, i))
                        expNearCnt++;
                    else
                        expNearCnt += 2;
                }
                else {
                    if (cache(0).affinity().isPrimary(locNode, i))
                        expDhtCnt++;
                    else
                        expNearCnt ++;
                }

                cache(0).put(i, i);
            }

            assertEquals(expNearCnt, commSpi.messageCount(GridNearAtomicUpdateRequest.class));
            assertEquals(expDhtCnt, commSpi.messageCount(GridDhtAtomicUpdateRequest.class));

            if (writeOrderMode == CLOCK) {
                for (int i = 1; i < 4; i++) {
                    commSpi = (TestCommunicationSpi)grid(i).configuration().getCommunicationSpi();

                    assertEquals(0, commSpi.messageCount(GridNearAtomicUpdateRequest.class));
                    assertEquals(0, commSpi.messageCount(GridDhtAtomicUpdateRequest.class));
                }
            }
            else {
                for (int i = 1; i < 4; i++) {
                    commSpi = (TestCommunicationSpi)grid(i).configuration().getCommunicationSpi();

                    assertEquals(0, commSpi.messageCount(GridNearAtomicUpdateRequest.class));
                }
            }
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * Test communication SPI.
     */
    private static class TestCommunicationSpi extends TcpCommunicationSpi {
        /** Counters map. */
        private Map<Class<?>, AtomicInteger> cntMap = new HashMap<>();

        /** {@inheritDoc} */
        @Override public void sendMessage(ClusterNode node, MessageAdapter msg)
            throws IgniteSpiException {
            AtomicInteger cntr = cntMap.get(((GridIoMessage)msg).message().getClass());

            if (cntr != null)
                cntr.incrementAndGet();

            super.sendMessage(node, msg);
        }

        /**
         * Registers message for counting.
         *
         * @param cls Class to count.
         */
        public void registerMessage(Class<?> cls) {
            AtomicInteger cntr = cntMap.get(cls);

            if (cntr == null)
                cntMap.put(cls, new AtomicInteger());
        }

        /**
         * @param cls Message type to get count.
         * @return Number of messages of given class.
         */
        public int messageCount(Class<?> cls) {
            AtomicInteger cntr = cntMap.get(cls);

            return cntr == null ? 0 : cntr.get();
        }

        /**
         * Resets counter to zero.
         */
        public void resetCount() {
            cntMap.clear();
        }
    }
}
