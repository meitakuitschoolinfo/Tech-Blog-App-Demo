# 📘 第6章

## Service を作る
― **業務ロジックの中心**。Repository ＋ Form が、ここで初めてつながる ―

---

## 🎯 6-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ Service の役割を **一文で説明できる**
✔ なぜ Controller が直接 Repository を呼ばないか説明できる
✔ **`@Transactional(readOnly = true)`** をクラスに付け、書き込みメソッドで上書きする理由が分かる
✔ **コンストラクタインジェクション**（`@RequiredArgsConstructor`）が `@Autowired` より好まれる理由を3つ挙げられる
✔ Form → Entity の **詰め替え** が書ける
✔ `PasswordEncoder.encode(...)` を **Service で呼ぶ** 理由を説明できる
✔ `UserDetailsService.loadUserByUsername(...)` の意味を説明できる
✔ カテゴリー削除前の **使用件数チェック** のような業務ルールが書ける

👉 ここで「**今までのピースが全部つながる**」感覚を体験してください。

---

## 🧩 6-2. Service とは何か？（超重要）

### 一言で言うと

> Service =
> **業務のルール・流れ・判断** を担当するクラス

### Service の立ち位置（再確認）

```text
[ Controller ]  ← HTTP受付・画面との橋渡し
     ↓
[  Service  ]   ← ★ ここ：業務の流れ・判断
     ↓
[ Repository ]  ← DBとのやり取りだけ
     ↓
[  Entity  ]    ← テーブル1行の形
     ↓
[ Database  ]
```

### Service が **やる** こと

- ✔ 業務ルールの判定（「削除できるか？」「重複していないか？」）
- ✔ Form → Entity への **詰め替え**
- ✔ 複数 Repository の組み合わせ呼び出し
- ✔ パスワードのハッシュ化／メール送信などの **「副作用」の呼び出し**
- ✔ トランザクション境界（`@Transactional`）の張り方

### Service が **やらない** こと

- ❌ HTTPリクエストを直接さわる（→ Controller）
- ❌ SQL を書く（→ Repository）
- ❌ HTMLを組み立てる（→ Thymeleaf）

👉 **「判断する」のが Service、「動かす」のが Repository**（PDF 3-12）。

---

## 🔄 6-3. なぜ Form の次に Service を書くのか？

### 鉄則（おさらい）

> **「依存される側 → 依存する側」** の順で作る

### Service の依存関係

```text
[ Service ]
   ├── Repository  ← 第4章で完成
   ├── Form        ← 第5章で完成
   ├── Entity      ← 第1〜3章で完成
   └── PasswordEncoder ← SecurityConfig で @Bean 定義
```

- Service は **「下流のすべて」に依存する**
- → 下流が揃ってから書くと **import エラーなくスラスラ書ける**

### この章で **書く順番**（重要）

| 順番 | 作るもの                      | 学ぶこと                                                          |
| ---: | ----------------------------- | ----------------------------------------------------------------- |
|    1 | `ResourceNotFoundException`   | **カスタム例外** の作り方（@ResponseStatus）                       |
|    2 | `SecurityConfig`（最小版）    | **`PasswordEncoder` Bean** の準備（第7章で本格化）                 |
|    3 | `CategoryService`             | Service の基本形＋**削除前チェック**                               |
|    4 | `BlogService`                 | Form ↔ Entity の **詰め替え** ＋ ページング                         |
|    5 | `UserService`                 | **`PasswordEncoder`** ＋ **`UserDetailsService` 実装**             |

---

## 🧱 6-4. ステップ① ResourceNotFoundException（カスタム例外）

完成ファイル：[ResourceNotFoundException.java](../src/main/java/com/meitaku/blog/exception/ResourceNotFoundException.java)

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### なぜわざわざ自作するのか？

- PDF 3-7 でも `.orElseThrow(() -> new ResourceNotFoundException("..."))` が登場
- 「存在しない」ことを **意味の通った名前** で表現したい
- → ログを見たときに「ああ、リソースが無かったんだな」と一発で分かる

### `@ResponseStatus(HttpStatus.NOT_FOUND)` の効果

- この例外が **Controller の外側まで到達** したら、Spring が自動で **HTTP 404** を返してくれる
- 個別の try-catch なしで「無ければ404」が成立する

### なぜ `RuntimeException` を継承するか？

