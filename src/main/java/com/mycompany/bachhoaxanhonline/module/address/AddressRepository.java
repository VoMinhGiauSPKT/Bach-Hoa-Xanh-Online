package com.mycompany.bachhoaxanhonline.module.address;

import com.mycompany.bachhoaxanhonline.entity.*;
import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddressRepository {

    public boolean isCustomerActive(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Object result = em.createNativeQuery(
                    "SELECT u.\"Deleted\" FROM \"NguoiDung\" u "
                    + "JOIN \"KhachHang\" kh ON u.\"maNguoiDung\" = kh.\"maKhachHang\" "
                    + "WHERE u.\"maNguoiDung\" = :customerId")
                    .setParameter("customerId", customerId.trim())
                    .getSingleResult();
            if (result instanceof Boolean) {
                return !((Boolean) result);
            }
            return true;
        } catch (NoResultException e) {
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            em.close();
        }
    }

    public long countByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return 0L;
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM \"DiaChi\" WHERE \"maKhachHang\" = :customerId")
                    .setParameter("customerId", customerId.trim())
                    .getSingleResult();
            return count != null ? count.longValue() : 0L;
        } catch (Exception e) {
            return 0L;
        } finally {
            em.close();
        }
    }

    public List<Address> findActiveByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createNativeQuery(
                    "SELECT \"maDiaChi\", \"tenNguoiNhan\", \"soDienThoai\", \"soNha\", \"phuong\", \"tinh\", "
                    + "\"laMacDinh\", \"Deleted\", \"maKhachHang\" "
                    + "FROM \"DiaChi\" "
                    + "WHERE \"maKhachHang\" = :customerId AND \"Deleted\" = false "
                    + "ORDER BY \"laMacDinh\" DESC, \"maDiaChi\" DESC")
                    .setParameter("customerId", customerId.trim());

            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();
            List<Address> addresses = new ArrayList<>();
            for (Object[] row : rows) {
                addresses.add(mapRowToAddress(row));
            }
            return addresses;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Address> findById(Long addressId) {
        if (addressId == null) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createNativeQuery(
                    "SELECT \"maDiaChi\", \"tenNguoiNhan\", \"soDienThoai\", \"soNha\", \"phuong\", \"tinh\", "
                    + "\"laMacDinh\", \"Deleted\", \"maKhachHang\" "
                    + "FROM \"DiaChi\" "
                    + "WHERE \"maDiaChi\" = :addressId")
                    .setParameter("addressId", addressId);

            Object[] row = (Object[]) query.getSingleResult();
            return Optional.of(mapRowToAddress(row));
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public Address insertAddress(Address address, boolean resetOthers) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (resetOthers) {
                em.createNativeQuery(
                        "UPDATE \"DiaChi\" SET \"laMacDinh\" = false WHERE \"maKhachHang\" = :customerId")
                        .setParameter("customerId", address.getMaKhachHang())
                        .executeUpdate();
            }

            Query insertQuery = em.createNativeQuery(
                    "INSERT INTO \"DiaChi\" (\"tenNguoiNhan\", \"soDienThoai\", \"soNha\", \"phuong\", \"tinh\", "
                    + "\"laMacDinh\", \"Deleted\", \"maKhachHang\") "
                    + "VALUES (:name, :phone, :street, :ward, :city, :isDefault, false, :customerId) "
                    + "RETURNING \"maDiaChi\"")
                    .setParameter("name", address.getTenNguoiNhan())
                    .setParameter("phone", address.getSoDienThoai())
                    .setParameter("street", address.getSoNha())
                    .setParameter("ward", address.getPhuong())
                    .setParameter("city", address.getTinh())
                    .setParameter("isDefault", address.getLaMacDinh())
                    .setParameter("customerId", address.getMaKhachHang());

            Number generatedId = (Number) insertQuery.getSingleResult();
            address.setMaDiaChi(generatedId.longValue());

            tx.commit();
            return address;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi lưu địa chỉ: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Address updateAddress(Long addressId, String customerId, String name, String phone,
                                 String street, String ward, String city, boolean isDefault) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (isDefault) {
                em.createNativeQuery(
                        "UPDATE \"DiaChi\" SET \"laMacDinh\" = false WHERE \"maKhachHang\" = :customerId")
                        .setParameter("customerId", customerId)
                        .executeUpdate();
            }

            em.createNativeQuery(
                    "UPDATE \"DiaChi\" SET "
                    + "\"tenNguoiNhan\" = :name, "
                    + "\"soDienThoai\" = :phone, "
                    + "\"soNha\" = :street, "
                    + "\"phuong\" = :ward, "
                    + "\"tinh\" = :city, "
                    + "\"laMacDinh\" = :isDefault "
                    + "WHERE \"maDiaChi\" = :addressId AND \"maKhachHang\" = :customerId")
                    .setParameter("name", name)
                    .setParameter("phone", phone)
                    .setParameter("street", street)
                    .setParameter("ward", ward)
                    .setParameter("city", city)
                    .setParameter("isDefault", isDefault)
                    .setParameter("addressId", addressId)
                    .setParameter("customerId", customerId)
                    .executeUpdate();

            tx.commit();
            return new Address(addressId, name, phone, street, ward, city, isDefault, false, customerId);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi cập nhật địa chỉ: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void setDefaultAddress(Long addressId, String customerId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Đặt toàn bộ địa chỉ của khách hàng này về false
            em.createNativeQuery(
                    "UPDATE \"DiaChi\" SET \"laMacDinh\" = false WHERE \"maKhachHang\" = :customerId")
                    .setParameter("customerId", customerId)
                    .executeUpdate();

            // 2. Đặt địa chỉ được chỉ định thành true
            em.createNativeQuery(
                    "UPDATE \"DiaChi\" SET \"laMacDinh\" = true WHERE \"maDiaChi\" = :addressId AND \"maKhachHang\" = :customerId")
                    .setParameter("addressId", addressId)
                    .setParameter("customerId", customerId)
                    .executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi đặt địa chỉ mặc định: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void deleteAddress(Long addressId, String customerId, boolean wasDefault) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Xóa cứng địa chỉ
            em.createNativeQuery(
                    "DELETE FROM \"DiaChi\" WHERE \"maDiaChi\" = :addressId AND \"maKhachHang\" = :customerId")
                    .setParameter("addressId", addressId)
                    .setParameter("customerId", customerId)
                    .executeUpdate();

            // 2. Cơ chế bàn giao mặc định tự động: nếu vừa xóa địa chỉ mặc định
            if (wasDefault) {
                try {
                    Object nextDefaultId = em.createNativeQuery(
                            "SELECT \"maDiaChi\" FROM \"DiaChi\" "
                            + "WHERE \"maKhachHang\" = :customerId AND \"Deleted\" = false "
                            + "ORDER BY \"maDiaChi\" DESC LIMIT 1")
                            .setParameter("customerId", customerId)
                            .getSingleResult();

                    if (nextDefaultId != null) {
                        Long newDefaultId = ((Number) nextDefaultId).longValue();
                        em.createNativeQuery(
                                "UPDATE \"DiaChi\" SET \"laMacDinh\" = true WHERE \"maDiaChi\" = :newDefaultId")
                                .setParameter("newDefaultId", newDefaultId)
                                .executeUpdate();
                    }
                } catch (NoResultException ignored) {
                    // Không còn địa chỉ nào khác
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Lỗi xóa địa chỉ: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    private Address mapRowToAddress(Object[] row) {
        Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
        String name = row[1] != null ? row[1].toString() : null;
        String phone = row[2] != null ? row[2].toString() : null;
        String street = row[3] != null ? row[3].toString() : null;
        String ward = row[4] != null ? row[4].toString() : null;
        String city = row[5] != null ? row[5].toString() : null;
        Boolean isDefault = row[6] != null ? (Boolean) row[6] : false;
        Boolean deleted = row[7] != null ? (Boolean) row[7] : false;
        String customerId = row[8] != null ? row[8].toString() : null;

        return new Address(id, name, phone, street, ward, city, isDefault, deleted, customerId);
    }
}
