/*******************************************************************************
 * Copyhacked (H) 2012-2016.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.tree.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.jsql.i18n.I18n;
import com.jsql.model.bean.database.AbstractElementDatabase;
import com.jsql.model.suspendable.AbstractSuspendable;
import com.jsql.util.StringUtil;
import com.jsql.util.ThreadUtil;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.tree.ActionLoadStop;
import com.jsql.view.swing.tree.ActionPauseUnpause;
import com.jsql.view.swing.tree.ImageObserverAnimated;
import com.jsql.view.swing.tree.ImageOverlap;
import com.jsql.view.swing.tree.PanelNode;

/**
 * Model adding functional layer to the node ; used by renderer and editor.
 */
public abstract class AbstractNodeModel {
	
    /**
     * Element from injection model in a linked list.
     */
    private AbstractElementDatabase elementDatabase;

    /**
     * Text for empty node.
     */
    private String emptyObject;

    /**
     * Current item injection progress regarding total number of elements.
     */
    private int indexProgress = 0;

    /**
     * Used by checkbox node ; true if checkbox is checked, false otherwise.
     */
    private boolean isSelected = false;

    /**
     * Indicates if process on current node is running.
     */
    private boolean isRunning = false;

    /**
     * True if current table node has checkbox selected, false otherwise.
     * Used to display popup menu and block injection start if no checkbox selected.
     */
    private boolean isContainingSelection = false;

    /**
     * True if current node has already been filled, false otherwise.
     * Used to display correct popup menu and block injection start if already done.
     */
    private boolean isLoaded = false;

    /**
     * True if current node is loading with unknown total number, false otherwise.
     * Used to display gif loader.
     */
    private boolean isProgressing = false;

    /**
     * True if current node is loading with total number known, false otherwise.
     * Used to display progress bar.
     */
    private boolean isLoading = false;
    
    private PanelNode panel;

    private boolean isEdited;

    /**
     * Create a functional model for tree node.
     * @param elementDatabase Database structural component
     */
    public AbstractNodeModel(AbstractElementDatabase elementDatabase) {
        this.elementDatabase = elementDatabase;
    }

    /**
     * Create an empty model for tree node.
     * @param emptyObject Empty tree default node
     */
    public AbstractNodeModel(String emptyObject) {
        this.emptyObject = emptyObject;
    }

    /**
     * Get the database parent of current node.
     * @return Parent
     */
    protected AbstractElementDatabase getParent() {
        return this.elementDatabase.getParent();
    }

    /**
     * Display a popup menu for a database or table node.
     * @param currentTableNode Current node
     * @param path Path of current node
     * @param x Popup menu x mouse coordinate
     * @param y Popup menu y mouse coordinate
     */
    public void showPopup(DefaultMutableTreeNode currentTableNode, TreePath path, MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        AbstractSuspendable<?> suspendableTask = ThreadUtil.get(this.elementDatabase);

        JMenuItem mnLoad = new JMenuItem(
            this.isRunning
                ? I18n.valueByKey("THREAD_STOP")
                : I18n.valueByKey("THREAD_LOAD"),
            'o'
        );
        mnLoad.setIcon(HelperUi.ICON_EMPTY);
        
        if (!this.isContainingSelection && !this.isRunning) {
            mnLoad.setEnabled(false);
        }
        mnLoad.addActionListener(new ActionLoadStop(this, currentTableNode));

        JMenuItem mnPause = new JMenuItem(
            // Report #133: ignore if thread not found
            suspendableTask != null && suspendableTask.isPaused()
                ? I18n.valueByKey("THREAD_RESUME")
                : I18n.valueByKey("THREAD_PAUSE"),
            's'
        );
        mnPause.setIcon(HelperUi.ICON_EMPTY);

        if (!this.isRunning) {
            mnPause.setEnabled(false);
        }
        mnPause.addActionListener(new ActionPauseUnpause(this, currentTableNode));
        
        popupMenu.add(mnLoad);
        popupMenu.add(mnPause);
        
        JMenuItem mnRestart = new JMenuItem(
            this instanceof NodeModelDatabase ? "Reload tables" :
            this instanceof NodeModelTable ? "Reload columns" : "?"
        );
        mnRestart.setIcon(HelperUi.ICON_EMPTY);

        mnRestart.setEnabled(!this.isRunning);
        mnRestart.addActionListener(actionEvent -> AbstractNodeModel.this.runAction());
        
        JMenuItem mnRename = new JMenuItem("Rename");
        mnRename.setIcon(HelperUi.ICON_EMPTY);
        
        mnRename.setEnabled(!this.isRunning);
        mnRename.addActionListener(actionEvent -> {
            AbstractNodeModel nodeModel = (AbstractNodeModel) currentTableNode.getUserObject();
            nodeModel.setIsEdited(true);
            
            AbstractNodeModel.this.getPanel().getLabel().setVisible(false);
            AbstractNodeModel.this.getPanel().getEditable().setVisible(true);
            
            MediatorGui.treeDatabase().setSelectionPath(path);
        });
        
        popupMenu.add(new JSeparator());
        popupMenu.add(mnRestart);
        popupMenu.add(mnRename);

        this.buildMenu(popupMenu, path);
        
        popupMenu.applyComponentOrientation(ComponentOrientation.getOrientation(I18n.getLocaleDefault()));

        popupMenu.show(
            MediatorGui.treeDatabase(),
            ComponentOrientation.getOrientation(I18n.getLocaleDefault()) == ComponentOrientation.RIGHT_TO_LEFT
            ? e.getX() - popupMenu.getWidth()
            : e.getX(),
            e.getY()
        );
        
        popupMenu.setLocation(
            ComponentOrientation.getOrientation(I18n.getLocaleDefault()) == ComponentOrientation.RIGHT_TO_LEFT
            ? e.getXOnScreen() - popupMenu.getWidth()
            : e.getXOnScreen(),
            e.getYOnScreen()
        );
    }
    
