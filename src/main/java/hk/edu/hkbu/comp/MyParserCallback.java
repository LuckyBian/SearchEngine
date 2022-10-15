package hk.edu.hkbu.comp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tartarus.snowball.ext.englishStemmer;
import org.yaml.snakeyaml.nodes.Tag;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyParserCallback extends HTMLEditorKit.ParserCallback {
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

    public List extraKey(String text){

        String[] result = text.split("[\\p{Punct}\\s]+");
        List<String> lastversion = new ArrayList<String>();

        String[] blabklist = new String[]{"a","an","the","be"};

        for(int i = 0; i < result.length; i++){
            if(is_alpha(result[i])){
                englishStemmer stemmer = new englishStemmer();
                stemmer.setCurrent(result[i]);
                result[i] = stemmer.getCurrent();

                if(Arrays.asList(blabklist).contains(result[i])){
                    continue;
                }

                if(!lastversion.contains(result[i])){
                    lastversion.add(result[i]);
                }
            }
        }
        return lastversion;
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

    public boolean is_alpha(String str) {
        if(str==null) return false;
        return str.matches("[a-zA-Z]+");
    }


    public Boolean goodweb(String url){
        String content = loadWebPage(url);
        String pattern = "<title>([\\s\\S]*?)</title>";
        Pattern r = Pattern.compile(pattern);
        Matcher m1 = r.matcher(content);
        String title = new String();

        if(m1.find()){
            title = m1.group(1).replace("\n","");
            title = title.replace("\t","");
            title = title.replaceAll("\r","");
            title = title.replaceAll("\\p{Punct}", "");
        }
        else{
            return false;
        }
        //System.out.println(title);

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(title);

        if(m.find()){
            return false;
        }

        String[] words = title.split(" ");

        if(words.length > 30){
            return false;
        }
        //System.out.println(title);
        return true;
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


    @Override
    public void handleText(char[] data, int pos) {
        content += " " + new String(data);
    }
}

