package com.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController extends GenericController {
	
	@GetMapping("/login")
	public String login() {
		logger.debug("login page.");
		return "login";
	}
}
