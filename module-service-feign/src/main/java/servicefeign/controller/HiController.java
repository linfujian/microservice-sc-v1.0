package servicefeign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import servicefeign.service.HiSerivceInterface;

/**
 * 
 * @author fujian
 *
 */
@RestController
public class HiController {

	@Autowired
	HiSerivceInterface service;
	
	@GetMapping("/hi")
	public String sayHi(@RequestParam(value="name", defaultValue="fujian") String name) {
		return service.sayHi(name);
	}
}
