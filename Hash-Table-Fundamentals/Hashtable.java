import java.util.*;

class VideoData {
    String id;
    String data;

    VideoData(String id, String data) {
        this.id = id;
        this.data = data;
    }
}

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

class MultiLevelCache {
    private final LRUCache<String, VideoData> l1 = new LRUCache<>(10000);
    private final LRUCache<String, VideoData> l2 = new LRUCache<>(100000);
    private final Map<String, VideoData> l3 = new HashMap<>();
    private final Map<String, Integer> accessCount = new HashMap<>();

    private int l1Hits = 0;
    private int l2Hits = 0;
    private int l3Hits = 0;

    private final int promoteThreshold = 5;

    public VideoData getVideo(String id) {
        if (l1.containsKey(id)) {
            l1Hits++;
            accessCount.put(id, accessCount.getOrDefault(id, 0) + 1);
            return l1.get(id);
        }

        if (l2.containsKey(id)) {
            l2Hits++;
            VideoData data = l2.get(id);
            int count = accessCount.getOrDefault(id, 0) + 1;
            accessCount.put(id, count);
            if (count >= promoteThreshold) {
                l1.put(id, data);
            }
            return data;
        }

        if (l3.containsKey(id)) {
            l3Hits++;
            VideoData data = l3.get(id);
            l2.put(id, data);
            accessCount.put(id, 1);
            return data;
        }

        return null;
    }

    public void putVideo(VideoData video) {
        l3.put(video.id, video);
    }

    public void invalidate(String id) {
        l1.remove(id);
        l2.remove(id);
        l3.remove(id);
        accessCount.remove(id);
    }

    public void getStatistics() {
        int total = l1Hits + l2Hits + l3Hits;
        double l1Rate = total == 0 ? 0 : (l1Hits * 100.0) / total;
        double l2Rate = total == 0 ? 0 : (l2Hits * 100.0) / total;
        double l3Rate = total == 0 ? 0 : (l3Hits * 100.0) / total;

        System.out.println("L1 Hit Rate: " + l1Rate + "% Avg Time: 0.5ms");
        System.out.println("L2 Hit Rate: " + l2Rate + "% Avg Time: 5ms");
        System.out.println("L3 Hit Rate: " + l3Rate + "% Avg Time: 150ms");

        double overallHit = ((l1Hits + l2Hits) * 100.0) / (total == 0 ? 1 : total);
        System.out.println("Overall Hit Rate: " + overallHit + "%");
    }

    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        cache.putVideo(new VideoData("video_123", "dataA"));
        cache.putVideo(new VideoData("video_999", "dataB"));

        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_999");

        cache.getStatistics();
    }
}