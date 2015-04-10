/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.monitoring.reconfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.Settings;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.xml.Log4jEntityResolver;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is a JMX MBean class that expose methods for log4j configuration.
 */
@ManagedResource(objectName = "org.apache.cocoon:group=Reconfiguration,name=Log4JReconfigurator")
public class Log4JReconfigurator {

    private static final String[] EXTENSIONS = new String[] { "xml", "properties" };

    private final Logger logger;
    private final LoggerRepository loggerRepository;
    private final DocumentBuilder docBuilder;

    private Settings settings;

    public Log4JReconfigurator() {
        this.loggerRepository = LogManager.getLoggerRepository();
        this.logger = Logger.getLogger(Log4JReconfigurator.class);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);

        try {
            this.docBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            this.logger.fatal(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        this.docBuilder.setErrorHandler(new DefaultHandler());
        this.docBuilder.setEntityResolver(new Log4jEntityResolver());
    }

    /**
     * Find all configured loggers and returns is as a array of <code>String</code>s
     *
     * @return list of all configured loggers with their level.
     */
    @ManagedAttribute(description = "Return a list of all configured loggers with their level.")
    public final String[] getLoggers() {
        List<String> result = new ArrayList<String>();

        @SuppressWarnings("unchecked")
        Enumeration<Logger> currentLoggers = this.loggerRepository.getCurrentLoggers();
        while (currentLoggers.hasMoreElements()) {
            Logger tmpLogger = currentLoggers.nextElement();
            if (tmpLogger.getLevel() != null) {
                result.add(tmpLogger.getName() + ": " + tmpLogger.getLevel());
            }
        }

        return result.toArray(new String[] {});
    }

    /**
     * Sets logging level for a particular package or a class.
     *
     * @param category name of the log category (usually a package or class name) whose log level
     *            should be changed.
     * @param newLogLevel new log level for that category. Available log levels are:
     *            <code>OFF</code>, <code>INFO</code>, <code>WARN</code>, <code>ERROR</code>,
     *            <code>FATAL</code>, <code>TRACE</code>, <code>DEBUG</code>, <code>ALL</code>
     * @return <code>true</code> if operation was successful, <code>false</code> otherwise.
     */
    @ManagedOperation(description = "Sets logging level for a particular package or a class. Returns "
            + "true if operation was successful.")
    @ManagedOperationParameters(value = {
            @ManagedOperationParameter(name = "category", description = "Name of the log category (usually "
                    + "a package or class name) whose log level should be changed."),
            @ManagedOperationParameter(name = "newLevel", description = "New log level for that category. "
                    + "Available log levels are: OFF, INFO, WARN, ERROR, FATAL, TRACE, DEBUG, ALL") })
    public final boolean setLoggingLevel(final String category, final String newLogLevel) {
        boolean result = false;

        Logger logger = this.loggerRepository.getLogger(category);
        if (logger != null) {
            logger.setLevel(Level.toLevel(newLogLevel.toUpperCase()));
            result = true;
        }

        return result;
    }

    /**
     * Sets new logging level for amount of time. After timeout log level is set back to old value.
     *
     * @param category name of the log category (usually a package or class name) whose log level
     *            should be changed.
     * @param temporalLogLevel temporal log level for that category that should be set for specified
     *            amount of time.
     * @param timeOut amount of time that temporalLevel should be active. Value of timeOut should
     *            match regular expression: ^[0-9.]+[dhm]?$ where <code>d</code> means day,
     *            <code>h</code> hours and <code>m</code> minutes
     * @return <code>true</code> if operation was successful, <code>false</code> otherwise.
     */
    @ManagedOperation(description = "Sets new logging level for amount of time. After timeout log level is set"
            + " back to old value.")
    @ManagedOperationParameters(value = {
            @ManagedOperationParameter(name = "category", description = "Name of the log category (usually"
                    + " a package or class name) whose log level should be changed."),
            @ManagedOperationParameter(name = "temporalLevel", description = "Temporal log level for that "
                    + "category that should be set for specified amount of time."),
            @ManagedOperationParameter(name = "timeOut", description = "Amount of time that temporalLevel "
                    + "should be active. Value of timeOut should match regular expression: ^[0-9.]+[dhm]?$ "
                    + "where 'd' means day, 'h' hours and 'm' minutes") })
    public final boolean setLoggingTempoporalLevel(final String category, final String temporalLogLevel,
            final String timeOut) {
        if (!timeOut.matches("^[0-9.]+[dhm]?$")) {
            throw new UnsupportedOperationException("Unsupported time-out format: " + timeOut);
        }

        boolean result = false;
        Logger logger = this.loggerRepository.getLogger(category);
        if (logger != null) {
            Level oldLevel = logger.getLevel();
            LoggingConfigurationResetter restoreThread = new LoggingConfigurationResetter(logger, oldLevel, timeOut
                    .toLowerCase());
            logger.setLevel(Level.toLevel(temporalLogLevel));
            restoreThread.start();
            result = true;
        }

        return result;
    }

    /**
     * Allows to change configuration of log4j on the fly. This function support both XML and
     * properties configuration files. Before reloading the configuration it checks that the new
     * config file contains at least one appender and all output files are accessible (for XML
     * configs it also validate XML syntax using schema or DTD)
     *
     * @param path absolute path to configuration file located on the server.
     * @return <code>true</code> if operation was successful, <code>false</code> otherwise.
     * @throws Exception if something unusual happens
     */
    @ManagedOperation(description = "Allows to change configuration of log4j on the fly. This function support "
            + "both XML and properties configuration files. Before reloading configuration it checks that the new "
            + "config file contains at least one appender and all output files are accessible (for XML configs it"
            + "also validate XML syntax using schema or DTD)")
    @ManagedOperationParameters(value = { @ManagedOperationParameter(name = "configFilePath", description = "Absolute path to configuration file.") })
    public final boolean loadNewConfigurationFile(String path) throws Exception {
        path = path.trim();

        // check extension
        if (FilenameUtils.isExtension(path, EXTENSIONS)) {
            File newConfig = new File(path);

            if (!newConfig.exists()) {
                this.logger.fatal("Cannot find file: " + path);
                throw new FileNotFoundException("Cannot find file: " + path);
            } else if (!newConfig.canRead()) {
                this.logAndThrowIOException("Cannot read file: " + path);
            }

            if (FilenameUtils.isExtension(path, "xml")) {
                this.loadNewXMLConfigurationFile(path);
            } else {
                this.loadNewPropertiesConfigurationFile(path);
            }
        } else {
            String message = "Unsupported file format: " + FilenameUtils.getExtension(path);
            this.logger.fatal(message);
            throw new ConfigurationException(message);
        }

        return true;
    }

    /**
     * Inject the settings object.
     *
     * @param s The settings bean.
     */
    public final void setSettings(final Settings s) {
        this.settings = s;
    }

    /**
     * Loads XML config file specified by <code>path</code> parameter. Then this file is validate
     * against DTD or schema, after that every appender is checked that it output file is
     * accessible.
     *
     * @param path absolute path to XML configuration file.
     * @throws Exception if something unusual happens
     */
    private void loadNewXMLConfigurationFile(final String path) throws Exception {
        Document doc;
        try { // validate XML config file
            doc = this.docBuilder.parse(path);
        } catch (Exception e) {
            this.logger.fatal(e.getMessage(), e);
            throw new IOException("Config file parse exception: " + e.getMessage());
        }

        // check access for all log files
        NodeList appenderList = doc.getElementsByTagName("appender");
        for (int i = 0; i < appenderList.getLength(); i++) {
            NodeList childNodes = appenderList.item(i).getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node item = childNodes.item(j);
                this.extractLogFilespathAndValidate(item);
            }
        }

        // apply new settings
        DOMConfigurator.configure(path);
    }

