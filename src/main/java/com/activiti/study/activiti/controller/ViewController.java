package com.activiti.study.activiti.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ViewController {
    /**
     * 8.进入流程图页面
     */
    @ApiOperation(value = "进入流程图页面", notes = "进入流程图页面")
    @GetMapping(value = "/view")
    public String intoImg() {
        return "view";
    }
}
