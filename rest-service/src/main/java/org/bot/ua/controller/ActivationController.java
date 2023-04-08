package org.bot.ua.controller;

import org.bot.ua.service.UserActivationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/user")
@RestController
public class ActivationController {
    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    /*@RequestMapping(method = RequestMethod.GET, value = "/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String id) {
        var res = userActivationService.activation(id);
        if (res) {
            var message = "Thank you for registering with FileFusionBot! Your account has been activated and you can now start using the bot.";
            return ResponseEntity.ok().body(message);
        }
        return ResponseEntity.internalServerError().build();
    }*/

    /*@GetMapping("/activation")
    public ModelAndView activateUser(@RequestParam("id") String activationId) {
        boolean isActivated = userActivationService.activation(activationId);

        ModelAndView modelAndView = new ModelAndView();

        if (isActivated) {
            modelAndView.setViewName("activation-success"); // the name of the view file for success page
        } else {
            modelAndView.setViewName("activation-failure"); // the name of the view file for failure page
        }

        return modelAndView;
    }*/

    @RequestMapping(method = RequestMethod.GET, value = "/activation")
    public ModelAndView activation(@RequestParam("id") String id) {
        var res = userActivationService.activation(id);
        if (res) {
            return new ModelAndView("activation");
        }
        return new ModelAndView("error");
    }


}
