# 📘 第1章

## Category Entity を作る
― 「テーブル＝クラス」を最初に体で覚える ―

---

## 🎯 1-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ Entity の役割を **一文で説明できる**
✔ なぜ最初に作るのが **Category なのか** 説明できる
✔ `@Entity` / `@Table` / `@Id` / `@GeneratedValue` / `@Column` の意味を分解して説明できる
✔ `@PrePersist` / `@PreUpdate` で **created_at / updated_at が自動で入る理由** を説明できる
✔ JPA が **「引数なしコンストラクタ」を要求する理由** を説明できる

👉 この章が理解できると「クラスとテーブルがつながる感覚」が一気に掴めます。

---

## 🧩 1-2. Entity とは何か？（超重要）

### 一言で言うと

> Entity =
> **「DBのテーブル1行」を Java のクラスとして表したもの**

### Entity の立ち位置（再確認）

```text
[ Controller ]
     ↓
[  Service  ]   ← 業務の流れ・判断
     ↓
[ Repository ]  ← DB操作だけ
     ↓
[  Entity  ]    ← ★ ここ：テーブル1行 = クラス1個
     ↓
[ Database ]
```

👉 **Entity は「形」だけを持つ。動きは持たない。**

---

## 🔄 1-3. なぜ「最初に Category から書く」のか？

### テーブル間の関係を見てみる

```text
categories  1 ────── 多  blogs
                       ↑
              category_id（外部キー）
```

つまり、

- **blogs テーブルは categories(id) を参照している**
- → `Blog` クラスを書くには、先に `Category` クラスが存在しないと困る
- → **参照される側から作る** のが原則

### 「依存される側 → 依存する側」の順で作る

| 順番 | 作るもの        | 理由                              |
| ---: | --------------- | --------------------------------- |
|    1 | `Category`      | 誰からも依存されていない（独立）  |
|    2 | `Blog`          | Category に依存している（FKあり） |
|    3 | `User`（admin） | 他のテーブルと関係なし（独立）    |

👉 この順番を守ると **コンパイルエラーが出ない／IDEの補完が効く**。

### 一言メモ

> 「Aが B を import する関係」になるものは、**B を先に作る**。
> プログラム全般に通じる超大事な順番感覚です。

---

## 🧠 1-4. Category クラス全体像

完成したファイル：[Category.java](../src/main/java/com/meitaku/blog/entity/Category.java)

このクラスは大きく分けて **4ブロック** でできています。

```text
①  @Entity / @Table         ← 「これはテーブルだよ」宣言ブロック
②  @Id / @GeneratedValue    ← 「主キーはこれ」宣言ブロック
③  @Column を持つフィールド ← 「カラム定義」ブロック
④  @PrePersist / @PreUpdate ← 「日時を自動で入れる」仕掛けブロック
```

👇 1つずつ分解していきます。

---

## 🧱 1-5. ブロック① `@Entity` と `@Table` の意味

```java
@Entity
@Table(name = "categories")
public class Category {
```

### 分解して読む

| 部分                         | 意味                                                 |
| ---------------------------- | ---------------------------------------------------- |
| `@Entity`                    | **このクラスは JPA エンティティです** と Spring に宣言 |
| `@Table(name = "categories")` | マッピングする **テーブル名** を明示                  |

### なぜ `@Table` を付けるのか？

JPA はクラス名から自動でテーブル名を推測しますが、

- クラス名: `Category`
- 推測されるテーブル名: `category`（単数）
- 実際のテーブル名: `categories`（複数）

👉 **クラス名とテーブル名が違うときは必ず `@Table(name = "...")` を書く**。

### ポイント
- `@Entity` がないと、Spring から **「ただのクラス」扱い** され DBにつながらない。
- `@Table` を省略すると 「クラス名 = テーブル名」 とみなされる。

---

