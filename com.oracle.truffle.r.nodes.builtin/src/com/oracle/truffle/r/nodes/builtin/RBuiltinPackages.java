/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.r.nodes.builtin.base.*;
import com.oracle.truffle.r.options.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.env.*;
import com.oracle.truffle.r.runtime.env.REnvironment.PutException;

/**
 * Support for the default set of packages in an R session. Setup is a two-phase process. The
 * meta-data for the possible set of default packages (which is a known set) is established
 * statically (to support an AOT-based VM without runtime reflection), and then the dynamic state is
 * established for a given subset of the packages at runtime, through the {@link #load} method.
 */
public final class RBuiltinPackages implements RBuiltinLookup {

    private static final LinkedHashMap<String, RBuiltinPackage> packages = new LinkedHashMap<>(6);

    private static final RBuiltinPackages instance = new RBuiltinPackages();

    static {
        RBuiltinPackages.add(new BasePackage());
    }

    protected static void add(RBuiltinPackage builtins) {
        packages.put(builtins.getName(), builtins);
    }

    public static RBuiltinPackages getInstance() {
        return instance;
    }

    public static Map<String, RBuiltinPackage> getPackages() {
        return packages;
    }

    public static void load(String name, MaterializedFrame frame, REnvironment envForFrame) {
        RBuiltinPackage pkg = packages.get(name);
        if (pkg == null) {
            Utils.fail("unknown default package: " + name);
        }
        pkg.setEnv(envForFrame);
        if (FastROptions.BindBuiltinNames.getValue()) {
            /*
             * All the RBuiltin PRIMITIVE methods that were created earlier need to be added to the
             * environment so that lookups through the environment work as expected.
             */
            Map<String, RBuiltinFactory> builtins = pkg.getBuiltins();
            for (Map.Entry<String, RBuiltinFactory> entrySet : builtins.entrySet()) {
                String methodName = entrySet.getKey();
                RBuiltinFactory builtinFactory = entrySet.getValue();
                builtinFactory.setEnv(envForFrame);
                RBuiltin builtin = builtinFactory.getRBuiltin();
                if (builtin.kind() != RBuiltinKind.INTERNAL) {
                    RFunction function = createFunction(builtinFactory, methodName);
                    try {
                        envForFrame.put(methodName, function);
                    } catch (PutException ex) {
                        Utils.fail("failed to install builtin function: " + methodName);
                    }
                }
            }
        }
        RPackageVariables.Handler varHandler = RPackageVariables.getHandler(name);
        if (varHandler != null) {
            varHandler.preInitialize(envForFrame);
        }
        pkg.loadSources(frame, envForFrame);
    }

    @Override
    public RFunction lookup(String methodName) {
        RFunction function = RContext.getInstance().getCachedFunction(methodName);
        if (function != null) {
            return function;
        }

        RBuiltinFactory builtin = lookupBuiltin(methodName);
        if (builtin == null) {
            return null;
        }
        return createFunction(builtin, methodName);
    }

    private static RFunction createFunction(RBuiltinFactory builtinFactory, String methodName) {
        RootCallTarget callTarget = RBuiltinNode.createArgumentsCallTarget(builtinFactory);
        RBuiltin builtin = builtinFactory.getRBuiltin();
        assert builtin != null;
        return RContext.getInstance().putCachedFunction(methodName, RDataFactory.createFunction(builtinFactory.getBuiltinNames()[0], callTarget, builtin, builtinFactory.getEnv().getFrame()));
    }

    public static RBuiltinFactory lookupBuiltin(String name) {
        for (RBuiltinPackage pkg : packages.values()) {
            RBuiltinFactory factory = pkg.lookupByName(name);
            if (factory != null) {
                return factory;
            }
        }
        return null;
    }

    /**
     * Used by {@link RDeparse} to detect whether a symbol is a builtin (or special), i.e. not an
     * {@link RBuiltinKind#INTERNAL}. N.B. special functions are not explicitly denoted currently,
     * only by virtue of the {@link RBuiltin#nonEvalArgs} attribute.
     */
    public boolean isPrimitiveBuiltin(String name) {
        for (RBuiltinPackage pkg : packages.values()) {
            RBuiltinFactory rbf = pkg.lookupByName(name);
            if (rbf != null && rbf.getRBuiltin().kind() != RBuiltinKind.INTERNAL) {
                return true;
            }
        }
        return false;
    }

}
