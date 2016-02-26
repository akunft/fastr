/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.test.builtins;

import org.junit.*;

import com.oracle.truffle.r.test.*;

// Checkstyle: stop line length check

public class TestBuiltin_levelsassign_ extends TestBase {

    @Test
    public void testlevelsassign_1() {
        assertEval(Ignored.Unknown,
                        "argv <- structure(list(structure(c(4L, 4L, 3L, 6L, 5L, 4L, 4L,     5L, 4L, 4L, 2L, 1L, 3L, 2L, 2L, 3L, 4L, 3L, 2L, 2L, 2L, 4L,     3L, 2L, 6L, 3L, 6L, 2L, 3L, 5L, 2L, 6L, 4L, 2L, 1L, 6L, 6L,     4L, 5L, 3L, 5L, 5L, 6L, 2L, 4L, 3L, 4L, 5L, 4L, 4L, 2L, 6L,     1L, 3L, 5L, 4L, 5L, 2L, 5L, 1L, 6L, 5L, 3L, 6L, 3L, 6L, 3L,     6L, 4L, 6L, 1L, 3L, 2L, 5L, 3L, 3L, 2L, 4L, 3L, 4L, 2L, 1L,     4L, 5L, 3L, 6L, 3L, 6L, 6L, 6L, 4L, 4L, 6L, 4L, 2L, 2L, 3L,     3L, 3L, 5L, 3L, 1L, 5L, 6L, 6L, 6L, 2L, 3L, 3L, 5L, 4L, 2L,     2L, 5L, 2L, 3L, 1L, 3L, 3L, 6L, 1L, 6L, 5L, 4L, 3L, 1L, 1L,     1L, 3L, 5L, 4L, 4L, 1L, 1L, 6L, 2L, 6L, 6L, 1L, 5L, 6L, 6L,     2L, 4L, 6L, 6L, 4L, 4L, 5L, 4L, 4L, 1L, 3L, 1L, 2L, 6L, 6L,     1L, 2L, 6L, 3L, 5L, 4L, 6L, 5L, 3L, 4L, 2L, 5L, 2L, 3L, 2L,     1L, 5L, 1L, 1L, 6L, 6L, 1L, 4L, 2L, 3L, 4L, 3L, 3L, 5L, 3L,     6L, 4L, 3L, 5L, 3L, 5L, 5L, 4L, 2L, 2L, 4L, 1L, 6L, 5L, 6L,     3L, 5L, 1L, 2L, 1L, 3L, 5L, 1L, 4L, 1L, 2L, 4L, 5L, 4L, 6L,     3L, 5L, 6L, 1L, 3L, 4L, 6L, 5L, 5L, 1L, 3L, 2L, 6L, 5L, 5L,     4L, 3L, 5L, 3L, 6L, 5L, 4L, 4L, 2L, 4L, 5L, 2L, 5L, 3L, 1L,     1L, 6L, 5L, 6L, 6L, 1L, 5L, 5L, 3L, 6L, 6L, 1L, 4L, 3L, 6L,     4L, 4L, 6L, 4L, 4L, 3L, 5L, 4L, 1L, 2L, 1L, 5L, 2L, 6L, 4L,     1L, 3L, 4L, 4L, 2L, 4L, 5L, 6L, 5L, 5L, 5L, 3L, 1L, 6L, 2L,     2L, 2L, 6L, 4L, 2L, 5L, 4L, 3L, 4L, 3L, 1L, 2L, 2L, 3L, 4L,     5L, 6L, 4L, 3L, 4L, 6L, 3L, 3L, 5L, 6L, 3L, 3L, 3L, 1L, 2L,     4L, 5L, 2L, 5L, 4L, 4L, 2L, 4L, 3L, 1L, 3L, 3L, 6L, 5L, 4L,     2L, 1L, 4L, 4L, 1L, 4L, 3L, 4L, 3L, 1L, 4L, 6L, 2L, 5L, 6L,     3L, 6L, 2L, 1L, 6L, 6L, 2L, 6L, 6L, 5L, 2L, 2L, 4L, 6L, 6L,     5L, 2L, 3L, 3L, 1L, 3L, 4L, 3L, 5L, 5L, 2L, 4L, 2L, 2L, 6L,     3L, 6L, 4L, 4L, 1L, 4L, 5L, 2L, 2L, 4L, 6L, 5L, 4L, 3L, 2L,     4L, 4L, 6L, 5L, 4L, 4L, 6L, 4L, 4L, 1L, 5L, 2L, 1L, 6L, 5L,     4L, 2L, 5L, 4L, 2L, 4L, 6L, 1L, 6L, 5L, 4L, 5L, 1L, 4L, 6L,     2L, 4L, 4L, 2L, 3L, 2L, 1L, 5L, 2L, 4L, 5L, 2L, 5L, 3L, 2L,     3L, 6L, 6L, 3L, 1L, 3L, 2L, 6L, 5L, 5L, 4L, 3L, 3L, 6L, 5L,     2L, 5L, 4L, 5L, 1L, 2L, 6L, 2L, 6L, 3L, 5L, 6L, 1L, 6L, 3L,     4L, 2L, 1L, 6L, 2L, 5L, 5L, 4L, 3L, 2L, 2L, 2L, 1L, 2L, 6L,     1L, 5L, 1L, 3L, 1L, 1L, 6L, 4L, 5L, 2L, 4L, 2L, 5L, 3L, 4L,     1L, 2L, 5L, 1L, 1L, 2L, 6L, 2L, 4L, 3L, 3L, 4L, 4L, 5L, 5L,     6L, 1L, 4L, 2L, 2L, 3L, 3L, 3L, 6L, 3L, 5L, 4L, 4L, 3L, 3L,     3L, 3L, 5L, 4L, 5L, 1L, 4L, 4L, 5L, 6L, 4L, 5L, 1L, 6L, 2L,     1L, 3L, 6L, 3L, 2L, 5L, 1L, 3L, 2L, 3L, 3L, 2L, 5L, 3L, 5L,     5L, 4L, 6L, 6L, 5L, 6L, 6L, 3L, 4L, 2L, 4L, 2L, 3L, 1L, 4L,     5L, 4L, 1L, 5L, 4L, 5L, 6L, 3L, 5L, 6L, 5L, 1L, 2L, 2L, 4L,     6L, 4L, 5L, 6L, 3L, 4L, 2L, 1L, 2L, 5L, 3L, 6L, 5L, 5L, 5L,     3L, 5L, 5L, 2L, 2L, 3L, 2L, 5L, 5L, 4L, 5L, 1L, 5L, 2L, 5L,     4L, 2L, 4L, 6L, 3L, 6L, 3L, 1L, 6L, 5L, 4L, 5L, 6L, 4L, 5L,     2L, 1L, 3L, 6L, 1L, 5L, 1L, 2L, 5L, 2L, 1L, 6L, 4L, 1L, 6L,     3L, 2L, 2L, 4L, 5L, 5L, 5L, 3L, 3L, 1L, 4L, 2L, 4L, 6L, 1L,     3L, 1L, 6L, 3L, 2L, 1L, 3L, 3L, 4L, 1L, 3L, 3L, 5L, 1L, 2L,     2L, 5L, 2L, 4L, 3L, 2L, 3L, 3L, 6L, 5L, 1L, 4L, 3L, 4L, 5L,     5L, 1L, 5L, 6L, 5L, 2L, 2L, 3L, 5L, 3L, 1L, 2L, 5L, 5L, 1L,     3L, 4L, 3L, 3L, 6L, 5L, 2L, 5L, 5L, 2L, 6L, 2L, 1L, 1L, 2L,     6L, 4L, 5L, 1L, 2L, 1L, 1L, 4L, 4L, 1L, 3L, 5L, 4L, 4L, 3L,     4L, 5L, 3L, 4L, 5L, 1L, 3L, 2L, 3L, 4L, 3L, 5L, 3L, 2L, 4L,     5L, 1L, 2L, 4L, 3L, 6L, 3L, 6L, 3L, 6L, 3L, 4L, 3L, 2L, 3L,     6L, 2L, 4L, 1L, 1L, 2L, 2L, 5L, 3L, 2L, 3L, 6L, 2L, 3L, 2L,     5L, 5L, 2L, 3L, 3L, 5L, 3L, 5L, 4L, 6L, 2L, 2L, 1L, 5L, 4L,     4L, 4L, 1L, 6L, 6L, 3L, 2L, 3L, 6L, 4L, 4L, 4L, 4L, 4L, 5L,     3L, 5L, 6L, 5L, 2L, 4L, 6L, 5L, 6L, 5L, 5L, 1L, 3L, 6L, 3L,     2L, 2L, 4L, 4L, 2L, 5L, 4L, 4L, 6L, 4L, 5L, 5L, 5L, 3L, 6L,     4L, 6L, 5L, 6L, 4L, 4L, 6L, 2L, 3L, 5L, 5L, 2L, 5L, 4L, 4L,     1L, 4L, 2L, 6L, 2L, 1L, 4L, 2L, 6L, 4L, 2L, 3L, 4L, 6L, 6L,     2L, 3L, 4L, 3L, 2L, 3L, 5L, 2L, 6L, 4L, 4L, 1L, 5L, 3L, 6L,     1L, 2L, 3L, 5L, 5L, 5L, 5L, 3L, 5L, 4L, 5L, 6L, 4L, 5L, 5L,     3L, 4L, 4L, 2L, 4L, 3L, 4L, 6L, 3L, 5L, 2L, 5L, 5L, 4L, 2L,     1L, 6L, 2L, 4L, 6L, 3L, 3L, 6L, 5L, 6L, 1L, 5L, 2L, 4L, 6L,     4L, 5L, 3L, 2L, 6L, 1L, 3L, 3L, 3L, 2L, 4L, 3L, 2L, 5L, 5L,     4L, 2L, 6L, 6L, 2L, 6L, 3L, 6L, 1L, 4L, 6L, 4L, 6L, 6L, 1L,     5L, 1L, 3L, 1L, 6L, 1L, 3L, 2L, 3L, 2L, 2L, 4L, 1L, 3L, 1L,     5L, 3L, 5L, 4L, 3L, 2L, 3L, 2L, 3L, 3L, 6L, 1L, 2L, 6L, 5L,     2L, 6L, 2L, 6L, 5L, 3L, 1L, 2L, 2L, 4L, 5L, 4L, 6L, 5L, 3L,     4L, 2L, 5L, 6L, 4L, 1L, 5L, 3L, 1L, 5L, 4L, 2L, 2L, 5L, 2L,     5L, 4L, 3L, 5L, 3L, 2L, 2L, 3L, 2L, 1L, 4L, 5L, 4L, 6L, 3L,     6L, 3L, 6L, 4L, 1L, 6L, 6L, 4L, 1L, 5L, 2L, 5L, 5L, 4L, 2L,     5L, 4L, 6L, 4L, 6L, 3L, 4L, 1L, 4L, 3L, 1L, 4L, 5L, 3L, 4L,     1L, 5L, 5L, 1L, 2L, 1L, 4L, 1L, 5L, 5L, 4L, 4L, 6L, 6L, 4L,     6L, 4L, 2L, 2L, 3L, 5L, 1L, 2L, 3L, 6L, 3L, 4L, 4L, 2L, 4L,     2L, 3L, 3L, 2L, 1L, 1L, 3L, 2L, 2L, 1L, 1L, 6L, 3L, 3L, 6L,     1L, 6L, 4L, 2L, 4L, 2L, 1L, 1L, 4L, 4L, 4L, 6L, 4L, 4L, 6L,     2L, 3L, 3L, 3L, 2L, 2L, 4L, 4L, 6L, 5L, 5L, 3L, 4L, 5L, 4L,     1L, 3L, 1L, 5L, 5L, 6L, 5L, 1L, 5L, 3L, 6L, 3L, 4L, 6L, 3L,     3L, 5L, 1L, 5L, 2L, 3L, 2L, 2L, 2L, 2L, 4L, 4L, 2L, 5L, 4L,     1L, 2L, 3L, 1L, 4L, 2L, 1L, 6L, 4L, 1L, 4L, 2L, 5L, 4L, 3L,     5L, 2L, 1L, 1L, 4L, 3L, 2L, 3L, 1L, 2L, 6L, 6L, 1L, 3L, 4L,     1L, 5L, 6L, 4L, 4L), .Label = c('(0,25]', '(25,35]', '(35,45]',     '(45,55]', '(55,65]', '(65,99]'), class = 'factor'), value = c('age1824',     'age2534', 'age3544', 'age4554', 'age5564', 'age6599')),     .Names = c('', 'value'));" +
                                        "do.call('levels<-', argv)");
    }

}
