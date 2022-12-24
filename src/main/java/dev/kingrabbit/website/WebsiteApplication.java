package dev.kingrabbit.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class WebsiteApplication implements ErrorController {

    public static void main(String[] args) {
        new WebSocketManager();
        SpringApplication.run(WebsiteApplication.class, args);
    }

    @RequestMapping("/error")
    @ResponseBody
    public String error() {
        return "<script>window.location.replace(\"/\");</script>";
    }

}
