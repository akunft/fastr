/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 1995, 1996, 1997  Robert Gentleman and Ross Ihaka
 * Copyright (c) 1995-2014, The R Core Team
 * Copyright (c) 2002-2008, The R Foundation
 * Copyright (c) 2016, 2017, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.library.stats;

import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.nullValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.missingValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.instanceOf;
import static com.oracle.truffle.r.runtime.nmath.MathConstants.DBL_MIN;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.r.nodes.attributes.GetFixedAttributeNode;
import com.oracle.truffle.r.nodes.attributes.SetAttributeNode;
import com.oracle.truffle.r.nodes.attributes.SpecialAttributesFunctions.GetDimAttributeNode;
import com.oracle.truffle.r.nodes.attributes.SpecialAttributesFunctions.SetClassAttributeNode;
import com.oracle.truffle.r.nodes.builtin.RExternalBuiltinNode;
import com.oracle.truffle.r.runtime.data.nodes.ReadAccessor;
import com.oracle.truffle.r.runtime.data.nodes.VectorReadAccess;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RDoubleVector;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.ops.na.NACheck;

public abstract class Cdist extends RExternalBuiltinNode.Arg4 {
    private static final NACheck naCheck = NACheck.create();

    @Child private GetFixedAttributeNode getNamesAttrNode = GetFixedAttributeNode.createNames();

    static {
        Casts casts = new Casts(Cdist.class);
        casts.arg(0).mustBe(nullValue().not(), RError.Message.VECTOR_IS_TOO_LARGE).mustBe(missingValue().not()).asDoubleVector();
        casts.arg(1).asIntegerVector().findFirst();
        casts.arg(2).mustBe(instanceOf(RList.class));
        casts.arg(3).asDoubleVector().findFirst();
    }

    @Specialization(guards = "method == cachedMethod")
    protected RDoubleVector cdist(RAbstractDoubleVector x, @SuppressWarnings("unused") int method, RList list, double p, @SuppressWarnings("unused") @Cached("method") int cachedMethod,
                    @Cached("create()") VectorReadAccess.Double xAccess,
                    @Cached("getMethod(method)") Method methodObj,
                    @Cached("create()") SetAttributeNode setAttrNode,
                    @Cached("create()") SetClassAttributeNode setClassAttrNode,
                    @Cached("create()") GetDimAttributeNode getDimNode) {
        int nr = getDimNode.nrows(x);
        int nc = getDimNode.ncols(x);
        int n = nr * (nr - 1) / 2; /* avoid int overflow for N ~ 50,000 */
        double[] ans = new double[n];
        RDoubleVector xm = x.materialize();
        rdistance(new ReadAccessor.Double(x, xAccess), nr, nc, ans, false, methodObj, p);
        RDoubleVector result = RDataFactory.createDoubleVector(ans, naCheck.neverSeenNA());
        DynamicObject resultAttrs = result.initAttributes();

        RStringVector names = (RStringVector) getNamesAttrNode.execute(list);
        if (names != null) {
            for (int i = 0; i < names.getLength(); i++) {
                String name = names.getDataAt(i);
                Object listValue = list.getDataAt(i);
                if (name.equals(RRuntime.CLASS_ATTR_KEY)) {
                    setClassAttrNode.execute(result, listValue instanceof RStringVector ? (RStringVector) listValue : RDataFactory.createStringVectorFromScalar((String) listValue));
                } else {
                    setAttrNode.execute(resultAttrs, name, listValue);
                }
            }
        }

        return result;
    }

    private static boolean bothNonNAN(double a, double b) {
        return !RRuntime.isNAorNaN(a) && !RRuntime.isNAorNaN(b);
    }

    private static boolean bothFinite(double a, double b) {
        return RRuntime.isFinite(a) && RRuntime.isFinite(b);
    }

    public Method getMethod(int method) {
        if (method < 1 || method > Method.values().length) {
            throw error(RError.Message.GENERIC, "distance(): invalid distance");
        }
        return Method.values()[method - 1];
    }

    private void rdistance(ReadAccessor.Double xAccess, int nr, int nc, double[] d, boolean diag, Method method, double p) {
        int ij; /* can exceed 2^31 - 1, but Java can't handle that */
        //
        if (method == Method.MINKOWSKI) {
            if (!RRuntime.isFinite(p) || p <= 0) {
                throw error(RError.Message.GENERIC, "distance(): invalid p");
            }
        }
        int dc = diag ? 0 : 1; /* diag=1: we do the diagonal */
        ij = 0;
        naCheck.enable(true);
        for (int j = 0; j <= nr; j++) {
            for (int i = j + dc; i < nr; i++) {
                double r = method.dist(xAccess, nr, nc, i, j, p);
                naCheck.check(r);
                d[ij++] = r;
            }
        }
    }

