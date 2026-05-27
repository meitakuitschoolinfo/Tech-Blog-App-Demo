# 📘 第7章（後半）

## 残りのテンプレート ＋ SecurityConfig 本格化 ＋ application.properties
― **動くアプリ** に仕上げる最終章 ―

> 📌 **更新ノート（第8章で変更あり）**
> 本章の `SecurityConfig` には **`/uploads/**` が含まれていません**。第8章で **画像アップロード対応** を追加した際、
> 公開ページからアップロード画像を表示できるよう `permitAll` のリストに `/uploads/**` を追加しています。
> あわせて `application.properties` に `spring.servlet.multipart.max-file-size` も追記。
> 詳しくは [第8章_画像アップロード対応.md](./第8章_画像アップロード対応.md) を参照してください。

> 📌 **更新ノート（第9章で変更あり）**
> 本章の `blog/detail.html` では、本文を1行ごとに見て **`##` で始まる行だけを `<h2>` 化** する簡易レンダリングを採用しています。
> 第9章で **CommonMark ライブラリ** を導入し、`#` `**bold**` `表` `---` 等を **正しくマークダウン解析→HTML描画** する本格対応に置き換えました。
> 詳しくは [第9章_マークダウン対応.md](./第9章_マークダウン対応.md) を参照してください。

---

## 🎯 7-2-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ Thymeleaf の **`th:each`** / **`th:if`** / **`th:replace`** が読める／書ける
✔ `Page<T>` から **ページャー UI** を組み立てられる
✔ `RedirectAttributes` のフラッシュを **テンプレートで表示** できる
✔ **削除ボタンを POST フォームで実装** できる（GET 削除を避ける理由が言える）
✔ `SecurityFilterChain` で **公開URL／要認証URL** を振り分けられる
✔ **`usernameParameter("email")`** で email ログインに切り替えられる
✔ `application.properties` の **DB接続 / JPA / 起動ポート** を設定できる
✔ 起動時の **典型的なつまづき** を切り分けられる

---

## 🧩 7-2-2. 今回作るもの一覧

```text
templates/
├── fragments/layout.html       ← ★ フラッシュ用フラグメントを追加
├── blog/list.html              ← 公開ブログ一覧（新規作成）
├── blog/detail.html            ← 公開ブログ詳細（新規作成）
├── admin/blog/list.html        ← 管理ブログ一覧（新規作成）
├── admin/category/list.html    ← 管理カテゴリー一覧（新規作成）
└── user/profile.html           ← マイページ（新規作成）

config/
└── SecurityConfig.java         ← ★ SecurityFilterChain を本格化

resources/
└── application.properties      ← ★ DB接続 / JPA / 起動ポート
```

---

## 🔄 7-2-3. なぜテンプレートを「Controller の **後**」に作るのか？

### Controller との対応関係

| Controller                  | 返すテンプレート               |
| --------------------------- | ------------------------------ |
| `HomeController` （/）      | （リダイレクトなので無し）     |
| `BlogListController` (/blogs)  | `blog/list.html`            |
| `BlogListController` (/admin/blogs) | `admin/blog/list.html` |
| `BlogDetailController`      | `blog/detail.html`             |
| `BlogRegisterController`    | `admin/blog/form.html`（既存） |
| `BlogEditController`        | `admin/blog/form.html`（既存） |
| `BlogDeleteController`      | （リダイレクトのみ）           |
| `LoginController`           | `auth/login.html`（既存）       |
| `UserRegisterController`    | `user/register.html`（既存）   |
| `MyPageController`          | `user/profile.html`            |
| `AdminCategoryController`   | `admin/category/list.html` ＋ `admin/category/form.html`（既存） |

👉 **Controller が「テンプレート名」と「モデル属性」を決めた後でテンプレートを書く** と、
　 `th:object="${blogForm}"` のような **キー名のズレ** が起きない。

---

## 🧱 7-2-4. ステップ① layout.html にフラッシュ表示フラグメントを追加

```html
<div th:fragment="flash">
    <div th:if="${message}"      class="bg-emerald-50 ..." th:text="${message}"></div>
    <div th:if="${errorMessage}" class="bg-red-50 ..."     th:text="${errorMessage}"></div>
</div>
```

### 使う側

```html
<div th:replace="~{fragments/layout :: flash}"></div>
```

