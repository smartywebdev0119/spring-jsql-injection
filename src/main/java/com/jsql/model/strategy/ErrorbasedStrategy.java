package com.jsql.model.strategy;

import org.apache.log4j.Logger;

import com.jsql.model.MediatorModel;
import com.jsql.model.accessible.bean.Request;
import com.jsql.model.exception.StoppableException;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * Injection strategy using error attack.
 */
public class ErrorbasedStrategy extends AbstractStrategy {
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getLogger(ErrorbasedStrategy.class);

    @Override
    public void checkApplicability() {
        LOGGER.trace("Error based test...");
        
        String performanceSourcePage = MediatorModel.model().injectWithoutIndex(
            MediatorModel.model().insertionCharacter + 
            MediatorModel.model().currentVendor.getValue().getSqlErrorBasedCheck()
        );

        isApplicable = performanceSourcePage.matches(
            "(?s).*(Duplicate entry '1' for key "
            + "|Like verdier '1' for "
            + "|Like verdiar '1' for "
            + "|Kattuv v��rtus '1' v�tmele "
            + "|Opakovan� k��� '1' \\(��slo k���a "
            + "|pienie '1' dla klucza "
            + "|Duplikalt bejegyzes '1' a "
            + "|Ens v�rdier '1' for indeks "
            + "|Dubbel nyckel '1' f�r nyckel "
            + "|kl�� '1' \\(��slo kl��e "
            + "|Duplicata du champ '1' pour la clef "
            + "|Entrada duplicada '1' para la clave "
            + "|Cimpul '1' e duplicat pentru cheia "
            + "|Dubbele ingang '1' voor zoeksleutel "
            + "|Valore duplicato '1' per la chiave "
            /*jp missing*/
            /*kr grk ukr rss missing*/
            + "|Dupliran unos '1' za klju"        
            + "|Entrada '1' duplicada para a chave ).*"
        );
        
        if (this.isApplicable) {
            this.allow();
        } else {
            this.unallow();
        }
    }

    @Override
    public void allow() {
        Request request = new Request();
        request.setMessage("MarkErrorbasedVulnerable");
        MediatorModel.model().interact(request);
    }

    @Override
    public void unallow() {
        Request request = new Request();
        request.setMessage("MarkErrorbasedInvulnerable");
        MediatorModel.model().interact(request);
    }

    @Override
    public String inject(String sqlQuery, String startPosition, AbstractSuspendable stoppable) throws StoppableException {
        return MediatorModel.model().injectWithoutIndex(
            MediatorModel.model().insertionCharacter +
            MediatorModel.model().currentVendor.getValue().getSqlErrorBased(sqlQuery, startPosition)
        );
    }

    @Override
    public void activateStrategy() {
        LOGGER.info("Using error based injection...");
        MediatorModel.model().setStrategy(Strategy.ERRORBASED);
        
        Request request = new Request();
        request.setMessage("MarkErrorbasedStrategy");
        MediatorModel.model().interact(request);
    }
    
    @Override
    public String getPerformanceLength() {
        /**
         * mysql errorbase renvoit 64 caract�res - 'SQLi' = 60
         * on va prendre 60 caract�res apr�s le marqueur SQLi
         */
        return "60" ;
    }
    
    @Override
    public String getName() {
        return "Error based";
    }
}
