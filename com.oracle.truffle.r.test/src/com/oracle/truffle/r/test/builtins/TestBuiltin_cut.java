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

public class TestBuiltin_cut extends TestBase {

    @Test
    public void testcut1() {
        assertEval("argv <- structure(list(x = c(1.0346112150611, 0.0440203704340609,     -1.18549563351614, 0.649014015685885, -0.790829522519262,     1.17598399721214, 0.983434156282803, 0.541081558957578, -0.192970091592294,     -0.606426057996866, -0.548929268331095, 0.063509821468168,     -0.0758711318520365, -0.587354948512591, 0.68016119925159,     -0.00101371953355839, -2.04690635406766, -1.15419169868302,     1.57552198814761, -1.2826021432906, -0.0106456026122299,     -1.45914938013444, -0.0877132204592902, 0.644476581366902,     -0.174036946316013, 0.70686472456958, -0.800624461731312,     0.708086372571733, -0.297996173821721, 0.0138099804238364,     0.380733372967531, 0.128771481990839, 0.323047326927391,     -0.311589835954256, 0.12558341704142, -0.298476619409494,     -0.102902974277467, -1.68917669167977, -1.42657554846613,     -0.833840095454306, 0.0781210754813429, 0.0966440325613869,     0.471427686648137, -0.755241646713195, -1.09526706499915,     0.226011761333169, -2.00335228166377, 0.871788133884678,     -0.17604759041044, -0.392043928011201, 1.52493115014745,     0.696042602954131, 0.929759768084036, -0.937385053991658,     -0.505487042445614, 0.658795125401, -0.530682170997639, 1.35048133622788,     1.1503422698982, -1.03530396801882, 0.222351695228838, -0.439226819350318,     0.829770867923565, -0.843987984792906, 0.634088156420345,     0.940832655747169, -0.0115852787804222, -0.410726945127659,     -0.0645734442095184, -0.285424906860716, -0.132298134315469,     2.015980478747, -0.49752866007857, 0.461543245850607, 0.372715664260582,     0.911907622481186, 1.75893179717408, -0.111357338665413,     0.390438066934087, -0.15682295730562, 0.249796941030751,     -0.986665869092954, 0.342284950759752, 0.814635047060746,     -0.0433704725852363, 0.0953296428157898, -0.710060187690398,     0.0162693336595326, 0.406214045314364, -0.85345069761213,     0.294458010294818, -0.381515531303645, -0.341521027080523,     0.221675587474675, -1.33867071234436, 0.807929975242687,     -0.126382937192597, -0.0352338330882248, -1.4928897757059,     -0.235586522320615), breaks = structure(c(-2.04690635406766,     -0.511785824583621, -0.0234095559343235, 0.488841154725497,     2.015980478747), .Names = c('0%', '25%', '50%', '75%', '100%')),     labels = FALSE), .Names = c('x', 'breaks', 'labels'));" +
                        "do.call('cut', argv)");
    }

