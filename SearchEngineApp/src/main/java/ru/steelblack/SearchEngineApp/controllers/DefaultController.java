package ru.steelblack.SearchEngineApp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class DefaultController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

//    @RequestMapping(value = "/assets/fonts/Montserrat/Montserrat-Medium.woff2", method = RequestMethod.GET)
//    public String method(){
//        return "/assets/fonts/Montserrat/Montserrat-Medium.woff2";
//    }
}


