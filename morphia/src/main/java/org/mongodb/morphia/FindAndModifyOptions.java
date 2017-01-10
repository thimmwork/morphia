/*
 * Copyright 2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.morphia;

import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.DBCollectionFindAndModifyOptions;

import java.util.concurrent.TimeUnit;

import static com.mongodb.assertions.Assertions.notNull;

/**
 * The options for find and modify operations.
 *
 * @since 1.3
 */
public final class FindAndModifyOptions {
    private DBCollectionFindAndModifyOptions options = new DBCollectionFindAndModifyOptions()
        .returnNew(true);

    /**
     * Sets the bypassDocumentValidation
     *
     * @param bypassDocumentValidation the bypassDocumentValidation
     * @return this
     */
    public FindAndModifyOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        options.bypassDocumentValidation(bypassDocumentValidation);
        return this;
    }

    /**
     * Sets the collation
     *
     * @param collation the collation
     * @return this
     * @mongodb.server.release 3.4
     */
    public FindAndModifyOptions collation(final Collation collation) {
        options.collation(collation);
        return this;
    }

    /**
     * Copies these options to a new instance
     *
     * @return a copy of this
     * @since 2.0
     */
    public FindAndModifyOptions copy() {
        FindAndModifyOptions copy = new FindAndModifyOptions();
        copy.bypassDocumentValidation(getBypassDocumentValidation());
        copy.collation(getCollation());
        copy.maxTime(getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        copy.projection(getProjection());
        copy.remove(isRemove());
        copy.returnNew(isReturnNew());
        copy.sort(getSort());
        copy.update(getUpdate());
        copy.upsert(isUpsert());
        copy.writeConcern(getWriteConcern());
        return copy;
    }

    /**
     * Returns the bypassDocumentValidation
     *
     * @return the bypassDocumentValidation
     */
    public Boolean getBypassDocumentValidation() {
        return options.getBypassDocumentValidation();
    }

    /**
     * Returns the collation options
     *
     * @return the collation options
     * @mongodb.server.release 3.4
     */
    public Collation getCollation() {
        return options.getCollation();
    }

    /**
     * Gets the maximum execution time on the server for this operation.  The default is 0, which places no limit on the execution time.
     *
     * @param timeUnit the time unit to return the result in
     * @return the maximum execution time in the given time unit
     * @mongodb.driver.manual reference/method/cursor.maxTimeMS/#cursor.maxTimeMS Max Time
     */
    public long getMaxTime(final TimeUnit timeUnit) {
        notNull("timeUnit", timeUnit);
        return options.getMaxTime(timeUnit);
    }

    /**
     * Returns the writeConcern
     *
     * @return the writeConcern
     * @mongodb.server.release 3.2
     */
    public WriteConcern getWriteConcern() {
        return options.getWriteConcern();
    }

    /**
     * Returns the remove
     *
     * @return the remove
     */
    public boolean isRemove() {
        return options.isRemove();
    }

    /**
     * Returns the returnNew
     *
     * @return the returnNew
     */
    public boolean isReturnNew() {
        return options.returnNew();
    }

    /**
     * Returns the upsert
     *
     * @return the upsert
     */
    public boolean isUpsert() {
        return options.isUpsert();
    }

    /**
     * Sets the maximum execution time on the server for this operation.
     *
     * @param maxTime  the max time
     * @param timeUnit the time unit, which may not be null
     * @return this
     * @mongodb.driver.manual reference/method/cursor.maxTimeMS/#cursor.maxTimeMS Max Time
     */
    public FindAndModifyOptions maxTime(final long maxTime, final TimeUnit timeUnit) {
        options.maxTime(maxTime, timeUnit);
        return this;
    }

    /**
     * Indicates whether to remove the elements matching the query or not
     *
     * @param remove true if the matching elements should be deleted
     * @return this
     */
    public FindAndModifyOptions remove(final boolean remove) {
        options.remove(remove);
        return this;
    }

    /**
     * Sets the returnNew
     *
     * @param returnNew the returnNew
     * @return this
     */
    public FindAndModifyOptions returnNew(final boolean returnNew) {
        options.returnNew(returnNew);
        return this;
    }

    /**
     * Indicates that an upsert should be performed
     *
     * @param upsert the upsert
     * @return this
     * @mongodb.driver.manual reference/method/db.collection.update/#upsert-behavior upsert
     */
    public FindAndModifyOptions upsert(final boolean upsert) {
        options.upsert(upsert);
        return this;
    }

    /**
     * Sets the writeConcern
     *
     * @param writeConcern the writeConcern
     * @return this
     * @mongodb.server.release 3.2
     */
    public FindAndModifyOptions writeConcern(final WriteConcern writeConcern) {
        options.writeConcern(writeConcern);
        return this;
    }

    public DBCollectionFindAndModifyOptions getOptions() {
        return copy().options;
    }

    DBObject getProjection() {
        return options.getProjection();
    }

    public FindAndModifyOptions projection(final DBObject projection) {
        options.projection(projection);
        return this;
    }

    /**
     * Returns the sort
     *
     * @return the sort
     */
    DBObject getSort() {
        return options.getSort();
    }

    /**
     * Sets the sort
     *
     * @param sort the sort
     * @return this
     */
    public FindAndModifyOptions sort(final DBObject sort) {
        options.sort(sort);
        return this;
    }

    /**
     * Returns the update
     *
     * @return the update
     */
    DBObject getUpdate() {
        return options.getUpdate();
    }

    /**
     * Sets the update
     *
     * @param update the update
     * @return this
     */
    public FindAndModifyOptions update(final DBObject update) {
        options.update(update);
        return this;
    }
}