    @Test
    public void testcut2() {
        assertEval("argv <- structure(list(x = structure(c(50, 47, 37, 71, 62, 53,     49, 56, 50, 47, 30, 20, 44, 33, 34, 39, 54, 40, 35, 33, 31,     47, 37, 27, 66, 44, 73, 26, 40, 61, 27, 77, 50, 31, 23, 72,     90, 46, 56, 43, 62, 59, 69, 30, 55, 39, 55, 59, 54, 47, 32,     72, 22, 43, 62, 52, 59, 26, 59, 22, 77, 60, 41, 77, 42, 81,     40, 70, 47, 77, 23, 39, 33, 56, 45, 42, 28, 53, 39, 51, 26,     18, 55, 64, 42, 71, 43, 68, 72, 74, 55, 53, 67, 47, 28, 32,     38, 42, 38, 64, 44, 18, 61, 67, 75, 70, 27, 42, 45, 62, 47,     27, 28, 58, 34, 42, 24, 43, 43, 72, 22, 73, 61, 55, 43, 25,     21, 19, 45, 62, 52, 51, 20, 24, 88, 32, 66, 73, 21, 63, 77,     77, 26, 52, 67, 68, 47, 46, 64, 51, 46, 23, 39, 22, 28, 74,     68, 23, 29, 80, 43, 58, 55, 78, 58, 45, 49, 29, 58, 27, 40,     34, 23, 62, 18, 19, 66, 81, 25, 53, 28, 36, 47, 44, 37, 63,     37, 71, 47, 38, 56, 44, 64, 59, 55, 35, 31, 47, 21, 76, 62,     86, 43, 56, 20, 34, 23, 45, 58, 19, 53, 24, 30, 50, 63, 47,     73, 41, 62, 82, 21, 38, 50, 66, 59, 63, 25, 38, 28, 67, 60,     62, 48, 44, 59, 39, 82, 61, 54, 51, 35, 54, 58, 27, 58, 40,     22, 19, 68, 65, 76, 69, 25, 65, 56, 39, 82, 77, 23, 51, 40,     78, 48, 46, 73, 51, 50, 37, 56, 46, 20, 30, 25, 65, 31, 70,     52, 22, 38, 53, 48, 29, 52, 60, 80, 57, 63, 61, 36, 23, 78,     28, 26, 35, 66, 50, 34, 60, 50, 45, 54, 42, 25, 31, 30, 41,     55, 62, 74, 47, 41, 48, 71, 38, 39, 61, 73, 41, 41, 41, 24,     28, 49, 58, 27, 57, 52, 54, 35, 53, 45, 19, 37, 38, 78, 57,     55, 32, 24, 47, 46, 22, 51, 39, 46, 38, 20, 53, 82, 33, 62,     72, 44, 76, 31, 24, 78, 70, 28, 70, 69, 56, 32, 35, 53, 79,     83, 63, 28, 44, 38, 24, 41, 46, 39, 62, 63, 33, 54, 27, 27,     75, 42, 88, 52, 46, 25, 49, 58, 28, 27, 50, 74, 59, 49, 41,     33, 51, 50, 72, 65, 55, 51, 88, 52, 48, 25, 62, 34, 25, 77,     65, 48, 33, 58, 46, 34, 55, 75, 24, 73, 65, 50, 63, 24, 52,     72, 31, 53, 51, 26, 42, 29, 25, 58, 34, 46, 64, 28, 57, 45,     33, 39, 68, 76, 41, 23, 45, 28, 66, 57, 64, 48, 38, 43, 68,     62, 32, 56, 55, 58, 24, 26, 81, 33, 73, 36, 65, 69, 19, 67,     40, 46, 35, 23, 79, 32, 58, 59, 53, 43, 31, 32, 28, 23, 35,     75, 22, 63, 25, 39, 24, 24, 67, 52, 56, 34, 54, 29, 56, 37,     46, 24, 35, 65, 20, 24, 35, 82, 29, 53, 45, 40, 51, 46, 60,     65, 75, 22, 49, 29, 29, 43, 43, 45, 76, 39, 58, 49, 51, 40,     41, 44, 43, 62, 48, 65, 23, 48, 52, 63, 69, 49, 58, 19, 79,     28, 25, 43, 76, 44, 29, 65, 20, 41, 35, 37, 38, 28, 56, 38,     57, 57, 52, 72, 70, 58, 67, 77, 42, 46, 31, 55, 28, 41, 18,     49, 56, 51, 21, 56, 47, 61, 83, 36, 63, 66, 56, 19, 34, 30,     55, 70, 48, 62, 67, 44, 48, 26, 20, 35, 63, 38, 83, 56, 56,     57, 40, 64, 57, 31, 34, 38, 27, 64, 56, 48, 57, 25, 62, 35,     63, 50, 33, 52, 84, 38, 82, 44, 22, 70, 57, 47, 56, 74, 53,     57, 27, 21, 45, 68, 22, 61, 18, 30, 64, 31, 23, 74, 54, 21,     69, 38, 33, 27, 48, 58, 62, 64, 41, 41, 23, 48, 31, 46, 84,     21, 45, 21, 78, 41, 33, 21, 37, 44, 47, 23, 36, 39, 61, 25,     27, 27, 57, 26, 46, 40, 31, 42, 42, 71, 60, 19, 49, 40, 52,     58, 61, 25, 60, 77, 63, 26, 27, 45, 56, 36, 19, 26, 61, 56,     19, 38, 48, 45, 36, 83, 65, 35, 63, 63, 29, 81, 26, 19, 25,     26, 78, 47, 57, 23, 28, 20, 19, 50, 49, 25, 44, 60, 55, 51,     41, 46, 57, 43, 49, 62, 25, 37, 31, 41, 50, 39, 60, 45, 30,     49, 58, 23, 30, 46, 36, 76, 41, 77, 45, 70, 45, 47, 39, 29,     36, 66, 31, 54, 24, 22, 31, 35, 62, 37, 33, 37, 87, 28, 42,     27, 60, 65, 32, 42, 36, 65, 39, 57, 51, 68, 33, 33, 23, 61,     50, 55, 53, 22, 67, 74, 36, 26, 42, 66, 48, 46, 55, 49, 48,     58, 39, 61, 82, 59, 29, 46, 81, 57, 85, 64, 59, 19, 42, 76,     38, 29, 27, 48, 53, 35, 60, 53, 52, 77, 52, 57, 64, 56, 44,     77, 52, 84, 58, 74, 52, 51, 74, 28, 43, 64, 64, 34, 63, 55,     54, 18, 46, 29, 88, 29, 22, 53, 35, 86, 48, 29, 41, 46, 79,     69, 34, 43, 50, 45, 31, 39, 56, 32, 80, 46, 49, 20, 57, 44,     76, 24, 32, 45, 62, 65, 61, 61, 39, 63, 54, 64, 75, 53, 60,     63, 36, 47, 46, 35, 52, 43, 52, 77, 40, 63, 29, 61, 65, 55,     28, 19, 75, 34, 51, 69, 41, 40, 74, 62, 86, 19, 63, 29, 52,     68, 50, 64, 43, 27, 66, 23, 40, 41, 39, 28, 48, 37, 29, 58,     65, 51, 27, 67, 83, 35, 73, 38, 66, 18, 47, 71, 49, 68, 71,     18, 59, 19, 37, 22, 71, 20, 40, 30, 44, 28, 29, 52, 20, 40,     23, 64, 38, 57, 52, 44, 35, 37, 32, 37, 38, 69, 19, 30, 77,     58, 31, 80, 29, 79, 57, 42, 20, 33, 28, 49, 65, 48, 90, 57,     43, 51, 32, 60, 73, 50, 23, 57, 38, 24, 65, 51, 28, 33, 56,     26, 61, 51, 45, 64, 41, 33, 34, 39, 31, 22, 55, 59, 54, 66,     40, 72, 45, 79, 46, 21, 82, 67, 52, 24, 57, 30, 57, 63, 50,     33, 56, 53, 67, 54, 67, 43, 51, 22, 53, 45, 21, 47, 63, 44,     51, 25, 57, 56, 21, 28, 23, 54, 20, 63, 65, 53, 54, 82, 66,     54, 68, 55, 31, 31, 36, 61, 25, 31, 36, 77, 39, 49, 55, 30,     51, 34, 44, 36, 35, 24, 23, 45, 30, 35, 20, 25, 66, 36, 41,     69, 19, 75, 50, 29, 49, 33, 20, 19, 52, 54, 53, 67, 51, 48,     82, 34, 45, 36, 41, 34, 32, 48, 49, 86, 63, 61, 40, 50, 63,     49, 25, 44, 25, 64, 64, 78, 58, 23, 61, 41, 76, 44, 54, 70,     39, 44, 64, 19, 56, 28, 39, 26, 33, 29, 34, 49, 46, 30, 59,     48, 21, 33, 44, 21, 49, 31, 20, 84, 55, 24, 50, 26, 63, 50,     44, 65, 28, 24, 22, 55, 42, 26, 44, 22, 35, 71, 66, 23, 42,     50, 24, 56, 66, 47, 50), value.labels = structure(c(99, 98,     0), .Names = c('89. RF', '88. DK', '00. NA'))), c(0, 25,     35, 45, 55, 65, 99)), .Names = c('x', ''));" +
                        "do.call('cut', argv)");
    }
}
