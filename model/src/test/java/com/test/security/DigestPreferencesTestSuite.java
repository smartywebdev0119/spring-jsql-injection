package com.test.security;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

import org.junitpioneer.jupiter.RepeatFailedTest;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.view.terminal.SystemOutTerminal;
import com.test.vendor.mysql.ConcreteMySqlErrorTestSuite;

import spring.security.SecurityConfiguration;

public class DigestPreferencesTestSuite extends ConcreteMySqlErrorTestSuite {
    
    @Override
    public void setupInjection() throws Exception {
        
        InjectionModel model = new InjectionModel();
        this.injectionModel = model;

        model.addObserver(new SystemOutTerminal());

        model.getMediatorUtils().getParameterUtil().initializeQueryString("http://localhost:8080/digest");
        model.getMediatorUtils().getParameterUtil().setListQueryString(Arrays.asList(
            new SimpleEntry<>("tenant", "mysql-error"),
            new SimpleEntry<>("name", "")
        ));
        
        model.getMediatorUtils().getAuthenticationUtil().setIsAuthentEnabled(true);
        model.getMediatorUtils().getAuthenticationUtil().setUsernameAuthentication(SecurityConfiguration.DIGEST_USERNAME);
        model.getMediatorUtils().getAuthenticationUtil().setPasswordAuthentication(SecurityConfiguration.DIGEST_PASSWORD);
        model.getMediatorUtils().getAuthenticationUtil().setAuthentication();
        
        model
        .getMediatorUtils()
        .getConnectionUtil()
        .withMethodInjection(model.getMediatorMethod().getQuery())
        .withTypeRequest("GET");
        
        model.setIsScanning(true);
        model.beginInjection();
    }
    
    @Override
    @RepeatFailedTest(3)
    public void listDatabases() throws JSqlException {
        super.listDatabases();
    }
}
