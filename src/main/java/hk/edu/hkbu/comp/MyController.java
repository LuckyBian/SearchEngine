package hk.edu.hkbu.comp;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hk.edu.hkbu.comp.tables.KURL;
import hk.edu.hkbu.comp.tables.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.IOException;
import java.util.*;

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


    //@ResponseBody
    @RequestMapping("/load")
    public String load(HttpServletRequest request,
                       String query,
                       String scope,
                       HttpServletResponse response,
                       @RequestParam(name = "title", required = false)
                               String title,
                       @RequestParam(name = "url", required = false)
                               String url,
                       Model model) throws IOException {
        //返回搜索结果，需要额外方法进行过滤
        if (query.equals("")) {
            return "redirect:/index.html";
        }

        if (scope != null && scope.equals("URL")) {
            if (query.matches("^[a-z0-9]+://.+")) {
                return "redirect:" + query;
            } else {
                return "redirect:/index.html";
            }
        } else {
            String[] words = query.split("[\\p{Punct}\\s+]");
            List<String> keywordList = SearchEngineApplication.keywords;
            int wordNumber = words.length;

            if (wordNumber == 1) {

                words[0] = MyParserCallback.stem(words[0]);
                words[0] = words[0].toLowerCase();

                Set<PageInfo> resultSet = MyParserCallback.onesearch(words[0]);
                request.setAttribute("resultSet", resultSet);
                return "index.html";
            } else if (wordNumber == 2) {
                words[0] = MyParserCallback.stem(words[0]);
                words[0] = words[0].toLowerCase();
                words[1] = MyParserCallback.stem(words[1]);
                words[1] = words[1].toLowerCase();

                if (query.matches("[A-z]+\\+[A-z]+")) {
                    Set<PageInfo> resultSet = MyParserCallback.andsearch(words[0], words[1]);
                    request.setAttribute("resultSet", resultSet);
                    return "index.html";
                } else if (query.matches("[A-z]+\\![A-z]+")) {
                    Set<PageInfo> resultSet = MyParserCallback.orsearch(words[0], words[1]);
                    request.setAttribute("resultSet", resultSet);
                    return "index.html";
                } else if (query.matches("[A-z]+-[A-z]+")) {
                    Set<PageInfo> resultSet = MyParserCallback.notsearch(words[0], words[1]);
                    request.setAttribute("resultSet", resultSet);
                    return "index.html";
                } else {
                    return "redirect:/index.html";
                }
            } else {
                return "redirect:/index.html";
            }
        }
    }
}
