package hk.edu.hkbu.comp;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import hk.edu.hkbu.comp.tables.URL;
import hk.edu.hkbu.comp.tables.PURL;
import hk.edu.hkbu.comp.tables.KURL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//这个是程序的主入口
// 网站对应链接---> Mycontroller

@SpringBootApplication
@Log4j2
public class SearchEngineApplication {

    //建立static变量，存储要求的三个table

    // URL是每识别的
    // PURL是识别过的
    // kURL存储关键词和链接对应关系

    public static URL urls = new URL();
    public static PURL purls = new PURL();

    public static List<KURL> kurls = Collections.synchronizedList(new ArrayList<>());

    public static List<String> keywords = new ArrayList<>();

    public static List<KURL> getKurls() {
        return kurls;
    }
    private static final Object lockObj = new Object();

    //程序开始
    public static void main(String[] args) throws IOException {

        SpringApplication.run(SearchEngineApplication.class, args);

        // 创建m，使用其中的过滤方法
        MyParserCallback m = new MyParserCallback();

        // 按要求创建变量，控制收集到的链接
        int U = 10;
        int V = 100;

        // seed URL，从这里开始
        String firstUrl = "https://biol.hkbu.edu.hk/";
        // 添加进去
        urls.add(firstUrl);

        // 开始循环，存储数据
        Runnable processWeb = () -> {

            while(true) {
                String currUrl = "";
                // Critical section 1 (Get a url for processing)
                synchronized (lockObj) {
                    if (urls.size() > 0) {
                        //将所有内容包括标签提取出来
                        currUrl = urls.remove(0);
                    } else {
                        try {
                            lockObj.wait(3000);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Critical section 1 end

                // Non-Critical section (Network IO + Parsing)
                String content = m.loadWebPage(currUrl);
                //判断内容是否符合要求，可以新添加，目前有中英文，长度
                if (!m.goodweb(content)) {
                    continue;
                }

                // 如果符合，且有链接，开始提取
                String title = "";

                //正则表达式提取标题
                String pattern = "<title>([\\s\\S]*?)</title>";
                Pattern r = Pattern.compile(pattern);
                Matcher m1 = r.matcher(content);
                if(m1.find()){
                    title = m1.group(1).replace("\n","");
                    title = title.replace("\t","");
                    title = title.replaceAll("\r","");
                    title = title.replaceAll("\\p{Punct}", "");
                    //String[] words = title.split(" ");
                }

                //提取内容
                String text = "";
                try {
                    text = m.loadPlainText(content);
                } catch (IOException e) {
                    log.error("Failed to parse page");
                    e.printStackTrace();
                }
                //提取keywords
                List<String> cleantext = m.extraKey(text);
                // Non-Critical section end

                // Critical section 2
                synchronized (lockObj) {
                    if (purls.size() >= V) {
                        break;
                    }
                    for (String keyword : cleantext) {

                        if (!keywords.contains(keyword)) {
                            keywords.add(keyword);
                            KURL l = new KURL(keyword, title, currUrl);

                            kurls.add(l);
                        } else {
                            int index = keywords.indexOf(keyword);
                            if (!kurls.get(index).urls.contains(currUrl)) {
                                kurls.get(index).urls.add(currUrl);
                                kurls.get(index).title.add(title);
                            }
                        }
                    }

                    //正则表达式提取链接
                    String pattern2 = "<a[^>]*href=\"((http|www)[^\\\\\"]*)\"";
                    Pattern r2 = Pattern.compile(pattern2);
                    Matcher m2 = r2.matcher(content);

                    // 将新的链接进行存储
                    while (m2.find()) {
                        String newUrl = m2.group(1);
                        if (!currUrl.equals(newUrl) && !urls.contains(newUrl) && !purls.contains(newUrl)) {
                            if (urls.size() < U) {
                                urls.add(newUrl);
                            }
                        }
                    }

                    //将目前链接导入PIRL
                    if (!purls.contains(currUrl)) {
                        purls.add(currUrl);
                    }

                    //输出链接数量
                    log.info("The number websites: {}", urls.size());
                    log.info("The number of identified websites: {}", purls.size());
                }
                // Critical section 2 end
            }
        };

        final int N_THREAD = 10;
        ExecutorService es = Executors.newFixedThreadPool(N_THREAD);
        for (int i = 0; i < N_THREAD; i++) {
            es.execute(processWeb);
        }
    }
}
