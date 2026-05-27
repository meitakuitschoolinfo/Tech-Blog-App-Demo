# 📘 第4章

## Repository を作る
― 添付PDF「Repository とは何か」を **3つのEntityに実適用** する ―

> 📌 この章は、研修用PDF「**第3章 Repository とは何か**」の内容を **このプロジェクトに当てはめて手を動かす章** です。
> 概念解説はPDFを参照しつつ、ここではコードに落とすところに集中します。

---

## 🎯 4-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ なぜ Entity を3つ作ったあとに **Repository をまとめて作る** のか説明できる
✔ `JpaRepository<Entity, ID>` の **第1型引数 / 第2型引数** を分解して説明できる
✔ `findByEmail` のような **メソッド名規約** がなぜ動くか説明できる
✔ `Optional<User>` を **null チェックの代わりに** 使える
✔ `Page<Blog>` と `Pageable` で **ページング検索** が書ける
✔ Repository に **書いてはいけないコード** が分かる

---

## 🧩 4-2. Repository の役割（PDF復習）

> Repository = **データベースと直接会話するための「専門窓口」**（PDF 3-2）

```text
[ Controller ]
     ↓
[  Service  ] ← 業務の流れ・判断
     ↓
[ Repository ] ← ★この章。DBとのやり取りだけ
     ↓
[ Database  ]
```

👉 **Repository は「DB操作以外をしてはいけない」層**。

---

## 🔄 4-3. なぜ Entity を全部作った後、Repository を **まとめて** 作るのか？

### 鉄則（おさらい）

> **「依存される側 → 依存する側」** の順で作る

### Repository から見ると…

```java
public interface BlogRepository extends JpaRepository<Blog, Long> { ... }
                                              ↑
                                          Entityがここで登場
```

- Repository は **必ず Entity に依存** する
- → Entity が **全部揃ってから** Repository を書くのが効率的
- → 行ったり来たりせずに、`repository/` フォルダ内で連続して書ける

### この章での **書く順番**（重要）

| 順番 | 作るRepository       | 学ぶこと                                      |
| ---: | -------------------- | --------------------------------------------- |
|    1 | `CategoryRepository` | **JpaRepository の最小形**                    |
|    2 | `UserRepository`     | **メソッド名規約** （`findByEmail`）           |
|    3 | `BlogRepository`     | **ページング**（`Page<T>` / `Pageable`） + 関連先での検索 |

> 📌 Entity の順は Category→Blog→User でしたが、
>    Repository は **「簡単 → 複雑」の階段順**にした方が学習効率が良いので順を入れ替えています。

---

## 🧠 4-4. 共通の型構造を分解する（PDF 3-4 復習）

3つの Repository すべてに共通する書き方：

```java
public interface XxxRepository extends JpaRepository<Entity, ID> {
}
```

### 分解

| 部分                     | 意味                                  |
| ------------------------ | ------------------------------------- |
| `interface`              | **クラスではなく** インターフェース   |
| `extends JpaRepository`  | 標準CRUDをすべて引き継ぐ              |
| `<Entity, ID>` 第1型引数 | 扱うEntity                            |
| `<Entity, ID>` 第2型引数 | そのEntityの **主キー（@Id）の型**    |

### 今回のプロジェクトに当てはめると

| Repository           | Entity     | 主キーの型 | 書き方                                    |
| -------------------- | ---------- | ---------- | ----------------------------------------- |
| `CategoryRepository` | `Category` | `Long`     | `extends JpaRepository<Category, Long>`   |
| `UserRepository`     | `User`     | `Long`     | `extends JpaRepository<User, Long>`       |
| `BlogRepository`     | `Blog`     | `Long`     | `extends JpaRepository<Blog, Long>`       |

👉 **テーブル定義で `id BIGSERIAL` → 第2型引数は `Long`**（PDF 3-4 と同じ理由）。

---

## 🧱 4-5. ステップ① CategoryRepository（最小形）

完成ファイル：[CategoryRepository.java](../src/main/java/com/meitaku/blog/repository/CategoryRepository.java)

```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}
```

### この **1行（extends ...）** だけで使える機能

| メソッド                   | 内部で発行されるSQL               |
| -------------------------- | --------------------------------- |
| `save(category)`           | INSERT または UPDATE              |
| `findById(id)`             | SELECT ... WHERE id = ?           |
| `findAll()`                | SELECT *                          |
| `findAll(Pageable)`        | SELECT * + LIMIT/OFFSET           |
| `deleteById(id)`           | DELETE WHERE id = ?               |
| `count()`                  | SELECT count(*)                   |

👉 **SQLを1行も書かずに** 全部使える。これがPDFの「3-3. Repositoryがある世界」。

### `@Repository` アノテーション

```java
@Repository
public interface CategoryRepository extends JpaRepository<...> { ... }
```

- 実は `JpaRepository` を継承していれば **省略しても動く**
- それでも書くのは **「このインターフェースの役割は Repositoryです」** と読み手に伝えるため
- 研修ではすべての Repository に **必ず付ける**

### `boolean existsByName(String name)`

