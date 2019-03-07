package servicehi.asyncthreadaop;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class AsynExecutor {
	
	@Bean
	@Lazy
	public ExecutorService defaultExecutor() {
		return Executors.newCachedThreadPool();
	}

}
