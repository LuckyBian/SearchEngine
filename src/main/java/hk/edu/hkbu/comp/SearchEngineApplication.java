package hk.edu.hkbu.comp;

import hk.edu.hkbu.comp.tables.*;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//这个是程序的主入口
// 网站对应链接---> Mycontroller

@SpringBootApplication
public class SearchEngineApplication {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SearchEngineApplication.class);

    //建立static变量，存储要求的三个table

    // URL是每识别的
    // PURL是识别过的
    // kURL存储关键词和链接对应关系

    public static URL urls = new URL();
    public static PURL purls = new PURL();

    public static List<KURL> kurls = Collections.synchronizedList(new ArrayList<>());

    public static DataTable dataTable = new DataTable();

    public static List<String> keywords = new ArrayList<>();

    public static List<KURL> getKurls() {
        return kurls;
    }
    private static final Object lockObj = new Object();

    //程序开始
    public static void main(String[] args) throws IOException, InterruptedException {

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
        Function<CountDownLatch, Runnable> processWeb = (CountDownLatch latch) -> {
            return () -> {

                while (true) {
                    String currUrl = "";
                    // Critical section 1 (Get a url for processing)
                    synchronized (lockObj) {
                        log.info("[CS1] Thread {} ENTERED", Thread.currentThread().getName());
                        if (urls.size() > 0) {
                            //将所有内容包括标签提取出来
                            currUrl = urls.remove(0);
                        } else {
                            try {
                                for (int i = 0; i < 10; i++) {
                                    log.warn("Thread {} is waiting for new URL ({}/{})",
                                            Thread.currentThread().getName(), i + 1, 10);
                                    lockObj.wait(1000);
                                    if (urls.size() != 0) {
                                        break;
                                    }
                                }
                                if (urls.size() == 0) {
                                    log.warn("Thread {} breaks", Thread.currentThread().getName());
                                    break;
                                } else {
                                    log.warn("Thread {} continues", Thread.currentThread().getName());
                                    continue;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    log.info("[CS1] Thread {} LEFT", Thread.currentThread().getName());
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
                    if (m1.find()) {
                        title = m1.group(1).replace("\n", "");
                        title = title.replace("\t", "");
                        title = title.replaceAll("\r", "");
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
                    // Build PageInfo
                    PageInfo pageInfo = new PageInfo(currUrl, title);
                    // Non-Critical section end

                    // Critical section 2
                    synchronized (lockObj) {
                        if (purls.size() >= V) {
                            break;
                        }
                        for (String keyword : cleantext) {

//                        if (!keywords.contains(keyword)) {
//                            keywords.add(keyword);
//                            KURL l = new KURL(keyword, title, currUrl);
//
//                            kurls.add(l);
//                        } else {
//                            int index = keywords.indexOf(keyword);
//                            if (!kurls.get(index).urls.contains(currUrl)) {
//                                kurls.get(index).urls.add(currUrl);
//                                kurls.get(index).title.add(title);
//                            }
//                        }
                            dataTable.add(keyword, pageInfo);
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
                        lockObj.notifyAll();
                    }
                    log.info("[CS2] Thread {} LEFT", Thread.currentThread().getName());
                    // Critical section 2 end
                }
                latch.countDown();
                log.info("Job finished. Jobs left {}", latch.getCount());
            };
        };

        final int N_THREAD = 10;
        ExecutorService es = Executors.newFixedThreadPool(N_THREAD);
        CountDownLatch latch = new CountDownLatch(N_THREAD);
        for (int i = 0; i < N_THREAD; i++) {
            es.execute(processWeb.apply(latch));
        }
        latch.await();

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("index.obj"));
        oos.writeObject(dataTable);
        oos.flush();
        oos.close();
        log.info("Data saved");

        // Start serving web
        SpringApplication.run(SearchEngineApplication.class, args);
    }
}
