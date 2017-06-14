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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.function.BiConsumer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.simple.JSONObject;

import com.sprhibrad.generator.SprHibRadGen.ListTarget;

/**
 * The most important gui object that hosts either a list of tools available to
 * be pushed on the project structure lists, or, right the latters, a container
 * that enumerates something specific to the project being designed; in this
 * case the order in which the items are listed may have crucial impact on the
 * resulting project or no impact: the list will be associated with a JSONArray
 * or a JSONObject respectively.
 */
public class ShrgList extends JScrollPane {

	interface Projection {
		void project(String key, JSONObject obj);

		void clear();
	}

	private ActionPerformer actionPerformer;
	
	public void setActionPerformer(ActionPerformer actionPerformer) {
		this.actionPerformer = actionPerformer;
	}

	JList list;
	protected DefaultListModel<Object> model;
	protected ListSelectionModel selectionModel;
	int currentSelectionIdx;
	ListTarget[] targets;
	private ShrgObject shrgObject;
	SprHibRadGen app = SprHibRadGen.app;	
	Projection projection;

	private Vector<Selector> selectors =  new Vector<Selector>();
	private Vector<ShrgObject> projectors =  new Vector<ShrgObject>();
	
	boolean unmodifiable;
	ShrgList clone;
	boolean changing;
	private String property; // debug helper
	
	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	public void setClone(ShrgList clone) {
		this.clone = clone;
	}

	public void setProjection(Projection projection) {
		this.projection = projection;
	}

	public void addSelector(Selector selector) {
		selectors.add(selector);
	}
	
	public void addProjector(ShrgObject projector) {
		projectors.add(projector);
	}
	
	private void fireProjections() {
		for (ShrgObject projector : projectors) 
			projector.set(true, true);
	}

	public void setUnmodifiable() {
		unmodifiable = true;
	}

	public void setShrgObject(ShrgObject shrgObject) {
		this.shrgObject = shrgObject;
	}

	public ShrgObject getShrgObject() {
		return shrgObject;
	}

	public void setTarget(ListTarget[] listTargets) {
		this.targets = listTargets;
		ButtonGroup group = new ButtonGroup();	
		for(ListTarget target : listTargets)
			group.add(target.getRadio());
	}

	ListTarget getTarget() {
		ListTarget retVal = null;
		if (targets != null) {
			for (ListTarget elem :  targets) {
				if (elem.getRadio() == null || elem.getRadio().isSelected()) {
					retVal = elem;
					break;
				}
			}
		}
		return retVal;
	}