- Controller の `redirectAttributes.addFlashAttribute("message", "...")` を **緑色バナー**
- `addFlashAttribute("errorMessage", "...")` を **赤色バナー** で表示
- 全 admin 系一覧で **1行** で組み込める

### globalErrors フラグメントも追加

```html
<div th:fragment="globalErrors" th:if="${#fields.hasGlobalErrors()}">
    <p th:each="err : ${#fields.globalErrors()}" th:text="${err}"></p>
</div>
```

- Controller の `bindingResult.reject("global.error", "...")` を **赤色バナー** で表示
- フィールド単独ではなく **フォーム全体のエラー**（例：メール重複）に使う

---

## 🧱 7-2-5. ステップ② 公開ブログ一覧（blog/list.html）の見どころ

### `th:each` でループ

```html
<article th:each="blog : ${page.content}">
    <span th:text="${blog.category.name}">カテゴリー</span>
    <h3 th:text="${blog.title}">タイトル</h3>
    ...
</article>
```

- `page.content` は **`List<Blog>`**（1ページ分のリスト）
- ループ変数 `blog` で1件ずつアクセス
- **`th:text` の中身は HTML エスケープされる** → XSS の心配がない

### `${blog.category.name}` が動く理由

- Entity の `Blog.category` は **`@ManyToOne(fetch = FetchType.LAZY)`**
- でも Controller の処理は **`@Transactional`** Service 内で行われている
- → セッションが開いている間に `category.getName()` が解決されている
- もしテンプレートで `LazyInitializationException` が出たら：
  - Service メソッド側で **`.getName()` まで呼んでおく**
  - もしくは EntityGraph や fetch join で先読み（第2章 2-6 で予告）

### `${#temporals.format(...)}` で日付整形

```html
<time th:text="${#temporals.format(blog.publishedAt, 'yyyy/MM/dd')}">2026/01/01</time>
```

- Thymeleaf 標準の `#temporals`（Java 8 日付型用ユーティリティ）
- `LocalDateTime` を任意フォーマットで文字列化

### ページャー UI の組み立て

```html
<div th:if="${page.totalPages > 1}">
    <a th:href="@{/blogs(page=${page.number - 1})}" th:if="${!page.first}">←</a>

    <a th:each="i : ${#numbers.sequence(0, page.totalPages - 1)}"
       th:href="@{/blogs(page=${i})}"
       th:text="${i + 1}"
       th:class="${i == page.number} ? '...active...' : '...normal...'">1</a>

    <a th:href="@{/blogs(page=${page.number + 1})}" th:if="${!page.last}">→</a>
</div>
```

- `page.totalPages`：総ページ数
- `page.number`：現在のページ（0始まり）
- `page.first` / `page.last`：先頭／末尾フラグ
- `#numbers.sequence(0, N)`：0..N の数列を作るユーティリティ
- URL組み立て `@{/blogs(page=${i})}` で `/blogs?page=3` を生成

---

## 🧱 7-2-6. ステップ③ 詳細ページ（blog/detail.html）

### 本文の **`##` を見出し化** する

```html
<th:block th:each="line : ${#strings.arraySplit(blog.content, '\n')}">
    <h2 th:if="${#strings.startsWith(line, '## ')}"
        th:text="${#strings.substring(line, 3)}">見出し</h2>
    <p th:unless="${#strings.startsWith(line, '## ') or #strings.isEmpty(line)}"
       th:text="${line}">段落</p>
</th:block>
```

- `#strings.arraySplit(..., '\n')`：改行で分割
- 行が `## ` で始まれば `<h2>`、それ以外は `<p>`
- 簡易マークダウン風レンダリング（**HTMLエスケープも自動**）

### `th:block`

- `th:each` などのループ処理だけ書きたいときに使う **「実体のないタグ」**
- 実際の HTML には何も出力されない
- `<div th:each>` だと無駄な `<div>` がネストするので避ける

---

## 🧱 7-2-7. ステップ④ 管理画面の一覧（admin/blog/list.html）

### **削除ボタンは POST フォーム** で実装

```html
<form th:action="@{/admin/blogs/{id}/delete(id=${blog.id})}" method="post"
      onsubmit="return confirm('本当にこのブログを削除しますか？');">
    <button type="submit">削除</button>
</form>
```

### なぜ `<a>` リンクではダメ？

