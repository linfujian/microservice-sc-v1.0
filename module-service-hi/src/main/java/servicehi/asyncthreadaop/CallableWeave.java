package servicehi.asyncthreadaop;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class CallableWeave<V> implements Callable<V> {

	private final Callable<V> c;
	private final Map<String, String> MDCContextMap;
	private final SecurityContext securityContext;
	
	public CallableWeave(Callable<V> c) {
		this.c = c;
		this.MDCContextMap = MDC.getCopyOfContextMap();
		this.securityContext = SecurityContextHolder.getContext();
	}
	@Override
	public V call() throws Exception {
		MDC.setContextMap(MDCContextMap);
		SecurityContextHolder.setContext(securityContext);
		V v = c.call();
		MDC.setContextMap(new HashMap<>());
		SecurityContextHolder.clearContext();
		return v;
	}

}
