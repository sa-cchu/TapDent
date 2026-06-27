# TapDent Database ER Diagram (ER図)

TapDent のエンティティ定義から作成したデータベースのER図および詳細設計情報です。

---

## 1. ER図 (Entity Relationship Diagram)

```mermaid
erDiagram
    roles ||--o{ admins : "has role"
    roles ||--o{ dental_clinic : "has role"
    roles ||--o{ patients : "has role"
    
    dental_clinic ||--o{ business_hours : "manages"
    dental_clinic ||--o{ calendar_exception : "registers"
    dental_clinic ||--o{ dental_chair : "possesses"
    dental_clinic ||--o{ treatment_type : "offers"
    dental_clinic ||--o{ patients : "registers"
    dental_clinic ||--o{ appointments : "manages"
    dental_clinic ||--o{ appointments_history : "logs"

    dental_chair ||--o{ calendar_exception : "restricted by"
    dental_chair ||--o{ chair_treatment : "assigned to"
    dental_chair ||--o{ appointments : "scheduled for"
    dental_chair ||--o{ appointments_history : "logged for"

    treatment_type ||--o{ chair_treatment : "assigned to"
    treatment_type ||--o{ appointments : "scheduled with"
    treatment_type ||--o{ appointments_history : "logged with"

    patients |o--o{ appointments : "makes"
    patients |o--o{ appointments_history : "made"

    token |o--o{ appointments : "verifies"
    token |o--o{ appointments_history : "verified"

    admins {
        int admin_id PK "管理者ID"
        varchar login_id UK "ログインID"
        varchar password "パスワード"
        varchar name "管理者名"
        int role_id FK "ロールID"
    }

    roles {
        int role_id PK "ロールID"
        varchar role_name UK "ロール名"
    }

    dental_clinic {
        bigint dental_id PK "歯科医院ID"
        varchar login_id UK "ログインID"
        varchar password "パスワード"
        varchar name "医院名"
        varchar address "住所"
        varchar tel "電話番号"
        varchar mail "メールアドレス"
        varchar contract_status "契約ステータス"
        int max_reserve_month "最大予約可能月数"
        boolean reservation_restrictions "予約制限有無"
        int role_id FK "ロールID"
        varchar public_url_token UK "公開用URLトークン"
    }

    business_hours {
        bigint business_id PK "営業時間ID"
        bigint dental_id FK, UK "歯科医院ID"
        varchar day_of_week UK "曜日"
        time open_at "開院時間"
        time close_at "閉院時間"
        time break_start_at "休憩開始時間"
        time break_end_at "休憩終了時間"
        boolean regular_holiday "定休日フラグ"
    }

    calendar_exception {
        bigint calendar_id PK "カレンダー例外ID"
        bigint dental_id FK "歯科医院ID"
        date target_date "対象日"
        boolean is_holiday "休診フラグ"
        bigint chair_id FK "チェアID (任意)"
        time start_at "制限開始時間"
        time end_at "制限終了時間"
    }

    dental_chair {
        bigint chair_id PK "チェアID"
        bigint dental_id FK "歯科医院ID"
        varchar chair_name "チェア名"
        boolean status "稼働ステータス"
    }

    chair_treatment {
        bigint ct_id PK "チェア診療ID"
        bigint chair_id FK, UK "チェアID"
        bigint treatment_id FK, UK "診療メニューID"
    }

    treatment_type {
        bigint treatment_id PK "診療メニューID"
        bigint dental_id FK "歯科医院ID"
        varchar treatment_name "診療メニュー名"
        int required_minutes "所要時間(分)"
        boolean status "公開ステータス"
        boolean is_existing_only "既存患者限定フラグ"
    }

    patients {
        bigint patient_id PK "患者ID"
        bigint dental_id FK, UK "歯科医院ID"
        varchar patient_code UK "患者番号"
        varchar name "氏名"
        date birthday "生年月日"
        varchar tel "電話番号"
        varchar email "メールアドレス"
        varchar status "ステータス"
        int role_id FK "ロールID"
    }

    token {
        bigint token_id PK "トークンID"
        varchar token_value UK "トークン値"
        bigint dental_id "歯科医院ID (論理関連)"
        varchar name "氏名"
        date birthday "生年月日"
        varchar tel "電話番号"
        varchar email "メールアドレス"
        datetime expiry_time "有効期限"
    }

    appointments {
        bigint appointment_id PK "予約ID"
        bigint dental_id FK "歯科医院ID"
        bigint chair_id FK "チェアID"
        bigint patient_id FK "患者ID (任意)"
        bigint token_id FK "トークンID (任意)"
        bigint treatment_id FK "診療メニューID"
        boolean appoint_method "予約方法"
        datetime start_at "予約開始日時"
        datetime end_at "予約終了日時"
        datetime update_at "更新日時"
        int status "ステータス"
    }

    appointments_history {
        bigint appointment_id PK "履歴ID (自動採番)"
        bigint dental_id FK "歯科医院ID"
        bigint chair_id FK "チェアID"
        bigint patient_id FK "患者ID (任意)"
        bigint token_id FK "トークンID (任意)"
        bigint treatment_id FK "診療メニューID"
        boolean appoint_method "予約方法"
        datetime start_at "予約開始日時"
        datetime end_at "予約終了日時"
        int status "ステータス"
        datetime archive_at "アーカイブ日時"
    }

    news {
        bigint news_id PK "ニュースID"
        varchar title "タイトル"
        text content "本文"
        datetime publish_at "公開日時"
    }
```