    /**
     * Extracts path to log file from XML configuration node and checks that it exist and is
     * writable.
     *
     * @param item
     * @throws IOException if directory doesn't exist or isn't writable
     */
    private void extractLogFilespathAndValidate(final Node item) throws IOException {
        if (!item.getNodeName().equalsIgnoreCase("param")) {
            return;
        }

        NamedNodeMap itemAttributes = item.getAttributes();
        if (itemAttributes == null) {
            return;
        }

        Node paramName = itemAttributes.getNamedItem("name");
        if (paramName != null && paramName.getNodeValue().equalsIgnoreCase("file")) {
            Node paramValue = itemAttributes.getNamedItem("value");
            if (paramValue != null) {
                String logpath = paramValue.getNodeValue();
                this.validateLogFile(logpath);
            }
        }
    }

    /**
     * Loads properties config file specified by <code>path</code> parameter. Then it validate that
     * file contains configuration for at least one appender and validate all configured log files
     * that they path exist and it is writable.
     *
     * @param path absolute path to properties configuration file.
     * @throws Exception if something unusual happens
     */
    private void loadNewPropertiesConfigurationFile(final String path) throws ConfigurationException, IOException {
        boolean result = false;
        File file = new File(path);

        // search for appender configuration
        Scanner fileScanner = new Scanner(file);
        while (fileScanner.hasNext()) {
            String line = fileScanner.nextLine();
            if (!result && line.toLowerCase().matches("^log4j\\.appender\\.[\\w\\.]+=[\\w\\.]+$")) {
                PropertyConfigurator.configure(file.getPath());
                result = true; // we got our "at least one appender" ;)
            }

            if (line.toLowerCase().matches("^log4j\\.appender\\.[\\w]+\\.file=[\\w\\.\\{\\}\\$/\\:]+$")) {
                String[] logFile = line.split("=");
                this.validateLogFile(logFile[1]);
            }
        }

        if (!result) {
            throw new ConfigurationException(
                    "No configured appenders, there should be at least one appender confgured.");
        }
    }

    /**
     * Helper method that logs exception and throws IOException.
     *
     * @param message that should be logged and passed to exception
     * @throws IOException always throws this exception
     */
    private void logAndThrowIOException(final String message) throws IOException {
        this.logger.fatal(message);
        throw new IOException(message);
    }

    /**
     * Validate log file path that it exists and is writable.
     *
     * @param logPath path to log file
     * @throws IOException if log file directory doesn't exist or is read only
     */
    private void validateLogFile(String logPath) throws IOException {
        String logDirpath = FilenameUtils.getFullPath(PropertyHelper.replace(logPath, this.settings));

        if (logDirpath.length() == 0) { // if logDirpath is empty
            logDirpath = "."; // current directory is log directory
        }

        if (!new File(logDirpath).exists()) {
            this.logAndThrowIOException("Log directory: " + logDirpath + " does not exist.");
        } else if (!new File(logPath).canWrite()) {
            this.logAndThrowIOException("Log file: " + logPath + " is read only.");
        }
    }
}
