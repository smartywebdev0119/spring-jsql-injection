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
package com.jsql.view.swing.interaction;

import com.jsql.model.MediatorModel;
import com.jsql.model.injection.strategy.AbstractStrategy;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.MediatorGui;

/**
 * Mark the injection as invulnerable to a normal injection.
 */
public class MarkNormalInvulnerable implements InteractionCommand {
    
    /**
     * @param interactionParams
     */
    public MarkNormalInvulnerable(Object[] interactionParams) {
        // Do nothing
    }

    @Override
    public void execute() {
        
        AbstractStrategy strategy = MediatorModel.model().getMediatorStrategy().getNormal();
        
        MediatorGui.panelAddressBar().getAddressMenuBar().markStrategyInvulnerable(strategy);
    }
}
