package com.mycompany.bachhoaxanhonline.module.employee;

import com.mycompany.bachhoaxanhonline.config.JpaUtil;
import com.mycompany.bachhoaxanhonline.module.auth.Auth;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmployeeEntityJpaTest {

    @Test
    public void testEmployeeEntityJpaLifecycle() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        long rand = System.currentTimeMillis() % 1000000;
        String employeeId = "emp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String username = "emp_jpa_" + rand;
        String phone = "09" + String.format("%08d", rand);
        String fullName = "Tran Thi Nhan Vien";

        try {
            tx.begin();

            // 1. Khởi tạo đối tượng Auth (NguoiDung)
            Auth user = new Auth();
            user.setMaNguoiDung(employeeId);
            user.setTenND(fullName);
            user.setTenDangNhap(username);
            user.setEmail("emp_" + rand + "@bachhoaxanh.test");
            user.setSoDienThoai(phone);
            user.setMatKhauHashed("hashed_pwd_example");
            user.setNgaySinh(LocalDate.of(1995, 5, 20));
            user.setDeleted(false);

            // 2. Khởi tạo đối tượng Employee liên kết với Auth qua @MapsId
            Employee emp = new Employee();
            emp.setUser(user);
            emp.setChucVu(EmployeeRole.STAFF);
            emp.setNgayVaoLam(LocalDate.now());
            emp.setDeleted(false);

            // 3. Persist Employee (CascadeType.PERSIST sẽ lưu đồng thời cả Auth và Employee)
            em.persist(emp);
            tx.commit();

            // 4. Đọc lại từ database sau khi xóa EntityManager cache
            em.clear();
            Employee foundEmp = em.find(Employee.class, employeeId);
            Assertions.assertNotNull(foundEmp);
            Assertions.assertEquals(employeeId, foundEmp.getMaNhanVien());
            Assertions.assertEquals(EmployeeRole.STAFF, foundEmp.getChucVu());
            Assertions.assertEquals(LocalDate.now(), foundEmp.getNgayVaoLam());
            Assertions.assertFalse(foundEmp.getDeleted());
            Assertions.assertNotNull(foundEmp.getUser());
            Assertions.assertEquals(fullName, foundEmp.getFullName());
            Assertions.assertEquals(username, foundEmp.getUsername());
            Assertions.assertEquals(phone, foundEmp.getPhoneNumber());

            // 5. Test NamedQuery "Employee.findByRole"
            List<Employee> staffs = em.createNamedQuery("Employee.findByRole", Employee.class)
                    .setParameter("chucVu", EmployeeRole.STAFF)
                    .getResultList();
            Assertions.assertFalse(staffs.isEmpty());

            // 6. Test NamedQuery "Employee.findByUsername"
            Employee empByUsername = em.createNamedQuery("Employee.findByUsername", Employee.class)
                    .setParameter("tenDangNhap", username)
                    .getSingleResult();
            Assertions.assertNotNull(empByUsername);
            Assertions.assertEquals(employeeId, empByUsername.getMaNhanVien());

            // 7. Cập nhật chức vụ lên ADMIN và test update
            tx.begin();
            foundEmp.setChucVu(EmployeeRole.ADMIN);
            em.merge(foundEmp);
            tx.commit();

            em.clear();
            Employee updatedEmp = em.find(Employee.class, employeeId);
            Assertions.assertEquals(EmployeeRole.ADMIN, updatedEmp.getChucVu());

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
