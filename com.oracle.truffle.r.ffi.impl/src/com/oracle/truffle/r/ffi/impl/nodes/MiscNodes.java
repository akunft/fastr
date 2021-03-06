/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.ffi.impl.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.r.ffi.impl.nodes.MiscNodesFactory.GetFunctionEnvironmentNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.MiscNodesFactory.LENGTHNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.MiscNodesFactory.OctSizeNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.MiscNodesFactory.RDoNewObjectNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.MiscNodesFactory.RDoSlotAssignNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.MiscNodesFactory.RDoSlotNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.MiscNodesFactory.RHasSlotNodeGen;
import com.oracle.truffle.r.nodes.access.AccessSlotNode;
import com.oracle.truffle.r.nodes.access.AccessSlotNodeGen;
import com.oracle.truffle.r.nodes.access.HasSlotNode;
import com.oracle.truffle.r.nodes.access.UpdateSlotNode;
import com.oracle.truffle.r.nodes.access.UpdateSlotNodeGen;
import com.oracle.truffle.r.nodes.attributes.SpecialAttributesFunctions.SetNamesAttributeNode;
import com.oracle.truffle.r.nodes.attributes.SpecialAttributesFunctionsFactory.SetNamesAttributeNodeGen;
import com.oracle.truffle.r.nodes.builtin.EnvironmentNodes.GetFunctionEnvironmentNode;
import com.oracle.truffle.r.nodes.builtin.casts.fluent.CastNodeBuilder;
import com.oracle.truffle.r.nodes.builtin.casts.fluent.HeadPhaseBuilder;
import com.oracle.truffle.r.nodes.objects.NewObject;
import com.oracle.truffle.r.nodes.objects.NewObjectNodeGen;
import com.oracle.truffle.r.nodes.unary.CastNode;
import com.oracle.truffle.r.nodes.unary.SizeToOctalRawNode;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.data.CharSXPWrapper;
import com.oracle.truffle.r.runtime.data.RArgsValuesAndNames;
import com.oracle.truffle.r.runtime.data.RFunction;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.RRawVector;
import com.oracle.truffle.r.runtime.data.RSymbol;
import com.oracle.truffle.r.runtime.data.RTypes;
import com.oracle.truffle.r.runtime.data.model.RAbstractContainer;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.data.nodes.GetDataAt;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.gnur.SEXPTYPE;

public final class MiscNodes {

    @TypeSystemReference(RTypes.class)
    public abstract static class LENGTHNode extends FFIUpCallNode.Arg1 {

        @Specialization
        protected int length(@SuppressWarnings("unused") RNull obj) {
            return 0;
        }

        @Specialization
        protected int length(@SuppressWarnings("unused") int x) {
            return 1;
        }

        @Specialization
        protected int length(@SuppressWarnings("unused") double x) {
            return 1;
        }

        @Specialization
        protected int length(@SuppressWarnings("unused") byte x) {
            return 1;
        }

        @Specialization
        protected int length(@SuppressWarnings("unused") String x) {
            return 1;
        }

        @Specialization
        protected int length(CharSXPWrapper obj) {
            return obj.getContents().length();
        }

        @Specialization
        protected int length(RAbstractContainer obj) {
            // Should this use RLengthNode?
            return obj.getLength();
        }

        @Specialization
        protected int length(REnvironment env) {
            // May seem wasteful of resources, but simple env.getFrame().getDescriptor().getSize()
            // is not correct!
            return env.ls(true, null, false).getLength();
        }

        @Specialization
        protected int length(RArgsValuesAndNames obj) {
            return obj.getLength();
        }

        @Fallback
        protected int length(Object obj) {
            CompilerDirectives.transferToInterpreter();
            throw RError.error(RError.SHOW_CALLER2, RError.Message.LENGTH_MISAPPLIED, SEXPTYPE.gnuRTypeForObject(obj).name());
        }