    /**
     * Draw the panel component based on node model.
     * @param tree
     * @param nodeRenderer
     * @param isSelected
     * @param isExpanded
     * @param isLeaf
     * @param row
     * @param hasFocus
     * @return
     */
    public Component getComponent(
        final JTree tree, Object nodeRenderer, final boolean isSelected, boolean isLeaf, boolean hasFocus
    ) {

        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeRenderer;
        panel = new PanelNode(tree, currentNode);

        panel.getLabel().setText(StringUtil.detectUtf8Html(this.toString()));
        panel.getLabel().setVisible(true);
        panel.showIcon();

        panel.setIcon(this.getLeafIcon(isLeaf));
        
        AbstractNodeModel nodeModel = (AbstractNodeModel) currentNode.getUserObject();
        
        if (StringUtil.isUtf8(this.getElementDatabase().toString())) {
            panel.getEditable().setFont(HelperUi.FONT_MONOSPACE);            
        } else {
            panel.getEditable().setFont(HelperUi.FONT_SEGOE);            
        }

        
        panel.getEditable().setText(StringUtil.detectUtf8(this.getElementDatabase().toString()));
        panel.getEditable().setVisible(nodeModel.isEdited);
        panel.getLabel().setVisible(!nodeModel.isEdited);

        if (isSelected) {
            if (hasFocus) {
                panel.getLabel().setBackground(HelperUi.COLOR_FOCUS_GAINED);
                panel.getLabel().setBorder(HelperUi.BORDER_FOCUS_GAINED);
            } else {
                panel.getLabel().setBackground(HelperUi.COLOR_FOCUS_LOST);
                panel.getLabel().setBorder(HelperUi.BORDER_FOCUS_LOST);
            }
        } else {
            panel.getLabel().setBackground(Color.WHITE);
            panel.getLabel().setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
        
        if (this.isLoading) {
            this.displayProgress(panel, currentNode);
            panel.hideIcon();
        } else if (this.isProgressing) {
            panel.showLoader();
            panel.hideIcon();

            AbstractSuspendable<?> suspendableTask = ThreadUtil.get(this.elementDatabase);
            if (suspendableTask != null && suspendableTask.isPaused()) {
                ImageIcon animatedGIFPaused = new ImageOverlap(HelperUi.PATH_PROGRESSBAR, HelperUi.PATH_PAUSE);
                animatedGIFPaused.setImageObserver(
                    new ImageObserverAnimated(
                        MediatorGui.treeDatabase(),
                        currentNode
                    )
                );
                panel.setLoaderIcon(animatedGIFPaused);
            }
        }
        
        return panel;
    }
    
    /**
     * Update progressbar ; dispay the pause icon if node is paused.
     * @param panel Panel that contains the bar to update
     * @param currentNode Functional node model object
     */
    protected void displayProgress(PanelNode panel, DefaultMutableTreeNode currentNode) {
        int dataCount = this.elementDatabase.getChildCount();
        panel.getProgressBar().setMaximum(dataCount);
        panel.getProgressBar().setValue(this.indexProgress);
        panel.getProgressBar().setVisible(true);
        
        // Report #135: ignore if thread not found
        AbstractSuspendable<?> suspendableTask = ThreadUtil.get(this.elementDatabase);
        if (suspendableTask != null && suspendableTask.isPaused()) {
            panel.getProgressBar().pause();
        }
    }
    
    /**
     * Display a popupmenu on mouse right click if needed.
     * @param tablePopupMenu Menu to display
     * @param path Treepath of current node
     */
    protected abstract void buildMenu(JPopupMenu tablePopupMenu, TreePath path);
    
    /**
     * Check if menu should be opened.
     * i.e: does not show menu on database except during injection.
     * @return True if popupup should be opened, false otherwise
     */
    public abstract boolean isPopupDisplayable();
    
    /**
     * Get icon displayed next to the node text.
     * @param isLeaf True will display an arrow icon, false won't
     * @return Icon to display
     */
    protected abstract Icon getLeafIcon(boolean isLeaf);
    
    /**
     * Run injection process (see GUIMediator.model().dao).
     * Used by database and table nodes.
     */
    public abstract void runAction();
    
    @Override
    public String toString() {
        return this.elementDatabase != null ? this.elementDatabase.getLabelCount() : this.emptyObject;
    }
    
    // Getter and setter

    public AbstractElementDatabase getElementDatabase() {
        return this.elementDatabase;
    }

    public int getIndexProgress() {
        return this.indexProgress;
    }

    public void setIndexProgress(int indexProgress) {
        this.indexProgress = indexProgress;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isContainingSelection() {
        return this.isContainingSelection;
    }

    public void setContainingSelection(boolean isContainingSelection) {
        this.isContainingSelection = isContainingSelection;
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public boolean isProgressing() {
        return this.isProgressing;
    }

    public void setProgressing(boolean isProgressing) {
        this.isProgressing = isProgressing;
    }

    public boolean isLoading() {
        return this.isLoading;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public PanelNode getPanel() {
        return panel;
    }

    public void setIsEdited(boolean b) {
        this.isEdited = b;
    }
    
}
