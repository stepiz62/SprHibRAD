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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JComboBox;

/**
 * The class performs role management from the point of you of the Application
 * design, that is management of access permissions of various area of the
 * application in terms of roles. Here also a user management is provided (this
 * class serves the main multi-panel) similar to that one available in the
 * running (generated or hand-written) SpriHibRad application where only role
 * attribution to users is possible (no area access permission to roles, instead) .
 */

public class Umanager {
	SprHibRadGen app = SprHibRadGen.app;
	HashMap<String, JComboBox> names =  new HashMap<String, JComboBox>();
	private boolean userRolesLoading;
	public Umanager() {
		names.put("roleTable"      , app.cmbRolesTable		);
        names.put("role"           , app.cmbRoleColumn		);
	    names.put("usersRoleTable" , app.cmbUserRolesTable	);
	    names.put("roleFk"         , app.cmbRoleFk			);
	    names.put("userFk"         , app.cmbUserFk			);
	    names.put("usersTable"     , app.cmbUsersTable		);
	    names.put("username"       , app.cmbUserColumn		);
	    names.put("password"       , app.cmbPasswordColumn	);
	}
    String get(String name) {
    	String value = (String) names.get(name).getSelectedItem();
    	if (value == null)
    		app.warningMsg("Select a value in the combobox '" + name + '"');
    	return value;
    }
                           
	private ResultSet qryResult(String sql) {
		ResultSet result = null;
		try {
			result = app.conn.createStatement().executeQuery(sql);					
		} catch (SQLException e) {
			app.outToConsole(e);
		}
		return result;
	}

	private boolean nextRec(ResultSet set) {
		boolean retVal = false;
		try {
			return set.next();
		} catch (SQLException e) {
			app.outToConsole(e);
		}
		return retVal;
	}

	private String getStrValue(ResultSet set) {
		String retVal = null;
		try {
			return set.getString(1);
		} catch (SQLException e) {
			app.outToConsole(e);
		}
		return retVal;
	}

	private int getIntValue(ResultSet set) {
		int retVal = 0;
		try {
			return set.getInt(1);
		} catch (SQLException e) {
			app.outToConsole(e);
		}
		return retVal;
	}
	
	void loadUsers() {
		ResultSet result = qryResult("Select " + get("username") + " From " + get("usersTable"));
		app.lstUsers.clear();
		while (nextRec(result))
			app.lstUsers.add(getStrValue(result));			
	}

	void loadUserRoles(String user) {
		userRolesLoading = true;
		ResultSet result = qryResult("Select " + get("role") + " From " + get("roleTable") + 
										" inner join " + get("usersRoleTable") + role_userRoles_Join() + 
										" inner join " + get("usersTable") + user_userRoles_Join() + 
										whereUser(user));
		app.lstUsersRoles.clear();
		while (nextRec(result))
			app.lstUsersRoles.add(getStrValue(result));
		userRolesLoading = false;
	}
	
	
	protected void createUser(String username, String password) {
		if (username.isEmpty() || password.isEmpty()) 
			app.warningMsg("Fill in the fields !");
		else {			
			if (password.length() < app.minPwdLen) 
				app.warningMsg("Password must be at least " + app.minPwdLen + " characters long !");
			else {			
				ResultSet result = qryResult(queryUser(username, false));
				if (nextRec(result))
					app.warningMsg("User already exists !");
				else {
					String databaseName = null;
					try {
						databaseName = app.conn.getCatalog();
					} catch (SQLException e) {
						app.outToConsole(e);
					}
					if (databaseName != null) {
						executeSql("Insert into " + get("usersTable") + "(" + get("username") + "," + get("password") + ") Values ('" + username + "','" + app.passwordEncoder.encode(password) + "')");
						app.lstUsers.repaint();
						try {
							executeSql(String.format("Create USER %s IDENTIFIED BY '%s'", username, password), false);
						} catch (Exception e) {
							app.warningMsg("May be the user is already defined in the dbms (try to remove it and add it again) or check the grants of the user " + app.userName + " !");
						}
						if (app.chkAllDbPrivilegies.isSelected())
							executeSql(String.format("GRANT ALL PRIVILEGES ON %s.* TO '%s'@'%%'  WITH GRANT OPTION", databaseName, username));
						else
							executeSql(String.format("GRANT SELECT, INSERT, UPDATE, DELETE  ON %s.* TO '%s'@'%%'", databaseName, username));
					}
					loadUsers();
					app.lstUsers.list.setSelectedValue(username, true);
					
				}
			}
		}
	}

