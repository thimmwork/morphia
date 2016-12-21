package org.mongodb.morphia.converters;

import com.mongodb.DBObject;
import com.mongodb.DBRef;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.ObjectFactory;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author scotthernandez
 */
public class KeyConverter extends TypeConverter {

    /**
     * Creates the Converter.
     */
    public KeyConverter() {
        super(Key.class);
    }

    @Override
    public Object decode(final Class targetClass, final Object o, final MappedField mappedField) {
        if (o == null) {
            return null;
        }
        Object id = o;
        String collectionName;
        String databaseName = null;
        boolean idOnly = false;
        Mapper mapper = getMapper();
        ObjectFactory objectFactory = mapper.getOptions().getObjectFactory();

        if (o instanceof DBRef) {
            DBRef ref = (DBRef) o;

            id = ref.getId();
            collectionName = ref.getCollectionName();
            databaseName = ref.getDatabaseName();
        } else {
            Class refClass;
            if (mappedField.getType().equals(Key.class)) {
                refClass = mappedField.getTypeParameters().get(0).getType();
            } else {
                refClass = mappedField.getTypeParameters().get(0).getSubClass();
            }

            idOnly = true;
            collectionName = mapper.getMappedClass(refClass)
                                   .getCollectionName();
        }

        if (id instanceof DBObject) {
            id = mapper.fromDb(null, (DBObject) id,
                               objectFactory.createInstance(mapper, mappedField, (DBObject) id), new DefaultEntityCache());
        }
        return Key.builder()
                  .id(id)
                  .collection(collectionName)
                  .database(databaseName)
                  .idOnly(idOnly)
                  .build();

    }

    @Override
    public Object encode(final Object t, final MappedField optionalExtraInfo) {
        if (t == null) {
            return null;
        }
        Key key = (Key) t;
        return key.isIdOnly() ? key.getId() : getMapper().keyToDBRef(key);
    }

}
