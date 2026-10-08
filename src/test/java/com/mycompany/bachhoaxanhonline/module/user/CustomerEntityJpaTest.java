package com.mycompany.bachhoaxanhonline.module.user;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CustomerEntityJpaTest {

    @Test
    public void testCustomerEntityJpaLifecycle() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        long rand = System.currentTimeMillis() % 1000000;
        String customerId = "kh_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String username = "kh_jpa_" + rand;
        String phone = "09" + String.format("%08d", rand);
        String fullName = "Nguyen Van Khach Hang";

        try {
            // Đảm bảo schema của KhachHang khớp với BachHoaXanhDB.sql
            tx.begin();
            em.createNativeQuery("ALTER TABLE \"KhachHang\" ADD COLUMN IF NOT EXISTS \"refreshToken\" TEXT").executeUpdate();
            em.createNativeQuery("ALTER TABLE \"KhachHang\" ADD COLUMN IF NOT EXISTS \"Deleted\" BOOLEAN NOT NULL DEFAULT FALSE").executeUpdate();
            tx.commit();

            tx.begin();

            // 1. Tạo đối tượng Auth (NguoiDung)
            Auth user = new Auth();
            user.setMaNguoiDung(customerId);
            user.setTenND(fullName);
            user.setTenDangNhap(username);
            user.setEmail("kh_" + rand + "@gmail.com");
            user.setSoDienThoai(phone);
            user.setMatKhauHashed("hashed_pwd_example");
            user.setNgaySinh(LocalDate.of(2000, 1, 1));
            user.setDeleted(false);

            // 2. Tạo đối tượng Customer liên kết với Auth qua @MapsId
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setRefreshToken("sample_refresh_token_123");
            customer.setDeleted(false);

            // 3. Persist Customer (CascadeType.PERSIST lưu đồng thời cả Auth và Customer)
            em.persist(customer);
            tx.commit();

            // 4. Đọc lại từ database sau khi xóa EntityManager cache
            em.clear();
            Customer foundCustomer = em.find(Customer.class, customerId);
            Assertions.assertNotNull(foundCustomer);
            Assertions.assertEquals(customerId, foundCustomer.getMaKhachHang());
            Assertions.assertEquals("sample_refresh_token_123", foundCustomer.getRefreshToken());
            Assertions.assertFalse(foundCustomer.getDeleted());
            Assertions.assertNotNull(foundCustomer.getUser());
            Assertions.assertEquals(fullName, foundCustomer.getFullName());
            Assertions.assertEquals(username, foundCustomer.getUsername());
            Assertions.assertEquals(phone, foundCustomer.getPhoneNumber());

            // 5. Test NamedQuery "Customer.findByUsername"
            Customer customerByUsername = em.createNamedQuery("Customer.findByUsername", Customer.class)
                    .setParameter("tenDangNhap", username)
                    .getSingleResult();
            Assertions.assertNotNull(customerByUsername);
            Assertions.assertEquals(customerId, customerByUsername.getMaKhachHang());

            // 6. Test NamedQuery "Customer.findByPhone"
            Customer customerByPhone = em.createNamedQuery("Customer.findByPhone", Customer.class)
                    .setParameter("soDienThoai", phone)
                    .getSingleResult();
            Assertions.assertNotNull(customerByPhone);
            Assertions.assertEquals(customerId, customerByPhone.getMaKhachHang());

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
