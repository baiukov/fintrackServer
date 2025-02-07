package me.vse.fintrackserver;

import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public abstract class DatabaseTest extends DataSourceBasedDBTestCase {
    @Override
    protected DataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(
                "jdbc:h2:mem:default;MODE=MYSQL;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:test_schema.sql'"
        );
        dataSource.setUser("sa");
        dataSource.setPassword("sa");
        return dataSource;
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(getClass().getClassLoader()
                .getResourceAsStream("datasets/defaultSet.xml"));
    }

    protected final DbUnitAssert assertion = new DbUnitAssert();
}
