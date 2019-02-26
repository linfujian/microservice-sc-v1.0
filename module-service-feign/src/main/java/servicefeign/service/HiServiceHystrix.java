package servicefeign.service;

import org.springframework.stereotype.Component;

@Component
public class HiServiceHystrix implements HiSerivceInterface {

	@Override
	public String sayHi(String name) {
		return "sorry," + name + ", it has a error";
	}

}