- 「同じ名前のカテゴリーが既にあるか？」を確認するための **存在チェック専用** メソッド
- 戻り値が `boolean` なので、SELECT文の中身は **count(*) > 0** に最適化される

---

## 🧱 4-6. ステップ② UserRepository（**メソッド名規約** を覚える）

完成ファイル：[UserRepository.java](../src/main/java/com/meitaku/blog/repository/UserRepository.java)

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### 「メソッド名から自動でSQLが作られる」仕組み（PDF 3-8 復習）

PDFで学んだとおり、Spring Data JPA は **メソッド名を英語ではなくルールとして** 解析する。

#### `findByEmail` の分解

| 部分    | 意味                       |
| ------- | -------------------------- |
| `find`  | 検索する（SELECT）         |
| `By`    | 条件指定                   |
| `Email` | Entity のフィールド名 `email` |

→ 自動生成されるSQL：

```sql
SELECT * FROM admin_users WHERE email = ?
```

#### 重要：**「Entity のフィールド名」が基準**（PDF 3-9）

```java
// User Entity
private String email;     // ← この名前が「Email」になる

// UserRepository
Optional<User> findByEmail(String email);
                       ^^^^^
                       Entityのフィールド名と一致させる
```

👉 **DBカラム名 `email` ではなく、Entityフィールド名 `email`** を見ている。
　 偶然今回は同じだが、`imageUrl`（DB:`image_url`）のような場合は **Java側の名前** を使う。

### `Optional<User>` を返す理由（PDF 3-7 復習）

```java
Optional<User> findByEmail(String email);
```

- DBに **「該当 email のユーザーが存在しない」可能性がある**
- もし `User` を直接返すと、呼び出し側で **null チェック忘れ** が発生する
- `Optional` ならコンパイラが「中身を取り出す処理」を強制してくれる

#### 正しい使い方（Service層で書く想定）

```java
User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new UsernameNotFoundException("管理者が見つかりません"));
```

### `existsByEmail` と `findByEmail` の使い分け

| やりたいこと              | 使うメソッド          |
| ------------------------- | --------------------- |
| ユーザーの**情報**が欲しい | `findByEmail`         |
| **存在の有無**だけ知りたい | `existsByEmail`       |

👉 「存在チェックだけ」のときに `findByEmail` を使うと **不要な列まで取ってくる**ので非効率。
　 **目的に合わせてメソッドを選ぶ**。

---

## 🧱 4-7. ステップ③ BlogRepository（**ページング** を覚える）

完成ファイル：[BlogRepository.java](../src/main/java/com/meitaku/blog/repository/BlogRepository.java)

```java
@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Page<Blog> findAllByOrderByPublishedAtDesc(Pageable pageable);
    Page<Blog> findByCategoryOrderByPublishedAtDesc(Category category, Pageable pageable);
    long countByCategory(Category category);
}
```

### `findAllByOrderByPublishedAtDesc` の分解（PDF 3-10 復習）

| 部分                       | 意味                              |
| -------------------------- | --------------------------------- |
| `findAll`                  | 全件検索（SELECT *）              |
| `By`                       | 条件指定（条件なしのときも `By` を入れる慣習がある／無しでもOK） |
| `OrderBy`                  | ソート指定                        |
| `PublishedAt`              | 並び替えキー（Entityのフィールド名）|
| `Desc`                     | 降順                              |

→ 自動生成されるSQL：

```sql
SELECT *
FROM blogs
ORDER BY published_at DESC
LIMIT ? OFFSET ?
```

### `Page<Blog>` の中身

`Page<Blog>` は **ページング結果のリッチな入れ物**：

| 取れる情報           | 用途                                      |
| -------------------- | ----------------------------------------- |
| `getContent()`       | 1ページ分の List<Blog>                    |
| `getTotalElements()` | 全件数                                    |
| `getTotalPages()`    | 総ページ数                                |
| `getNumber()`        | 現在のページ番号（0始まり）               |
| `isFirst()` / `isLast()` | 最初／最後のページか                  |

👉 これがあるから、ページャー画面（「1 / 2 / 3 / 次へ」）が簡単に作れる。

### `Pageable` ってどう作るの？（先取り）

Controller / Service 側でこう書く：

```java
// 0ページ目（=1ページ目）、1ページ9件
Pageable pageable = PageRequest.of(0, 9);
Page<Blog> page = blogRepository.findAllByOrderByPublishedAtDesc(pageable);
```

👉 **ページ番号は 0 始まり**。これは Spring Data JPA の決まり。
　 画面表示の「1ページ目」と内部の「0ページ目」のズレに注意。

### `findByCategoryOrderByPublishedAtDesc(Category category, ...)` のポイント

- 引数が `Long categoryId` ではなく **`Category category`** （Entityそのもの）
- → JPAが内部で `category.getId()` を取り出してSQLに渡してくれる
- → 第2章で `@ManyToOne private Category category;` にしておいた恩恵がここで出る

### `countByCategory(Category category)` の使い道

- カテゴリー削除前に「このカテゴリーを使っているブログは何件？」を確認する
- 0件なら削除OK、1件以上なら削除NGメッセージを出す（Service 層で判定）

