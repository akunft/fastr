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
package com.oracle.truffle.r.test.packages.analyzer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oracle.truffle.r.test.packages.analyzer.detectors.DiffDetector;
import com.oracle.truffle.r.test.packages.analyzer.detectors.InstallationProblemDetector;
import com.oracle.truffle.r.test.packages.analyzer.detectors.RErrorDetector;
import com.oracle.truffle.r.test.packages.analyzer.detectors.RInternalErrorDetector;
import com.oracle.truffle.r.test.packages.analyzer.detectors.SegfaultDetector;
import com.oracle.truffle.r.test.packages.analyzer.detectors.UnsupportedSpecializationDetector;
import com.oracle.truffle.r.test.packages.analyzer.dump.HTMLDumper;
import com.oracle.truffle.r.test.packages.analyzer.model.RPackage;
import com.oracle.truffle.r.test.packages.analyzer.model.RPackageTestRun;
import com.oracle.truffle.r.test.packages.analyzer.parser.LogFileParseException;
import com.oracle.truffle.r.test.packages.analyzer.parser.LogFileParser;
import com.oracle.truffle.r.test.packages.analyzer.parser.LogFileParser.LogFile;

/**
 * Main class of the package analysis tool.<br>
 * <p>
 * Expected directory structure: <code>
 * root<br>
 * -+-- packageName<br>
 * ----+- version<br>
 * ------+- testRunNumber<br>
 * --------+- packageName.log<br>
 * --------+- testfiles<br>
 * ----------+- diff<br>
 * ----------+- fastr<br>
 * ----------+- gnu<br>
 * </code>
 * </p>
 */
public class PTAMain {
    private static final Logger LOGGER = Logger.getLogger(PTAMain.class.getName());
    private static final String LOG_FILE_NAME = "pta.log";

    public static void main(String[] args) throws IOException {
        OptionsParser parser = new OptionsParser();
        parser.registerOption("help");
        parser.registerOption("outDir", "html");
        parser.registerOption("glob", "*");
        parser.registerOption("since", "last2weeks");
        parser.registerOption("console");
        parser.registerOption("verbose");

        String[] remainingArgs = parser.parseOptions(args);
        if (parser.has("help")) {
            printHelpAndExit();
        }

        if (remainingArgs.length != 1) {
            System.err.println("Unknown arguments: " + Arrays.toString(remainingArgs));
            printHelpAndExit();
        }

        configureLogger(parser);

        Date sinceDate = parseSinceDate(parser);
        LOGGER.info("Considering only test runs since: " + sinceDate);

        Path outDir = Paths.get(parser.get("outDir"));
        ftw(Paths.get(remainingArgs[0]), outDir, sinceDate, parser.get("glob"));
    }

    private static final Pattern REL_SINCE_PATTERN = Pattern.compile("last(\\d+)(days?|weeks?|months?)");

    private static Date parseSinceDate(OptionsParser parser) {
        String sinceDateStr = parser.get("since");

        Matcher matcher = REL_SINCE_PATTERN.matcher(sinceDateStr);

        if (matcher.matches()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            GregorianCalendar cal = new GregorianCalendar();
            switch (unit) {
                case "day":
                case "days":
                    cal.add(Calendar.DATE, -amount);
                    break;
                case "week":
                case "weeks":
                    cal.add(Calendar.WEEK_OF_YEAR, -amount);
                    break;
                case "month":
                case "months":
                    cal.add(Calendar.MONTH, -amount);
                    break;
                default:
                    throw new RuntimeException("Invalid unit: " + unit);
            }

            return cal.getTime();
        }

        try {
            return new SimpleDateFormat("MM-dd-yyyy").parse(sinceDateStr);
        } catch (ParseException e) {
            LOGGER.severe("Invalid date: " + e.getMessage());
            System.exit(1);
        }
        // should never be reached
        return null;
    }