	public ShrgList() {
		super();
		list = new JList();
		setViewportView(list);
		model = new DefaultListModel<Object>();
		list.setModel(model);	
		list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!e.isConsumed()) {
					if (e.getClickCount() == 2)
						doubleClick(e);
					e.consume();
				}
			}
		});
		selectionModel = list.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					currentSelectionIdx = selectionModel.getLeadSelectionIndex();
					String selection = (String) list.getSelectedValue();
					if (actionPerformer != null)
						actionPerformer.handle(selection);
					if (selectors.size() > 0)
						SprHibRadGen.app.changingSelectors ++;
					for (Selector selector : selectors)
						selector.setAttr(selection);
					if ( ! unmodifiable)
						integrateAction();
					if (selectors.size() > 0)
						SprHibRadGen.app.changingSelectors --;
				}
			}
		});
		list.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_CANCEL) 
					if (!unmodifiable) {
						boolean doIt = true;
						if (actionPerformer != null)
							doIt = actionPerformer.remove((String) list.getSelectedValue());
						if (doIt)
							remove(currentSelectionIdx);
					}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}
	
	public void doubleClick(MouseEvent e) {
		ListTarget target = getTarget();
		if (target == null && targets != null && targets.length > 0)
			app.warningMsg("Make a selection of the target list !");
		else if (target != null ) 
			if (target.getList().list.isEnabled()) {
				String selectedVal = (String) list.getSelectedValue();
				boolean doAdd = selectedVal != null;				
				if (doAdd && target.directorCombo != null && target.types.size() != 0 // that is : source is pumping a field
						&& selectedVal.compareTo("id") != 0) {
					FieldDescriptor fieldDescr = app.fieldDescriptor(target.directorCombo, selectedVal);
					if (fieldDescr == null) //metadata just loaded by fieldDescriptor()
						doAdd = false;
					else { 
						if (! target.types.contains(fieldDescr.sqlType) &&  (target.enabler == null || ! target.enabler.enable(selectedVal, target.directorCombo))) {
							app.warningMsg("Type not allowed !");
							doAdd = false;
						}
					}
				}
				if (doAdd)
					target.getList().add(selectedVal);
			} else
				
				app.warningMsg("Make a parent selection in order to the target be enabled !");
	}
	
	public boolean contains(String label) {
		return model.contains(label);		
	}
	
	public void add(Object object) {
		if (model.indexOf(object) == -1) { 
			if (changing)
				return;
			else
				changing = true;
			model.addElement(object);
			list.repaint();
			if (isThereSelectorToFeed())
				selectorToFeed().addItem((String) object);
			if (clone != null)
				clone.add(object);
			if (actionPerformer != null)
				actionPerformer.add((String) object);			
			if (SprHibRadGen.app.changingSelectors == 0)
				integrateAction();
			changing = false;
		}
		fireProjections();
	}
	
	void integrateAction() {
		if (shrgObject != null)
			shrgObject.set(false);
	}
	
	boolean isThereSelectorToFeed() {
		return shrgObject != null && shrgObject.getTargetSelector() != null;
	}
	
	public void remove(int index) {
		if (changing)
			return;
		else
			changing = true;
		ListTarget target = getTarget();
		if (target != null) {
			String selection = (String) model.getElementAt(index);
			ShrgList targetList = target.getList();
			if (targetList.contains(selection))
				for (int i = 0; i < targetList.model.size(); i++)
					if (((String) targetList.model.getElementAt(i)).compareTo(selection) == 0) {
						targetList.remove(i);
						break;
					}					
		}
		model.remove(index);
		if (isThereSelectorToFeed())
			selectorToFeed().removeItemAt(index);
		if (clone != null)
			clone.remove(index);
		integrateAction();
		changing = false;
		fireProjections();
	}

	public void removeAll() {
		if (changing)
			return;
		else
			changing = true;
		model.removeAllElements();
		if (clone != null)
			clone.removeAll();
		changing = false;
		fireProjections();
	}
	
	ShrgComboBox selectorToFeed() {
		return shrgObject.getTargetSelector();
	}

	public void clear() {
		clear(false);
	}
	
	public void clear(boolean forProjection) {
		model.clear();
		if (clone != null)
			clone.clear(forProjection);
		if (isThereSelectorToFeed())
			selectorToFeed().removeAllItems(forProjection);
	}
	
	public void set(ShrgJSONArray jsonArray, Boolean toGui) {
		if (toGui)
			for (Object obj : jsonArray)
				add(jsonArray.getKey((JSONObject) obj));
		 else {
			// check json deleting
			Vector<String> deletions = new Vector<String>();
			String key = null;
			JSONObject jsonObj;
			for (Object obj : jsonArray) {
				key = jsonArray.getKey((JSONObject)obj);
				if ( ! contains(key))
					deletions.add(key);		
			}
			for (String literal : deletions)
				jsonArray.remove(literal);
			// check json adding
			for (int i = 0; i < model.getSize(); i++) {
				key =  (String) model.get(i);
				if (jsonArray.indexOf(key)==-1)
					jsonArray.add(key);
			}		
		}
	}

	public void set(JSONObject jsonObj, Boolean toGui) {
		if (toGui) {
			jsonObj.forEach(new BiConsumer<String, JSONObject>() {
			@Override	
				public void accept(String key, JSONObject obj) {
					add(key);
				}});		
		} else {
			String key = null;
			JSONObject newNode = null;
			// check json deleting
			Vector<String> deletions = new Vector<String>();
			jsonObj.forEach(new BiConsumer<String, JSONObject>() {
			@Override
				public void accept(String key, JSONObject obj) {
					if ( ! contains(key))
						deletions.add(key);
				}});		
			for (String literal : deletions)
				jsonObj.remove(literal);
				
			// check json adding
			for (int i = 0; i < model.getSize(); i++) {
				key =  (String) model.get(i);
				if (jsonObj.get(key)==null)
					jsonObj.put(key, new JSONObject());
			}		
		}
	}
	
	public void project(ShrgJSONArray array) {
		if (SprHibRadGen.app.projection != null)
			return;
		SprHibRadGen.app.projection = projection;
		projection.clear();
		for (Object obj : array)
			if(projection != null) 
				projection.project(array.getKey((JSONObject) obj), array.getValue((JSONObject) obj));
		SprHibRadGen.app.projection = null;
	}

	public void project(JSONObject jsonObj) {
		projection.clear();
		jsonObj.forEach(new BiConsumer<String, JSONObject>() {
		@Override	
			public void accept(String key, JSONObject obj) {
				if(projection != null) 
					projection.project(key, obj);
			}});		
	}

	public void enable(Boolean truth) {
		list.setEnabled(truth);
	}

}
