/*
// $Id$
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2006-2006 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j;

import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Property;

import java.util.List;

/**
 * Description of structure of a particular axis of an {@link CellSet}.
 *
 * <p>For example, in the MDX statement</p>
 *
 * <blockquote>
 * <pre>
 * SELECT
 *   {[Measures].Members} ON COLUMNS,
 *   CrossJoin([Store].Members, [Gender].Children)
 *   DIMENSION PROPERTIES
 *      MEMBER_ORDINAL,
 *      MEMBER_UNIQUE_NAME,
 *      DISPLAY_INFO ON ROWS
 * FROM [Sales]
 * </pre>
 * </blockquote>
 *
 * <p>the ROWS axis is described by the following metadata:</p>
 *
 * <table border="1">
 * <tr>
 * <th>Attribute</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>hierarchies</td>
 * <td>{[Store], [Gender]}</td>
 * </tr>
 * <tr>
 * <td>properties</td>
 * <td>{MEMBER_ORDINAL, MEMBER_UNIQUE_NAME, DISPLAY_INFO}</td>
 * </tr>
 * </table>
 *
 * @author jhyde
 * @version $Id$
 * @since Oct 23, 2006
 */
public interface CellSetAxisMetaData {
    /**
     * Returns the definition of the axis.
     * ({@link Axis#SLICER}, {@link Axis#ROWS}, and so forth.)
     */
    Axis getAxis();

    /**
     * Returns the hierarchies which are mapped onto this axis.
     */
    List<Hierarchy> getHierarchies();

    /**
     * Returns the member properties which are returned on this axis.
     */
    List<Property> getProperties();
}

// End CellSetAxisMetaData.java
