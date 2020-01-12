package com.jsql.util.tampering;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

public class TamperingUtil {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    private boolean isBase64 = false;
    private boolean isVersionComment = false;
    private boolean isFunctionComment = false;
    private boolean isEqualToLike = false;
    private boolean isRandomCase = false;
    private boolean isHexToChar = false;
    private boolean isQuoteToUtf8 = false;
    private boolean isEval = false;
    private boolean isSpaceToMultilineComment = false;
    private boolean isSpaceToDashComment = false;
    private boolean isSpaceToSharpComment = false;
    
    private String customTamper = null;
    
    private static final ScriptEngine NASHORN_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");

    public void set(
        boolean isBase64,
        boolean isVersionComment,
        boolean isFunctionComment,
        boolean isEqualToLike,
        boolean isRandomCase,
        boolean isHexToChar,
        boolean isQuoteToUtf8,
        boolean isEval,
        boolean isSpaceToMultilineComment,
        boolean isSpaceToDashComment,
        boolean isSpaceToSharpComment
    ) {
        
        this.isBase64 = isBase64;
        this.isVersionComment = isVersionComment;
        this.isFunctionComment = isFunctionComment;
        this.isEqualToLike = isEqualToLike;
        this.isRandomCase = isRandomCase;
        this.isHexToChar = isHexToChar;
        this.isQuoteToUtf8 = isQuoteToUtf8;
        this.isEval = isEval;
        this.isSpaceToMultilineComment = isSpaceToMultilineComment;
        this.isSpaceToDashComment = isSpaceToDashComment;
        this.isSpaceToSharpComment = isSpaceToSharpComment;
    }
    
    private static String eval(String out, String js) {
        Object result = null;
        
        try {
            NASHORN_ENGINE.eval(js);
            
            Invocable invocable = (Invocable) NASHORN_ENGINE;
            result = invocable.invokeFunction("tampering", out);
        } catch (ScriptException | NoSuchMethodException e) {
            LOGGER.warn(e);
            result = out;
        }
        
        return result.toString();
    }
    
    public String tamper(String in) {
        
        String sqlQueryDefault = in;
        
        String lead = null;
        String sqlQuery = null;
        String trail = null;
        
        
        Matcher m = Pattern.compile("(?s)(.*SlQqLs)(.*)(lSqQsL.*)").matcher(sqlQueryDefault);
        if (m.find()) {
           lead = m.group(1);
           sqlQuery = m.group(2);
           trail = m.group(3);
        }

        if (this.isHexToChar) {
            sqlQuery = eval(sqlQuery, TamperingType.HEX_TO_CHAR.instance().getJavascript());
        }

        if (this.isFunctionComment) {
            sqlQuery = eval(sqlQuery, TamperingType.COMMENT_TO_METHOD_SIGNATURE.instance().getJavascript());
        }

        if (this.isVersionComment) {
            sqlQuery = eval(sqlQuery, TamperingType.VERSIONED_COMMENT_TO_METHOD_SIGNATURE.instance().getJavascript());
        }
        
        if (this.isEqualToLike) {
            sqlQuery = eval(sqlQuery, TamperingType.EQUAL_TO_LIKE.instance().getJavascript());
        }
        
        // Dependency to: EQUAL_TO_LIKE
        if (this.isSpaceToDashComment) {
            sqlQuery = eval(sqlQuery, TamperingType.SPACE_TO_DASH_COMMENT.instance().getJavascript());
            
        } else if (this.isSpaceToMultilineComment) {
            sqlQuery = eval(sqlQuery, TamperingType.SPACE_TO_MULTILINE_COMMENT.instance().getJavascript());
            
        } else if (this.isSpaceToSharpComment) {
            sqlQuery = eval(sqlQuery, TamperingType.SPACE_TO_SHARP_COMMENT.instance().getJavascript());
        }
        
        if (this.isRandomCase) {
            sqlQuery = eval(sqlQuery, TamperingType.RANDOM_CASE.instance().getJavascript());
        }
        
        if (this.isEval) {
            sqlQuery = eval(sqlQuery, this.customTamper);
        }
        
        if (this.isBase64) {
            sqlQuery = eval(sqlQuery, TamperingType.BASE64.instance().getJavascript());
        }
        
        sqlQuery = lead + sqlQuery + trail;
        
        // Include character insertion at the beginning of query
        if (this.isQuoteToUtf8) {
            sqlQuery = eval(sqlQuery, TamperingType.QUOTE_TO_UTF8.instance().getJavascript());
        }
        
        // Probl�me si le tag contient des caract�res sp�ciaux
        sqlQuery = sqlQuery.replaceAll("(?i)SlQqLs", "");
        sqlQuery = sqlQuery.replaceAll("(?i)lSqQsL", "");
        
        return sqlQuery;
    }

    public String getCustomTamper() {
        return this.customTamper;
    }

    public void setCustomTamper(String customTamper) {
        this.customTamper = customTamper;
    }

}
