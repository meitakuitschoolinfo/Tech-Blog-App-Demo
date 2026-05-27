# 📘 第5章

## Form を作る（＋テンプレートも同時に）
― **画面 ⇔ サーバー** をつなぐ「入力の箱」を完全理解する ―

> 📌 **更新ノート（第8章で変更あり）**
> 本章では `BlogRegisterForm` / `BlogEditForm` が画像を **URL文字列** で受け取る設計になっていますが、
> 第8章で **ファイルアップロード方式（`MultipartFile`）** に切り替えています。
> 最新の Form 構造は [第8章_画像アップロード対応.md](./第8章_画像アップロード対応.md) を参照してください。
> ─ なお、ここでの「画面 ⇔ Form のキー名一致」の原則は変わりません。

---

## 🎯 5-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ Form クラスの役割を **一文で説明できる**
✔ **「Entity じゃダメな理由」** を3つ以上挙げられる
✔ `@NotBlank` / `@NotNull` / `@Size` / `@Email` の **使い分け** ができる
✔ テンプレートの **`th:field`** と Form フィールドの関係を絵で書ける
✔ Form の `categoryId: Long` と Entity の `category: Category` の **違い** を説明できる
✔ Register 用 / Edit 用フォームを **分ける／分けない** の判断基準を持っている

👉 ここで **「画面とJavaの架け橋」** が腹落ちすると、コントローラが一気に読みやすくなります。

---

## 🧩 5-2. Form とは何か？（超重要）

### 一言で言うと

> Form =
> **「画面の入力1件分」を Java のクラスとして表したもの**

### Form の立ち位置

```text
[ ブラウザ ] ──HTMLフォーム──▶ [ Controller ]
                                   │
                                   ▼
                              [  Form  ] ← ★ ここ：画面の入力をそのまま受け取る箱
                                   │
                                   ▼
                              [ Service ]
                                   │
                                   ▼
                              [ Entity ] ← DBの形
```

👉 Form は **「画面側の都合」** に近いクラス。
　 Entity は **「DB側の都合」** に近いクラス。
　 **形は似ていても役割が違う**。

---

## 🔄 5-3. なぜ Repository の次に Form を書くのか？

### 鉄則（おさらい）

> **「依存される側 → 依存する側」** の順で作る

### Service と Form の依存関係

```text
[ Service ]
   ├── Repository  ← 第4章で完成
   └── Form        ← ★ 第5章でここ
```

- Service は **Repository と Form の両方を使う**
- → Service より先に、両者を揃えておく必要がある
- → Repository（第4章） → Form（第5章） → Service（第6章） の順

### この章で **書く順番**（重要）

| 順番 | 作るもの               | 学ぶこと                                            |
| ---: | ---------------------- | --------------------------------------------------- |
|    1 | `CategoryForm`         | **Form の最小形**＋register/edit を1つで兼ねるパターン |
|    2 | `BlogRegisterForm`     | **複数の制約**を組み合わせる（`@NotBlank` + `@Size` + `@NotNull`） |
|    3 | `BlogEditForm`         | **register と edit を分ける** パターン（id持ち）      |
|    4 | `UserRegisterForm`     | **`@Email`** の使い方                                |
|    5 | `LoginForm`            | **Spring Security との接点** を意識した最小フォーム  |
|    + | 各画面の Thymeleaf テンプレート | `th:field` で Form と紐づける                  |

---

## 🆚 5-4. ★最重要★ Form と Entity を **絶対に混ぜない** 理由

### 「Entity を画面で使い回せばいいじゃん」と思った瞬間にやらかすこと

| ＃   | 問題                                                                 | 起きること                                                |
| ---- | -------------------------------------------------------------------- | --------------------------------------------------------- |
|  ①   | Entity に **バリデーションを書き散らかす**                           | DB制約と画面制約が混ざって読めなくなる                    |
|  ②   | Entity の **書き換えたくないフィールド** まで画面から書き換えられる | `id` や `createdAt` を改ざんされる                        |
|  ③   | パスワードのような **画面入力 ≠ DB保存値** に対応できない             | 生パスワードを Entity に直で入れて事故                    |
|  ④   | Entity のリレーション (`Category category`) を画面から扱うのは難しい | 画面は `categoryId: Long` で扱う方が圧倒的に楽            |
|  ⑤   | 画面が変わるたびに **Entity が腐っていく**                            | 「この画面用のフィールド」が混入する                       |

