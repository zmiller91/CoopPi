package coop.pi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    private static final Context INSTANCE = new Context("C:\\Users\\zmiller\\IdeaProjects\\CoopPi\\context.txt");

    private final Map<String, String> context;

    public Context(String location) {
        this.context = parse(location);
    }

    public String privateKey() {
        return context.get("private_key");
    }

    public String publicKey() {
        return context.get("public_key");
    }

    public String certKey() {
        return context.get("cert_key");
    }

    public String endpoint() {
        return context.get("endpoint");
    }

    public String clientId() {
        return context.get("client_id");
    }

    public String shadowName() {
        return context.get("shadow_name");
    }

    private Map<String, String> parse(String location) {
        try {

            Map<String, String> context = new HashMap<>();
            List<String> configs = Files.readAllLines(Paths.get(location));

            for (String config : configs) {
                String[] parts = config.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    context.put(key, value);
                }
            }

            return context;

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Context getInstance() {
        return INSTANCE;
    }

}
