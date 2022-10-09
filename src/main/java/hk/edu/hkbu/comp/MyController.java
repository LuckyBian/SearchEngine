package hk.edu.hkbu.comp;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import hk.edu.hkbu.comp.MyParserCallback;
import hk.edu.hkbu.comp.SearchEngineApplication;
@Controller
public class MyController {
    //如果没有额外mapping，跳转index.html

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
        //返回搜索结果，需要额外方法进行过滤
        String a = SearchEngineApplication.getUrls().urls.get(0);

        return "<h1>" + a + "!</h1>";
    }
}
