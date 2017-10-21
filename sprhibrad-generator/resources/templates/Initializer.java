
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.sprhibrad.framework.configuration.MenuConfig;
import com.sprhibrad.framework.configuration.ShrConfigurator;

§

public class Initializer  extends AbstractAnnotationConfigDispatcherServletInitializer  {

    @Override
	public void onStartup(ServletContext servletContext) throws ServletException {
   		super.onStartup(servletContext);
        final ShrConfigurator shrConfigurator = new ShrConfigurator();

   		servletContext.addListener(new HttpSessionListener() {
			@Override
			public void sessionCreated(HttpSessionEvent event) {
				event.getSession().setMaxInactiveInterval(60 * Integer.valueOf(shrConfigurator.getProperty("session.maxInactiveMinutes"))); 				
			}
			@Override
			public void sessionDestroyed(HttpSessionEvent arg0) {
			}
			});
   		ConfigurationSource source = null;
		try {
			source = new ConfigurationSource(new FileInputStream(servletContext.getRealPath("") + File.separator + "WEB-INF/log4j2.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
   		Configurator.initialize(null, source);        
        LogManager.getLogger(Initializer.class).info("log4j configured !");
 
        MenuConfig menuConfig = new MenuConfig();
        buildMenu(menuConfig);
        shrConfigurator.loadSHRparamsIntoContext(servletContext, menuConfig);        
        
	}


	@Override
	protected Class<?>[] getRootConfigClasses() {
		 return new Class[] { WebSecurityConfig.class, WebConf.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		 return null;
	}

	@Override
	protected String[] getServletMappings() {
		 return new String[] { "/" };

	}

	private void buildMenu(MenuConfig menuConfig) {
§
 	}

}
