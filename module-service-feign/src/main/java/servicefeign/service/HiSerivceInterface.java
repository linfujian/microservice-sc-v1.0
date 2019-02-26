package servicefeign.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value="service-hi", fallback=HiServiceHystrix.class)
public interface HiSerivceInterface {

	@GetMapping("/hi")
	public String sayHi(@RequestParam(value="name") String name);
}
