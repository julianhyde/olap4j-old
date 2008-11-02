/*
// $Id$
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2006-2008 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j;

import org.olap4j.metadata.Cube;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * An object that represents a precompiled OLAP statement.
 *
 * <p>An OLAP statement is precompiled and stored in a
 * <code>PreparedOlapStatement</code> object. This object can then be used to
 * efficiently execute this statement multiple times.</p>
 *
 * <p>A <code>PreparedOlapStatement</code> is generally created using
 * {@link OlapConnection#prepareOlapStatement(String)}.</p>
 *
 * <p><B>Note:</B> The setter methods (<code>setShort</code>,
 * <code>setString</code>, and so on) for setting IN parameter values
 * must specify types that are compatible with the defined type of
 * the input parameter. For instance, if the IN parameter has type
 * <code>INTEGER</code>, then the method <code>setInt</code> should be used.</p>
 *
 * <p>If a parameter has Member type, use the {@link #setObject(int, Object)}
 * method to set it. A {@link OlapException} will be thrown if the object is not
 * an instance of {@link org.olap4j.metadata.Member} or does not belong to the
 * correct {@link org.olap4j.metadata.Hierarchy}.</p>
 *
 * <p>The method {@link #getParameterMetaData()} returns a description of the
 * parameters, as in JDBC. The result is an {@link OlapParameterMetaData}.
 *
 * <p>Unlike JDBC, it is not necessary to assign a value to every parameter.
 * This is because OLAP parameters have a default value. Parameters have their
 * default value until they are set, and then retain their new values for each
 * subsequent execution of this <code>PreparedOlapStatement</code>.
 *
 * @see OlapConnection#prepareOlapStatement(String)
 * @see CellSet
*
 * @author jhyde
 * @version $Id$
 * @since Aug 22, 2006
 */
public interface PreparedOlapStatement
    extends PreparedStatement, OlapStatement
{
    /**
     * Executes the MDX query in this <code>PreparedOlapStatement</code> object
     * and returns the <code>CellSet</code> object generated by the query.
     *
     * @return an <code>CellSet</code> object that contains the data produced
     *         by the query; never <code>null</code>
     * @exception OlapException if a database access error occurs
     */
    CellSet executeQuery()  throws OlapException;

    /**
     * Retrieves the number, types and properties of this
     * <code>PreparedOlapStatement</code> object's parameters.
     *
     * @return an <code>OlapParameterMetaData</code> object that contains
     *         information about the number, types and properties of this
     *         <code>PreparedOlapStatement</code> object's parameters
     * @exception OlapException if a database access error occurs
     * @see OlapParameterMetaData
     */
    OlapParameterMetaData getParameterMetaData() throws OlapException;

    /**
     * Retrieves a <code>CellSetMetaData</code> object that contains
     * information about the axes and cells of the <code>CellSet</code> object
     * that will be returned when this <code>PreparedOlapStatement</code> object
     * is executed.
     *
     * @return the description of this <code>CellSet</code>'s axes
     * and cells
     * @exception OlapException if a database access error occurs
     */
    CellSetMetaData getMetaData() throws SQLException;

    /**
     * Returns the cube (or virtual cube) which this statement is based upon.
     *
     * @return cube this statement is based upon
     */
    Cube getCube();

}

// End PreparedOlapStatement.java
