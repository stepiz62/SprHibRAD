
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import javax.annotation.Resource;
import javax.sql.DataSource;

§
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.PathMatcher;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.CompositeUriComponentsContributor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.util.UrlPathHelper;

import com.sprhibrad.framework.configuration.ShrCurrencyFormatter;
import com.sprhibrad.framework.configuration.ShrDateFormatter;
import com.sprhibrad.framework.configuration.ShrRequestMappingHandlerAdapter;
import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;
import com.sprhibrad.framework.converter.LangConverter;

§

import com.sprhibrad.framework.configuration.UserManager;
import com.sprhibrad.framework.controller.BirtViewFactory;
import com.sprhibrad.framework.controller.ShrBirtView;



@Configuration
@EnableWebMvc
§
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class WebConf extends WebMvcConfigurationSupport {

    @Resource
    private Environment env;

§
    
	@Autowired
	LangConverter langConverter;
	
	@Autowired
	ShrCurrencyFormatter currencyFormatter;
	
	@Autowired
	ShrDateFormatter dateFormatter;

	@Bean
    public UrlBasedViewResolver setupViewResolver() {
        UrlBasedViewResolver resolver = new UrlBasedViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate retVal = new JdbcTemplate();
		retVal.setDataSource(dataSource());
		return retVal;
	}

	@Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
         
        dataSource.setDriverClassName(env.getRequiredProperty("db.driver"));
        dataSource.setUrl(env.getRequiredProperty("db.url"));
        dataSource.setUsername(env.getRequiredProperty("db.username"));
        dataSource.setPassword(env.getRequiredProperty("db.password"));
         
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setPackagesToScan(new String[] {env.getRequiredProperty("entitymanager.shr_packages.to.scan"), 
        													env.getRequiredProperty("entitymanager.packages.to.scan")});
        sessionFactoryBean.setHibernateProperties(hibProperties());
        return sessionFactoryBean;
    }
    
    private Properties hibProperties() {
        Properties properties = new Properties();
        setHibProperty(properties, "hibernate.dialect");
        setHibProperty(properties, "hibernate.show_sql");
        setHibProperty(properties, "hibernate.format_sql");
        setHibProperty(properties, "hibernate.id.new_generator_mappings");
        return properties;  
    }
    
    private void setHibProperty(Properties properties, String propName) {
    	properties.put(propName, env.getRequiredProperty(propName));
    } 
    
    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
    
    @Bean
    public MessageSource messageSource() {
    	return new ShrResourceBundleMessageSource();
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.getDefault());
        return sessionLocaleResolver;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
     return new BCryptPasswordEncoder();
    }
        
    @Bean
    public CommonsMultipartResolver filterMultipartResolver() {
        return new CommonsMultipartResolver();
    }

	@Override
	protected void addFormatters(FormatterRegistry registry) {
		addGeneratedConverters(registry);
     	registry.addConverter(langConverter);
    	registry.addFormatterForFieldType(BigDecimal.class, currencyFormatter);
    	registry.addFormatterForFieldType(Date.class, dateFormatter);
    }
 
 	@Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
 //       /** B I R T */
		registry.addViewController("/reports").setViewName("birtView");
   }

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("*.css").addResourceLocations("/WEB-INF/resources/css/");
		registry.addResourceHandler("*.js").addResourceLocations("/WEB-INF/resources/js/");
		registry.addResourceHandler("*.jpg").addResourceLocations("/WEB-INF/resources/images/");
		super.addResourceHandlers(registry);
	}

	@Override
	protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
		return new ShrRequestMappingHandlerAdapter();
	}

	// the methods above are to make effectively the class annotation-inherited --- DON'T REMOVE

	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		return super.requestMappingHandlerMapping();
	}

	@Bean
	public ContentNegotiationManager mvcContentNegotiationManager() {
		return super.mvcContentNegotiationManager();
	}

	@Bean
	public HandlerMapping viewControllerHandlerMapping() {
		return super.viewControllerHandlerMapping();
	}

	@Bean
	public BeanNameUrlHandlerMapping beanNameHandlerMapping() {
		return super.beanNameHandlerMapping();
	}

	@Bean
	public HandlerMapping resourceHandlerMapping() {
		return super.resourceHandlerMapping();
	}

	@Bean
	public ResourceUrlProvider mvcResourceUrlProvider() {
		return super.mvcResourceUrlProvider();
	}

	@Bean
	public HandlerMapping defaultServletHandlerMapping() {
		return super.defaultServletHandlerMapping();
	}

	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		return super.requestMappingHandlerAdapter();
	}

	@Bean
	public FormattingConversionService mvcConversionService() {
		return super.mvcConversionService();
	}

	@Bean
	public Validator mvcValidator() {
		return super.mvcValidator();
	}

	@Bean
	public PathMatcher mvcPathMatcher() {
		return super.mvcPathMatcher();
	}

	@Bean
	public UrlPathHelper mvcUrlPathHelper() {
		return super.mvcUrlPathHelper();
	}

	@Bean
	public CompositeUriComponentsContributor mvcUriComponentsContributor() {
		return super.mvcUriComponentsContributor();
	}

	@Bean
	public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
		return super.httpRequestHandlerAdapter();
	}

	@Bean
	public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
		return super.simpleControllerHandlerAdapter();
	}

	@Bean
	public HandlerExceptionResolver handlerExceptionResolver() {
		return super.handlerExceptionResolver();
	}

	@Bean
	public ViewResolver mvcViewResolver() {
		return super.mvcViewResolver();
	}

	@Bean public BeanNameViewResolver beanNameResolver(){ 
		BeanNameViewResolver br = new BeanNameViewResolver() ;
		return br; 
	} 

	// the methods above are to make the class effectively annotation-inherited --- DON'T REMOVE THEM
	
	@Bean
    public UserManager userManager() {
		return new UserManagerImpl();
	}

/****    B I R T   ****/

	@Bean 
	public BirtViewFactory birtViewFactory(){ 
		BirtViewFactory bvf = new BirtViewFactory() {
			@Override
			public ShrBirtView create() {
				return  birtView();
			}}; 
		return bvf; 
	}

	@Bean 
	@Lazy
	public BirtView birtView(){ 
		BirtView bv = new BirtView(); 
		bv.setBirtEngine( engine().getObject() );
		return bv; 
	}

	@Bean 
	@Lazy
	protected BirtEngineFactory engine(){ 
		BirtEngineFactory factory = new BirtEngineFactory() ;  
		factory.setLogLevel( Level.WARNING);
		String logPath = env.getRequiredProperty("birt.logDir");
		if (Files.notExists(Paths.get(logPath)))
			try {
				Files.createDirectories(Paths.get(logPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		factory.setLogDirectory(new FileSystemResource(logPath));
		return factory ; 
	}

§
	
	/*** Generation contribution ***/
	private void addGeneratedConverters(FormatterRegistry registry) {
§
	}

}
