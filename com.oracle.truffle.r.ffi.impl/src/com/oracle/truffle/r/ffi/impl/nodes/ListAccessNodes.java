/*
 * Copyright (c) 2017, 2017, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.r.ffi.impl.nodes.ListAccessNodesFactory.CADDRNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.ListAccessNodesFactory.CADRNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.ListAccessNodesFactory.CARNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.ListAccessNodesFactory.CDDRNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.ListAccessNodesFactory.CDRNodeGen;
import com.oracle.truffle.r.ffi.impl.nodes.ListAccessNodesFactory.SETCARNodeGen;
import com.oracle.truffle.r.nodes.attributes.SpecialAttributesFunctions.GetNamesAttributeNode;
import com.oracle.truffle.r.nodes.attributes.SpecialAttributesFunctions.SetNamesAttributeNode;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.data.RArgsValuesAndNames;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RLanguage;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.RPairList;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.RSymbol;
import com.oracle.truffle.r.runtime.data.RTypes;
import com.oracle.truffle.r.runtime.data.nodes.GetDataCopy;

/**
 * Nodes that implement {@code CAR}, {@code CDR}, etc. N.B. GNU R does not error check the
 * arguments; it will crash (segv) if given, say, a numeric arg.
 */
public final class ListAccessNodes {

    @TypeSystemReference(RTypes.class)
    public abstract static class CARNode extends FFIUpCallNode.Arg1 {
        @Specialization
        protected Object car(RPairList pl) {
            return pl.car();
        }

        @Specialization
        protected Object car(RLanguage lang) {
            return lang.getDataAtAsObject(0);
        }

        @Specialization
        protected Object car(RArgsValuesAndNames args) {
            return args.getArgument(0);
        }

        @Specialization
        protected Object car(RSymbol sym) {
            return sym;
        }

        @Specialization
        protected Object car(RList list) {
            return list.getDataAt(0);
        }

        @Specialization
        protected Object car(@SuppressWarnings("unused") RNull nil) {
            return RNull.instance;
        }

        @Fallback
        protected Object car(@SuppressWarnings("unused") Object obj) {
            throw RInternalError.unimplemented("CAR only works on pair lists, language objects, argument lists, and symbols");
        }

        public static CARNode create() {
            return CARNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public static final class SETCADRNode extends FFIUpCallNode.Arg2 {
        @Child private SETCARNode setcarNode = SETCARNode.create();
        @Child private CDRNode cdrNode = CDRNode.create();

        @Override
        public Object executeObject(Object x, Object y) {
            return setcarNode.executeObject(cdrNode.executeObject(x), y);
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class SETCARNode extends FFIUpCallNode.Arg2 {
        public static SETCARNode create() {
            return SETCARNodeGen.create();
        }

        @Specialization
        protected Object doRLang(RLanguage x, Object y) {
            x.getPairList().setCar(y);
            return y;
        }

        @Specialization
        protected Object doRLang(RPairList x, Object y) {
            x.setCar(y);
            return y;
        }

        @Fallback
        protected Object car(@SuppressWarnings("unused") Object x, @SuppressWarnings("unused") Object y) {
            throw RInternalError.unimplemented("SETCAR only works on pair lists or language objects");
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class CDRNode extends FFIUpCallNode.Arg1 {
        @Specialization
        protected Object cdr(RPairList pl) {
            return pl.cdr();
        }

        @Specialization
        protected Object cdr(RLanguage lang) {
            RPairList l = lang.getPairList();
            return l.cdr();
        }

        @Specialization
        protected Object cdr(RArgsValuesAndNames args) {
            return args.toPairlist().cdr();
        }

        @Specialization
        protected Object cdr(RList list,
                        @Cached("create()") GetDataCopy.String getDataCopyNode,
                        @Cached("create()") GetNamesAttributeNode getNamesNode,
                        @Cached("create()") SetNamesAttributeNode setNamesNode) {
            if (list.getLength() == 1) {
                return RNull.instance;
            }
            RStringVector names = getNamesNode.getNames(list);
            RList copy = RDataFactory.createList(list.getDataCopy());
            if (names != null) {
                String[] namesDataCopy = getDataCopyNode.execute(names);
                setNamesNode.setNames(copy, RDataFactory.createStringVector(namesDataCopy, true));
            }
            return copy;
        }

        @Fallback
        protected Object cdr(@SuppressWarnings("unused") Object obj) {
            throw RInternalError.unimplemented("CDR only works on pair lists, language objects, and argument lists");
        }

        public static CDRNode create() {
            return CDRNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class CADRNode extends FFIUpCallNode.Arg1 {
        @Specialization
        protected Object cadr(RPairList pl) {
            return pl.cadr();
        }

        @Specialization
        protected Object cadr(RLanguage lang) {
            return lang.getDataAtAsObject(1);
        }

        @Fallback
        protected Object cadr(@SuppressWarnings("unused") Object obj) {
            throw RInternalError.unimplemented("CADR only works on pair lists and language objects");
        }

        public static CADRNode create() {
            return CADRNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class CADDRNode extends FFIUpCallNode.Arg1 {
        @Specialization
        protected Object caddr(RPairList pl) {
            return pl.caddr();
        }

        @Specialization
        protected Object caddr(RLanguage lang) {
            return lang.getDataAtAsObject(2);
        }

        @Fallback
        protected Object caddr(@SuppressWarnings("unused") Object obj) {
            throw RInternalError.unimplemented("CADDR only works on pair lists and language objects");
        }

        public static CADDRNode create() {
            return CADDRNodeGen.create();
        }
    }

    @TypeSystemReference(RTypes.class)
    public abstract static class CDDRNode extends FFIUpCallNode.Arg1 {
        @Specialization
        protected Object cddr(RPairList pl) {
            return pl.cddr();
        }

        @Specialization
        protected Object cdr(RLanguage lang) {
            RPairList l = lang.getPairList();
            return l.cddr();
        }

        @Fallback
        protected Object cddr(@SuppressWarnings("unused") Object obj) {
            throw RInternalError.unimplemented("CDDR only works on pair lists and language objects");
        }

        public static CDDRNode create() {
            return CDDRNodeGen.create();
        }
    }
}
