package com.test;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.h2.tools.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junitpioneer.jupiter.RepeatFailedTest;
import org.springframework.boot.SpringApplication;

import com.jsql.model.InjectionModel;
import com.jsql.model.bean.database.Column;
import com.jsql.model.bean.database.Database;
import com.jsql.model.bean.database.Table;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;

import spring.TargetApplication;

@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class AbstractTestSuite {
    
    static {
        // Use Timeout fix in Model
        PropertyConfigurator.configure("src/test/resources/logger/log4j.stdout.properties");
        jcifs.Config.registerSmbURLHandler();
    }
    
    /**
     * Using default log4j.properties from root /
     */
    protected static final Logger LOGGER = Logger.getRootLogger();

    public static final String HOSTNAME = "localhost";
    
    private List<String> databasesFromJdbc = new ArrayList<>();
    private List<String> tablesFromJdbc = new ArrayList<>();
    private List<String> columnsFromJdbc = new ArrayList<>();
    private List<String> valuesFromJdbc = new ArrayList<>();
    
    protected String jdbcURL;
    protected String jdbcUser;
    protected String jdbcPass;
    
    protected String jdbcQueryForDatabaseNames;
    protected String jdbcQueryForTableNames;
    protected String jdbcQueryForColumnNames;
    protected String jdbcQueryForValues;
    
    protected String jdbcColumnForDatabaseName;
    protected String jdbcColumnForTableName;
    protected String jdbcColumnForColumnName;
    
    protected String jsqlDatabaseName;
    protected String jsqlTableName;
    protected String jsqlColumnName;
    
    private static AtomicBoolean isSetupStarted = new AtomicBoolean(false);
    
    private static AtomicBoolean isSetupDone = new AtomicBoolean(false);
    
    protected InjectionModel injectionModel;
    
    public abstract void setupInjection() throws Exception;
    
    @BeforeAll
    public synchronized void initializeBackend() throws Exception {
        
        if (this.isSetupStarted.compareAndSet(false, true)) {
            
            LOGGER.info("@BeforeClass: loading H2, Hibernate and Spring...");
            Server.createTcpServer().start();
            TargetApplication.initializeDatabases();
            SpringApplication.run(TargetApplication.class, new String[] {});
            
            isSetupDone.set(true);
        }
            
        while (!isSetupDone.get()) {
            Thread.sleep(1000);
            LOGGER.info("@BeforeClass: backend is setting up, please wait...");
        }
            
        if (this.injectionModel == null) {
            
            this.requestJdbc();
            this.setupInjection();
        }
    }

    public void initialize() throws Exception {
        
        LOGGER.warn("AbstractTestSuite and ConcreteTestSuite are for initialization purpose. Run test suite or unit test instead.");
        throw new InjectionFailureException();
    }

    public void requestJdbc() throws SQLException {
        
        try (
            Connection conn = DriverManager.getConnection(this.jdbcURL, this.jdbcUser, this.jdbcPass);
                
            Statement statementDatabase = conn.createStatement();
            ResultSet resultSetDatabase = statementDatabase.executeQuery(this.jdbcQueryForDatabaseNames);
                
            Statement statementTable = conn.createStatement();
            ResultSet resultSetTable = statementTable.executeQuery(this.jdbcQueryForTableNames);
                
            Statement statementColumn = conn.createStatement();
            ResultSet resultSetColumn = statementColumn.executeQuery(this.jdbcQueryForColumnNames);
                
            Statement statementValues = conn.createStatement();
            ResultSet resultSetValues = statementValues.executeQuery(this.jdbcQueryForValues);
        ) {
            
            while (resultSetDatabase.next()) {
                String dbName = resultSetDatabase.getString(this.jdbcColumnForDatabaseName);
                this.databasesFromJdbc.add(dbName);
            }
            
            while (resultSetTable.next()) {
                String tableName = resultSetTable.getString(this.jdbcColumnForTableName);
                this.tablesFromJdbc.add(tableName);
            }
            
            while (resultSetColumn.next()) {
                String colName = resultSetColumn.getString(this.jdbcColumnForColumnName);
                this.columnsFromJdbc.add(colName);
            }

            while (resultSetValues.next()) {
                String value = resultSetValues.getString(this.jsqlColumnName);
                this.valuesFromJdbc.add(value);
            }
        } catch (SQLException e) {
            
            LOGGER.error(e, e);
        }
    }

    @RepeatFailedTest(3)
    public void listDatabases() throws JSqlException {
        
        Set<String> valuesFromInjection = new HashSet<>();
        Set<String> valuesFromJdbc = new HashSet<>();
        
        try {
            List<String> databases = this.injectionModel.getDataAccess().listDatabases()
                .stream()
                .map(Database::toString)
                .collect(Collectors.toList());

            valuesFromInjection.addAll(databases);
            valuesFromJdbc.addAll(AbstractTestSuite.this.databasesFromJdbc);

            LOGGER.info("ListDatabases: found "+ valuesFromInjection +" to find "+ valuesFromJdbc);

            assertTrue(!valuesFromInjection.isEmpty() && !valuesFromJdbc.isEmpty() && valuesFromInjection.containsAll(valuesFromJdbc));
            
        } catch (AssertionError e) {
            
            Set<String> tablesUnkown = Stream.concat(
                valuesFromInjection.stream().filter(value -> !valuesFromJdbc.contains(value)),
                valuesFromJdbc.stream().filter(value -> !valuesFromInjection.contains(value))
            ).collect(Collectors.toCollection(TreeSet::new));
            
            throw new AssertionError(String.format("Unknown databases: %s\n%s", tablesUnkown, e));
        }
    }

    @RepeatFailedTest(3)
    public void listTables() throws JSqlException {
        
        Set<String> valuesFromInjection = new HashSet<>();
        Set<String> valuesFromJdbc = new HashSet<>();

        try {
            List<String> tables = this.injectionModel.getDataAccess().listTables(new Database(AbstractTestSuite.this.jsqlDatabaseName, "0"))
                .stream()
                .map(Table::toString)
                .collect(Collectors.toList());

            valuesFromInjection.addAll(tables);
            valuesFromJdbc.addAll(AbstractTestSuite.this.tablesFromJdbc);

            LOGGER.info(String.format("Tables: found %s to find %s", valuesFromInjection, valuesFromJdbc));
            assertTrue(!valuesFromInjection.isEmpty() && !valuesFromJdbc.isEmpty() && valuesFromInjection.equals(valuesFromJdbc));
            
        } catch (AssertionError e) {
            
            Set<String> tablesUnkown = Stream.concat(
                valuesFromInjection.stream().filter(value -> !valuesFromJdbc.contains(value)),
                valuesFromJdbc.stream().filter(value -> !valuesFromInjection.contains(value))
            ).collect(Collectors.toCollection(TreeSet::new));
            
            throw new AssertionError(String.format("Unknown tables: %s\n%s", tablesUnkown, e));
        }
    }

    @RepeatFailedTest(3)
    public void listColumns() throws JSqlException {
        
        Set<String> valuesFromInjection = new HashSet<>();
        Set<String> valuesFromJdbc = new HashSet<>();

        try {
            List<String> columns = this.injectionModel.getDataAccess().listColumns(
                    new Table(AbstractTestSuite.this.jsqlTableName, "0",
                        new Database(AbstractTestSuite.this.jsqlDatabaseName, "0")
                    )
                ).stream()
                .map(Column::toString)
                .collect(Collectors.toList());

            valuesFromInjection.addAll(columns);
            valuesFromJdbc.addAll(AbstractTestSuite.this.columnsFromJdbc);

            LOGGER.info(String.format("listColumns: found %s to find %s", valuesFromInjection, valuesFromJdbc));
            assertTrue(!valuesFromInjection.isEmpty() && !valuesFromJdbc.isEmpty() && valuesFromInjection.equals(valuesFromJdbc));
            
        } catch (AssertionError e) {
            
            Set<String> columnsUnkown = Stream.concat(
                valuesFromInjection.stream().filter(value -> !valuesFromJdbc.contains(value)),
                valuesFromJdbc.stream().filter(value -> !valuesFromInjection.contains(value))
            ).collect(Collectors.toCollection(TreeSet::new));
            
            throw new AssertionError(String.format("Unknown columns: %s\n%s", columnsUnkown, e));
        }
    }

    @RepeatFailedTest(3)
    public void listValues() throws JSqlException {
        
        Set<String> valuesFromInjection = new TreeSet<>();
        Set<String> valuesFromJdbc = new TreeSet<>();

        try {
            String[][] rows = this.injectionModel.getDataAccess().listValues(Arrays.asList(
                new Column(AbstractTestSuite.this.jsqlColumnName,
                    new Table(AbstractTestSuite.this.jsqlTableName, "0",
                        new Database(AbstractTestSuite.this.jsqlDatabaseName, "0")
                    )
                )
            ));
            
            List<String> valuesFound = Arrays.asList(rows).stream()
                // => row number, occurrence, value1, value2...
                .map(row -> row[2].replaceAll("\r\n", "\n"))
                .collect(Collectors.toList());

            valuesFromInjection.addAll(valuesFound);
            valuesFromJdbc.addAll(AbstractTestSuite.this.valuesFromJdbc);

            String logValuesFromInjection = valuesFromInjection.toString()
                .replaceAll("\n", "[n]")
                .replaceAll("\r", "[r]");
            
            String logValuesFromJdbc = valuesFromJdbc.toString()
                .replaceAll("\n", "[n]")
                .replaceAll("\r", "[r]");
            
            LOGGER.info(String.format("Values: found %s to find %s", logValuesFromInjection, logValuesFromJdbc));
            
            assertTrue(!valuesFromInjection.isEmpty() && !valuesFromJdbc.isEmpty() && valuesFromInjection.equals(valuesFromJdbc));
            
        } catch (AssertionError e) {
            
            Set<String> valuesUnknown = Stream.concat(
                valuesFromInjection.stream().filter(value -> !valuesFromJdbc.contains(value)),
                valuesFromJdbc.stream().filter(value -> !valuesFromInjection.contains(value))
            ).collect(Collectors.toCollection(TreeSet::new));
            
            throw new AssertionError(String.format("Unknown values: %s\n%s", valuesUnknown, e));
        }
    }
}