- `Exception`（チェック例外）だと **メソッドシグネチャに `throws` が必要** → 呼び出し側が汚れる
- `RuntimeException`（非チェック例外）なら宣言不要
- Spring の世界は **非チェック例外が主流**

---

## 🧱 6-5. ステップ② SecurityConfig（PasswordEncoder だけ用意）

完成ファイル：[SecurityConfig.java](../src/main/java/com/meitaku/blog/config/SecurityConfig.java)

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### なぜこの章で SecurityConfig を作るのか？

- `UserService` で **パスワードをハッシュ化したい** → `PasswordEncoder` が必要
- でも `PasswordEncoder` は **Spring の DI コンテナに居る Bean** でないと注入できない
- → **`@Bean` でDI登録するため** に最低限の SecurityConfig を作る

### 「最小版」と言っているのは？

- `SecurityFilterChain`（どのURLに認証を要求するか）はまだ書いていない
- ログインURL、ログアウト処理、`/admin/**` の保護などは **第7章で追加**
- → 今の段階ではまだ **ブラウザでログイン画面に飛ばされたりはしない**

### `BCryptPasswordEncoder` を選ぶ理由

- ハッシュ関数として **業界標準**
- 1ハッシュにつき自動で salt が混ざる
- 計算コストが高め（攻撃者にも遅い＝安全）
- Spring Security のデフォルトとも整合

---

## 🧱 6-6. ステップ③ CategoryService（Service の基本形）

完成ファイル：[CategoryService.java](../src/main/java/com/meitaku/blog/service/CategoryService.java)

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;

    public List<Category> findAll() { ... }
    public Category findById(Long id) { ... }
    public CategoryForm toEditForm(Long id) { ... }

    @Transactional
    public Category register(CategoryForm form) { ... }

    @Transactional
    public Category update(CategoryForm form) { ... }

    @Transactional
    public void delete(Long id) { ... }
}
```

### `@Service` アノテーション

- このクラスを **Spring の DI コンテナに Service 役として登録** する
- 同じ機能のアノテーションに `@Component` があるが、**「役割を表す」** ために `@Service` を使う

### `@Transactional(readOnly = true)` をクラスに付ける理由（重要）

- **既定** を読み取り専用にしておくと、参照系メソッドで毎回書かなくて済む
- 書き込みメソッドだけ `@Transactional`（readOnly=false 相当）で **上書き** する
- メリット：
  - 参照系で誤って書き込みが走るとエラーで気づける
  - 読み取り専用は内部最適化が効く（Hibernateのダーティチェック省略など）

### `@RequiredArgsConstructor` ＋ `private final` の威力（重要）

```java
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;
    // ↑ Lombok が以下のコンストラクタを自動生成する：
    //   public CategoryService(CategoryRepository categoryRepository, BlogRepository blogRepository) {
    //       this.categoryRepository = categoryRepository;
    //       this.blogRepository = blogRepository;
    //   }
}
```

#### なぜ `@Autowired` よりこちらが推奨？

| 観点                     | フィールドインジェクション `@Autowired` | コンストラクタインジェクション `@RequiredArgsConstructor` |
| ------------------------ | --------------------------------------- | --------------------------------------------------------- |
| `final` にできる         | ❌                                      | ⭕                                                       |
| テストで Mock を渡しやすい | ❌（リフレクション必須）              | ⭕（コンストラクタに直接渡せる）                          |
| 循環参照に **早く気づく**   | ❌（起動後にバレる）                  | ⭕（起動時にエラー）                                      |
| **必須依存** であることが型で見える | ❌                              | ⭕（コンストラクタ引数として表現）                        |

👉 **2026年現在のベストプラクティスはコンストラクタインジェクション一択**。

### `register` メソッドの流れ

```java
@Transactional
public Category register(CategoryForm form) {
    if (categoryRepository.existsByName(form.getName())) {
        throw new IllegalArgumentException("同じ名前のカテゴリーが既に存在します");
    }
    Category category = new Category();
    category.setName(form.getName());
    return categoryRepository.save(category);
}
```

3ステップ：

1. **業務ルールチェック**（同名カテゴリーが無いか）
2. Form → Entity への **詰め替え**
3. Repository に **保存**

### `delete` メソッド：**「削除できるか？」 は Service の仕事**（PDF 3-12）

```java
@Transactional
public void delete(Long id) {
    Category category = findById(id);
    long usageCount = blogRepository.countByCategory(category);
    if (usageCount > 0) {
        throw new IllegalStateException(
            "このカテゴリーは " + usageCount + " 件のブログで使われているため削除できません");
    }
    categoryRepository.delete(category);
}
```

- 「ブログで使われていないか？」の **判定は Service**
- 「数える」「消す」の **実行は Repository**
- これがPDFの **「判断するService／動かすRepository」** の典型例

### `IllegalArgumentException` vs `IllegalStateException` の使い分け

| 例外                          | いつ使う？                                          |
| ----------------------------- | --------------------------------------------------- |
| `IllegalArgumentException`    | **引数が不正**（重複、フォーマット違反など）         |
| `IllegalStateException`       | **オブジェクトの状態が不正**（依存先がある／使用中） |
| `ResourceNotFoundException`   | **対象が存在しない**（→ HTTP 404）                  |

---

## 🧱 6-7. ステップ④ BlogService（Form ↔ Entity 詰め替え）

完成ファイル：[BlogService.java](../src/main/java/com/meitaku/blog/service/BlogService.java)

### Form → Entity の詰め替えコード（register）

```java
@Transactional
public Blog register(BlogRegisterForm form) {
    Category category = categoryRepository.findById(form.getCategoryId())
        .orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません: id=" + form.getCategoryId()));

    Blog blog = new Blog();
    blog.setTitle(form.getTitle());
    blog.setContent(form.getContent());
    blog.setImageUrl(form.getImageUrl());
    blog.setCategory(category);    // ★ Long → Category への変換が完了

    return blogRepository.save(blog);
}
```

### ★最重要★ `Long categoryId` → `Category category` への変換

- Form は **`Long categoryId`** を持っている（第5章 5-8）
- Entity は **`Category category`** を持っている（第2章 2-5）
- **その間をつなぐのが Service の仕事**

```text
[ 画面 ] → Long categoryId → [ Form ]
                                 │
                                 ▼ Service が Repository で解決
                              [ Category Entity ]
                                 │
                                 ▼ Entity にセット
                              [ Blog Entity (DB) ]
