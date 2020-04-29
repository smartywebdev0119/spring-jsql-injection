package com.jsql.view.swing.text;

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jsql.view.swing.text.action.DeleteNextCharAction;
import com.jsql.view.swing.text.action.DeletePrevCharAction;
import com.jsql.view.swing.util.UiUtil;

/**
 * Textfield with information text displayed when empty.
 */
@SuppressWarnings("serial")
public class JTextAreaPlaceholder extends JTextArea {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * Text to display when empty.
     */
    private String placeholderText = StringUtils.EMPTY;
    
    /**
     * Create a textfield with hint and default value.
     * @param placeholder Text displayed when empty
     * @param value Default value
     */
    public JTextAreaPlaceholder(String placeholder, String value) {
        
        this(placeholder);
        this.setText(value);
    }
    
    /**
     * Create a textfield with hint.
     * @param placeholder Text displayed when empty
     */
    public JTextAreaPlaceholder(String placeholder) {
        
        this.placeholderText = placeholder;
        
        UiUtil.initialize(this);
    }

    @Override
    public void paint(Graphics g) {
        
        // Fix #6350: ArrayIndexOutOfBoundsException on paint()
        // Fix #90822: IllegalArgumentException on paint()
        // Fix #90761: StateInvariantError on paint()
        // StateInvariantError possible on jdk 8 when WrappedPlainView.drawLine in paint()
        try {
            super.paint(g);
            
            if (StringUtils.isEmpty(this.getText())) {
                
                UiUtil.drawPlaceholder(this, g, this.placeholderText);
            }
            
        } catch (IllegalArgumentException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            
            LOGGER.error(e.getMessage(), e);
        }
    }
}