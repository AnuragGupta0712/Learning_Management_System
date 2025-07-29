package EdTech.User.service;

import EdTech.User.dto.LoginResponse;
import EdTech.User.model.OtpStorage;
import EdTech.User.repository.OtpStorageRepository;
import EdTech.User.repository.UserRepository;
import EdTech.User.security.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private final Random random = new Random();

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OtpStorageRepository otpRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }

    public ResponseEntity<String> generateAndSendOtp(String email) {
        Optional<OtpStorage> existingOtp = otpRepo.findByEmail(email);

        if(userRepo.findByEmail(email).isPresent()) {
            String otp = generateOtp();
            sendOtpEmail(email, otp);

            OtpStorage otpEntity;
            if (existingOtp.isPresent()) {
                // ✅ Update existing OTP
                otpEntity = existingOtp.get();
                otpEntity.setOtp(otp);
                otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            } else {
                // ✅ Create new entry
                otpEntity = new OtpStorage();
                otpEntity.setEmail(email);
                otpEntity.setOtp(otp);
                otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            }

            otpRepo.save(otpEntity); // Save updated or new
            return ResponseEntity.ok("OTP sent successfully!");
        } else {
            return new ResponseEntity<>("Not registered", HttpStatus.NOT_FOUND);
        }
    }


    public LoginResponse validateOtpAndGenerateToken(String email, String inputOtp) {
        Optional<OtpStorage> record = otpRepo.findByEmail(email);

        if (record.isPresent()) {
            OtpStorage otpStored = record.get();
            boolean isValid = otpStored.getOtp().equals(inputOtp) &&
                    otpStored.getExpiryTime().isAfter(LocalDateTime.now());

            if (isValid) {
                // Load user details to generate JWT
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                String token = jwtHelper.generateToken(userDetails);

                // Optional: delete OTP after successful validation
                otpRepo.delete(otpStored);

                return LoginResponse.builder()
                        .token(token)
                        .username(userDetails.getUsername())
                        .build();
            }
        }

        // You can also throw custom exception here instead
        throw new RuntimeException("Invalid or expired OTP");
    }
}