- `<a href>` は **GET** リクエスト
- GET だと：
  - ブラウザのプリフェッチ／URL直打ち／ブックマークで削除が走る
  - 検索エンジンが「リンクの先」をクロールして消す事故も
- → **削除は POST 一択**

### CSRF トークンの自動挿入

```html
<form th:action="@{/logout}" method="post">
```

- Thymeleaf ＋ Spring Security の組み合わせでは、
- `method="post"` かつ `th:action` を書くと **CSRFトークンの hidden input が自動挿入** される
- 自分で書く必要なし
- → これが無いと Security が「不正リクエスト」として弾く

### ログアウトボタン

```html
<form th:action="@{/logout}" method="post">
    <button type="submit">ログアウト</button>
</form>
```

- 同じくPOSTで `/logout` に投げる
- 設定は SecurityConfig の `.logout(...)` で（後述）

### `<tbody th:if="${page.totalElements == 0}">` で 0件メッセージ

- データが無いときに「登録されているブログはありません」を出す
- `th:if` で表示／非表示を切り替える

---

## 🧱 7-2-8. ステップ⑤ マイページ（user/profile.html）

### `globalErrors` フラグメントの利用

```html
<div th:replace="~{fragments/layout :: globalErrors}"></div>
```

- メール重複等で Service が投げる `IllegalArgumentException` を
- Controller が `bindingResult.reject("global.error", e.getMessage())` で登録
- それを赤バナーとして表示

### アバター文字を抜き出す

```html
<div th:text="${#strings.substring(userRegisterForm.name, 0, 1)}">A</div>
```

- 名前の先頭1文字を取り出してアバター代わりに表示
- `#strings.substring(str, start, end)`：部分文字列

---

## 🔐 7-2-9. ステップ⑥ ★最重要★ SecurityConfig 本格化

