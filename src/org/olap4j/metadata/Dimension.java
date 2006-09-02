/*
// $Id$
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2006-2006 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j.metadata;

/**
 * <code>Dimension</code> ...
 *
 * @author jhyde
 * @version $Id$
 * @since Aug 22, 2006
 */
public interface Dimension {
    /**
     * Returns the name of this Dimension.
     */
    String getName();

    /**
     * Returns the hierarchies in this Dimension.
     *
     * <p>Many dimensions have only one Hierarchy, whose name is the same as the
     * Dimension.
     */
    NamedList<Hierarchy> getHierarchies();

    /**
     * Returns the root member or members of this Dimension.
     *
     * <p>If the dimension has an 'all' member, then this will be the sole
     * root member.
     */
    NamedList<Member> getRootMembers();

    /**
     * Returns the type of this Dimension.
     */
    Dimension.Type getDimensionType();

    /**
     * Enumeration of the types of a <code>Dimension</code>.
     *
     * @see Level.Type
     * @see Member.Type
     * @see Dimension#getDimensionType
     */
    public enum Type {
        /**
         * Indicates that the dimension is not related to time.
         */
        Standard,

        /**
         * Indicates that a dimension is a time dimension.
         */
        Time,
    }
}

// End Dimension.java
