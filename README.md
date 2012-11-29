olap4j
======

Olap4j is an open Java API for accessing OLAP data.

Prerequisites
=============

Olap4j requires ant (version 1.7 or later) and JDK 1.7 to build. (Once built, it also runs under JDK 1.5 and 1.6.)

Download and build
==================

```bash
$ git clone git://github.com/julianhyde/olap4j.git
$ cd olap4j
$ ant
```

Writing a simple program
========================

You can now write and run a simple program against olap4j. For example, under
Java 1.6 or later,

```java
import org.olap4j.*;
import org.olap4j.metadata.Member;
import java.sql.*;

Class.forName("org.olap4j.driver.xmla.XmlaOlap4jDriver");
Connection connection =
    DriverManager.getConnection(
        "jdbc:xmla:Server=http://example.com:8080/mondrian/xmla");
OlapConnection olapConnection = connection.unwrap(OlapConnection.class);
OlapStatement statement = olapConnection.createStatement();
CellSet cellSet =
    statement.executeOlapQuery(
        "SELECT {[Measures].[Unit Sales]} ON 0,\n"
        + "{[Product].Children} ON 1\n"
        + "FROM [Sales]");
for (Position row : cellSet.getAxes().get(1)) {
    for (Position column : cellSet.getAxes().get(0)) {
        for (Member member : row.getMembers()) {
            System.out.println(member.getUniqueName());
        }
        for (Member member : column.getMembers()) {
            System.out.println(member.getUniqueName());
        }
        final Cell cell = cellSet.getCell(column, row);
        System.out.println(cell.getFormattedValue());
        System.out.println();
    }
}
```

Or, if you are using the in-process mondrian driver, include mondrian.jar
and its dependencies in your classpath, and change the
appropriate lines in the above code to the following:

```java
Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
Connection connection =
    DriverManager.getConnection(
        "jdbc:mondrian:"
        + "Jdbc='jdbc:odbc:MondrianFoodMart';"
        + "Catalog='file://c:/open/mondrian/demo/FoodMart.xml';"
        + "JdbcDrivers=sun.jdbc.odbc.JdbcOdbcDriver;");
```

Packages and Roadmap
====================

The core API of olap4j version 1.0 is a Long Term Support (LTS) release,
but some parts of the olap4j project will remain considered as experimental,
thus subject to change in future releases. 

Core packages
=============

The core packages in olap4j-1.0 are as follows.

* org.olap4j.driver.xmla - Generic XML/A driver.
* org.olap4j.mdx - Core objects of the MDX model.
* org.olap4j.mdx.parser - Parser for the MDX query language.
* org.olap4j.metadata - Discovery of an OLAP server's metadata.
* org.olap4j.type - System for the core MDX object model and the metadata package.

Experimental packages
=====================

The following packages are considered experimental and are subject to change.

* org.olap4j.query - Programmatic Query Model.
* org.olap4j.transform - Core MDX object model transformation utilities.
* org.olap4j.layout - Utility classes to display CellSets.
* org.olap4j.CellSetListener and all associated classes - Event-based system for real time updates of CellSet objects.
* org.olap4j.Scenario and all associated classes - Statistical simulations module.

More information
================

If you have downloaded a release:
* <a href="README.txt">README.txt</a> describes the release structure.
* The VERSION.txt file holds the version number of the release.
* <a href="CHANGES.txt">CHANGES.txt</a> describes what has changed in the release.

General project information:
* License: Apache License, Version 2.0.
* Lead developers: <a href="https://github.com/julianhyde">Julian Hyde</a>, <a href="https://github.com/lucboudreau">Luc Boudreau</a>.
* Project page: <a href="http://www.olap4j.org">olap4j.org</a>.
* Javadoc: http://www.olap4j.org/api
* Source code: http://github.com/julianhyde/olap4j
* Developers list: https://lists.sourceforge.net/lists/listinfo/olap4j-devel
* Forum: http://sourceforge.net/p/olap4j/discussion/577988/

# End README.md