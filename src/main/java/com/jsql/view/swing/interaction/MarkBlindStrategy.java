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
package com.jsql.view.swing.interaction;

import com.jsql.model.injection.strategy.Strategy;
import com.jsql.view.swing.MediatorGui;

/**
 * Mark the injection as invulnerable to a blind injection.
 */
public class MarkBlindStrategy implements InteractionCommand {
    /**
     * @param interactionParams
     */
    public MarkBlindStrategy(Object[] interactionParams) {
        // Do nothing
    }

    @Override
    public void execute() {
        MediatorGui.managerDatabase().panelStrategy.setEnabled(true);
        MediatorGui.managerDatabase().panelStrategy.setText(Strategy.BLIND.toString());
        for (int i = 0 ; i < MediatorGui.managerDatabase().panelStrategy.getItemCount() ; i++) {
            if (MediatorGui.managerDatabase().panelStrategy.getItem(i).getText().equals(Strategy.BLIND.toString())) {
                MediatorGui.managerDatabase().panelStrategy.getItem(i).setSelected(true);
                break;
            }
        }
    }
}
