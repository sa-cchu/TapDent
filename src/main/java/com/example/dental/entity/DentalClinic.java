package com.example.dental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.hibernate.annotations.SQLDelete;

import com.example.dental.enums.ContractStatusName;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dental_clinic")
@SQLDelete(sql = "UPDATE dental_clinic SET is_deleted = true WHERE dental_id = ?")
@Getter
@Setter
public class DentalClinic {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "dental_id")
   private Long dentalId;

   @Column(name = "login_id", nullable = false, unique = true, length = 50)
   private String loginId;

   @Column(nullable = false, length = 255)
   private String password;

   @Column(nullable = false, length = 50)
   private String name;

   @Column(length = 255)
   private String address;

   @Column(length = 20)
   private String tel;

   @Column(length = 255)
   private String mail;

   @Enumerated(EnumType.STRING)
   @Column(name = "contract_status", nullable = false, length = 10)
   private ContractStatusName contractStatus;

   @Column(name = "max_reserve_month")
   private Integer maxReserveMonth;

   @Column(name = "reservation_restrictions", nullable = false)
   private Boolean reservationRestrictions = false;

   // 医院アカウントに権限を紐付け
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "role_id", nullable = false)
   private Role role;

   // URL悪用防止用のランダムトークン
   @Column(name = "public_url_token", nullable = false, unique = true, length = 255)
   private String publicUrlToken;

   @Column(name = "limit_dentist", nullable = false)
   private Integer limitDentist = 0;

   @Column(name = "limit_hygienist", nullable = false)
   private Integer limitHygienist = 0;

   @Column(name = "limit_orthodontist", nullable = false)
   private Integer limitOrthodontist = 0;

   @Column(name = "limit_implantologist", nullable = false)
   private Integer limitImplantologist = 0;

}