👉 **画面の都合と DB の都合は混ぜるな**。これが鉄則。

### 二段制約（Form + Entity）の絵

```text
[ 画面 ] ─入力─▶ [ Form ]     ← @NotBlank / @Size / @Email（画面寄りのチェック）
                    │
                    ▼ Service が値を詰め替え
                  [ Entity ] ← @Column(nullable=false, length=...)（DB寄りの制約）
                    │
                    ▼
                  [ DB ]    ← NOT NULL / VARCHAR(N) / UNIQUE（最後の砦）
```

3重で守るので、**どこかが抜けても他の層が守る** ようになる。

---

## 🧠 5-5. Form クラスの共通テンプレ

すべての Form は基本的にこの形：

```java
@Getter
@Setter
@NoArgsConstructor
public class XxxForm {
    @NotBlank(message = "...")
    @Size(max = ...)
    private String xxx;
}
```

### なぜ Entity と **同じ Lombok 3点セット** なのか？

| アノテーション         | なぜ必要？                                                          |
| ---------------------- | ------------------------------------------------------------------- |
| `@Getter` / `@Setter`  | Thymeleaf や Spring が **リフレクションで読み書き** するため        |
| `@NoArgsConstructor`   | Spring が画面入力を詰めるとき、**まず引数なしで生成→setter** するため |

👉 Entity と同じ理由で **JPA/Springが内部でリフレクションを使うから必須**。

---

## 🏷 5-6. バリデーション・アノテーション早見表（実用）

| アノテーション   | 何を弾く？                              | 主な対象型     | 例                                  |
| ---------------- | --------------------------------------- | -------------- | ----------------------------------- |
| `@NotNull`       | null だけ                               | 数値・参照型   | `@NotNull Long categoryId;`         |
| `@NotEmpty`      | null + 空文字 + 空コレクション          | String, List等 | `@NotEmpty List<String> tags;`      |
| `@NotBlank`      | null + 空文字 + **空白だけ** の文字列   | String専用     | `@NotBlank String title;`           |
| `@Size(min,max)` | 文字数／要素数                          | String, List等 | `@Size(max = 200)`                  |
| `@Email`         | メアド形式（`@` を含む）                | String         | `@Email String email;`              |
| `@Min` / `@Max`  | 数値の最小／最大                        | 数値           | `@Min(1) int qty;`                  |
| `@Pattern`       | 正規表現                                | String         | `@Pattern(regexp = "...")`          |

### 使い分けで一番ハマる「`@NotBlank` vs `@NotNull` vs `@NotEmpty`」

```text
@NotNull   ────  null だけNG（"" や "   " はOK！）
@NotEmpty  ────  null も "" もNG（"   " はOK！）
@NotBlank  ────  null も "" も "   " もNG ← 文字列は基本これ
```

👉 **String には `@NotBlank` を使う**、と覚える。
　 数値や参照型（Long など）には `@NotBlank` は付けられない → `@NotNull` を使う。

---

## 🧱 5-7. ステップ① CategoryForm（最小形＋兼用）

完成ファイル：[CategoryForm.java](../src/main/java/com/meitaku/blog/form/category/CategoryForm.java)

```java
public class CategoryForm {
    private Long id;                          // 編集時のみ値が入る
    @NotBlank @Size(max = 100)
    private String name;
}
```

### このフォームの特徴

- **1クラスで register / edit を兼用** している
- 新規 → `id == null`
- 編集 → `id != null`
- Controller 側で `id` の有無で分岐する

### なぜ Blog は分けて、Category は分けないのか？

- フィールド数が少なく、**Register と Edit でバリデーションが完全に同じ** → 1クラスでOK
- 将来「カテゴリーの登録時だけ何かチェック」が出てきたら、その時に分ければ良い
- → **DRY（Don't Repeat Yourself）と YAGNI（必要になるまで作らない）** のバランス

> 📌 MDのパッケージ構成には `form/category` は明記されていないが、
>    画面側で必要なので追加している。
>    → **「仕様は完璧ではない、必要なら埋める」** という現場感覚も学びどころ。

---

## 🧱 5-8. ステップ② BlogRegisterForm（複数の制約の組み合わせ）

完成ファイル：[BlogRegisterForm.java](../src/main/java/com/meitaku/blog/form/blog/BlogRegisterForm.java)

