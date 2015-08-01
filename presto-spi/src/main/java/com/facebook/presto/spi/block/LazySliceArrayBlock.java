/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.spi.block;

import io.airlift.slice.Slice;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.facebook.presto.spi.block.BlockValidationUtil.checkValidPositions;
import static com.facebook.presto.spi.block.SliceArrayBlock.deepCopyAndCompact;
import static com.facebook.presto.spi.block.SliceArrayBlock.getSliceArraySizeInBytes;
import static io.airlift.slice.SizeOf.SIZE_OF_BYTE;
import static io.airlift.slice.SizeOf.SIZE_OF_INT;
import static io.airlift.slice.Slices.copyOf;
import static io.airlift.slice.Slices.wrappedIntArray;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class LazySliceArrayBlock
        extends AbstractVariableWidthBlock
{
    private final int positionCount;
    private final AtomicInteger sizeInBytes = new AtomicInteger(-1);

    private LazyBlockLoader<LazySliceArrayBlock> loader;
    private Slice[] values;
    private boolean dictionary;
    private int[] ids;
    private boolean[] isNull;

    public LazySliceArrayBlock(int positionCount, LazyBlockLoader<LazySliceArrayBlock> loader)
    {
        if (positionCount < 0) {
            throw new IllegalArgumentException("positionCount is negative");
        }
        this.positionCount = positionCount;
        this.loader = requireNonNull(loader);
    }

    @Override
    public BlockEncoding getEncoding()
    {
        return new LazySliceArrayBlockEncoding();
    }

    @Override
    public Block copyPositions(List<Integer> positions)
    {
        checkValidPositions(positions, positionCount);
        assureLoaded();

        if (dictionary) {
            return compactAndGet(positions, false);
        }

        Slice[] newValues = new Slice[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            if (!isEntryNull(positions.get(i))) {
                newValues[i] = copyOf(values[positions.get(i)]);
            }
        }
        return new SliceArrayBlock(positions.size(), newValues);
    }

    @Override
    protected Slice getRawSlice(int position)
    {
        assureLoaded();
        return values[getPosition(position)];
    }

    @Override
    protected int getPositionOffset(int position)
    {
        return 0;
    }

    @Override
    protected boolean isEntryNull(int position)
    {
        assureLoaded();
        if (isNull != null) {
            return isNull[position];
        }
        return values[getPosition(position)] == null;
    }

    @Override
    public int getPositionCount()
    {
        return positionCount;
    }

    @Override
    public int getLength(int position)
    {
        assureLoaded();
        return values[getPosition(position)].length();
    }

    @Override
    public int getSizeInBytes()
    {
        int sizeInBytes = this.sizeInBytes.get();
        if (sizeInBytes < 0) {
            assureLoaded();
            sizeInBytes = getSliceArraySizeInBytes(values);
            if (dictionary) {
                sizeInBytes += ids.length * SIZE_OF_INT;
                sizeInBytes += isNull.length * SIZE_OF_BYTE;
            }
            this.sizeInBytes.set(sizeInBytes);
        }
        return sizeInBytes;
    }

    @Override
    public int getRetainedSizeInBytes()
    {
        // TODO: This should account for memory used by the loader.
        return getSizeInBytes();
    }

    @Override
    public Block getRegion(int positionOffset, int length)
    {
        int positionCount = getPositionCount();
        if (positionOffset < 0 || length < 0 || positionOffset + length > positionCount) {
            throw new IndexOutOfBoundsException("Invalid position " + positionOffset + " in block with " + positionCount + " positions");
        }

        assureLoaded();
        if (dictionary) {
            List<Integer> positions = IntStream.range(positionOffset, positionOffset + length).boxed().collect(toList());
            compactAndGet(positions, false);
        }
        Slice[] newValues = Arrays.copyOfRange(values, positionOffset, positionOffset + length);
        return new SliceArrayBlock(length, newValues);
    }

    @Override
    public Block copyRegion(int positionOffset, int length)
    {
        int positionCount = getPositionCount();
        if (positionOffset < 0 || length < 0 || positionOffset + length > positionCount) {
            throw new IndexOutOfBoundsException("Invalid position " + positionOffset + " in block with " + positionCount + " positions");
        }

        assureLoaded();

        if (dictionary) {
            List<Integer> positions = IntStream.range(positionOffset, positionOffset + length).boxed().collect(toList());
            return compactAndGet(positions, true);
        }
        return new SliceArrayBlock(length, deepCopyAndCompact(values, positionOffset, length));
    }

    @Override
    public void assureLoaded()
    {
        if (values != null) {
            return;
        }
        loader.load(this);

        if (values == null) {
            throw new IllegalArgumentException("Lazy block loader did not load this block");
        }

        // clear reference to loader to free resources, since load was successful
        loader = null;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("LazySliceArrayBlock{");
        sb.append("positionCount=").append(getPositionCount());
        sb.append('}');
        return sb.toString();
    }

    private Block compactAndGet(List<Integer> positions, boolean copy)
    {
        List<Integer> distinctPositions = positions.stream().distinct().collect(toList());
        List<Integer> currentDictionaryIndexes = distinctPositions.stream().map(this::getPosition).collect(toList());
        List<Integer> positionsToCopy = currentDictionaryIndexes.stream().distinct().collect(toList());

        Slice[] newValues = new Slice[positionsToCopy.size()];
        for (int i = 0; i < positionsToCopy.size(); i++) {
            Slice value = values[positionsToCopy.get(i)];
            if (copy) {
                newValues[i] = copyOf(value);
            }
            else {
                newValues[i] = value;
            }
        }

        int[] newIds = new int[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            int oldIndex = currentDictionaryIndexes.get(distinctPositions.indexOf(positions.get(i)));
            newIds[i] = positionsToCopy.indexOf(oldIndex);
        }
        return new DictionaryBlock(positions.size(), new SliceArrayBlock(newValues.length, newValues), wrappedIntArray(newIds));
    }

    private int getPosition(int position)
    {
         if (dictionary) {
             return ids[position];
         }
         return position;
    }

    public Slice[] getValues()
    {
        assureLoaded();
        return values;
    }

    public int[] getIds()
    {
        return ids;
    }

    public boolean isDictionary()
    {
        assureLoaded();
        return dictionary;
    }

    public void setValues(Slice[] values)
    {
        requireNonNull(values, "values is null");
        this.values = values;
    }

    public void setValues(Slice[] values, int[] ids, boolean[] isNull)
    {
        requireNonNull(values, "values is null");
        requireNonNull(ids, "ids is null");
        requireNonNull(isNull, "isNull is null");

        if (ids.length != positionCount) {
            throw new IllegalArgumentException(format("ids length %s is not equal to positionCount %s", ids.length, positionCount));
        }

        if (ids.length != isNull.length) {
            throw new IllegalArgumentException("ids length does not match isNull length");
        }

        setValues(values);
        this.ids = ids;
        this.isNull = isNull;
        this.dictionary = true;
    }
}
