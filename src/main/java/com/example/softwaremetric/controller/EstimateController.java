package com.example.softwaremetric.controller;

import com.example.softwaremetric.model.EstimationInput;
import com.example.softwaremetric.service.EstimationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EstimateController {

    private final EstimationService estimationService;

    public EstimateController(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @GetMapping("/estimate")
    public String estimate(Model model) {
        model.addAttribute("input", EstimationInput.defaults());
        return "estimate";
    }

    @PostMapping("/estimate")
    public String estimateProject(@ModelAttribute EstimationInput input, Model model) {
        model.addAttribute("input", input);
        try {
            model.addAttribute("result", estimationService.estimate(input));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "estimate";
    }
}