```

👉 **「Service が Form と Entity の橋渡しをする」** ことが、ここで実感できる。

### Entity → Form の詰め替え（toEditForm）

編集画面を開く時の流れ：

```java
public BlogEditForm toEditForm(Long id) {
    Blog blog = findById(id);
    BlogEditForm form = new BlogEditForm();
    form.setId(blog.getId());
    form.setTitle(blog.getTitle());
    form.setContent(blog.getContent());
    form.setImageUrl(blog.getImageUrl());
    form.setCategoryId(blog.getCategory().getId());   // ★ Category → Long に戻す
    return form;
}
```

- **register / update とは逆方向の詰め替え**
- `blog.getCategory().getId()` の部分が **LAZY な関連を初めて触る** → `@Transactional` がスコープを保証してくれる

### update の流れ

```java
@Transactional
public Blog update(BlogEditForm form) {
    Blog blog = findById(form.getId());                       // ← 既存を取得
    Category category = categoryRepository.findById(...)     // ← 新カテゴリーも取得
        .orElseThrow(...);

    blog.setTitle(form.getTitle());
    blog.setContent(form.getContent());
    blog.setImageUrl(form.getImageUrl());
    blog.setCategory(category);

    return blogRepository.save(blog);   // id ありの save → UPDATE文
}
```

- **「既存を取得 → 値を上書き → save」** のパターン
- `save` は **id があれば UPDATE、無ければ INSERT** を自動判断（PDF 3-6）

---

## 🧱 6-8. ステップ⑤ UserService（PasswordEncoder ＋ UserDetailsService）

完成ファイル：[UserService.java](../src/main/java/com/meitaku/blog/service/UserService.java)

### `implements UserDetailsService`

```java
public class UserService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("管理者が見つかりません: " + email));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())   // ハッシュ済みをそのまま渡す
                .authorities("ROLE_ADMIN")
                .build();
    }
}
```

#### `UserDetailsService` とは？

- Spring Security が **「ログインリクエストが来たぞ。誰が誰だか教えて」** と呼んでくる窓口
- 戻り値の **`UserDetails`** が「ログイン中ユーザー」として Security 内部で持ち回される

#### 「我々の Entity の User」 と 「Spring Security の User」 は **別物**

```java
// 我々の Entity
import com.meitaku.blog.entity.User;

