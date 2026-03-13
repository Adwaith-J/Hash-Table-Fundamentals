import java.util.*;

class ParkingSpot {
    String plate;
    long entry;
    int probes;
    Status status;
}

enum Status {
    EMPTY, OCCUPIED, DELETED
}

class ParkingLot {
    private final ParkingSpot[] table;
    private final int capacity;
    private int size = 0;
    private long totalProbes = 0;
    private final Map<Integer, Integer> hourly = new HashMap<>();

    ParkingLot(int capacity) {
        this.capacity = capacity;
        table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
            table[i].status = Status.EMPTY;
        }
    }

    private int hash(String plate) {
        return Math.abs(plate.hashCode()) % capacity;
    }

    public void parkVehicle(String plate) {
        int index = hash(plate);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;
        }

        table[index].plate = plate;
        table[index].entry = System.currentTimeMillis();
        table[index].probes = probes;
        table[index].status = Status.OCCUPIED;

        size++;
        totalProbes += probes;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourly.put(hour, hourly.getOrDefault(hour, 0) + 1);

        System.out.println("Assigned spot #" + index + " (" + probes + " probes)");
    }

    public void exitVehicle(String plate) {
        int index = hash(plate);

        while (table[index].status != Status.EMPTY) {
            if (table[index].status == Status.OCCUPIED && plate.equals(table[index].plate)) {
                long durationMs = System.currentTimeMillis() - table[index].entry;
                double hours = durationMs / 3600000.0;
                double fee = Math.ceil(hours) * 5;

                table[index].status = Status.DELETED;
                table[index].plate = null;
                size--;

                System.out.println("Spot #" + index + " freed, Duration: " + hours + "h, Fee: $" + fee);
                return;
            }
            index = (index + 1) % capacity;
        }

        System.out.println("Vehicle not found");
    }

    public void getStatistics() {
        double occupancy = (size * 100.0) / capacity;
        double avgProbes = size == 0 ? 0 : totalProbes / (double) size;

        int peakHour = -1;
        int max = 0;
        for (Map.Entry<Integer, Integer> e : hourly.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                peakHour = e.getKey();
            }
        }

        System.out.println("Occupancy: " + occupancy + "%");
        System.out.println("Avg Probes: " + avgProbes);
        System.out.println("Peak Hour: " + peakHour + "-" + (peakHour + 1));
    }

    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot(500);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();
    }
}