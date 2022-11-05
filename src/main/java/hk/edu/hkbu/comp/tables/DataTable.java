package hk.edu.hkbu.comp.tables;

import lombok.Getter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataTable implements Serializable {
    @Getter
    private final Map<String, Set<PageInfo>> index = new HashMap<>();
    public void add(String key, PageInfo page) {
        Set<PageInfo> set = index.computeIfAbsent(key, k -> new HashSet<>());
        set.add(page);
    }
    public Set<PageInfo> search(String keyword) {
        return index.getOrDefault(keyword, new HashSet<>());
    }
}
