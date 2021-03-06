/*
 * Copyright 2017 Pilosa Corp.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.pilosa.client;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a result from Bitmap, Union, Intersect, Difference and Range PQL calls.
 *
 * @see <a href="https://www.pilosa.com/docs/query-language/">Query Language</a>
 */
public final class RowResult implements QueryResult {
    /**
     * Returns the attributes of the reply.
     *
     * @return attributes
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    /**
     * Returns the columns in the reply.
     *
     * @return list of column IDs where the corresponding bit is 1
     */
    public List<Long> getColumns() {
        return this.columns;
    }

    /**
     * Returns the keys in the reply (Enterprise version)
     *
     * @return keys
     */
    public List<String> getKeys() {
        return this.keys;
    }

    @Override
    public int getType() {
        return QueryResultType.ROW;
    }

    @Override
    public RowResult getRow() {
        return this;
    }

    @Override
    public List<CountResultItem> getCountItems() {
        return TopNResult.defaultItems();
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public long getValue() {
        return 0;
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("RowResult(attrs=%s, columns=%s, keys=%s)",
                this.attributes, this.columns, this.keys);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RowResult)) {
            return false;
        }
        RowResult rhs = (RowResult) obj;
        return new EqualsBuilder()
                .append(this.attributes, rhs.attributes)
                .append(this.columns, rhs.columns)
                .append(this.keys, rhs.keys)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(31, 47)
                .append(this.attributes)
                .append(this.columns)
                .append(this.keys)
                .toHashCode();
    }

    static RowResult create(Map<String, Object> attributes, List<Long> columns, List<String> keys) {
        RowResult result = new RowResult();
        result.attributes = (attributes == null) ? defaultAttributes : attributes;
        result.columns = (columns == null) ? defaultColumns : columns;
        result.keys = (keys == null) ? defaultKeys : keys;
        return result;
    }

    static RowResult fromInternal(Internal.QueryResult q) {
        Internal.Row b = q.getRow();
        return create(Util.protobufAttrsToMap(b.getAttrsList()),
                b.getColumnsList(),
                b.getKeysList());
    }

    static RowResult defaultResult() {
        return defaultResult;
    }

    static {
        RowResult result = new RowResult();
        result.attributes = new HashMap<>();
        result.columns = new ArrayList<>();
        defaultResult = result;
    }

    private static RowResult defaultResult;
    private static Map<String, Object> defaultAttributes = new HashMap<>(0);
    private static List<Long> defaultColumns = new ArrayList<>(0);
    private static List<String> defaultKeys = new ArrayList<>(0);

    private Map<String, Object> attributes;
    private List<Long> columns;
    private List<String> keys;
}