### 全体像（[SecurityConfig.java](../src/main/java/com/meitaku/blog/config/SecurityConfig.java)）

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/blogs", "/blogs/**", "/login",
                                 "/admin/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/admin/blogs", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}
```

### `authorizeHttpRequests`：URL 単位のアクセス制御

| ルール                                                    | 意味                                       |
| --------------------------------------------------------- | ------------------------------------------ |
| `.requestMatchers("...").permitAll()`                     | 誰でもアクセスOK（未認証OK）               |
| `.requestMatchers("/admin/**").authenticated()`           | ログイン必須                               |
| `.anyRequest().authenticated()`                           | 上記以外もログイン必須                     |

**順番が重要**：

- `permitAll()` を先に書かないと、`/admin/register` まで `authenticated()` に飲まれる
- → **「より具体的な permit → 後で authenticated」** が鉄則

### `formLogin`：ログインフォーム認証の設定

| 設定                                          | 役割                                                 |
| --------------------------------------------- | ---------------------------------------------------- |
| `.loginPage("/login")`                        | 認証が必要なとき自動で飛ばす画面（GET）              |
| `.loginProcessingUrl("/login")`               | フォーム POST 先（Spring Security が直接処理）       |
| `.usernameParameter("email")`                 | input[name="email"] をユーザー名として扱う           |
| `.passwordParameter("password")`              | input[name="password"] をパスワードとして扱う        |
| `.defaultSuccessUrl("/admin/blogs", true)`    | 認証成功後の遷移先（true=常にここ）                  |
| `.failureUrl("/login?error")`                 | 失敗時の遷移先（`?error` で login.html がバナー表示） |
| `.permitAll()`                                | ログイン関連URLは未認証でもアクセスOK                |

### ★Spring Security が `UserDetailsService` を自動接続する仕組み

- `UserService` が **`implements UserDetailsService`**
- これを Spring が **自動検出** して認証のユーザー読み込みに使う
- `PasswordEncoder` Bean と組み合わせて **DaoAuthenticationProvider** を自動構築
- → SecurityConfig に明示的に書かなくても繋がる（Spring Security 6.x の挙動）

### `logout`：ログアウト設定

```java
.logout(logout -> logout
    .logoutUrl("/logout")              // POST /logout で発火
    .logoutSuccessUrl("/login?logout") // 成功後の遷移先
    .permitAll()
);
```

- `LogoutController` を **書く必要なし**（Spring Security が自動処理）
- フォーム POST で `/logout` に送るだけ

### CSRF は **デフォルトで ON**

- フォーム POST には **必ず CSRF トークンの hidden input が必要**
- Thymeleaf の `th:action` を使うと **自動挿入** されるので意識しなくてOK
- ただし、`<form action="...">`（th:action じゃない）で書くと **挿入されない** → 403 になる

---

## ⚙ 7-2-10. ステップ⑦ application.properties

完成ファイル：[application.properties](../src/main/resources/application.properties)

```properties
# DB接続
spring.datasource.url=jdbc:postgresql://localhost:5432/techblog
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# サーバー
server.port=8080
```

### `spring.jpa.hibernate.ddl-auto` の選択肢

| 値         | 動作                                | いつ使う？                |
| ---------- | ----------------------------------- | ------------------------- |
| `create`   | 毎回 DROP→CREATE（**データ消える**）| ローカルの実験用          |
| `update`   | 差分があれば ALTER                  | **学習・開発（おすすめ）**|
| `validate` | 差分があったらエラー                | 本番直前                  |
| `none`     | 何もしない                          | マイグレーションツール併用 |

👉 **業務では `validate` + Flyway / Liquibase が定石**。今回は学習のため `update`。

### 事前準備：PostgreSQL に DB を作る

```sql
-- psql などで実行
CREATE DATABASE techblog;
```

- ユーザー名／パスワード／URL は環境に合わせて変える
- Docker でやるなら：
  ```bash
  docker run -d --name pg -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16
  docker exec -it pg psql -U postgres -c "CREATE DATABASE techblog;"
  ```

---

## ▶ 7-2-11. 起動手順

### 1. PostgreSQL を起動（上記参照）

### 2. ターミナルで起動

```bash
# Windows
mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

### 3. ブラウザで動作確認

| URL                              | 何が見える？                         |
| -------------------------------- | ------------------------------------ |
| `http://localhost:8080/`         | リダイレクトされてブログ一覧（最初は0件） |
| `http://localhost:8080/admin/register` | 管理者新規登録                       |
| `http://localhost:8080/login`    | ログイン画面                          |
| `http://localhost:8080/admin/blogs` | 認証必須。未ログインなら /login に飛ばされる |

### 4. 初期データ投入（任意）

DB に直接 INSERT する場合：

```sql
INSERT INTO categories (name, created_at, updated_at) VALUES
('Frontend', NOW(), NOW()),
('Backend',  NOW(), NOW()),
('AI / Machine Learning', NOW(), NOW()),
('Design',   NOW(), NOW());
```

管理者は **画面の `/admin/register`** から登録するのが簡単（パスワードが自動でハッシュ化される）。

---

## ❌ 7-2-12. よくあるつまづき

| 症状                                                  | 原因と対処                                                              |
| ----------------------------------------------------- | ----------------------------------------------------------------------- |
| 起動直後、 `/` を開いたら **ログイン画面** に飛ぶ      | SecurityConfig で `/` を `permitAll()` していない → 設定確認            |
| ログインボタンを押すと **403 Forbidden**              | フォームの `th:action` を `action` に書いてしまっている → CSRF未挿入   |
| `LazyInitializationException` がテンプレートで出る    | Service の `@Transactional` 内で `.getName()` まで触っていない          |
| 起動時に `Cannot open connection ... Connection refused` | PostgreSQL 未起動／URL or 認証情報違い                                 |
| `relation "blogs" does not exist`                     | `ddl-auto=none` のまま起動した／DBが空のままになっている                |
| ログイン後 **同じ画面に戻る**                          | `loginProcessingUrl` と `loginPage` が一致しているか／email が登録済か |
| `Encoded password does not look like BCrypt`          | DBに **生パスワード** が入っている → 必ず `/admin/register` 経由で作る  |
| Tailwind が効かない                                   | CDN タグ `<script src="https://cdn.tailwindcss.com">` が head に無い   |
| `getId()` 等のIDEエラー                               | Lombok プラグイン未インストール（コンパイル自体は通る）                 |

---

## 📝 7-2-13. ソースを書くときの順番（実演）

実際にこの後半を書いた **頭の動き** の順番：

1. **layout.html にフラッシュ／全体エラー用フラグメント** を先に追加
2. **公開系テンプレート**（blog/list, blog/detail）を作る
   - まずユーザーが訪れる画面 → ここが動くと達成感が出る
3. **管理系一覧テンプレート**（admin/blog/list, admin/category/list）
   - フラッシュ・削除POST・ページャーの完全パターン
4. **マイページ**（user/profile）
   - `@AuthenticationPrincipal` 連携の確認画面
5. **SecurityConfig 本格化**
   - 公開URL／要認証URLを **permitAll を先・authenticated を後** で書く
   - `usernameParameter("email")` で email ログインに切り替え
6. **application.properties** で DB を繋ぐ
7. **起動 → 一気通貫の動作確認**

---

## ✨ 7-2-14. ソースを書くときのポイント（必読）

### ポイント①：**Controller のモデル属性キーとテンプレートを必ず合わせる**

`th:object="${blogForm}"` と `model.addAttribute("blogForm", ...)` のように、
**スペル・大文字小文字まで完全一致**。

### ポイント②：**`th:each` で `Page<T>` を扱うときは `page.content`**

`Page<T>` そのものをループしない（できない）。
`.content` で List を取り出す。

### ポイント③：**削除は必ず POST フォーム＋ confirm**

`<a>` リンクでは絶対にやらない。
`onsubmit="return confirm('...')"` で誤操作も防ぐ。

### ポイント④：**`th:action`＋`method="post"` で CSRF 自動挿入**

`action=` を直接書くと挿入されず 403 になる。

### ポイント⑤：**SecurityConfig は permitAll → authenticated の順で**

順番がルール評価順になるので、**より具体的な permit を先** に書く。

### ポイント⑥：**`usernameParameter("email")` を忘れない**

何もしないと Spring Security は input[name="username"] を探す。
email でログインするなら必ず指定。

### ポイント⑦：**`UserDetailsService` 実装は自動で組み込まれる**

明示的に `.userDetailsService(...)` と書かなくてOK（Bean があれば勝手につながる）。

### ポイント⑧：**`LazyInitializationException` が出たら Service 側を疑う**

Service の `@Transactional` メソッドの中で、テンプレートが触るプロパティを **先に呼んでおく**。

### ポイント⑨：**`ddl-auto` は学習中だけ `update`**

本番では `validate` + マイグレーションツール。
研修コードのまま本番に持ち込まない。

---

## ✅ 7-2-15. 第7章（後半）まとめ

✔ **テンプレート ← Controller** で参照キーを合わせる
✔ `Page<T>` のループは **`page.content`**、メタは `page.number/totalPages/first/last`
✔ **削除は POST フォーム**＋`onsubmit confirm`
✔ **`th:action`＋`method="post"`** で CSRF が自動挿入される
✔ `SecurityFilterChain` で公開／要認証を振り分ける（permit が先、authenticated が後）
✔ `usernameParameter("email")` で email ログインに切替
✔ `UserDetailsService` 実装クラスは **自動で組み込まれる**（明示不要）
✔ `application.properties` で DB / JPA / 起動ポート設定
✔ `ddl-auto=update` は **学習中だけ**

---

## 🎉 全章まとめ：作った成果物

| 層             | クラス／ファイル                                              | 章 |
| -------------- | ------------------------------------------------------------- | -- |
| Entity         | `Category` / `Blog` / `User`                                  | 1〜3 |
| Repository     | `CategoryRepository` / `BlogRepository` / `UserRepository`    | 4 |
| Form           | `CategoryForm` / `BlogRegisterForm` / `BlogEditForm` / `UserRegisterForm` / `LoginForm` | 5 |
| Template (form)| `auth/login` / `user/register` / `admin/blog/form` / `admin/category/form` | 5 |
| Service        | `CategoryService` / `BlogService` / `UserService`             | 6 |
| Exception      | `ResourceNotFoundException`                                   | 6 |
| Controller     | 10 個（home / blog × 5 / auth / user × 2 / category）         | 7-1 |
| Template (view)| `blog/list` / `blog/detail` / `admin/blog/list` / `admin/category/list` / `user/profile` | 7-2 |
| Config         | `SecurityConfig`（最小→本格化）／ `application.properties`    | 6・7-2 |

7章で **動くアプリ** に到達しました。
ここからは：

- **テストの追加**（`@SpringBootTest` / `@DataJpaTest` / `@WebMvcTest`）
- **本格的なバリデーション**（カスタムバリデータ）
- **画像アップロード**（MultipartFile）
- **本番用 SecurityConfig**（CORS、CSP、HTTPS強制）
- **DBマイグレーション**（Flyway）
- **CI/CD**

など、応用領域に進めます。

お疲れさまでした 🎉
