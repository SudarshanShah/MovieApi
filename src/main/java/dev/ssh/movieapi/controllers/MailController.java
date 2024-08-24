package dev.ssh.movieapi.controllers;

import dev.ssh.movieapi.services.EmailService;
import dev.ssh.movieapi.dtos.MailBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
@CrossOrigin(origins = "*")
public class MailController {

    private final EmailService emailService;

    public MailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/sendmail")
    public ResponseEntity<String> sendMail(@RequestBody MailBody mailBody) {
        emailService.sendSimpleMessage(mailBody);
        return ResponseEntity.ok("Email is sent!");
    }

}
