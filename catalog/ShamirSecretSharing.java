import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ShamirSecretSharing {

    
    public static int decodeValue(String base, String value) {
        return Integer.parseInt(value, Integer.parseInt(base));
    }

    
    public static double lagrangeInterpolation(List<int[]> points) {
        double result = 0.0;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            double term = points.get(i)[1];  // y_i
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    term *= (0.0 - points.get(j)[0]) / (points.get(i)[0] - points.get(j)[0]);
                }
            }
            result += term;
        }

        return result;
    }

    
    public static Map<String, Map<String, String>> parseJson(String jsonInput) {
        Map<String, Map<String, String>> data = new HashMap<>();

        
        Pattern keysPattern = Pattern.compile("\"keys\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher keysMatcher = keysPattern.matcher(jsonInput);
        if (keysMatcher.find()) {
            String keysSection = keysMatcher.group(1);
            Map<String, String> keysMap = new HashMap<>();

            
            Pattern keyValuePattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\\d+)");
            Matcher keyValueMatcher = keyValuePattern.matcher(keysSection);
            while (keyValueMatcher.find()) {
                keysMap.put(keyValueMatcher.group(1), keyValueMatcher.group(2));
            }
            data.put("keys", keysMap);
        }

        
        Pattern rootPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher rootMatcher = rootPattern.matcher(jsonInput);
        while (rootMatcher.find()) {
            String key = rootMatcher.group(1);
            String rootSection = rootMatcher.group(2);

            Map<String, String> rootMap = new HashMap<>();
            Pattern innerPattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"(.*?)\"");
            Matcher innerMatcher = innerPattern.matcher(rootSection);
            while (innerMatcher.find()) {
                rootMap.put(innerMatcher.group(1), innerMatcher.group(2));
            }
            data.put(key, rootMap);
        }

        return data;
    }

    
    public static int findSecret(String jsonInput) {
        
        Map<String, Map<String, String>> data = parseJson(jsonInput);

        
        System.out.println("Parsed Data: " + data);

        int n = Integer.parseInt(data.get("keys").get("n"));
        int k = Integer.parseInt(data.get("keys").get("k"));

        
        List<int[]> points = new ArrayList<>();
        for (String key : data.keySet()) {
            if (key.matches("\\d+")) {  // Only consider numeric keys (ignore 'keys' section)
                int x = Integer.parseInt(key);

                
                Map<String, String> innerMap = data.get(key);
                if (innerMap != null && innerMap.containsKey("base") && innerMap.containsKey("value")) {
                    String base = innerMap.get("base");
                    String yEncoded = innerMap.get("value");

                    
                    int y = decodeValue(base, yEncoded);
                    points.add(new int[]{x, y});
                } else {
                    System.out.println("Missing 'base' or 'value' for key: " + key);
                }
            }
        }

        // Sort the points list by the x-value, and take only the first k points for interpolation
        points = points.stream().sorted(Comparator.comparingInt(o -> o[0])).limit(k).collect(Collectors.toList());

        
        double secret = lagrangeInterpolation(points);

        
        return (int) Math.round(secret) % 13;
    }

    
    public static void main(String[] args) {
        String jsonInput = "{\n" +
            "    \"keys\": {\n" +
            "        \"n\": 4,\n" +
            "        \"k\": 3\n" +
            "    },\n" +
            "    \"1\": {\n" +
            "        \"base\": \"10\",\n" +
            "        \"value\": \"4\"\n" +
            "    },\n" +
            "    \"2\": {\n" +
            "        \"base\": \"2\",\n" +
            "        \"value\": \"111\"\n" +
            "    },\n" +
            "    \"3\": {\n" +
            "        \"base\": \"10\",\n" +
            "        \"value\": \"12\"\n" +
            "    },\n" +
            "    \"6\": {\n" +
            "        \"base\": \"4\",\n" +
            "        \"value\": \"213\"\n" +
            "    }\n" +
            "}";

        
        int secret = findSecret(jsonInput);
        System.out.println("The secret is: " + secret);
    }
}