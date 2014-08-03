/*******************************************************************************
 * Copyhacked (H) 2012-2014.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 *
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 *******************************************************************************/
package com.jsql.view.textcomponent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.jsql.view.popupmenu.JPopupComponentMenu;

/**
 * Add a popup menu to Decorated component.
 * @param <T> Component like JTextField or JTextArea to decorate
 */
@SuppressWarnings("serial")
public class JPopupComponent<T extends JComponent> extends JComponent implements JComponentDecorator<T> {

    /**
     * Decorated component.
     */
    protected T proxy;
    
    /**
     * Get the decorated component, add popup menu Select All and Copy.
     * @param proxy Swing component to decorate
     */
    public JPopupComponent(final T proxy) {
        super();
        
        this.proxy = proxy;
        
        this.proxy.setComponentPopupMenu(new JPopupComponentMenu(this.proxy));
        
        this.proxy.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                
                // Left button will deselect text after selectAll, so only for right click
                if(SwingUtilities.isRightMouseButton(e)){
                    JPopupComponent.this.proxy.requestFocusInWindow();
                }
            }
        });
    }

    @Override
    public T getProxy() {
        return this.proxy;
    }
}