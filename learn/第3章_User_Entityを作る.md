# 📘 第3章

## User Entity を作る
― **クラス名とテーブル名が違う** ／ **パスワードの扱い** を最初に身につける ―

---

## 🎯 3-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ なぜクラス名 `User` ／ テーブル名 `admin_users` のように **食い違わせる** ことがあるか説明できる
✔ `@Table(name = "...")` がこの場面で **絶対に必要** な理由を説明できる
✔ `email` を **`unique = true`** にする理由を説明できる
✔ `password` カラムを **`VARCHAR(255)`** にする本当の理由を説明できる
✔ **「パスワードを生のまま保存しない」** という鉄則を説明できる
✔ 「ハッシュ化はどの層で行うべきか？」を答えられる

👉 ここで **「Entity と Security の境界線」** を最初に引いておくと、後で楽になります。

---

## 🧩 3-2. User Entity の立ち位置

```text
admin_users
   ├── 管理者のログイン情報
   ├── 画面：ログイン / 管理者登録 / 管理者編集 / プロフィール
   └── 他のテーブルとの外部キー関係：なし（独立）
```

👉 **Category と同じく独立Entity**（FKを持たない／持たれない）。
　 だから Blog の前でも後でも作れるが、研修順としては **3番目** に作る。

---

## 🔄 3-3. なぜ 1→2→3 の順で `User` が **最後** なのか？

### 順番の根拠（再掲）

| 順番 | 作るもの   | 理由                              |
| ---: | ---------- | --------------------------------- |
|    1 | `Category` | 誰からも依存されていない（独立）  |
|    2 | `Blog`     | Category に依存している（FKあり） |
|    3 | `User`     | 他のテーブルと関係なし（独立）    |

### 「独立Entityが2つあるとき」のルール

- どちらから作ってもコンパイルは通る
- だが研修・ドキュメントの読み手にとっては **「業務上の主役 → 脇役」の順** が読みやすい
- このシステムの主役は **ブログ記事（Blog）**
- 管理者は「Blogを操作する人」＝ **脇役**
- → だから **Blog の後** に User

👉 **「業務として目立つもの → 裏方」** の順で作ると資料も整いやすい。

---

## 🧠 3-4. User クラス全体像

完成したファイル：[User.java](../src/main/java/com/meitaku/blog/entity/User.java)

構造は **第1章の Category とほぼ同じ**。違いは：

| 項目                          | Category                  | User                                  |
| ----------------------------- | ------------------------- | ------------------------------------- |
| クラス名 vs テーブル名        | `Category` ⇔ `categories` | **`User` ⇔ `admin_users`**            |
| UNIQUE カラム                 | `name`                    | **`email`**                           |
| 長文／暗号化考慮カラム        | なし                      | **`password`（BCryptハッシュ）**      |

👉 **新登場は実質「2-1. クラス名とテーブル名の不一致」と「2-2. パスワード扱い」の2点**。

---

## 🏷 3-5. ★ ポイント① ★ クラス名とテーブル名が違うときの `@Table`

```java
@Entity
@Table(name = "admin_users")   // ← ★ クラス名と全然違うので絶対に明示
public class User { ... }
```

### なぜ `User` というクラス名にするのか？

- アプリ内では「ログインする人 = User」が **一般的な呼び方**
- Spring Security の世界も `User` / `UserDetails` という用語で統一されている
- 一方DBの世界では `user` は **PostgreSQL の予約語** で危ない → `admin_users` と複数形＋プレフィックス
- → **Javaの世界とDBの世界で別の名前** を使うのが現実的

### `@Table` を **省略するとどうなるか**

```java
@Entity   // ← @Tableを書かない
public class User { ... }
```

- JPA が推測するテーブル名は **`user`**
- DBには `user` テーブルなんて無い
- → 起動時にエラー or 別テーブルを勝手に作ろうとして失敗

👉 **「クラス名 ≠ テーブル名」のときは @Table が無いと動かない**、と覚える。

### 命名のすれ違いを記録するためのコメント

実コードでは、こうしたすれ違いに **必ずコメント** を残すと親切：

```java
@Table(name = "admin_users")   // クラス名 User と異なるので明示
```

---

## 📧 3-6. ★ ポイント② ★ `email` の `unique = true`

