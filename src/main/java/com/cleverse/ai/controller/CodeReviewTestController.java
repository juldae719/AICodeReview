package com.cleverse.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping(value={"/api/codereview/test"})
public class CodeReviewTestController {

	@RequestMapping("/")
	public String portal() {
		int a1 = 19;
		double a2 = a1 + 10.5;
		System.out.println(a2);
		return "index";
	}
}