// Spring Security 側
org.springframework.security.core.userdetails.User
```

- 同じ名前なので **import 衝突する**
- → Spring 側は **フルパス** で書いて衝突を回避（実コード参照）

#### ハッシュは絶対に **元に戻さない**

```java
.password(user.getPassword())   // ← DBに入ってる「ハッシュ済み文字列」をそのまま渡す
```

- 「ログイン時に入力された生パスワード」と「DBに入っているハッシュ」を
  **`PasswordEncoder.matches(生, ハッシュ)`** で照合するのは Spring Security の内部処理
- 我々の Service は **「ハッシュをそのまま渡す」だけ**
- 生に戻す処理は絶対に書かない（そもそも数学的に不可能）

### register での **ハッシュ化**

```java
@Transactional
public User register(UserRegisterForm form) {
    if (userRepository.existsByEmail(form.getEmail())) {
        throw new IllegalArgumentException("このメールアドレスは既に登録されています");
    }
    User user = new User();
    user.setName(form.getName());
    user.setEmail(form.getEmail());
    user.setPassword(passwordEncoder.encode(form.getPassword()));  // ★ハッシュ化
    return userRepository.save(user);
}
```

第3章 3-7／3-8 で「**ハッシュ化は Service の仕事**」と言った内容の **実装そのもの** がここ。

### 重複チェックの「二段構え」

第5章 5-10 で予告したパターンが完成する：

| 層         | 重複チェック                          |
| ---------- | ------------------------------------- |
| **Service**| `existsByEmail` で **事前に弾く**     |
| **DB**     | UNIQUE 制約で **最後の砦** として守る |

→ 同時登録の競合があっても、DB が必ず止めてくれる。

---

## 🌀 6-9. `@Transactional` の張り方（深掘り）

### 基本形（今回の3 Service すべて）

```java
@Service
@Transactional(readOnly = true)    // ← クラス全体は「読み取り専用」が既定
@RequiredArgsConstructor
public class XxxService {

    public Xxx findById(Long id) { ... }   // ← 読み取り専用がそのまま効く

    @Transactional                          // ← 書き込み系で上書き
    public Xxx register(...) { ... }

