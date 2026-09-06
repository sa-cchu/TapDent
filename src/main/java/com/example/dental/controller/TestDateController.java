package com.example.dental.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.dental.form.CalendarExceptionForm;

@RestController
public class TestDateController {
    @PostMapping("/test-date")
    public String testDate(@RequestBody CalendarExceptionForm form) {
        return "Received date: " + form.getTargetDate().toString();
    }
}
