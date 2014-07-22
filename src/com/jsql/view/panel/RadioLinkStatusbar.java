package com.jsql.view.panel;

import java.awt.font.TextAttribute;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.jsql.view.GUIMediator;
import com.jsql.view.GUITools;

@SuppressWarnings("serial")
public class RadioLinkStatusbar extends RadioLink{
	
	static ArrayList<JLabel> group = new ArrayList<JLabel>();

	public RadioLinkStatusbar(String string) {
		super(string);
		init();
	}

	public RadioLinkStatusbar(String string, boolean isSelected) {
		super(string, isSelected);
		init();
	}
	
	private void init(){
		group.add(this);
		this.setHorizontalAlignment(SwingConstants.LEFT);
	}

	@Override
	void action() {
		GUIMediator.model().applyStrategy(RadioLinkStatusbar.this.getText());
	}

	@Override
	ArrayList<JLabel> getGroup() {
		return group;
	}
	
	@Override
	protected boolean isActivable() {
		return RadioLinkStatusbar.this.getIcon() == GUITools.TICK && 
				!RadioLinkStatusbar.this.getFont().getAttributes().containsValue(TextAttribute.WEIGHT_BOLD);
	}
}