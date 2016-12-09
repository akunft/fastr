package com.oracle.truffle.r.nodes.builtin;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Interface for services providing {@link RBuiltinNode}'s that are dynamically loaded.
 *
 * See {@link RBuiltinPackages}
 */
public interface Extension {
	Map<Class<?>, Supplier<RBuiltinNode>> entries();
}
