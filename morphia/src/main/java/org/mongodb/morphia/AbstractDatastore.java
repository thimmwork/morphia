/*
 * Copyright 2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.morphia;

import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MappingException;
import org.mongodb.morphia.mapping.lazy.proxy.ProxyHelper;
import org.mongodb.morphia.query.CountOptions;
import org.mongodb.morphia.query.DefaultQueryFactory;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryFactory;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * This provides the base level implementation of the Datastore.  The default implementations are the legacy DatastoreImpl and the new
 * implementation based on the POJO Codec in the driver.
 */
@SuppressWarnings("deprecation")
public abstract class AbstractDatastore implements AdvancedDatastore {
    private final Morphia morphia;
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private WriteConcern defConcern;
    private volatile QueryFactory queryFactory = new DefaultQueryFactory();

    AbstractDatastore(final Morphia morphia, final MongoClient mongoClient, final String databaseName) {
        this.morphia = morphia;
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase(databaseName);
    }

    @Override
    public <T, V> WriteResult delete(final Class<T> clazz, final V id) {
        return delete(clazz, id, new DeleteOptions().writeConcern(getWriteConcern(clazz)));
    }

    @Override
    public <T> WriteResult delete(final Query<T> query) {
        return delete(query, new DeleteOptions().writeConcern(getWriteConcern(query.getEntityClass())));
    }

    @Override
    @Deprecated
    public <T> WriteResult delete(final Query<T> query, final WriteConcern wc) {
        return delete(query, new DeleteOptions().writeConcern(wc));
    }

    @Override
    public <T> WriteResult delete(final T entity) {
        return delete(entity, getWriteConcern(entity));
    }

    @Override
    @Deprecated
    public <T> WriteResult delete(final T entity, final WriteConcern wc) {
        return delete(entity, new DeleteOptions().writeConcern(wc));
    }

    @Override
    @Deprecated
    public <T> void ensureIndex(final Class<T> type, final String fields) {
        ensureIndex(type, null, fields, false, false);
    }

    @Override
    public void ensureIndexes() {
        ensureIndexes(false);
    }

    @Override
    public <T> void ensureIndexes(final Class<T> clazz) {
        ensureIndexes(clazz, false);
    }

    @Override
    public Key<?> exists(final Object entityOrKey) {
        return buildExistsQuery(entityOrKey)
            .getKey();
    }

    @Override
    public <T> Query<T> find(final Class<T> clazz) {
        return createQuery(clazz);
    }

    @Override
    @Deprecated
    public <T, V> Query<T> find(final Class<T> clazz, final String property, final V value) {
        final Query<T> query = createQuery(clazz);
        return query.filter(property, value);
    }

    @Override
    @Deprecated
    public <T, V> Query<T> find(final Class<T> clazz, final String property, final V value, final int offset, final int size) {
        final Query<T> query = createQuery(clazz);
        query.offset(offset);
        query.limit(size);
        return query.filter(property, value);
    }

    @Override
    public <T> T findAndDelete(final Query<T> query) {
        return findAndDelete(query, new FindAndModifyOptions());
    }

    @Override
    public <T> T findAndModify(final Query<T> query, final UpdateOperations<T> operations) {
        return findAndModify(query, operations, new FindAndModifyOptions()
            .returnNew(true));
    }

    @Override
    @Deprecated
    public <T> T findAndModify(final Query<T> query, final UpdateOperations<T> operations, final boolean oldVersion) {
        return findAndModify(query, operations, new FindAndModifyOptions()
            .returnNew(!oldVersion)
            .upsert(false));
    }

    @Override
    @Deprecated
    public <T> T findAndModify(final Query<T> query, final UpdateOperations<T> operations, final boolean oldVersion,
                               final boolean createIfMissing) {
        return findAndModify(query, operations, new FindAndModifyOptions()
            .returnNew(!oldVersion)
            .upsert(createIfMissing));

    }

    @Override
    public <T> List<T> getByKeys(final Iterable<Key<T>> keys) {
        return getByKeys(null, keys);
    }

