package com.example.softwaremetric.controller;

import com.example.softwaremetric.model.UseCaseModelInput;
import com.example.softwaremetric.service.UseCaseModelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UseCaseModelController {

    private final UseCaseModelService useCaseModelService;

    public UseCaseModelController(UseCaseModelService useCaseModelService) {
        this.useCaseModelService = useCaseModelService;
    }

    @GetMapping("/use-case-model")
    public String useCaseModel(Model model) {
        model.addAttribute("input", UseCaseModelInput.defaults());
        return "use-case-model";
    }

    @PostMapping("/use-case-model")
    public String calculateUseCaseModel(@ModelAttribute UseCaseModelInput input, Model model) {
        model.addAttribute("input", input);
        try {
            model.addAttribute("result", useCaseModelService.calculate(input));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "use-case-model";
    }
}
