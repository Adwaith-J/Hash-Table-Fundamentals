import java.util.*;

public class FlashSaleInventoryManager {

    private HashMap<String, Integer> inventory = new HashMap<>();

    private HashMap<String, Queue<Integer>> waitingList = new HashMap<>();

    public FlashSaleInventoryManager() {
        inventory.put("IPHONE15_256GB", 100);
        waitingList.put("IPHONE15_256GB", new LinkedList<>());
    }
    public synchronized int checkStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    public synchronized String purchaseItem(String productId, int userId) {

        int stock = inventory.getOrDefault(productId, 0);

        if (stock > 0) {
            inventory.put(productId, stock - 1);
            return "Success, " + (stock - 1) + " units remaining";
        }

        Queue<Integer> queue = waitingList.get(productId);
        queue.add(userId);

        return "Added to waiting list, position #" + queue.size();
    }

    public synchronized void restock(String productId, int quantity) {

        inventory.put(productId,
                inventory.getOrDefault(productId, 0) + quantity);

        Queue<Integer> queue = waitingList.get(productId);

        while (inventory.get(productId) > 0 && !queue.isEmpty()) {

            int user = queue.poll();
            inventory.put(productId, inventory.get(productId) - 1);

            System.out.println("User " + user + " purchase fulfilled from waiting list.");
        }
    }

    public static void main(String[] args) {

        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        System.out.println("Stock: " + manager.checkStock("IPHONE15_256GB"));

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));

        for (int i = 0; i < 100; i++) {
            manager.purchaseItem("IPHONE15_256GB", i);
        }

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));
    }
}