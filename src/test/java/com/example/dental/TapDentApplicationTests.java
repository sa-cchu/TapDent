package com.example.dental;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.example.dental.enums.RoleName;

@SpringBootTest
class TapDentApplicationTests {

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void testListTemplate() {
        Context context = new Context();

        com.example.dental.dto.AdminDto admin = new com.example.dental.dto.AdminDto();
        admin.setName("テスト管理者");
        com.example.dental.dto.RoleDto role = new com.example.dental.dto.RoleDto();
        role.setRoleName(RoleName.ROLE_ADMIN);
        admin.setRole(role);

        com.example.dental.dto.DentalClinicDto clinic = new com.example.dental.dto.DentalClinicDto();
        clinic.setDentalId(1L);
        clinic.setName("ひまわり歯科");
        clinic.setLoginId("himawari");
        clinic.setPublicUrlToken("dummy-token-123");
        
        context.setVariable("admin", admin);
        context.setVariable("clinics", java.util.List.of(clinic));
        context.setVariable("searchName", "");
        context.setVariable("searchStatus", null);
        context.setVariable("page", null);
        context.setVariable("statuses", com.example.dental.enums.ContractStatusName.values());

        try {
            String result = templateEngine.process("admin/clinics/list", context);
            System.out.println("RENDERED HTML OUTPUT:\n" + result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
