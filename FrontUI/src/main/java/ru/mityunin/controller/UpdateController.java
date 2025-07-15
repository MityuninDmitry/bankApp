package ru.mityunin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mityunin.dto.AuthRequest;
import ru.mityunin.dto.UserDto;
import ru.mityunin.service.AccountsService;

@Controller
@RequestMapping("/update")
public class UpdateController {
    private final AccountsService accountsService;

    public UpdateController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping("/password")
    public String updatePassword(AuthRequest authRequest, Model model) {
        boolean success = accountsService.updatePassword(authRequest);
        if (success) {
            model.addAttribute("passwordUpdated", true);
        } else {
            model.addAttribute("passwordError", "Failed to update password");
        }
        return "redirect:/home";
    }

    @PostMapping("/userInfo")
    public String updateUserInfo(UserDto userDto, Model model) {
        boolean success = accountsService.updateUserInfo(userDto);
        if (success) {
            model.addAttribute("userInfoUpdated", true);
        } else {
            model.addAttribute("userInfoUpdatedError", "Failed to update user info");
        }
        return "redirect:/home";
    }
}