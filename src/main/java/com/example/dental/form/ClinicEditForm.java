package com.example.dental.form;

import com.example.dental.enums.ContractStatusName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 歯科医院編集用のフォームクラス
 */
public class ClinicEditForm {

    @NotBlank(message = "医院名を入力してください")
    @Size(max = 50, message = "医院名は50文字以内で入力してください")
    private String name;

    @Size(max = 255, message = "住所は255文字以内で入力してください")
    private String address;

    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    private String mail;

    @NotNull(message = "契約ステータスを選択してください")
    private ContractStatusName contractStatus;

    // パスワードは空の場合は変更しないものとする
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;

    // --- Getter / Setter ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public ContractStatusName getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(ContractStatusName contractStatus) {
        this.contractStatus = contractStatus;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
