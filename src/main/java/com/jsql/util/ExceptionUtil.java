package com.jsql.util;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Utility class managing an exception reporting mecanism.
 * It uses Github as the issue webtracker.
 */
public class ExceptionUtil {
	
    /**
     * Using default log4j.properties from root /
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    // Utility class
    private ExceptionUtil() {
        // not called
    }

    /**
     * Handler class processing errors on top of the JVM in order to send
     * a report to Github automatically.
     */
    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            // for other uncaught exceptions
            LOGGER.error("Unhandled Exception on "+ thread.getName(), throwable);
            
            //  Report #214: ignore if OutOfMemoryError: Java heap space
            if (PreferencesUtil.isReportingBugs() && !(throwable instanceof OutOfMemoryError)) {
            	GitUtil.sendUnhandledException(thread.getName(), throwable);
            }
        }
        
    }
    
    /**
     * Add the error reporting mecanism on top of the JVM in order to
     * intercept and process the error to Github.
     */
    public static void setUncaughtExceptionHandler() {
    	
    	// Regular Exception
    	Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

    	// Event dispatching thread Exception
    	try {
			SwingUtilities.invokeAndWait(() -> 
		        // We are in the event dispatching thread
				Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler())
		    );
		} catch (InvocationTargetException | InterruptedException e) {
			LOGGER.error("Unhandled Exception on ExceptionUtil", e);
		}
    	
    }
    
}
