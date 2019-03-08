package servicehi.propertyutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.Cleanup;
import lombok.SneakyThrows;

@Component
public class ReadProperyFromDB implements BeanFactoryPostProcessor {

	@Override
	@SneakyThrows
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		Environment env = beanFactory.getBean(Environment.class);
		String url = env.getProperty("spring.datasource.url");
		String username = env.getProperty("spring.datasource.data-username");
		String password = env.getProperty("spring.datasource.data-password");
		
		Class.forName("com.mysql.jdbc.Driver");
		@Cleanup
		Connection con = DriverManager.getConnection(url, username, password);
		
		@Cleanup
		Statement sta = con.createStatement();
		
		@Cleanup
		ResultSet rs = sta.executeQuery("select pro_key, pro_val from demo where 1=1");
		
		while(rs.next()) {
			System.setProperty(rs.getString(1), rs.getString(2));
		}
		
	}	
}
