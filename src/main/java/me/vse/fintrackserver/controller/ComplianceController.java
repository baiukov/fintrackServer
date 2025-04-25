package me.vse.fintrackserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComplianceController {

    @GetMapping("/support")
    public String support(Model model) {
        return "html/supportService";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        return "html/privacyPolicy";
    }
}
