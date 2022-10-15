package hk.edu.hkbu.comp;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hk.edu.hkbu.comp.tables.KURL;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    //@ResponseBody
    @RequestMapping("/load")
    public String load(HttpServletRequest request,
                       String query,
                       String scope,
                       HttpServletResponse response,
                       @RequestParam(name = "title",required = false)
                       String title,
                       @RequestParam(name = "url",required = false)
                       String url,
                       Model model) throws IOException {
        //返回搜索结果，需要额外方法进行过滤
        if (query.equals(" ")) {
            return "redirect:/index.html";
        }

        if (scope != null && scope.equals("URL")) {
            if(query.matches("^[a-z0-9]+://.+")){
                return "redirect:"+query;
            }
            else{
                return "redirect:/index.html";
            }
        }

        else {
            String[] words = query.split(" ");
            List<KURL> table3 = SearchEngineApplication.getKurls();
            List keywordList = SearchEngineApplication.keywords;
            int wordNumber = words.length;

            if(wordNumber == 1){
                englishStemmer stemmer = new englishStemmer();
                stemmer.setCurrent(words[0]);
                words[0] = stemmer.getCurrent();
                if(keywordList.contains(words[0])){
                    int webIndex = keywordList.indexOf(words[0]);
                    KURL kurls = new KURL();
                    kurls = table3.get(webIndex);

                    Map<String,String> map = new HashMap<>();
                    for(int j = 0 ; j < kurls.getTitle().size();j++){
                        map.put(kurls.getTitle().get(j),kurls.getUrls().get(j));
                    }
                    request.setAttribute("map",map);

                    return "index.html";
                }
                else{
                    return "redirect:/index.html";
                }
            }

            else if(wordNumber == 2){
                response.sendRedirect("index.html");
            }
            else{
                return "Please input 1 or 2 words";
            }
        }
        return "index";
    }
}
