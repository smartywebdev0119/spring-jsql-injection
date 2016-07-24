package com.test.method;

import org.junit.BeforeClass;

import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.model.injection.strategy.Strategy;
import com.jsql.util.ConnectionUtil;
import com.jsql.view.terminal.SystemOutTerminal;
import com.test.AbstractTestSuite;
import com.test.mysql.ConcreteMysqlTestSuite;

public class CookieTest extends ConcreteMysqlTestSuite {
    // pour chaque vendor/m�thode/strategy
    /**
     * liste db, table, colonne, value
     * valeur � rallonge
     * caract�re sp�cial \
     * @throws InjectionFailureException
     */

    @BeforeClass
    public static void initialize() throws InjectionFailureException {
        InjectionModel model = new InjectionModel();
        MediatorModel.register(model);
        model.sendVersionToView();

        MediatorModel.model().addObserver(new SystemOutTerminal());

        ConnectionUtil.setUrlBase("http://"+ AbstractTestSuite.HOSTNAME +"/simulate_cookie.php");
        ConnectionUtil.setDataHeader("Cookie:lib=0");
        ConnectionUtil.setMethodInjection(MethodInjection.HEADER);

        MediatorModel.model().injection();

        MediatorModel.model().setStrategy(Strategy.NORMAL);
    }
}
