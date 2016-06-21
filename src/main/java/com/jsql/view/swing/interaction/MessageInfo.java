/*******************************************************************************
 * Copyhacked (H) 2012-2014.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.interaction;

import com.jsql.model.injection.MediatorModel;
import com.jsql.view.swing.MediatorGui;

/**
 * Update the general information in status bar.
 */
public class MessageInfo implements InteractionCommand {
    /**
     * @param nullParam
     */
    public MessageInfo(Object[] nullParam) {
        // Do nothing
    }

    @Override
    public void execute() {
        MediatorGui.panelStatus().setInfos(
            MediatorModel.model().versionDatabase,
            MediatorModel.model().currentDatabase,
            MediatorModel.model().currentUser,
            MediatorModel.model().authenticatedUser
        );
    }
}
