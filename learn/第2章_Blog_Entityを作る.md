# 📘 第2章

## Blog Entity を作る
― **外部キー（FK）を Java でどう表すか** を完全理解する ―

---

## 🎯 2-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ なぜ `Category` の次に `Blog` を作るのか **順番の理由** を説明できる
✔ 外部キー `category_id` を **`@ManyToOne` + `@JoinColumn`** で表現できる
✔ `FetchType.LAZY` と `FetchType.EAGER` の違いを説明できる
✔ `TEXT` 型を `@Column(columnDefinition = "TEXT")` で作る理由が分かる
✔ **公開日時 / 作成日時 / 更新日時** の3つの違いを説明できる
✔ `image_url` を nullable にしている理由を説明できる

👉 この章で **「関連（リレーション）」** が腹落ちすると、JPA がぐっと楽になります。

---

## 🧩 2-2. Blog Entity の立ち位置

```text
categories  1 ────── 多  blogs
   ↑                       │
   │                category_id（外部キー）
   │
   └── Category Entity（第1章で作成済み）
```

つまり Blog Entity は：

- 自分自身のカラム（title / content / image_url / 日時系）に加えて
- **「どの Category に属するか」** を表す **外部キー** を持つ

👉 ここが第1章との一番大きな違いです。

---

## 🔄 2-3. なぜ `Category` の次に `Blog` を書くのか？

### 鉄則（第1章のおさらい）

> **「参照される側 → 参照する側」** の順で作る

### 具体的に何が起きるか？

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id", nullable = false)
private Category category;   // ← 「Category」クラスを使う！
```

- `Blog` クラスの中に `private Category category;` が出てくる
- → `Category` クラスが **先に存在していないとコンパイルできない**
- → だから **Category → Blog の順** で作る

### 一言メモ

> 「使う側より、使われる側を先に作る」。
> プログラミング全般の鉄則。

---

## 🧠 2-4. Blog クラス全体像

完成したファイル：[Blog.java](../src/main/java/com/meitaku/blog/entity/Blog.java)

このクラスは大きく分けて **5ブロック** でできています。

```text
①  @Entity / @Table         ← 「これはテーブルだよ」宣言ブロック
②  @Id / @GeneratedValue    ← 「主キーはこれ」宣言ブロック
③  @Column を持つフィールド ← 「素のカラム定義」ブロック
④  @ManyToOne / @JoinColumn ← ★ 新登場：「外部キー」ブロック
⑤  @PrePersist / @PreUpdate ← 「日時を自動で入れる」仕掛けブロック
```

👉 ①②③⑤ は第1章とほぼ同じ。**新しいのは ④ だけ**。

---

## 🆕 2-5. ★ 新登場 ★ `@ManyToOne` と `@JoinColumn`（超重要）

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id", nullable = false)
private Category category;
```

### なぜ `Long categoryId` ではなく `Category category` なのか？

❌ **やりがちな書き方（FKをただの数字として持つ）**

```java
@Column(name = "category_id")
private Long categoryId;   // ← ただの数字。Categoryの情報は取れない
```

⭕ **JPA らしい書き方（オブジェクトとして持つ）**

```java
@ManyToOne
@JoinColumn(name = "category_id", nullable = false)
private Category category;   // ← .getName() などが直接呼べる！
```

👉 後者なら、画面で

```html
<span th:text="${blog.category.name}">カテゴリー名</span>
```

のように **オブジェクトのドットでつなげる** ことができる。これが JPA の威力。

### `@ManyToOne` の意味を分解

| 部分                    | 意味                                                      |
| ----------------------- | --------------------------------------------------------- |
| `@ManyToOne`            | 「**多 対 1**」の関連です、と宣言（Blog 多 ─ 1 Category） |
| `fetch = FetchType.LAZY`| Category は **必要になるまで読み込まない** という指示     |

### `@JoinColumn` の意味を分解

| 属性                | 意味                                                |
| ------------------- | --------------------------------------------------- |
| `name = "category_id"` | DB上の **FKカラム名** を指定                     |
| `nullable = false`  | FKは必須（NOT NULL）。カテゴリー無しのブログは無い  |

### 補足：「多対1」ってどっちが多？

```text
Blog ── Category  の関係を、Blog 側から見ると：

   私（1つのBlog）から見ると、Categoryは1つ
   でも Category側から見ると、その Category を使う Blog は たくさんある
   → だから Blog 側のアノテーションは「多→1」 = @ManyToOne
```

👉 **「自分から見て」相手が1つなら ManyToOne。**

---

## 🌀 2-6. `FetchType.LAZY` と `FetchType.EAGER` の違い（超重要）

```java
@ManyToOne(fetch = FetchType.LAZY)   // ⭕ 推奨
@ManyToOne(fetch = FetchType.EAGER)  // ❌ 基本使わない
```

