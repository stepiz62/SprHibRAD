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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiConsumer;

import javax.naming.NamingException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sprhibrad.generator.ShrgComboBox.FeedingFilter;
import com.sprhibrad.generator.ShrgJsonPeeker.RetVal;
import com.sprhibrad.generator.ShrgList.Projection;

/**
 * Editable with WindowBuilder, the class renders a hierarchy-structured
 * multi-panel Swing Frame.
 * 
 *  First, the class accesses the database instance to load all the metadata needed to design the generation project.
 * 
 * Together with {@link Generator}, the class is the
 * most important class of the code generation area of SprHibRAD. By means of
 * {@link #initializeGuiLogic()} it realizes the logical connection among the
 * various gui objects and maps there content with well identified paths within
 * the Json tree used to collect the Generator Project data. For that it has available the most part of helper classes of the package.
 * 
 * Beyond the basic operation about the Generator Project management it performs
 * the suited actions in response to user's actions, to keep the Json tree
 * synchronized with the gui content.
 * 
 * @see ShrgObject
 * @see Selector
 * @see ShrgGuiObject
 * @see ShrgJSONArray
 * @see ShrgJsonPeeker
 */
public class SprHibRadGen {

	PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	Map<Integer, String> jTypesMap;

	interface TargetEnabler {
		boolean enable(String fieldName, ShrgComboBox directorCombo);
	}

	class ListTarget {
		JRadioButton radio;
		ShrgList list;
		HashSet<Integer> types = new HashSet<Integer>();
		TargetEnabler enabler;
		ShrgComboBox directorCombo;

		public TargetEnabler getTargetEnabler() {
			return enabler;
		}

		public void setTargetEnabler(TargetEnabler targetEnabler) {
			this.enabler = targetEnabler;
		}

		public ListTarget(JRadioButton radio, ShrgList list) {
			this(radio, list, null, null);
		}

		public ListTarget(JRadioButton radio, ShrgList list, int[] typesArray, ShrgComboBox directorCombo) {
			this(radio, list, typesArray, directorCombo, null);
		}

		public ListTarget(JRadioButton radio, ShrgList list, int[] typesArray, ShrgComboBox directorCombo,
				TargetEnabler targetEnabler) {
			this.radio = radio;
			this.list = list;
			if (typesArray != null)
				for (int type : typesArray)
					types.add(type);
			this.enabler = targetEnabler;
			this.directorCombo = directorCombo;
		}

		public JRadioButton getRadio() {
			return radio;
		}

		public ShrgList getList() {
			return list;
		}
	}

	class OnFieldTypeEnabler implements ShrgGuiObject.EnablerOnField {
		int[] typesArray;

		public OnFieldTypeEnabler(int[] typesArray) {
			this.typesArray = typesArray;
		}

		@Override
		public boolean isToEnable(String fieldName) {
			boolean retVal = false;
			FieldDescriptor field = SprHibRadGen.app.getColInfo(fieldName);
			if (field != null)
				retVal = typeIsIn(field.sqlType, typesArray);
			return retVal;
		}
	}

	public boolean typeIsIn(int testType, int[] types) {
		boolean retVal = false;
		for (int type : types) {
			if (testType == type) {
				retVal = true;
				break;
			}
		}
		return retVal;
	}

	public static final Cursor defCursor = Cursor.getDefaultCursor();
	public static final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	private ShrgFrame frame;
	static ShrgFrame mainFrame;
	ShrgJsonWrapper confProperties;
	public ShrgJsonWrapper prjProperties;
	public ShrgJsonPeeker shrgJsonPeeker = new ShrgJsonPeeker();
	String projectPathName;
	String jdbcDriverClass;
	String connectionUrl;
	String dbName;
	String userName;
	String password;
	String targetPackage;
	Connection conn;

	public static SprHibRadGen app;
	TablesInfo tablesInfo = new TablesInfo();

	private JTextField x;
	JTextField txtjdbcDriverClass;
	JTextField txtDbPath;
	JTextField txtDbName;
	JTextField txtUserName;
	JTextField txtPassword;
	JTextField txtTargetPackage;
	private JEditorPane console;
	private JEditorPane console2;

	Selector lstFKsSelector;
	Selector cmbEntitySelector;
	Selector lstCriteriaFieldsSelector;
	Selector lstFormFieldsSelector;
	Selector lstResultFieldsSelector;
	Selector lstOrderedFieldsSelector;
	Selector lstChildrenEntitiesSelector;
	Selector lstDetailFieldsSelector;
	Selector lstDetailOrderedFieldsSelector;

	Selector cmbDetailEntitySelector;

	public ShrgObject entities;
	public ShrgObject menuEntities;
	private ShrgObject formFields;
	private ShrgObject criteriaFields;
	private ShrgObject resultFields;
	private ShrgObject orderedFields;
	private ShrgObject childrenEntities;
	private ShrgObject detailFields;
	private ShrgObject detailOrderedFields;

	private ShrgObject formFieldReadOnly;
	private ShrgObject criterionWithOp;
	private ShrgObject resultColOrdVersus;
	private ShrgObject viewProperty;
	private ShrgObject noDelete;
	private ShrgObject noAdd;
	private ShrgObject detailColOrdVersus;

	private ShrgList lstTables;
	private ShrgList lstEntities;
	private ShrgList lstMenu;
	private ShrgList lstVerboseFields;
	private ShrgList lstFormFields;
	private ShrgList lstAvailableFields;
	private ShrgList lstResultFields;
	private ShrgList lstEntities2;
	private ShrgList lstChildrenEntities;
	private ShrgList lstCriteriaFields;
	private ShrgList lstOrderedFields;
	private ShrgList lstFormFields2;
	private ShrgList lstDetailFields;
	private ShrgList lstDetailOrderedFields;

	public ShrgComboBox cmbEntity;
	private ShrgComboBox cmbOrientation1;
	public ShrgComboBox cmbDetailEntity;
	private ShrgComboBox cmbOrientation2;
	private JRadioButton radioButton;
	private JRadioButton radioButton_1;
	private ShrgCheckBox chkReadOnly;
	private ShrgCheckBox chkCurrency;
	private ShrgCheckBox chkPercent;
	private ShrgCheckBox chkBoolean;
	private ShrgCheckBox chkRadioButtons;
	private ShrgCheckBox chkWithOperator;
	private ShrgCheckBox chkViewProperty;
	private ShrgCheckBox chkNoDelete;
	private ShrgCheckBox chkNoAdd;
	private ShrgCheckBox chkVerboseLiteral;
	private ShrgCheckBox chkDocument;
	private ShrgCheckBox chkPrintable;
	private ShrgCheckBox chkValidate;
	private ShrgCheckBox chkShInitbinder;

	public JSONParser jsonParser;

	private Generator generator;

	String mainConfFile = "SprHibRadGen.properties";
	private String projectExtension = "shrg";

	boolean metadataLoaded = false;
	private ShrgObject formFieldCurrency;
	private ShrgObject formFieldPercent;
	private ShrgObject formFieldBoolean;
	private JButton btnGenerate;
	private ShrgComboBox cmbFieldTargetEntity;
	private ShrgObject fieldTargetEntity;
	private ShrgList lstFKs;
	private ShrgObject fks;
	private JButton btnSaveProject;
	private ShrgObject verboseFields;
	private ShrgObject formFields2;
	private Selector cmbDetailEntitySelectorForFieldsForm2;
	private ShrgList lstDetailsEntities;
	private ShrgObject detailsEntities;
	private JRadioButton radioButton_5;
	private JRadioButton radioButton_7;
	private JRadioButton radioButton_6;
	private ShrgComboBox cmbParentFk;
	private ShrgComboBox cmbChildFk;
	private ShrgObject parentFkcatalog;
	private Selector cmbParentFkSelector;
	private Selector lstChildrenEntitiesSelectorForFeeding;
	private ShrgObject parentFk;
	private ShrgObject childFkcatalog;
	private ShrgObject childFk;
	private ShrgObject verboseLiteral;
	private ShrgObject formFieldDocument;
	private ShrgObject formPrintable;
	private boolean initializing;
	private ShrgObject shInitbinder;
	private ShrgObject validate;
	private ShrgObject radioButtons;
	private ShrgComboBox cmbPreviewPath;
	private ShrgObject previewPath;
	public Projection projection;
	private JCheckBox chkLanguageFile;
	private int[] manageableTypes = new int[] { Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.REAL, Types.FLOAT,
			Types.DOUBLE, Types.DATE, Types.VARCHAR };
	private int[] idTypes = new int[] { Types.INTEGER };
	ShrgComboBox cmbRolesTable;
	ShrgComboBox cmbRoleColumn;
	ShrgList lstAvailableRoles;
	ShrgList lstRoles;
	private ShrgList lstAvailableAccessModes;
	private ShrgList lstRoleAccessModes;
	private ShrgObject rolesTable;
	private ShrgObject roleColumn;
	public boolean metadataLoading;
	private ShrgObject roles;
	private ShrgObject roleAccessModes;
	private Selector lstRolesSelector;
	ShrgComboBox cmbRoleFk;
	ShrgComboBox cmbUserRolesTable;
	ShrgComboBox cmbUserFk;
	ShrgComboBox cmbUsersTable;
	ShrgComboBox cmbUserColumn;
	private ShrgObject userRolesTable;
	private ShrgObject roleFk;
	private ShrgObject userFk;
	private ShrgObject usersTable;
	private ShrgObject userColumn;
	ShrgComboBox cmbPasswordColumn;
	private ShrgObject passwordColumn;
	private JTextField txtUser;
	private JTextField txtPwd;
	ShrgList lstUsers;
	ShrgList lstUsersRoles;
	private ShrgList lstAvailableRoles2;
	JCheckBox chkAllDbPrivilegies;
	Umanager uMngr;
	JTextField txtRoleName;
	ShrgList lstRoleNames;

	public int minPwdLen;

	public boolean clearingOnProjection;

	public int changingSelectors;
	JTextField txtShrAppName;
	JTextField txtHibDialect;
	JTextField txtHibShowSql;
	JTextField txtHibFormatSql;
	JTextField txtShrPageSize;
	JTextField txtShrUserprefsMenu;
	JTextField txtShrDateStyle;
	JTextField txtShrCurrencyCountry;
	JTextField txtShrLangParameterizedReports;
	JTextField txtShrMinPwdSize;
	JTextField txtBirtLogDir;
	JTextField txtSessionMaxInactiveMinutes;

	private ShrgCheckBox chkPassword;

	private ShrgObject formFieldPassword;
	JTextField txtTargetDir;
	private JButton btnTargetDir;
	private ShrgCheckBox chkClassSpecificDictio;

	private ShrgObject classSpecificDictio;