```java
public class BlogRegisterForm {
    @NotBlank @Size(max = 200) private String title;
    @NotBlank                   private String content;
    @NotNull                    private Long categoryId;   // ★ Category ではなく Long
    @Size(max = 500)            private String imageUrl;   // 任意項目 → @NotBlank 無し
}
```

### ★最重要★ なぜ `Long categoryId` であって `Category category` ではないのか？

| 観点                    | `Long categoryId`（Form）        | `Category category`（Entity） |
| ----------------------- | -------------------------------- | ----------------------------- |
| 画面の `<select value>` | そのまま使える                   | できない（id を取り出して使う） |
| バリデーション          | `@NotNull` で完結                | 複雑になる                    |
| JPAから独立             | ⭕                              | ❌                            |
| Service側で詰め替え     | `categoryRepository.findById()` | 不要                          |

👉 **Form は ID（Long）だけ持つ**。
　 **Service が Repository を使って Entity に変換する**。
　 これが Spring Boot の鉄板パターン。

### `imageUrl` に `@NotBlank` を付けていない理由

- テーブル定義書で **NOT NULL ではない**（任意項目）
- → Form でも必須にしない
- → 入力があった場合に限り `@Size(max = 500)` がチェックする

> 💡 任意項目で `@Size` だけ書く → 「入力があったら長さチェック、無くてもOK」。

---

## 🧱 5-9. ステップ③ BlogEditForm（id を持たせる）

完成ファイル：[BlogEditForm.java](../src/main/java/com/meitaku/blog/form/blog/BlogEditForm.java)

```java
public class BlogEditForm {
    @NotNull private Long id;                        // ★ hidden で送ってくる
    @NotBlank @Size(max = 200) private String title;
    @NotBlank                  private String content;
    @NotNull                   private Long categoryId;
    @Size(max = 500)           private String imageUrl;
}
```

### `Register` と `Edit` を **分ける** メリット

| メリット                                                  | 説明                                                      |
| --------------------------------------------------------- | --------------------------------------------------------- |
| Register に **`id` が無い** ことを型レベルで強制できる    | 新規登録で id が紛れ込む事故を防げる                      |
| 編集時にだけ必要なバリデーションを書きやすい              | 例：編集時のみ排他制御用の `version` を増やす場合         |
| Controller のメソッドシグネチャが **意図を明確に伝える**  | `create(BlogRegisterForm form)` と `update(BlogEditForm form)` |

### `Register` と `Edit` を **分けない（1クラス）** メリット

- コードの重複が無い
- フィールド変更時のメンテが楽

👉 **「フィールドの責務が大きく変わるなら分ける」** が判断基準。
　 今回は **MDの指定に従って分ける** ことで「分けた場合の書き方」を学ぶ。

### Edit フォームの id は **hidden で送る**

```html
<input type="hidden" th:if="${isEdit}" th:field="*{id}">
```

- 編集画面を表示する時点で id が確定している
- ユーザーには触らせない → `type="hidden"`
- Controller で `@Valid BlogEditForm form` を受け取ると **id がそのまま入っている**

---

## 🧱 5-10. ステップ④ UserRegisterForm（`@Email` 登場）

完成ファイル：[UserRegisterForm.java](../src/main/java/com/meitaku/blog/form/user/UserRegisterForm.java)

```java
public class UserRegisterForm {
    @NotBlank @Size(max = 100)             private String name;
    @NotBlank @Email @Size(max = 255)      private String email;
    @NotBlank @Size(min = 8, max = 255)    private String password;
}
```

### `@Email` の動き

- `@` を含む基本的なメアド形式かどうかをチェック
- **厳密な存在確認はしない**（DNS/SMTPまで見ない）
- → 「形式チェックレベル」と理解する

### パスワードの `@Size(min = 8)` の意味

- DB制約には 「8文字以上」は無い（`VARCHAR(255)` だけ）
- → これは **画面側の品質ルール**
- → だから **Form 側に書く**（Entityには書かない）

👉 **「DB制約」と「業務ルール」を混ぜない**。
　 業務ルール（複雑性チェックなど）は Form / Service にだけ書く。

### 重複チェック（emailが既にあるか）は ここではやらない

- バリデーションアノテーションは **1フィールド単独で完結するチェック** が得意
- 「DBに同じemailがあるか？」は **DB問い合わせが必要** → Service の仕事

→ Form は「形」だけチェック、Service は「業務」をチェック、Entity/DBは「整合性」を守る。
　 **3層構え**。

---

## 🧱 5-11. ステップ⑤ LoginForm（Security の入口）

