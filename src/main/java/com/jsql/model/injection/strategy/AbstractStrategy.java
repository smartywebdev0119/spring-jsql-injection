package com.jsql.model.injection.strategy;

import java.util.EnumMap;
import java.util.Map;

import com.jsql.model.InjectionModel;
import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * Define a strategy to inject SQL with methods like Error and Time.
 */
public abstract class AbstractStrategy {
    
    public AbstractStrategy(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }
    InjectionModel injectionModel;
	
    /**
     * True if injection can be used, false otherwise.
     */
    protected boolean isApplicable = false;

    /**
     * Return if this strategy can be used to inject SQL.
     * @return True if strategy can be used, false otherwise.
     */
    public boolean isApplicable() {
        return this.isApplicable;
    }

    /**
     * Test if this strategy can be used to inject SQL.
     * @return
     * @throws InjectionFailureException
     * @throws StoppedByUserSlidingException
     */
    public abstract void checkApplicability() throws JSqlException;
    
    /**
     * Inform the view that this strategy can be used.
     */
    protected abstract void allow(int... i);
    
    /**
     * Inform the view that this strategy can't be used.
     */
    protected abstract void unallow(int... i);
    
    public void markVulnerability(Interaction message, int... i) {
        Request request = new Request();
        request.setMessage(message);
        
        Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
        msgHeader.put(Header.URL, this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlByUser());
        
        if (i != null && i.length > 0) {
            msgHeader.put(Header.SOURCE, i[0]);
            msgHeader.put(Header.INJECTION_MODEL, this.injectionModel);
        }

        request.setParameters(msgHeader);
        this.injectionModel.sendToViews(request);
    }
    
    /**
     * Start the strategy work.
     * @return Source code
     */
    public abstract String inject(String sqlQuery, String startPosition, AbstractSuspendable<String> stoppable) throws StoppedByUserSlidingException;
    
    /**
     * Change the strategy of the model to current strategy.
     */
    public abstract void activateStrategy();
    
    /**
     * Get number of characters you can obtain from the strategy.
     */
    public abstract String getPerformanceLength();
    
    /**
     * Get the injection strategy name.
     */
    public abstract String getName();
    
    // TODO strategy Error
    public Integer getIndexMethodError() {
        return null;
    }

    protected String visibleIndex;
    
    public String getVisibleIndex() {
        return this.visibleIndex;
    }

    public void setVisibleIndex(String visibleIndex) {
        this.visibleIndex = visibleIndex;
    }
    
}
