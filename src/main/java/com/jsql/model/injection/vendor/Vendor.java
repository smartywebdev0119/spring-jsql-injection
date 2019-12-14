package com.jsql.model.injection.vendor;

import org.apache.commons.lang3.StringUtils;

import com.jsql.model.injection.vendor.model.AbstractVendor;
import com.jsql.model.injection.vendor.model.VendorXml;

public enum Vendor {
    
//    AUTO("Database auto", null),
//    ACCESS("Access", new VendorXml("access.xml")),
//    COCKROACHDB("CockroachDB", new VendorXml("cockroachdb.xml")),
//    CUBRID("CUBRID", new VendorXml("cubrid.xml")),
//    DB2("DB2", new VendorXml("db2.xml")),
//    DERBY("Derby", new VendorXml("derby.xml")),
//    FIREBIRD("Firebird", new VendorXml("firebird.xml")),
//    H2("H2", new VendorXml("h2.xml")),
//    HANA("Hana", new VendorXml("hana.xml")),
//    HSQLDB("HSQLDB", new VendorXml("hsqldb.xml")),
//    INFORMIX("Informix", new VendorXml("informix.xml")),
//    INGRES("Ingres", new VendorXml("ingres.xml")),
//    MAXDB("MaxDB", new VendorXml("maxdb.xml")),
//    MCKOI("Mckoi", new VendorXml("mckoi.xml")),
//    MEMSQL("MemSQL", new VendorXml("memsql.xml")),
//    MYSQL("MySQL", new VendorXml("mysql.xml")),
//    NEO4J("Neo4j", new VendorXml("neo4j.xml")),
//    NUODB("NuoDB", new VendorXml("nuodb.xml")),
//    ORACLE("Oracle", new VendorXml("oracle.xml")),
//    POSTGRESQL("PostgreSQL", new VendorXml("postgresql.xml")),
//    SQLITE("SQLite", new VendorXml("sqlite.xml")) {
//        
//        @Override
//        public String transform(String resultToParse) {
//            
//            StringBuilder resultSQLite = new StringBuilder();
//            String resultTmp = resultToParse.replaceFirst(".+?\\(", "").trim().replaceAll("\\)$", "");
//            resultTmp = resultTmp.replaceAll("\\(.+?\\)", "");
//            
//            for (String columnNameAndType: resultTmp.split(",")) {
//                // Some recent SQLite use tabulation character as a separator => split() by any  white space \s
//                String columnName = columnNameAndType.trim().split("\\s")[0];
//                
//                // Some recent SQLite enclose names with ` => strip those `
//                columnName = StringUtils.strip(columnName, "`");
//                
//                if (!"CONSTRAINT".equals(columnName) && !"UNIQUE".equals(columnName)) {
//                    resultSQLite.append((char) 4 + columnName + (char) 5 + "0" + (char) 4 + (char) 6);
//                }
//            }
//
//            return resultSQLite.toString();
//            
//        }
//        
//    },
//    SQLSERVER("SQL Server", new VendorXml("sqlserver.xml")),
//    SYBASE("Sybase", new VendorXml("sybase.xml")),
//    TERADATA("Teradata", new VendorXml("teradata.xml")),
//    VERTICA("Vertica", new VendorXml("vertica.xml"));
//    
////    // TODO Pojo injection
////    private List<StrategyInjection> strategies = new ArrayList<>();
////    private List<Integer> methodsError = new ArrayList<>();
//
//    private final String labelVendor;
//    
//    private final AbstractVendor instanceVendor;
//    
//    private Vendor(String labelVendor, AbstractVendor instanceVendor) {
//        this.labelVendor = labelVendor;
//        this.instanceVendor = instanceVendor;
//    }
//    
//    public AbstractVendor instance() {
//        return this.instanceVendor;
//    }
//    
//    @Override
//    public String toString() {
//        return this.labelVendor;
//    }
//    
//    public String transform(String resultToParse) {
//        return "";
//    }
    
}