```java
@Column(name = "email", nullable = false, unique = true, length = 255)
private String email;
```

### なぜ `email` を UNIQUE にするのか？

- このアプリでは **email がログインIDの代わり** になっている
- → 同じemailのユーザーが2人いると、**どっちでログインしてるか分からなくなる**
- → DBの **UNIQUE 制約** で「同じemailは絶対1件」と保証する

### Service / Form 側との関係（先取り）

| 層        | やること                                                    |
| --------- | ----------------------------------------------------------- |
| Form      | 「メール形式か？」を `@Email` でチェック（次章以降）        |
| Service   | 「既に登録済みかどうか？」を確認（事前チェック）             |
| **Entity（ここ）** | **「DBで絶対に重複させない」最後の砦** として `unique = true` |

👉 **3段構えで重複を防ぐ**。Entity の UNIQUE は最後の安全網。

### なぜ Service だけのチェックではダメ？

- 同じ瞬間に2人が同じemailで登録ボタンを押すと、Service のチェックを **両方すり抜ける**
- → DB側で UNIQUE を持っていないと、両方INSERTが成功してしまう
- → だから **「DBの UNIQUE 制約」は必ず必要**

---

## 🔒 3-7. ★ ポイント③ ★ `password` のカラム設計

```java
@Column(name = "password", nullable = false, length = 255)
private String password;
```

### `length = 255` の意味

- 画面で入力される **生のパスワードは8〜20文字程度**
- だったら `length = 30` でいいんじゃない？　**NO。**
- DBには **「BCryptで暗号化されたハッシュ文字列（約60文字）」** が入る
- → 余裕を見て **`255`**（VARCHARの定番サイズ）にする

### 「ハッシュ文字列」って何？

BCryptで暗号化すると、生パスワードがこんな文字列に変換される：

```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

- どんな短い元パスワードでも、出力は **常に約60文字**
- 同じ元パスワードでも、毎回 **違う文字列が出る**（saltが混ざる）
- **元に戻せない**（一方向ハッシュ）

### 鉄則：パスワードは Entity に「生のまま」入れない

```java
// ❌ 絶対NG
user.setPassword("password");
userRepository.save(user);

