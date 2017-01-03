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

package org.mongodb.morphia.internal;

import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.ValidationException;

import java.util.Iterator;

import static java.lang.String.format;
import static org.mongodb.morphia.internal.MorphiaUtils.join;

/**
 * This is an internal class and is subject to change or removal.
 *
 * @since 1.3
 */
public class MapperPathTarget extends PathTarget<MappedField> {
    private Mapper mapper;
    private MappedClass context;
    private MappedClass root;

    /**
     * Creates a resolution context for the given root and path.
     *
     * @param mapper mapper
     * @param root   root
     * @param path   path
     */
    public MapperPathTarget(final Mapper mapper, final MappedClass root, final String path) {
        super(path);
        this.root = root;
        this.mapper = mapper;
    }

    @Override
    public String toString() {
        return String.format("PathTarget{root=%s, segments=%s, target=%s}", root.getClass().getSimpleName(), getSegments(), getTarget());
    }

    @Override
    protected void resolve() {
        context = this.root;
        resetPosition();
        MappedField field = null;
        while (hasNext()) {
            String segment = next();

            if (segment.equals("$") || segment.matches("[0-9]+")) {  // array operator
                if (!hasNext()) {
                    throw new ValidationException("The given path is invalid: " + getPath());
                }
                segment = next();
            }
            field = resolveField(segment);

            if (field != null) {
                if (!field.isMap()) {
                    translate(field.getNameToStore());
                } else {
                    if (hasNext()) {
                        next();  // consume the map key segment
                    }
                }
            } else {
                if (isValidateNames()) {
                    throw new ValidationException(format("Could not resolve path '%s' against '%s'.", join(getSegments(), '.'),
                                                         root.getClazz().getName()));
                }
            }
        }
        setTarget(field);
        markResolved();
    }

    private MappedField resolveField(final String segment) {
        MappedField mf = context.getMappedField(segment);
        if (mf == null) {
            mf = context.getMappedFieldByJavaField(segment);
        }
        if (mf == null) {
            Iterator<MappedClass> subTypes = mapper.getSubTypes(context).iterator();
            while (mf == null && subTypes.hasNext()) {
                context = subTypes.next();
                mf = resolveField(segment);
            }
        }

        if (mf != null) {
            context = mapper.getMappedClass(mf.getSubClass() != null ? mf.getSubClass() : mf.getConcreteType());
        }
        return mf;
    }

}
