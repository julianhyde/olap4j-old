/*
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2007-2008 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j.mdx;

import junit.framework.TestCase;

import java.util.*;

/**
 * Testcase for org.olap4j.mdx package.
 *
 * @author jhyde
 * @version $Id$
 * @since Dec 12, 2007
 */
public class MdxTest extends TestCase {
    /**
     * Tests methods {@link IdentifierNode#quoteMdxIdentifier(String)},
     * {@link IdentifierNode#unparseIdentifierList(java.util.List)}.
     */
    public void testQuoteMdxIdentifier() {
        assertEquals(
            "[San Francisco]",
            IdentifierNode.quoteMdxIdentifier("San Francisco"));

        assertEquals(
            "[a [bracketed]] string]",
            IdentifierNode.quoteMdxIdentifier("a [bracketed] string"));

        assertEquals(
            "[Store].[USA].[California]",
            IdentifierNode.unparseIdentifierList(
                Arrays.asList(
                    new IdentifierNode.NameSegment(
                        null, "Store", IdentifierNode.Quoting.QUOTED),
                    new IdentifierNode.NameSegment(
                        null, "USA", IdentifierNode.Quoting.QUOTED),
                    new IdentifierNode.NameSegment(
                        null, "California", IdentifierNode.Quoting.QUOTED))));
    }

    public void testImplode() {
        List<IdentifierNode.Segment> fooBar =
            Arrays.<IdentifierNode.Segment>asList(
                new IdentifierNode.NameSegment(
                    null, "foo", IdentifierNode.Quoting.UNQUOTED),
                new IdentifierNode.NameSegment(
                    null, "bar", IdentifierNode.Quoting.QUOTED));
        assertEquals(
            "foo.[bar]",
            IdentifierNode.unparseIdentifierList(fooBar));

        List<IdentifierNode.Segment> empty = Collections.emptyList();
        assertEquals("", IdentifierNode.unparseIdentifierList(empty));

        List<IdentifierNode.Segment> nasty =
            Arrays.<IdentifierNode.Segment>asList(
                new IdentifierNode.NameSegment(
                    null, "string", IdentifierNode.Quoting.QUOTED),
                new IdentifierNode.NameSegment(
                    null, "with", IdentifierNode.Quoting.QUOTED),
                new IdentifierNode.NameSegment(
                    null, "a [bracket] in it", IdentifierNode.Quoting.QUOTED));
        assertEquals(
            "[string].[with].[a [bracket]] in it]",
            IdentifierNode.unparseIdentifierList(nasty));
    }

    public void testParseIdentifier() {
        List<IdentifierNode.Segment> segments =
            IdentifierNode.parseIdentifier(
                "[string].[with].[a [bracket]] in it]");
        assertEquals(3, segments.size());
        assertEquals(
            "a [bracket] in it",
            segments.get(2).getName());

        segments = IdentifierNode.parseIdentifier(
            "[Worklog].[All].[calendar-[LANGUAGE]].js]");
        assertEquals(3, segments.size());
        assertEquals(
            "calendar-[LANGUAGE].js",
            segments.get(2).getName());

        try {
            segments = IdentifierNode.parseIdentifier("[foo].bar");
            fail("expected exception, got " + segments);
        } catch (IllegalArgumentException e) {
            assertEquals(
                "Invalid member identifier '[foo].bar'",
                e.getMessage());
        }

        try {
            segments = IdentifierNode.parseIdentifier("[foo].[bar");
            fail("expected exception, got " + segments);
        } catch (IllegalArgumentException e) {
            assertEquals(
                "Invalid member identifier '[foo].[bar'",
                e.getMessage());
        }
    }
}

// End MdxTest.java
