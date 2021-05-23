package ch.uzh.ifi.hase.soprafs21.service;

import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MailService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final JavaMailSender javaMailSender;

    public MailService(UserRepository userRepository, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.javaMailSender = javaMailSender;
    }

    public void sendEMail(String to, String body, String subject){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setFrom("soprafs21g21@gmail.com");
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);

        javaMailSender.send(simpleMailMessage);
    }

    public void forgotPassword(String username){
        User forgottenUser = userRepository.findByUsername(username);

        // checks if User exists
        if (forgottenUser == null){
            String errorMessage = "User with username %s does not exist!";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(errorMessage, username));
        }

        String passwordTxt = """
                Hello %s\s
                \s
                Your password is %s\s
                Make sure to change it in your profile tab when You log back in!
                If you did not request this change, please ignore this mail.""";
        passwordTxt = String.format(passwordTxt, forgottenUser.getUsername(), forgottenUser.getPassword());

        sendEMail(forgottenUser.getMail(), passwordTxt, "Forgot Password C.R.E.A.M.");
    }
}
