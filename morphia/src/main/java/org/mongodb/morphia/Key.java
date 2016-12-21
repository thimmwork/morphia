package org.mongodb.morphia;


import com.mongodb.DBRef;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <p> The key object; this class is take from the app-engine datastore (mostly) implementation. It is also Serializable and GWT-safe,
 * enabling your entity objects to be used for GWT RPC should you so desire. </p> <p/> <p> You may use normal DBRef objects as
 * relationships
 * in your entities if you desire neither type safety nor GWT-ability. </p>
 *
 * @param <T> The type of the entity
 * @author Jeff Schnitzer <jeff@infohazard.org> (from Objectify codebase)
 * @author Scott Hernandez (adapted to morphia/mongodb)
 */
@SuppressWarnings("CheckStyle")
public class Key<T> implements Serializable, Comparable<Key<T>> {
    private DBRef dbRef;
    private Class<? extends T> type;
    private boolean idOnly;
    private T entity;

    /**
     * Id value
     */
    private byte[] idBytes;

    /**
     * For GWT serialization
     */
    protected Key() {
    }

    /**
     * Create a key with an id
     *
     * @param type       the type of the entity
     * @param collection the collection in which the entity lives
     * @param id         the value of the entity's ID
     */
    public Key(final Class<? extends T> type, final String collection, final Object id) {
        this.type = type;
        this.dbRef = new DBRef(collection, id);
    }

    /**
     * Create a key with an id
     *
     * @param type       the type of the entity
     * @param collection the collection in which the entity lives
     * @param idBytes    the value of the entity's ID
     */
    public Key(final Class<? extends T> type, final String collection, final byte[] idBytes) {
        this.type = type;
        this.idBytes = Arrays.copyOf(idBytes, idBytes.length);
        this.dbRef = new DBRef(collection, idBytes);
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    /**
     * @return the collection name.
     */
    public String getCollection() {
        return dbRef.getCollectionName();
    }

    /**
     * Sets the collection name.
     *
     * @param collection the collection to use
     */
    public void setCollection(final String collection) {
        dbRef = new DBRef(dbRef.getDatabaseName(), collection, dbRef.getId());
    }

    public DBRef getDBRef() {
        return dbRef;
    }

    public T getEntity() {
        return entity;
    }

    /**
     * @return the id associated with this key.
     */
    public Object getId() {
        return dbRef.getId();
    }

    public boolean isIdOnly() {
        return idOnly;
    }

    /**
     * @return type of the entity
     */
    public Class<? extends T> getType() {
        return type;
    }

    void setEntity(final T entity) {
        this.entity = entity;
    }

    /**
     * Sets the type of the entity for this Key
     *
     * @param clazz the type to use
     */
    public void setType(final Class<? extends T> clazz) {
        type = clazz;
    }

    @Override
    public int compareTo(final Key<T> other) {
        checkState(this);
        checkState(other);

        int cmp;
        // First collection
        if (other.type != null && type != null) {
            cmp = type.getName().compareTo(other.type.getName());
            if (cmp != 0) {
                return cmp;
            }
        }
        cmp = compareNullable(dbRef.getCollectionName(), other.getDBRef().getCollectionName());
        if (cmp != 0) {
            return cmp;
        }

        try {
            cmp = compareNullable((Comparable<?>) getDBRef().getId(), (Comparable<?>) other.getDBRef().getId());
            if (cmp != 0) {
                return cmp;
            }
        } catch (Exception e) {
            // Not a comparable, use equals and String.compareTo().
            cmp = getDBRef().getId().equals(other.getDBRef().getId()) ? 0 : 1;
            if (cmp != 0) {
                return getDBRef().getId().toString().compareTo(other.getDBRef().getId().toString());
            }
        }

        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Key)) {
            return false;
        }

        final Key<?> key = (Key<?>) o;

        if (idOnly != key.idOnly) {
            return false;
        }
        return dbRef.equals(key.dbRef);
    }

    @Override
    public int hashCode() {
        int result = dbRef.hashCode();
        result = 31 * result + (idOnly ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder bld = new StringBuilder("Key{");

        if (getDBRef().getCollectionName() != null) {
            bld.append("collection=");
            bld.append(getDBRef().getCollectionName());
        } else {
            bld.append("type=");
            bld.append(type.getName());
        }
        bld.append(", id=");
        bld.append(getDBRef().getId());
        bld.append("}");

        return bld.toString();
    }

    /** */
    @SuppressWarnings("unchecked")
    private static int compareNullable(final Comparable o1, final Comparable o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else {
            return o1.compareTo(o2);
        }
    }

    private void checkState(final Key k) {
        if (k.type == null && k.dbRef.getCollectionName() == null) {
            throw new IllegalStateException("Collection must be specified (or a class).");
        }
        if (k.dbRef.getId() == null && k.idBytes == null) {
            throw new IllegalStateException("id must be specified");
        }
    }

    public static class Builder<T> {
        private String database;
        private String collection;
        private Class<? extends T> type;
        private Object id;
        private boolean idOnly = false;
        private T entity;

        private Builder() {}

        public Key<T> build() {
            Key<T> key = new Key<T>();
            key.dbRef = new DBRef(database, collection, id);
            key.entity = entity;
            key.type = type;
            key.idOnly = idOnly;

            if (this.collection == null && this.id == null) {
                throw new IllegalStateException("A Key requires at least a collection or ID: " + key);
            }
            return key;
        }

        public Builder<T> database(final String database) {
            this.database = database;
            return this;
        }

        public Builder<T> collection(final String collection) {
            this.collection = collection;
            return this;
        }

        public Builder<T> type(final Class<? extends T> type) {
            this.type = type;
            return this;
        }

        public Builder<T> idOnly(final boolean idOnly) {
            this.idOnly = idOnly;
            return this;
        }

        public Builder<T> id(final Object id) {
            this.id = id;
            return this;
        }

        public Builder<T> entity(final T entity) {
            this.entity = entity;
            return this;
        }
    }

    public static class KeyOptions {
        private boolean idOnly;
        private String collection;
        private String database;

        public KeyOptions collection(final String collection) {
            this.collection = collection;
            return this;
        }

        public String collection() {
            return collection;
        }

        public String database() {
            return database;
        }

        public boolean idOnly() {
            return idOnly;
        }

        public KeyOptions database(final String database) {
            this.database = database;
            return this;
        }

        public KeyOptions idOnly(final boolean idOnly) {
            this.idOnly = idOnly;
            return this;
        }
    }
}
