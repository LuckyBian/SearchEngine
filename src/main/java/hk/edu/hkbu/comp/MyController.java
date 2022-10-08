package hk.edu.hkbu.comp;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

@Controller
public class MyController {

    @GetMapping("greeting")
    @ResponseBody
    String sayHello(
            @RequestParam(name = "name", required = false, defaultValue = "there")
            String name) {

        return "<h1>Hello " + name + "!</h1>";
    }

    @GetMapping("load")
    @ResponseBody
    String load(HttpServletRequest request) {
        return "<h1>" + request + "!</h1>";
    }

}
