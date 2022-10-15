package hk.edu.hkbu.comp;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hk.edu.hkbu.comp.tables.KURL;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
    public String load(HttpServletRequest request, String query, String scope, HttpServletResponse response) throws IOException {
        //返回搜索结果，需要额外方法进行过滤
        if (query.equals(" ")) {
            response.sendRedirect("index.html");
        }

        if (scope != null && scope.equals("URL")) {

            if(query.matches("^[a-z0-9]+://.+")){
                response.sendRedirect(query);
            }
            else{
                return "This is not a URL, Please reload the page and search again!";
            }
        }
        else {
            String[] words = query.split(" ");
            List<KURL> table3 = SearchEngineApplication.getKurls();
            List keywordList = SearchEngineApplication.keywords;
            int wordNumber = words.length;

            if(wordNumber == 1){
                if(keywordList.contains(query)){
                    int webIndex = keywordList.indexOf(query);
                    KURL kurls = table3.get(webIndex);
                    request.setAttribute("titles",kurls.getTitle());
                    request.setAttribute("urls",kurls.getUrls());
                    request.setAttribute("test","test");
                    response.sendRedirect("index.html");
                }
            }

            else if(wordNumber == 2){
                response.sendRedirect("index.html");
            }
            else{
                return "Please input 1 or 2 words";
            }


            return "redirect:/index.html";
        }
        return "error";
    }
}