---

## ⚠️ 4-8. Repository でやってはいけないこと（PDF 3-11 を実適用）

### ❌ `if` / `for` だらけの処理を書く

```java
// ❌ NG
public interface BlogRepository extends JpaRepository<Blog, Long> {
    default List<Blog> getPublishedOrEmpty(boolean flag) {
        if (flag) return findAll();
        else return List.of();
    }
}
```

→ **業務判定は Service の仕事**。Repository に分岐を入れない。

### ❌ ビジネスルールを書く

```java
// ❌ NG
default void deleteIfUnused(Long id) {
    if (countByCategory(...) > 0) return;
    deleteById(id);
}
```

→ 「使われていなかったら消す」は **業務ルール**。Service へ。

### ❌ 1つの Repository で複数 Entity を扱う

```java
// ❌ NG
public interface BlogAndCategoryRepository
    extends JpaRepository<Blog, Long> {
    List<Category> findCategories();  // ← Category を返してしまう
}
```

→ **1 Entity に 1 Repository**（PDF 3-4 の原則）。

---

## 🆚 4-9. Service と Repository の役割分担（PDF 3-12 を実適用）

| 例                              | Service               | Repository               |
| ------------------------------- | --------------------- | ------------------------ |
| 「カテゴリーを削除できるか？」  | ⭕ 業務判定          | -                        |
| 「使われている件数を数える」    | -                     | ⭕ `countByCategory(...)` |
| 「結果に応じてDELETEする」      | ⭕ 判定後に呼び出し  | ⭕ `deleteById(...)` を実行 |
| 「パスワードをハッシュ化する」  | ⭕ Encoder.encode    | -                        |
| 「ユーザーをDBに保存する」      | -                     | ⭕ `save(user)`           |

> 👉 **判断するのが Service、動かすのが Repository**。

---

## 📝 4-10. ソースを書くときの順番（実演）

実際に Repository 3つを書いた **頭の動き** の順番：

1. **CategoryRepository** から書く
   - 最小形（`extends JpaRepository<Category, Long>` だけ）でまず**動く形**にする
   - その後 `existsByName` を追加
2. **UserRepository** に進む
   - メソッド名規約に慣れる練習として `findByEmail` を書く
   - 戻り値を **必ず `Optional<User>`** にする
   - 重複チェック用に `existsByEmail` を追加
3. **BlogRepository** に進む
   - ページング検索 `Page<Blog> findAllByOrderByPublishedAtDesc(Pageable)` を書く
   - 関連先で絞り込む `findByCategoryOrderByPublishedAtDesc` を追加
   - 削除判定用の `countByCategory` を追加

👉 **「最小形 → メソッド名規約 → ページング」** という難易度の階段で進む。

---

## ✨ 4-11. ソースを書くときのポイント（必読）

### ポイント①：**`interface` であって `class` ではない**

実装は **Springが起動時に自動生成** する（PDF 3-5）。
クラスにしようとしない。

### ポイント②：**型引数は `<Entity, 主キーの型>`**

`<Category, Long>` のように2つ。
1個忘れたらコンパイルエラー。

### ポイント③：**戻り値が「無いかも」のときは Optional**

`findByEmail` のように **0件の可能性がある単件取得** は `Optional`。
`null` を返すメソッドは設計が古い。

### ポイント④：**ページングは `Page<T>` と `Pageable`**

`List<T>` で返してしまうと、画面のページャーが作れなくなる。
**最初から `Page<T>` で受ける**。

### ポイント⑤：**メソッド名の単語は「Entityのフィールド名」と一致させる**

`findByEmail` / `findByCategory` / `OrderByPublishedAt` …
**DBカラム名ではなく、Entityフィールド名**。

### ポイント⑥：**Repository には if/for/業務判定を書かない**

「削除できるか？」「ハッシュ化する」のような **判断は Service の仕事**。
Repository は **DBへの入出力だけ**。

### ポイント⑦：**1 Entity = 1 Repository**

複数Entityを1ファイルで扱いたくなったら、設計を疑う。

---

## ✅ 4-12. 第4章まとめ（完全理解）

✔ Repository は **DB操作専用** の窓口（PDF 3-2）
✔ Entityを全部作った後に **まとめて作る** と効率的
✔ 書く順番は **簡単 → 複雑**：Category → User → Blog
✔ 型引数は **`<Entity, 主キー型>`**
✔ 単件取得は **`Optional<Entity>`** を返す（null を返さない）
✔ メソッド名は **`find / exists / count + By + フィールド名`** のルール
✔ ページング検索は **`Page<T>` + `Pageable`**
✔ Repository に **if / for / 業務判定 を書かない**

---

## 🔜 次の章

**第5章：Form を作る**
― 画面からの入力を受け取る箱と、入力チェック（バリデーション）―

次は **画面層との接点** を作ります。
ここで **「Entity と Form を分ける理由」**（第2章で先取りした内容）の本番が始まります。
`@NotBlank` / `@Size` / `@Email` などのバリデーションも登場します。
