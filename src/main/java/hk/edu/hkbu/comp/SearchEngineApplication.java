package hk.edu.hkbu.comp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import hk.edu.hkbu.comp.tables.KURL;
import hk.edu.hkbu.comp.tables.URL;
import hk.edu.hkbu.comp.tables.PURL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class SearchEngineApplication {

    public static void main(String[] args) {

        KURL kurls = new KURL();
        URL urls = new URL();
        PURL purls = new PURL();
        MyParserCallback m = new MyParserCallback();
        int U = 100;
        int V = 100;


        String firstUrl = "https://biol.hkbu.edu.hk/";
        urls.urls.add(firstUrl);

        while(100 > purls.purls.size()){

            if(!m.goodweb(urls.urls.get(0))){
                urls.urls.remove(0);
                continue;
            }
            if(urls.urls.size() > 0){
                String content = m.loadWebPage(urls.urls.get(0));
                //if (content.contains("<html")){
                    String pattern = "<title>([\\s\\S]*?)</title>";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m1 = r.matcher(content);
                    if(m1.find()){
                        String title = m1.group(1).replace("\n","");
                        title = title.replace("\t","");
                        title = title.replaceAll("\r","");
                        title = title.replaceAll("\\p{Punct}", "");

                        String[] words = title.split(" ");

                        for (String word : words) {
                            kurls.kurls.put(word, urls.urls.get(0));
                        }
                    }

                    String pattern2 = "<a[^>]*href=\\\"((http|www)[^\\\\\\\"]*)\\\"";
                    Pattern r2 = Pattern.compile(pattern2);
                    Matcher m2 = r2.matcher(content);

                    while (m2.find()){
                        if (!urls.urls.contains(m2.group(1)) && !purls.purls.contains(m2.group(1))){
                            if(urls.urls.size() < U){
                                    urls.urls.add(m2.group(1));
                            }
                        }
                    }

                if(!purls.purls.contains(urls.urls.get(0))){
                    purls.purls.add(urls.urls.get(0));
                }
                urls.urls.remove(0);
            }
            System.out.println("The number websites: "+urls.urls.size());
            System.out.println("The number of identified websites: "+purls.purls.size());
        }
        SpringApplication.run(SearchEngineApplication.class, args);
    }
}
