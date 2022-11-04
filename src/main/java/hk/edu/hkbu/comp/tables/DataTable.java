package hk.edu.hkbu.comp.tables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataTable {
    private Map<String, Set<PageInfo>> index = new HashMap<>();
    public void add(String key, PageInfo page) {
        Set<PageInfo> set = index.get(key);
        if (set == null) {
            set = new HashSet<>();
            index.put(key, set);
        }
        set.add(page);
    }
    public Set<PageInfo> search(String keyword) {
        return index.getOrDefault(keyword, new HashSet<>());
    }
}
