package com.mycompany.bachhoaxanhonline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích đọc cấu hình linh hoạt:
 * 1. Ưu tiên cao nhất: Biến môi trường hệ thống (System.getenv) - dùng cho Docker / Render.
 * 2. Ưu tiên thứ hai: File .env tại thư mục làm việc hoặc classpath - dùng cho môi trường Local Dev.
 * 3. Fallback: Giá trị mặc định được truyền vào.
 */
public class ConfigUtil {

    private static final Map<String, String> ENV_CACHE = new HashMap<>();
    private static boolean initialized = false;

    static {
        loadDotEnv();
    }

    private static synchronized void loadDotEnv() {
        if (initialized) {
            return;
        }
        initialized = true;

        // Thử tìm file .env tại working directory hoặc thư mục cha
        File[] candidateFiles = new File[]{
            new File(".env"),
            new File("..", ".env"),
            new File(System.getProperty("user.dir", "."), ".env")
        };

        for (File candidate : candidateFiles) {
            if (candidate.exists() && candidate.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(candidate, StandardCharsets.UTF_8))) {
                    parseEnvLines(reader);
                    return;
                } catch (Exception e) {
                    System.err.println("Không thể đọc file .env từ: " + candidate.getAbsolutePath() + " - " + e.getMessage());
                }
            }
        }

        // Thử tìm trong ClassLoader (resources)
        try (InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream(".env")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    parseEnvLines(reader);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void parseEnvLines(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eqIdx = line.indexOf('=');
            if (eqIdx > 0) {
                String key = line.substring(0, eqIdx).trim();
                String value = line.substring(eqIdx + 1).trim();
                // Bỏ dấu nháy kép hoặc nháy đơn bao ngoài nếu có
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    if (value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                ENV_CACHE.put(key, value);
            }
        }
    }

    /**
     * Lấy giá trị biến cấu hình.
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Lấy giá trị biến cấu hình kèm giá trị mặc định.
     */
    public static String get(String key, String defaultValue) {
        if (key == null || key.isEmpty()) {
            return defaultValue;
        }

        // 1. Kiểm tra System.getenv (Ưu tiên số 1 cho Render/Docker)
        String systemEnv = System.getenv(key);
        if (systemEnv != null && !systemEnv.trim().isEmpty()) {
            return systemEnv.trim();
        }

        // 2. Kiểm tra System.getProperty
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.trim().isEmpty()) {
            return systemProp.trim();
        }

        // 3. Kiểm tra .env cache
        String fileEnv = ENV_CACHE.get(key);
        if (fileEnv != null && !fileEnv.trim().isEmpty()) {
            return fileEnv.trim();
        }

        return defaultValue;
    }

    /**
     * Lấy giá trị số nguyên.
     */
    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
