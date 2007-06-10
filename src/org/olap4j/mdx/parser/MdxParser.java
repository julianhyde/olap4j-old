/*
// $Id$
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2006-2006 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j.mdx.parser;

import org.olap4j.mdx.SelectNode;
import org.olap4j.mdx.ParseTreeNode;

/**
 * Parser for the MDX query language.
 *
 * <p>A parser is reusable but not reentrant: you can call {@link #parseSelect}
 * and {@link #parseExpression} several times, but not at the same time
 * from different threads.
 *
 * @see MdxParserFactory
 * @see org.olap4j.mdx.parser.impl.DefaultMdxParserImpl
 *
 * @author jhyde
 * @version $Id$
 * @since Aug 22, 2006
 */
public interface MdxParser {
    /**
     * Parses an MDX Select statement and returns the {@link SelectNode} at the
     * root of the parse tree.
     *
     * <p>In order to be parsed successfully, the expression must be
     * syntactically correct but does not need to be valid. (Syntactic
     * correctness and validity are described further in the description of
     * {@link #parseExpression(String)}.)
     *
     * @param mdx MDX query string
     * @return Parse tree
     */
    SelectNode parseSelect(String mdx);

    /**
     * Parses an MDX expression and returns a parse tree.
     *
     * <p>An expression is a combination of operators and operands, which can
     * occur in many places inside an MDX query, such as the definition of a
     * calculated member or an axis.
     *
     * <p>In order to be parsed successfully, the expression must be
     * syntactically correct but does not need to be valid.
     * For example,
     *
     * <blockquote><code>(1 + (2 + 3)</code></blockquote>
     *
     * is syntactically incorrect,
     * because there are more open parentheses "(" than close parentheses ")",
     * and the parser will give an error. Conversely,
     *
     * <blockquote><code>(1 + [Measures].[Bad Measure])</code></blockquote>
     *
     * is syntactically correct, and the parser
     * will successfully create a parse tree, even if
     * <code>[Measures].[Bad Measure]</code> does not exist.
     *
     * @param mdx MDX expression
     * @return Parse tree
     */
    ParseTreeNode parseExpression(String mdx);
}

// End MdxParser.java