        public static LENGTHNode create() {
            return LENGTHNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class RDoSlotNode extends FFIUpCallNode.Arg2 {

        @Child private AccessSlotNode accessSlotNode;

        RDoSlotNode() {
            accessSlotNode = AccessSlotNodeGen.create(false);
        }

        @Specialization
        Object doSlot(Object o, RSymbol nameSym) {
            return accessSlotNode.executeAccess(o, nameSym.getName());
        }

        @Fallback
        Object doSlot(@SuppressWarnings("unused") Object o, Object name) {
            throw RError.error(RError.SHOW_CALLER2, RError.Message.INVALID_ARGUMENT_OF_TYPE, "name", SEXPTYPE.gnuRTypeForObject(name).name());
        }

        public static RDoSlotNode create() {
            return RDoSlotNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class RDoSlotAssignNode extends FFIUpCallNode.Arg3 {

        @Child private UpdateSlotNode updateSlotNode;

        RDoSlotAssignNode() {
            updateSlotNode = UpdateSlotNodeGen.create();
        }

        @Specialization
        Object doSlotAssign(Object o, String name, Object value) {
            return updateSlotNode.executeUpdate(o, name, value);
        }

        @Specialization
        Object doSlotAssign(Object o, RSymbol name, Object value) {
            return updateSlotNode.executeUpdate(o, name.getName(), value);
        }

        @Fallback
        Object doSlot(@SuppressWarnings("unused") Object o, Object name, @SuppressWarnings("unused") Object value) {
            throw RError.error(RError.SHOW_CALLER2, RError.Message.INVALID_ARGUMENT_OF_TYPE, "name", SEXPTYPE.gnuRTypeForObject(name).name());
        }

        public static RDoSlotAssignNode create() {
            return RDoSlotAssignNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class RDoNewObjectNode extends FFIUpCallNode.Arg1 {

        @Child private NewObject newObjectNode;

        RDoNewObjectNode() {
            newObjectNode = NewObjectNodeGen.create();
        }

        @Specialization
        Object doNewObject(Object classDef) {
            return newObjectNode.execute(classDef);
        }

        public static RDoNewObjectNode create() {
            return RDoNewObjectNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class RHasSlotNode extends FFIUpCallNode.Arg2 {

        @Child private HasSlotNode hasSlotNode;

        RHasSlotNode() {
            hasSlotNode = HasSlotNode.create(false);
        }

        @Specialization
        Object doSlot(Object o, RSymbol nameSym) {
            return hasSlotNode.executeAccess(o, nameSym.getName());
        }

        @Fallback
        @SuppressWarnings("unused")
        Object doSlot(Object o, Object name) {
            return false;
        }

        public static RHasSlotNode create() {
            return RHasSlotNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class NamesGetsNode extends FFIUpCallNode.Arg2 {

        @Child private SetNamesAttributeNode setNamesNode;

        NamesGetsNode() {
            setNamesNode = SetNamesAttributeNodeGen.create();
        }

        @Specialization
        Object doNewObject(Object vec, Object val) {
            setNamesNode.execute(vec, val);
            return vec;
        }

        public static NamesGetsNode create() {
            return MiscNodesFactory.NamesGetsNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class GetFunctionEnvironment extends FFIUpCallNode.Arg1 {

        /**
         * Returns the environment that {@code func} was created in.
         */
        @Specialization
        protected Object environment(RFunction fun,
                        @Cached("create()") GetFunctionEnvironmentNode getEnvNode) {
            return getEnvNode.getEnvironment(fun);
        }

        public static GetFunctionEnvironment create() {
            return GetFunctionEnvironmentNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class OctSizeNode extends FFIUpCallNode.Arg1 {

        @Specialization
        protected RRawVector octSize(Object size,
                        @Cached("create()") SizeToOctalRawNode sizeToOctal,
                        @Cached("createCast()") CastNode castToDoubleNode,
                        @Cached("create()") GetDataAt.Double getDataNode) {

            Object val = castToDoubleNode.doCast(size);
            if (val instanceof RAbstractDoubleVector) {
                RAbstractDoubleVector vec = (RAbstractDoubleVector) val;
                return sizeToOctal.execute(getDataNode.get(vec, vec.getInternalStore(), 0));
            }
            return sizeToOctal.execute(val);

        }

        protected CastNode createCast() {
            HeadPhaseBuilder<Double> findFirst = CastNodeBuilder.newCastBuilder().mustNotBeMissing().allowNull().asDoubleVector().findFirst();
            return findFirst.buildCastNode();
        }

        public static OctSizeNode create() {
            return OctSizeNodeGen.create();
        }
    }

}
