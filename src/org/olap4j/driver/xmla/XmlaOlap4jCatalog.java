/*
// $Id$
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2007-2010 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j.driver.xmla;

import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;
import org.olap4j.impl.Named;
import org.olap4j.impl.Olap4jUtil;
import org.olap4j.metadata.*;

/**
 * Implementation of {@link org.olap4j.metadata.Catalog}
 * for XML/A providers.
 *
 * @author jhyde
 * @version $Id$
 * @since May 23, 2007
 */
class XmlaOlap4jCatalog implements Catalog, Named {
    final XmlaOlap4jDatabaseMetaData olap4jDatabaseMetaData;
    private final String name;
    final DeferredNamedListImpl<XmlaOlap4jSchema> schemas;
    private final XmlaOlap4jDatabase database;

    XmlaOlap4jCatalog(
        XmlaOlap4jDatabaseMetaData olap4jDatabaseMetaData,
        XmlaOlap4jDatabase database,
        String name)
    {
        this.database = database;
        assert olap4jDatabaseMetaData != null;
        assert name != null;
        this.olap4jDatabaseMetaData = olap4jDatabaseMetaData;
        this.name = name;

        // Some servers don't support MDSCHEMA_MDSCHEMATA, so we will
        // override the list class so it tries it first, and falls
        // back to the MDSCHEMA_CUBES trick, where ask for the cubes,
        // restricting results on the catalog, and while
        // iterating on the cubes, take the schema name from this recordset.
        //
        // Many servers (SSAS for example) won't support the schema name column
        // in the returned rowset. This has to be taken into account as well.
        this.schemas =
            new DeferredNamedListImpl<XmlaOlap4jSchema>(
                XmlaOlap4jConnection.MetadataRequest.DBSCHEMA_SCHEMATA,
                new XmlaOlap4jConnection.Context(
                    olap4jDatabaseMetaData.olap4jConnection,
                    olap4jDatabaseMetaData,
                    this,
                    null, null, null, null, null),
                new XmlaOlap4jConnection.CatalogSchemaHandler(this.name),
                null)
            {
                @Override
                protected void populateList(NamedList<XmlaOlap4jSchema> list)
                        throws OlapException
                {
                    // First try DBSCHEMA_SCHEMATA
                    try {
                        super.populateList(list);
                    } catch (OlapException e) {
                        // Fallback to MDSCHEMA_CUBES trick
                        XmlaOlap4jConnection conn =
                            XmlaOlap4jCatalog.this
                            .olap4jDatabaseMetaData.olap4jConnection;
                        conn.populateList(
                            list,
                            new XmlaOlap4jConnection.Context(
                                conn,
                                conn.olap4jDatabaseMetaData,
                                XmlaOlap4jCatalog.this,
                                null, null, null, null, null),
                                XmlaOlap4jConnection.MetadataRequest
                                    .MDSCHEMA_CUBES,
                                new XmlaOlap4jConnection.CatalogSchemaHandler(
                                    XmlaOlap4jCatalog.this.name),
                                new Object[0]);
                    }
                }
            };
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof XmlaOlap4jCatalog) {
            XmlaOlap4jCatalog that = (XmlaOlap4jCatalog) obj;
            return this.name.equals(that.name);
        }
        return false;
    }

    public NamedList<Schema> getSchemas() throws OlapException {
        return Olap4jUtil.cast(schemas);
    }

    public String getName() {
        return name;
    }

    public OlapDatabaseMetaData getMetaData() {
        return olap4jDatabaseMetaData;
    }

    public XmlaOlap4jDatabase getDatabase() {
        return database;
    }
}

// End XmlaOlap4jCatalog.java
