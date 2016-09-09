/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin.casts;

import com.oracle.truffle.r.runtime.RType;

/**
 * Represents a single step in the cast pipeline. {@code PipelineStep}, {@code Mapper} and
 * {@code Filter} are only symbolic representation of the pipeline, these objects can be transformed
 * to something useful by using corresponding visitors, e.g. {@linek PipelineStepVisitor}. Steps can
 * be chained as a linked list by setting the next step in the chain using
 * {@link #setNext(PipelineStep)}. The order of steps should be the same as the order of cast
 * pipeline API invocations.
 */
public abstract class PipelineStep {

    private PipelineStep next;

    public final PipelineStep getNext() {
        return next;
    }

    public final PipelineStep setNext(PipelineStep next) {
        this.next = next;
        return this;
    }

    public abstract <T> T accept(PipelineStepVisitor<T> visitor);

    public interface PipelineStepVisitor<T> {
        T visit(FindFirstStep step);

        T visit(AsVectorStep step);

        T visit(MapStep step);

        T visit(MapIfStep step);

        T visit(FilterStep step);

        T visit(NotNAStep step);

        T visit(DefaultErrorStep step);
    }

    /**
     * Changes the current default error, which is used by steps/filters that do not have error
     * message set explicitly.
     */
    public static final class DefaultErrorStep extends PipelineStep {
        private final MessageData defaultMessage;

        public DefaultErrorStep(MessageData defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public MessageData getDefaultMessage() {
            return defaultMessage;
        }

        @Override
        public <T> T accept(PipelineStepVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * If the replacement is set (!= null), then maps NA values to the replacement, otherwise raises
     * given error on NA value of any type.
     */
    public static final class NotNAStep extends PipelineStep {
        private final MessageData message;
        private final Object replacement;

        public NotNAStep(Object replacement) {
            this.message = null;
            this.replacement = replacement;
        }

        public NotNAStep(MessageData message) {
            this.message = message;
            this.replacement = null;
        }

        public MessageData getMessage() {
            return message;
        }

        public Object getReplacement() {
            return replacement;
        }

        @Override
        public <T> T accept(PipelineStepVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Takes the first element of a vector. If the vector is empty, null or missing, then either
     * raises an error or returns default value if set.
     */
    public static final class FindFirstStep extends PipelineStep {
        private final MessageData error;
        private final Object defaultValue;
        private final Class<?> elementClass;

        public FindFirstStep(Object defaultValue, Class<?> elementClass, MessageData error) {
            this.defaultValue = defaultValue;
            this.elementClass = elementClass;
            this.error = error;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public Class<?> getElementClass() {
            return elementClass;
        }

        public MessageData getError() {
            return error;
        }

        @Override
        public <T> T accept(PipelineStepVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Converts the value to a vector of given {@link RType}. Null and missing values are forwarded.
     */
    public static final class AsVectorStep extends PipelineStep {
        private final RType type;

        public AsVectorStep(RType type) {
            assert type.isVector() && type != RType.List : "AsVectorStep supports only vector types minus list.";
            this.type = type;
        }

        public RType getType() {
            return type;
        }

        @Override
        public <T> T accept(PipelineStepVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static final class MapStep extends PipelineStep {
        private final Mapper mapper;

        public MapStep(Mapper mapper) {
            this.mapper = mapper;
        }

        public Mapper getMapper() {
            return mapper;
        }

        @Override
        public <T> T accept(PipelineStepVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Allows to execute on of given pipeline chains depending on the condition.
     */
    public static final class MapIfStep extends PipelineStep {
        private final Filter filter;
        private final PipelineStep trueBranch;
        private final PipelineStep falseBranch;

        public MapIfStep(Filter filter, PipelineStep trueBranch, PipelineStep falseBranch) {
            this.filter = filter;
            this.trueBranch = trueBranch;
            this.falseBranch = falseBranch;
        }

        public Filter getFilter() {
            return filter;
        }

        public PipelineStep getTrueBranch() {
            return trueBranch;
        }

        public PipelineStep getFalseBranch() {
            return falseBranch;
        }

        @Override
        public <T> T accept(PipelineStepVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Raises an error if the value does not conform to the given filter.
     */
    public static final class FilterStep extends PipelineStep {
        private final Filter filter;

        public FilterStep(Filter filter) {
            this.filter = filter;
        }

        public Filter getFilter() {
            return filter;
        }

        @Override
        public <T> T accept(PipelineStepVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}
