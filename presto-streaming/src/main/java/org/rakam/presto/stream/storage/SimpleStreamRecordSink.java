package org.rakam.presto.stream.storage;

import com.facebook.presto.operator.aggregation.Accumulator;
import com.facebook.presto.spi.ConnectorPageSink;
import com.facebook.presto.spi.Page;
import com.facebook.presto.spi.block.Block;
import io.airlift.slice.Slice;

import java.util.Collection;

/**
 * Created by buremba <Burak Emre Kabakcı> on 19/01/15 13:47.
 */
public class SimpleStreamRecordSink implements ConnectorPageSink
{

    private final SimpleRowTable table;

    public SimpleStreamRecordSink(SimpleRowTable table) {
        this.table = table;
    }

    @Override
    public void appendPage(Page page, Block sampleWeightBlock) {
        for (Accumulator accumulator : table.getAggregations()) {
            accumulator.addInput(page);
        }
    }

    @Override
    public Collection<Slice> commit() {
        return null;
    }

    @Override
    public void rollback() {

    }
}
