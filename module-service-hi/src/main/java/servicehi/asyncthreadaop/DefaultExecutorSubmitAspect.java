package servicehi.asyncthreadaop;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import lombok.SneakyThrows;

@Aspect
@Component
public class DefaultExecutorSubmitAspect {

	@Around("execution(* java.util.concurrent.Executor.*(..))")
	@SneakyThrows
	public Object intercept(ProceedingJoinPoint joinPoint) {
		
		Object[] args = joinPoint.getArgs();
		for(int i =0; i< args.length; i++) {
			args[i] = processArgs(args[i]);
		}
		
		return joinPoint.proceed();
	}
	
	Object processArgs(Object arg) {
		if(arg instanceof Runnable) {
			Runnable r = (Runnable)arg;
			return new RunnableWeave(r);
		}
		if(arg instanceof Callable<?>) {
			Callable<?> c = (Callable<?>)arg;
			return new CallableWeave<>(c);
		}
		if(arg instanceof Collection<?>) {
			Collection<?> cs = (Collection<?>)arg;
			List<Object> collect = cs.stream().map(this::processArgs).collect(Collectors.toList());
			return collect;
		}
		return arg;
	}
}