---

## 2. テーブル定義詳細

### 2.1. `roles` (ロール定義)
システム全体の権限を管理するマスターテーブルです。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `role_id` | `INT AUTO_INCREMENT` **[PK]** | ロールID |
| `role_name` | `VARCHAR(10) UNIQUE NOT NULL` | 権限名 (`ROLE_PATIENT`, `ROLE_CLINIC`, `ROLE_ADMIN`) |

### 2.2. `admins` (管理者情報)
マスタシステムを管理する管理者情報です。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `admin_id` | `INT AUTO_INCREMENT` **[PK]** | 管理者ID |
| `login_id` | `VARCHAR(50) UNIQUE NOT NULL` | ログイン用ID |
| `password` | `VARCHAR(255) NOT NULL` | ハッシュ化されたパスワード |
| `name` | `VARCHAR(20) NOT NULL` | 管理者表示名 |
| `role_id` | `INT NOT NULL` **[FK -> roles.role_id]** | ロールID |

### 2.3. `dental_clinic` (歯科医院情報)
サービスを利用する歯科医院の情報です。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `dental_id` | `BIGINT AUTO_INCREMENT` **[PK]** | 歯科医院ID |
| `login_id` | `VARCHAR(50) UNIQUE NOT NULL` | ログイン用ID |
| `password` | `VARCHAR(255) NOT NULL` | ハッシュ化されたパスワード |
| `name` | `VARCHAR(50) NOT NULL` | 歯科医院名 |
| `address` | `VARCHAR(255)` | 住所 |
| `tel` | `VARCHAR(20)` | 電話番号 |
| `mail` | `VARCHAR(255)` | メールアドレス |
| `contract_status` | `VARCHAR(10) NOT NULL` | 契約ステータス (`ACTIVE`, `INACTIVE` 等) |
| `max_reserve_month` | `INT` | 最大予約可能月数 |
| `reservation_restrictions` | `BOOLEAN NOT NULL DEFAULT FALSE` | 予約制限有無 |
| `role_id` | `INT NOT NULL` **[FK -> roles.role_id]** | ロールID |
| `public_url_token` | `VARCHAR(255) UNIQUE NOT NULL` | 公開用URL用のユニークトークン |

### 2.4. `business_hours` (営業時間設定)
各歯科医院の曜日ごとの営業時間設定です。

> [!NOTE]
> `dental_id` と `day_of_week` の組み合わせにユニーク制約（複合ユニーク）が設定されています。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `business_id` | `BIGINT AUTO_INCREMENT` **[PK]** | 営業時間設定ID |
| `dental_id` | `BIGINT NOT NULL` **[FK -> dental_clinic.dental_id]** | 歯科医院ID |
| `day_of_week` | `VARCHAR(255) NOT NULL` | 曜日 (Java標準の `DayOfWeek` 文字列) |
| `open_at` | `TIME NOT NULL` | 開院時刻 |
| `close_at` | `TIME NOT NULL` | 閉院時刻 |
| `break_start_at` | `TIME` | 休憩開始時刻 |
| `break_end_at` | `TIME` | 休憩終了時刻 |
| `regular_holiday` | `BOOLEAN NOT NULL DEFAULT FALSE` | 定休日フラグ |

