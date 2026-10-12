package com.mycompany.bachhoaxanhonline.util;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public final class CodeGeneratorUtil {

    private CodeGeneratorUtil() {
    }

    /**
     * Sinh mã tự tăng theo tiền tố chuẩn (SP0001, HD0001, NV0001, KH0001, DM0001, GH0001, TT0001, KM0001...).
     * 
     * @param em EntityManager hiện tại
     * @param entityName Tên JPA Entity (ví dụ: "Product", "Order", "Auth", "Category", "Cart", "Payment", "Promotion")
     * @param idFieldName Tên trường khóa chính trong Entity (ví dụ: "maSanPham", "maDonHang", "maNguoiDung", ...)
     * @param prefix Tiền tố mã (ví dụ: "SP", "HD", "NV", "KH", "DM", "GH", "TT", "KM")
     * @param digits Số chữ số đệm số 0 (ví dụ: 4 -> SP0001)
     * @return Chuỗi mã tiếp theo
     */
    public static synchronized String generateNextCode(EntityManager em, String entityName, String idFieldName, String prefix, int digits) {
        if (prefix == null) {
            prefix = "";
        }
        String cleanPrefix = prefix.trim().toUpperCase();
        String jpql = "SELECT e." + idFieldName + " FROM " + entityName + " e WHERE UPPER(e." + idFieldName + ") LIKE :pattern";

        List<String> ids = em.createQuery(jpql, String.class)
                .setParameter("pattern", cleanPrefix + "%")
                .getResultList();

        long maxNum = 0;
        for (String id : ids) {
            if (id == null) {
                continue;
            }
            String trimmed = id.trim().toUpperCase();
            if (trimmed.startsWith(cleanPrefix)) {
                String numPart = trimmed.substring(cleanPrefix.length());
                if (numPart.startsWith("_") || numPart.startsWith("-")) {
                    numPart = numPart.substring(1);
                }
                try {
                    long val = Long.parseLong(numPart);
                    if (val > maxNum) {
                        maxNum = val;
                    }
                } catch (NumberFormatException ignored) {
                    // Bỏ qua nếu có ký tự đặc biệt hoặc mã UUID cũ không phải số
                }
            }
        }

        long nextNum = maxNum + 1;
        return String.format("%s%0" + digits + "d", cleanPrefix, nextNum);
    }

    /**
     * Tự động quản lý lifecycle của EntityManager khi không có sẵn em.
     */
    public static synchronized String generateNextCode(String entityName, String idFieldName, String prefix, int digits) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return generateNextCode(em, entityName, idFieldName, prefix, digits);
        } finally {
            em.close();
        }
    }
}
