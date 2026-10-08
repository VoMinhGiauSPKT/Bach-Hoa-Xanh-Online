package com.mycompany.bachhoaxanhonline.module.user;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public static class EmployeeInfo {
        private final String position;
        private final LocalDate hireDate;

        public EmployeeInfo(String position, LocalDate hireDate) {
            this.position = position;
            this.hireDate = hireDate;
        }

        public String getPosition() {
            return position;
        }

        public LocalDate getHireDate() {
            return hireDate;
        }
    }

    public Optional<Auth> findUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Auth user = em.find(Auth.class, userId.trim());
            return Optional.ofNullable(user);
        } finally {
            em.close();
        }
    }

    public Optional<Customer> findCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Customer customer = em.find(Customer.class, customerId.trim());
            return Optional.ofNullable(customer);
        } finally {
            em.close();
        }
    }

    public boolean isEmployee(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"NhanVien\" WHERE \"maNhanVien\" = :id AND \"Deleted\" = FALSE")
                    .setParameter("id", userId.trim())
                    .getSingleResult();
            return count != null && count.longValue() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            em.close();
        }
    }

    public EmployeeInfo getEmployeeInfo(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT \"chucVu\", \"ngayVaoLam\" FROM \"NhanVien\" WHERE \"maNhanVien\" = :id AND \"Deleted\" = FALSE")
                    .setParameter("id", userId.trim())
                    .getResultList();

            if (rows.isEmpty()) {
                return null;
            }

            Object[] row = rows.get(0);
            String position = row[0] != null ? row[0].toString() : "STAFF";
            LocalDate hireDate = null;
            if (row[1] instanceof Date) {
                hireDate = ((Date) row[1]).toLocalDate();
            } else if (row[1] instanceof LocalDate) {
                hireDate = (LocalDate) row[1];
            }

            return new EmployeeInfo(position, hireDate);
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<UserResponse.AddressData> findAddressesByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT \"maDiaChi\", \"tenNguoiNhan\", \"soDienThoai\", \"soNha\", \"phuong\", \"tinh\", \"laMacDinh\" "
                            +
                            "FROM \"DiaChi\" WHERE \"maKhachHang\" = :id AND \"Deleted\" = FALSE " +
                            "ORDER BY \"laMacDinh\" DESC, \"maDiaChi\" ASC")
                    .setParameter("id", customerId.trim())
                    .getResultList();

            List<UserResponse.AddressData> addresses = new ArrayList<>();
            for (Object[] row : rows) {
                Long addressId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String receiverName = row[1] != null ? row[1].toString() : "";
                String phoneNumber = row[2] != null ? row[2].toString() : "";
                String street = row[3] != null ? row[3].toString() : "";
                String ward = row[4] != null ? row[4].toString() : "";
                String city = row[5] != null ? row[5].toString() : "";
                Boolean isDefault = row[6] != null ? Boolean.valueOf(row[6].toString()) : false;

                addresses.add(new UserResponse.AddressData(
                        addressId, receiverName, phoneNumber, street, ward, city, isDefault));
            }
            return addresses;
        } catch (Exception e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public void updatePasswordAndRevokeToken(String userId, String newHashedPassword) {
        if (userId == null || newHashedPassword == null) {
            return;
        }
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Auth user = em.find(Auth.class, userId.trim());
            if (user != null) {
                user.setMatKhauHashed(newHashedPassword);
                user.setRefreshToken(null);
                em.merge(user);
            }

            try {
                em.createNativeQuery("UPDATE \"KhachHang\" SET \"refreshToken\" = NULL WHERE \"maKhachHang\" = :id")
                        .setParameter("id", userId.trim())
                        .executeUpdate();
            } catch (Exception ignored) {
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật mật khẩu trong cơ sở dữ liệu: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
