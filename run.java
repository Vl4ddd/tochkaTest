
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    static class DateInfo {
        String date;
        int change;

        DateInfo(String date, int change) {
            this.date = date;
            this.change = change;
        }
    }

    public static boolean checkCapacity(int maxCapacity, List<Map<String, String>> guests) {
        List<DateInfo> dateInfoList = new ArrayList<>();
        for (Map<String, String> guest : guests) {
            String checkIn = guest.get("check-in");
            String checkOut = guest.get("check-out");
            dateInfoList.add(new DateInfo(checkIn, 1));
            dateInfoList.add(new DateInfo(checkOut, -1));
        }

        Collections.sort(dateInfoList,
                Comparator.comparing((DateInfo d) -> d.date)
                        .thenComparingInt(d -> d.change));

        int currentGuests = 0;
        for (DateInfo dateInfo : dateInfoList) {
            currentGuests += dateInfo.change;
            if (currentGuests > maxCapacity) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.substring(1, json.length() - 1);

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim().replace("\"", "");
            map.put(key, value);
        }

        return map;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int maxCapacity = Integer.parseInt(scanner.nextLine());

        int n = Integer.parseInt(scanner.nextLine());

        List<Map<String, String>> guests = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String jsonGuest = scanner.nextLine();
            Map<String, String> guest = parseJsonToMap(jsonGuest);
            guests.add(guest);
        }

        boolean result = checkCapacity(maxCapacity, guests);

        System.out.println(result ? "True" : "False");

        scanner.close();
    }
}