	protected boolean deleteUser(String user) {
		boolean retVal = user != null && app.yesNoQuestion("Do you really want to delete the user '" + user + "' ?");
		if (retVal) {
			executeSql(String.format("DROP USER '%s'@'%%'", user));
			executeSql("Delete " + get("usersRoleTable") + ".* From " + get("usersRoleTable") + " inner join " + get("usersTable") + user_userRoles_Join() + whereUser(user));
			executeSql("Delete From " + get("usersTable") + whereUser(user));
		}
		return retVal;
	}

	protected boolean deleteUserRole(String userRole) {
		boolean retVal = userRole != null;
		if (retVal) 
			executeSql("Delete " + get("usersRoleTable") + ".* From " + get("usersRoleTable") + " inner join " + get("roleTable") + role_userRoles_Join() + 
					whereRole(userRole));		
		return retVal;	
	}

	protected int getId(String sql) {
		int retVal = 0;
		ResultSet result = qryResult(sql);
		if (nextRec(result))
			retVal = getIntValue(result);		
		return retVal;
	}
	
	protected void addUserRole(String userRole) {
		if (userRolesLoading)
			return;
		int userId = getId( queryUser((String) app.lstUsers.list.getSelectedValue(), true));
		int roleId = getId( "Select id From " + get("roleTable") + whereRole(userRole));
		executeSql("Insert into " + get("usersRoleTable") + "(" + get("userFk") + "," + get("roleFk") + ") Values (" + userId + "," + roleId + ")");	
	}

	protected String queryUser(String username, boolean iden) {
		return "Select " + (iden ? "id" : get("username")) + " From " + get("usersTable") + whereUser(username);
	}

	protected String queryRoles() {
		if (get("role") == null || get("roleTable") == null)
			return null;
		else
			return "Select " + get("role") + " From " + get("roleTable");
	}

	protected String whereUser(String user) {
		return " where " + get("usersTable") + "." + get("username") + " = '" + user + "'";
	}

	protected String whereRole(String role) {
		return " where " + get("roleTable") + "." + get("role") + " = '" + role + "'";
	}

	protected String user_userRoles_Join() {
		return " On " + get("usersTable") + ".id = " + get("usersRoleTable") + "." + get("userFk");
	}
	
	protected String role_userRoles_Join() {
		return " On " + get("roleTable") + ".id = " + get("usersRoleTable") + "." + get("roleFk");
	}

	private void executeSql(String sql) {
		try {
			executeSql(sql, true);
		} catch (Exception e) {
		}
	}
	
	private void executeSql(String sql, boolean manageException) throws Exception {
		PreparedStatement stmnt;
		try {
			stmnt = app.conn.prepareStatement(sql);
			stmnt.executeUpdate();
		} catch (SQLException e) {
			if (manageException)
				app.outToConsole(e);
			else
				throw new Exception(e);
		}
	}
	
	public void addRole() {
		String addingRole = app.txtRoleName.getText();
		if (!addingRole.isEmpty()) {
			ResultSet result = qryResult(queryRoles() + whereRole(addingRole));
			if (nextRec(result))
				app.warningMsg("Role already exists !");
			else
				executeSql("Insert into " + get("roleTable") + "(" + get("role") + ") Values ('" + addingRole + "')");
			loadRoles();
		}
	}
	
	public void loadRoles() {
		String query = queryRoles();
		if (query != null) {
			ResultSet result = qryResult(queryRoles());
			app.lstRoleNames.clear();
			while (nextRec(result))
				app.lstRoleNames.add(getStrValue(result));	
		}
	}
	public boolean removeRole(String selectedItem) {
		boolean retVal = selectedItem != null;
		if (retVal) 
			executeSql("Delete " + get("roleTable") + ".* From " + get("roleTable") + whereRole(selectedItem));		
		return retVal;	
	}
	

}
