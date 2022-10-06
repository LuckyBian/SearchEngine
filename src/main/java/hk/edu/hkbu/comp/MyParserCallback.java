package hk.edu.hkbu.comp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yaml.snakeyaml.nodes.Tag;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

class MyParserCallback extends HTMLEditorKit.ParserCallback {
    public String content = new String();
    public List<String> urls = new ArrayList<String>();

    String loadPlainText(String urlString) throws IOException {
        MyParserCallback callback = new MyParserCallback();
        ParserDelegator parser = new ParserDelegator();

        URL url = new URL(urlString);
        InputStreamReader reader = new InputStreamReader(url.openStream());
        parser.parse(reader, callback, true);

        return callback.content;
    }

    public static List<String> getUniqueWords(String text) {
        String[] words = text.split("[0-9\\W]+");
        ArrayList<String> uniqueWords = new ArrayList<String>();

        for (String w : words) {
            w = w.toLowerCase();

            if (!uniqueWords.contains(w))
                uniqueWords.add(w);
        }

        return uniqueWords;
    }

    String loadWebPage(String urlString) {
        byte[] buffer = new byte[1024];
        String content = new String();

        try {

            URL url = new URL(urlString);
            InputStream in = url.openStream();
            int len;

            while((len = in.read(buffer)) != -1)
                content += new String(buffer);

        } catch (IOException e) {

            content = "<h1>Unable to download the page</h1>" + urlString;

        }

        return content;
    }

    public void handleStartTag(Tag tag, MutableAttributeSet attrSet, int pos)
    {
        if (tag.toString().equals("a")) {

            Enumeration<?> e = attrSet.getAttributeNames();

            while (e.hasMoreElements()) {

                Object aname = e.nextElement();

                if (aname.toString().equals("href")) {
                    String u = (String) attrSet.getAttribute(aname);
                    if (urls.size() < 30 && !urls.contains(u))
                        urls.add(u);
                }
            }
        }
    }

    boolean isAbsURL(String str) {
        return str.matches("^[a-z0-9]+://.+");
    }

    URL toAbsURL(String str, URL ref) throws MalformedURLException {
        URL url = null;

        String prefix = ref.getProtocol() + "://" + ref.getHost();

        if (ref.getPort() > -1)
            prefix += ":" + ref.getPort();

        if (!str.startsWith("/")) {
            int len = ref.getPath().length() - ref.getFile().length();
            String tmp = "/" + ref.getPath().substring(0, len) + "/";
            prefix += tmp.replace("//", "/");
        }
        url = new URL(prefix + str);

        return url;
    }



    List<String> getURLs(String srcPage) throws IOException {
        URL url = new URL(srcPage);
        InputStreamReader reader = new InputStreamReader(url.openStream());

        ParserDelegator parser = new ParserDelegator();
        MyParserCallback callback = new MyParserCallback();
        parser.parse(reader, callback, true);

        for (int i=0; i<callback.urls.size(); i++) {
            String str = callback.urls.get(i);
            if (!isAbsURL(str))
                callback.urls.set(i, toAbsURL(str, url).toString());
        }

        return callback.urls;
    }

    @GetMapping("greeting")
    @ResponseBody
    String sayHello(
            @RequestParam(name = "name", required = false, defaultValue = "there")
            String name) {

        return "<h1>Hello " + name + "!</h1>";
    }



    @Override
    public void handleText(char[] data, int pos) {
        content += " " + new String(data);
    }
}

