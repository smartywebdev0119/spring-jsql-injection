package com.test.vendor.mysql;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

import org.junit.BeforeClass;

import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.model.injection.strategy.StrategyInjection;
import com.jsql.util.ConnectionUtil;
import com.jsql.util.ParameterUtil;
import com.jsql.util.PreferencesUtil;
import com.jsql.view.terminal.SystemOutTerminal;

public class MysqlErrobasedHeaderTestSuite extends ConcreteMysqlErrorTestSuite {

    @BeforeClass
    public static void initialize() throws Exception {
        runSpringApplication();
        
        InjectionModel model = new InjectionModel();
        MediatorModel.register(model);
        model.displayVersion();

        MediatorModel.model().addObserver(new SystemOutTerminal());

        PreferencesUtil.setNotTestingConnection(true);
        
        ParameterUtil.initQueryString("http://localhost:8080/greeting-error");
        ParameterUtil.initRequest("");
        ParameterUtil.setQueryString(
            Arrays.asList(
                new SimpleEntry<String, String>("tenantId", "mysql-error"), 
                new SimpleEntry<String, String>("name", "1'")
            )
        );
        ConnectionUtil.setMethodInjection(MethodInjection.QUERY);
        ConnectionUtil.setTypeRequest("GET");
        
        MediatorModel.model().beginInjection();

        MediatorModel.model().setStrategy(StrategyInjection.ERROR);
    }
    
}
