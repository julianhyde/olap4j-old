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

import org.olap4j.metadata.Property;

import java.util.List;
import java.sql.ResultSet;

/**
 * Cell returned from a {@link OlapResultSet}.
 *
 * @author jhyde
 * @version $Id$
 * @since Aug 22, 2006
 */
public interface ResultCell {
    /**
     * Returns the {@link OlapResultSet} that this ResultCell belongs to.
     *
     * @return OlapResultSet, never null
     */
    OlapResultSet getResultSet();

    /**
     * Returns the ordinal of this ResultCell.
     *
     * <p>The formula is the sequence, zero-based, which the cell would be
     * visited in a raster-scan through all of the cells of this
     * {@link OlapResultSet}. The ordinal of the first cell is zero, and the
     * ordinal of the last cell is the product of the lengths of the axes, minus
     * 1. For example, if a result has 10 columns and 20
     * rows, then:<ul>
     * <li>(row 0, column 0) has ordinal 0,</li>
     * <li>(row 0, column 1) has ordinal 1,</li>
     * <li>(row 1, column 0) has ordinal 10,</li>
     * <li>(row 19, column 9) has ordinal 199.</li>
     * </ul>
     */
    int getOrdinal();

    /**
     * Returns the coordinates of this ResultCell in its {@link ResultAxis}.
     *
     * <p>This method is provided for convenience. It is equivalent to the
     * following code:
     * <blockquote>
     * <code>
     *    getResult().ordinalToCoordinateList(getOrdinal())
     * </code>
     * </blockquote>
     */
    List<Integer> getCoordinateList();

    /**
     * Returns the value of a given property for this ResultCell.
     *
     * @see org.olap4j.OlapResultSet#getMetaData()
     * @see Todo
     */
    Object getPropertyValue(Property property);

    /**
     * Returns whether this cell is empty.
     *
     * @return Whether this cell is empty.
     */
    boolean isEmpty();

    /**
     * Returns whether an error occurred while evaluating this cell.
     *
     * @return Whether an error occurred while evaluating this cell.
     */
    boolean isError();

    /**
     * Returns whether the value of this cell is NULL.
     *
     * @return Whether the value of this cell is NULL.
     */
    boolean isNull();

    /**
     * Returns the value of this cell as a <code>double</code> value.
     */
    double getDoubleValue() throws OlapException;

    /**
     * @see Todo
     */
    String getErrorText();

    /**
     * @see Todo
     * returns a OlapException if the cell is an error
     */
    Object getValue();

    /**
     * Returns the value of this ResultCell, formatted according to the
     * FORMAT_STRING property and using the numeric formatting tokens the
     * current locale.
     *
     * @return Formatted value of this ResultCell
     */
    String getFormattedValue();

    /**
     * Drills through from this cell to the underlying fact table data,
     * and returns a {@link java.sql.ResultSet} of the results.
     */
    ResultSet drillThrough();
}

// End ResultCell.java
