package com.example.dental.enums;

/**
 * 診療メニューに必要なスタッフの種別（予約制限の枠と連動）
 */
public enum TargetStaffType {
    DENTIST,         // 歯科医師
    HYGIENIST,       // 歯科衛生士
    ORTHODONTIST,    // 矯正医
    IMPLANTOLOGIST,  // インプラント医
    NONE             // 制限なし（チェアの空きのみ依存）
}
