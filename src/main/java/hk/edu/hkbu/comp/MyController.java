package hk.edu.hkbu.comp;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

@Controller
public class MyController {

    @GetMapping({"**", "search"})
    @ResponseBody
    String load(HttpServletRequest request) {
        return String.format("You are browsing %s with %s!",
                request.getRequestURI(), request.getQueryString());
    }
}