    @Transactional
    public void delete(Long id) { ... }
}
```

### `@Transactional` は何をしてくれる？

1. メソッド開始時に **トランザクションを開く**
2. メソッドが正常終了したら **コミット**
3. **`RuntimeException` が throw されたら自動で ロールバック**
4. メソッド中は **DBセッションが開いた状態** → LAZY な関連も触れる

### `readOnly = true` の意味

- このトランザクションでは「書き込みしません」と Spring に宣言する
- Hibernate は **ダーティチェック（変更検知）を省略** して高速化
- 万が一 save を呼んでも **コミット時に flush しない** → エラー検知に役立つ

### よくあるハマり：プロキシ越しでないと効かない

```java
@Service
public class FooService {
    @Transactional
    public void a() {
        this.b();    // ← ★ 内部呼び出しだと @Transactional が効かない！
    }
    @Transactional
    public void b() { ... }
}
```

- `@Transactional` は Spring の **プロキシ機構** で効く
- 同クラス内の `this.xxx()` 呼び出しは **プロキシを通らない** → 効かない
- 解決：別クラスに切り出すか、自身を `@Autowired` し直す

---

## ❌ 6-10. 初心者がやりがちなNG

| NGパターン                                                       | 何が起きる？                                          |
| ---------------------------------------------------------------- | ----------------------------------------------------- |
| Controller から Repository を直接呼ぶ                            | Service が空洞化／業務ルールが散らばる                |
| Service で SQL を組み立てる                                      | 層が崩壊／テストできない                              |
| Service で HTTP 関連クラスを使う                                 | Controller との結合が密になる                         |
| Service で `setCreatedAt(now)` を書く                             | Entity の `@PrePersist` の意味が無くなる              |
| 生パスワードを `user.setPassword(form.getPassword())` で保存     | **重大なセキュリティ事故**                            |
| `@Autowired` を新しく書く                                        | コンストラクタインジェクションを習慣化できない        |
| `@Transactional` を **private メソッド** に付ける                | プロキシが効かない → 効果ゼロ                         |
| クラスに `@Transactional` を付けず、毎メソッドに書く             | 抜け漏れバグの温床                                    |
| 業務エラーを `Exception` で投げる                                | 呼び出し側が `throws` だらけになる／意味が伝わらない  |

---

## 📝 6-11. ソースを書くときの順番（実演）

実際に Service 関連5ファイルを書いた **頭の動き** の順番：

1. **`ResourceNotFoundException`** を先に作る（後で Service から投げる用）
2. **`SecurityConfig`（最小版）** を作って `PasswordEncoder` Bean を用意
3. **`CategoryService`** から始める（依存が浅く、Service の基本形を学ぶのに最適）
4. **`BlogService`** に進む（Form ↔ Entity 詰め替えとページング）
5. **`UserService`** で締める（`PasswordEncoder` ＋ `UserDetailsService` 実装）

各 Service の中の **書く順番**（共通テンプレ）：

1. クラス宣言＋3点セット（`@Service` / `@Transactional(readOnly = true)` / `@RequiredArgsConstructor`）
2. `final` フィールドで依存を宣言
3. **参照系メソッド**（`findAll` / `findById` / `findBy○○`）
4. **編集画面初期表示用** の詰め替えメソッド（`toEditForm`）
5. **書き込み系メソッド**（`register` / `update` / `delete`）— `@Transactional` を忘れず付ける

---

## ✨ 6-12. ソースを書くときのポイント（必読）

### ポイント①：**`@Service` ＋ `@Transactional(readOnly = true)` ＋ `@RequiredArgsConstructor` をセットで覚える**

これがServiceの **三種の神器**。
理由を訊かれたら即答できるように。

### ポイント②：**依存は `private final` ＋ コンストラクタインジェクション**

`@Autowired` フィールドは時代遅れ。
**テスト容易性／不変性／循環参照早期検出** の3点で勝つ。

### ポイント③：**書き込み系メソッドには `@Transactional` を必ず明示**

クラスに付けた `readOnly = true` を **必ず上書き** する。
書き忘れると INSERT/UPDATE が動かないこともある。

### ポイント④：**Form → Entity 詰め替えは Service の中心仕事**

ID → Entity の解決（`categoryRepository.findById(...)`）も Service の仕事。
Controller でやらない、Form にやらせない。

### ポイント⑤：**ハッシュ化は Service。Entity は触らない**

`PasswordEncoder.encode(...)` を呼ぶのは Service 一択。

### ポイント⑥：**業務ルール違反は意味のある例外で投げる**

- 「存在しない」 → `ResourceNotFoundException`
- 「引数不正・重複」 → `IllegalArgumentException`
- 「状態不正・使用中」 → `IllegalStateException`

### ポイント⑦：**LAZY 関連を触る処理は `@Transactional` の内側に書く**

`blog.getCategory().getName()` のような呼び出しは、
Service メソッドの中（トランザクション内）で済ませる。
画面で初めて触ろうとすると `LazyInitializationException`。

### ポイント⑧：**`@Transactional` は public な「外部から呼ばれるメソッド」に付ける**

private メソッドや this 経由の呼び出しでは効かない。

---

## ✅ 6-13. 第6章まとめ（完全理解）

✔ Service = **業務の流れ・判断** を担う層
✔ **依存される側（Exception / Encoder）→ Service** の順で作る
✔ **`@Service` / `@Transactional(readOnly = true)` / `@RequiredArgsConstructor`** が三種の神器
✔ 依存は **`private final` ＋ コンストラクタインジェクション**
✔ 書き込み系は **`@Transactional` で上書き**
✔ Form と Entity の **詰め替えは Service の中心仕事**
✔ **パスワードのハッシュ化は Service**（Entity に触らせない）
✔ **業務ルール** は意味のある例外で表現する
✔ `UserService` は `UserDetailsService` を実装して **Spring Security と接続** する

---

## 🔜 次の章

**第7章：Controller を作る（＋ SecurityConfig 本格化 ＋ 残りのテンプレート）**

ついに最終章。

- `@Controller` の作り方／URL 設計
- `Model` への詰め替え
- フォーム POST の受け取り（`@Valid`／`BindingResult`）
- リダイレクト ＋ フラッシュメッセージ
- `SecurityConfig` の本格化（`SecurityFilterChain`／ログインURL／認可）
- 一覧・詳細ページのテンプレート（残り）

これですべてのピースが **動く形** になります。