    public enum Method {
        EUCLIDEAN {
            @Override
            public double dist(ReadAccessor.Double xAccess, int nr, int nc, final int i1in, final int i2in, double p) {
                int i1 = i1in;
                int i2 = i2in;
                double dev;
                double dist;
                int count;
                int j;

                count = 0;
                dist = 0;
                for (j = 0; j < nc; j++) {
                    if (bothNonNAN(xAccess.getDataAt(i1), xAccess.getDataAt(i2))) {
                        dev = (xAccess.getDataAt(i1) - xAccess.getDataAt(i2));
                        if (!RRuntime.isNAorNaN(dev)) {
                            dist += dev * dev;
                            count++;
                        }
                    }
                    i1 += nr;
                    i2 += nr;
                }
                if (count == 0) {
                    return RRuntime.DOUBLE_NA;
                }
                if (count != nc) {
                    dist /= ((double) count / nc);
                }
                return Math.sqrt(dist);

            }
        },
        MAXIMUM {
            @Override
            public double dist(ReadAccessor.Double xAccess, int nr, int nc, final int i1in, final int i2in, double p) {
                int i1 = i1in;
                int i2 = i2in;
                double dev;
                double dist;
                int count;
                int j;

                count = 0;
                dist = -Double.MAX_VALUE;
                for (j = 0; j < nc; j++) {
                    if (bothNonNAN(xAccess.getDataAt(i1), xAccess.getDataAt(i2))) {
                        dev = Math.abs(xAccess.getDataAt(i1) - xAccess.getDataAt(i2));
                        if (!RRuntime.isNAorNaN(dev)) {
                            if (dev > dist) {
                                dist = dev;
                            }
                            count++;
                        }
                    }
                    i1 += nr;
                    i2 += nr;
                }
                if (count == 0) {
                    return RRuntime.DOUBLE_NA;
                }
                return dist;

            }
        },
        MANHATTAN {
            @Override
            public double dist(ReadAccessor.Double xAccess, int nr, int nc, final int i1in, final int i2in, double p) {
                int i1 = i1in;
                int i2 = i2in;
                double dev;
                double dist;
                int count;
                int j;

                count = 0;
                dist = 0;
                for (j = 0; j < nc; j++) {
                    if (bothNonNAN(xAccess.getDataAt(i1), xAccess.getDataAt(i2))) {
                        dev = Math.abs(xAccess.getDataAt(i1) - xAccess.getDataAt(i2));
                        if (!RRuntime.isNAorNaN(dev)) {
                            dist += dev;
                            count++;
                        }
                    }
                    i1 += nr;
                    i2 += nr;
                }
                if (count == 0) {
                    return RRuntime.DOUBLE_NA;
                }
                if (count != nc) {
                    dist /= ((double) count / nc);
                }
                return dist;

            }
        },
        CANBERRA {
            @Override
            public double dist(ReadAccessor.Double xAccess, int nr, int nc, final int i1in, final int i2in, double p) {
                int i1 = i1in;
                int i2 = i2in;
                double dev;
                double dist;
                double sum;
                double diff;
                int count;
                int j;

                count = 0;
                dist = 0;
                for (j = 0; j < nc; j++) {
                    if (bothNonNAN(xAccess.getDataAt(i1), xAccess.getDataAt(i2))) {
                        sum = Math.abs(xAccess.getDataAt(i1) + xAccess.getDataAt(i2));
                        diff = Math.abs(xAccess.getDataAt(i1) - xAccess.getDataAt(i2));
                        if (sum > DBL_MIN || diff > DBL_MIN) {
                            dev = diff / sum;
                            if (!RRuntime.isNAorNaN(dev) ||
                                            (!RRuntime.isFinite(diff) && diff == sum &&
                                                            /* use Inf = lim x -> oo */ ((dev = 1.) != 0))) {
                                dist += dev;
                                count++;
                            }
                        }
                    }
                    i1 += nr;
                    i2 += nr;
                }
                if (count == 0) {
                    return RRuntime.DOUBLE_NA;
                }
                if (count != nc) {
                    dist /= ((double) count / nc);
                }
                return dist;

            }
        },
        BINARY {
            @Override
            public double dist(ReadAccessor.Double xAccess, int nr, int nc, final int i1in, final int i2in, double p) {
                int i1 = i1in;
                int i2 = i2in;
                int total;
                int count;
                int dist;
                int j;

                total = 0;
                count = 0;
                dist = 0;

                for (j = 0; j < nc; j++) {
                    if (bothNonNAN(xAccess.getDataAt(i1), xAccess.getDataAt(i2))) {
                        if (!bothFinite(xAccess.getDataAt(i1), xAccess.getDataAt(i2))) {
                            RError.warning(RError.SHOW_CALLER2, RError.Message.GENERIC, "treating non-finite values as NA");
                        } else {
                            if (xAccess.getDataAt(i1) != 0. || xAccess.getDataAt(i2) != 0.) {
                                count++;
                                if (!(xAccess.getDataAt(i1) != 0. && xAccess.getDataAt(i2) != 0.)) {
                                    dist++;
                                }
                            }
                            total++;
                        }
                    }
                    i1 += nr;
                    i2 += nr;
                }

                if (total == 0) {
                    return RRuntime.DOUBLE_NA;
                }
                if (count == 0) {
                    return 0;
                }
                return (double) dist / count;

            }
        },
        MINKOWSKI {
            @Override
            public double dist(ReadAccessor.Double xAccess, int nr, int nc, final int i1in, final int i2in, double p) {
                int i1 = i1in;
                int i2 = i2in;
                double dev;
                double dist;
                int count;
                int j;

                count = 0;
                dist = 0;
                for (j = 0; j < nc; j++) {
                    if (bothNonNAN(xAccess.getDataAt(i1), xAccess.getDataAt(i2))) {
                        dev = (xAccess.getDataAt(i1) - xAccess.getDataAt(i2));
                        if (!RRuntime.isNAorNaN(dev)) {
                            dist += Math.pow(Math.abs(dev), p);
                            count++;
                        }
                    }
                    i1 += nr;
                    i2 += nr;
                }
                if (count == 0) {
                    return RRuntime.DOUBLE_NA;
                }
                if (count != nc) {
                    dist /= ((double) count / nc);
                }
                return Math.pow(dist, 1.0 / p);
            }
        };

        public abstract double dist(ReadAccessor.Double xAccess, int nr, int nc, int i1, int i2, double p);
    }
}
