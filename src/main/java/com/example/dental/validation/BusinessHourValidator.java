package com.example.dental.validation;

import com.example.dental.form.BusinessHourForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalTime;

public class BusinessHourValidator implements ConstraintValidator<ValidBusinessHour, BusinessHourForm> {

    @Override
    public boolean isValid(BusinessHourForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        // 休診日の場合はバリデーション不要
        if (form.getRegularHoliday() != null && form.getRegularHoliday()) {
            return true;
        }

        boolean isValid = true;
        String dayLabel = form.getDayOfWeekLabel() != null ? form.getDayOfWeekLabel() : "";

        LocalTime openAt = form.getOpenAt();
        LocalTime closeAt = form.getCloseAt();
        LocalTime breakStart = form.getBreakStartAt();
        LocalTime breakEnd = form.getBreakEndAt();

        // 1. 営業時間は必須
        if (openAt == null || closeAt == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(dayLabel + "曜日の営業時間が入力されていません。")
                    .addPropertyNode("openAt").addConstraintViolation();
            return false;
        }

        // 2. 開始時間より終了時間が早い（または同じ）場合はNG
        if (!openAt.isBefore(closeAt)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(dayLabel + "曜日の閉店時間は開店時間より後にしてください。")
                    .addPropertyNode("closeAt").addConstraintViolation();
            isValid = false;
        }

        // 3. 休憩時間のバリデーション
        if (breakStart != null || breakEnd != null) {
            // 片方のみはNG
            if (breakStart == null || breakEnd == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(dayLabel + "曜日の休憩時間が正しく入力されていません。")
                        .addPropertyNode("breakStartAt").addConstraintViolation();
                isValid = false;
            } else {
                // 休憩開始より終了が早い（または同じ）場合はNG
                if (!breakStart.isBefore(breakEnd)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(dayLabel + "曜日の休憩終了時間は休憩開始時間より後にしてください。")
                            .addPropertyNode("breakEndAt").addConstraintViolation();
                    isValid = false;
                }
                
                // 休憩時間は営業時間の範囲内でなければならない
                // 休憩開始が営業開始より前、または休憩終了が営業終了より後の場合はNG
                if (breakStart.isBefore(openAt) || breakEnd.isAfter(closeAt)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(dayLabel + "曜日の休憩時間は営業時間の範囲内で設定してください。")
                            .addPropertyNode("breakStartAt").addConstraintViolation();
                    isValid = false;
                }
            }
        }

        return isValid;
    }
}
