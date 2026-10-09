package com.mycompany.bachhoaxanhonline.config;

import com.mycompany.bachhoaxanhonline.util.ConfigUtil;
import vn.payos.PayOS;

public class PayOSConfig {

    private static PayOS payOS;

    public static synchronized PayOS getPayOS() {
        if (payOS == null) {
            String clientId = ConfigUtil.get("PAYOS_CLIENT_ID", "NHAP_CLIENT_ID_CUA_BAN_VAO_FILE_.ENV");
            String apiKey = ConfigUtil.get("PAYOS_API_KEY", "NHAP_API_KEY_CUA_BAN_VAO_FILE_.ENV");
            String checksumKey = ConfigUtil.get("PAYOS_CHECKSUM_KEY", "NHAP_CHECKSUM_KEY_CUA_BAN_VAO_FILE_.ENV");
            
            payOS = new PayOS(clientId, apiKey, checksumKey);
        }
        return payOS;
    }
}