| 種類    | 動き                                              | デメリット                                   |
| ------- | ------------------------------------------------- | -------------------------------------------- |
| `LAZY`  | Blog を取った時は category は **まだ取らない**     | 後で `.category.getName()` した時に追加SQL   |
| `EAGER` | Blog を取ると **必ず Category も同時に取る**       | 一覧取得で **N+1問題** が爆発しやすい        |

### N+1 問題（業務でよく刺さる落とし穴）

`EAGER` で Blog 100件取ると：

```sql
SELECT * FROM blogs;                  -- 1回
SELECT * FROM categories WHERE id=1;  -- 100回
SELECT * FROM categories WHERE id=2;
...
```

👉 SQLが **101回** 飛ぶ。100件で気づかなくても、10000件で死ぬ。

### じゃあ LAZY の落とし穴は？

LAZY だと、**画面表示の時に DBコネクションが閉じていると例外**：

```
LazyInitializationException: could not initialize proxy ...
```

👉 対処は **`@Transactional` を Service に付ける** か、**フェッチジョイン** を使う。
→ これは第6章（Service）と第4章（Repository）で扱います。

### この章での結論

> **LAZY を基本にする**。
> 例外が出たら「View まで Transaction が届いていない」と疑う。

---

## 📄 2-7. `TEXT` 型のカラム（content）

```java
@Column(name = "content", nullable = false, columnDefinition = "TEXT")
private String content;
```

### なぜ `columnDefinition = "TEXT"` を書くのか？

- `String` のデフォルトは **`VARCHAR(255)`**
- ブログ本文は **255文字を簡単に超える**
- → DB側で **`TEXT` 型（長さ制限なし）** にしたい
- → JPA に「このカラムは TEXT で作って」と直接指示する

### 代替の書き方（参考）

```java
@Lob
@Column(name = "content", nullable = false)
private String content;
```

`@Lob` でも長文を扱えますが、PostgreSQL では **CLOB（巨大オブジェクト）** として作られることがあり扱いづらい。
👉 **PostgreSQL では `columnDefinition = "TEXT"` が一番素直**。

### NG例

```java
@Column(name = "content", nullable = false)   // ❌ 自動で VARCHAR(255)
private String content;
```

→ 長文を保存しようとすると DB エラー。**本文系は必ず TEXT** と覚える。

---

## 🖼 2-8. `image_url` を nullable にしている理由

```java
@Column(name = "image_url", length = 500)
private String imageUrl;
```

- `nullable = false` を **書いていない** → NULL 許可
- テーブル定義書でも `NOT NULL` 欄が `-`（NOT NULL ではない）

### なぜ任意項目にするのか？

- 「画像を後から付ける」運用がよくある
- 「URLが切れていても本文は読みたい」という要件
- → **画像は無くてもブログは成立する**

👉 **NOT NULL を闇雲に付けない**。要件に従う。

### 補足：カラム名の変換

| Java側         | DB側           |
| -------------- | -------------- |
| `imageUrl`     | `image_url`    |

- JPA は **キャメルケース → スネークケース** を自動変換できる設定もある
- ただし本研修では `@Column(name = "image_url")` と **明示的に書く**
- → 読み手がDBを開かなくてもカラム名がわかる

---

## ⏰ 2-9. 3つの「日時」の違い（超重要）

Blog には日時カラムが **3つ** 出てきます。混同しないこと。

| カラム         | 何の日時？                                  | 誰がセット？           |
| -------------- | ------------------------------------------- | ---------------------- |
| `published_at` | 画面に出す **公開日時**（編集可）           | ユーザー（画面で指定） |
| `created_at`   | DBに **レコードを作った日時**（変えない）   | システム（自動）       |
| `updated_at`   | DBの **レコードを最後に更新した日時**       | システム（自動）       |

### `@PrePersist` の中身

```java
@PrePersist
protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    if (this.publishedAt == null) {     // ← ★ ここがポイント
        this.publishedAt = now;
    }
    this.createdAt = now;
    this.updatedAt = now;
}
```

### `publishedAt` だけ `if (== null)` で囲んでいる理由

- `publishedAt` は **画面から指定される可能性がある**
- 上書きしてしまうと、ユーザーが入れた値が消える
- だから **「指定が無ければデフォルトとして今を入れる」** にする

👉 一方 `createdAt` / `updatedAt` は **常にシステムが上書き** でOK。

### NG例

```java
// ❌ 公開日時まで強制上書きしてしまう
@PrePersist
protected void onCreate() {
    this.publishedAt = LocalDateTime.now();   // ← ユーザー指定が消える
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}
```

---

## ❌ 2-10. 初心者がやりがちなNG

