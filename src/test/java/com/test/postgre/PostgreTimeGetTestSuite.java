package com.test.postgre;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.model.exception.PreparationException;
import com.jsql.model.exception.StoppableException;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.model.injection.strategy.Strategy;
import com.jsql.util.ConnectionUtil;
import com.jsql.view.terminal.SystemOutTerminal;

public class PostgreTimeGetTestSuite extends ConcretePostgreTestSuite {

    @BeforeClass
    public static void initialize() throws PreparationException {
        InjectionModel model = new InjectionModel();
        MediatorModel.register(model);
        model.sendVersionToView();
        new SystemOutTerminal();

        ConnectionUtil.urlByUser = "http://127.0.0.1/pg_simulate_get.php";
        ConnectionUtil.dataQuery = "?lib=1";
        ConnectionUtil.methodInjection = MethodInjection.QUERY;

        MediatorModel.model().injection();

        MediatorModel.model().setStrategy(Strategy.TIME);
    }

    @Override
    @Test
    @Ignore
    public void listColumns() throws PreparationException, StoppableException {
        // Empty on purpose
    }

    @Override
    @Test
    @Ignore
    public void listTables() throws PreparationException, StoppableException {
        // Empty on purpose
    }
}
