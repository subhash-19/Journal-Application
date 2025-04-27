package net.engineeringdigest.journalapplication.controller;

import lombok.extern.slf4j.Slf4j;
import net.engineeringdigest.journalapplication.dto.UserDTO;
import net.engineeringdigest.journalapplication.entity.User;
import net.engineeringdigest.journalapplication.service.UserDetailsServiceImpl;
import net.engineeringdigest.journalapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Slf4j
//@Tag(name = "Public APIs")
public class PublicController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserService userService;

    @Autowired
    public PublicController(AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

//    @Autowired
//    private JwtUtil jwtUtil;

    @GetMapping("/health-check")
    public String healthCheck() {
        log.info("Health is ok !");
        return "Ok";
    }

    @PostMapping("/signup")
    public void signup(@RequestBody UserDTO user) {
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setUserName(user.getUserName());
        newUser.setPassword(user.getPassword());
        newUser.setSentimentAnalysis(user.isSentimentAnalysis());
        userService.saveNewUser(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
//            String jwt = jwtUtil.generateToken(userDetails.getUsername());
//            return new ResponseEntity<>(jwt, HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            log.error("Exception occurred while createAuthenticationToken ", e);
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.BAD_REQUEST);
        }
    }
}