### 2.5. `dental_chair` (診療用チェア情報)
各歯科医院が持つ診療用チェア（ユニット）情報です。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `chair_id` | `BIGINT AUTO_INCREMENT` **[PK]** | チェアID |
| `dental_id` | `BIGINT NOT NULL` **[FK -> dental_clinic.dental_id]** | 歯科医院ID |
| `chair_name` | `VARCHAR(20) NOT NULL` | チェア表示名 |
| `status` | `BOOLEAN NOT NULL DEFAULT TRUE` | 稼働ステータス (TRUE:稼働、FALSE:非稼働) |

### 2.6. `treatment_type` (診療メニュー)
各歯科医院が提供する診療メニュー（虫歯治療、クリーニング等）です。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `treatment_id` | `BIGINT AUTO_INCREMENT` **[PK]** | 診療メニューID |
| `dental_id` | `BIGINT NOT NULL` **[FK -> dental_clinic.dental_id]** | 歯科医院ID |
| `treatment_name` | `VARCHAR(20) NOT NULL` | メニュー名 |
| `required_minutes` | `INT NOT NULL` | 所要時間 (分単位) |
| `status` | `BOOLEAN NOT NULL DEFAULT TRUE` | 公開ステータス (TRUE:公開、FALSE:非公開) |
| `is_existing_only` | `BOOLEAN NOT NULL DEFAULT TRUE` | 制限対象 (TRUE:全員、FALSE:既存患者限定) |

### 2.7. `chair_treatment` (チェア別対応メニュー)
チェアがどの診療メニューに対応しているかを紐付ける中間テーブルです。

> [!NOTE]
> `chair_id` と `treatment_id` の組み合わせにユニーク制約（複合ユニーク）が設定されています。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `ct_id` | `BIGINT AUTO_INCREMENT` **[PK]** | チェア別対応メニューID |
| `chair_id` | `BIGINT NOT NULL` **[FK -> dental_chair.chair_id]** | チェアID |
| `treatment_id` | `BIGINT NOT NULL` **[FK -> treatment_type.treatment_id]** | 診療メニューID |

### 2.8. `calendar_exception` (カレンダー例外設定)
祝日や特定日の休診・チェア制限情報を管理します。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `calendar_id` | `BIGINT AUTO_INCREMENT` **[PK]** | 例外設定ID |
| `dental_id` | `BIGINT NOT NULL` **[FK -> dental_clinic.dental_id]** | 歯科医院ID |
| `target_date` | `DATE NOT NULL` | 設定対象日 |
| `is_holiday` | `BOOLEAN NOT NULL DEFAULT FALSE` | 終日休診フラグ |
| `chair_id` | `BIGINT` **[FK -> dental_chair.chair_id] (Null許容)** | 特定チェア制限時のチェアID |
| `start_at` | `TIME` | 制限開始時間 |
| `end_at` | `TIME` | 制限終了時間 |

### 2.9. `patients` (患者アカウント情報)
登録されている患者（既存患者）の情報です。

> [!NOTE]
> `dental_id` と `patient_code` の組み合わせにユニーク制約（複合ユニーク）が設定されています。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `patient_id` | `BIGINT AUTO_INCREMENT` **[PK]** | 患者ID |
| `dental_id` | `BIGINT NOT NULL` **[FK -> dental_clinic.dental_id]** | 所属歯科医院ID |
| `patient_code` | `VARCHAR(20) NOT NULL` | 診察券番号・患者コード |
| `name` | `VARCHAR(50) NOT NULL` | 氏名 |
| `birthday` | `DATE NOT NULL` | 生年月日 |
| `tel` | `VARCHAR(20)` | 電話番号 |
| `email` | `VARCHAR(255)` | メールアドレス |
| `status` | `VARCHAR(10) NOT NULL` | アカウントステータス |
| `role_id` | `INT NOT NULL` **[FK -> roles.role_id]** | ロールID |

