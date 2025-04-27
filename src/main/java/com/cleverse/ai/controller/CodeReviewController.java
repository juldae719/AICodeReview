package com.cleverse.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping(value={"/api/codereview"})
public class CodeReviewController {

	@RequestMapping("/")
	public String portal() {
		return "index";
	}
}
