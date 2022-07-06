/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.store.trino;

import io.trino.spi.connector.ConnectorPartitionHandle;
import io.trino.spi.connector.ConnectorSplit;
import io.trino.spi.connector.ConnectorSplitSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/** Trino {@link ConnectorSplitSource}. */
public class TrinoSplitSource implements ConnectorSplitSource {

    private final Queue<TrinoSplit> splits;

    public TrinoSplitSource(List<TrinoSplit> splits) {
        this.splits = new LinkedList<>(splits);
    }

    @Override
    public CompletableFuture<ConnectorSplitBatch> getNextBatch(
            ConnectorPartitionHandle partitionHandle, int maxSize) {
        List<ConnectorSplit> batch = new ArrayList<>();
        for (int i = 0; i < maxSize; i++) {
            TrinoSplit split = splits.poll();
            if (split == null) {
                break;
            }
            batch.add(split);
        }
        return CompletableFuture.completedFuture(new ConnectorSplitBatch(batch, isFinished()));
    }

    @Override
    public void close() {}

    @Override
    public boolean isFinished() {
        return splits.isEmpty();
    }
}
