# 📘 BrightTech Blog

Spring Boot 4.x + Thymeleaf + PostgreSQL で作る、学習用の **テックブログシステム** です。
公開ブログの閲覧・カテゴリー別管理・管理者によるブログCRUDを備えています。

---

## 📑 目次

1. [システム概要](#1-システム概要)
2. [技術スタック](#2-技術スタック)
3. [作り方を学ぶには（learn フォルダ）](#3-作り方を学ぶには-learn-フォルダ)
4. [セットアップ・起動方法](#4-セットアップ起動方法)
5. [テーブル定義書](#5-テーブル定義書)
6. [ER図](#6-er図)
7. [フォルダ階層図](#7-フォルダ階層図)
8. [URLマッピング](#8-urlマッピング)
9. [シーケンス図（機能ごと）](#9-シーケンス図機能ごと)
10. [アクティビティ図（機能ごと）](#10-アクティビティ図機能ごと)

---

## 1. システム概要

**BrightTech Blog** は、フロントエンド・バックエンド・AI/ML・デザインなどテックカテゴリーのブログを発信・管理するための Web アプリです。

### 主な機能

| 区分           | 機能                                                                 |
| -------------- | -------------------------------------------------------------------- |
| 公開ページ     | ブログ一覧（ページング）／ブログ詳細                                 |
| 管理者認証     | 新規登録（サインアップ）／ログイン／ログアウト／プロフィール編集     |
| ブログ管理     | 新規登録／編集／削除／一覧表示                                       |
| カテゴリー管理 | 新規登録／編集／削除（使用中チェック付き）／一覧表示                 |

### 想定ユーザー

- **一般訪問者**：ログイン不要でブログを閲覧
- **管理者**：ログイン後、ブログ／カテゴリー／自身のプロフィールを管理

---

## 2. 技術スタック

### バックエンド

| カテゴリー       | 技術                       | バージョン         |
| ---------------- | -------------------------- | ------------------ |
| 言語             | Java                       | 17                 |
| フレームワーク   | Spring Boot                | 4.0.6              |
| Web              | Spring MVC                 | （Boot 同梱）      |
| データアクセス   | Spring Data JPA / Hibernate| （Boot 同梱）      |
| 認証・認可       | Spring Security 6.x        | （Boot 同梱）      |
| バリデーション   | Jakarta Bean Validation    | （Boot 同梱）      |
| 補助ライブラリ   | Lombok                     | 最新               |
| DB ドライバ      | PostgreSQL JDBC            | 最新               |

### フロントエンド（サーバーサイドレンダリング）

| カテゴリー       | 技術                                | 備考              |
| ---------------- | ----------------------------------- | ----------------- |
| テンプレート     | Thymeleaf                           | HTML5 ベース      |
| Security 連携    | thymeleaf-extras-springsecurity6    | CSRF / 認可表示   |
| CSS              | Tailwind CSS（CDN）                 | 学習用に CDN 経由 |

### データベース

| カテゴリー | 技術       | バージョン |
| ---------- | ---------- | ---------- |
| RDBMS      | PostgreSQL | 14+ 推奨   |

### ビルド・開発ツール

| カテゴリー    | 技術                | 備考                  |
| ------------- | ------------------- | --------------------- |
| ビルド        | Maven (mvnw)        | wrapper 同梱          |
| IDE 推奨      | Spring Tool Suite / IntelliJ IDEA / VS Code | Lombok プラグイン推奨 |

---

## 3. 作り方を学ぶには（learn フォルダ）

> 🎓 **本リポジトリは「学習用」です**。
> ソースを真似て書くだけでなく、**`learn/` フォルダの中の章立て資料** を読むことで、
> 「**なぜその順番で書くのか・各層の役割は何か・どこでよくつまづくのか**」を理解できます。

### learn フォルダの構成

```text
learn/
├── 第1章_Category_Entityを作る.md          ← 一番依存が薄い Entity から
├── 第2章_Blog_Entityを作る.md              ← @ManyToOne で関連を表す
├── 第3章_User_Entityを作る.md              ← パスワードの扱い／@Table の必要性
├── 第4章_Repositoryを作る.md               ← JpaRepository とメソッド名規約／ページング
├── 第5章_Formを作る.md                     ← 入力チェック ＋ Thymeleaf テンプレート同時作成
├── 第6章_Serviceを作る.md                  ← 業務ロジックの中心・トランザクション
├── 第7章_1_Controllerを作る.md             ← @GetMapping / @PostMapping / @Valid / PRG
└── 第7章_2_テンプレートとSecurityConfig.md ← 残りのテンプレート・認証・DB接続
```

### 各章で学べること

| 章 | 学べる主なテーマ                                                              |
| -: | ----------------------------------------------------------------------------- |
| 1  | `@Entity` / `@Table` / `@Id` / `@GeneratedValue` / `@Column` / `@PrePersist`  |
| 2  | `@ManyToOne` / `@JoinColumn` / `FetchType.LAZY` / N+1問題 / `TEXT` 型         |
| 3  | クラス名 ⇔ テーブル名のズレ／`UNIQUE` 制約／パスワードを生で保存しない原則    |
| 4  | `JpaRepository<E, ID>` / `findByXxx` メソッド名規約 / `Optional` / `Page`     |
| 5  | `@NotBlank` / `@NotNull` / `@Email` の使い分け／Entity ↔ Form の分離         |
| 6  | `@Service` / `@Transactional` / コンストラクタインジェクション／業務エラー   |
| 7-1| `@Controller` / `@Valid + BindingResult` / **PRG パターン**／フラッシュメッセージ |
| 7-2| `SecurityFilterChain` / email ログイン / CSRF / Lazy 例外対処                 |

### 推奨学習順序

```text
1章 → 2章 → 3章 → 4章 → 5章 → 6章 → 7-1章 → 7-2章
```

**「依存される側から先に作る」** が貫かれているので、上から順に読めば
コンパイルエラーやインポート不足で詰まりません。

---

## 4. セットアップ・起動方法

### 4-1. 前提環境

- Java 17 以上
- PostgreSQL 14 以上（ローカル or Docker）
- Maven（同梱の `mvnw` でOK）

### 4-2. データベース準備

```sql
CREATE DATABASE tech_blogs;
```

Docker で立てる例：

```bash
docker run -d --name pg \
  -e POSTGRES_PASSWORD=123456 \
  -p 5433:5432 \
  postgres:16
docker exec -it pg psql -U postgres -c "CREATE DATABASE tech_blogs;"
```

### 4-3. 接続情報を確認

`src/main/resources/application.properties` を環境に合わせて修正：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/tech_blogs
spring.datasource.username=postgres
spring.datasource.password=123456
```

### 4-4. アプリ起動

```bash
# Windows
mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

### 4-5. ブラウザでアクセス

| URL                                       | 内容                                |
| ----------------------------------------- | ----------------------------------- |
| http://localhost:8080/                    | リダイレクト → ブログ一覧            |
| http://localhost:8080/admin/register      | 管理者を新規登録（最初はここから）   |
| http://localhost:8080/login               | ログイン                            |
| http://localhost:8080/admin/blogs        | 管理画面（要ログイン）               |

---

## 5. テーブル定義書

### 5-1. テーブル一覧

| No | 論理名             | 物理名         | 用途                                       |
| --:| ------------------ | -------------- | ------------------------------------------ |
|  1 | 管理者テーブル     | `admin_users`  | 管理者ログイン・プロフィール管理           |
|  2 | カテゴリーテーブル | `categories`   | ブログのカテゴリー分類                     |
|  3 | ブログテーブル     | `blogs`        | ブログ記事（タイトル／本文／画像／公開日） |

### 5-2. `admin_users`（管理者）

| カラム名     | 型           | 桁数 | NOT NULL | PK | UNIQUE | 既定値              | 説明                              |
| ------------ | ------------ | ---:| :------: | :-:| :----: | ------------------- | --------------------------------- |
| `id`         | BIGSERIAL    |  -  |    ●     | ● |   -    | 自動採番            | 管理者ID                          |
| `name`       | VARCHAR      | 100 |    ●     | -  |   -    | -                   | 管理者の表示名                    |
| `email`      | VARCHAR      | 255 |    ●     | -  |   ●    | -                   | ログインID                        |
| `password`   | VARCHAR      | 255 |    ●     | -  |   -    | -                   | BCrypt ハッシュ済みパスワード     |
| `created_at` | TIMESTAMP    |  -  |    ●     | -  |   -    | CURRENT_TIMESTAMP   | 作成日時（自動）                  |
| `updated_at` | TIMESTAMP    |  -  |    ●     | -  |   -    | CURRENT_TIMESTAMP   | 更新日時（自動）                  |

### 5-3. `categories`（カテゴリー）

| カラム名     | 型           | 桁数 | NOT NULL | PK | UNIQUE | 既定値              | 説明                  |
| ------------ | ------------ | ---:| :------: | :-:| :----: | ------------------- | --------------------- |
| `id`         | BIGSERIAL    |  -  |    ●     | ● |   -    | 自動採番            | カテゴリーID          |
| `name`       | VARCHAR      | 100 |    ●     | -  |   ●    | -                   | カテゴリー名          |
| `created_at` | TIMESTAMP    |  -  |    ●     | -  |   -    | CURRENT_TIMESTAMP   | 作成日時              |
| `updated_at` | TIMESTAMP    |  -  |    ●     | -  |   -    | CURRENT_TIMESTAMP   | 更新日時              |

### 5-4. `blogs`（ブログ記事）

| カラム名       | 型         | 桁数 | NOT NULL | PK | FK              | 既定値              | 説明                            |
| -------------- | ---------- | ---:| :------: | :-:| --------------- | ------------------- | ------------------------------- |
| `id`           | BIGSERIAL  |  -  |    ●     | ● | -               | 自動採番            | ブログID                        |
| `title`        | VARCHAR    | 200 |    ●     | -  | -               | -                   | タイトル                        |
| `content`      | TEXT       |  -  |    ●     | -  | -               | -                   | 本文                            |
| `image_url`    | VARCHAR    | 500 |    -     | -  | -               | NULL                | アイキャッチ画像URL（任意）     |
| `category_id`  | BIGINT     |  -  |    ●     | -  | `categories(id)` | -                  | カテゴリー外部キー              |
| `published_at` | TIMESTAMP  |  -  |    ●     | -  | -               | CURRENT_TIMESTAMP   | 公開日時（画面で指定可能）      |
| `created_at`   | TIMESTAMP  |  -  |    ●     | -  | -               | CURRENT_TIMESTAMP   | 作成日時                        |
| `updated_at`   | TIMESTAMP  |  -  |    ●     | -  | -               | CURRENT_TIMESTAMP   | 更新日時                        |

---

## 6. ER図

```mermaid
erDiagram
    ADMIN_USERS {
        bigserial id PK
        varchar(100) name
        varchar(255) email UK
        varchar(255) password
        timestamp created_at
        timestamp updated_at
    }

    CATEGORIES {
        bigserial id PK
        varchar(100) name UK
        timestamp created_at
        timestamp updated_at
    }

    BLOGS {
        bigserial id PK
        varchar(200) title
        text content
        varchar(500) image_url
        bigint category_id FK
        timestamp published_at
        timestamp created_at
        timestamp updated_at
    }

    CATEGORIES ||--o{ BLOGS : "has many"
```

**関係性**

- `CATEGORIES (1) ─ (多) BLOGS` ：1つのカテゴリーが複数のブログを持つ
- `ADMIN_USERS` は他テーブルと FK 関係なし（独立）

---

## 7. フォルダ階層図

```text
TechBlog/
├── README.md                              ← このファイル
├── HELP.md                                ← Spring Boot 標準のヘルプ
├── pom.xml                                ← Maven 依存定義
├── mvnw, mvnw.cmd                         ← Maven Wrapper
│
├── learn/                                 ← 📘 章立て学習資料
│   ├── 第1章_Category_Entityを作る.md
│   ├── 第2章_Blog_Entityを作る.md
│   ├── 第3章_User_Entityを作る.md
│   ├── 第4章_Repositoryを作る.md
│   ├── 第5章_Formを作る.md
│   ├── 第6章_Serviceを作る.md
│   ├── 第7章_1_Controllerを作る.md
│   └── 第7章_2_テンプレートとSecurityConfig.md
│
├── src/main/java/com/meitaku/blog/
│   ├── TechBlogApplication.java           ← エントリーポイント
│   │
│   ├── config/
│   │   └── SecurityConfig.java            ← Spring Security 設定
│   │
│   ├── controller/                        ← Controller 層
│   │   ├── home/    HomeController.java
│   │   ├── blog/    BlogListController.java
│   │   │            BlogDetailController.java
│   │   │            BlogRegisterController.java
│   │   │            BlogEditController.java
│   │   │            BlogDeleteController.java
│   │   ├── auth/    LoginController.java
│   │   ├── user/    UserRegisterController.java
│   │   │            MyPageController.java
│   │   └── category/ AdminCategoryController.java
│   │
│   ├── service/                           ← Service 層（業務ロジック）
│   │   ├── CategoryService.java
│   │   ├── BlogService.java
│   │   └── UserService.java               (implements UserDetailsService)
│   │
│   ├── repository/                        ← Repository 層（DB操作）
│   │   ├── CategoryRepository.java
│   │   ├── BlogRepository.java
│   │   └── UserRepository.java
│   │
│   ├── entity/                            ← Entity 層（テーブル対応）
│   │   ├── Category.java
│   │   ├── Blog.java
│   │   └── User.java
│   │
│   ├── form/                              ← Form 層（画面入力）
│   │   ├── category/ CategoryForm.java
│   │   ├── blog/     BlogRegisterForm.java
│   │   │             BlogEditForm.java
│   │   └── user/     UserRegisterForm.java
│   │                 LoginForm.java
│   │
│   └── exception/
│       └── ResourceNotFoundException.java ← 404 用カスタム例外
│
└── src/main/resources/
    ├── application.properties             ← DB接続 / JPA / サーバー設定
    │
    └── templates/                         ← Thymeleaf テンプレート
        ├── fragments/layout.html          ← head / flash / globalErrors / fieldError
        ├── blog/
        │   ├── list.html                  ← 公開ブログ一覧
        │   └── detail.html                ← 公開ブログ詳細
        ├── auth/
        │   └── login.html                 ← ログイン
        ├── user/
        │   ├── register.html              ← 管理者新規登録
        │   └── profile.html               ← マイページ
        └── admin/
            ├── blog/
            │   ├── list.html              ← 管理ブログ一覧
            │   └── form.html              ← ブログ登録／編集
            └── category/
                ├── list.html              ← 管理カテゴリー一覧
                └── form.html              ← カテゴリー登録／編集
```

---

## 8. URLマッピング

### 8-1. 公開ページ（認証不要）

| メソッド | URL              | Controller              | メソッド          | 説明                       |
| -------- | ---------------- | ----------------------- | ----------------- | -------------------------- |
| GET      | `/`              | `HomeController`        | `index`           | `/blogs` へリダイレクト    |
| GET      | `/blogs`         | `BlogListController`    | `listForUser`     | 公開ブログ一覧（ページング）|
| GET      | `/blogs/{id}`    | `BlogDetailController`  | `detail`          | 公開ブログ詳細             |

### 8-2. 認証関連（認証不要）

| メソッド | URL                | Controller / 担当         | メソッド   | 説明                                  |
| -------- | ------------------ | ------------------------- | ---------- | ------------------------------------- |
| GET      | `/login`           | `LoginController`         | `showForm` | ログインフォーム表示                  |
| POST     | `/login`           | **Spring Security**       | -          | 認証処理（自作 Controller なし）      |
| POST     | `/logout`          | **Spring Security**       | -          | ログアウト処理（自作 Controller なし）|
| GET      | `/admin/register`  | `UserRegisterController`  | `showForm` | 管理者サインアップフォーム表示        |
| POST     | `/admin/register`  | `UserRegisterController`  | `register` | 管理者新規登録                        |

### 8-3. 管理画面：ブログ（`/admin/blogs/**`、認証必須）

| メソッド | URL                            | Controller                | メソッド        | 説明                  |
| -------- | ------------------------------ | ------------------------- | --------------- | --------------------- |
| GET      | `/admin/blogs`                 | `BlogListController`      | `listForAdmin`  | 管理ブログ一覧        |
| GET      | `/admin/blogs/new`             | `BlogRegisterController`  | `showForm`      | ブログ新規登録フォーム |
| POST     | `/admin/blogs`                 | `BlogRegisterController`  | `register`      | ブログ新規登録        |
| GET      | `/admin/blogs/{id}/edit`       | `BlogEditController`      | `showForm`      | ブログ編集フォーム    |
| POST     | `/admin/blogs/{id}/edit`       | `BlogEditController`      | `update`        | ブログ更新            |
| POST     | `/admin/blogs/{id}/delete`     | `BlogDeleteController`    | `delete`        | ブログ削除            |

### 8-4. 管理画面：カテゴリー（`/admin/categories/**`、認証必須）

| メソッド | URL                                    | Controller                   | メソッド            | 説明                  |
| -------- | -------------------------------------- | ---------------------------- | ------------------- | --------------------- |
| GET      | `/admin/categories`                    | `AdminCategoryController`    | `list`              | カテゴリー一覧        |
| GET      | `/admin/categories/new`                | `AdminCategoryController`    | `showRegisterForm`  | 新規登録フォーム      |
| POST     | `/admin/categories`                    | `AdminCategoryController`    | `register`          | 新規登録              |
| GET      | `/admin/categories/{id}/edit`          | `AdminCategoryController`    | `showEditForm`      | 編集フォーム          |
| POST     | `/admin/categories/{id}/edit`          | `AdminCategoryController`    | `update`            | 更新                  |
| POST     | `/admin/categories/{id}/delete`        | `AdminCategoryController`    | `delete`            | 削除（使用中チェック）|

### 8-5. 管理画面：プロフィール（認証必須）

| メソッド | URL              | Controller         | メソッド   | 説明                      |
| -------- | ---------------- | ------------------ | ---------- | ------------------------- |
| GET      | `/admin/profile` | `MyPageController` | `showForm` | 自身のプロフィール表示    |
| POST     | `/admin/profile` | `MyPageController` | `update`   | 自身のプロフィール更新    |

---

## 9. シーケンス図（機能ごと）

### 9-1. 公開ブログ閲覧（一覧）

```mermaid
sequenceDiagram
    actor User as 訪問者
    participant Browser as ブラウザ
    participant Ctrl as BlogListController
    participant Svc as BlogService
    participant Repo as BlogRepository
    participant DB as PostgreSQL
    participant View as blog/list.html

    User->>Browser: URL: /blogs
    Browser->>Ctrl: GET /blogs?page=0
    Ctrl->>Svc: findLatest(pageable)
    Svc->>Repo: findAllByOrderByPublishedAtDesc(pageable)
    Repo->>DB: SELECT * FROM blogs ORDER BY published_at DESC LIMIT 9 OFFSET 0
    DB-->>Repo: 行データ
    Repo-->>Svc: Page<Blog>
    Svc-->>Ctrl: Page<Blog>
    Ctrl->>View: model.addAttribute("page", page)
    View-->>Browser: HTML（ブログカード一覧）
    Browser-->>User: 描画
```

### 9-2. 公開ブログ詳細

```mermaid
sequenceDiagram
    actor User as 訪問者
    participant Browser as ブラウザ
    participant Ctrl as BlogDetailController
    participant Svc as BlogService
    participant Repo as BlogRepository
    participant DB as PostgreSQL
    participant View as blog/detail.html

    User->>Browser: ブログカードをクリック
    Browser->>Ctrl: GET /blogs/{id}
    Ctrl->>Svc: findById(id)
    Svc->>Repo: findById(id)
    Repo->>DB: SELECT * FROM blogs WHERE id=?
    alt 見つかった場合
        DB-->>Repo: 1行
        Repo-->>Svc: Optional<Blog>
        Svc-->>Ctrl: Blog
        Ctrl->>View: model.addAttribute("blog", blog)
        View-->>Browser: HTML（詳細）
    else 見つからない場合
        DB-->>Repo: 0行
        Repo-->>Svc: Optional.empty()
        Svc-->>Svc: throw ResourceNotFoundException
        Svc-->>Browser: HTTP 404
    end
```

### 9-3. 管理者ログイン

```mermaid
sequenceDiagram
    actor User as 管理者
    participant Browser as ブラウザ
    participant Sec as SpringSecurityFilter
    participant UserSvc as UserService（UserDetailsService）
    participant Repo as UserRepository
    participant Encoder as PasswordEncoder
    participant DB as PostgreSQL

    User->>Browser: email + password 入力
    Browser->>Sec: POST /login (email, password, _csrf)
    Sec->>UserSvc: loadUserByUsername(email)
    UserSvc->>Repo: findByEmail(email)
    Repo->>DB: SELECT * FROM admin_users WHERE email=?
    DB-->>Repo: User行
    Repo-->>UserSvc: Optional<User>
    UserSvc-->>Sec: UserDetails（emailと hashed password）
    Sec->>Encoder: matches(rawPassword, hashedPassword)
    Encoder-->>Sec: true / false
    alt 認証成功
        Sec-->>Browser: 302 Redirect → /admin/blogs
    else 認証失敗
        Sec-->>Browser: 302 Redirect → /login?error
    end
```

### 9-4. ブログ新規登録

```mermaid
sequenceDiagram
    actor User as 管理者
    participant Browser as ブラウザ
    participant Ctrl as BlogRegisterController
    participant Svc as BlogService
    participant CatRepo as CategoryRepository
    participant Repo as BlogRepository
    participant DB as PostgreSQL

    User->>Browser: ブログ登録ボタン
    Browser->>Ctrl: GET /admin/blogs/new
    Ctrl-->>Browser: form.html（空のBlogRegisterForm）

    User->>Browser: 入力 → 「登録する」
    Browser->>Ctrl: POST /admin/blogs (title, content, categoryId, imageUrl, _csrf)

    Ctrl->>Ctrl: @Valid 実行（@NotBlank/@Size/@NotNull）

    alt バリデーションエラーあり
        Ctrl-->>Browser: form.html 再描画（エラー付き）
    else バリデーションOK
        Ctrl->>Svc: register(form)
        Svc->>CatRepo: findById(categoryId)
        CatRepo->>DB: SELECT * FROM categories WHERE id=?
        DB-->>CatRepo: 行
        CatRepo-->>Svc: Optional<Category>
        Svc->>Svc: Form → Blog Entity 詰め替え
        Svc->>Repo: save(blog)
        Repo->>DB: INSERT INTO blogs ...
        DB-->>Repo: 採番された id
        Repo-->>Svc: 保存済み Blog
        Svc-->>Ctrl: Blog
        Ctrl->>Ctrl: addFlashAttribute("message", "ブログを登録しました")
        Ctrl-->>Browser: 302 Redirect → /admin/blogs
        Browser->>Ctrl: GET /admin/blogs
        Ctrl-->>Browser: 一覧画面（成功バナー付き）
    end
```

### 9-5. ブログ削除

```mermaid
sequenceDiagram
    actor User as 管理者
    participant Browser as ブラウザ
    participant Ctrl as BlogDeleteController
    participant Svc as BlogService
    participant Repo as BlogRepository
    participant DB as PostgreSQL

    User->>Browser: 「削除」ボタン
    Browser->>Browser: confirm("本当に削除しますか？")
    User->>Browser: OK

    Browser->>Ctrl: POST /admin/blogs/{id}/delete (_csrf)
    Ctrl->>Svc: delete(id)
    Svc->>Repo: findById(id)
    Repo->>DB: SELECT
    DB-->>Repo: 1行
    Repo-->>Svc: Optional<Blog>
    Svc->>Repo: delete(blog)
    Repo->>DB: DELETE FROM blogs WHERE id=?
    Svc-->>Ctrl: void
    Ctrl->>Ctrl: addFlashAttribute("message", "ブログを削除しました")
    Ctrl-->>Browser: 302 Redirect → /admin/blogs
```

### 9-6. カテゴリー削除（使用中チェックあり）

```mermaid
sequenceDiagram
    actor User as 管理者
    participant Browser as ブラウザ
    participant Ctrl as AdminCategoryController
    participant CatSvc as CategoryService
    participant CatRepo as CategoryRepository
    participant BlogRepo as BlogRepository
    participant DB as PostgreSQL

    User->>Browser: 「削除」ボタン
    Browser->>Ctrl: POST /admin/categories/{id}/delete

    Ctrl->>CatSvc: delete(id)
    CatSvc->>CatRepo: findById(id)
    CatRepo->>DB: SELECT
    DB-->>CatRepo: Category
    CatRepo-->>CatSvc: Optional<Category>

    CatSvc->>BlogRepo: countByCategory(category)
    BlogRepo->>DB: SELECT count(*) FROM blogs WHERE category_id=?
    DB-->>BlogRepo: 件数
    BlogRepo-->>CatSvc: long

    alt 件数 > 0 （使用中）
        CatSvc-->>Ctrl: throw IllegalStateException
        Ctrl->>Ctrl: addFlashAttribute("errorMessage", "使われているため削除できません")
        Ctrl-->>Browser: 302 Redirect（赤バナー）
    else 件数 == 0
        CatSvc->>CatRepo: delete(category)
        CatRepo->>DB: DELETE FROM categories
        CatSvc-->>Ctrl: void
        Ctrl->>Ctrl: addFlashAttribute("message", "カテゴリーを削除しました")
        Ctrl-->>Browser: 302 Redirect（緑バナー）
    end
```

### 9-7. 管理者新規登録（サインアップ）

```mermaid
sequenceDiagram
    actor User as 新規管理者
    participant Browser as ブラウザ
    participant Ctrl as UserRegisterController
    participant Svc as UserService
    participant Encoder as PasswordEncoder
    participant Repo as UserRepository
    participant DB as PostgreSQL

    User->>Browser: GET /admin/register
    Browser->>Ctrl: GET /admin/register
    Ctrl-->>Browser: register.html（空フォーム）

    User->>Browser: 名前/email/パスワード 入力
    Browser->>Ctrl: POST /admin/register

    Ctrl->>Ctrl: @Valid 実行
    alt バリデーションNG
        Ctrl-->>Browser: register.html 再描画
    else バリデーションOK
        Ctrl->>Svc: register(form)
        Svc->>Repo: existsByEmail(email)
        Repo->>DB: SELECT count(*) FROM admin_users WHERE email=?
        DB-->>Repo: 0 or 1
        alt 既に存在
            Repo-->>Svc: true
            Svc-->>Ctrl: throw IllegalArgumentException
            Ctrl->>Ctrl: bindingResult.reject
            Ctrl-->>Browser: register.html 再描画（エラー）
        else 新規
            Repo-->>Svc: false
            Svc->>Encoder: encode(rawPassword)
            Encoder-->>Svc: BCryptハッシュ文字列
            Svc->>Repo: save(user with hashedPassword)
            Repo->>DB: INSERT INTO admin_users
            DB-->>Repo: 採番id
            Svc-->>Ctrl: User
            Ctrl->>Ctrl: addFlashAttribute("message", "登録しました")
            Ctrl-->>Browser: 302 Redirect → /login
        end
    end
```

### 9-8. プロフィール更新

```mermaid
sequenceDiagram
    actor User as ログイン中管理者
    participant Browser as ブラウザ
    participant Sec as SpringSecurity
    participant Ctrl as MyPageController
    participant Svc as UserService
    participant Encoder as PasswordEncoder
    participant Repo as UserRepository
    participant DB as PostgreSQL

    User->>Browser: プロフィール画面
    Browser->>Sec: GET /admin/profile（セッションCookie）
    Sec->>Ctrl: @AuthenticationPrincipal でログイン情報を注入
    Ctrl->>Svc: findByEmail(principal.username)
    Svc->>Repo: findByEmail
    Repo->>DB: SELECT
    DB-->>Repo: User
    Svc-->>Ctrl: User → UserRegisterForm
    Ctrl-->>Browser: profile.html（現在値）

    User->>Browser: 編集して「保存」
    Browser->>Ctrl: POST /admin/profile

    Ctrl->>Ctrl: @AuthenticationPrincipal で本人IDを取得（URL からは取らない）
    Ctrl->>Ctrl: @Valid
    Ctrl->>Svc: update(currentUser.id, form)
    Svc->>Encoder: encode(newPassword)
    Encoder-->>Svc: ハッシュ
    Svc->>Repo: save(user)
    Repo->>DB: UPDATE admin_users
    DB-->>Repo: OK
    Ctrl->>Ctrl: addFlashAttribute("message", "更新しました")
    Ctrl-->>Browser: 302 Redirect → /admin/profile
```

---

## 10. アクティビティ図（機能ごと）

### 10-1. 公開ブログ閲覧（一覧）

```mermaid
flowchart TD
    Start([開始]) --> A[GET /blogs にアクセス]
    A --> B[Pageable をクエリから受け取る<br/>page=N&size=9]
    B --> C[blogService.findLatest pageable]
    C --> D[SELECT ORDER BY published_at DESC LIMIT/OFFSET]
    D --> E{件数あり?}
    E -- あり --> F[Page&lt;Blog&gt; を model に追加]
    E -- なし --> G[空の Page を model に追加]
    F --> H[blog/list.html を返す]
    G --> H
    H --> I[ブラウザでカード一覧 + ページャー描画]
    I --> End([終了])
```

### 10-2. ブログ詳細

```mermaid
flowchart TD
    Start([開始]) --> A[GET /blogs/id にアクセス]
    A --> B[blogService.findById id]
    B --> C{1件取得できた?}
    C -- Yes --> D[blog を model に追加]
    C -- No --> E[ResourceNotFoundException → 404画面]
    D --> F[blog/detail.html を返す]
    F --> G[ブラウザで詳細描画]
    G --> End([終了])
    E --> End
```

### 10-3. 管理者ログイン

```mermaid
flowchart TD
    Start([開始]) --> A["GET /login で画面表示"]
    A --> B["email と password を入力 → POST /login"]
    B --> C["Spring Security が受信"]
    C --> D["UserService.loadUserByUsername(email)"]
    D --> E{email が存在?}
    E -- No --> F["/login?error にリダイレクト"]
    E -- Yes --> G["PasswordEncoder.matches で照合"]
    G --> H{一致?}
    H -- No --> F
    H -- Yes --> I["セッション開始 → /admin/blogs にリダイレクト"]
    F --> Goal([終了])
    I --> Goal
```

### 10-4. ブログ新規登録

```mermaid
flowchart TD
    Start([開始]) --> A["GET /admin/blogs/new で空フォーム表示"]
    A --> B["入力して POST /admin/blogs"]
    B --> C["@Valid でバリデーション"]
    C --> D{エラーあり?}
    D -- Yes --> E["再描画 (categories と isEdit=false を再設定)"]
    E --> Goal1([終了])
    D -- No --> F["BlogService.register(form)"]
    F --> G["categoryId を Category Entity に解決"]
    G --> H{カテゴリー存在?}
    H -- No --> I["ResourceNotFoundException"]
    I --> Goal1
    H -- Yes --> J["Form を Blog Entity に詰め替え"]
    J --> K["blogRepository.save で INSERT"]
    K --> L["addFlashAttribute(message)"]
    L --> M["Redirect → /admin/blogs"]
    M --> Goal([終了])
```

### 10-5. ブログ編集

```mermaid
flowchart TD
    Start([開始]) --> A["GET /admin/blogs/{id}/edit"]
    A --> B["blogService.toEditForm(id) で DB を BlogEditForm に詰め替え"]
    B --> C["フォーム表示 (isEdit=true)"]
    C --> D["編集して POST /admin/blogs/{id}/edit"]
    D --> E["@Valid 実行"]
    E --> F{エラーあり?}
    F -- Yes --> G["再描画"]
    G --> Goal1([終了])
    F -- No --> H["blogService.update(form)"]
    H --> I["既存Blogを取得し値を上書きして save (UPDATE)"]
    I --> J["addFlashAttribute(message)"]
    J --> K["Redirect → /admin/blogs"]
    K --> Goal([終了])
```

### 10-6. ブログ削除

```mermaid
flowchart TD
    Start([開始]) --> A[一覧で「削除」ボタンクリック]
    A --> B[ブラウザで confirm 表示]
    B --> C{OKか?}
    C -- Cancel --> End1([終了])
    C -- OK --> D[POST /admin/blogs/id/delete + _csrf]
    D --> E[blogService.delete id]
    E --> F{Blog存在?}
    F -- No --> G[ResourceNotFoundException → 404]
    G --> End1
    F -- Yes --> H[blogRepository.delete → DELETE]
    H --> I[addFlashAttribute message]
    I --> J[Redirect → /admin/blogs]
    J --> End([終了])
```

### 10-7. カテゴリー削除（使用中チェック）

```mermaid
flowchart TD
    Start([開始]) --> A[「削除」ボタンクリック → POST]
    A --> B[categoryService.delete id]
    B --> C[対象 Category を取得]
    C --> D[blogRepository.countByCategory category]
    D --> E{使用件数 > 0?}
    E -- Yes --> F[IllegalStateException スロー]
    F --> G[Controller で catch]
    G --> H[addFlashAttribute errorMessage]
    H --> J[Redirect → 一覧 赤バナー]
    E -- No --> K[categoryRepository.delete → DELETE]
    K --> L[addFlashAttribute message]
    L --> J
    J --> End([終了])
```

### 10-8. 管理者新規登録

```mermaid
flowchart TD
    Start([開始]) --> A["GET /admin/register で空フォーム"]
    A --> B["入力 → POST /admin/register"]
    B --> C["@Valid 実行"]
    C --> D{エラーあり?}
    D -- Yes --> E["再描画"]
    E --> Goal1([終了])
    D -- No --> F["userService.register(form)"]
    F --> G["existsByEmail でチェック"]
    G --> H{既に存在?}
    H -- Yes --> I["IllegalArgumentException"]
    I --> J["Controller で catch → bindingResult.reject"]
    J --> E
    H -- No --> K["passwordEncoder.encode で BCryptハッシュ化"]
    K --> L["User Entity 作成 → save (INSERT)"]
    L --> M["addFlashAttribute(message)"]
    M --> N["Redirect → /login"]
    N --> Goal([終了])
```

### 10-9. プロフィール更新

```mermaid
flowchart TD
    Start([開始]) --> A["GET /admin/profile"]
    A --> B["@AuthenticationPrincipal で本人取得 (URLからIDは取らない)"]
    B --> C["userService.findByEmail(principal.username)"]
    C --> D["User を UserRegisterForm に詰め替え"]
    D --> E["profile.html 表示"]
    E --> F["編集 → POST /admin/profile"]
    F --> G["@Valid 実行"]
    G --> H{エラーあり?}
    H -- Yes --> I["再描画"]
    I --> Goal1([終了])
    H -- No --> J["userService.update(currentUser.id, form)"]
    J --> K{emailを変更?}
    K -- Yes --> L["existsByEmail で重複チェック"]
    L --> M{重複?}
    M -- Yes --> N["IllegalArgumentException"]
    N --> O["Controller で catch → 再描画"]
    O --> Goal1
    M -- No --> P["name と email を上書き"]
    K -- No --> P
    P --> Q["passwordEncoder.encode で新パスワードをハッシュ化"]
    Q --> R["save (UPDATE)"]
    R --> S["addFlashAttribute(message)"]
    S --> T["Redirect → /admin/profile"]
    T --> Goal([終了])
```

---

## 🎉 おわりに

このシステムは **学習用** に作られており、`learn/` フォルダの章立て資料と合わせて読むことで、
**Spring Boot による典型的な3層アーキテクチャ** + **Thymeleaf** + **Spring Security** の
基本パターンを一通り体験できます。

ここから先のステップ（応用編）：

- 単体テスト・統合テスト（`@DataJpaTest` / `@WebMvcTest` / `@SpringBootTest`）
- DBマイグレーション（Flyway / Liquibase）
- 画像アップロード（`MultipartFile`）
- ロール分割（`ROLE_ADMIN` / `ROLE_EDITOR`）
- CI/CD（GitHub Actions）

お疲れさまでした 🎉
