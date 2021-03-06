/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.ffi.impl.nfi;

import static com.oracle.truffle.r.ffi.impl.common.RFFIUtils.guaranteeInstanceOf;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.ffi.impl.common.JavaUpCallsRFFIImpl;
import com.oracle.truffle.r.ffi.impl.nfi.TruffleNFI_UpCallsRFFIImplFactory.VectorWrapperMRFactory.DispatchAllocateNodeGen;
import com.oracle.truffle.r.ffi.impl.upcalls.FFIUnwrapNode;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.data.CharSXPWrapper;
import com.oracle.truffle.r.runtime.data.RComplexVector;
import com.oracle.truffle.r.runtime.data.RDoubleVector;
import com.oracle.truffle.r.runtime.data.RIntVector;
import com.oracle.truffle.r.runtime.data.RLogicalVector;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.RRawVector;
import com.oracle.truffle.r.runtime.data.RVector;
import com.oracle.truffle.r.runtime.ffi.DLL;
import com.oracle.truffle.r.runtime.ffi.DLL.CEntry;
import com.oracle.truffle.r.runtime.ffi.DLL.DLLInfo;
import com.oracle.truffle.r.runtime.ffi.DLL.DotSymbol;
import com.oracle.truffle.r.runtime.ffi.NativeFunction;
import com.oracle.truffle.r.runtime.ffi.UnsafeAdapter;

public class TruffleNFI_UpCallsRFFIImpl extends JavaUpCallsRFFIImpl {

    private final Node asPointer = Message.AS_POINTER.createNode();

    @Override
    @TruffleBoundary
    public Object Rf_mkCharLenCE(Object bytes, int len, int encoding) {
        // "bytes" is actually a Long unboxed from a NativePointer
        // TODO: handle encoding properly
        long address;
        try {
            address = ForeignAccess.sendAsPointer(asPointer, (TruffleObject) bytes);
        } catch (UnsupportedMessageException ex) {
            throw RInternalError.shouldNotReachHere(ex);
        }
        return CharSXPWrapper.create(TruffleNFI_Utils.getString(address, len));
    }

    @Override
    public Object R_alloc(int n, int size) {
        long result = UnsafeAdapter.UNSAFE.allocateMemory(n * size);
        getContext().transientAllocations.add(result);
        return result;
    }

    @MessageResolution(receiverType = VectorWrapper.class)
    public static class VectorWrapperMR {

        @Resolve(message = "IS_POINTER")
        public abstract static class IntVectorWrapperNativeIsPointerNode extends Node {
            protected Object access(@SuppressWarnings("unused") VectorWrapper receiver) {
                return true;
            }
        }

        // TODO: with separate version of this for the different types, it would be more efficient
        // and not need the dispatch
        public abstract static class DispatchAllocate extends Node {
            private static final long EMPTY_DATA_ADDRESS = 0x1BAD;

            public abstract long execute(Object vector);

            @Specialization
            protected static long get(RIntVector vector) {
                return vector.allocateNativeContents();
            }

            @Specialization
            protected static long get(RLogicalVector vector) {
                return vector.allocateNativeContents();
            }

            @Specialization
            protected static long get(RRawVector vector) {
                return vector.allocateNativeContents();
            }

            @Specialization
            protected static long get(RDoubleVector vector) {
                return vector.allocateNativeContents();
            }

            @Specialization
            protected static long get(RComplexVector vector) {
                return vector.allocateNativeContents();
            }

            @Specialization
            protected static long get(CharSXPWrapper vector) {
                return vector.allocateNativeContents();
            }

            @Specialization
            protected static long get(@SuppressWarnings("unused") RNull nullValue) {
                // Note: GnuR is OK with, e.g., INTEGER(NULL), but it's illegal to read from or
                // write to the resulting address.
                return EMPTY_DATA_ADDRESS;
            }

            @Fallback
            protected static long get(Object vector) {
                throw RInternalError.shouldNotReachHere("invalid wrapped object " + vector.getClass().getSimpleName());
            }
        }

        // TODO: "TO_NATIVE" should do the actual work of transferring the data

        @Resolve(message = "AS_POINTER")
        public abstract static class IntVectorWrapperNativeAsPointerNode extends Node {
            @Child private DispatchAllocate dispatch = DispatchAllocateNodeGen.create();

            protected long access(VectorWrapper receiver) {
                long address = dispatch.execute(receiver.vector);
                // System.out.println(String.format("allocating native buffer for %s at %16x",
                // receiver.vector.getClass().getSimpleName(), address));
                return address;
            }
        }

        // TODO: "READ", "WRITE"

        @CanResolve
        public abstract static class VectorWrapperCheck extends Node {
            protected static boolean test(TruffleObject receiver) {
                return receiver instanceof VectorWrapper;
            }
        }
    }

    public static final class VectorWrapper implements TruffleObject {

        private final Object vector;

        public VectorWrapper(Object vector) {
            this.vector = vector;
        }

        @Override
        public ForeignAccess getForeignAccess() {
            return VectorWrapperMRForeign.ACCESS;
        }
    }

    @Override
    public Object INTEGER(Object x) {
        // also handles LOGICAL
        assert x instanceof RIntVector || x instanceof RLogicalVector || x == RNull.instance;
        return new VectorWrapper(guaranteeVectorOrNull(x, RVector.class));
    }

    @Override
    public Object LOGICAL(Object x) {
        return new VectorWrapper(guaranteeVectorOrNull(x, RLogicalVector.class));
    }

    @Override
    public Object REAL(Object x) {
        return new VectorWrapper(guaranteeVectorOrNull(x, RDoubleVector.class));
    }

    @Override
    public Object RAW(Object x) {
        return new VectorWrapper(guaranteeVectorOrNull(x, RRawVector.class));
    }

    @Override
    public Object COMPLEX(Object x) {
        return new VectorWrapper(guaranteeVectorOrNull(x, RComplexVector.class));
    }

    @Override
    public Object R_CHAR(Object x) {
        return new VectorWrapper(guaranteeVectorOrNull(x, CharSXPWrapper.class));
    }

    @Override
    @TruffleBoundary
    public Object getCCallable(String pkgName, String functionName) {
        DLLInfo lib = DLL.safeFindLibrary(pkgName);
        CEntry result = lib.lookupCEntry(functionName);
        if (result == null) {
            throw RError.error(RError.NO_CALLER, RError.Message.UNKNOWN_OBJECT, functionName);
        }
        return result.address.value;
    }

    @Override
    @TruffleBoundary
    protected DotSymbol setSymbol(DLLInfo dllInfo, int nstOrd, Object routines, int index) {
        Node executeNode = Message.createExecute(4).createNode();
        try {
            return (DotSymbol) FFIUnwrapNode.unwrap(
                            ForeignAccess.sendExecute(executeNode, TruffleNFI_Context.getInstance().lookupNativeFunction(NativeFunction.Rdynload_setSymbol), dllInfo, nstOrd, routines, index));
        } catch (InteropException ex) {
            throw RInternalError.shouldNotReachHere(ex);
        }
    }

    private static TruffleNFI_Context getContext() {
        return (TruffleNFI_Context) RContext.getInstance().getStateRFFI();
    }

    private static Object guaranteeVectorOrNull(Object obj, Class<?> clazz) {
        if (obj == RNull.instance) {
            return RNull.instance;
        }
        return guaranteeInstanceOf(obj, clazz);
    }
}
