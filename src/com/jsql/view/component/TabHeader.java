/*******************************************************************************
 * Copyhacked (H) 2012-2013.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jsql.view.GUIMediator;

/**
 * Panel displayed as a header for tabs
 */
@SuppressWarnings("serial")
public class TabHeader extends JPanel implements MouseListener{
	
    public TabHeader(){
        this(new ImageIcon(TabHeader.class.getResource("/com/jsql/view/images/table.png")));
    }
    
	public TabHeader(ImageIcon imageIcon){
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        this.setOpaque(false);
        
        // Set the text of tab
        JLabel tabTitleLabel = new JLabel(){
            public String getText() {
                int i = GUIMediator.right().indexOfTabComponent(TabHeader.this);
                if (i != -1) {
                    return GUIMediator.right().getTitleAt(i);
                }
                return null;
            }
        };
        tabTitleLabel.setIcon(imageIcon);
        this.add(tabTitleLabel);
        
        // Icon for closing tab
        Icon closeIcon = new ImageIcon(this.getClass().getResource("/com/jsql/view/images/close.png"));
        Dimension closeButtonSize = new Dimension(closeIcon.getIconWidth(), closeIcon.getIconHeight());
        
        JButton tabCloseButton = new JButton(closeIcon);
        tabCloseButton.setPreferredSize(closeButtonSize);
        tabCloseButton.setContentAreaFilled(false);
        tabCloseButton.setFocusable(false);
        tabCloseButton.setBorderPainted(false);
        tabCloseButton.setRolloverEnabled(true); // turn on before rollovers work
        tabCloseButton.setRolloverIcon(new ImageIcon(this.getClass().getResource("/com/jsql/view/images/closeRollover.png")));
        tabCloseButton.setPressedIcon(new ImageIcon(this.getClass().getResource("/com/jsql/view/images/closePressed.png")));
        tabCloseButton.addMouseListener(this);
        
        this.add(tabCloseButton);
    }
    
    /**
     * Action for close button: remove tab
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e))return;
        int closeTabNumber = GUIMediator.right().indexOfTabComponent(TabHeader.this);
        GUIMediator.right().removeTabAt(closeTabNumber);
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}
