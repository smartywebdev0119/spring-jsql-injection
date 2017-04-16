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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.jsql.i18n.I18n;
import com.jsql.model.accessible.DataAccess;
import com.jsql.model.bean.database.Table;
import com.jsql.model.suspendable.AbstractSuspendable;
import com.jsql.util.ThreadUtil;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.tree.ImageObserverAnimated;
import com.jsql.view.swing.tree.ImageOverlap;
import com.jsql.view.swing.tree.PanelNode;

/**
 * Table model displaying the table icon on the label.
 */
public class NodeModelTable extends AbstractNodeModel {
	
    /**
     * Node as a table model.
     * @param table Element table coming from model
     */
    public NodeModelTable(Table table) {
        super(table);
    }

    @Override
    protected Icon getLeafIcon(boolean leaf) {
        if (leaf) {
            return HelperUi.ICON_TABLE_GO;
        } else {
            return HelperUi.ICON_TABLE;
        }
    }

    @Override
    protected void displayProgress(PanelNode panel, DefaultMutableTreeNode currentNode) {
        if ("information_schema".equals(this.getParent().toString())) {
            panel.showLoader();
            
            AbstractSuspendable<?> suspendableTask = ThreadUtil.get(this.getElementDatabase());
            if (suspendableTask != null && suspendableTask.isPaused()) {
                ImageIcon animatedGifPaused = new ImageOverlap(HelperUi.PATH_PROGRESSBAR, HelperUi.PATH_PAUSE);
                animatedGifPaused.setImageObserver(new ImageObserverAnimated(MediatorGui.treeDatabase(), currentNode));
                panel.setLoaderIcon(animatedGifPaused);
            }
        } else {
            super.displayProgress(panel, currentNode);
        }
    }

    @Override
    public void runAction() {
        final Table selectedTable = (Table) this.getElementDatabase();
        
        if (!this.isRunning()) {
            MediatorGui.frame().getTreeNodeModels().get(this.getElementDatabase()).removeAllChildren();
            DefaultTreeModel treeModel = (DefaultTreeModel) MediatorGui.treeDatabase().getModel();
            treeModel.reload(MediatorGui.frame().getTreeNodeModels().get(this.getElementDatabase()));
            
            new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {
                	Thread.currentThread().setName("SwingWorkerNodeModelTable");
                    return DataAccess.listColumns(selectedTable);
                }
                
            }.execute();
            
            this.setRunning(true);
        }
    }

    @Override
    protected void buildMenu(JPopupMenu tablePopupMenu, final TreePath path) {
        JMenuItem menuItemCheckAll = new JMenuItem(I18n.valueByKey("COLUMNS_CHECK_ALL"), 'C');
        I18n.addComponentForKey("COLUMNS_CHECK_ALL", menuItemCheckAll);
        JMenuItem menuItemUncheckAll = new JMenuItem(I18n.valueByKey("COLUMNS_UNCHECK_ALL"), 'U');
        I18n.addComponentForKey("COLUMNS_UNCHECK_ALL", menuItemUncheckAll);

        menuItemCheckAll.setIcon(HelperUi.ICON_EMPTY);
        menuItemUncheckAll.setIcon(HelperUi.ICON_EMPTY);

        if (!this.isLoaded()) {
            menuItemCheckAll.setEnabled(false);
            menuItemUncheckAll.setEnabled(false);
        }

        class ActionCheckbox implements ActionListener {
            private boolean isCheckboxesSelected;
            
            ActionCheckbox(boolean isCheckboxesSelected) {
                this.isCheckboxesSelected = isCheckboxesSelected;
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
                final DefaultMutableTreeNode currentTableNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                final AbstractNodeModel currentTableModel = (AbstractNodeModel) currentTableNode.getUserObject();
                
                DefaultTreeModel treeModel = (DefaultTreeModel) MediatorGui.treeDatabase().getModel();

                int tableChildCount = treeModel.getChildCount(currentTableNode);
                for (int i = 0 ; i < tableChildCount ; i++) {
                    DefaultMutableTreeNode currentChild = (DefaultMutableTreeNode) treeModel.getChild(currentTableNode, i);
                    if (currentChild.getUserObject() instanceof AbstractNodeModel) {
                        AbstractNodeModel columnTreeNodeModel = (AbstractNodeModel) currentChild.getUserObject();
                        columnTreeNodeModel.setSelected(this.isCheckboxesSelected);
                        currentTableModel.setContainingSelection(this.isCheckboxesSelected);
                    }
                }

                treeModel.nodeChanged(currentTableNode);
            }
        }

        class ActionCheckAll extends ActionCheckbox {
            ActionCheckAll() {
                super(true);
            }
        }

        class ActionUncheckAll extends ActionCheckbox {
            ActionUncheckAll() {
                super(false);
            }
        }

        menuItemCheckAll.addActionListener(new ActionCheckAll());
        menuItemUncheckAll.addActionListener(new ActionUncheckAll());

        menuItemCheckAll.setIcon(HelperUi.ICON_EMPTY);
        menuItemUncheckAll.setIcon(HelperUi.ICON_EMPTY);

        tablePopupMenu.add(new JSeparator());
        tablePopupMenu.add(menuItemCheckAll);
        tablePopupMenu.add(menuItemUncheckAll);
    }
    
    @Override
    public boolean isPopupDisplayable() {
        return this.isLoaded() || !this.isLoaded() && this.isRunning();
    }
    
}