## 🆔 1-6. ブロック② 主キー `@Id` / `@GeneratedValue`

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id")
private Long id;
```

### `@Id` の役割

> **「このフィールドがテーブルの PRIMARY KEY ですよ」** という宣言。

Entity に必ず **1つは必要** です。
（無いと Spring 起動時にエラーになる）

### `@GeneratedValue(strategy = GenerationType.IDENTITY)` の意味

| 戦略       | 意味                              | 相性のいいDB        |
| ---------- | --------------------------------- | ------------------- |
| `IDENTITY` | **DB側で自動採番**してもらう       | PostgreSQL の BIGSERIAL / MySQL の AUTO_INCREMENT |
| `SEQUENCE` | DBのシーケンス機能を使う          | Oracle / PostgreSQL |
| `AUTO`     | JPAにおまかせ                     | （非推奨：DBが変わると挙動が変わる）|

👉 今回のテーブル定義書は `id BIGSERIAL PRIMARY KEY` → **IDENTITY 一択**。

### なぜ型は `Long` なのか？

- DBの型: `BIGSERIAL`（= 8バイト整数）
- 対応する Java の型: **`Long`**（`long` でなく **ラッパー型の `Long`**）

### なぜ `long` ではなく `Long` なのか？（超重要）

```java
private Long id;   // ⭕ こっち
private long id;   // ❌ こっちはダメ
```

- 新規作成時は **`id` がまだ存在しない（null）** 状態
- `long` は基本型なので **null を入れられない**（自動で 0 になる）
- すると JPA が「ID=0 のレコード」と勘違いして INSERT が UPDATE になる事故が起きる

👉 **主キーは必ずラッパー型（`Long`）にする。**

---

## 📦 1-7. ブロック③ `@Column` の細かい指定

```java
@Column(name = "name", nullable = false, unique = true, length = 100)
private String name;
```

### `@Column` の引数を分解

| 属性           | 意味                                    | テーブル定義書のどこ？ |
| -------------- | --------------------------------------- | --------------------- |
| `name`         | DBカラム名                              | 物理名 `name`          |
| `nullable`     | NULLを許可するか（`false`=NOT NULL）    | NOT NULL 列            |
| `unique`       | 一意制約を付けるか                      | UNIQUE 列              |
| `length`       | VARCHAR の最大文字数                    | 桁数 100               |
| `updatable`    | UPDATE時に書き換えを許可するか          | created_at に使用      |

### なぜ `@Column(name = "...")` を毎回書くのか？

JPA はフィールド名 `userName` を `user_name` のように自動変換できますが、
**「明示的に書く方が読み手にやさしい」** ので、研修ではすべて明示します。

### NG例（やりがち）

```java
@Column
private String name;   // ← 制約が一切無い！ NULLも入る
```

👉 **制約は Entity に書くのが基本。DBだけに任せない。**

---

## ⏰ 1-8. ブロック④ `@PrePersist` / `@PreUpdate`（自動日時セット）

```java
@PrePersist
protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
}