// ⭕ 正しい：Service層で「ハッシュに変換してから」 setPassword
String hashed = passwordEncoder.encode(rawPassword);
user.setPassword(hashed);
userRepository.save(user);
```

👉 **生パスワードの状態で `setPassword(...)` を呼んではいけない**。
　 これが「パスワードを生で保存しない」の実装上の意味。

---

## 🧭 3-8. ハッシュ化は **どの層** の仕事か？（重要）

| 層         | パスワードに対する責務                                  |
| ---------- | ------------------------------------------------------- |
| Controller | フォームから受け取る                                    |
| **Service**| **`PasswordEncoder.encode(...)` でハッシュ化する**      |
| Repository | DBに保存・取得するだけ                                  |
| **Entity** | **「ハッシュ文字列を入れる String 欄」を提供するだけ**  |

### Entity が **やってはいけない** こと

```java
// ❌ Entity の setter でハッシュ化してしまう
public void setPassword(String raw) {
    this.password = new BCryptPasswordEncoder().encode(raw);
}
```

- Entity に Spring Security の依存が紛れる
- テストしづらい
- 「既にハッシュ済みの値をDBから読んだ時」に二重ハッシュになる事故

👉 **Entity は「形」だけ。ハッシュ化のような「動き」は Service の責任**。
　 → これが PDF 第3章のいう「層の責務分担」そのもの。

---

## ❌ 3-9. 初心者がやりがちなNG

| NGパターン                                                  | 何が起きる？                                       |
| ----------------------------------------------------------- | -------------------------------------------------- |
| `@Table(name = "admin_users")` を忘れる                     | `user` テーブルを探しに行って起動失敗               |
| `email` を UNIQUE にしない                                  | 同じemailで複数アカウント作成可能になる             |
| `password` の `length` を 30 など短くする                   | BCryptハッシュが入りきらず INSERT エラー            |
| 生パスワードを `setPassword(rawPassword)` してDB保存        | **重大なセキュリティ事故**（漏えいで全員危険）      |
| Entity の `setPassword` 内でハッシュ化する                  | 二重ハッシュ・テスト困難・責務がぼやける            |
| ユーザー一覧画面に `user.getPassword()` を表示する          | ハッシュとはいえ表示する理由がない／監査NG          |

👉 **「Entity に生パスワードを触らせない」「ハッシュ化はService」** をセットで覚える。

---

## 🆚 3-10. Entity と Spring Security `UserDetails` の違い（先取り）

| 種類             | 役割                                                          |
| ---------------- | ------------------------------------------------------------- |
| Entity `User`    | **DBのadmin_users 1行** を表す純粋なJPAクラス                 |
| `UserDetails`    | Spring Security が認証に使う **「ログイン中のユーザー」の型** |

両者を **同じクラスに混ぜない** のが今の主流。

```text
[ DB ] ── User(Entity) ── 変換 ──→ UserDetails ── Security
```

👉 ここでは **Entity だけを作る**。`UserDetails` への変換は後の章（Service / SecurityConfig）。

---

## 📝 3-11. ソースを書くときの順番（実演）

実際に User.java を書いた **頭の動き** の順番：

1. **空のクラス** → `public class User {}`
2. **「これはテーブル」宣言** → `@Entity` ＋ **`@Table(name = "admin_users")`** ← 名前ズレに注意
3. **主キー** → `id` + `@Id` + `@GeneratedValue`
4. **業務カラム（上から順）** → `name` → `email`（UNIQUE!） → `password`（length=255）
5. **日時系** → `createdAt` / `updatedAt`
6. **自動セット仕掛け** → `@PrePersist` / `@PreUpdate`
7. **Lombok** → `@Getter` / `@Setter` / `@NoArgsConstructor`

👉 構造は Category とほぼ同じ。**意識するのは `@Table` と `email` UNIQUE と `password` の length**。

---

## ✨ 3-12. ソースを書くときのポイント（必読）

### ポイント①：**クラス名とテーブル名のズレは @Table で吸収**

`User` クラスと `admin_users` テーブルは命名思想が違う。
**読み手が混乱しないようにコメント** も添える。

### ポイント②：**ログインIDになるカラムは必ず UNIQUE**

`email` でログインするなら `unique = true`。
**「Serviceでチェックすればいい」と思っても、競合（同時登録）でDBが守ってくれる**。

### ポイント③：**パスワードカラムは VARCHAR(255)**

BCryptハッシュ（約60文字）を入れる前提。
「ユーザー入力の長さ」で決めない。**「DBに入る値の長さ」で決める**。

### ポイント④：**ハッシュ化は Service の仕事、Entity の仕事ではない**

```text
Service  : 生 → ハッシュに変換して setPassword
Entity   : 受け取ったハッシュをそのまま DB に渡すだけ
```

責務を混ぜないこと。Entity に Spring Security を import したら **既におかしい**。

### ポイント⑤：**Entity に「ログインできるか？」のような業務判定を書かない**

「停止中ユーザーか？」「最終ログインから30日経ったか？」のような判定は **Service の仕事**。
Entity は **テーブルの形だけ**。

### ポイント⑥：**画面表示時に `password` を出さない**

ハッシュとはいえ、画面・ログ・API レスポンスに `password` を出すクセを最初から付けない。
（Thymeleaf テンプレートでは `user.email` / `user.name` まで。）

---

## ✅ 3-13. 第3章まとめ（完全理解）

✔ User Entity は **`admin_users` テーブル** に対応する
✔ クラス名とテーブル名がズレる場合は **`@Table(name = ...)` 必須**
✔ ログインIDになる `email` は **`unique = true`** で重複防止
✔ `password` カラムは **`VARCHAR(255)`**（BCryptハッシュ用）
✔ ハッシュ化は **Service の仕事**。Entity でやってはいけない
✔ Entity は **「形」だけ**。Spring Security の知識は持ち込まない

---

## 🔜 次の章

**第4章：Repository を作る**
― 添付PDFのテーマ。`CategoryRepository` / `BlogRepository` / `UserRepository` の3兄弟 ―

次は **DB操作を担当する層** を作ります。
PDFで学んだ `JpaRepository<Entity, ID>` を、いよいよ **3つの Entity に対して** 書いていきます。
ここで `findByEmail` のような **メソッド名規約** が活躍します。
