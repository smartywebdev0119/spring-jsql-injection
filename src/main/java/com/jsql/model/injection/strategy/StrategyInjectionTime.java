/*******************************************************************************
 * Copyhacked (H) 2012-2016.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 *
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 *******************************************************************************/
package com.jsql.model.injection.strategy;

import org.apache.log4j.Logger;

import com.jsql.model.MediatorModel;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.strategy.blind.InjectionTime;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * Injection strategy using time attack.
 */
public class StrategyInjectionTime extends AbstractStrategy {
	
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    /**
     * Injection method using time attack.
     */
    private InjectionTime timeInjection;
    
    @Override
    public void checkApplicability() throws StoppedByUserSlidingException {
        LOGGER.trace("Checking strategy Time...");
        
        this.timeInjection = new InjectionTime();
        
        this.isApplicable = this.timeInjection.isInjectable();
        
        if (this.isApplicable) {
            LOGGER.debug("Vulnerable to Time injection");
            this.allow();
        } else {
            this.unallow();
        }
    }
    
    @Override
    public void allow() {
        this.markVulnerable(Interaction.MARK_TIME_VULNERABLE);
    }

    @Override
    public void unallow() {
        this.markVulnerable(Interaction.MARK_TIME_INVULNERABLE);
    }

    @Override
    public String inject(String sqlQuery, String startPosition, AbstractSuspendable<String> stoppable) throws StoppedByUserSlidingException {
        return this.timeInjection.inject(
            MediatorModel.model().getVendor().instance().sqlTime(sqlQuery, startPosition),
            stoppable
        );
    }

    @Override
    public void activateStrategy() {
        LOGGER.info("Using strategy ["+ this.getName() +"]");
        MediatorModel.model().setStrategy(StrategyInjection.TIME);
        
        Request requestMessageBinary = new Request();
        requestMessageBinary.setMessage(Interaction.MESSAGE_BINARY);
        requestMessageBinary.setParameters(this.timeInjection.getInfoMessage());
        MediatorModel.model().sendToViews(requestMessageBinary);
        
        Request requestMarkTimeStrategy = new Request();
        requestMarkTimeStrategy.setMessage(Interaction.MARK_TIME_STRATEGY);
        MediatorModel.model().sendToViews(requestMarkTimeStrategy);
    }
    
    @Override
    public String getPerformanceLength() {
        return "65565";
    }
    
    @Override
    public String getName() {
        return "Time";
    }
    
}