    private static void configureLogger(OptionsParser parser) throws IOException {
        LogManager.getLogManager().reset();
        Logger rootLogger = Logger.getLogger("");
        ConsoleHandler consoleHandler = new ConsoleHandler();

        Level defaultLogLevel = Level.INFO;
        if (parser.has("verbose")) {
            defaultLogLevel = Level.ALL;
        }
        rootLogger.setLevel(defaultLogLevel);

        if (parser.has("console")) {
            consoleHandler.setLevel(defaultLogLevel);
        } else {
            // set log level of console handlers to SEVERE
            consoleHandler.setLevel(Level.SEVERE);
            FileHandler fileHandler = new FileHandler(LOG_FILE_NAME);
            fileHandler.setLevel(defaultLogLevel);
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);
        }
        rootLogger.addHandler(consoleHandler);
    }

    private static final String LF = System.lineSeparator();

    private static void ftw(Path root, Path outDir, Date sinceDate, String glob) throws IOException {
        // TODO FS checking

        HTMLDumper htmlDumper = new HTMLDumper(outDir);

        // fail early
        try {
            if (!htmlDumper.createAndCheckOutDir()) {
                LOGGER.severe("Cannot write to output directory: " + outDir);
                System.exit(1);
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("Cannot create output directory: %s ", e.getMessage()));
            System.exit(1);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, glob)) {
            Collection<RPackage> pkgs = new LinkedList<>();
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    Collection<RPackage> pkgVersions = visitPackageRoot(p, sinceDate);
                    pkgs.addAll(pkgVersions);
                }
            }
            htmlDumper.dump(pkgs);
        }
    }

    private static Collection<RPackage> visitPackageRoot(Path pkgRoot, Date sinceDate) throws IOException {
        String pkgName = pkgRoot.getFileName().toString();

        Collection<RPackage> pkgs = new LinkedList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pkgRoot)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    pkgs.add(visitPackageVersion(p, pkgName, sinceDate));
                }
            }
        }
        return pkgs;
    }

    private static RPackage visitPackageVersion(Path pkgVersionDir, String pkgName, Date sinceDate) {
        String pkgVersion = pkgVersionDir.getFileName().toString();
        RPackage pkg = new RPackage(pkgName, pkgVersion);
        LOGGER.info("Found package " + pkg);

        Collection<RPackageTestRun> runs = new LinkedList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pkgVersionDir)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    RPackageTestRun testRun = visitTestRun(p, pkg, sinceDate);
                    if (testRun != null) {
                        runs.add(testRun);
                    }
                }
            }
            pkg.setTestRuns(runs);
        } catch (IOException e) {
            LOGGER.severe("Error while reading package root of \"" + pkgName + "\"");
        }

        return pkg;
    }

    private static RPackageTestRun visitTestRun(Path testRunDir, RPackage pkg, Date sinceDate) {
        int testRun = Integer.parseInt(testRunDir.getFileName().toString());
        LOGGER.info("Visiting test run " + testRun + " of package " + pkg);
        try {
            RPackageTestRun pkgTestRun = new RPackageTestRun(pkg, testRun);
            Path logFile = testRunDir.resolve(pkg.getName() + ".log");
            FileTime lastModifiedTime = Files.getLastModifiedTime(logFile);
            if (isNewerThan(lastModifiedTime, sinceDate)) {
                Collection<Problem> problems = parseLogFile(logFile, pkgTestRun);
                pkgTestRun.setProblems(problems);
                return pkgTestRun;
            } else {
                LOGGER.info(String.format("Skipping package test run %s because it is too old (%s must be newer than %s)", pkgTestRun, lastModifiedTime, sinceDate));
            }
        } catch (IOException | LogFileParseException e) {
            LOGGER.severe(String.format("Error while parsing test run %d of package \"%s-%s\": %s", testRun,
                            pkg.getName(), pkg.getVersion(), e.getMessage()));
        }
        return null;
    }

    private static boolean isNewerThan(FileTime lastModifiedTime, Date sinceDate) {
        Date lastModDate = new Date(lastModifiedTime.toMillis());
        return sinceDate.compareTo(lastModDate) <= 0;
    }

    private static Collection<Problem> parseLogFile(Path logFile, RPackageTestRun pkgTestRun) throws IOException {
        LOGGER.info("Parsing log file " + logFile);

        LogFileParser lfParser = new LogFileParser(logFile, pkgTestRun);
        lfParser.addDetector(LogFileParser.Token.BEGIN_SUGGESTS_INSTALL, InstallationProblemDetector.INSTANCE);
        lfParser.addDetector(SegfaultDetector.INSTANCE);
        lfParser.addDetector(RErrorDetector.INSTANCE);
        lfParser.addDetector(UnsupportedSpecializationDetector.INSTANCE);
        lfParser.addDetector(RInternalErrorDetector.INSTANCE);
        lfParser.addTestResultDetector(DiffDetector.INSTANCE);

        LogFile parseLogFile = lfParser.parseLogFile();
        Collection<Problem> problems = parseLogFile.collectProblems();
        pkgTestRun.setSuccess(parseLogFile.isSuccess());

        // log problems
        LOGGER.fine("Overall test result: " + (pkgTestRun.isSuccess() ? "OK" : "FAILED"));
        for (Problem problem : problems) {
            LOGGER.fine(problem.toString());
        }

        return problems;
    }

    private static void printHelpAndExit() {
        StringBuilder sb = new StringBuilder();
        sb.append("USAGE: ").append(PTAMain.class.getSimpleName()).append(" [OPTIONS] ROOT").append(LF);
        sb.append(LF);
        sb.append("OPTIONS:").append(LF);
        sb.append("    --help\t\tShow this help page").append(LF);
        sb.append("    --since SPEC\tOnly consider package tests satisfying the specified age (default: \"last2weeks\").").append(LF);
        sb.append("    \t\t\tSPEC is either an absolute date in format MM-dd-yyyy or relative in format:").append(LF);
        sb.append("    \t\t\tlast<n>(days|weeks|months).").append(LF);
        sb.append("    --glob GLOB\t\tGlob-style directory filter for packages to consider (default: \"*\").").append(LF);
        sb.append("    --outDir PATH\tPath to directory for HTML output (default: \"html\").").append(LF);
        sb.append("    --console\t\tPrint output to console (by default, only errors are printed).").append(LF);
        System.out.println(sb.toString());
        System.exit(1);
    }

    private static class OptionsParser {

        private Map<String, String> options = new HashMap<>();
        private Map<String, Option> registered = new HashMap<>();

        public String[] parseOptions(String[] args) {
            int i = 0;
            while (i < args.length) {
                String key = args[i];
                if (key.startsWith("--") && registered.containsKey(getBareName(key))) {
                    Option option = registered.get(getBareName(key));
                    String value = null;
                    if (option.hasValue) {
                        value = getOptionArg(args, i);
                        ++i;
                    }
                    options.put(getBareName(key), value);
                } else {
                    break;
                }
                ++i;
            }
            return Arrays.copyOfRange(args, i, args.length);
        }

        private static String getBareName(String key) {
            if (key.startsWith("--")) {
                return key.substring(2);

            }
            throw new RuntimeException("Invalid option name: " + key);
        }

        private static String getOptionArg(String[] args, int keyIndex) {
            if (keyIndex + 1 < args.length) {
                return args[keyIndex + 1];
            }
            throw new RuntimeException("Missing value for option: " + args[keyIndex]);
        }

        public boolean has(String key) {
            return options.containsKey(key);
        }

        public String get(String key) {
            if (has(key)) {
                return options.get(key);
            }
            if (registered.containsKey(key)) {
                return registered.get(key).defaultValue;
            }
            throw new RuntimeException("Unknown option: " + key);
        }

        public void registerOption(String optionName, String defaultValue) {
            registered.put(optionName, new Option(true, defaultValue));
        }

        public void registerOption(String optionName) {
            registered.put(optionName, new Option(false, null));
        }

        private static class Option {
            final boolean hasValue;
            final String defaultValue;

            protected Option(boolean hasValue, String defaultValue) {
                this.hasValue = hasValue;
                this.defaultValue = defaultValue;
            }

        }
    }
}