    @Override
    public <T> long getCount(final Query<T> query) {
        return query.count();
    }

    @Override
    public <T> long getCount(final Query<T> query, final CountOptions options) {
        return query.count(options);
    }

    @Override
    public WriteConcern getDefaultWriteConcern() {
        return defConcern;
    }

    @Override
    public void setDefaultWriteConcern(final WriteConcern defConcern) {
        this.defConcern = defConcern;
    }

    @Override
    public MongoClient getMongo() {
        return mongoClient;
    }

    @Override
    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    @Override
    public void setQueryFactory(final QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public <T> Key<T> merge(final T entity) {
        return merge(entity, getWriteConcern(entity));
    }

    @Override
    public <T> Iterable<Key<T>> save(final Iterable<T> entities) {
        Iterator<T> iterator = entities.iterator();
        return !iterator.hasNext()
               ? Collections.<Key<T>>emptyList()
               : save(entities, getWriteConcern(iterator.next()));
    }

    @Override
    public <T> Iterable<Key<T>> save(final Iterable<T> entities, final WriteConcern wc) {
        return save(entities, new InsertOptions().writeConcern(wc));
    }

    @Override
    public <T> Iterable<Key<T>> save(final Iterable<T> entities, final InsertOptions options) {
        final List<Key<T>> savedKeys = new ArrayList<Key<T>>();
        for (final T ent : entities) {
            savedKeys.add(save(ent, options));
        }
        return savedKeys;

    }

    @Override
    @Deprecated
    public <T> Iterable<Key<T>> save(final T... entities) {
        return save(asList(entities), new InsertOptions());
    }

    @Override
    public <T> Key<T> save(final T entity) {
        return save(entity, new InsertOptions());
    }

    @Override
    @Deprecated
    public <T> Key<T> save(final T entity, final WriteConcern wc) {
        return save(entity, new InsertOptions()
            .writeConcern(wc));
    }

    @Override
    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> operations) {
        return update(query, operations, new UpdateOptions()
            .upsert(false)
            .multi(true)
            .writeConcern(getWriteConcern(query.getEntityClass())));
    }

