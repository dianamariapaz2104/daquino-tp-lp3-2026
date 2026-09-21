package py.edu.uc.lp3.da.cs2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

	@GetMapping("/")
	public String holaMundo() {
		return "Hola mundo";
	}
}