package hk.edu.hkbu.comp.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class indexController {

    @RequestMapping("/")
    public String index(){
        return "index";
    }
}