    @Override
    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing) {
        return update(query, operations, new UpdateOptions()
            .upsert(createIfMissing)
            .writeConcern(getWriteConcern(query.getEntityClass())));
    }

    @Override
    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing,
                                    final WriteConcern wc) {
        return update(query, operations, new UpdateOptions()
            .upsert(createIfMissing)
            .multi(true)
            .writeConcern(wc));
    }

    @Override
    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> operations) {
        return update(query, operations, new UpdateOptions());
    }

    @Override
    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing) {
        return update(query, operations, new UpdateOptions().upsert(createIfMissing));

    }

    @Override
    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> operations, final boolean createIfMissing,
                                         final WriteConcern wc) {
        return update(query, operations, new UpdateOptions()
            .upsert(createIfMissing)
            .writeConcern(wc));
    }

    @Override
    @Deprecated
    public <T> void ensureIndex(final String collection, final Class<T> type, final String fields) {
        ensureIndex(collection, type, null, fields, false, false);
    }

    @Override
    public <T> void ensureIndexes(final String collection, final Class<T> clazz) {
        ensureIndexes(collection, clazz, false);
    }

    @Override
    public Key<?> exists(final Object entityOrKey, final ReadPreference readPreference) {
        final Query<?> query = buildExistsQuery(entityOrKey);
        if (readPreference != null) {
            query.useReadPreference(readPreference);
        }
        return query.getKey();
    }

    @Override
    public <T> Query<T> find(final String collection, final Class<T> clazz) {
        return createQuery(collection, clazz);
    }

    @Override
    public <T, V> Query<T> find(final String collection, final Class<T> clazz, final String property, final V value, final int offset,
                                final int size) {
        return find(collection, clazz, property, value, offset, size, true);
    }

    @Override
    public <T> Key<T> insert(final T entity) {
        return insert(entity, getWriteConcern(entity));
    }

    @Override
    public <T> Key<T> insert(final T entity, final WriteConcern wc) {
        return insert(entity, new InsertOptions().writeConcern(wc));
    }

    @Override
    @Deprecated
    public <T> Iterable<Key<T>> insert(final T... entities) {
        return insert(asList(entities));
    }

    /**
     * Inserts entities in to the database
     *
     * @param entities the entities to insert
     * @param <T>      the type of the entities
     * @return the keys of entities
     */
    @Override
    public <T> Iterable<Key<T>> insert(final Iterable<T> entities) {
        return insert(entities, new InsertOptions()
            .writeConcern(getDefaultWriteConcern()));
    }

    @Override
    public <T> Iterable<Key<T>> insert(final Iterable<T> entities, final WriteConcern wc) {
        return insert(entities, new InsertOptions().writeConcern(wc));
    }

    @Override
    public <T> Iterable<Key<T>> insert(final String collection, final Iterable<T> entities) {
        return insert(collection, entities, new InsertOptions());
    }

    @Override
    public <T> Key<T> save(final String collection, final T entity) {
        final T unwrapped = ProxyHelper.unwrap(entity);
        return save(collection, entity, getWriteConcern(unwrapped));
    }

    /**
     * Find all instances by type in a different collection than what is mapped on the class given skipping some documents and returning a
     * fixed number of the remaining.
     *
     * @param collection The collection use when querying
     * @param clazz      the class to use for mapping the results
     * @param property   the document property to query against
     * @param value      the value to check for
     * @param offset     the number of results to skip
     * @param size       the maximum number of results to return
     * @param validate   if true, validate the query
     * @param <T>        the type to query
     * @param <V>        the type to filter value
     * @return the query
     */
    public <T, V> Query<T> find(final String collection, final Class<T> clazz, final String property, final V value, final int offset,
                                final int size, final boolean validate) {
        final Query<T> query = find(collection, clazz);
        if (!validate) {
            query.disableValidation();
        }
        query.offset(offset);
        query.limit(size);
        return query.filter(property, value).enableValidation();
    }

    /**
     * @return the Morphia instance used by this Datastore.
     */
    public Morphia getMorphia() {
        return morphia;
    }

    protected abstract <T> String getCollectionName(Class<T> clazz);

    protected MongoDatabase getDatabase() {
        return database;
    }

    protected abstract WriteConcern getWriteConcern(Object clazzOrEntity);

    private Query<?> buildExistsQuery(final Object entityOrKey) {
        final Object unwrapped = ProxyHelper.unwrap(entityOrKey);
        final Key<?> key = getKey(unwrapped);
        final Object id = key.getId();
        if (id == null) {
            throw new MappingException("Could not get id for " + unwrapped.getClass().getName());
        }

        return find(key.getCollection(), key.getType()).filter(Mapper.ID_KEY, key.getId());
    }

    <T> MongoCollection<T> getMongoCollection(final Class<T> clazz) {
        return getMongoCollection(getCollectionName(clazz), clazz);
    }

    <T> MongoCollection<T> getMongoCollection(final String name, final Class<T> clazz) {
        return database.getCollection(name, clazz);
    }

    <T> FindAndModifyOptions enforceWriteConcern(final FindAndModifyOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                .copy()
                .writeConcern(getWriteConcern(klass));
        }
        return options;
    }

    <T> InsertOptions enforceWriteConcern(final InsertOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                .copy()
                .writeConcern(getWriteConcern(klass));
        }
        return options;
    }

    <T> UpdateOptions enforceWriteConcern(final UpdateOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                .copy()
                .writeConcern(getWriteConcern(klass));
        }
        return options;
    }

    <T> DeleteOptions enforceWriteConcern(final DeleteOptions options, final Class<T> klass) {
        if (options.getWriteConcern() == null) {
            return options
                .copy()
                .writeConcern(getWriteConcern(klass));
        }
        return options;
    }
}