@PreUpdate
protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}
```

### 何が起きているのか？

| アノテーション | 呼ばれるタイミング | やっていること                        |
| -------------- | ------------------ | ------------------------------------- |
| `@PrePersist`  | INSERT直前         | `created_at` と `updated_at` をセット |
| `@PreUpdate`   | UPDATE直前         | `updated_at` だけを上書き             |

👉 これがあるので **Service側で `setCreatedAt(...)` を書く必要が無くなる**。

### `created_at` には `updatable = false` を付けた理由

```java
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;
```

- 作成日時は **「最初の1回だけ書き込む」** べき情報
- 後から UPDATE で書き換えられないように **`updatable = false`** をつける
- これで万が一 `setCreatedAt(...)` を呼んでも DB は反応しない（安全）

### NG例

```java
// ❌ Service側でいちいち書く
blog.setCreatedAt(LocalDateTime.now());
blog.setUpdatedAt(LocalDateTime.now());
blogRepository.save(blog);
```

👉 これを Entity 側に閉じ込めるのが `@PrePersist` の威力。

---

## 🧰 1-9. Lombok（`@Getter` / `@Setter` / `@NoArgsConstructor`）

```java
@Getter
@Setter
@NoArgsConstructor
public class Category { ... }
```

### それぞれの役割

| アノテーション         | 自動生成されるもの                     |
| ---------------------- | -------------------------------------- |
| `@Getter`              | 全フィールドの `getXxx()`              |
| `@Setter`              | 全フィールドの `setXxx()`              |
| `@NoArgsConstructor`   | `public Category() {}`（引数なし）     |

### なぜ `@NoArgsConstructor` が **必須** なのか？（超重要）

> JPA は **リフレクションで Entity をインスタンス化する** ため、
> **引数なしコンストラクタが絶対に必要**。

無いと Spring 起動時に次のようなエラーが出ます：

```
No default constructor for entity: com.meitaku.blog.entity.Category
```

👉 **Entity を作ったら反射的に `@NoArgsConstructor` を付ける** と覚える。

---

## ❌ 1-10. 初心者がやりがちなNG

| NGパターン                                    | 何が起きる？                                   |
| --------------------------------------------- | ---------------------------------------------- |
| `@Entity` を忘れる                            | Spring が認識せず、Repository が動かない       |
| 主キーの型を `long` にする                    | INSERT のはずが UPDATE になる事故              |
| `@NoArgsConstructor` を付け忘れる             | 起動時にエラー                                 |
| `@Column` を全く書かない                      | NULL や長すぎる文字列が入って後で爆発する      |
| `created_at` を Service 側で毎回 set する     | コードが冗長になり、書き忘れバグが起きる       |
| Entity に業務ロジックを書く                   | テストできない／責務がぼやける                 |

👉 **Entity = 「DBテーブル1行の形」だけ。動きは書かない。**

---

## 🆚 1-11. Entity と DTO / Form の違い（先取り）

| 種類      | 役割                                     | どこと結びつく？ |
| --------- | ---------------------------------------- | ---------------- |
| Entity    | DBテーブル1行を表す                      | DB               |
| Form      | 画面の入力フォーム1個を表す              | 画面             |
| DTO       | 層をまたいで運ぶ箱                       | 層と層の間       |

👉 **画面の都合を Entity に持ち込むな**、というのが大原則。
Form は第5章で別途作ります。

---

## 📝 1-12. ソースを書くときの順番（実演）

実際に Category.java を書いたときの **頭の動き** を順番に：

1. **空のクラスを書く** → `public class Category {}`
2. **「これはテーブル」** と宣言 → `@Entity` / `@Table(name = "categories")`
3. **テーブル定義書を見ながら主キーを書く** → `id` フィールド + `@Id` + `@GeneratedValue`
4. **業務カラムを書く** → `name` フィールド + `@Column(...)`
5. **共通カラムを書く** → `createdAt` / `updatedAt`
6. **日時を自動で入れる仕掛けを足す** → `@PrePersist` / `@PreUpdate`
7. **Lombokで getter/setter/コンストラクタを生成** → `@Getter` / `@Setter` / `@NoArgsConstructor`

👉 **「定義書を上から順に写す」感覚** で書ける。これがEntityのいいところ。

---

## ✨ 1-13. ソースを書くときのポイント（必読）

### ポイント①：定義書を **左に置いて** 写す

テーブル定義書（カラム名 / 型 / NOT NULL / UNIQUE / 桁数）を1行ずつ Entity の `@Column` に変換していけば、ほぼ間違わない。

### ポイント②：**「制約」を Entity に書く**

- `nullable = false` → NOT NULL
- `unique = true` → UNIQUE
- `length = N` → VARCHAR(N)
- `updatable = false` → 作成日時など書き換え不可カラム

DBだけに任せず、**Java側にも書く**と読み手にやさしい。

### ポイント③：**主キーは `Long`、自動採番は `IDENTITY`**

これは PostgreSQL の `BIGSERIAL` を使う限りほぼ固定。考えなくていい。

### ポイント④：**`@NoArgsConstructor` は反射的に付ける**

Entity を作ったらまずこれ。理由は「JPAがリフレクションで使うから」。

### ポイント⑤：**`created_at` / `updated_at` は Entity 内で完結させる**

`@PrePersist` / `@PreUpdate` を使えば、Service 側で日時セットを書く必要が無くなる。

### ポイント⑥：**Entity に業務ロジックを書かない**

「このカテゴリーは削除できるか？」のような判断は **Service の仕事**。
Entity は形だけ。

---

## ✅ 1-14. 第1章まとめ（完全理解）

✔ Entity = **テーブル1行を表す Java クラス**
✔ **依存される側（Category）から先に作る**
✔ `@Entity` / `@Table` / `@Id` / `@GeneratedValue` / `@Column` がEntityの五大基本アノテーション
✔ 主キーは **`Long` 型 + `IDENTITY` 戦略**
✔ `@NoArgsConstructor` は **JPAが必要とするので必須**
✔ `created_at` / `updated_at` は **`@PrePersist` / `@PreUpdate` で自動化**
✔ Entityは **「形」だけ。動きは Service へ**

---

## 🔜 次の章

**第2章：Blog Entity を作る**
― 外部キー（`category_id`）をどう表現するか ―

次は、**「カテゴリーを参照するブログ」** を Entity でどう表現するかをやります。
ここで初めて `@ManyToOne` という関連付けが登場します。
