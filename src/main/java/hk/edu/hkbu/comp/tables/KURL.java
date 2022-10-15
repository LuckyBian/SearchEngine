package hk.edu.hkbu.comp.tables;

import java.util.ArrayList;
import java.util.List;

public class KURL {
    public String key = new String();
    public List<String> title = new ArrayList<String>();
    public List<String> urls = new ArrayList<String>();

    public KURL(String key, String title, String url){
        List<String> t = new ArrayList<String>();
        t.add(title);
        this.title = t;

        this.key = key;

        List<String> u = new ArrayList<String>();
        u.add(url);
        this.urls = u;
    }

    public List<String> getUrls() {
        return urls;
    }

    public String getKey() {
        return key;
    }


    public List<String> getTitle() {
        return title;
    }

    public void setTitle(List<String> title) {
        this.title = title;
    }

    public void setKey(String key) {
        this.key = key;
    }



    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
