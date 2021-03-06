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
package com.wrmsr.presto.reactor;

import com.wrmsr.presto.reactor.tuples.PkTuple;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public class TableEvent
{
    public enum Operation
    {
        INSERT,
        UPDATE,
        DELETE
    }

    public static TableEvent insert(PkTuple after)
    {
        return new TableEvent(Operation.INSERT, Optional.<PkTuple>empty(), Optional.of(after));
    }

    public static TableEvent update(PkTuple before, PkTuple after)
    {
        return new TableEvent(Operation.UPDATE, Optional.of(before), Optional.of(after));
    }

    public static TableEvent delete(PkTuple before)
    {
        return new TableEvent(Operation.INSERT, Optional.of(before), Optional.<PkTuple>empty());
    }

    protected final Operation operation;
    protected final Optional<PkTuple> before;
    protected final Optional<PkTuple> after;

    public TableEvent(Operation operation, Optional<PkTuple> before, Optional<PkTuple> after)
    {
        switch (operation) {
            case INSERT: {
                checkArgument(!before.isPresent());
                checkArgument(after.isPresent());
                break;
            }
            case UPDATE: {
                checkArgument(before.isPresent());
                checkArgument(after.isPresent());
                break;
            }
            case DELETE: {
                checkArgument(before.isPresent());
                checkArgument(!after.isPresent());
                break;
            }
            default: {
                throw new IllegalArgumentException(operation.toString());
            }
        }
        this.operation = operation;
        this.before = before;
        this.after = after;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public Optional<PkTuple> getBefore()
    {
        return before;
    }

    public Optional<PkTuple> getAfter()
    {
        return after;
    }

    @Override
    public String toString()
    {
        return "TableEvent{" +
                "operation=" + operation +
                ", before=" + before +
                ", after=" + after +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableEvent that = (TableEvent) o;
        return Objects.equals(operation, that.operation) &&
                Objects.equals(before, that.before) &&
                Objects.equals(after, that.after);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(operation, before, after);
    }
}