### 2.10. `token` (仮患者予約用ワンタイムトークン)
未登録の新規患者が一時的に予約を行うためのトークン情報です。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `token_id` | `BIGINT AUTO_INCREMENT` **[PK]** | トークンID |
| `token_value` | `VARCHAR(255) UNIQUE NOT NULL` | トークンハッシュ値 |
| `dental_id` | `BIGINT NOT NULL` | 対象の歯科医院ID (論理キー) |
| `name` | `VARCHAR(20) NOT NULL` | 氏名 |
| `birthday` | `DATE NOT NULL` | 生年月日 |
| `tel` | `VARCHAR(20) NOT NULL` | 電話番号 |
| `email` | `VARCHAR(255) NOT NULL` | メールアドレス |
| `expiry_time` | `DATETIME NOT NULL` | 有効期限 |

### 2.11. `appointments` (予約情報)
現在スケジュールされている予約のデータです。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `appointment_id` | `BIGINT AUTO_INCREMENT` **[PK]** | 予約ID |
| `dental_id` | `BIGINT NOT NULL` **[FK -> dental_clinic.dental_id]** | 歯科医院ID |
| `chair_id` | `BIGINT NOT NULL` **[FK -> dental_chair.chair_id]** | 使用するチェアID |
| `patient_id` | `BIGINT` **[FK -> patients.patient_id] (Null許容)** | 患者ID (本会員予約の場合) |
| `token_id` | `BIGINT` **[FK -> token.token_id] (Null許容)** | 仮会員トークンID (新規予約の場合) |
| `treatment_id` | `BIGINT NOT NULL` **[FK -> treatment_type.treatment_id]** | 診療メニューID |
| `appoint_method` | `BOOLEAN NOT NULL` | 予約方法 (TRUE:オンライン, FALSE:オフライン) |
| `start_at` | `DATETIME NOT NULL` | 予約開始時間 |
| `end_at` | `DATETIME NOT NULL` | 予約終了時間 |
| `update_at` | `DATETIME NOT NULL` | 更新日時 |
| `status` | `INT NOT NULL` | 予約ステータス (Enum Ordinal) |

### 2.12. `appointments_history` (予約履歴)
過去の予約やキャンセルされた予約のアーカイブです。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `appointment_id` | `BIGINT AUTO_INCREMENT` **[PK]** | 履歴レコード固有のID |
| `dental_id` | `BIGINT NOT NULL` **[FK -> dental_clinic.dental_id]** | 歯科医院ID |
| `chair_id` | `BIGINT NOT NULL` **[FK -> dental_chair.chair_id]** | チェアID |
| `patient_id` | `BIGINT` **[FK -> patients.patient_id] (Null許容)** | 患者ID (本会員予約の場合) |
| `token_id` | `BIGINT` **[FK -> token.token_id] (Null許容)** | 仮会員トークンID (新規予約の場合) |
| `treatment_id` | `BIGINT NOT NULL` **[FK -> treatment_type.treatment_id]** | 診療メニューID |
| `appoint_method` | `BOOLEAN NOT NULL` | 予約方法 (TRUE:オンライン, FALSE:オフライン) |
| `start_at` | `DATETIME NOT NULL` | 予約開始時間 |
| `end_at` | `DATETIME NOT NULL` | 予約終了時間 |
| `status` | `INT NOT NULL` | 予約ステータス (Enum Ordinal) |
| `archive_at` | `DATETIME NOT NULL` | アーカイブ登録された日時 |

### 2.13. `news` (お知らせ情報)
各医院または全体の公開ニュース情報です。

| カラム物理名 | 型・制約 | 説明 |
| :--- | :--- | :--- |
| `news_id` | `BIGINT AUTO_INCREMENT` **[PK]** | ニュースID |
| `title` | `VARCHAR(20) NOT NULL` | お知らせタイトル |
| `content` | `TEXT NOT NULL` | お知らせ本文 |
| `publish_at` | `DATETIME NOT NULL` | 公開日時 |
