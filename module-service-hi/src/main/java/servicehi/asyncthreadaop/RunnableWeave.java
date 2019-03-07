package servicehi.asyncthreadaop;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class RunnableWeave  implements Runnable {

	private final Runnable r;
	private final Map<String, String> MDCContextMap;
	private final SecurityContext securityContext;
	
	public RunnableWeave(Runnable r) {
		this.r = r;
		this.MDCContextMap = MDC.getCopyOfContextMap();
		securityContext = SecurityContextHolder.getContext();
	}
	
	@Override
	public void run() {
		MDC.setContextMap(MDCContextMap);
		SecurityContextHolder.setContext(securityContext);
		r.run();
		SecurityContextHolder.clearContext();
		MDC.setContextMap(new HashMap<>());
	}

}
