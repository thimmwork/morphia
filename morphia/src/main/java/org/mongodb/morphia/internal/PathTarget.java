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

import java.util.List;

import static java.util.Arrays.asList;
import static org.mongodb.morphia.internal.MorphiaUtils.join;

/**
 * This is an internal class and is subject to change or removal.
 *
 * @param <T> the target type
 * @since 1.3
 */
public abstract class PathTarget<T> {
    private final String path;
    private final List<String> segments;
    private boolean validateNames = true;
    private int position;
    private boolean resolved = false;
    private T target;

    /**
     * Creates a resolution context for the path.
     *
     * @param path path
     */
    protected PathTarget(final String path) {
        segments = asList(path.split("\\."));
        this.path = path;
    }

    String getPath() {
        return path;
    }

    List<String> getSegments() {
        return segments;
    }

    boolean isValidateNames() {
        return validateNames;
    }

    /**
     * Disables validation of path segments.
     */
    public void disableValidation() {
        resolved = false;
        validateNames = false;
    }

    protected boolean hasNext() {
        return position < segments.size();
    }

    /**
     * Returns the translated path for this context.  If validation is disabled, that path could be the same as the initial value.
     *
     * @return the translated path
     */
    public String translatedPath() {
        if (!resolved) {
            resolve();
        }
        return join(segments, '.');
    }

    /**
     * Returns the MappedField found at the end of a path.  May be null if the path is invalid and validation is disabled.
     *
     * @return the field
     */
    public T getTarget() {
        if (!resolved) {
            resolve();
        }
        return target;
    }

    protected void setTarget(final T target) {
        this.target = target;
    }

    protected abstract void resolve();

    void markResolved() {
        this.resolved = true;
    }

    String next() {
        return segments.get(position++);
    }

    void translate(final String nameToStore) {
        segments.set(position - 1, nameToStore);
    }

    void resetPosition() {
        this.position = 0;
    }
}
