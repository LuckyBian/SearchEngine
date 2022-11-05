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

    //程序开始
    public static void main(String[] args) throws IOException, InterruptedException {
        // Start serving web
        SpringApplication.run(SearchEngineApplication.class, args);
    }
}
