package com.test.vendor.sqlserver;

import org.junit.BeforeClass;
import org.junit.Ignore;

import com.jsql.model.exception.InjectionFailureException;

@Ignore
public class SQLServerNormalGetTestSuite extends ConcreteSQLServerTestSuite {

    @Override
    @BeforeClass
    public void initialize3() throws InjectionFailureException {
//        InjectionModel model = new InjectionModel();
//        MediatorModel.register(model);
//        model.displayVersion();
//
//        MediatorModel.model().addObserver(new SystemOutTerminal());
//
//        ConnectionUtil.setUrlBase("http://"+ AbstractTestSuite.HOSTNAME +"/sqlserver_simulate_get.php");
//        ParameterUtil.setQueryString(Arrays.asList(new SimpleEntry<String, String>("lib", "0")));
//        ConnectionUtil.setMethodInjection(MethodInjection.QUERY);
//
//        MediatorModel.model().beginInjection();
//
//        MediatorModel.model().setStrategy(StrategyInjection.NORMAL);
    }
    
}