| NGパターン                                                       | 何が起きる？                                     |
| ---------------------------------------------------------------- | ------------------------------------------------ |
| FKを `Long categoryId` だけで持つ                                | `.category.getName()` のような関連参照ができない |
| `@ManyToOne` を `EAGER` のまま放置                               | N+1問題で本番DBが死ぬ                            |
| 本文を `String` だけで宣言（`columnDefinition` なし）            | 長文で `value too long` エラー                   |
| `publishedAt` を `@PrePersist` で強制上書きする                   | 画面で指定した日時が無視される                   |
| `category` フィールドを `nullable = true` のままにする           | カテゴリー無しのブログが作れて整合性が崩れる     |
| Entity に「公開できるか？」のような業務判定メソッドを書く        | 責務がぼやけてテストが書きづらい                 |

---

## 🆚 2-11. Entity と Form の違い（再掲・先取り）

| 種類   | このクラス（例）          | 役割                                    |
| ------ | ------------------------- | --------------------------------------- |
| Entity | `Blog`                    | DB の1行を表す（保存・取得用）          |
| Form   | `BlogRegisterForm` 等     | 画面の入力1件を表す（バリデーション付き）|

例えば「タイトルを200文字以内にする」は **両方** に書く：

- Entity 側：`@Column(length = 200)` → **DBレベルでの制約**
- Form 側：`@Size(max = 200)` → **画面レベルでの制約**

👉 Entity と Form は **「似てるけど別物」**。第5章で混乱しないように要注意。

---

## 📝 2-12. ソースを書くときの順番（実演）

実際に Blog.java を書いた **頭の動き** の順番：

1. **空のクラス** → `public class Blog {}`
2. **テーブル宣言** → `@Entity` / `@Table(name = "blogs")`
3. **主キー** → `id` + `@Id` + `@GeneratedValue`
4. **普通のカラム（上から順に写す）** → `title` → `content` → `imageUrl`
5. **★ 外部キー** → `category` を **`@ManyToOne` + `@JoinColumn`** で書く
6. **日時系** → `publishedAt` → `createdAt` → `updatedAt`
7. **自動セット仕掛け** → `@PrePersist` / `@PreUpdate`
8. **Lombok** → `@Getter` / `@Setter` / `@NoArgsConstructor`

👉 **第1章と違うのは「5」だけ**。あとは同じリズム。

---

## ✨ 2-13. ソースを書くときのポイント（必読）

### ポイント①：**外部キーは「数字」じゃなく「オブジェクト」で持つ**

```java
private Category category;   // ⭕
// ❌ private Long categoryId;
```

JPA を使う最大のメリットは **「オブジェクトのドットで関連を辿れる」** こと。
ここを `Long` で持ってしまうと、JPAを使う意味が半減する。

### ポイント②：**`@ManyToOne` は LAZY** を基本にする

`EAGER` は手軽だが、件数が増えた瞬間に **N+1 で死ぬ**。
**最初から LAZY** にして、必要な所で `@Transactional` を付ける癖を。

### ポイント③：**長文は `columnDefinition = "TEXT"`**

`String` 型のデフォルトは `VARCHAR(255)`。
ブログ本文・説明文・コメント等は **必ず TEXT** に。

### ポイント④：**「日時」は意味で分けて考える**

- 公開日時（ユーザーが決める） → `publishedAt`
- 作成日時（システムが決める／不変） → `createdAt`
- 更新日時（システムが決める／毎回更新） → `updatedAt`

**3つの違いを書ける人が、業務で日時バグを生まない**。

### ポイント⑤：**NOT NULL は要件に従う**

`image_url` のように **本当に任意なら NOT NULL を付けない**。
「とりあえず NOT NULL」は後で必ず困る。

### ポイント⑥：**カラム名は `@Column(name = ...)` で明示**

JPA の自動変換に頼らず、**書いて明示** する。
DBを開かなくてもファイル1つでカラム構造が分かる状態にする。

### ポイント⑦：**Entity に判定ロジックを書かない**

「このブログは下書き状態か？」「削除できるか？」は **Service の仕事**。
Entity は **「形」だけ**。

---

## ✅ 2-14. 第2章まとめ（完全理解）

✔ Blog は **Category を参照する** ので、Category の **後** に作る
✔ 外部キーは **`@ManyToOne` + `@JoinColumn`** で **オブジェクト** として持つ
✔ `FetchType.LAZY` を基本にする（`EAGER` は N+1 の温床）
✔ 本文系は **`columnDefinition = "TEXT"`** で長文対応
✔ `image_url` は **要件に従って nullable**
✔ `publishedAt` / `createdAt` / `updatedAt` は **意味が違う**
✔ `publishedAt` の `@PrePersist` は **「null のときだけ」** セットする
✔ Entity に **業務ロジックを書かない**

---

## 🔜 次の章

**第3章：User Entity を作る**
― 管理者ログイン用テーブル `admin_users` を Entity 化 ―

次は **管理者ユーザー** を扱います。
ここでは **「パスワードをそのまま保存しない」** という Spring Security との接点も先取りで触れます。