完成ファイル：[LoginForm.java](../src/main/java/com/meitaku/blog/form/user/LoginForm.java)

```java
public class LoginForm {
    @NotBlank @Email private String email;
    @NotBlank        private String password;
}
```

### なぜ「最小限」なのか？

- ログインフォームは **「形式チェックだけ」** あれば充分
- 「ユーザーが存在するか？」「パスワードが合っているか？」は **Spring Security の仕事**
- → Form は **画面表示と空入力ガード** だけを担当

### 第7章の予告

`/login` への POST は **Spring Security が直接受け取る** ように設定する予定。
そのため、LoginForm は **GET時の `th:object` バインド用** がメインになる。
（バリデーションは「空欄を弾く」程度に効くと思っておく）

---

## 🧩 5-12. ★同時に作る★ テンプレート側との対応

### 「`th:field="*{name}"` が name 属性を作る」仕組み

Thymeleaf の `th:field="*{name}"` は、HTMLレンダリング時に：

```html
<input id="name" name="name" value="（Formのname値）">
```

を **自動生成** する。

- `id="name"` ← フィールド名
- `name="name"` ← フィールド名（POST時にサーバーが見る名前）
- `value="..."` ← Form の現在値

👉 **`th:field` の中身と Form のフィールド名が一致していれば、ぜんぶ自動でつながる**。

### 1対1の対応表（今回）

| テンプレート                                | `th:field`           | Formクラス                | Formフィールド    |
| ------------------------------------------- | -------------------- | ------------------------- | ----------------- |
| `auth/login.html`                           | `*{email}`           | `LoginForm`               | `email`           |
| 〃                                          | `*{password}`        | 〃                        | `password`        |
| `user/register.html`                        | `*{name}`            | `UserRegisterForm`        | `name`            |
| 〃                                          | `*{email}`           | 〃                        | `email`           |
| 〃                                          | `*{password}`        | 〃                        | `password`        |
| `admin/blog/form.html`                      | `*{title}`           | `BlogRegisterForm` or `BlogEditForm` | `title`           |
| 〃                                          | `*{content}`         | 〃                        | `content`         |
| 〃                                          | `*{categoryId}`      | 〃                        | `categoryId`      |
| 〃                                          | `*{imageUrl}`        | 〃                        | `imageUrl`        |
| 〃（編集時のみ）                            | `*{id}`              | `BlogEditForm`            | `id`              |
| `admin/category/form.html`                  | `*{name}`            | `CategoryForm`            | `name`            |
| 〃（編集時のみ）                            | `*{id}`              | 〃                        | `id`              |

👉 **「Formのフィールド名を変えたら、対応するテンプレートも変える」** という強い結びつき。
　 → だから **同時に作る** のが正解。

### バリデーションエラーの表示

`fragments/layout.html` の `fieldError` フラグメントを使って：

```html
<div th:replace="~{fragments/layout :: fieldError('title')}"></div>
```

これで、title のエラーがあれば赤字で表示される（無ければ何も出ない）。
全フォームで **同じ書き方** ができるよう、共通フラグメント化している。

### `th:object` と `th:action` の役割

| 属性          | 役割                                                  |
| ------------- | ----------------------------------------------------- |
| `th:object`   | このフォームが扱う Form オブジェクト（モデル属性名）  |
| `th:action`   | 送信先URL（`@{...}` で contextパスを意識せず書ける）  |
| `th:field`    | object内のフィールドへの bind（name/id/value 自動生成） |
| `th:errors`   | 該当フィールドのエラーメッセージ                      |

---

## 🎨 5-13. CSS（Tailwind）の話

- 本研修では **Tailwind CSS の CDN** を `fragments/layout.html` の `<head>` で読み込んでいる
- 本物のプロジェクトでは **ビルドして本番用CSSを生成** するが、学習中はCDNで十分
- HTMLに直接 `class="..."` を書くスタイル（ユーティリティファースト）

> 💡 本番化の話は研修の本筋ではないので、ここでは触れない。

---

## ❌ 5-14. 初心者がやりがちなNG

