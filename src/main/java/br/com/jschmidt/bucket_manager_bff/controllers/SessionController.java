package br.com.jschmidt.bucket_manager_bff.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SessionController {

    @GetMapping("/session")
    public String showSessionId(HttpSession session, Model model) {
        model.addAttribute("sessionId", session.getId());
        return "session";
    }
}
