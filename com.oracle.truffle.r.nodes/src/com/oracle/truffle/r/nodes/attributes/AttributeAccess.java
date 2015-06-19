/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.nodes.attributes;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.*;
import com.oracle.truffle.r.runtime.data.*;

/**
 * Simple attribute access node that specializes on the position at which the attribute was found
 * last time.
 */
public abstract class AttributeAccess extends Node {

    protected final String name;

    protected AttributeAccess(String name) {
        this.name = name.intern();
    }

    public abstract Object execute(RAttributes attr);

    protected boolean nameMatches(RAttributes attr, int index) {
        /*
         * The length check is against names.length instead of size, so that the check folds into
         * the array bounds check.
         */
        return index != -1 && attr.getNames().length > index && attr.getNames()[index] == name;
    }

    @Specialization(guards = "nameMatches(attr, index)")
    protected Object accessCached(RAttributes attr, //
                    @Cached("attr.find(name)") int index) {
        return attr.getValues()[index];
    }

    @Specialization(guards = "cachedSize == attr.size()")
    @ExplodeLoop
    protected Object accessCachedSize(RAttributes attr, //
                    @Cached("attr.size()") int cachedSize, //
                    @Cached("create()") BranchProfile foundProfile, //
                    @Cached("create()") BranchProfile notFoundProfile) {
        String[] names = attr.getNames();
        for (int i = 0; i < cachedSize; i++) {
            if (names[i] == name) {
                foundProfile.enter();
                return attr.getValues()[i];
            }
        }
        notFoundProfile.enter();
        return null;
    }

    @Specialization(contains = "accessCached")
    protected Object access(RAttributes attr) {
        return attr.get(name);
    }
}