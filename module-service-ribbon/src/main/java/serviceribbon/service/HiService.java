package serviceribbon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * 
 * @author fujian
 *
 */
@Service
public class HiService {

	@Autowired
	RestTemplate restTemplate;
	
	@HystrixCommand(fallbackMethod="hiError")
	public String sayhi(String name) {
		return restTemplate.getForObject("http://SERVICE-HI/hi?name="+name, String.class);
	}
	
	public String hiError(String name) {
		return "hi," + name + ",sorry,error!";
	}
}
