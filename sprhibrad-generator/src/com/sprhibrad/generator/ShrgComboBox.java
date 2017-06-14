/*
	Copyright (c) 2017, Stefano Pizzocaro. All rights reserved. Use is subject to license terms.

	This file is part of SprHibRAD 1.0.

	SprHibRAD 1.0 is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	SprHibRAD 1.0 is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public License
	along with SprHibRAD 1.0.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sprhibrad.generator;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import java.util.function.BiConsumer;

import javax.swing.JComboBox;

import org.json.simple.JSONObject;

/**
 * One of the gui components that takes a seat in Json tree project. It may be
 * made to identify a sub-context of definition, that is, it may collaborate to
 * identify the path of group of an attributes or sets of values within the Json tree. 
 * In other words it can impersonate a "selector" of context, in order to have data displayed or stored.
 */

public class ShrgComboBox extends JComboBox<String>   {

	private ShrgObject shrgObject;

	private Vector<Selector> selectors =  new Vector<Selector>();;
	boolean changing;
	Vector<ShrgComboBox> clones =  new Vector<ShrgComboBox>();
	private ShrgObject shrgObjectForFeeding;
	private Vector<ShrgObject> projectors =  new Vector<ShrgObject>();

	private boolean feeding;

	private boolean projecting;

	private ActionPerformer actionPerformer;

	private String property;
	
	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public void setActionPerformer(ActionPerformer actionPerformer) {
		this.actionPerformer = actionPerformer;
	}

	public ShrgComboBox() {
		super();
		addActionListener(this);
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_CANCEL) {
					if (actionPerformer != null)
						actionPerformer.remove((String) getSelectedItem());
					setSelectedItem(null);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	public void addProjector(ShrgObject projector) {
		projectors.add(projector);
	}

	private void fireProjections() {
		for (ShrgObject projector : projectors) 
			projector.set(true, true);
	}

	public void enable(Boolean truth) {
		setEnabled(truth);
	}
	
	public ShrgObject getShrgObject() {
		return shrgObject;
	}
	
	public ShrgObject getShrgObject(Boolean forFeeding) {
		return forFeeding ? shrgObjectForFeeding : shrgObject;
	}

	public void setShrgObject(ShrgObject shrgObject) {
		this.shrgObject = shrgObject;
	}

	public void setShrgObject(ShrgObject shrgObject, Boolean forFeeding) {
		if (forFeeding)
			shrgObjectForFeeding = shrgObject;
		else
			setShrgObject(shrgObject);
	}
	
	public void addSelector(Selector selector) {
		this.selectors.add(selector);
	}

	
	public void addClone(ShrgComboBox clone) {
		clones.add(clone);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().compareTo("comboBoxChanged") == 0 && ! isFeeding()) {	
			if ( ! projecting) {
				String selection = (String) getSelectedItem();
				changing = true;
				if (selectors.size() > 0)
					SprHibRadGen.app.changingSelectors ++;
				for (Selector selector : selectors)
					selector.setAttr(selection);
				if (shrgObject != null) {
					Object obj = shrgObject.getGuiObject().getObject();
					if (shrgObject != null &&  (! (obj instanceof ShrgList) || ! ((ShrgList) obj).changing) && ! projecting && ! SprHibRadGen.app.metadataLoading)
						shrgObject.set(false);
				}
				fireProjections();
				if (actionPerformer != null) {
					actionPerformer.handle(selection);
					actionPerformer.add(selection);
				}
				changing = false;
				if (selectors.size() > 0)
					SprHibRadGen.app.changingSelectors --;
			}
		} 
		super.actionPerformed(e);
	}


	@Override
	public void addItem(String item) {
		if ( ! changing) {
			super.addItem(item);
			for (ShrgComboBox cmb : clones)
				cmb.addItem(item);
		}
	}

	@Override
	public void removeItemAt(int anIndex) {
		if ( ! changing) {
			super.removeItemAt(anIndex);
			for (ShrgComboBox cmb : clones)
				cmb.removeItemAt(anIndex);		}
	}

	@Override
	public void removeAllItems() {
		removeAllItems(false);
	}
	
	public void removeAllItems(boolean forProjection) {
		if (forProjection) {
			projecting = true;
			SprHibRadGen.app.clearingOnProjection = true;
		}
		if ( ! changing) {
			super.removeAllItems();
			for (ShrgComboBox cmb : clones)
				cmb.removeAllItems(forProjection);		
		}
		if (forProjection) {
			projecting = false;
			SprHibRadGen.app.clearingOnProjection = false;
		}
	}
	
	public void clear() {
		clear(false);
	}
	
	public void clear(Boolean forFeeding) {
		if (forFeeding) {
			setFeeding(true);
			removeAllItems();
			setFeeding(false);
		} else {
			setSelectedIndex(-1);
			for (ShrgComboBox cmb : clones)
				cmb.clear(false);	
		}
	}
	
	public interface FeedingFilter {
		boolean allow(String termKey, JSONObject termObj, ShrgComboBox theCombo); 
	}
	
	FeedingFilter feedingFilter;
	
	public void setFeedingFilter(FeedingFilter feedingFilter) {
		this.feedingFilter = feedingFilter;
	}

	public void set(JSONObject jsonObj, Boolean toGui) {
		if (toGui) {
			Object value =  jsonObj.get("value");
			if (jsonObj.containsKey("value"))
				setSelectedItem((String) value);
			else {
				setFeeding(true);
				jsonObj.forEach (new BiConsumer<String, JSONObject>() {
					@Override
					public void accept(String key, JSONObject termObj) {
						if (feedingFilter == null || feedingFilter.allow(key, termObj, ShrgComboBox.this))
							addItem(key);
					}		
				});
				setFeeding(false);
		}
	} else
			jsonObj.put("value", getSelectedItem());
	}
	
	public void set(ShrgJSONArray jsonArray, Boolean toGui) {
		if (toGui) {
			setFeeding(true);
			for (Object obj : jsonArray)
				addItem(jsonArray.getKey((JSONObject) obj));
			setFeeding(false);
		}
	}

	@Override
	public void removeItem(Object anObject) {
		if (actionPerformer != null && ! changing)
			actionPerformer.remove((String) anObject);
		super.removeItem(anObject);
	}

	public boolean isFeeding() {
		return feeding;
	}

	public void setFeeding(boolean feeding) {
		this.feeding = feeding;
	}
	

}
