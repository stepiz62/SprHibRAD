
§
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.sprhibrad.framework.configuration.UserManager;
import com.sprhibrad.framework.service.IShrService;

public class UserManagerImpl implements UserManager{
	
	@Autowired
§

	@Override
	public String getUserEntityName() {
§
	}

	@Override
	public void createUserDDL(JdbcTemplate jdbcTemplate, String database, String userName, String password) {
		jdbcTemplate.execute(String.format("Create USER %s IDENTIFIED BY '%s'", userName, password));
		jdbcTemplate.execute(String.format("GRANT SELECT, INSERT, UPDATE, DELETE  ON %s.* TO '%s'@'%%'", database, userName));
	}

	@Override
	public String dropUserDDL() {
		return "DROP USER %s";
	}

	@Override
	public String changePasswordDDL() {
		return "SET PASSWORD FOR '%s'@'%' = PASSWORD('%s')";
	}

	@Override
	public String getPassword(Object entity) {
§
	}

	@Override
	public void setPassword(Object entity, String clearPwd) {
§
	}

	@Override
	public String getUsername(Object entity) {
§
	}

	@Override
	public IShrService getUserService() {
§
	}

	@Override
	public String getUserNameField() {
§
	}
	
	
}
