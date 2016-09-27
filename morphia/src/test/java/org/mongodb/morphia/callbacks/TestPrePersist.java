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

package org.mongodb.morphia.callbacks;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.mongodb.morphia.TestBase;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;

import java.util.ArrayList;
import java.util.List;

public class TestPrePersist extends TestBase {
    @Test
    public void hasThisElement() {
        getDs().createQuery(GroupChat.class)
               .field("members").hasThisElement(new GroupMember("some name", new ObjectId()));
    }

    @Test
    public void elemMatch() {
        getDs().createQuery(GroupChat.class)
               .field("members").elemMatch(getDs().createQuery(GroupMember.class)
                                                  .filter("name", "some name")
                                                  .filter("id", new ObjectId()));
    }

    private static class GroupChat {
        @Id
        private ObjectId id;
        private List<GroupMember> members = new ArrayList<GroupMember>();

        @PrePersist
        public void prepersist() {
            throw new RuntimeException("This should not be run when querying");
        }
    }

    private static class GroupMember {
        @Id
        private ObjectId id;
        private final String name;

        public GroupMember(final String name, final ObjectId id) {
            this.name = name;
            this.id = id;
        }

        @PrePersist
        public void prepersist() {
            throw new RuntimeException("This should not be run when querying");
        }
    }
}