	boolean long_op_success;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SprHibRadGen window = new SprHibRadGen();
				} catch (Exception e) {
					app.outToConsole(e);
				}
			}
		});
	}

	public SprHibRadGen() {
		app = this;
		initJTypes();

		if (System.getProperty("os.name").toLowerCase().indexOf("windows") < 0) {
			Font defaultFont = new Font("Tahoma", Font.PLAIN, 11);
			if (defaultFont != null) {
				UIManager.put("Button.font", defaultFont);
				UIManager.put("RadioButton.font", defaultFont);
				UIManager.put("CheckBox.font", defaultFont);
				UIManager.put("ComboBox.font", defaultFont);
				UIManager.put("Label.font", defaultFont);
				UIManager.put("List.font", defaultFont);

				UIManager.put("Panel.font", defaultFont);
				UIManager.put("ScrollPane.font", defaultFont);
				UIManager.put("TabbedPane.font", defaultFont);
				UIManager.put("Table.font", defaultFont);
				UIManager.put("TableHeader.font", defaultFont);
				UIManager.put("TextField.font", defaultFont);
				UIManager.put("PasswordField.font", defaultFont);
			}
		}
		
		
		
		initialize();
		initializeGuiLogic();
		confProperties.load(mainConfFile);
		projectPathName = (String) confProperties.getPrpty("projectPathName");
		mainFrame = frame;
		mainFrame.setVisible(true);
		initializing = true;
		if (loadProject())
			setGui();
		else
			newProject();
		generator = new Generator();
		cmbEntity.clear();
		initializing = false;
	}

	private void initJTypes() {
		jTypesMap = new HashMap<Integer, String>();

		jTypesMap.put(Types.CHAR, "String");
		jTypesMap.put(Types.VARCHAR, "String");
		jTypesMap.put(Types.LONGVARCHAR, "String");
		jTypesMap.put(Types.FLOAT, "Float");
		jTypesMap.put(Types.DOUBLE, "Double");
		jTypesMap.put(Types.REAL, "Double");
		jTypesMap.put(Types.NUMERIC, "Double");
		jTypesMap.put(Types.BIGINT, "Double");
		jTypesMap.put(Types.INTEGER, "Long");
		jTypesMap.put(Types.SMALLINT, "Integer");
		jTypesMap.put(Types.BIT, "Boolean");
		jTypesMap.put(Types.DATE, "Date");
		jTypesMap.put(Types.TIMESTAMP, "DateTime");
		jTypesMap.put(Types.BLOB, "byte[]");
		jTypesMap.put(Types.VARBINARY, "byte[]");
		jTypesMap.put(Types.LONGVARBINARY, "byte[]");
	}

	public String javaType(FieldDescriptor field) {
		return jTypesMap.get(field.sqlType);
	}

	public void warningMsg(String text) {
		JOptionPane.showMessageDialog(null, text, "SprHibRAD Generator", JOptionPane.WARNING_MESSAGE, null);

	}

	public boolean yesNoQuestion(String text) {
		return JOptionPane.showConfirmDialog(app.mainFrame, text, "SprHibRAD Generator", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null) == JOptionPane.YES_OPTION;
	}

	public boolean checkDatum(JTextField textBox) {
		return !textBox.getText().isEmpty();
	}

	public boolean checkMainData() {
		boolean retVal = checkDatum(x) && checkDatum(txtjdbcDriverClass) && checkDatum(txtDbPath)
				&& checkDatum(txtDbName) && checkDatum(txtUserName) && checkDatum(txtPassword)
				&& checkDatum(txtTargetPackage);
		if (!retVal)
			app.warningMsg("Fill out all main project data !");
		return retVal;
	}

	private void initialize() {
		frame = new ShrgFrame(this);
		frame.setResizable(false);
		frame.setBounds(100, 100, 1018, 643);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Vector<Image> images = new Vector<Image>();
		images.add(Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource("res/SprHibRAD_icon.png")));
		frame.setIconImages(images);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		x = new JTextField();
		x.setBounds(224, 8, 526, 20);
		panel.add(x);
		x.setColumns(10);
		x.setEditable(false);

		JButton btnOpenProject = new JButton("Open project");
		btnOpenProject.setBounds(10, 42, 109, 21);
		btnOpenProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openProject();
			}
		});
		panel.add(btnOpenProject);

		JLabel lblConfigurationFile = new JLabel("Configuration file");
		lblConfigurationFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lblConfigurationFile.setBounds(124, 11, 97, 14);
		panel.add(lblConfigurationFile);

		txtjdbcDriverClass = new JTextField();
		txtjdbcDriverClass.setBounds(834, 8, 166, 20);
		panel.add(txtjdbcDriverClass);
		txtjdbcDriverClass.setColumns(10);

		txtDbPath = new JTextField();
		txtDbPath.setBounds(224, 32, 260, 20);
		panel.add(txtDbPath);
		txtDbPath.setColumns(10);

		JLabel lblDbPath = new JLabel("db path");
		lblDbPath.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbPath.setBounds(124, 35, 97, 14);
		panel.add(lblDbPath);

		txtDbName = new JTextField();
		txtDbName.setBounds(575, 32, 87, 20);
		txtDbName.setColumns(10);
		panel.add(txtDbName);

		JLabel lblDbName = new JLabel("db name");
		lblDbName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbName.setBounds(494, 35, 78, 14);
		panel.add(lblDbName);

		JLabel lblUsername = new JLabel("username");
		lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsername.setBounds(666, 35, 73, 14);
		panel.add(lblUsername);

		txtUserName = new JTextField();
		txtUserName.setBounds(740, 32, 89, 20);
		txtUserName.setColumns(10);
		panel.add(txtUserName);

		JLabel lblPassword = new JLabel("password");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setBounds(832, 35, 70, 14);
		panel.add(lblPassword);

		txtPassword = new JPasswordField();
		txtPassword.setBounds(903, 32, 97, 20);
		txtPassword.setColumns(10);
		panel.add(txtPassword);

		JLabel lblDriverClass = new JLabel("driver class");
		lblDriverClass.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDriverClass.setBounds(759, 11, 72, 14);
		panel.add(lblDriverClass);

		JButton btnNewProject = new JButton("New project");
		btnNewProject.setBounds(10, 11, 109, 21);
		btnNewProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newProject();
			}
		});
		panel.add(btnNewProject);

		JButton btnConnectToDb = new JButton("Load metadata and user/roles");
		btnConnectToDb.setBounds(118, 103, 213, 23);
		btnConnectToDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadMetadata();
			}
		});
		panel.add(btnConnectToDb);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(8, 537, 333, 68);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane);

		console = new JEditorPane();
		scrollPane.setViewportView(console);
		console.setEditable(false);
		console.setMargin(new Insets(5, 5, 5, 20));
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(351, 537, 649, 68);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane_1);

		console2 = new JEditorPane();
		console2.setEditable(false);
		console2.setMargin(new Insets(5, 5, 5, 20));
		scrollPane_1.setViewportView(console2);

		JLabel lblConsole = new JLabel("Console");
		lblConsole.setBounds(10, 522, 80, 14);
		panel.add(lblConsole);

		JLabel lblConsole_1 = new JLabel("Console 2");
		lblConsole_1.setBounds(351, 522, 82, 14);
		panel.add(lblConsole_1);

		JButton btnClearCsl = new JButton("Clear");
		btnClearCsl.setBounds(251, 520, 89, 16);
		btnClearCsl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				console.setText(null);
			}
		});
		panel.add(btnClearCsl);

		JButton btnClearCsl2 = new JButton("Clear");
		btnClearCsl2.setBounds(911, 520, 89, 16);
		btnClearCsl2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				console2.setText(null);
			}
		});
		panel.add(btnClearCsl2);

		btnSaveProject = new JButton("Save project");
		btnSaveProject.setBounds(10, 74, 109, 21);
		btnSaveProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		panel.add(btnSaveProject);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 130, 992, 386);
		panel.add(tabbedPane);

		JPanel panel_8 = new JPanel();
		tabbedPane.addTab("Application configuration", null, panel_8, null);
		panel_8.setLayout(null);

		txtHibDialect = new JTextField();
		txtHibDialect.setBounds(222, 11, 331, 20);
		panel_8.add(txtHibDialect);
		txtHibDialect.setColumns(10);

		JLabel lblNewLabel = new JLabel("hibernate.dialect");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(1, 14, 218, 14);
		panel_8.add(lblNewLabel);

		JLabel lblHibernateshowsql = new JLabel("hibernate.show_sql");
		lblHibernateshowsql.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHibernateshowsql.setBounds(1, 39, 218, 14);
		panel_8.add(lblHibernateshowsql);

		txtHibShowSql = new JTextField();
		txtHibShowSql.setColumns(10);
		txtHibShowSql.setBounds(222, 36, 40, 20);
		panel_8.add(txtHibShowSql);

		JLabel lblHibernateformatsql = new JLabel("hibernate.format_sql");
		lblHibernateformatsql.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHibernateformatsql.setBounds(1, 63, 218, 14);
		panel_8.add(lblHibernateformatsql);

		txtHibFormatSql = new JTextField();
		txtHibFormatSql.setColumns(10);
		txtHibFormatSql.setBounds(222, 60, 40, 20);
		panel_8.add(txtHibFormatSql);

		JLabel lblSprhibradpagesize = new JLabel("sprHibRad.pagesize");
		lblSprhibradpagesize.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSprhibradpagesize.setBounds(1, 140, 218, 14);
		panel_8.add(lblSprhibradpagesize);

		txtShrPageSize = new JTextField();
		txtShrPageSize.setColumns(10);
		txtShrPageSize.setBounds(222, 137, 40, 20);
		panel_8.add(txtShrPageSize);

		txtShrUserprefsMenu = new JTextField();
		txtShrUserprefsMenu.setColumns(10);
		txtShrUserprefsMenu.setBounds(222, 160, 40, 20);
		panel_8.add(txtShrUserprefsMenu);

		JLabel lblSprhibraduserprefsmenu = new JLabel("sprHibRad.userprefsmenu");
		lblSprhibraduserprefsmenu.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSprhibraduserprefsmenu.setBounds(1, 163, 218, 14);
		panel_8.add(lblSprhibraduserprefsmenu);

		JLabel lblSprhibraddatestyle = new JLabel("sprHibRad.dateStyle");
		lblSprhibraddatestyle.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSprhibraddatestyle.setBounds(1, 188, 218, 14);
		panel_8.add(lblSprhibraddatestyle);

		txtShrDateStyle = new JTextField();
		txtShrDateStyle.setColumns(10);
		txtShrDateStyle.setBounds(222, 185, 117, 20);
		panel_8.add(txtShrDateStyle);

		txtShrCurrencyCountry = new JTextField();
		txtShrCurrencyCountry.setColumns(10);
		txtShrCurrencyCountry.setBounds(222, 209, 27, 20);
		panel_8.add(txtShrCurrencyCountry);

		JLabel lblSprhibradcurrencycountry = new JLabel("sprHibRad.currencyCountry");
		lblSprhibradcurrencycountry.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSprhibradcurrencycountry.setBounds(1, 212, 218, 14);
		panel_8.add(lblSprhibradcurrencycountry);

		JLabel lblSprhibradlangparameterizedreports = new JLabel("sprHibRad.langParameterizedReports");
		lblSprhibradlangparameterizedreports.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSprhibradlangparameterizedreports.setBounds(1, 237, 218, 14);
		panel_8.add(lblSprhibradlangparameterizedreports);

		txtShrLangParameterizedReports = new JTextField();
		txtShrLangParameterizedReports.setColumns(10);
		txtShrLangParameterizedReports.setBounds(222, 234, 40, 20);
		panel_8.add(txtShrLangParameterizedReports);

		txtShrMinPwdSize = new JTextField();
		txtShrMinPwdSize.setColumns(10);
		txtShrMinPwdSize.setBounds(222, 260, 40, 20);
		panel_8.add(txtShrMinPwdSize);

		JLabel lblSprhibradminpwdsize = new JLabel("sprHibRad.minPwdSize");
		lblSprhibradminpwdsize.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSprhibradminpwdsize.setBounds(1, 263, 218, 14);
		panel_8.add(lblSprhibradminpwdsize);

		JLabel lblbirtLogDir = new JLabel("birt.logDir");
		lblbirtLogDir.setHorizontalAlignment(SwingConstants.RIGHT);
		lblbirtLogDir.setBounds(1, 297, 218, 14);
		panel_8.add(lblbirtLogDir);

		txtBirtLogDir = new JTextField();
		txtBirtLogDir.setColumns(10);
		txtBirtLogDir.setBounds(222, 294, 331, 20);
		panel_8.add(txtBirtLogDir);

		txtSessionMaxInactiveMinutes = new JTextField();
		txtSessionMaxInactiveMinutes.setColumns(10);
		txtSessionMaxInactiveMinutes.setBounds(222, 323, 40, 20);
		panel_8.add(txtSessionMaxInactiveMinutes);

		JLabel lblSessionmaxinactiveminutes = new JLabel("session.maxInactiveMinutes");
		lblSessionmaxinactiveminutes.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSessionmaxinactiveminutes.setBounds(1, 326, 218, 14);
		panel_8.add(lblSessionmaxinactiveminutes);

		JLabel lblShrAppName = new JLabel("sprHibRad.appName");
		lblShrAppName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblShrAppName.setBounds(1, 116, 218, 14);
		panel_8.add(lblShrAppName);

		txtShrAppName = new JTextField();
		txtShrAppName.setColumns(10);
		txtShrAppName.setBounds(222, 113, 331, 20);
		panel_8.add(txtShrAppName);

		JPanel InvolvedEntities = new JPanel();
		tabbedPane.addTab("Involved entities", null, InvolvedEntities, null);
		InvolvedEntities.setLayout(null);

		lstTables = new ShrgList();

		lstTables.setBounds(21, 22, 164, 302);
		InvolvedEntities.add(lstTables);

		lstEntities = new ShrgList();
		lstEntities.setBounds(251, 22, 164, 302);
		InvolvedEntities.add(lstEntities);

		JLabel lblTables = new JLabel("Tables");
		lblTables.setBounds(22, 6, 164, 14);
		InvolvedEntities.add(lblTables);

		JLabel lblEntities = new JLabel("Model entities");
		lblEntities.setBounds(251, 7, 164, 14);
		InvolvedEntities.add(lblEntities);

		lstMenu = new ShrgList();
		lstMenu.setBounds(483, 22, 164, 302);
		InvolvedEntities.add(lstMenu);

		JLabel lblMenuCompositionentities = new JLabel("Menu (entities involved)");
		lblMenuCompositionentities.setBounds(483, 6, 164, 14);
		InvolvedEntities.add(lblMenuCompositionentities);

		JLabel label = new JLabel("------>");
		label.setBounds(196, 132, 52, 14);
		InvolvedEntities.add(label);

		JLabel label_1 = new JLabel("------>");
		label_1.setBounds(425, 132, 55, 14);
		InvolvedEntities.add(label_1);

		JLabel lblDoubleClickTo = new JLabel(
				"Double click to push the item to the list on the right or press the CANC/DEL key to delete the item from the target list");
		lblDoubleClickTo.setBounds(24, 325, 886, 14);
		InvolvedEntities.add(lblDoubleClickTo);

		cmbRolesTable = new ShrgComboBox();
		cmbRolesTable.setBounds(732, 33, 155, 20);
		InvolvedEntities.add(cmbRolesTable);

		cmbRoleColumn = new ShrgComboBox();
		cmbRoleColumn.setBounds(843, 64, 115, 20);
		InvolvedEntities.add(cmbRoleColumn);

		JLabel lblRolesTable = new JLabel("Roles table");
		lblRolesTable.setBounds(732, 16, 82, 14);
		InvolvedEntities.add(lblRolesTable);

		JLabel lblRoleLiteralColumn = new JLabel("Literal column");
		lblRoleLiteralColumn.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRoleLiteralColumn.setBounds(749, 67, 90, 14);
		InvolvedEntities.add(lblRoleLiteralColumn);

		JLabel lblUsersTable = new JLabel("Users table");
		lblUsersTable.setBounds(732, 228, 86, 14);
		InvolvedEntities.add(lblUsersTable);

		cmbUsersTable = new ShrgComboBox();
		cmbUsersTable.setBounds(732, 245, 155, 20);
		InvolvedEntities.add(cmbUsersTable);

		JLabel lblUserLiteralColumn = new JLabel("Literal column");
		lblUserLiteralColumn.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUserLiteralColumn.setBounds(746, 279, 93, 14);
		InvolvedEntities.add(lblUserLiteralColumn);

		cmbUserColumn = new ShrgComboBox();
		cmbUserColumn.setBounds(843, 276, 115, 20);
		InvolvedEntities.add(cmbUserColumn);

		JLabel lblUsersRolesTable = new JLabel("User's roles table");
		lblUsersRolesTable.setBounds(689, 135, 103, 14);
		InvolvedEntities.add(lblUsersRolesTable);

		cmbUserRolesTable = new ShrgComboBox();
		cmbUserRolesTable.setBounds(689, 152, 155, 20);
		InvolvedEntities.add(cmbUserRolesTable);

		JLabel lblRoleFk = new JLabel("Role fk");
		lblRoleFk.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRoleFk.setBounds(787, 129, 53, 14);
		InvolvedEntities.add(lblRoleFk);

		cmbRoleFk = new ShrgComboBox();
		cmbRoleFk.setBounds(843, 125, 115, 20);
		InvolvedEntities.add(cmbRoleFk);

		JLabel lblUserFk = new JLabel("User fk");
		lblUserFk.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUserFk.setBounds(784, 186, 57, 14);
		InvolvedEntities.add(lblUserFk);

		cmbUserFk = new ShrgComboBox();
		cmbUserFk.setBounds(843, 183, 115, 20);
		InvolvedEntities.add(cmbUserFk);

		JLabel lblPassoColumn = new JLabel("Password column");
		lblPassoColumn.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassoColumn.setBounds(734, 307, 105, 14);
		InvolvedEntities.add(lblPassoColumn);

		cmbPasswordColumn = new ShrgComboBox();
		cmbPasswordColumn.setBounds(843, 304, 115, 20);
		InvolvedEntities.add(cmbPasswordColumn);

		JPanel panel_7 = new JPanel();
		tabbedPane.addTab("Roles management", null, panel_7, null);
		panel_7.setLayout(null);

		txtRoleName = new JTextField();
		txtRoleName.setColumns(10);
		txtRoleName.setBounds(76, 41, 97, 20);
		panel_7.add(txtRoleName);

		JLabel lblRoleName = new JLabel("Role name");
		lblRoleName.setBounds(76, 27, 97, 14);
		panel_7.add(lblRoleName);

		JButton btnAddRole = new JButton("Add ----->");
		btnAddRole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uMngr.addRole();
			}
		});
		btnAddRole.setBounds(76, 85, 125, 23);
		panel_7.add(btnAddRole);

		lstRoleNames = new ShrgList();
		lstRoleNames.setBounds(260, 42, 180, 226);
		panel_7.add(lstRoleNames);

		JLabel lblRoleNames = new JLabel("Role names");
		lblRoleNames.setBounds(260, 27, 135, 14);
		panel_7.add(lblRoleNames);

		JPanel entitysTasks = new JPanel();
		tabbedPane.addTab("Entity's tasks", null, entitysTasks, null);
		entitysTasks.setLayout(null);

		cmbEntity = new ShrgComboBox();
		cmbEntity.setBounds(72, 11, 155, 20);
		entitysTasks.add(cmbEntity);
		cmbEntity.setActionPerformer(new ActionPerformer() {
			@Override
			public void handle(String selectedItem) {
				onEntitySelected(selectedItem);
			}
		});

		JLabel lblEntity = new JLabel("Entity");
		lblEntity.setHorizontalAlignment(SwingConstants.RIGHT);
		lblEntity.setBounds(6, 14, 64, 14);
		entitysTasks.add(lblEntity);

		JTabbedPane entityTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		entityTabbedPane.setBounds(9, 42, 968, 313);
		entitysTasks.add(entityTabbedPane);

		JPanel panel_1 = new JPanel();
		entityTabbedPane.addTab("Main", null, panel_1, null);
		panel_1.setLayout(null);

		JLabel lblAvailableFields = new JLabel("Available fields");
		lblAvailableFields.setBounds(9, 39, 106, 14);
		panel_1.add(lblAvailableFields);

		lstAvailableFields = new ShrgList();
		lstAvailableFields.setBounds(9, 57, 105, 115);
		panel_1.add(lstAvailableFields);
		lstAvailableFields.setUnmodifiable();

		JLabel label_2 = new JLabel("-->");
		label_2.setBounds(118, 104, 26, 14);
		panel_1.add(label_2);

		JLabel lblFormFields = new JLabel("Form fields");
		lblFormFields.setBounds(416, 3, 106, 14);
		panel_1.add(lblFormFields);

		lstFormFields = new ShrgList();
		lstFormFields.setBounds(416, 19, 105, 115);
		panel_1.add(lstFormFields);

		JLabel label_3 = new JLabel("---->");
		label_3.setBounds(522, 70, 54, 14);
		panel_1.add(label_3);

		lstResultFields = new ShrgList();
		lstResultFields.setBounds(624, 88, 105, 84);
		panel_1.add(lstResultFields);

		JLabel lblSearchResultFields = new JLabel("Search result fields");
		lblSearchResultFields.setBounds(624, 72, 133, 14);
		panel_1.add(lblSearchResultFields);

		JLabel lblSearchCriteriaFields = new JLabel("Search criteria fields");
		lblSearchCriteriaFields.setBounds(624, 3, 133, 14);
		panel_1.add(lblSearchCriteriaFields);

		lstCriteriaFields = new ShrgList();
		lstCriteriaFields.setBounds(624, 19, 105, 48);
		panel_1.add(lstCriteriaFields);

		radioButton = new JRadioButton("-->");
		radioButton.setBounds(563, 44, 56, 23);
		panel_1.add(radioButton);

		radioButton_1 = new JRadioButton("-->");
		radioButton_1.setBounds(563, 86, 56, 23);
		panel_1.add(radioButton_1);

		JLabel lblSearchOrderedFields = new JLabel("Search ordered fields");
		lblSearchOrderedFields.setBounds(760, 137, 133, 14);
		panel_1.add(lblSearchOrderedFields);

		lstOrderedFields = new ShrgList();
		lstOrderedFields.setBounds(760, 153, 105, 48);
		panel_1.add(lstOrderedFields);

		cmbOrientation1 = new ShrgComboBox();
		cmbOrientation1.setModel(new DefaultComboBoxModel(new String[] { "asc", "desc" }));
		cmbOrientation1.setBounds(870, 167, 86, 20);
		panel_1.add(cmbOrientation1);

		chkReadOnly = new ShrgCheckBox("Read-only");
		chkReadOnly.setBounds(387, 153, 102, 19);
		panel_1.add(chkReadOnly);

		chkCurrency = new ShrgCheckBox("Currency");
		chkCurrency.setBounds(486, 153, 102, 19);
		panel_1.add(chkCurrency);

		chkBoolean = new ShrgCheckBox("Boolean");
		chkBoolean.setBounds(486, 174, 102, 19);
		panel_1.add(chkBoolean);

		chkPercent = new ShrgCheckBox("Percent (%)");
		chkPercent.setBounds(387, 174, 102, 19);
		panel_1.add(chkPercent);

		chkWithOperator = new ShrgCheckBox("with operator");
		chkWithOperator.setBounds(735, 17, 130, 23);
		panel_1.add(chkWithOperator);

		JPanel panel_2 = new JPanel();
		panel_2.setForeground(Color.BLACK);
		entityTabbedPane.addTab("Children entities", null, panel_2, null);
		panel_2.setLayout(null);
		
		lstEntities2 = new ShrgList();
		lstEntities2.setBounds(26, 32, 164, 201);
		panel_2.add(lstEntities2);

		lstEntities.setClone(lstEntities2);

		JLabel lblEntities_1 = new JLabel("Model entities");
		lblEntities_1.setBounds(27, 15, 164, 14);
		panel_2.add(lblEntities_1);

		JLabel label_5 = new JLabel("----------->");
		label_5.setBounds(214, 98, 77, 14);
		panel_2.add(label_5);

		JLabel lblChildrenEntities = new JLabel("Children entities");
		lblChildrenEntities.setBounds(310, 15, 164, 14);
		panel_2.add(lblChildrenEntities);

		lstChildrenEntities = new ShrgList();
		lstChildrenEntities.setBounds(309, 32, 164, 114);
		panel_2.add(lstChildrenEntities);

		JLabel lblDetailsEntities = new JLabel("Details entities");
		lblDetailsEntities.setBounds(551, 102, 164, 14);
		panel_2.add(lblDetailsEntities);

		lstDetailsEntities = new ShrgList();
		lstDetailsEntities.setBounds(550, 119, 164, 114);
		panel_2.add(lstDetailsEntities);

		JLabel label_8 = new JLabel("------>");
		label_8.setBounds(483, 121, 57, 14);
		panel_2.add(label_8);

		JLabel lblParentFk = new JLabel("Parent FK");
		lblParentFk.setHorizontalAlignment(SwingConstants.RIGHT);
		lblParentFk.setBounds(502, 14, 77, 14);
		panel_2.add(lblParentFk);

		cmbParentFk = new ShrgComboBox();
		cmbParentFk.setBounds(588, 11, 155, 20);
		panel_2.add(cmbParentFk);
		
		
		JLabel lblChildFk = new JLabel("Child FK");
		lblChildFk.setHorizontalAlignment(SwingConstants.RIGHT);
		lblChildFk.setBounds(502, 44, 77, 14);
		panel_2.add(lblChildFk);

		cmbChildFk = new ShrgComboBox();
		cmbChildFk.setBounds(588, 41, 155, 20);
		panel_2.add(cmbChildFk);
		
		JTextArea txtrBeSureTo = new JTextArea();
		txtrBeSureTo.setBackground(Color.ORANGE);
		txtrBeSureTo.setEditable(false);
		txtrBeSureTo.setForeground(Color.BLACK);
		txtrBeSureTo.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtrBeSureTo.setEnabled(false);
		txtrBeSureTo.setWrapStyleWord(true);
		txtrBeSureTo.setLineWrap(true);
		txtrBeSureTo.setText("Be sure to have already defined the foreign keys and associated field target entities, for every entity chosen as child entity. If not do it by selecting the entity in the main drop down list and select the 'Main' tab-pane. ");
		txtrBeSureTo.setBounds(766, 10, 187, 136);
		panel_2.add(txtrBeSureTo);
		
		JPanel panel_3 = new JPanel();
		entityTabbedPane.addTab("Detail grid structure", null, panel_3, null);
		panel_3.setLayout(null);

		JLabel lblDetailEntity = new JLabel("Detail entity");
		lblDetailEntity.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDetailEntity.setBounds(33, 24, 93, 14);
		panel_3.add(lblDetailEntity);

		cmbDetailEntity = new ShrgComboBox();
		cmbDetailEntity.setBounds(136, 21, 155, 20);
		panel_3.add(cmbDetailEntity);

		JLabel lblFieldProperties = new JLabel("|------------ Form field properties ------------|");
		lblFieldProperties.setBounds(386, 138, 213, 14);
		panel_1.add(lblFieldProperties);

		JLabel lblFieldsTargetEntity = new JLabel("Field target entity");
		lblFieldsTargetEntity.setHorizontalAlignment(SwingConstants.LEFT);
		lblFieldsTargetEntity.setBounds(190, 223, 147, 14);
		panel_1.add(lblFieldsTargetEntity);

		lstFKs = new ShrgList();
		lstFKs.setBounds(191, 147, 105, 71);
		panel_1.add(lstFKs);

		cmbFieldTargetEntity = new ShrgComboBox();
		cmbFieldTargetEntity.setBounds(190, 240, 159, 20);
		panel_1.add(cmbFieldTargetEntity);

		JLabel lblForeignKeys = new JLabel("Foreign keys");
		lblForeignKeys.setBounds(190, 128, 106, 14);
		panel_1.add(lblForeignKeys);

		radioButton_5 = new JRadioButton("-->");
		radioButton_5.setBounds(143, 53, 46, 23);
		panel_1.add(radioButton_5);

		radioButton_7 = new JRadioButton("-->");
		radioButton_7.setBounds(143, 149, 48, 23);
		panel_1.add(radioButton_7);

		radioButton_6 = new JRadioButton("--------------------------------------------------------->");
		radioButton_6.setBounds(143, 100, 264, 23);
		panel_1.add(radioButton_6);

		JLabel lblVerboseFields = new JLabel("Verbose fields");
		lblVerboseFields.setBounds(190, 3, 106, 14);
		panel_1.add(lblVerboseFields);

		lstVerboseFields = new ShrgList();
		lstVerboseFields.setBounds(190, 19, 105, 55);
		panel_1.add(lstVerboseFields);

		chkVerboseLiteral = new ShrgCheckBox("Verbose literal");
		chkVerboseLiteral.setBounds(10, 7, 133, 23);
		panel_1.add(chkVerboseLiteral);

		JLabel label_9 = new JLabel("-->");
		label_9.setBounds(733, 153, 24, 14);
		panel_1.add(label_9);

		chkPrintable = new ShrgCheckBox("Printable");
		chkPrintable.setBounds(9, 193, 159, 23);
		panel_1.add(chkPrintable);

		chkDocument = new ShrgCheckBox("Document");
		chkDocument.setBounds(486, 195, 102, 19);
		panel_1.add(chkDocument);

		chkValidate = new ShrgCheckBox("'validate()' skeleton");
		chkValidate.setBounds(9, 216, 175, 23);
		panel_1.add(chkValidate);

		chkShInitbinder = new ShrgCheckBox("'shrInitBinder()' skeleton");
		chkShInitbinder.setBounds(9, 241, 175, 23);
		panel_1.add(chkShInitbinder);

		chkRadioButtons = new ShrgCheckBox("Radio buttons");
		chkRadioButtons.setBounds(387, 195, 102, 19);
		panel_1.add(chkRadioButtons);

		cmbPreviewPath = new ShrgComboBox();
		cmbPreviewPath.setBounds(393, 250, 155, 20);
		panel_1.add(cmbPreviewPath);

		JLabel lblPreviewPath = new JLabel("Preview field");
		lblPreviewPath.setBounds(393, 236, 128, 14);
		panel_1.add(lblPreviewPath);

		JLabel label_11 = new JLabel("Versus");
		label_11.setBounds(870, 150, 72, 14);
		panel_1.add(label_11);

		chkPassword = new ShrgCheckBox("Password");
		chkPassword.setBounds(387, 215, 102, 19);
		panel_1.add(chkPassword);
		
		chkClassSpecificDictio = new ShrgCheckBox("Entity-specific dict.");
		chkClassSpecificDictio.setBounds(486, 215, 141, 19);
		panel_1.add(chkClassSpecificDictio);

		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_1.setBounds(21, 39, 917, 221);
		panel_3.add(tabbedPane_1);

		JPanel panel_4 = new JPanel();
		tabbedPane_1.addTab("", null, panel_4, null);
		panel_4.setLayout(null);

		JLabel label_4 = new JLabel("Form fields");
		label_4.setBounds(30, 25, 127, 14);
		panel_4.add(label_4);

		lstFormFields2 = new ShrgList();
		lstFormFields2.setBounds(30, 43, 127, 115);
		panel_4.add(lstFormFields2);

		JLabel lblDetailGridFields = new JLabel("Detail grid fields");
		lblDetailGridFields.setBounds(220, 27, 127, 14);
		panel_4.add(lblDetailGridFields);

		lstDetailFields = new ShrgList();
		lstDetailFields.setBounds(220, 45, 127, 87);
		panel_4.add(lstDetailFields);

		JLabel lblDetailOrderedFields = new JLabel("Detail ordered fields");
		lblDetailOrderedFields.setBounds(396, 103, 127, 14);
		panel_4.add(lblDetailOrderedFields);

		lstDetailOrderedFields = new ShrgList();
		lstDetailOrderedFields.setBounds(396, 121, 127, 48);
		panel_4.add(lstDetailOrderedFields);
		lstDetailOrderedFieldsSelector = new Selector(lstDetailOrderedFields, "detailOrderedFields");

		JLabel lblOrientation = new JLabel("Versus");
		lblOrientation.setBounds(530, 121, 71, 14);
		panel_4.add(lblOrientation);

		cmbOrientation2 = new ShrgComboBox();
		cmbOrientation2.setBounds(530, 138, 86, 20);
		panel_4.add(cmbOrientation2);
		cmbOrientation2.setModel(new DefaultComboBoxModel(new String[] { "asc", "desc" }));

		// childrenEntities.setSelector(cmbDetailEntity);
		// cmbEntity.addClone(cmbJoinEntity);

		chkViewProperty = new ShrgCheckBox("View property");
		chkViewProperty.setBounds(717, 66, 149, 23);
		panel_4.add(chkViewProperty);

		chkNoDelete = new ShrgCheckBox("No delete");
		chkNoDelete.setBounds(717, 92, 149, 23);
		panel_4.add(chkNoDelete);

		chkNoAdd = new ShrgCheckBox("No add");
		chkNoAdd.setBounds(717, 118, 149, 23);
		panel_4.add(chkNoAdd);

		JLabel label_10 = new JLabel("---->");
		label_10.setBounds(162, 96, 50, 14);
		panel_4.add(label_10);

		JLabel label_6 = new JLabel("---->");
		label_6.setBounds(350, 120, 42, 14);
		panel_4.add(label_6);

		JPanel panel_5 = new JPanel();
		entityTabbedPane.addTab("Roles restrictions", null, panel_5, null);
		panel_5.setLayout(null);

		lstAvailableRoles = new ShrgList();
		lstAvailableRoles.setBounds(34, 29, 188, 95);
		panel_5.add(lstAvailableRoles);

		JLabel lblAvailableRoles = new JLabel("Available roles");
		lblAvailableRoles.setBounds(34, 11, 188, 14);
		panel_5.add(lblAvailableRoles);

		JLabel lblRoles = new JLabel("Roles");
		lblRoles.setBounds(339, 46, 156, 14);
		panel_5.add(lblRoles);

		lstRoles = new ShrgList();
		lstRoles.setBounds(338, 62, 127, 62);
		panel_5.add(lstRoles);

		JLabel label_7 = new JLabel("------->");
		label_7.setBounds(245, 79, 84, 14);
		panel_5.add(label_7);

		JLabel lblAccessModes = new JLabel("Available access modes");
		lblAccessModes.setBounds(34, 134, 188, 14);
		panel_5.add(lblAccessModes);

		lstAvailableAccessModes = new ShrgList();
		lstAvailableAccessModes.setBounds(34, 152, 188, 95);
		panel_5.add(lstAvailableAccessModes);

		JLabel lblRoleCcessModes = new JLabel("Role access modes");
		lblRoleCcessModes.setBounds(338, 135, 156, 14);
		panel_5.add(lblRoleCcessModes);

		lstRoleAccessModes = new ShrgList();
		lstRoleAccessModes.setBounds(338, 153, 127, 62);
		panel_5.add(lstRoleAccessModes);

		JLabel label_12 = new JLabel("------->");
		label_12.setBounds(245, 173, 84, 14);
		panel_5.add(label_12);

		JPanel panel_6 = new JPanel();
		tabbedPane.addTab("Users management", null, panel_6, null);
		panel_6.setLayout(null);

		lstAvailableRoles2 = new ShrgList();
		lstAvailableRoles2.setBounds(40, 199, 189, 130);
		panel_6.add(lstAvailableRoles2);

		JLabel label_13 = new JLabel("Available roles");
		label_13.setBounds(40, 181, 127, 14);
		panel_6.add(label_13);

		lstUsers = new ShrgList();
		lstUsers.setBounds(273, 54, 127, 102);
		panel_6.add(lstUsers);

		JLabel lblUsers = new JLabel("Users");
		lblUsers.setBounds(273, 39, 127, 14);
		panel_6.add(lblUsers);

		JLabel lblRoles_1 = new JLabel("User's roles");
		lblRoles_1.setBounds(273, 181, 127, 14);
		panel_6.add(lblRoles_1);

		lstUsersRoles = new ShrgList();
		lstUsersRoles.setBounds(273, 199, 258, 89);
		panel_6.add(lstUsersRoles);

		JLabel label_17 = new JLabel("-->");
		label_17.setBounds(233, 237, 30, 14);
		panel_6.add(label_17);

		JLabel label_15 = new JLabel("username");
		label_15.setHorizontalAlignment(SwingConstants.RIGHT);
		label_15.setBounds(10, 56, 77, 14);
		panel_6.add(label_15);

		txtUser = new JTextField();
		txtUser.setColumns(10);
		txtUser.setBounds(89, 53, 97, 20);
		panel_6.add(txtUser);

		JLabel label_16 = new JLabel("password");
		label_16.setHorizontalAlignment(SwingConstants.RIGHT);
		label_16.setBounds(10, 78, 77, 14);
		panel_6.add(label_16);

		txtPwd = new JTextField();
		txtPwd.setColumns(10);
		txtPwd.setBounds(89, 75, 97, 20);
		panel_6.add(txtPwd);

		JButton btnAddUser = new JButton("Create ----->");
		btnAddUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!metadataLoaded)
					loadMetadata();
				uMngr.createUser(txtUser.getText(), txtPwd.getText());
			}
		});
		btnAddUser.setBounds(89, 97, 127, 23);
		panel_6.add(btnAddUser);

		chkAllDbPrivilegies = new JCheckBox("with all db privilegies");
		chkAllDbPrivilegies.setBounds(89, 121, 174, 23);
		panel_6.add(chkAllDbPrivilegies);
		
		JLabel lblCreationAndDeletion = new JLabel(" ----- Creation and deletion of users  is provided for MySql dbms only -----");
		lblCreationAndDeletion.setForeground(Color.RED);
		lblCreationAndDeletion.setBounds(40, 17, 584, 14);
		panel_6.add(lblCreationAndDeletion);

		JPanel panel_10 = new JPanel();
		panel_10.setBorder(new LineBorder(Color.GRAY, 2));
		panel_10.setBounds(336, 59, 663, 67);
		panel.add(panel_10);
		panel_10.setLayout(null);

		JLabel lblPackage = new JLabel("Java package");
		lblPackage.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPackage.setBounds(1, 42, 93, 14);
		panel_10.add(lblPackage);

		txtTargetPackage = new JTextField();
		txtTargetPackage.setBounds(96, 39, 252, 20);
		panel_10.add(txtTargetPackage);
		txtTargetPackage.setColumns(10);

		chkLanguageFile = new JCheckBox("Language file");
		chkLanguageFile.setBounds(552, 6, 104, 23);
		panel_10.add(chkLanguageFile);

		btnGenerate = new JButton("Generate");
		btnGenerate.setBounds(556, 38, 100, 23);
		panel_10.add(btnGenerate);

		txtTargetDir = new JTextField();
		txtTargetDir.setEditable(false);
		txtTargetDir.setColumns(10);
		txtTargetDir.setBounds(114, 8, 435, 20);
		panel_10.add(txtTargetDir);

		btnTargetDir = new JButton("Target dir");
		btnTargetDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String target = buildFileChooser(projectPathName, true, "Choose the directory path where the SprHibRAD Project will be created").getAbsolutePath();
				if (target != null) {
					txtTargetDir.setText(target);
					prjProperties.setPrpty("targetDir", target);					
				}					
			}
		});
		btnTargetDir.setBounds(6, 7, 104, 23);
		panel_10.add(btnTargetDir);

		JLabel lblGeneration = new JLabel("Generation");
		lblGeneration.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGeneration.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblGeneration.setBounds(214, 60, 119, 15);
		panel.add(lblGeneration);
		
		JButton btnAbout = new JButton("... About");
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				console.setText(null);
				console2.setText(null);
				outToConsole("SprHibRAD Generator " + mainFrame.version);
				outToConsole("Licensed under LGPL");
				outToConsole("@2017 Stefano Pizzocaro ");
				console2.setText("As part of the SprHibRAD Suite, it is the tool for building an entire Dynamic Web Application based on SprHibRAD Framework.");
			}
		});
		btnAbout.setBounds(10, 103, 87, 21);
		panel.add(btnAbout);
		btnGenerate.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				btnSaveProject.grabFocus();
				if (app.checkMainData()) {
					if (!metadataLoaded)
						loadMetadata();
					if (metadataLoaded)
						generator.generate();
				}
			}
		});

	}

	public boolean withLanguageFile() {
		return chkLanguageFile.isSelected();
	}

	public FieldDescriptor fieldDescriptor(ShrgComboBox entitySelector, String fieldName) {
		HashMap<String, TableDescriptor> tabMap = tablesMap();
		if (entitySelector.getSelectedItem() == null)
			return null;
		else {
			TableDescriptor tabDescr = tabMap.get(entitySelector.getSelectedItem());
			return tabDescr == null ? null : tabDescr.colsInfo.fieldsMap.get(fieldName);
		}
	}

	void initializeGuiLogic() {
		jsonParser = new JSONParser();
		confProperties = new ShrgJsonWrapper(this, false);
		prjProperties = new ShrgJsonWrapper(this, true);

		formFields = new ShrgObject(prjProperties, lstFormFields, "formFields", true);

		cmbEntitySelector = new Selector(cmbEntity, "entities");
		formFields.addParentSelector(cmbEntitySelector, true);

		lstAvailableRoles.setUnmodifiable();
		lstAvailableRoles.setTarget(new ListTarget[] { new ListTarget(null, lstRoles) });
		roles = new ShrgObject(prjProperties, lstRoles, "roles");
		roles.addParentSelector(cmbEntitySelector, true);
		lstRolesSelector = new Selector(lstRoles, "roles");
		lstRoles.setActionPerformer(new ActionPerformer() {
			@Override
			void handle(String selectedItem) {
				lstRoleAccessModes.list.setEnabled(selectedItem != null);
			}

			@Override
			boolean remove(String selectedItem) {
				return true;
			}
			
		});

		lstAvailableAccessModes.setUnmodifiable();
		loadAvailableAccessModes();
		lstAvailableAccessModes.setTarget(new ListTarget[] { new ListTarget(null, lstRoleAccessModes) });
		roleAccessModes = new ShrgObject(prjProperties, lstRoleAccessModes, "roleAccessModes");
		roleAccessModes.addParentSelector(cmbEntitySelector, false);
		roleAccessModes.addParentSelector(lstRolesSelector, true);

		lstTables.setUnmodifiable();
		lstTables.setTarget(new ListTarget[] { new ListTarget(null, lstEntities) });
		lstEntities.setTarget(new ListTarget[] { new ListTarget(null, lstMenu) });
		lstResultFieldsSelector = new Selector(lstResultFields, "resultFields");

		criteriaFields = new ShrgObject(prjProperties, lstCriteriaFields, "criteriaFields", true);
		criteriaFields.addParentSelector(cmbEntitySelector, true);

		lstCriteriaFieldsSelector = new Selector(lstCriteriaFields, "criteriaFields");

		lstOrderedFieldsSelector = new Selector(lstOrderedFields, "orderedFields");

		orderedFields = new ShrgObject(prjProperties, lstOrderedFields, "orderedFields", true);
		orderedFields.addParentSelector(cmbEntitySelector, true);

		lstFormFields.setTarget(
				new ListTarget[] { new ListTarget(radioButton, lstCriteriaFields, manageableTypes, cmbEntity),
						new ListTarget(radioButton_1, lstResultFields, manageableTypes, cmbEntity, new TargetEnabler() {
							@Override
							public boolean enable(String fieldName, ShrgComboBox directorCombo) {
								return hasPreview(fieldName, directorCombo);
							}
						}) });

		lstResultFields
				.setTarget((new ListTarget[] { new ListTarget(null, lstOrderedFields, manageableTypes, cmbEntity) }));

		resultColOrdVersus = new ShrgObject(prjProperties, cmbOrientation1, "resultColOrdVersus");
		resultColOrdVersus.addParentSelector(cmbEntitySelector, false);
		resultColOrdVersus.addParentSelector(lstOrderedFieldsSelector, true);

		lstFormFieldsSelector = new Selector(lstFormFields, "formFields");
		formFieldReadOnly = new ShrgObject(prjProperties, chkReadOnly, "formFieldReadOnly");
		formFieldReadOnly.addParentSelector(cmbEntitySelector, false);
		formFieldReadOnly.addParentSelector(lstFormFieldsSelector, true);

		formFieldCurrency = new ShrgObject(prjProperties, chkCurrency, "formFieldCurrency",
				new OnFieldTypeEnabler(new int[] { Types.REAL, Types.FLOAT, Types.DOUBLE }));
		formFieldCurrency.addParentSelector(cmbEntitySelector, false);
		formFieldCurrency.addParentSelector(lstFormFieldsSelector, true);

		radioButtons = new ShrgObject(prjProperties, chkRadioButtons, "radioButtons",
				new OnFieldTypeEnabler(new int[] { Types.BIT, Types.INTEGER }));
		radioButtons.addParentSelector(cmbEntitySelector, false);
		radioButtons.addParentSelector(lstFormFieldsSelector, true);

		formFieldBoolean = new ShrgObject(prjProperties, chkBoolean, "formFieldBoolean",
				new OnFieldTypeEnabler(new int[] { Types.TINYINT, Types.SMALLINT, Types.INTEGER }));
		formFieldBoolean.addParentSelector(cmbEntitySelector, false);
		formFieldBoolean.addParentSelector(lstFormFieldsSelector, true);

		formFieldPercent = new ShrgObject(prjProperties, chkPercent, "formFieldPercent",
				new OnFieldTypeEnabler(new int[] { Types.REAL, Types.FLOAT, Types.DOUBLE }));
		formFieldPercent.addParentSelector(cmbEntitySelector, false);
		formFieldPercent.addParentSelector(lstFormFieldsSelector, true);

		formFieldDocument = new ShrgObject(prjProperties, chkDocument, "formFieldDocument",
				new OnFieldTypeEnabler(new int[] { Types.BLOB, Types.VARBINARY, Types.LONGVARBINARY }));
		formFieldDocument.addParentSelector(cmbEntitySelector, false);
		formFieldDocument.addParentSelector(lstFormFieldsSelector, true);

		formFieldPassword = new ShrgObject(prjProperties, chkPassword, "formFieldPassword",
				new OnFieldTypeEnabler(new int[] { Types.VARCHAR }));
		formFieldPassword.addParentSelector(cmbEntitySelector, false);
		formFieldPassword.addParentSelector(lstFormFieldsSelector, true);

		classSpecificDictio = new ShrgObject(prjProperties, chkClassSpecificDictio, "classSpecificDictio");
		classSpecificDictio.addParentSelector(cmbEntitySelector, false);
		classSpecificDictio.addParentSelector(lstFormFieldsSelector, true);

		previewPath = new ShrgObject(prjProperties, cmbPreviewPath, "previewPath",
				new OnFieldTypeEnabler(new int[] { Types.BLOB, Types.VARBINARY, Types.LONGVARBINARY }));
		previewPath.addParentSelector(cmbEntitySelector, false);
		previewPath.addParentSelector(lstFormFieldsSelector, true);

		rolesTable = new ShrgObject(prjProperties, cmbRolesTable, "rolesTable");
		roleColumn = new ShrgObject(prjProperties, cmbRoleColumn, "roleColumn");

		userRolesTable = new ShrgObject(prjProperties, cmbUserRolesTable, "userRolesTable");
		roleFk = new ShrgObject(prjProperties, cmbRoleFk, "roleFk");
		userFk = new ShrgObject(prjProperties, cmbUserFk, "userFk");
		usersTable = new ShrgObject(prjProperties, cmbUsersTable, "usersTable");
		userColumn = new ShrgObject(prjProperties, cmbUserColumn, "userColumn");
		passwordColumn = new ShrgObject(prjProperties, cmbPasswordColumn, "passwordColumn");

		setTableFieldsCombosHierarchy(cmbRolesTable, new ShrgComboBox[] { cmbRoleColumn });
		setTableFieldsCombosHierarchy(cmbUserRolesTable, new ShrgComboBox[] { cmbRoleFk, cmbUserFk });
		setTableFieldsCombosHierarchy(cmbUsersTable, new ShrgComboBox[] { cmbUserColumn, cmbPasswordColumn });

		cmbRoleColumn.setActionPerformer(new ActionPerformer() {
			@Override
			public void handle(String selectedItem) {
				if (selectedItem != null)
					uMngr.loadRoles();
			}
		});

		lstFormFields.setActionPerformer(new ActionPerformer() {
			@Override
			public boolean remove(String selectedItem) {
				cmbPreviewPath.removeItem(selectedItem);
				return true;
			}
		});

		cmbPreviewPath.setActionPerformer(new ActionPerformer() {
			@Override
			public void add(String selectedItem) {
				String currentValue = (String) cmbPreviewPath.getSelectedItem();
				if (currentValue != null && !lstFormFields.contains(currentValue)) {
					lstFormFields.add(currentValue);
					formFields.set(false);
				}
			}

			@Override
			public boolean remove(String selectedItem) {
				ShrgJSONArray formFields = new ShrgJSONArray(app.shrgJsonPeeker.peek(prjProperties.obj,
						new String[] { "entities", (String) cmbEntity.getSelectedItem(), "formFields" }).array);
				String imageField = null;
				String preview = null;
				String formFieldKey = null;
				for (Object formField : formFields) {
					formFieldKey = ShrgJSONArray.getKey((JSONObject) formField);
					preview = shrgJsonPeeker.peek(formFields,
							new String[] { formFieldKey, "previewPath", "value" }).value;
					if (preview != null && preview.compareTo(selectedItem) == 0)
						imageField = formFieldKey;
				}
				if (imageField != null) {
					warningMsg("The field that has '" + selectedItem
							+ "' as preview field will be removed from 'Search result' and 'Detail grid' lists also when the entity runs as detail !");
					((JSONObject) shrgJsonPeeker.peek(formFields,
							new String[] { imageField, "previewPath" }).hostingObj).clear();
					JSONObject entities = (JSONObject) prjProperties.obj.get("entities");
					String srcImageField = imageField;
					entities.forEach(new BiConsumer<String, JSONObject>() {
						@Override
						public void accept(String entityName, JSONObject entityObj) {
							if (entityName.compareTo((String) cmbEntity.getSelectedItem()) == 0) {
								JSONArray resultFields = (JSONArray) entityObj.get("resultFields");
								int index = 0;
								for (Object resultField : resultFields) {
									if (ShrgJSONArray.getKey((JSONObject) resultField).compareTo(srcImageField) == 0) {
										resultFields.remove(index);
										break;
									}
									index++;
								}
							}
							JSONArray detailsEntities = (JSONArray) entityObj.get("detailsEntities");
							for (Object detailsEntity : detailsEntities) {
								if (ShrgJSONArray.getKey((JSONObject) detailsEntity)
										.compareTo((String) cmbEntity.getSelectedItem()) == 0) {
									JSONArray detailsFields = (JSONArray) ShrgJSONArray
											.getValue((JSONObject) detailsEntity).get("detailFields");
									int index = 0;
									for (Object detailsField : detailsFields) {
										if (ShrgJSONArray.getKey((JSONObject) detailsField)
												.compareTo(srcImageField) == 0) {
											detailsFields.remove(index);
											break;
										}
										index++;
									}
								}
							}
						}
					});
					resultFields.set(true);
				}
				return true;
			}
		});

		lstRoleNames.setActionPerformer(new ActionPerformer() {

			@Override
			public boolean remove(String selectedItem) {
				return uMngr.removeRole(selectedItem);
			}
		});

		formPrintable = new ShrgObject(prjProperties, chkPrintable, "formPrintable");
		formPrintable.addParentSelector(cmbEntitySelector, true);

		criterionWithOp = new ShrgObject(prjProperties, chkWithOperator, "withOp");
		criterionWithOp.addParentSelector(cmbEntitySelector, false);
		criterionWithOp.addParentSelector(lstCriteriaFieldsSelector, true);

		childrenEntities = new ShrgObject(prjProperties, lstChildrenEntities, "childrenEntities", true);
		childrenEntities.addParentSelector(cmbEntitySelector, true);

		lstEntities2.setTarget(new ListTarget[] { new ListTarget(null, lstChildrenEntities) });

		lstChildrenEntitiesSelectorForFeeding = new Selector(lstChildrenEntities, "entities");
		lstChildrenEntitiesSelector = new Selector(lstChildrenEntities, "childrenEntities");
		FullManagedCombo parentFKfmc = setFKsLogic(cmbParentFk, "parentFk", true);
		parentFkcatalog = parentFKfmc.catalog;
		parentFk = parentFKfmc.value;
		FullManagedCombo childFKfmc = setFKsLogic(cmbChildFk, "childFk", false);
		childFkcatalog = childFKfmc.catalog;
		childFk = childFKfmc.value;

		lstChildrenEntities.setProjection(new Projection() {
			@Override
			public void clear() {
				lstDetailsEntities.clear(true);
			}

			@Override
			public void project(String key, JSONObject obj) {
				lstDetailsEntities.add(getDetailEntityFromTheChildOne(key, obj).entity);
			}
		});
		lstChildrenEntities.addProjector(childrenEntities);
		cmbChildFk.addProjector(childrenEntities);
		
		resultFields = new ShrgObject(prjProperties, lstResultFields, "resultFields", true);
		resultFields.addParentSelector(cmbEntitySelector, true);

		entities = new ShrgObject(prjProperties, lstEntities, "entities");
		entities.applySelectionDomain(cmbEntity);

		menuEntities = new ShrgObject(prjProperties, lstMenu, "menuEntities", true);
		noAdd = new ShrgObject(prjProperties, chkNoAdd, "noAdd");

		cmbDetailEntitySelector = new Selector(cmbDetailEntity, "detailsEntities");

		verboseLiteral = new ShrgObject(prjProperties, chkVerboseLiteral, "verboseLiteral");
		verboseLiteral.addParentSelector(cmbEntitySelector, true);

		shInitbinder = new ShrgObject(prjProperties, chkShInitbinder, "shInitbinder");
		shInitbinder.addParentSelector(cmbEntitySelector, true);

		validate = new ShrgObject(prjProperties, chkValidate, "validate");
		validate.addParentSelector(cmbEntitySelector, true);

		noAdd.addParentSelector(cmbEntitySelector, false);
		noAdd.addParentSelector(cmbDetailEntitySelector, true);

		detailFields = new ShrgObject(prjProperties, lstDetailFields, "detailFields", true);

		detailFields.addParentSelector(cmbEntitySelector, false);
		detailFields.addParentSelector(cmbDetailEntitySelector, true);

		lstFormFields2.setUnmodifiable();
		formFields2 = new ShrgObject(prjProperties, lstFormFields2, "formFields", true);
		detailOrderedFields = new ShrgObject(prjProperties, lstDetailOrderedFields, "detailOrderedFields", true);

		cmbDetailEntitySelectorForFieldsForm2 = new Selector(cmbDetailEntity, "entities");

		formFields2.addParentSelector(cmbDetailEntitySelectorForFieldsForm2, true);
		detailOrderedFields.addParentSelector(cmbEntitySelector, false);
		detailOrderedFields.addParentSelector(cmbDetailEntitySelector, true);

		detailColOrdVersus = new ShrgObject(prjProperties, cmbOrientation2, "detailColOrdVersus");

		detailColOrdVersus.addParentSelector(cmbEntitySelector, false);
		detailColOrdVersus.addParentSelector(cmbDetailEntitySelector, false);
		detailColOrdVersus.addParentSelector(lstDetailOrderedFieldsSelector, true);

		viewProperty = new ShrgObject(prjProperties, chkViewProperty, "viewProperty");

		viewProperty.addParentSelector(cmbEntitySelector, false);
		viewProperty.addParentSelector(cmbDetailEntitySelector, true);

		lstDetailFieldsSelector = new Selector(lstDetailFields, "detailFields");

		noDelete = new ShrgObject(prjProperties, chkNoDelete, "noDelete");

		noDelete.addParentSelector(cmbEntitySelector, false);
		noDelete.addParentSelector(cmbDetailEntitySelector, true);

		detailsEntities = new ShrgObject(prjProperties, lstDetailsEntities, "detailsEntities", true);
		detailsEntities.applySelectionDomain(cmbDetailEntity);
		detailsEntities.addParentSelector(cmbEntitySelector, true);

		fks = new ShrgObject(prjProperties, lstFKs, "fks");
		fks.addParentSelector(cmbEntitySelector, true);

		lstFKsSelector = new Selector(lstFKs, "fks");

		fieldTargetEntity = new ShrgObject(prjProperties, cmbFieldTargetEntity, "fieldTargetEntity",
				new OnFieldTypeEnabler(new int[] { Types.INTEGER }));
		fieldTargetEntity.addParentSelector(cmbEntitySelector, false);
		fieldTargetEntity.addParentSelector(lstFKsSelector, true);

		cmbEntity.addClone(cmbFieldTargetEntity);

		verboseFields = new ShrgObject(prjProperties, lstVerboseFields, "verboseFields", true);
		verboseFields.addParentSelector(cmbEntitySelector, true);

		lstAvailableFields.setTarget(
				new ListTarget[] { new ListTarget(radioButton_5, lstVerboseFields, manageableTypes, cmbEntity),
						new ListTarget(radioButton_6, lstFormFields),
						new ListTarget(radioButton_7, lstFKs, idTypes, cmbEntity) });

		lstFormFields2.setTarget(new ListTarget[] {
				new ListTarget(null, lstDetailFields, manageableTypes, cmbDetailEntity, new TargetEnabler() {
					@Override
					public boolean enable(String fieldName, ShrgComboBox directorCombo) {
						return hasPreview(fieldName, directorCombo);
					}
				}) });

		lstDetailFields.setTarget(
				(new ListTarget[] { new ListTarget(null, lstDetailOrderedFields, manageableTypes, cmbDetailEntity) }));

		lstRoleNames.setClone(lstAvailableRoles);
		lstAvailableRoles.setClone(lstAvailableRoles2);

		lstAvailableRoles2.setUnmodifiable();
		lstAvailableRoles2.setTarget(new ListTarget[] { new ListTarget(null, lstUsersRoles) });
		lstUsers.setActionPerformer(new ActionPerformer() {

			@Override
			void handle(String selectedItem) {
				uMngr.loadUserRoles(selectedItem);
			}

			@Override
			void add(String selectedItem) {
				lstUsersRoles.clear();
			}

			@Override
			boolean remove(String selectedItem) {
				return uMngr.deleteUser(selectedItem);
			}

		});
		lstUsersRoles.setActionPerformer(new ActionPerformer() {

			@Override
			void add(String selectedItem) {
				uMngr.addUserRole(selectedItem);
			}

			@Override
			boolean remove(String selectedItem) {
				return uMngr.deleteUserRole(selectedItem);
			}

		});
		uMngr = new Umanager();
	}

	private void setTableFieldsCombosHierarchy(ShrgComboBox tableCombo, ShrgComboBox columnCombos[]) {
		tableCombo.setActionPerformer(new ActionPerformer() {
			@Override
			public void add(String selectedItem) {
				for (ShrgComboBox columnCombo : columnCombos) {
					columnCombo.removeAllItems();
					TableDescriptor tableDescr = tablesInfo.tablesMap.get(selectedItem);
					if (tableDescr != null) {
						columnCombo.setFeeding(true);
						for (FieldDescriptor field : tableDescr.colsInfo.fields)
							columnCombo.addItem(field.name);
						columnCombo.setFeeding(false);
					}
				}
			}
		});
	}

	private void loadAvailableAccessModes() {
		lstAvailableAccessModes.add("add");
		lstAvailableAccessModes.add("list");
		lstAvailableAccessModes.add("edit");
		lstAvailableAccessModes.add("view");
		lstAvailableAccessModes.add("delete");
		lstAvailableAccessModes.add("uploadBinary");
		lstAvailableAccessModes.add("viewImage");
		lstAvailableAccessModes.add("viewHandout");
		lstAvailableAccessModes.add("reports");
	}

	protected boolean hasPreview(String fieldName, ShrgComboBox directorCombo) {
		boolean retval = false;
		if (fieldDescriptor(directorCombo, fieldName).javaType().compareTo("byte[]") == 0) {
			if (app.shrgJsonPeeker.peek(prjProperties.obj,
					new String[] { "entities", (String) directorCombo.getSelectedItem(), "formFields", fieldName,
							"previewPath", "value" }).value != null)
				retval = true;
			else
				warningMsg(
						"Only when an 'Preview field' value is selected as associated preview, the field can populate the target list !");
		}
		return retval;
	}

	public DetailDescriptor getDetailEntityFromTheChildOne(String childEntityName, JSONObject childEntityObject) {
		RetVal peekRetVal = shrgJsonPeeker.peek(childEntityObject, new String[] { "childFk", "value" });
		DetailDescriptor retVal = new DetailDescriptor();
		retVal.m2mChildFkMember = peekRetVal.value;
		retVal.entity = (peekRetVal.value == null ? childEntityName
				: shrgJsonPeeker.peek(prjProperties.obj, new String[] { "entities", childEntityName, "fks",
						peekRetVal.value, "fieldTargetEntity", "value" }).value);
		return retVal;
	}

	class DetailDescriptor {
		String entity;
		String m2mChildFkMember;
	}

	class FullManagedCombo {
		public ShrgObject catalog;
		public ShrgObject value;
	}

	FullManagedCombo setFKsLogic(ShrgComboBox cmb, String property, boolean targetAsParentEntity) {
		FullManagedCombo retVal = new FullManagedCombo();
		retVal.catalog = new ShrgObject(prjProperties, cmb, "fks", false, true);
		retVal.catalog.addParentSelector(lstChildrenEntitiesSelectorForFeeding, true);

		retVal.value = new ShrgObject(prjProperties, cmb, property);
		retVal.value.addParentSelector(cmbEntitySelector, false);
		retVal.value.addParentSelector(lstChildrenEntitiesSelector, true);

		cmb.setFeedingFilter(new FeedingFilter() {
			@Override
			public boolean allow(String termKey, JSONObject termObj, ShrgComboBox theCombo) {
				int matchResult = theCombo.getShrgObject().selHierachy.get(0).attr
						.compareTo((String) ((JSONObject) termObj.get("fieldTargetEntity")).get("value"));
				return targetAsParentEntity ? matchResult == 0 : matchResult != 0;
			}
		});
		return retVal;
	}

	protected void onEntitySelected(String selectedItem) {
		lstAvailableFields.removeAll();
		cmbPreviewPath.clear(true);
		TableDescriptor tableDescr = tablesInfo.tablesMap.get(selectedItem);
		if (tableDescr != null) {
			for (FieldDescriptor field : tableDescr.colsInfo.fields) {
				lstAvailableFields.add(field.name);
				if (field.javaType().compareTo("byte[]") == 0)
					cmbPreviewPath.addItem(field.name);
			}
		}
		cmbPreviewPath.setSelectedIndex(-1);
	}

	protected void loadMetadata() {
		if (!app.checkMainData())
			return;
		getFromGui();
		if (!initializing)
			warningMsg(
					"Metadata will be now loaded ! All selections will be cleared but the underlaying project remains unchanged !"
							+ "\nPlease perfom again possible selections you already have done to recall the scenario you were operating in ! ");
		metadataLoading = true;
		setWaitCursor(true);
		if (getDbConn()) {
			metadataLoaded = jdbcOpen();
			cmbEntity.setSelectedIndex(cmbEntity.getSelectedIndex());
		}
		setWaitCursor(false);
		metadataLoading = false;
		outToConsole("Loading meta data : " + (metadataLoaded ? "Success !" : "FAILURE !"));
		if (metadataLoaded) {
			cmbEntity.clear();
			rolesTable.set(true);
			roleColumn.set(true);
			userRolesTable.set(true);
			roleFk.set(true);
			userFk.set(true);
			usersTable.set(true);
			userColumn.set(true);
			passwordColumn.set(true);
			uMngr.loadUsers();
		}
	}

	protected void openProject() {
		closeDbConn();
		setProjectPathName(
				buildFileChooser(projectPathName, false, "Select the SprHibRAD Generator project file to open"), false);
		if (loadProject())
			setGui();
	}

	private void setProjectPathName(File file, boolean newProject) {
		if (file != null) {
			String pathName = file.getAbsolutePath();
			String extension = "." + projectExtension;
			projectPathName = pathName + (pathName.indexOf(extension) >= 0 ? "" : extension);
			confProperties.setPrpty("projectPathName", projectPathName);
		}
	}

	public boolean loadProject() {
		boolean retVal = true;
		if (projectPathName == null) {
			prjProperties.setObj(null);
			retVal = false;																																					
		} else {
			retVal = prjProperties.load(projectPathName);
			if (retVal) {
				jdbcDriverClass = prjProperties.getPrpty("jdbcDriverClass");
				connectionUrl = prjProperties.getPrpty("connectionUrl");
				dbName = prjProperties.getPrpty("dbName");
				userName = prjProperties.getPrpty("userName");
				password = prjProperties.getPrpty("password");
				targetPackage = prjProperties.getPrpty("targetPackage");
			}
		}
		return retVal;
	}

	void clearGui() {
		x.setText("");
		txtjdbcDriverClass.setText("");
		txtDbPath.setText("");
		txtDbName.setText("");
		txtUserName.setText("");
		txtPassword.setText("");
		txtTargetPackage.setText("");
		entities.clear(false);
		entities.set(false);
		menuEntities.clear(false);
		menuEntities.set(false);
		cmbEntity.clear();
		cmbDetailEntity.clear();
		cmbOrientation1.clear();
		cmbOrientation2.clear();

		txtShrAppName.setText("");
		txtHibDialect.setText("");
		txtHibShowSql.setText("");
		txtHibFormatSql.setText("");
		txtShrPageSize.setText("");
		txtShrUserprefsMenu.setText("");
		txtShrDateStyle.setText("");
		txtShrCurrencyCountry.setText("");
		txtShrLangParameterizedReports.setText("");
		txtShrMinPwdSize.setText("");
		txtBirtLogDir.setText("");
		txtSessionMaxInactiveMinutes.setText("");
	}

	void setGui() {
		x.setText(projectPathName);
		txtjdbcDriverClass.setText(jdbcDriverClass);
		txtDbPath.setText(connectionUrl);
		txtDbName.setText(dbName);
		txtUserName.setText(userName);
		txtPassword.setText(password);
		txtTargetPackage.setText(targetPackage);
		txtTargetDir.setText(prjProperties.getPrpty("targetDir"));
		setLists(true);

		txtShrAppName.setText(prjProperties.getPrpty("ShrAppName"));
		txtHibDialect.setText(prjProperties.getPrpty("HibDialect"));
		txtHibShowSql.setText(prjProperties.getPrpty("HibShowSql"));
		txtHibFormatSql.setText(prjProperties.getPrpty("HibFormatSql"));
		txtShrPageSize.setText(prjProperties.getPrpty("ShrPageSize"));
		txtShrUserprefsMenu.setText(prjProperties.getPrpty("ShrUserprefsMenu"));
		txtShrDateStyle.setText(prjProperties.getPrpty("ShrDateStyle"));
		txtShrCurrencyCountry.setText(prjProperties.getPrpty("ShrCurrencyCountry"));
		txtShrLangParameterizedReports.setText(prjProperties.getPrpty("ShrLangParameterizedReports"));
		txtShrMinPwdSize.setText(prjProperties.getPrpty("ShrMinPwdSize"));
		txtBirtLogDir.setText(prjProperties.getPrpty("BirtLogDir"));
		txtSessionMaxInactiveMinutes.setText(prjProperties.getPrpty("SessionMaxInactiveMinutes"));

		frame.repaint();
	}

	void disableAndClear(ShrgComboBox cmb) {
		cmb.clear();
		cmb.setEnabled(false);
	}

	private void setLists(boolean toGui) {
		if (toGui) {
			chkReadOnly.setEnabled(false);
			chkCurrency.setEnabled(false);
			chkPercent.setEnabled(false);
			chkBoolean.setEnabled(false);
			chkDocument.setEnabled(false);
			chkPassword.setEnabled(false);
			chkClassSpecificDictio.setEnabled(false);
			chkRadioButtons.setEnabled(false);
			chkVerboseLiteral.setEnabled(false);
			chkWithOperator.setEnabled(false);
			chkViewProperty.setEnabled(false);
			chkNoDelete.setEnabled(false);
			chkNoAdd.setEnabled(false);
			chkShInitbinder.setEnabled(false);
			chkValidate.setEnabled(false);
			lstRoleAccessModes.list.setEnabled(false);
			disableAndClear(cmbParentFk);
			disableAndClear(cmbChildFk);
			disableAndClear(cmbOrientation1);
			disableAndClear(cmbOrientation2);
			disableAndClear(cmbFieldTargetEntity);
			disableAndClear(cmbPreviewPath);
		}
		entities.set(toGui);
		menuEntities.set(toGui);
		formFields.set(toGui);
		fks.set(toGui);
		resultFields.set(toGui);
		criteriaFields.set(toGui);
		orderedFields.set(toGui);
		childrenEntities.set(toGui);
		detailFields.set(toGui);
		detailOrderedFields.set(toGui);
		roles.set(toGui);
		roleAccessModes.set(toGui);
	}

	void getFromGui() {
		projectPathName = x.getText();
		jdbcDriverClass = txtjdbcDriverClass.getText();
		connectionUrl = txtDbPath.getText();
		dbName = txtDbName.getText();
		userName = txtUserName.getText();
		password = txtPassword.getText();
		targetPackage = txtTargetPackage.getText();
		prjProperties.setPrpty("jdbcDriverClass", jdbcDriverClass);
		prjProperties.setPrpty("connectionUrl", connectionUrl);
		prjProperties.setPrpty("dbName", dbName);
		prjProperties.setPrpty("userName", userName);
		prjProperties.setPrpty("password", password);
		prjProperties.setPrpty("targetPackage", targetPackage);

		prjProperties.setPrpty("ShrAppName", txtShrAppName.getText());
		prjProperties.setPrpty("HibDialect", txtHibDialect.getText());
		prjProperties.setPrpty("HibShowSql", txtHibShowSql.getText());
		prjProperties.setPrpty("HibFormatSql", txtHibFormatSql.getText());
		prjProperties.setPrpty("ShrPageSize", txtShrPageSize.getText());
		prjProperties.setPrpty("ShrUserprefsMenu", txtShrUserprefsMenu.getText());
		prjProperties.setPrpty("ShrDateStyle", txtShrDateStyle.getText());
		prjProperties.setPrpty("ShrCurrencyCountry", txtShrCurrencyCountry.getText());
		prjProperties.setPrpty("ShrLangParameterizedReports", txtShrLangParameterizedReports.getText());
		prjProperties.setPrpty("ShrMinPwdSize", txtShrMinPwdSize.getText());
		prjProperties.setPrpty("BirtLogDir", txtBirtLogDir.getText());
		prjProperties.setPrpty("SessionMaxInactiveMinutes", txtSessionMaxInactiveMinutes.getText());
	}

	public void outToConsole(JEditorPane targetPane, String text) {
		targetPane.setText(targetPane.getText() + text + "\n");
	}

	public void outToConsole(String text) {
		outToConsole(console, text);
	}

	public void outToConsole(Exception e) {
		e.printStackTrace();
		app.long_op_success = false;
		outToConsole(console2, e.toString());
	}

	public boolean setWaitCursor(boolean set) {
		boolean oldIsWaitCursor = mainFrame.getCursor().equals(waitCursor);
		mainFrame.setCursor(set ? waitCursor : defCursor);
		return oldIsWaitCursor;
	}

	private File buildFileChooser(String pathname, boolean onlyDirectory, String title) {
		if (pathname == null)
			pathname = System.getProperty("user.home");
		setWaitCursor(true);
		JFileChooser fc = new JFileChooser();
		if (onlyDirectory)
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else
			fc.setFileFilter(new FileNameExtensionFilter(
					"	 project file (*." + projectExtension + ")", projectExtension));
		fc.setDialogTitle(title);
		fc.setApproveButtonText("Ok");
		fc.setCurrentDirectory(new java.io.File(pathname));
		setWaitCursor(false);
		File retFile = null;
		if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
			retFile = fc.getSelectedFile();
		return retFile;
	}

	void save() {
		getFromGui();
		if (!projectPathName.isEmpty()) {
			prjProperties.store(projectPathName);
			confProperties.store(mainConfFile);
		}
	}

	void newProject() {
		closeDbConn();
		clearGui();
		setProjectPathName(buildFileChooser(projectPathName, false,
				"Choose the name (and location) of the new SprHibRAD project file"), true);
		setGui();
	}

	public void onClosing() {
		closeDbConn();
		btnSaveProject.grabFocus();
		save();
	}

	public void closeDbConn() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
			conn = null;
		}
	}

	public boolean getDbConn() {
		try {
			conn = DriverManager.getConnection(connectionUrl, userName, password);
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			outToConsole(e.getMessage());
			outToConsole(e);
		}
		return conn != null;
	}

	public Statement createStmnt() throws SQLException, NamingException {
		return conn.prepareStatement(new String("fictitious"));
	}

	public boolean jdbcOpen() {
		boolean retVal = false;
		try {
			tablesInfo.clear();
			DatabaseMetaData dbMetadata = conn.getMetaData();
			ResultSet tablesTypesRs = dbMetadata.getTableTypes();
			ResultSet tablesRs = dbMetadata.getTables(null, null, "%", new String[] { "TABLE" });
			String tableName;
			ResultSet columnRs = null;
			ResultSet pkRs = null;
			ResultSetMetaData columnMetadata = null;
			int m_colCount = 0;
			Statement m_statement = createStmnt();
			FieldDescriptor currColumn = null;
			TableDescriptor currTable = null;
			ColsInfo colsInfo = null;
			lstTables.clear();
			cmbRolesTable.clear();
			cmbUserRolesTable.clear();
			cmbUsersTable.clear();
			while (tablesRs.next()) {
				currTable = new TableDescriptor();
				tablesInfo.tables.add(currTable);
				tableName = tablesRs.getString(3).toLowerCase();
				lstTables.add(tableName);
				cmbRolesTable.addItem(tableName);
				cmbUserRolesTable.addItem(tableName);
				cmbUsersTable.addItem(tableName);
				tablesInfo.tablesMap.put(tableName, currTable);
				currTable.name = tableName;
				pkRs = dbMetadata.getPrimaryKeys(null, null, tableName);
				while (pkRs.next())
					currTable.pkCols.add(pkRs.getString("COLUMN_NAME").toLowerCase());
				if (currTable.pkCols.size() > 1) {
					outToConsole("Table '" + tableName + "' has pk made by " + currTable.pkCols.size()
							+ " columns: A SprHibRAD requirement is that Pk is made by at most one column !");
					break;
				}
				columnRs = m_statement.executeQuery("Select * from " + tableName + " limit 0");
				columnMetadata = columnRs.getMetaData();
				m_colCount = columnMetadata.getColumnCount();
				int index;
				colsInfo = currTable.colsInfo;
				for (int i = 0; i < m_colCount; i++) {
					currColumn = new FieldDescriptor();
					index = i + 1;
					currColumn.name = columnMetadata.getColumnLabel(index).toLowerCase();
					colsInfo.fields.add(i, currColumn);
					colsInfo.fieldsMap.put(currColumn.name, currColumn);
					currColumn.sqlType = columnMetadata.getColumnType(index);
					currColumn.precision = columnMetadata.getPrecision(index); // size
					currColumn.scale = columnMetadata.getScale(index); // decimals
					currColumn.isNullable = columnMetadata.isNullable(index) == ResultSetMetaData.columnNullable;
					if (currTable.pkCols.size() > 0) {
						if (currTable.pkCols.get(0).compareTo(currColumn.name) == 0)
							currColumn.isID = true;
						else if (currColumn.name.compareTo("id") == 0) {
							outToConsole("Table '" + tableName
									+ "': A SprHibRAD requirement is that if a column is named 'id', that one is to be the PK !");
							break;
						}
					}
					if (currTable.pkCols.size() == 0 && currColumn.name.compareTo("id") == 0)
						currColumn.isID = true;
				}
			}
			retVal = true;
		} catch (Exception e) {
			outToConsole(e);
		}
		return retVal;
	}

	HashMap<String, TableDescriptor> tablesMap() {
		if (tablesInfo.tablesMap.size() == 0)
			loadMetadata();
		return tablesInfo.tablesMap;
	}

	public FieldDescriptor getColInfo(String fieldName) {
		HashMap<String, TableDescriptor> tabMap = tablesMap();
		if (cmbEntitySelector.attr == null)
			return null;
		else
			return tabMap.get(cmbEntitySelector.attr).colsInfo.fieldsMap.get(fieldName);
	}
}
