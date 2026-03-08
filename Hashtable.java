import java.util.*;

public class UsernameChecker {

    private HashMap<String, Integer> usernameToUserId = new HashMap<>();

    private HashMap<String, Integer> attemptFrequency = new HashMap<>();

    public UsernameChecker() {
        usernameToUserId.put("john_doe", 101);
        usernameToUserId.put("admin", 1);
        usernameToUserId.put("alex", 102);
    }
    public boolean checkAvailability(String username) {

        attemptFrequency.put(username,
                attemptFrequency.getOrDefault(username, 0) + 1);

        return !usernameToUserId.containsKey(username);
    }

    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!usernameToUserId.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String dotVersion = username.replace("_", ".");
        if (!usernameToUserId.containsKey(dotVersion)) {
            suggestions.add(dotVersion);
        }

        return suggestions;
    }

    public String getMostAttempted() {

        String mostAttempted = null;
        int max = 0;

        for (String username : attemptFrequency.keySet()) {
            int count = attemptFrequency.get(username);

            if (count > max) {
                max = count;
                mostAttempted = username;
            }
        }

        return mostAttempted;
    }
    public void registerUser(String username, int userId) {
        usernameToUserId.put(username, userId);
    }

    public static void main(String[] args) {

        UsernameChecker system = new UsernameChecker();

        System.out.println(system.checkAvailability("john_doe")); 

        System.out.println(system.checkAvailability("jane_smith")); 

        System.out.println(system.suggestAlternatives("john_doe"));

        System.out.println(system.getMostAttempted());
    }
}