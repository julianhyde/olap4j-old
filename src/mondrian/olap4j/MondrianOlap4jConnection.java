/*
// $Id$
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2007-2007 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap4j;

import mondrian.olap.*;

import java.sql.*;
import java.util.*;

import org.olap4j.*;
import org.olap4j.Cell;
import org.olap4j.Axis;
import org.olap4j.type.BooleanType;
import org.olap4j.type.CubeType;
import org.olap4j.type.DecimalType;
import org.olap4j.type.DimensionType;
import org.olap4j.type.SymbolType;
import org.olap4j.type.TupleType;
import org.olap4j.type.Type;
import org.olap4j.type.StringType;
import org.olap4j.type.SetType;
import org.olap4j.type.NumericType;
import org.olap4j.type.NullType;
import org.olap4j.type.MemberType;
import org.olap4j.metadata.*;
import org.olap4j.metadata.Schema;
import org.olap4j.mdx.parser.MdxParserFactory;
import org.olap4j.mdx.parser.MdxParser;
import org.olap4j.mdx.parser.impl.DefaultMdxParserImpl;

/**
 * Implementation of {@link org.olap4j.OlapConnection}
 * for the Mondrian OLAP engine.
 *
 * @author jhyde
 * @version $Id$
 * @since May 23, 2007
 */
abstract class MondrianOlap4jConnection implements OlapConnection {
    /**
     * Handler for errors.
     */
    final Helper helper = new Helper();

    /**
     * Underlying mondrian connection. Set on creation, cleared on close.
     */
    mondrian.olap.Connection connection;

    /**
     * Current schema.
     */
    MondrianOlap4jSchema olap4jSchema;

    /**
     * Map from mondrian schema objects to olap4j schemas.
     */
    final Map<mondrian.olap.Schema,MondrianOlap4jSchema> schemaMap =
        new HashMap<mondrian.olap.Schema, MondrianOlap4jSchema>();

    private final MondrianOlap4jDatabaseMetaData olap4jDatabaseMetaData;

    /**
     * The name of the sole catalog.
     */
    static final String LOCALDB_CATALOG_NAME = "LOCALDB";
    private static final String CONNECT_STRING_PREFIX = "jdbc:mondrian:";

    final Factory factory;

    /**
     * Creates an Olap4j connection to Mondrian.
     *
     * <p>This method is intentionally package-protected. The public API
     * uses the traditional JDBC {@link java.sql.DriverManager}.
     * See {@link mondrian.olap4j.MondrianOlap4jDriver} for more details.
     *
     * @pre acceptsURL(url)
     *
     * @param url Connect-string URL
     * @param info Additional properties
     * @throws SQLException if there is an error
     */
    MondrianOlap4jConnection(
        Factory factory,
        String url,
        Properties info)
        throws SQLException
    {
        this.factory = factory;
        if (!acceptsURL(url)) {
            // This is not a URL we can handle.
            // DriverManager should not have invoked us.
            throw new AssertionError(
                "does not start with '" + CONNECT_STRING_PREFIX + "'");
        }
        String x = url.substring(CONNECT_STRING_PREFIX.length());
        Util.PropertyList list = Util.parseConnectString(x);
        for (Map.Entry<String,String> entry : toMap(info).entrySet()) {
            list.put(entry.getKey(), entry.getValue());
        }
        this.connection =
            mondrian.olap.DriverManager.getConnection(list, null);
        this.olap4jDatabaseMetaData =
            factory.newDatabaseMetaData(this);
        this.olap4jSchema = toOlap4j(connection.getSchema());
    }

    static boolean acceptsURL(String url) {
        return url.startsWith(CONNECT_STRING_PREFIX);
    }

