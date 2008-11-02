/*
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2007-2008 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j;

import org.olap4j.driver.xmla.XmlaOlap4jDriver;
import org.olap4j.driver.xmla.proxy.XmlaOlap4jProxy;
import org.olap4j.test.TestContext;

import java.sql.*;
import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Implementation of {@link org.olap4j.test.TestContext.Tester} which speaks
 * to the olap4j driver for XML/A.
 *
 * @author jhyde
 * @version $Id$
 */
public class XmlaTester implements TestContext.Tester {
    final XmlaOlap4jProxy proxy;
    final String cookie;
    private Connection connection;

    /**
     * Creates an XmlaTester
     *
     * @throws ClassNotFoundException on error
     * @throws IllegalAccessException on error
     * @throws InstantiationException on error
     * @throws NoSuchMethodException on error
     * @throws InvocationTargetException on error
     */
    public XmlaTester()
        throws ClassNotFoundException, IllegalAccessException,
        InstantiationException, NoSuchMethodException,
        InvocationTargetException
    {
        final Properties properties = TestContext.getTestProperties();
        final String catalogUrl =
            properties.getProperty(
                TestContext.Property.XMLA_CATALOG_URL.path);
        Map<String, String> catalogNameUrls =
            new HashMap<String, String>();
        catalogNameUrls.put("FoodMart", catalogUrl);
        String urlString =
            properties.getProperty(TestContext.Property.CONNECT_URL.path);

        final Class<?> clazz = Class.forName("mondrian.olap4j.MondrianInprocProxy");
        final Constructor<?> constructor =
            clazz.getConstructor(Map.class, String.class);
        this.proxy =
            (XmlaOlap4jProxy) constructor.newInstance(
                catalogNameUrls, urlString);
        this.cookie = XmlaOlap4jDriver.nextCookie();
        XmlaOlap4jDriver.PROXY_MAP.put(cookie, proxy);
    }

    public Connection createConnection() throws SQLException {
        if (connection != null) {
            return connection;
        }
        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("oops", e);
        }
        Properties info = new Properties();
        info.setProperty(
            XmlaOlap4jDriver.Property.Catalog.name(), "FoodMart");
        connection =
            DriverManager.getConnection(
                getURL(),
                info);
        return connection;
    }

    public Connection createConnectionWithUserPassword() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("oops", e);
        }
        Properties info = new Properties();
        info.setProperty(
            XmlaOlap4jDriver.Property.Catalog.name(), "FoodMart");
        return DriverManager.getConnection(
            getURL(), USER, PASSWORD);
    }

    public String getDriverUrlPrefix() {
        return DRIVER_URL_PREFIX;
    }

    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

    public String getURL() {
        return "jdbc:xmla:Server=http://foo;Catalog=FoodMart;TestProxyCookie=" + cookie;
    }

    public Flavor getFlavor() {
        return Flavor.XMLA;
    }

    public TestContext.Wrapper getWrapper() {
        return TestContext.Wrapper.NONE;
    }

    public static final String DRIVER_CLASS_NAME =
         "org.olap4j.driver.xmla.XmlaOlap4jDriver";

    public static final String DRIVER_URL_PREFIX = "jdbc:xmla:";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
}

// End XmlaTester.java
