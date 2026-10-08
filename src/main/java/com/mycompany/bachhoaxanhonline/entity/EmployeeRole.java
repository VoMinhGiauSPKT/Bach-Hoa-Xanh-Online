package com.mycompany.bachhoaxanhonline.entity;

/**
 * Enum đại diện cho các chức vụ của nhân viên tương ứng với enum_chucvu_nhanvien trong PostgreSQL:
 * CREATE TYPE enum_chucvu_nhanvien AS ENUM ('ADMIN', 'STAFF');
 */
public enum EmployeeRole {
    ADMIN,
    STAFF;

    public static EmployeeRole fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            return STAFF;
        }
        try {
            return EmployeeRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STAFF;
        }
    }
}
