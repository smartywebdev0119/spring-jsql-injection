/*******************************************************************************
 * Copyhacked (H) 2012-2020.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.tree.model;

import javax.swing.Icon;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.jsql.model.bean.database.Database;
import com.jsql.view.swing.util.MediatorHelper;
import com.jsql.view.swing.util.UiUtil;

/**
 * Database model displaying the database icon on the label.
 */
public class NodeModelDatabase extends AbstractNodeModel {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * Node as a database model.
     * @param database Element database coming from model
     */
    public NodeModelDatabase(Database database) {
        super(database);
    }

    @Override
    protected Icon getLeafIcon(boolean leaf) {
        
        if (leaf) {
            
            return UiUtil.ICON_DATABASE_GO;
            
        } else {
            
            return UiUtil.ICON_DATABASE;
        }
    }

    @Override
    public void runAction() {
        
        if (this.isRunning()) {
            return;
        }
    
        MediatorHelper.treeDatabase().getTreeNodeModels().get(this.getElementDatabase()).removeAllChildren();
        
        DefaultTreeModel treeModel = (DefaultTreeModel) MediatorHelper.treeDatabase().getModel();
        
        // Fix #90522: ArrayIndexOutOfBoundsException on reload()
        try {
            treeModel.reload(MediatorHelper.treeDatabase().getTreeNodeModels().get(this.getElementDatabase()));
            
        } catch (ArrayIndexOutOfBoundsException e) {
            
            LOGGER.error(e.getMessage(), e);
        }
        
        new SwingWorker<Object, Object>() {
            
            @Override
            protected Object doInBackground() throws Exception {
                
                Thread.currentThread().setName("SwingWorkerNodeModelDatabase");
                Database selectedDatabase = (Database) NodeModelDatabase.this.getElementDatabase();
                return MediatorHelper.model().getDataAccess().listTables(selectedDatabase);
            }
        }.execute();
        
        this.setRunning(true);
    }

    @Override
    public boolean isPopupDisplayable() {
        
        return this.isLoaded() || !this.isLoaded() && this.isRunning();
    }

    @Override
    protected void buildMenu(JPopupMenuCustomExtract tablePopupMenu, TreePath path) {
        // Do nothing
    }
}
