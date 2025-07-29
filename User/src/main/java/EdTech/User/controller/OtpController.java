package EdTech.User.controller;

import EdTech.User.dto.LoginResponse;
import EdTech.User.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/generate")
    public ResponseEntity<String> generate(@RequestParam String email) {
        otpService.generateAndSendOtp(email);
        return ResponseEntity.ok("OTP sent to email!");
    }

    @PostMapping("/verify")
    public ResponseEntity<LoginResponse> verifyOtp(@RequestParam String email,
                                                   @RequestParam String otp) {
        LoginResponse response = otpService.validateOtpAndGenerateToken(email, otp);
        return ResponseEntity.ok(response);
    }

}
