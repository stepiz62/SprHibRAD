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
  
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiConsumer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sprhibrad.generator.SprHibRadGen.DetailDescriptor;
import com.sprhibrad.names.Names;

/**
 * The class performs the generation of the code of a SprHibRAD based
 * application. It processes the content of the SprHibRAD Generator project (the
 * Json tree) developed through the use of the gui object provided by the
 * {@link SprHibRadGen} class. Like the project editor class, the project
 * processor too makes use of the package {@code org.json.simple} to do the job,
 * by being the project file a Json file.
 * 
 * Part of the generated files (MVC files) are completely synthesized basing on
 * the project content, others (like configuration files) are template based
 * generated with the pattern substitution mechanism, again based on the project
 * file. Other part of the files that make the final SprHibRAD application
 * project are completely copied "as are" from the 'resources' directory.
 * 
 * @see ShrgJSONArray
 * @see ShrgJsonPeeker
 */
public class Generator {

	class ShrgFileWriter extends FileWriter {

		int indentationCount;

		int indentationIncrease = 0;
		boolean noTerminator;
		public Vector<Injector> injectors;
		
		public void addInjector(Injector injector) {
			if (injectors == null)
				injectors = new Vector<Injector>();
			injectors.add(injector);			
		}
		
		public boolean isNoTerminator() {
			return noTerminator;
		}
		public void setNoTerminator(boolean noTerminator) {
			this.noTerminator = noTerminator;
		}
		public ShrgFileWriter(String fileName) throws IOException {
			super(fileName);
		}
		void increaseIndent() {
			indentationCount++;
		}
		void decreaseIndent() {
			indentationCount--;
		}
		public int getIndentationCount() {
			return indentationCount;
		}
		public void setIndentationCount(int indentationCount) {
			this.indentationCount = indentationCount;
		}
		
		@Override
		public void write(String str) throws IOException {
			indentationCount += indentationIncrease;
			StringBuilder builder = new StringBuilder("");
			for (int i=0; i < indentationCount; i++) 
				builder.append("\t");
			super.write(builder.toString() + str);
		}
		
		public void setIndentationIncrease(int increase) {
			indentationIncrease = increase;			
		}
		
	}
	
	class FkManager {
		JSONObject entityObj;
		JSONObject fksObj;
		HashSet<String> fks = new HashSet<String>();
		ShrgFileWriter writer;
		protected int counter;
		
