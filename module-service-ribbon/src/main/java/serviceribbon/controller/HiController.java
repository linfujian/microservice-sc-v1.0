package serviceribbon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serviceribbon.service.HiService;

/**
 * 
 * @author fujian
 *
 */
@RestController
public class HiController {

	@Autowired
	HiService serivce;
	
	@RequestMapping("/hi")
	public String hi(@RequestParam String name) {
		return serivce.sayhi(name);
	}
}
