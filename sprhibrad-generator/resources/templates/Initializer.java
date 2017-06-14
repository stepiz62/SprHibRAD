
import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.sprhibrad.framework.configuration.MenuConfig;
import com.sprhibrad.framework.configuration.ShrConfigurator;

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
        DOMConfigurator.configure(servletContext.getRealPath("") + File.separator + "WEB-INF/log4j.xml");
        Logger.getLogger(Initializer.class).info("log4j configured !");
 
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
