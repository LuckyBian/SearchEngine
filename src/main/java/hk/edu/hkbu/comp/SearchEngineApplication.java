package hk.edu.hkbu.comp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import hk.edu.hkbu.comp.tables.URL;
import hk.edu.hkbu.comp.tables.PURL;
import hk.edu.hkbu.comp.tables.KURL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//这个是程序的主入口
// 网站对应链接---> Mycontroller

@SpringBootApplication
public class SearchEngineApplication {

    //建立static变量，存储要求的三个table

    // URL是每识别的
    // PURL是识别过的
    // kURL存储关键词和链接对应关系

    public static URL urls = new URL();
    public static PURL purls = new PURL();

    public static List<KURL> kurls = new ArrayList<KURL>();

    public static List<String> keywords = new ArrayList<String>();

    // 其他类获取三个变量

    public static PURL getPurls() {
        return purls;
    }

    public static URL getUrls() {
        return urls;
    }

    public static List<KURL> getKurls() {
        return kurls;
    }

    //程序开始
    public static void main(String[] args) throws IOException {


        // 创建m，使用其中的过滤方法
        MyParserCallback m = new MyParserCallback();

        // 按要求创建变量，控制收集到的链接
        int U = 100;
        int V = 100;

        // seed URL，从这里开始
        String firstUrl = "https://biol.hkbu.edu.hk/";

        // 添加进去
        urls.urls.add(firstUrl);

        // 开始循环，存储数据
        while(1 > purls.purls.size()){

            //判断当前链接是否符合要求，可以新添加，目前有中英文，长度
            if(!m.goodweb(urls.urls.get(0))){
                urls.urls.remove(0);
                continue;
            }

            // 如果符合，且有链接，开始提取
            if(urls.urls.size() > 0){
                //将所有内容包括标签提取出来
                String content = m.loadWebPage(urls.urls.get(0));
                String title = new String();

                //正则表达式提取标题
                String pattern = "<title>([\\s\\S]*?)</title>";
                Pattern r = Pattern.compile(pattern);
                Matcher m1 = r.matcher(content);
                if(m1.find()){
                    title = m1.group(1).replace("\n","");
                    title = title.replace("\t","");
                    title = title.replaceAll("\r","");
                    title = title.replaceAll("\\p{Punct}", "");
                    String[] words = title.split(" ");
                }

                //提取内容
                String text = m.loadPlainText(urls.urls.get(0));
                //提取keywords
                List cleantext = m.extraKey(text);

                for(int k = 0; k < cleantext.size(); k++){
                    String keyword = (String) cleantext.get(k);
                    String url = urls.urls.get(0);

                    if(!keywords.contains(keyword)){
                        keywords.add(keyword);
                        KURL l = new KURL(keyword,title,url);
                        kurls.add(l);
                    }
                    else{
                        int index = keywords.indexOf(keyword);
                        if(!kurls.get(index).urls.equals(url)){
                            kurls.get(index).urls.add(url);
                            kurls.get(index).title.add(title);
                        }
                    }
                }

                //正则表达式提取链接
                String pattern2 = "<a[^>]*href=\\\"((http|www)[^\\\\\\\"]*)\\\"";
                Pattern r2 = Pattern.compile(pattern2);
                Matcher m2 = r2.matcher(content);

                // 将新的链接进行存储
                while (m2.find()){
                    if (!urls.urls.contains(m2.group(1)) && !purls.purls.contains(m2.group(1))){
                        if(urls.urls.size() < U){
                            urls.urls.add(m2.group(1));
                        }
                    }
                }

                    //将目前链接导入PIRL
                if(!purls.purls.contains(urls.urls.get(0))){
                    purls.purls.add(urls.urls.get(0));
                }
                urls.urls.remove(0);
            }
            //输出链接数量
            System.out.println("The number websites: "+urls.urls.size());
            System.out.println("The number of identified websites: "+purls.purls.size());
        }
        SpringApplication.run(SearchEngineApplication.class, args);
    }
}
