import java.util.*;
import java.util.concurrent.*;

class PageViewEvent {
    String url;
    String userId;
    String source;

    public PageViewEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class PageStats {
    String url;
    int views;

    PageStats(String url, int views) {
        this.url = url;
        this.views = views;
    }
}

public class RealTimeAnalytics {

    private Map<String, Integer> pageViews = new ConcurrentHashMap<>();

    private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    private Map<String, Integer> sourceCounts = new ConcurrentHashMap<>();

    public void processEvent(PageViewEvent event) {

        pageViews.put(event.url, pageViews.getOrDefault(event.url, 0) + 1);

        uniqueVisitors.putIfAbsent(event.url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(event.url).add(event.userId);

        sourceCounts.put(event.source,
                sourceCounts.getOrDefault(event.source, 0) + 1);
    }


    public List<PageStats> getTopPages() {

        PriorityQueue<PageStats> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(p -> p.views));

        for (String url : pageViews.keySet()) {

            PageStats stats = new PageStats(url, pageViews.get(url));

            minHeap.offer(stats);

            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<PageStats> result = new ArrayList<>();

        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll());
        }

        Collections.reverse(result);
        return result;
    }


    public void getDashboard() {

        System.out.println("===== REAL TIME DASHBOARD =====");

        System.out.println("\nTop Pages:");

        List<PageStats> topPages = getTopPages();

        int rank = 1;
        for (PageStats p : topPages) {

            int unique = uniqueVisitors.get(p.url).size();

            System.out.println(rank++ + ". "
                    + p.url + " - "
                    + p.views + " views ("
                    + unique + " unique)");
        }

        System.out.println("\nTraffic Sources:");

        int total = sourceCounts.values().stream().mapToInt(i -> i).sum();

        for (String source : sourceCounts.keySet()) {

            int count = sourceCounts.get(source);

            double percent = (count * 100.0) / total;

            System.out.printf("%s: %.2f%%\n", source, percent);
        }

        System.out.println("===============================\n");
    }


    public static void main(String[] args) {

        RealTimeAnalytics analytics = new RealTimeAnalytics();

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(
                analytics::getDashboard,
                5,
                5,
                TimeUnit.SECONDS
        );

        Random rand = new Random();

        String[] urls = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai-future",
                "/world/politics"
        };

        String[] sources = {
                "google",
                "facebook",
                "direct",
                "twitter"
        };

        while (true) {

            PageViewEvent event = new PageViewEvent(
                    urls[rand.nextInt(urls.length)],
                    "user_" + rand.nextInt(10000),
                    sources[rand.nextInt(sources.length)]
            );

            analytics.processEvent(event);
        }
    }
}