| NGパターン                                              | 何が起きる？                                          |
| ------------------------------------------------------- | ----------------------------------------------------- |
| Entity を画面の `th:object` にする                      | id や createdAt が改ざんされる／設計が崩れる          |
| `@NotBlank` を Long / 数値型に付ける                    | コンパイル時にはOKだが動かない・誤解の元              |
| 任意項目に `@NotBlank` を付ける                         | 空のままで送ると毎回エラーになり、画面が使えなくなる   |
| Form の `categoryId` を `Category category` にする      | バインディングが複雑になる／画面側で扱いづらい        |
| パスワードの「8文字以上」を Entity 側に書く             | DBの責務を超えてしまう（業務ルールはForm/Service）    |
| `th:field` の中身と Form のフィールド名がズレる         | 値が反映されない／エラーになる（よくあるバグ）        |
| バリデーションメッセージを全部 "入力してください" にする| ユーザーがどこを直せばいいか分からない                |

---

## 📝 5-15. ソースを書くときの順番（実演）

実際に Form 5つ＋テンプレート5つを書いた **頭の動き** の順番：

1. **CategoryForm** から書く（最小形 / register-edit 兼用）
2. **BlogRegisterForm** に進む（複数の制約を組み合わせる練習）
3. **BlogEditForm** を書く（`id` を持たせて register と分けるパターン）
4. **UserRegisterForm** で `@Email` を覚える
5. **LoginForm** で Spring Security 入口の最小形を学ぶ
6. **テンプレート（fragments/layout.html）** を先に作って共通部品を用意
7. 各 Form に対応するテンプレートを順に作る
   - `auth/login.html` ← `LoginForm`
   - `user/register.html` ← `UserRegisterForm`
   - `admin/blog/form.html` ← `BlogRegisterForm` / `BlogEditForm`（兼用）
   - `admin/category/form.html` ← `CategoryForm`

👉 **Java側を先に確定 → テンプレートで紐づける** がベストな順序。
　 逆に書くとフィールド名がブレやすい。

---

## ✨ 5-16. ソースを書くときのポイント（必読）

### ポイント①：**Form と Entity は別物。混ぜない**

「画面の都合」と「DBの都合」は別。
**Form = 画面寄り、Entity = DB寄り**。

### ポイント②：**Form は ID（Long）で関連を持つ。Entity（Category）を持たない**

`categoryId: Long` と書く。
Service が Repository を使って Entity に変換する。

### ポイント③：**`@NotBlank` は String、`@NotNull` は数値・参照型**

文字列なら `@NotBlank` 一択。
数値・参照型なら `@NotNull`。
混ぜると動かない／意図がぼやける。

### ポイント④：**任意項目に `@NotBlank` を付けない**

`imageUrl` のように任意なら `@Size` だけにする。

### ポイント⑤：**「業務ルール」は Form / Service へ。「DB制約」は Entity / DB へ**

パスワードの最小文字数のような業務ルールは Form。
NOT NULL / UNIQUE のような整合性は Entity / DB。

### ポイント⑥：**`th:field="*{xxx}"` の中身は Form のフィールド名と完全一致**

ズレるとバインドされない・エラーになる。
→ 「Form 側を先に確定」「テンプレート側で参照」が安全。

### ポイント⑦：**Register と Edit を分けるかは「責務が違うか」で判断**

完全に同じならまとめてOK。
`id` の有無や、編集時だけ必要なフィールドがあれば分ける。

### ポイント⑧：**エラー表示は共通フラグメント化する**

毎フォームで `th:errors` を書き散らかさない。
`fragments/layout :: fieldError(...)` のように共通化する。

---

## ✅ 5-17. 第5章まとめ（完全理解）

✔ Form = **「画面の入力1件」を表す Java クラス**
✔ Entity と Form は **絶対に混ぜない**（責務が違う）
✔ Form は **`@Getter` / `@Setter` / `@NoArgsConstructor`** が必須
✔ String には **`@NotBlank`**、数値／参照型には **`@NotNull`**
✔ Form は **`Long categoryId`** で関連を持つ（Entity は持たない）
✔ Register と Edit を分けるかは **「責務が違うか」** で判断
✔ テンプレートの **`th:field="*{xxx}"`** は Form のフィールド名と一致させる
✔ エラー表示は **共通フラグメント** にまとめる

---

## 🔜 次の章

**第6章：Service を作る**
― 業務ロジックの中心。**Repository + Form の橋渡し** ―

次は **業務ロジック層** を作ります。
ここで初めて：

- Form → Entity への **詰め替え**
- パスワードの **ハッシュ化**（PasswordEncoder）
- カテゴリー削除前の **使用件数チェック**
- `findByEmail` で取った `Optional<User>` を **`.orElseThrow(...)`** で取り出す

など、今までの章で仕込んだピースが **全部つながり始めます**。