		public FkManager(JSONObject entityObj, ShrgFileWriter writer) {
			this.entityObj = entityObj;
			this.writer = writer;
			render();
		}
		public int counter() {
			return counter;
		}
		void render() {
			fksObj = (JSONObject) entityObj.get("fks");
			if (fksObj != null) {
				if (fksObj.size() > 0)
					prolog();
				fksObj.forEach(new BiConsumer<String, JSONObject>() {
					@Override
					public void accept(String fkField, JSONObject fkFieldObj) {
						fks.add(fkField);
						String targetEntity = getAttribute(fkFieldObj, "fieldTargetEntity");
						if (targetEntity != null)
							renderBody(writer, memberName(fkField), targetEntity, fkField);
					}
				});
				if (fksObj.size() > 0)
					epilog();
			}

		}
		protected void epilog() {}
		protected void prolog() {}
		public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) {};
	}
	
	String memberName(String fieldName) {
		int index = fieldName.indexOf("_id");							
		return index == fieldName.length() - 3 ? fieldName.substring(0, index) : fieldName;
	}
	
	JSONObject project;
	ShrgFileWriter fileWriter;
	SprHibRadGen app = SprHibRadGen.app;
	Map<String, Map<String, ShrgFileWriter>> packagesMap;
	Map<String , ShrgFileWriter> modelWriters;	
	Map<String , ShrgFileWriter> daoWriters;
	Map<String , ShrgFileWriter> serviceWriters;
	Map<String , ShrgFileWriter> controllerWriters;
	Map<String , ShrgFileWriter> converterWriters;
	Map<String , ShrgFileWriter> reportsWriters;
	Map<String , ShrgFileWriter> lookUpJspsWriters;
	Map<String , ShrgFileWriter> formJspsWriters;
	Map<String , ShrgFileWriter> configWriters;
	ShrgFileWriter birtEngineFactory_writer;
	ShrgFileWriter birtView_writer;
	ShrgFileWriter applicationInitializer_writer;
	ShrgFileWriter applicationWebConf_writer;
	ShrgFileWriter securityInitializer_writer;
	ShrgFileWriter securityConfig_writer;
	ShrgFileWriter dictionary_en_US_writer;
	ShrgFileWriter applicationConf_writer;
	String modelPath;
	String daoPath;
	String servicePath;
	String controllerPath;
	String converterPath;
	String reportsPath;
	String configPath;
	private String resourcesFolderPath;
	private String packagePath;
	Map<String , String> packagesPathMap;
	private String jsps_FolderPath;
	HashSet<String> messagesSet = new HashSet<String>();
	HashMap<String, Vector<String>> dataObjectRenderings = new HashMap<String, Vector<String>>();
			
	
	public Generator() {
		init(false);
	}

	public void init(boolean onGeneration) {
		if ( ! app.checkMainData())
			return;
		
		modelWriters = 		new HashMap<String, ShrgFileWriter>();
		daoWriters =		 	new HashMap<String, ShrgFileWriter>();
		serviceWriters = 		new HashMap<String, ShrgFileWriter>();
		controllerWriters = 	new HashMap<String, ShrgFileWriter>();
		converterWriters = 	new HashMap<String, ShrgFileWriter>();
		reportsWriters = 		new HashMap<String, ShrgFileWriter>();
		lookUpJspsWriters = 	new HashMap<String, ShrgFileWriter>();
		formJspsWriters = 		new HashMap<String, ShrgFileWriter>();
		configWriters = 		new HashMap<String, ShrgFileWriter>();
		packagesMap = new HashMap<String, Map<String, ShrgFileWriter>>();
		packagesMap.put("model", modelWriters);
		packagesMap.put("dao", daoWriters);
		packagesMap.put("service", serviceWriters);
		packagesMap.put("controller", controllerWriters);
		packagesMap.put("converter", converterWriters);
		packagesMap.put("reports", reportsWriters);
		packagesMap.put(configurePackage, configWriters);

		project = app.prjProperties.obj;
		String projectPath = targetProjectPath();
		String src_main_FolderPath = projectPath + "/src/main";
		String javaFolderPath = src_main_FolderPath + "/java"; 
		resourcesFolderPath = src_main_FolderPath + "/resources"; 
		if (app.targetPackage != null) {
			packagePath = javaFolderPath + "/" + app.targetPackage.replace(".", "/");
			packagesPathMap = new HashMap<>();
			packagesMap.forEach(new BiConsumer<String, Map<String , ShrgFileWriter>>() {
				@Override
				public void accept(String packageName, Map<String, ShrgFileWriter> writer) {
					packagesPathMap.put(packageName, packagePath + "/" + packageName);
					createDirectories(packagePath + "/" + packageName);
				}});
			createDirectories(resourcesFolderPath);
			jsps_FolderPath = projectPath + "/WebContent/WEB-INF/views/";
			createDirectories(jsps_FolderPath);		
			messagesSet.clear();
			dataObjectRenderings.clear();
		}
		if (onGeneration) {	
			copyFile("resources", "messages_en_US.properties", "/src/main");
			copyFile("", ".classpath", "");
			copyFile("WebContent/reports/", "en_US", "");
			copyFile("WebContent/WEB-INF/resources/css", "shr.css", "");
			copyFile("WebContent/WEB-INF/resources/images", "handout.jpg", "");
			copyFile("WebContent/WEB-INF/resources/images", "noImg.jpg", "");
			copyFile("WebContent/WEB-INF/resources/js", "SprHibRAD.js", "");
			copyFile("WebContent/WEB-INF/views", "changePwd_form.jsp", "");
			copyFile("WebContent/WEB-INF/views", "exception.jsp", "");
			copyFile("WebContent/WEB-INF/views", "home.jsp", "");
			copyFile("WebContent/WEB-INF/views", "login.jsp", "");
			copyFile("WebContent/WEB-INF/views", "noaccess.jsp", "");
			copyFile("WebContent/WEB-INF/views", "sessionexp.jsp", "");
			copyFile("WebContent/WEB-INF/views", "uploadBinary.jsp", "");
			copyFile("WebContent/WEB-INF/views", "userPrefs_form.jsp", "");
			copyFile("WebContent/WEB-INF", "log4j2.xml", "");
			copyFile("WebContent/WEB-INF", "SprHibRad.tld", "");
			copyFile("WebContent/META-INF", "MANIFEST.MF", "");
			copyFile(".settings", "org.eclipse.jdt.core.prefs", "");
			copyFile(".settings", "org.eclipse.m2e.core.prefs", "");
			copyFile(".settings", "org.eclipse.wst.common.project.facet.core.xml", "");
			copyFile(".settings", "org.eclipse.wst.validation.prefs", "");
			
		}
	}
	
	protected void copyFile(String path, String fileName, String targetPath) {
		try {			
			Path source = Paths.get(genResPath() + "/" + path + "/" + fileName);
			String targetPathString = targetProjectPath() + targetPath + "/" + path;
			createDirectories(targetPathString);
			Files.copy(source, Paths.get(targetPathString).resolve(source.getFileName()), 
								new CopyOption[] {StandardCopyOption.REPLACE_EXISTING});
		} catch (IOException e) {
			if ( ! (e instanceof DirectoryNotEmptyException))
				app.outToConsole(e);
		}
	}

	protected void createDirectories(String path) {
		try {
			Files.createDirectories(Paths.get(path));
		} catch (IOException e) {
			app.outToConsole(e);
		}
	}
	
	protected void includeImports(String packageName, Map<String, ShrgFileWriter> writers) {
		writers.forEach(new BiConsumer<String, ShrgFileWriter>() {
			@Override
			public void accept(String entity, ShrgFileWriter writer) {
				includeImports(packageName, writer, entity);
			}});
	}
	
	interface Injector {
		void inject(ShrgFileWriter writer);
	}
	
	String genResPath() {
		return Paths.get("").toAbsolutePath().toString()  + "/resources/";
	}
	
	String targetProjectPath() {
		SprHibRadGen app = SprHibRadGen.app;
		String storedTargetDir = app.txtTargetDir.getText();
		return storedTargetDir.isEmpty() ? app.projectPathName.substring(0, app.projectPathName.indexOf(".")) : (storedTargetDir + "/" + app.txtShrAppName.getText().replace(" ", "_"));
	}
	
	protected void includeImports(String packageName, ShrgFileWriter writer, String entity) {
		if (writer == null)
			return;
		FileReader fileReader = null;
		try {
			boolean isVerboseLiteral = entityIs(entity, "verboseLiteral") && packageName.compareTo("model") == 0;
			fileReader = new FileReader(genResPath() + "imports/" + (isVerboseLiteral ? "literalCollections" : packageName) + ".java");
		} catch (FileNotFoundException e) {
			app.outToConsole(e);
		}
		String input;
		int c;
		try {
			while ((c=fileReader.read()) != -1)
					writer.write(c);
		} catch (IOException e) {
			app.outToConsole(e);
		}
	}
	
	FileReader templateReader(String dir, String ext, String name) {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(genResPath() + dir + (dir.length() == 0 ? "" : "/") + name + (ext != null && ! ext.isEmpty() ? "." + ext : ""));
		} catch (FileNotFoundException e) {
			app.outToConsole(e);
		}
		return fileReader;
	}

	protected void copyJavaTemplate(ShrgFileWriter writer, String name) {
		copyTemplate(templateReader("templates", "java", name), writer);
	}

	void copyTemplate(FileReader fileReader, ShrgFileWriter writer) {
		String input;
		int c = 0;
		int injectionIndex = 0;
		try {
			while ((c = fileReader.read()) != -1)  {
				if (c == '\u00a7') {
					writer.injectors.get(injectionIndex++).inject(writer);
					while (((c = fileReader.read()) == 10 || c == 13) && c != -1);
				} 
				if (c != -1)
					writer.write(c);
			}
		} catch (IOException e) {
			app.outToConsole(e);
		}
	}

	private boolean entityIs(String entity, String feature) {
		return app.shrgJsonPeeker.isAttributeSet(project, new String[] {"entities", entity, feature, "value"});
	}
	
	public final String configurePackage = "configuration";
	
	public void createConfigWriter(String fileName) throws IOException {
		configWriters.put(fileName, new ShrgFileWriter(packagesPathMap.get(configurePackage) + "/" + fileName + ".java"));			
	}
	
	public void createConfigWriters() {
		try {
			createConfigWriter("BirtEngineFactory");
			createConfigWriter("BirtView");
			createConfigWriter("Initializer");
			createConfigWriter("WebConf");
			createConfigWriter("SecurityWebApplicationInitializer");
			createConfigWriter("WebSecurityConfig");
			createConfigWriter("UserManagerImpl");
		} catch (IOException e) {
			app.outToConsole(e);
		}		
	}
	
	public void createWriters(String entity) {
		String classTheme = toClassName(entity);
		try {
			boolean isVerboseLiteral = entityIs(entity, "verboseLiteral");
			boolean withReports = entityIs(entity, "formPrintable");
			modelWriters.put(		entity, new ShrgFileWriter(packagesPathMap.get("model") 		+ "/" + classTheme + ".java"));
			daoWriters.put(		entity, new ShrgFileWriter(packagesPathMap.get("dao")  			+ "/" + classTheme + "Dao.java"));
			serviceWriters.put(	entity, new ShrgFileWriter(packagesPathMap.get("service")  		+ "/" + classTheme + "Service.java"));
			reportsWriters.put(	entity, withReports ? new ShrgFileWriter(packagesPathMap.get("reports")  	+ "/" + classTheme + "Reports.java") :
													null);
			converterWriters.put(	entity, isVerboseLiteral ? new ShrgFileWriter(packagesPathMap.get("converter")  	+ "/" + classTheme + "Converter.java") :
													null);			
			controllerWriters.put(	entity, isVerboseLiteral ? null :
										new ShrgFileWriter(packagesPathMap.get("controller")  	+ "/" + classTheme + "Controller.java"));
			lookUpJspsWriters.put(	entity, isVerboseLiteral ? null :
										new ShrgFileWriter(jsps_FolderPath + Names.plural(entity) + ".jsp"));
			formJspsWriters.put(entity, isVerboseLiteral ? null :
									new ShrgFileWriter(jsps_FolderPath + entity + "_form.jsp"));
		} catch (IOException e) {
			app.outToConsole(e);
		}
	}

	private String toClassName(String entity) {
		return entity.substring(0,1).toUpperCase() + entity.substring(1);
	}


	public void closeFiles(Map<String , ShrgFileWriter> fileWriters) {
		fileWriters.forEach (new BiConsumer<String, ShrgFileWriter>() {
			@Override
			public void accept(String t, ShrgFileWriter writer) {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						app.outToConsole(e);
					}
				}
			}
			
		});
	}

	public void closeFile(ShrgFileWriter fileWriter) {
		try {
			fileWriter.close();
		} catch (IOException e) {
			app.outToConsole(e);
		}
	}
	

	public void store(Map<String , ShrgFileWriter> fileWriters, String text) {
		fileWriters.forEach(new BiConsumer<String, ShrgFileWriter>() {
			@Override
			public void accept(String entity, ShrgFileWriter writer) {
				if (writer != null) {
					try {
						writer.write(text + ";\n");
					} catch (IOException e) {
						app.outToConsole(e);
					}		
				}
			}});
	}
	
	public void write(ShrgFileWriter fileWriter, String text) {
		write(fileWriter, text, false);
	}
	
	public void write(ShrgFileWriter fileWriter, String text, boolean noTerminator) {
		try {
			text = text.trim();
			ShrgFileWriter shrgFileWriter = fileWriter;
			int len = text.length();
			boolean openingBlock = len > 0 && text.charAt(len-1) == '{';
			boolean closingBlock = len > 0 && text.charAt(len-1) == '}';
			if (closingBlock)
				shrgFileWriter.decreaseIndent();
			shrgFileWriter.write(text + (len == 0 || text.indexOf("@") == 0 || text.charAt(len-1) == '>' ||
										openingBlock || closingBlock || shrgFileWriter.isNoTerminator() || noTerminator ? "" : ";") + "\n");
			if (openingBlock)
				shrgFileWriter.increaseIndent();
		} catch (IOException e) {
			app.outToConsole(e);
		}					
	}

	ShrgFileWriter createResourceWriter(String fileName) {
		return createWriter(resourcesFolderPath + "/" + fileName);
	}

	ShrgFileWriter createWriter(String filePathName) {
		ShrgFileWriter writer = null;
		try {
			writer = new ShrgFileWriter(filePathName);
			writer.setNoTerminator(true);
		} catch (IOException e) {
			app.outToConsole(e);
		}
		return writer;
	}
	
	public void generate()  {
		app.long_op_success = true;
		app.setWaitCursor(true);
		init(true);
		ShrgFileWriter pomFileWriter = null;
		ShrgFileWriter eclipseProjectFileWriter = null;
		ShrgFileWriter eclipseComponentFileWriter = null;
		try {
			String aValidProjectLiteral = app.txtShrAppName.getText().replace(" ", "_");

			pomFileWriter = createWriter(targetProjectPath() + "/pom.xml");
			pomFileWriter.addInjector(new Injector() {
				@Override
				public void inject(ShrgFileWriter writer) {
					writer.setIndentationCount(1);
					write(writer, "<groupId>" + aValidProjectLiteral + "</groupId>");
					write(writer, "<artifactId>" + aValidProjectLiteral + "</artifactId>");
				}
			});
			copyTemplate(templateReader("", "xml", "pom"), pomFileWriter);

			eclipseProjectFileWriter = createWriter(targetProjectPath() + "/.project");
			eclipseProjectFileWriter.addInjector(new Injector() {
				@Override
				public void inject(ShrgFileWriter writer) {
					writer.setIndentationCount(1);
					write(writer, "<name>" + aValidProjectLiteral + "</name>");
				}
			});
			copyTemplate(templateReader("", "project", ""), eclipseProjectFileWriter);

			eclipseComponentFileWriter = createWriter(
					targetProjectPath() + "/.settings/org.eclipse.wst.common.component");
			eclipseComponentFileWriter.addInjector(new Injector() {
				@Override
				public void inject(ShrgFileWriter writer) {
					writer.setIndentationCount(1);
					write(writer, "<wb-module deploy-name=\"" + aValidProjectLiteral + "-0.0.1-SNAPSHOT\">");
				}
			});
			eclipseComponentFileWriter.addInjector(new Injector() {
				@Override
				public void inject(ShrgFileWriter writer) {
					writer.setIndentationCount(2);
					write(writer, "<property name=\"context-root\" value=\"" + aValidProjectLiteral + "\"/>");
					write(writer, "<property name=\"java-output-path\" value=\"/" + aValidProjectLiteral
							+ "/target/classes\"/>");
				}
			});
			copyTemplate(templateReader(".settings", "", "org.eclipse.wst.common.component"),
					eclipseComponentFileWriter);

			if (app.withLanguageFile())
				dictionary_en_US_writer = createResourceWriter("dictionary_en_US.properties");
			applicationConf_writer = createResourceWriter("application.properties");

			JSONObject entities = (JSONObject) project.get("entities");
			entities.forEach(new BiConsumer<String, JSONObject>() {
				@Override
				public void accept(String entityName, JSONObject entityObj) {
					createWriters(entityName);
				}
			});
			createConfigWriters();

			createApplicationConfigurationFile();

			String packageRow = "package " + app.targetPackage + ".";
			packagesMap.forEach(new BiConsumer<String, Map<String, ShrgFileWriter>>() {
				@Override
				public void accept(String packageName, Map<String, ShrgFileWriter> writers) {
					store(writers, packageRow + packageName);
					if (packageName.compareTo(configurePackage) != 0)
						includeImports(packageName, writers);
				}
			});

			setConfigurationClassesInjectors(entities);
			configWriters.forEach(new BiConsumer<String, ShrgFileWriter>() {
				@Override
				public void accept(String fileName, ShrgFileWriter writer) {
					copyJavaTemplate(writer, fileName);
				}
			});

			entities.forEach(new BiConsumer<String, JSONObject>() {
				@Override
				public void accept(String entityName, JSONObject entityObj) {
					storeMessage("entity." + entityName + "=" + entityName);
					boolean isVerboseLiteral = isAttributeSet(entityObj, "verboseLiteral");
					createModelPojo(entityName, entityObj, isVerboseLiteral);
					createDaoClass(entityName, entityObj, entities, isVerboseLiteral);
					createServiceClass(entityName, entityObj);
					if (isVerboseLiteral)
						createConverterClass(entityName, entityObj);
					else {
						storeMessage("entities." + Names.plural(entityName) + "=" + Names.plural(entityName));
						createControllerClass(entityName, entityObj);
						createJsp(entityName, entityObj);
					}
					if (isAttributeSet(entityObj, "formPrintable"))
						createReportsClass(entityName, entityObj);
				}
			});

			if (app.withLanguageFile()) {
				storeMessage("entity.userPrefs=User's preferences");
				storeMessage("attr.menuAtTheTop=at the top");
				storeMessage("attr.menuOnTheLeft=on the left");
				storeMessage("attr.locale=Language and country");
				storeMessage("attr.hmenu=Menu position");
			}
		} catch (Exception e) {
			app.long_op_success = false;
		} finally {
			packagesMap.forEach(new BiConsumer<String, Map<String, ShrgFileWriter>>() {
				@Override
				public void accept(String packageName, Map<String, ShrgFileWriter> fileWriters) {
					closeFiles(fileWriters);
				}
			});
			closeFiles(lookUpJspsWriters);
			closeFiles(formJspsWriters);
			if (app.withLanguageFile())
				closeFile(dictionary_en_US_writer);
			closeFile(applicationConf_writer);
			closeFile(pomFileWriter);
			closeFile(eclipseProjectFileWriter);
			closeFile(eclipseComponentFileWriter);
		}
		app.outToConsole("Code generation : " + (app.long_op_success ? "Success" : "FAILURE") + " !");
		app.setWaitCursor(false);
	}
	
	private void createApplicationConfigurationFile() {
		write(applicationConf_writer, "#Hibernate Configuration");
		write(applicationConf_writer, "db.driver = " + app.txtjdbcDriverClass.getText());
		write(applicationConf_writer, "db.url = " + app.txtDbPath.getText());
		write(applicationConf_writer, "db.username = " + app.txtUserName.getText());
		write(applicationConf_writer, "db.password = " + app.txtPassword.getText());
		write(applicationConf_writer, "");
		write(applicationConf_writer, "#Hibernate Configuration");
		write(applicationConf_writer, "hibernate.dialect = " + app.txtHibDialect.getText());
		write(applicationConf_writer, "hibernate.show_sql = " + app.txtHibShowSql.getText());
		write(applicationConf_writer, "hibernate.format_sql = " + app.txtHibFormatSql.getText());
		write(applicationConf_writer, "hibernate.id.new_generator_mappings = false");
		write(applicationConf_writer, "entitymanager.shr_packages.to.scan = com.sprhibrad.framework.model");
		write(applicationConf_writer, "entitymanager.packages.to.scan = " + app.txtTargetPackage.getText() + ".model");
		write(applicationConf_writer, "");
		write(applicationConf_writer, "#SprHibRad application configuration");
		write(applicationConf_writer, "sprHibRad.appName = " + app.txtShrAppName.getText());
		write(applicationConf_writer, "sprHibRad.pagesize = " + app.txtShrPageSize.getText());
		write(applicationConf_writer, "sprHibRad.userprefsmenu = " + app.txtShrUserprefsMenu.getText());
		write(applicationConf_writer, "sprHibRad.dateStyle = " + app.txtShrDateStyle.getText());
		write(applicationConf_writer, "sprHibRad.currencyCountry = " + app.txtShrCurrencyCountry.getText());
		write(applicationConf_writer, "sprHibRad.langParameterizedReports = " + app.txtShrLangParameterizedReports.getText());
		write(applicationConf_writer, "sprHibRad.minPwdSize = " + app.txtShrMinPwdSize.getText());
		write(applicationConf_writer, "");
		write(applicationConf_writer, "#BIRT");
		write(applicationConf_writer, "birt.logDir = " + app.txtBirtLogDir.getText());
		write(applicationConf_writer, "");
		write(applicationConf_writer, "#Session");
		write(applicationConf_writer, "session.maxInactiveMinutes = " + app.txtSessionMaxInactiveMinutes.getText());
	}

	private void setConfigurationClassesInjectors(JSONObject entities) {
		configWriters.get("Initializer").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				writer.setIndentationCount(0);
				write(writer, "import " + app.targetPackage + ".configuration.Initializer");
				write(writer, "");
			}
		});
		configWriters.get("Initializer").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				writer.setIndentationCount(2);
				JSONArray menuEntities = (JSONArray) project.get("menuEntities");
				for (Object object : menuEntities)	 
					write(writer, "menuConfig.addItem(\"" + ShrgJSONArray.getKey((JSONObject) object)
							+ "\", null)");
			}
		});

		Vector<String> verboseLiterals = new Vector<String>();
		Vector<String> printables = new Vector<String>();
		entities.forEach(new BiConsumer<String, JSONObject>() {
			@Override
			public void accept(String entityName, JSONObject entityObj) {
				if (isAttributeSet(entityObj, "verboseLiteral"))
					verboseLiterals.add(entityName);
				if (isAttributeSet(entityObj, "formPrintable"))
					printables.add(entityName);
			}
		});
		configWriters.get("WebConf").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				for (String literalCollection : verboseLiterals) {
					write(writer, "import " + app.targetPackage
							+ ".converter." + toClassName(literalCollection)
							+ "Converter");
				}
				for (String printable : printables) {
					write(writer, "import " + app.targetPackage
							+ ".reports." + toClassName(printable)
							+ "Reports");
				}
			}
		});		
		configWriters.get("WebConf").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				writer.setIndentationCount(0);
				write(writer, "import " + app.targetPackage + ".configuration.BirtEngineFactory");
				write(writer, "");
			}
		});
		addOneRowInjector("WebConf", "@ComponentScan(basePackages = \"" + app.targetPackage + ",com.sprhibrad\")", 0);
		configWriters.get("WebConf").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				writer.setIndentationCount(1);
				for (String literalCollection : verboseLiterals) {
					write(writer, "");
					write(writer, "@Autowired");
					write(writer, toClassName(literalCollection)
							+ "Converter " + literalCollection
							+ "Converter");
				}
			} 
		});
		configWriters.get("WebConf").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				for (String printable : printables) {
					write(writer, "");
					write(writer, "@Bean");
					write(writer, toClassName(printable) + "Reports " + printable + "Reports() {");
					write(writer, "return new " + toClassName(printable) + "Reports()");
					write(writer, "}");
				}
			} 
		});
		configWriters.get("WebConf").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				writer.setIndentationCount(2);
				for (String literalCollection : verboseLiterals) {
					write(writer, "registry.addConverter(" + literalCollection + "Converter)");
				}
			}
		});
		String usersTable = getAttribute(project, "usersTable");
		String userName = getAttribute(project, "userColumn");
		String password = getAttribute(project, "passwordColumn");
		configWriters.get("WebSecurityConfig").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				writer.setIndentationCount(3);
				String rolesTable = getAttribute(project, "rolesTable");
				String userRolesTable = getAttribute(project, "userRolesTable");
				String roleFk = getAttribute(project, "roleFk");
				String userFk = getAttribute(project, "userFk");
				String roleName = getAttribute(project, "roleColumn");
				write(writer, ".usersByUsernameQuery(\"select " + userName + " username, " + 
									password + " password, true enabled from " + usersTable + 
									" where username=?\")", true);
				write(writer, ".authoritiesByUsernameQuery(", true);
				writer.setIndentationCount(4);
				write(writer, "\"SELECT " + usersTable + "." + userName
						+ " username, CONCAT('ROLE_', " + rolesTable + "." + roleName + ") role \" +", true);
				write(writer, "\"FROM " + usersTable + " \" +", true);
				write(writer, "\"INNER JOIN " + userRolesTable + " ON " + usersTable + ".id = " 
							+ userRolesTable + "." + userFk + " \" +", true);
				write(writer, "\"INNER JOIN " + rolesTable + " ON " + rolesTable + ".id = " + 
							userRolesTable + "." + roleFk + " \" +", true);
				write(writer, "\"where " + usersTable + "." + userName + " =?\")", true);
			}
		});
		configWriters.get("WebSecurityConfig").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				writer.setIndentationCount(3);
				entities.forEach(new BiConsumer<String, JSONObject>() {
					@Override
					public void accept(String entityName, JSONObject entityObj) {
						JSONObject roles = (JSONObject) app.shrgJsonPeeker.peek(entityObj, new String[] {"roles"}).hostingObj;
						if (roles != null) {
							roles.forEach(new BiConsumer<String, JSONObject>() {
								@Override
								public void accept(String role, JSONObject roleObj) {
									JSONObject roleAccessModes = (JSONObject) app.shrgJsonPeeker.peek(roleObj, new String[] {"roleAccessModes"}).hostingObj;
									if (roleAccessModes != null && roleAccessModes.size() > 0)
										roleAccessModes.forEach(new BiConsumer<String, JSONObject>() {
											@Override
											public void accept(String roleAccessMode, JSONObject roleAccessModeObj) {
												write(writer, ".antMatchers(\"/" + entityName + "/" + roleAccessMode + "/**\").hasRole(\"" + role + "\")", true);
											}
										});
									else
										write(writer, ".antMatchers(\"/" + entityName + "/**\").hasRole(\"" + role + "\")", true);
								}
							});
						}						
					}
				}
				);				
			}
		});
		configWriters.get("UserManagerImpl").addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
				write(writer, "import " + app.targetPackage + ".model." + toClassName(usersTable));
			}
		});
		addOneRowInjector("UserManagerImpl", "private IShrService<" + toClassName(usersTable) + "> " + usersTable + "Service", 1);
		addOneRowInjector("UserManagerImpl", "return \""  + usersTable + "\"", 2);
		addOneRowInjector("UserManagerImpl", "return ((" + toClassName(usersTable) + ") entity)." + accessorName(password, "get") + "()", 2);
		addOneRowInjector("UserManagerImpl", "((" + toClassName(usersTable) + ") entity)." + accessorName(password, "set") + "(clearPwd)", 2);
		addOneRowInjector("UserManagerImpl", "return ((" + toClassName(usersTable) + ") entity)." + accessorName(userName, "get") + "()", 2);
		addOneRowInjector("UserManagerImpl", "return " + usersTable + "Service", 2);
		addOneRowInjector("UserManagerImpl", "return \"" + userName + "\"", 2);
	}

	private void addOneRowInjector(String className, String row, int indentValue) {
		configWriters.get(className).addInjector(new Injector() {
			@Override
			public void inject(ShrgFileWriter writer) {
					writer.setIndentationCount(indentValue);
					write(writer, row);
			}
		});
	}
	
		

	protected void createConverterClass(String entityName, JSONObject entityObj) {
		ShrgFileWriter writer = converterWriters.get(entityName);
		String className = toClassName(entityName);
		writeModelAndServiceImports(writer, className);

		write(writer, "");
		write(writer, "@Component");
		write(writer, "public class " + className
				+ "Converter  extends VerboseLiteralConverter<" + className
				+ ">{");
		write(writer, "@Autowired");
		write(writer, "IShrService<" + className + "> " + entityName + "Service");
		write(writer, "@Override");
		write(writer, "protected " + className
				+ " getObject(Serializable id) {");
		write(writer, "return " + entityName
				+ "Service.getObject(Integer.parseInt((String) id))");
		write(writer, "}");
		write(writer, "}");
	}
	
	class ReportCriterium {
		String fieldArg;
		String operatorArg;
	}
	
	protected void createReportsClass(String entityName, JSONObject entityObj) {
		ShrgFileWriter writer = reportsWriters.get(entityName);
		String className = toClassName(entityName);
		write(writer, "import " + app.targetPackage + ".model." + className);
		ShrgJSONArray criteriaFields = app.shrgJsonPeeker.peek(entityObj, new String[] {"criteriaFields"}).array;
		JSONObject criteriaFieldObj;
		int count = 1;
		Vector<ReportCriterium> criteria = new Vector<ReportCriterium>();
		ReportCriterium criterium;
		for (Object object : criteriaFields) {
			criterium = new ReportCriterium();
			criterium.fieldArg = ShrgJSONArray.getKey((JSONObject) object);
			criteriaFieldObj = ShrgJSONArray.getValue((JSONObject) object);
			criterium.operatorArg = isAttributeSet(criteriaFieldObj, "withOp") ? (criterium.fieldArg + "_op" + (count > 1 ? String.valueOf(count) : "" )) : null;
			criteria.add(criterium);
		}
		write(writer, "");
		write(writer, "@Service");
		write(writer, "@Transactional");
		write(writer, "public class " + className + "Reports  extends ShrService<" + className + ">   {");
		write(writer, "");
		write(writer, "@Autowired");
		write(writer, "private IShrDao<" + className + "> " + entityName + "DAO");
		write(writer, "");
		write(writer, "@Override");
		write(writer, "protected IShrDao<" + className + "> getDao() {");
		write(writer, "return " + entityName + "DAO");
		write(writer, "}");
		write(writer, "");
		write(writer, "public List<" + className + "> list(String criteria[], String criteria_op[], String orders, String orientations) {");
		write(writer, "DataSetClauses clauses = new DataSetClauses()");
		int index = 0;
		for (ReportCriterium reportCriterium : criteria) {
			write(writer, "clauses.addCriterionForReport(\"" + reportCriterium.fieldArg + "\", criteria_op[" + index + "], criteria[" + index + "])");
			index++;
		}
		write(writer, "clauses.loadOrderClauses(orders, orientations)");
		write(writer, "return super.getObjects(0, clauses, 0)");
		write(writer, "}");
		write(writer, "}");
	}

	private void jspHeading(ShrgFileWriter writer) {
		write(writer, "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>");
		write(writer, "<%@ taglib prefix=\"form\" uri=\"http://www.springframework.org/tags/form\"%>");
		write(writer, "<%@ taglib prefix=\"shr\" uri=\"/WEB-INF/SprHibRad.tld\"%>");
		write(writer, "<!DOCTYPE html >");
		write(writer, "<html>");
	}
	
	protected void createJsp(String entityName, JSONObject entityObj) {
		ShrgFileWriter writer = formJspsWriters.get(entityName);
		jspHeading(writer);
		writer.setIndentationIncrease(1);
		write(writer, "<shr:dataForm modelAttribute=\"" + entityName
							+ "\" entityId=\"${" + entityName + ".id}\">");
		write(writer, "<table>");
		write(writer, "<tbody>");
		writer.setIndentationIncrease(0);
		writer.increaseIndent();
		writeBody(entityName, entityObj, writer);
		writer.setIndentationIncrease(-1);
		write(writer, "</tbody>");
		write(writer, "</table>");
		writer.setIndentationIncrease(0);
		write(writer, "<shr:statusBar />");
		write(writer, "<shr:formButtons />");

		JSONArray childrenEntities = (JSONArray) entityObj.get("childrenEntities");
		ShrgJSONArray detailsEntities = new ShrgJSONArray((JSONArray) entityObj.get("detailsEntities"));
		DetailDescriptor detailDescriptor = null;
		ShrgJSONArray detailFields = null;
		String childEntityName = null;
		boolean viewProperty;
		boolean noDelete;
		boolean noAdd;
		JSONObject detailEntityObj;
		for (Object childEntity : childrenEntities) {
			childEntityName = ShrgJSONArray.getKey((JSONObject) childEntity);		
			detailDescriptor = app.getDetailEntityFromTheChildOne(
													childEntityName,
													ShrgJSONArray.getValue((JSONObject) childEntity));
			detailEntityObj = (JSONObject) detailsEntities.get(detailDescriptor.entity);
			detailFields = app.shrgJsonPeeker.peek(detailEntityObj,  
					new String[] { "detailFields" }).array;
			viewProperty = isAttributeSet(detailEntityObj, "viewProperty");
			noDelete = isAttributeSet(detailEntityObj, "noDelete");
			noAdd = isAttributeSet(detailEntityObj, "noAdd");
			write(writer, "<shr:detailManager entity='" + childEntityName + "' "
					+ (detailDescriptor.m2mChildFkMember == null ? "" :
						" property='" + memberName(detailDescriptor.m2mChildFkMember) + "' ")
					+ " fields='" + commaSeparatedList(getFields(detailEntityObj,  detailDescriptor.entity, "detailFields", false, null, null), "") + "' "
					+ (viewProperty ? (" viewProperty='true' ") : "")
					+ (noDelete ? (" noDelete='true' ") : "")
					+ (noAdd ? (" noAdd='true' ") : "")
					+ "/>");
		}
		writer.setIndentationIncrease(-1);
		write(writer, "</shr:dataForm>");
		write(writer, "</html>");

		
		writer = lookUpJspsWriters.get(entityName);
		jspHeading(writer);
		writer.setIndentationIncrease(1);
		write(writer, "<shr:nvform modelAttribute=\"" + entityName + "\">");
		write(writer, "<shr:listManager>");
		
		JSONArray criteriaFields = (JSONArray) entityObj.get("criteriaFields");
		if (criteriaFields.size() > 0) {
			write(writer, "<shr:searchBox>");
			writer.setIndentationIncrease(0);
			writer.increaseIndent();
			String fieldName = null;
			boolean withOp;
			for (Object criteriaField : criteriaFields) {
				fieldName = ShrgJSONArray.getKey((JSONObject) criteriaField);
				withOp = isAttributeSet(ShrgJSONArray.getValue((JSONObject) criteriaField), "withOp");
				write(writer, "<shr:searchCriterion path=\"" + memberName(fieldName) + "\"" + (withOp ? (" withOp=\"true\"") : "") + ">");
				writer.increaseIndent();
				writeDataObject(writer, memberName(fieldName));
				writer.decreaseIndent();
				write(writer, "</shr:searchCriterion>");
			}
			writer.decreaseIndent();
			write(writer, "</shr:searchBox>");
			
		}
		write(writer, "<shr:statusBar />");
		writeResultManager(writer, entityObj, entityName);
		if (isAttributeSet(entityObj, "formPrintable"))
			write(writer, "<shr:iteratorButtons><shr:report name=\"" + entityName + "List\" /></shr:iteratorButtons>");
		else
			write(writer, "<shr:iteratorButtons />");
		writer.setIndentationIncrease(-1);
		write(writer, "</shr:listManager>");
		write(writer, "</shr:nvform>");
		write(writer, "</html>");
	
	}
	
	enum DataItemType {
		text,
		check,
		radio,
		combo,
		image, 
		handout, 
		detailProvider,
		no_Type
	}
	
	private void writeBody(String entityName, JSONObject entityObj, ShrgFileWriter writer) {
		HashMap<String, String> literalCollectionsLabel = new HashMap<String, String>(); 
		HashMap<String, String> targetEntities = new HashMap<String, String>(); 
		HashSet<String> fks = new FkManager(entityObj, writer) {
			@Override
			public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) {
				if (isVerboseLiteral(targetEntity)) {
					ShrgJSONArray formFields = app.shrgJsonPeeker.peek(project, new String[] {"entities", targetEntity, "formFields"}).array;
					literalCollectionsLabel.put(fkField, ShrgJSONArray.getKey((JSONObject) formFields.get(0)));
					targetEntities.put(fkField, targetEntity);
					if (formFields.size() > 1)
						app.outToConsole(targetEntity + " is set to be a 'Verbose literal' : form fields following the first one will not be processed ! ");
				}
			}
		}.fks;	
		JSONArray formFields = (JSONArray) entityObj.get("formFields");
		String fieldName;
		JSONObject fieldObj;
		FieldDescriptor fieldDescriptor;
		DataItemType dataItemType = null;
		String previewPath = null;
		HashSet previewFields = new HashSet();
		for (Object formField : formFields) {
			fieldName = ShrgJSONArray.getKey((JSONObject) formField);
			fieldObj = ShrgJSONArray.getValue((JSONObject) formField);
			if (fks.contains(fieldName)) {
				String labelField = literalCollectionsLabel.get(fieldName);
				writeDataItem(writer, 
									labelField == null ? memberName(fieldName) : fieldName, 
									labelField == null ? DataItemType.detailProvider : DataItemType.combo, 
									isAttributeSet(fieldObj, "formFieldReadOnly"), 
									false, 
									null, labelField, targetEntities.get(fieldName), entityName);					
			} else {
				fieldDescriptor = fieldMetadata(entityName, fieldName);
				switch (fieldDescriptor.javaType()) {
				case  "String":       
				case  "Float":       
				case  "Double":       
				case  "Date":       
				case  "DateTime":     
					dataItemType = DataItemType.text;
					break;
				case  "Long":       
				case  "Integer":      
					dataItemType = isAttributeSet(fieldObj, "radioButtons") ? DataItemType.radio : isAttributeSet(fieldObj, "formFieldBoolean") ? DataItemType.check : DataItemType.text;
					break;
				case  "Boolean":      
					dataItemType = isAttributeSet(fieldObj, "radioButtons") ? DataItemType.radio : DataItemType.check;
					break;
				case  "byte[]":      
					previewPath = getAttribute(fieldObj, "previewPath");
					if (previewPath != null)
						previewFields.add(previewPath);
					dataItemType = isAttributeSet(fieldObj, "formFieldDocument") ? DataItemType.handout : 
													previewPath == null ? DataItemType.no_Type : DataItemType.image;
					break;
				}
				if ( ! previewFields.contains(fieldName))
						writeDataItem(writer, fieldName, dataItemType, isAttributeSet(fieldObj, "formFieldReadOnly"), 
								isAttributeSet(fieldObj, "formFieldPassword"), previewPath, null, null, entityName);
			}
		}
	}
	
	private boolean isBinary(DataItemType type) {
		return type == DataItemType.image || type == DataItemType.handout;
	}
	
	private void writeDataItem(ShrgFileWriter writer, String fieldName, DataItemType type, 
								boolean readOnly, boolean password, String previewPath, String labelField, String targetEntity, String entityName) {
		String member =  memberName(fieldName);
		write(writer, "<tr>");
		writer.setIndentationIncrease(1);
		write(writer, "<shr:" + (isBinary(type) ? "binary" : "data") 
				+ "Item  path='" + memberName(member) + "' "
				+ (readOnly ? " readOnly='true' " : "")
				+ (previewPath==null ? "" : (" previewPath='" + previewPath + "' "))
				+ " >");
		storeRendering(writer, member, type, labelField, targetEntity, entityName, password);
		writeDataObject(writer, member);
		writer.setIndentationIncrease(-1);
		write(writer, "</shr:" + (isBinary(type) ? "binary" : "data") + "Item>");
		writer.setIndentationIncrease(0);
		if(type==DataItemType.detailProvider)
			write(writer, "<shr:detailProvider detailMember='" + member
					+ "' targetId=\"${" + entityName
					+ "." + member
					+ ".id}\" "
					+ (readOnly ? " readOnly='true'" : "")
					+ " />");
		writer.decreaseIndent();
		write(writer, "</tr>");
	}
	
	private void writeDataObject(ShrgFileWriter writer, String fieldName) {
		Vector<String> rendering = dataObjectRenderings.get(fieldName);
		for (String row : rendering) {
			if (row.indexOf("IND_")== 0)
				writer.setIndentationIncrease(Integer.valueOf(row.substring(4)));
			else
				write(writer, row);				
		}
	}
	
	private void storeRendering(ShrgFileWriter writer, String fieldName, DataItemType type, 
								String labelField, String targetEntity, String entityName, boolean password) {
		Vector<String> rendering = new Vector<String>();
		dataObjectRenderings.put(fieldName, rendering);
		String literal = null;
		switch(type) {
		case text:
			rendering.add((password ? "<shr:sinput type=\"password\" />" :
										(fieldMetadata(entityName, fieldName).precision > 32 ?  "<shr:textArea />" : "<shr:input />")) 
							+ "<shr:errors />");
			break;
		case check:
			rendering.add("<shr:checkBox /><shr:errors />");
			break;
		case image:
			rendering.add("<shr:image editable='true' />");
			break;
		case handout:
			rendering.add("<shr:handout editable='true' />");
			break;
		case radio:
			rendering.add("<shr:errors />");
			literal = fieldName + "_01";
			rendering.add("<shr:radioButton value='0' valueLabel='attr." + literal + "' /><br>");
			storeMessage("attr." + literal + "=" + literal);
			rendering.add("IND_0");
			literal = fieldName + "_02";
			rendering.add("<shr:radioButton value='1' valueLabel='attr." + literal + "' /><br>");
			storeMessage("attr." + literal + "=" + literal);
			rendering.add("<%-- <shr:radioButton value=<N> valueLabel='attr.<keyN>' /><br> --%>");
			break;
		case combo:
			rendering.add("<shr:select>");
			rendering.add("<form:option value=\"${null}\" label=\"----\" />");
			rendering.add("IND_0");
			rendering.add( "<form:options items=\"${" + Names.plural(targetEntity) + "}\" itemLabel=\"" + labelField + "\" itemValue=\"id\" />");
			rendering.add("IND_-1");
			rendering.add("</shr:select>");
			rendering.add("IND_0");
			rendering.add("<shr:errors />");
			rendering.add("IND_-1");
 			break;
		case detailProvider:
			rendering.add("<table class='detailProvider' width='100%'><tr><td>${shr:renderEntity(" + entityName + "." + fieldName + ", pageContext)}</td>");
			rendering.add("IND_0");
			rendering.add("<td><form:input type=\"hidden\" path=\"id\" /></td>");
			rendering.add("</tr></table>");
			rendering.add("IND_-1");
			break;
		}
	}

	private void writeResultManager(ShrgFileWriter writer, JSONObject entityObj, String entityName) {
		String fields = commaSeparatedList(getFields(entityObj,  entityName, "resultFields", false, null, null), "");
		Vector<String> orientations = new Vector<String>();
		Vector<String> orders = getFields(entityObj,  entityName, "orderedFields", false, orientations, "resultColOrdVersus")  ;
		
		write(writer, "<shr:resultManager fields='" + fields
				+ "' order='" + commaSeparatedList(orders, "")
				+ "' orientation='" + commaSeparatedList(orientations, "")
				+ "'  />");
	}

	protected void createControllerClass(String entityName, JSONObject entityObj) {
		ShrgFileWriter writer = controllerWriters.get(entityName);
		String className = toClassName(entityName);
		write(writer, "import " + app.targetPackage + ".model." + className);
		new FkManager(entityObj, writer) {
			@Override
			public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) {
				writeModelAndServiceImports(writer, toClassName(targetEntity));
			}			
		};
		JSONArray childrenEntities = (JSONArray) entityObj.get("childrenEntities");
		for (Object childEntity : childrenEntities)
			writeModelAndServiceImports(writer, toClassName(ShrgJSONArray.getKey((JSONObject) childEntity)));
		write(writer, "");
		write(writer, "@Controller");
		write(writer, "public class " + className + "Controller extends ImplShrController<" + className + "> {" );
		writeServiceMember(writer, entityName);
		for (Object childEntity : childrenEntities)
			writeServiceMember(writer, ShrgJSONArray.getKey((JSONObject) childEntity));
		new FkManager(entityObj, writer) {
			@Override
			public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) {
				if (isVerboseLiteral(targetEntity)) 
					writeServiceMember(writer, targetEntity); 
			}			 			
		};
		
		write(writer, "");
		write(writer, "@Override");
		write(writer, "protected IShrService<" + className + "> getService() {");
		write(writer, "return " + entityName + "Service");
		write(writer, "}");
		
		write(writer, "");
		write(writer, "@Override");
		write(writer, "protected " + className + " getEntityInstance() {");
		write(writer, "return new " + className + "()");
		write(writer, "}");      
		
		int verboseLiteralsCounter = new FkManager(entityObj, writer) {
			Vector<String> verboseLiterals;
			@Override
			protected void prolog() {
				verboseLiterals = new Vector<String>();
				counter = 0;
			}
			@Override
			public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) { // actually only preparing takes place here : epilog takes rendering responsibility instead
				if (app.shrgJsonPeeker.isAttributeSet(project, new String[] {"entities", targetEntity, "verboseLiteral", "value"})) 
					verboseLiterals.add(targetEntity);
			}
			@Override
			protected void epilog() {
				if (verboseLiterals.size() > 0) {
					write(writer, "");
					write(writer, "@Override");
					write(writer, "protected void addEditingAttributes(ModelAndView modelAndView, " + className + " obj) {");
					for (String targetEntity : verboseLiterals){
						String memberClassName = toClassName(targetEntity);
						write(writer, "modelAndView.addObject(\"" + Names.plural(targetEntity) + "\", " + targetEntity + "Service.getObjects(null, null, null))");
					}
					write(writer, "}");
				}
				counter = verboseLiterals.size();
			}
		}.counter();

		write(writer, "");
		write(writer, "@RequestMapping(value = \"/" + entityName + "/add\")");
		write(writer, "public ModelAndView newObject(HttpServletRequest request, @RequestParam(required=false) String fk) {");
		write(writer, "return super.newObject(request, fk)");
		write(writer, "}");
		
		write(writer, "");
		write(writer, "@RequestMapping(value = \"/" + entityName + "/save\")");
		write(writer, "protected ModelAndView saveObject(@Valid " + className + " object, BindingResult result, HttpServletRequest request) {");
		write(writer, "return super.saveObject(object, result, request)");
		write(writer, "}");

		
		write(writer, "");
		write(writer, "@RequestMapping(value = \"/" + entityName + "/list/{iteration}\")");
		write(writer, "protected ModelAndView objectList(@PathVariable Integer iteration, " + className
																+ " object, BindingResult result, HttpServletRequest request) {");
		write(writer, "return super.objectList(iteration, object, result, request, " + imagesParam(entityObj, entityName, "formFields") + ")");
		write(writer, "}");

		
		write(writer, "");
		write(writer, "@RequestMapping(value = \"/" + entityName + "/choices/{iteration}\")");
		write(writer, "protected ModelAndView objectSelectionList(@PathVariable Integer iteration, " + className + " object, " + 
						"BindingResult result, HttpServletRequest request, @RequestParam String targetMember) {");
		write(writer, "return super.objectSelectionList(iteration, object, result, request, null, targetMember)");
		write(writer, "}");
		
		write(writer, "");
		write(writer, "@RequestMapping(value=\"/" + entityName + "/edit/{id}\")");
		write(writer, "protected ModelAndView editObject(@PathVariable Integer id, HttpServletRequest request) {");
		write(writer, "return super.editObject(id, request)");
		write(writer, "}");
		
		write(writer, "");
		write(writer, "@RequestMapping(value=\"/" + entityName + "/view/{id}\")");
		write(writer, "protected ModelAndView viewObject(@PathVariable Integer id, HttpServletRequest request) {");
		write(writer, "return super.viewObject(id, request, " + imagesParam(entityObj, entityName, "formFields") + ")");
		write(writer, "}");

		write(writer, "");
		write(writer, "@RequestMapping(value=\"/" + entityName + "/update/{id}\")");
		boolean usersTable = getAttribute(project, "usersTable").compareTo(entityName) == 0;		
		write(writer, "public ModelAndView updateObject(@Valid " + className + " object, BindingResult result, HttpServletRequest request" + (usersTable ? "," : ") {"), true);
		if (usersTable) {
			int oldIndentCount = writer.getIndentationCount() + 1;
			writer.setIndentationCount(16);
			write(writer, "@RequestParam(required=false) String oldPwd, ");
			write(writer, "@RequestParam(required=false) String currPwd, ");
			write(writer, "@RequestParam(required=false) String pwd2) {");
			writer.setIndentationCount(oldIndentCount);
			write(writer, "if (request.getParameter(\"currPwd\") != null)", true);
			writer.increaseIndent();
			write(writer, "super.checkPasswords(object, result, oldPwd, currPwd, pwd2, request)");
			writer.decreaseIndent();
		}
		write(writer, "return super.updateObject(object, result, request)");
		write(writer, "}");
		
		write(writer, "");
		write(writer, "@RequestMapping(value=\"/" + entityName + "/select/{id}\")");
		write(writer, "public String selectObject(@PathVariable Integer id, HttpServletRequest request, @RequestParam String targetMember) {");
		write(writer, "return super.selectObject(id, request, targetMember)");
		write(writer, "}");
		
		write(writer, "");
		write(writer, "@RequestMapping(value=\"/" + entityName + "/delete/{id}\")");
		write(writer, "public String deleteObject(@PathVariable Integer id, HttpServletRequest request) {");
		write(writer, "return super.deleteObject(id, request)");
		write(writer, "}");
	    
		boolean detailsWithImages = false;
		if (childrenEntities.size() > 0) {
			write(writer, "");
			write(writer, "@Override");
			write(writer, "protected void addDetailsAttributes(ModelAndView modelAndView, " + className + " obj, HttpServletRequest request) {");
			for (Object childEntity : childrenEntities)
				if (write_addDetailsAttribute(writer, entityObj, entityName, (JSONObject) childEntity))
					detailsWithImages = true;
			write(writer, "}");
		}
		
		JSONArray criteriaFields = (JSONArray) entityObj.get("criteriaFields");
		if (criteriaFields.size() > 0) {
			write(writer, "");
			write(writer, "@Override");
			write(writer, "protected void setListFilter(" + className + " entity, HttpServletRequest request) {");
			String memberName = null;
			for (Object criteriaField : criteriaFields) {
				memberName = memberName(ShrgJSONArray.getKey((JSONObject) criteriaField));
				write(writer, "addToFilter(\"" + memberName + "\", entity." + accessorName(memberName, "get") + "(), request)");
			}
			write(writer, "}");
		}

		boolean imageMethods = false;
		boolean documentMethods = false;
		JSONArray formFields = (JSONArray) entityObj.get("formFields");		
		JSONObject formFieldInnerObj = null;
		for (Object formField : formFields) {
			formFieldInnerObj = (JSONObject) ShrgJSONArray.getValue((JSONObject) formField);
			if (! imageMethods && getAttribute(formFieldInnerObj, "previewPath") != null)
				imageMethods = true;
			if (! documentMethods && isAttributeSet(formFieldInnerObj, "formFieldDocument"))
				documentMethods = true;			
		}
		
		if (imageMethods || documentMethods) {
			write(writer, "");
			write(writer, "@RequestMapping(value=\"/" + entityName + "/uploadBinary\")");
			write(writer, "public ModelAndView uploadBinary(" + className + " object, @RequestParam String op, @RequestParam String pp, HttpServletRequest request) {");
			write(writer, "return super.uploadBinary(object, op, pp, request)");
			write(writer, "}");
			
			write(writer, "");
			write(writer, "@RequestMapping(\"/" + entityName + "/doUploadBinary\")");
			write(writer, "public ModelAndView doUploadBinary(@RequestParam MultipartFile file, @RequestParam String op, @RequestParam String pp, HttpServletRequest request) {");
			write(writer, "return super.doUploadBinary(file, op, pp, request)");
			write(writer, "}");
			
			write(writer, "");
			write(writer, "@RequestMapping(value=\"/" + entityName + "/deleteBinary\")");
			write(writer, "public ModelAndView deleteBinary(@RequestParam String op, @RequestParam String pp, HttpServletRequest request) {");
			write(writer, "return super.deleteBinary(op, pp, request)");
			write(writer, "}");
		}

		
		if (imageMethods || detailsWithImages) {
			write(writer, "");
			write(writer, "@ResponseBody");
			write(writer, "@RequestMapping(value=\"/" + entityName + "/{key}.{ext}\")");
			write(writer, "public byte[] getImage(@PathVariable String key, @PathVariable String ext) {");
			write(writer, "return super.getImage(key, ext)");
			write(writer, "}");
				
			write(writer, "");
			write(writer, "@RequestMapping(\"/" + entityName + "/viewImage\")");
			write(writer, "public String viewImage(@RequestParam Integer id, @RequestParam String target, HttpServletRequest request) {");
			write(writer, "return super.viewBinary(id, target, request, true)");
			write(writer, "}");
		}
		
		if (documentMethods) {
			write(writer, "");
			write(writer, "@RequestMapping(\"/" + entityName + "/viewHandout\")");
			write(writer, "public String viewHandout(@RequestParam String target, HttpServletRequest request) {");
			write(writer, "return super.viewBinary(null, target, request, false)");
			write(writer, "}");
		}
		
		if (isAttributeSet((JSONObject) entityObj, "formPrintable")) {
			write(writer, "");
			write(writer, "@RequestMapping(value = \"/" + entityName + "/reports\")");
			write(writer, "protected void reportHandler(@RequestParam Map<String, String> map, HttpServletRequest request, HttpServletResponse response) {");
			write(writer, "HashMap<String, Object> params = new HashMap<String, Object>()");
			String fieldName = null;
			JSONObject criteriaFieldObj = null;
			for (Object criteriaField : criteriaFields) {
				fieldName = ShrgJSONArray.getKey((JSONObject) criteriaField);
				write(writer, "params.put(\"" + fieldName + "\", map.get(\"" + fieldName + "\"))");
				criteriaFieldObj = ShrgJSONArray.getValue((JSONObject) criteriaField);
				if (isAttributeSet(criteriaFieldObj, "withOp"))
					write(writer, "params.put(\"" + fieldName + "_op\", map.get(\"" + fieldName + "\" + Utils.operatorIdSuffix))");
			}
			write(writer, "params.put(\"orders\", map.get(\"_iterResult-order\"))");
			write(writer, "params.put(\"orientations\", map.get(\"_iterResult-orientation\"))");
			write(writer, "super.reportHandler(map, params, request, response)");
			write(writer, "}");
		}

		if (isAttributeSet(entityObj, "shInitbinder")) {
			write(writer, "");
			write(writer, "@Override");
			write(writer, "protected void shrInitBinder(WebDataBinder binder) {");
			write(writer, "//binder.addValidators(....a validator......);");
			write(writer, "}");
		}

		if (isAttributeSet(entityObj, "validate")) {
			write(writer, "");
			write(writer, "@Override");
			write(writer, "protected boolean validate(" + className + " object, BindingResult result) {");
			write(writer, "boolean retVal = false");
			write(writer, "// if (....condition .....)");
			write(writer, "//    result.rejectValue(....params......);");
			write(writer, "return retVal || super.validate(object, result)");
			write(writer, "}");
		}
		
		JSONObject fks = (JSONObject) entityObj.get("fks"); 
		if (fks.size() - verboseLiteralsCounter >= 1) { //  navigates to at least one relation 
			write(writer, "");
			write(writer, "@RequestMapping(value = \"/" + entityName + "/freeze/**\")");
			write(writer, "public String freezeObject(" + className + " object, @RequestParam String redir, @RequestParam String targetMember, @RequestParam String action,HttpServletRequest request) {");
			write(writer, "return super.freezeObject(object, request, redir, targetMember, action)");
			write(writer, "}");
		}
	
		write(writer, "}");
	}

	private boolean write_addDetailsAttribute(ShrgFileWriter writer, JSONObject entityObj, String entityName, JSONObject childEntity) {
		String parentFk = getAttribute((JSONObject) ShrgJSONArray.getValue((JSONObject) childEntity), "parentFk");
		String childFk = getAttribute((JSONObject) ShrgJSONArray.getValue((JSONObject) childEntity), "childFk");
		String childEntityName = ShrgJSONArray.getKey((JSONObject) childEntity);
		String detailsEntity = childFk == null ? childEntityName :
									app.shrgJsonPeeker.peek(project, new String[] {
										"entities", childEntityName, "fks", childFk, "fieldTargetEntity", "value"
											}).value;
		if (parentFk == null) {
			app.outToConsole("Select e 'parentFK' value for  '" + childEntityName + "' entity, since child entity of '" + entityName + "' !");
			return false;
		} else {
			JSONObject detailsObject = (JSONObject) app.shrgJsonPeeker.peek(entityObj, new String[] {"detailsEntities", detailsEntity}).hostingObj;
			ShrgJSONArray detailOrderedFields = app.shrgJsonPeeker.peek(entityObj, new String[] {"detailsEntities", detailsEntity, "detailOrderedFields"}).array;
			StringBuilder order = new StringBuilder("");
			StringBuilder orientation = new StringBuilder("");
			String versus = null;
			boolean first = true;	
			for (Object detailOrderedField : detailOrderedFields) {
				if ( ! first) {
					order.append(",");
					orientation.append(",");
				}
				order.append(ShrgJSONArray.getKey((JSONObject) detailOrderedField));
				versus = getAttribute((JSONObject) ShrgJSONArray.getValue((JSONObject) detailOrderedField), "detailColOrdVersus");
				orientation.append(versus == null ? "asc" : versus);
				if (first)
					first = false;
			}
			String imagesParam = imagesParam(detailsObject, detailsEntity, "detailFields");
			write(writer, "addDetailsAttribute(\"" + Names.plural(childEntityName) + "\", " + childEntityName + "Service, " + 
												(childFk == null ? null : ("\"" + memberName(childFk)) + "\"") +
												", obj, modelAndView, request, \"" + order + "\", \"" + orientation + 
												"\", \"" + memberName(parentFk) + "\", " + imagesParam + ")");
			return imagesParam != null;
		}
	}

	private String imagesParam(JSONObject entityObj, String entityName, String domain) {
		Vector<String> previeFields = getFields(entityObj, entityName, domain, true, null, null);
		return previeFields.size() == 0 ? null : "new String[] {" + commaSeparatedList(previeFields, "\"") + "}"	;
	}

	class myBool {
		boolean value;
	}
	
	private String commaSeparatedList(Object fields, String embracingChar) {
		StringBuilder lab = new StringBuilder(""); 
		myBool first = new  myBool();
		first.value = true;
		if (fields instanceof JSONArray)
			for (Object field : ((JSONArray) fields))
				cat(first, lab, embracingChar, ShrgJSONArray.getKey((JSONObject) field));
		else 
			for (String field : ((Vector<String>) fields))
				cat(first, lab, embracingChar, field);
		return lab.toString();		
	}
	
	private void cat(myBool first, StringBuilder lab, String embracingChar, String field) {
		if (!first.value)
			lab.append(",");
		lab.append(embracingChar + field + embracingChar);
		if (first.value)
			first.value = false;
	}
	
	private Vector<String> getFields(JSONObject entityObj, String entityName, String domain, 
									boolean previewPathsOnly, Vector<String> innerValues, String innerSubDomain) {
		Vector<String> retVal = new Vector<String>();
		ShrgJSONArray array = app.shrgJsonPeeker.peek(entityObj, new String[] {domain}).array;
		String field = null;
		String previewPath = null;
		boolean preview;
		String innerValue;
		for (Object obj : array) {
			preview = false;
			field = ShrgJSONArray.getKey((JSONObject) obj);
			if (innerValues != null) {
				JSONObject innerObj = ShrgJSONArray.getValue((JSONObject) obj);
				innerValue = getAttribute(innerObj, innerSubDomain);
				if (innerValue != null)
					innerValues.add(innerValue);
			}
			if (fieldMetadata(entityName, field).javaType().compareTo("byte[]") == 0) {
				previewPath = app.shrgJsonPeeker.peek(project, new String[] {"entities", entityName, "formFields", field, "previewPath", "value"}).value;
				if (previewPath != null)
					preview = true;
			}
			if (! previewPathsOnly || preview)
				retVal.add(preview ? previewPath : memberName(field));
			if (preview && ! previewPathsOnly)
				retVal.add("[" + field + "]");
		}
		return retVal;
	}
	
	private FieldDescriptor fieldMetadata(String tableName, String fieldName) {
		return app.tablesInfo.tablesMap.get(tableName).colsInfo.fieldsMap.get(fieldName);
	}
	
	private void writeServiceMember(ShrgFileWriter writer, String entityName) {
		write(writer, "");
		write(writer, "@Autowired");
		write(writer, "private IShrService<" + toClassName(entityName) + "> " + entityName + "Service" );
	}

	private void writeModelAndServiceImports(ShrgFileWriter writer, String className) {
		writeModelAnd_imports(writer, className, "service", "Service");
	}
	private void writeModelAndDaoImports(ShrgFileWriter writer, String className) {
		writeModelAnd_imports(writer, className, "dao", "Dao");
	}
	private void writeModelAnd_imports(ShrgFileWriter writer, String className, String andLowC, String andUpperC) {
		write(writer, "import " + app.targetPackage + "." + andLowC + "." + className + andUpperC);
		write(writer, "import " + app.targetPackage + ".model." + className);
	}
	
	protected void createServiceClass(String entityName, JSONObject entityObj) {
		ShrgFileWriter writer = serviceWriters.get(entityName);
		String className = toClassName(entityName);
		write(writer, "import " + app.targetPackage + ".model." + className);
		new FkManager(entityObj, writer) {
			@Override
			public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) {
				if (isVerboseLiteral(targetEntity)) 
					writeModelAndDaoImports(writer, Generator.this.toClassName(targetEntity));
			}
		};

		write(writer, "");
		write(writer, "@Service");
		write(writer, "@Transactional");
		write(writer, "public class " + className + "Service extends ShrService<" + className + "> {" );
		
		write(writer, "");
		write(writer, "@Autowired");
		write(writer, "private IShrDao<" + className + "> " + entityName + "Dao");
		
		write(writer, "");
		write(writer, "@Override");
		write(writer, "protected IShrDao<" + className + "> getDao() {");
		write(writer, "return " + entityName + "Dao");
		write(writer, "}");
		write(writer, "");
		write(writer, "}" );
	}

	boolean isVerboseLiteral(String entityName) {
		return app.shrgJsonPeeker.isAttributeSet(project, new String[] {"entities", entityName, "verboseLiteral", "value"});
	}
	protected void createDaoClass(String entityName, JSONObject entityObj, JSONObject entitiesObj, boolean isVerboseLiteral) {
		ShrgFileWriter writer = daoWriters.get(entityName);
		String className = toClassName(entityName);
		write(writer, "import " + app.targetPackage + ".model." + className);
		write(writer, "");
		write(writer, "@Repository");
		write(writer, "public class " + className + "Dao extends ShrDao<" + className + "> {" );
		
		if ( ! isVerboseLiteral) {
			TableDescriptor tabDescr = app.tablesInfo.tablesMap.get(entityName);
			write(writer, "@Override");
			write(writer, "protected void update(" + className + " object, "  + className + " objectInDb) {");
			HashSet<String> fks = new FkManager(entityObj, writer) {
				@Override
				public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) {
					updateLine(writer, memberName);
				}
			}.fks;
			JSONArray fieldsObj = (JSONArray) entityObj.get("formFields");
			if(fieldsObj != null && fieldsObj.size() > 0) {
				ShrgJSONArray shrgFormFields = new ShrgJSONArray(fieldsObj);
				for (FieldDescriptor field : tabDescr.colsInfo.fields) 
					if (shrgFormFields.get(field.name) != null && !fks.contains(field.name) && field.javaType().compareTo("byte[]") != 0)
						updateLine(writer, field.name);
			}
			write(writer, "}" );
		}
		write(writer, "");
		write(writer, "}" );
	}
	
	private void updateLine(ShrgFileWriter writer, String fieldName) {
		write(writer, "objectInDb." + accessorName(fieldName, "set") + "(object." + accessorName(fieldName, "get") + "())");
	}
	
	protected void createModelPojo(String entityName, JSONObject entityObj, boolean isVerboseLiteral) {
		boolean usersTable = getAttribute(project, "usersTable").compareTo(entityName) == 0;		
		ShrgFileWriter writer = modelWriters.get(entityName);
		if (usersTable)
			write(writer, "import com.sprhibrad.framework.model.UserTable;");

		write(writer, "");
		write(writer, "@Entity");
		write(writer, "@Table(name = \"" + entityName + "\")");
		write(writer, "public class " + toClassName(entityName) + 
						(isVerboseLiteral ? " extends VerboseLiteral" : 
							(" implements ShrEntity" + (usersTable ? ", UserTable" : ""))) + " {" );
		TableDescriptor tabDescr = app.tablesInfo.tablesMap.get(entityName);
		if (tabDescr.pkCols.size() > 1) 
			try {
				throw new Exception("SprHibRad: only one column is allowed in the PK definition");
			} catch (Exception e) {
				app.outToConsole(e);
			}
		else {
			HashSet<String> fks = new FkManager(entityObj, writer) {}.fks;	
			JSONArray fieldsObj = (JSONArray) entityObj.get("formFields");
			ShrgJSONArray shrgFormFields = new ShrgJSONArray(fieldsObj);
			String pkField = null;
			if (tabDescr.pkCols.size() == 1)
				pkField = tabDescr.pkCols.get(0);
			JSONObject fieldObj = null;
			int counter = 0;
			for (FieldDescriptor field : tabDescr.colsInfo.fields) {
				if (!fks.contains(field.name)) {
					if (pkField != null && field.name.compareTo(pkField) == 0 || isVerboseLiteral && field.name.compareTo("id") == 0 ) {
						write(writer, "");
						write(writer, "@Id");
						if ( ! isVerboseLiteral)
							write(writer, "@GeneratedValue");
						if (field.name.compareTo("id") != 0)	
							write(writer, "@Column(name=\"" + field.name + "\")");	
						write(writer, "private Integer id");
					} else {
						fieldObj = shrgFormFields.get(field.name);
						if (fieldObj != null) {
							if (! isVerboseLiteral) {
								String key = (isAttributeSet(fieldObj, "classSpecificDictio") ? (entityName + ".") : "") + 
												field.name;
								storeMessage("attr." + key + "=" + key);
							}
							writeFieldMember(writer, field, fieldObj, isVerboseLiteral);	
							counter++;
							if (isVerboseLiteral && field.name.compareTo(ShrgJSONArray.getKey((JSONObject) shrgFormFields.get(0))) == 0) {
								write(writer, "");
								write(writer, "@Override");
								write(writer, "public String literalField() {");
								write(writer, "return \"" + field.name + "\"");
								write(writer, "}");
							}
						}
					}
				}
			}
			for (FieldDescriptor field : tabDescr.colsInfo.fields) {
				fieldObj = shrgFormFields.get(field.name);
				if (!fks.contains(field.name) && (field.isID || fieldObj != null))
					writeGetterSetter(writer, (field.isID ? "id" : field.name), getType(fieldObj, field),  isVerboseLiteral);
			}					
			JSONArray childrenEntities = (JSONArray) entityObj.get("childrenEntities");
			if (childrenEntities != null) {
				String childEntity = null;
				JSONObject childEntityObj;
				for (Object obj : childrenEntities) {
					childEntityObj =  ShrgJSONArray.getValue((JSONObject) obj);
					childEntity = ShrgJSONArray.getKey((JSONObject) obj);
					String parentFK = getAttribute(childEntityObj, "parentFk");
					if (parentFK != null) {
						write(writer, "");
						write(writer, "@OneToMany(cascade = {CascadeType.REMOVE}, mappedBy=\"" + memberName(parentFK) + "\", orphanRemoval=true)");
						write(writer, "private Set<" + toClassName(childEntity) + "> " + Names.plural(childEntity) + " = new HashSet<" + toClassName(childEntity) + ">()");
						if (getAttribute(childEntityObj, "childFk") != null)
							writeGetterSetter(writer, Names.plural(childEntity), "Set<" + toClassName(childEntity) + ">");
					}
				}
			}
			new FkManager(entityObj, writer) {	
				@Override
				public void renderBody(ShrgFileWriter writer, String memberName, String targetEntity, String fkField) {
					write(writer, "");
					write(writer, "@ManyToOne");
					write(writer, "@JoinColumn(name = \"" + fkField + "\")");
					write(writer, "private " + toClassName(targetEntity) + " " + memberName);
					writeGetterSetter(writer, memberName, toClassName(targetEntity));
					storeMessage("attr." + memberName + "=" + memberName);
				}
			};
			
			JSONArray verboseFields = (JSONArray) entityObj.get("verboseFields");
			write(writer, "");
			if (! isVerboseLiteral) {
				write(writer, "@Override");
				write(writer, "public Vector<String> render() {");
				write(writer, "Vector<String> retVal = new Vector<String>()");
				StringBuilder builder = new StringBuilder();
				boolean isString;
				boolean first = true;
				if (verboseFields != null) {
					String verboseField = null;
					for (Object verboseFieldObj : verboseFields) {
						verboseField = ShrgJSONArray.getKey((JSONObject) verboseFieldObj);
						write(writer, "retVal.add(\"" + verboseField + "\")");
					}
				}
				write(writer, "return retVal");
				write(writer, "}");
			}
		}
		write(writer, "");
		write(writer, "}" );
	}

	private void storeMessage(String text) {
		if (! messagesSet.contains(text) && app.withLanguageFile()) {
			messagesSet.add(text);
			write(dictionary_en_US_writer, text);
		}
	}

	boolean isAttributeSet(JSONObject parentObj, String attrName) {
		boolean retVal = false;
		String value = getAttribute(parentObj, attrName);
		if (value != null)
			retVal = value.compareTo("true") == 0;
		return retVal;
	}

	String getAttribute(JSONObject parentObj, String attrName) {
		String retVal = null;
		JSONObject attrObj = (JSONObject) parentObj.get(attrName);
		if (attrObj != null)
			retVal = (String) attrObj.get("value");
		return retVal;
	}
	
	private void writeFieldMember(ShrgFileWriter writer, FieldDescriptor field, JSONObject fieldObj, boolean readOnly) {
		write(writer, "");
		if (isAttributeSet(fieldObj, "classSpecificDictio"))
			write(writer, "@ClassSpecificDictionary");	
		if (!readOnly) {
			if (field.javaType().compareTo("String") == 0) {
				int lowerBound = field.isNullable ? 0 : 1;
				write(writer, "@Size(min = " + String.valueOf(lowerBound) + ", max = " + String.valueOf(field.precision)
						+ ")");
			} else if (!field.isNullable)
				write(writer, "@NotNull");
		}
		if (isAttributeSet(fieldObj, "formFieldPercent"))
			write(writer, "@NumberFormat(style = Style.PERCENT)");	
		if (field.javaType().compareTo("byte[]") == 0 && getAttribute(fieldObj, "previewPath") != null)
			write(writer, "@Basic(fetch=FetchType.LAZY)");
		write(writer, "private " + getType(fieldObj, field) + " " + field.name);	
	}
	
	private String getType(JSONObject fieldObj, FieldDescriptor field) {
		if (fieldObj == null)
			return field.javaType();
		else
			return isAttributeSet(fieldObj, "formFieldCurrency") ? "BigDecimal" : 
				isAttributeSet(fieldObj, "formFieldBoolean") ? "Boolean" : field.javaType();
	}
	
	private void writeGetterSetter(ShrgFileWriter writer, FieldDescriptor field) {
		writeGetterSetter(writer, field, false);
	}
	
	private void writeGetterSetter(ShrgFileWriter writer, FieldDescriptor field, boolean readOnly) {
		writeGetterSetter(writer, field.name, field.javaType(), readOnly);
	}
	
	private void writeGetterSetter(ShrgFileWriter writer, String name, String type) {
		writeGetterSetter(writer, name, type, false);
	}
	
	private String accessorName(String fieldName, String prefix) {
		return  prefix + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
	}
	
	private void writeGetterSetter(ShrgFileWriter writer, String name, String type, boolean readOnly) {
		write(writer, "");
		write(writer, "public " + type + " " + accessorName(name, "get") + "() {");
		write(writer, "    return " + name);
		write(writer, "}");
		if (! readOnly) {
			write(writer, "public void " + accessorName(name, "set") + "(" + type + " " +  name + ") {");
			write(writer, "    this." + name + " = " + name);
			write(writer, "}");
		}
	}

}