    public OlapStatement createStatement() {
        return new MondrianOlap4jStatement(this);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String nativeSQL(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean getAutoCommit() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void commit() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void rollback() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void close() throws SQLException {
        if (connection != null) {
            mondrian.olap.Connection c = connection;
            connection = null;
            c.close();
        }
    }

    public boolean isClosed() throws SQLException {
        return connection == null;
    }

    public OlapDatabaseMetaData getMetaData() {
        return olap4jDatabaseMetaData;
    }

    public NamedList<Catalog> getCatalogs() {
        return olap4jDatabaseMetaData.getCatalogObjects();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isReadOnly() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setCatalog(String catalog) throws SQLException {
        if (!catalog.equals(LOCALDB_CATALOG_NAME)) {
            throw new UnsupportedOperationException();
        }
    }

    public String getCatalog() throws SQLException {
        return LOCALDB_CATALOG_NAME;
    }

    public void setTransactionIsolation(int level) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getTransactionIsolation() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Statement createStatement(
        int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public PreparedStatement prepareStatement(
        String sql,
        int resultSetType,
        int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public CallableStatement prepareCall(
        String sql,
        int resultSetType,
        int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setHoldability(int holdability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Savepoint setSavepoint() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Statement createStatement(
        int resultSetType,
        int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public PreparedStatement prepareStatement(
        String sql,
        int resultSetType,
        int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public CallableStatement prepareCall(
        String sql,
        int resultSetType,
        int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public PreparedStatement prepareStatement(
        String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public PreparedStatement prepareStatement(
        String sql, int columnIndexes[]) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public PreparedStatement prepareStatement(
        String sql, String columnNames[]) throws SQLException {
        throw new UnsupportedOperationException();
    }

    // implement Wrapper

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        } else if (iface.isInstance(connection)) {
            return iface.cast(connection);
        }
        throw helper.createException("does not implement '" + iface + "'");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) ||
            iface.isInstance(connection);
    }

    // implement OlapConnection

    public PreparedOlapStatement prepareOlapStatement(
        String mdx)
        throws OlapException
    {
        return factory.newPreparedStatement(mdx, this);
    }

    public MdxParserFactory getParserFactory() {
        return new MdxParserFactory() {
            public MdxParser createMdxParser(OlapConnection connection) {
                return new DefaultMdxParserImpl(connection);
            }
        };
    }

    public Schema getSchema() throws OlapException {
        return olap4jSchema;
    }

    MondrianOlap4jCube toOlap4j(mondrian.olap.Cube cube) {
        MondrianOlap4jSchema schema  = toOlap4j(cube.getSchema());
        return new MondrianOlap4jCube(cube, schema);
    }

    MondrianOlap4jDimension toOlap4j(mondrian.olap.Dimension dimension) {
        return new MondrianOlap4jDimension(
            toOlap4j(dimension.getSchema()),
            dimension);
    }

    synchronized MondrianOlap4jSchema toOlap4j(mondrian.olap.Schema schema) {
        MondrianOlap4jSchema olap4jSchema = schemaMap.get(schema);
        if (olap4jSchema == null) {
            final MondrianOlap4jCatalog olap4jCatalog =
                (MondrianOlap4jCatalog) getCatalogs().get(LOCALDB_CATALOG_NAME);
            olap4jSchema =
                new MondrianOlap4jSchema(
                    olap4jCatalog,
                    schema.getSchemaReader(),
                    schema);
            schemaMap.put(schema, olap4jSchema);
        }
        return olap4jSchema;
    }

    Type toOlap4j(mondrian.olap.type.Type type) {
        if (type instanceof mondrian.olap.type.BooleanType) {
            return new BooleanType();
        } else if (type instanceof mondrian.olap.type.CubeType) {
            final mondrian.olap.Cube mondrianCube =
                ((mondrian.olap.type.CubeType) type).getCube();
            return new CubeType(toOlap4j(mondrianCube));
        } else if (type instanceof mondrian.olap.type.DecimalType) {
            mondrian.olap.type.DecimalType decimalType =
                (mondrian.olap.type.DecimalType) type;
            return new DecimalType(
                decimalType.getPrecision(),
                decimalType.getScale());
        } else if (type instanceof mondrian.olap.type.DimensionType) {
            mondrian.olap.type.DimensionType dimensionType =
                (mondrian.olap.type.DimensionType) type;
            return new DimensionType(
                toOlap4j(dimensionType.getDimension()));
        } else if (type instanceof mondrian.olap.type.HierarchyType) {
            return new BooleanType();
        } else if (type instanceof mondrian.olap.type.LevelType) {
            return new BooleanType();
        } else if (type instanceof mondrian.olap.type.MemberType) {
            final mondrian.olap.type.MemberType memberType =
                (mondrian.olap.type.MemberType) type;
            return new MemberType(
                toOlap4j(memberType.getDimension()),
                toOlap4j(memberType.getHierarchy()),
                toOlap4j(memberType.getLevel()),
                toOlap4j(memberType.getMember()));
        } else if (type instanceof mondrian.olap.type.NullType) {
            return new NullType();
        } else if (type instanceof mondrian.olap.type.NumericType) {
            return new NumericType();
        } else if (type instanceof mondrian.olap.type.SetType) {
            final mondrian.olap.type.SetType setType =
                (mondrian.olap.type.SetType) type;
            return new SetType(toOlap4j(setType.getElementType()));
        } else if (type instanceof mondrian.olap.type.StringType) {
            return new StringType();
        } else if (type instanceof mondrian.olap.type.TupleType) {
            mondrian.olap.type.TupleType tupleType =
                (mondrian.olap.type.TupleType) type;
            final Type[] types = toOlap4j(tupleType.elementTypes);
            return new TupleType(types);
        } else if (type instanceof mondrian.olap.type.SymbolType) {
            return new SymbolType();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    MondrianOlap4jMember toOlap4j(mondrian.olap.Member member) {
        if (member == null) {
            return null;
        }
        throw new UnsupportedOperationException();
    }

    MondrianOlap4jLevel toOlap4j(mondrian.olap.Level level) {
        if (level == null) {
            return null;
        }
        return new MondrianOlap4jLevel(
            toOlap4j(level.getDimension().getSchema()),
            level);
    }

    MondrianOlap4jHierarchy toOlap4j(mondrian.olap.Hierarchy hierarchy) {
        if (hierarchy == null) {
            return null;
        }
        return new MondrianOlap4jHierarchy(
            toOlap4j(hierarchy.getDimension().getSchema()),
            hierarchy);
    }

    Type[] toOlap4j(mondrian.olap.type.Type[] mondrianTypes) {
        final Type[] types = new Type[mondrianTypes.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = toOlap4j(mondrianTypes[i]);
        }
        return types;
    }

    public Axis toOlap4j(mondrian.olap.AxisOrdinal axisOrdinal) {
        if (false) {
            return null;
        }
        throw new UnsupportedOperationException();
    }

    public static Map<String, String> toMap(final Properties properties) {
        return new AbstractMap<String, String>() {
            public Set<Entry<String, String>> entrySet() {
                return (Set) properties.entrySet();
            }
        };
    }

    // inner classes

    static class Helper {
        SQLException createException(String msg) {
            return new SQLException(msg);
        }

        OlapException createException(Cell context, String msg) {
            OlapException exception = new OlapException(msg);
            exception.setContext(context);
            return exception;
        }

        OlapException createException(Cell context, String msg, Throwable cause) {
            OlapException exception = new OlapException(msg, cause);
            exception.setContext(context);
            return exception;
        }

        public OlapException toOlapException(SQLException e) {
            if (e instanceof OlapException) {
                return (OlapException) e;
            } else {
                return new OlapException(null, e);
            }
        }
    }
}

// End MondrianOlap4jConnection.java
