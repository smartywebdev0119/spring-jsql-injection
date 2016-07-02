package com.jsql.view.swing.radio;

import java.awt.event.MouseEvent;

import com.jsql.model.MediatorModel;

/**
 * Mouse adapter for radio link effect (hover and click).
 */
public class RadioStrategyMouseAdapter extends RadioMethodMouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
        if (MediatorModel.model().isProcessFinished) {
            super.mouseClicked(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (MediatorModel.model().isProcessFinished) {
            super.mouseEntered(e);
        }
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        if (MediatorModel.model().isProcessFinished) {
            super.mouseExited(e);
        }
    }
}