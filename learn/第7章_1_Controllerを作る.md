# 📘 第7章（前半）

## Controller を作る
― ついに **HTTP の入口** を作る。今までのピース全部を「URL から呼べる」状態にする ―

> 📌 第7章は **大ボリューム** なので **前後半に分割** します。
> - **7-1（この章）**：Controller 10ファイルと、ルーティング・PRG・バリデーション再描画の理解
> - **7-2（次章）**：残りの Thymeleaf テンプレート、`SecurityConfig` 本格化、application.properties、動作確認

---

## 🎯 7-1-1. この章のゴール（重要）

この章を終えたら、次のことが **説明できる状態** を目指します。

✔ Controller の役割を **一文で説明できる**
✔ `@Controller` と `@RestController` の違いを説明できる
✔ `@GetMapping` / `@PostMapping` / `@PathVariable` の使い分けができる
✔ `@Valid` + `BindingResult` で **入力チェック→再描画** の流れが書ける
✔ **PRGパターン**（Post-Redirect-Get）を説明できる
✔ `RedirectAttributes.addFlashAttribute` で **1回限りメッセージ** を渡せる
✔ `@AuthenticationPrincipal` で **ログイン中ユーザー** を受け取れる
✔ Controller に **書いてはいけないコード** が分かる

---

## 🧩 7-1-2. Controller とは何か？（超重要）

### 一言で言うと

> Controller =
> **HTTPリクエストを受け取り、Service を呼び出し、画面（テンプレート）を返す** クラス

### Controller の立ち位置（再確認）

```text
[ ブラウザ ] ──HTTP──▶ [ Controller ] ← ★ ここ：URL を受け取って画面を返す
                            │
                            ▼ Service を呼ぶ
                       [  Service  ]    ← 業務の流れ・判断
                            │
                            ▼
                       [ Repository ]   ← DB操作だけ
```

### Controller が **やる** こと

- ✔ URL とメソッドの紐付け（`@GetMapping("/blogs")` など）
- ✔ リクエストパラメータ／パス変数／フォームの **受け取り**
- ✔ 入力チェック（`@Valid`）の **発動**
- ✔ Service の呼び出し
- ✔ テンプレート名の返却 or リダイレクト

### Controller が **やらない** こと

- ❌ SQL を書く（→ Repository）
- ❌ 業務ルール（→ Service）
- ❌ ハッシュ化／メール送信などの副作用（→ Service）
- ❌ HTMLを文字列で組み立てる（→ Thymeleaf）

👉 **薄く保つのがコツ**。Controller が太ったら、それは大体 Service の仕事を吸ってる。

---

## 🔄 7-1-3. なぜ Service の **次** に Controller を書くのか？

### 鉄則（おさらい）

> **「依存される側 → 依存する側」** の順で作る

### Controller の依存関係

```text
[ Controller ]
   ├── Service    ← 第6章で完成
   ├── Form       ← 第5章で完成
   ├── Entity     ← 第1〜3章で完成
   └── Spring Security 〜 SecurityConfig（一部）
```

- Controller は **「最も上の層」** で、すべてに依存する
- → 全部完成してから書くと **import 時に詰まらない**

### この章で **書く順番**（重要）

| 順番 | 作る Controller          | URL                                | 学ぶこと                                |
| ---: | ------------------------ | ---------------------------------- | --------------------------------------- |
|    1 | `HomeController`         | `/`                                | **最小形**＋リダイレクト                |
|    2 | `BlogListController`     | `/blogs` / `/admin/blogs`          | **`Pageable`** によるページング         |
|    3 | `BlogDetailController`   | `/blogs/{id}`                      | **`@PathVariable`**                     |
|    4 | `BlogRegisterController` | `/admin/blogs/new`, POST `/admin/blogs` | **`@Valid` + `BindingResult` + PRG**    |
|    5 | `BlogEditController`     | `/admin/blogs/{id}/edit`           | 編集パターン（GET フォーム + POST 更新）|
|    6 | `BlogDeleteController`   | POST `/admin/blogs/{id}/delete`    | **削除は必ず POST**                     |
|    7 | `LoginController`        | `/login`                           | **POST は Spring Security に渡す**      |
|    8 | `UserRegisterController` | `/admin/register`                  | 新規登録の典型形                        |
|    9 | `MyPageController`       | `/admin/profile`                   | **`@AuthenticationPrincipal`**          |
|   10 | `AdminCategoryController`| `/admin/categories/**`             | **`@RequestMapping` で集約**            |

---

## 🧱 7-1-4. Controller 共通の書き方

すべての Controller に共通：

```java
@Controller
@RequiredArgsConstructor
public class XxxController {
    private final XxxService xxxService;

    @GetMapping("/xxx")
    public String show(Model model) {
        model.addAttribute("...", ...);
        return "xxx/yyy";   // ← テンプレートファイル名（拡張子なし）
    }
}
```

### `@Controller` vs `@RestController`

| 種類              | 戻り値の扱い                          | 主な用途                  |
| ----------------- | ------------------------------------- | ------------------------- |
| `@Controller`     | 文字列 → **テンプレート名** として解釈 | サーバーサイドHTMLレンダリング |
| `@RestController` | 戻り値 → **JSON にシリアライズ**       | REST API                  |

👉 今回は Thymeleaf で HTML を返すので **`@Controller` 一択**。

### `Model` への `addAttribute`

```java
model.addAttribute("blog", blog);
```

- テンプレートで `${blog}` として参照できる
- キー名は **テンプレート側と完全一致** させる

### 戻り値（テンプレートパス）

```java
return "blog/list";          // → templates/blog/list.html
return "redirect:/admin/blogs";  // → 別URLにリダイレクト
```

---

## 🆔 7-1-5. URLからの値の受け取り3パターン

### ① `@PathVariable`：URL の一部から取る

```java
@GetMapping("/blogs/{id}")
public String detail(@PathVariable Long id) { ... }
//                                   ↑
//                              {id} がここに自動で入る
```

### ② `Pageable`（クエリパラメータから取る）

```java
@GetMapping("/blogs")
public String list(@PageableDefault(size = 9) Pageable pageable) { ... }
//   URL: /blogs?page=0&size=9
//                ↑
//        page と size が pageable に自動で入る
```

### ③ フォーム（Form クラスに自動バインド）

```java
@PostMapping("/admin/blogs")
public String register(@Valid BlogRegisterForm blogForm,
                       BindingResult bindingResult) { ... }
//   フォーム送信内容が blogForm の各フィールドに詰められる
//   （Form のフィールド名 = input の name 属性）
```

---

## ✅ 7-1-6. ★最重要★ バリデーション → 再描画の流れ

### コードの定型パターン（4ステップ）

```java
@PostMapping("/admin/blogs")
public String register(
        @Valid BlogRegisterForm blogForm,         // ① @Valid で発動
        BindingResult bindingResult,              // ② 直後に置く（順番厳守）
        Model model,
        RedirectAttributes redirectAttributes) {

    // ③ エラーがあれば再描画
    if (bindingResult.hasErrors()) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/blog/form";
    }

    try {
        blogService.register(blogForm);
    } catch (IllegalArgumentException e) {
        // ④ 業務ルール違反もフォームエラーとして再描画
        bindingResult.reject("global.error", e.getMessage());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/blog/form";
    }

    redirectAttributes.addFlashAttribute("message", "ブログを登録しました");
    return "redirect:/admin/blogs";   // ★ PRG
}
```

### ★超重要★ `BindingResult` は `@Valid` の **直後** に置く

```java
// ⭕ 正しい
public String register(@Valid BlogRegisterForm form, BindingResult bindingResult, ...) { }

// ❌ 間違い：間に何か挟まると BindingResult が空になる
public String register(@Valid BlogRegisterForm form, Model model, BindingResult bindingResult) { }
```

順番を守らないと **エラー情報が取れなくなる**。

### `bindingResult.hasErrors()` の判定

- `true` → エラーあり。**同じテンプレートを再描画** する
- `false` → エラーなし。**Service を呼んで処理を進める**

### 再描画時に **モデルを再度詰め直す** 理由

- カテゴリーのドロップダウンや「編集フラグ」は GET 時に詰めたもの
- POST → 再描画では **GET 時のモデルは引き継がれない** → 自分で詰め直す
- 詰め忘れるとドロップダウンが空・フラグが false に戻るバグになる

### `bindingResult.reject(...)` で **業務エラー** をフォームエラー化

- Service が `IllegalArgumentException`（重複など）を投げる
- これを `reject(...)` でフォーム全体のエラーとして登録する
- テンプレート側で `th:errors="${#fields.globalErrors()}"` で表示する

---

## 🔁 7-1-7. ★超重要★ PRG パターン（Post-Redirect-Get）

### PRG とは

> POST で処理した後は、**必ずリダイレクト**してから GET で画面を表示する

```text
POST /admin/blogs（登録）
       ↓
   blogService.register(...)
       ↓
   return "redirect:/admin/blogs";   ← ★ HTML を直接返さない
       ↓
GET /admin/blogs（リダイレクト先）
       ↓
   一覧画面を新たに描画
```

### なぜ必要？

- POST 直後に HTML を返すと、ブラウザが **「F5で再送信しますか？」** を出す
- うっかり OK すると **登録が2回走る** ＝ データ不整合
- リダイレクトしておけば、F5 は **リダイレクト先（GET）の再読み込み** で安全

### `RedirectAttributes.addFlashAttribute` の意味

```java
redirectAttributes.addFlashAttribute("message", "ブログを登録しました");
return "redirect:/admin/blogs";
```

- リダイレクト先で **1回だけ** 取れる属性
- テンプレートで `${message}` として参照可能
- 「保存しました」「削除しました」のトースト表示に最適

---

## 🔐 7-1-8. `@AuthenticationPrincipal` でログイン中ユーザーを取る

### コード

```java
@GetMapping("/admin/profile")
public String showForm(@AuthenticationPrincipal UserDetails principal, Model model) {
    User currentUser = userService.findByEmail(principal.getUsername());
    ...
}
```

### `UserDetails` から取れるもの

| メソッド             | 意味                              |
| -------------------- | --------------------------------- |
| `getUsername()`      | ログインID（このアプリでは email）|
| `getAuthorities()`   | 持っている権限（ROLE_ADMIN等）   |

### なぜ URL の id を信用しない？

- `/admin/profile/{id}` のような設計だと **他人の id を入れて編集できてしまう**
- → **「いま誰がログインしているか？」** だけを信用する
- → `principal.getUsername()` で email を得て、自分の User を取り直す

👉 **自分自身の編集は URL ではなくセッションから引く**。これがセキュリティの基本。

---

## ⛏ 7-1-9. 個別 Controller の見どころ

### `HomeController`

```java
@GetMapping("/")
public String index() {
    return "redirect:/blogs";
}
```

- 最小コードでリダイレクトを学べる
- `redirect:` プレフィックスは **テンプレート名ではなく内部リダイレクト指示**

### `BlogListController`

```java
@GetMapping("/blogs")
public String listForUser(@PageableDefault(size = 9) Pageable pageable, Model model) {
    Page<Blog> page = blogService.findLatest(pageable);
    model.addAttribute("page", page);
    return "blog/list";
}
```

- **1ファイルで /blogs と /admin/blogs の両方** を担当
- 同じ Service メソッドを呼んで **別テンプレート** に渡す
- `@PageableDefault` でクエリ無し時のデフォルトを指定

### `BlogDetailController`

- `@PathVariable Long id` で `/blogs/{id}` から id を取る
- Service が `ResourceNotFoundException` を投げれば自動で **HTTP 404**

### `BlogRegisterController` / `BlogEditController`

- **GET = フォーム表示、POST = 登録/更新** の典型2メソッドパターン
- 兼用テンプレート `admin/blog/form` を使うので、`isEdit` フラグを必ず渡す
- 再描画時にも `categories` / `isEdit` を **必ず詰め直す**

### `BlogDeleteController`

```java
@PostMapping("/admin/blogs/{id}/delete")
```

- **削除は必ず POST**
- GET にすると、リンクを踏むだけ／ブラウザのプリフェッチで消える事故が起きる

### `LoginController`

```java
@GetMapping("/login")
public String showForm(Model model) {
    model.addAttribute("loginForm", new LoginForm());
    return "auth/login";
}
```

- **GET だけ書く**
- POST は **Spring Security が直接受け取る**（次章 SecurityConfig 参照）
- 自分で POST `/login` を書くと Spring Security と衝突する

### `UserRegisterController`

- ログインしていない状態でアクセスする「サインアップ」画面
- 成功後は `/login` にリダイレクトしてフラッシュメッセージを表示

### `MyPageController`

- ログイン中の人 **本人のプロフィール** を編集する
- URL に id を含めず、`@AuthenticationPrincipal` で本人を特定

### `AdminCategoryController`

- **1クラスで CRUD 全部** を担当（Blog の分割スタイルとは対比）
- クラスに `@RequestMapping("/admin/categories")` を付けて URL の共通プレフィックスをまとめる
- 削除時の `IllegalStateException`（使用中）はフラッシュの **`errorMessage`** で表示

> 📌 MD のパッケージ構成に Category Controller が無いが、Service と Form が存在するため追加。
> 　 **「仕様の穴を Controller でも埋める」** という現場感覚の演習。

---

## ❌ 7-1-10. 初心者がやりがちなNG

| NGパターン                                          | 何が起きる？                                         |
| --------------------------------------------------- | ---------------------------------------------------- |
| Controller から Repository を直接呼ぶ               | Service の存在意義が消える／業務ロジックが散らばる   |
| `BindingResult` を `@Valid` の **直後以外** に書く  | エラーが取れない（黙って空になる）                   |
| POST 後にテンプレートを直接返す（リダイレクトしない）| F5 で多重送信が起きる（PRG違反）                     |
| 再描画時に `categories` を詰め直し忘れる            | ドロップダウンが空になる／NullPointer               |
| 削除を GET で受ける                                 | 意図せず削除が走る（リンク踏むだけで消える）         |
| `/admin/profile/{id}` のように URL から ID を取る   | 他人の ID を入れて編集できてしまう                   |
| Controller でハッシュ化／メール送信などをする       | テストできない／Service と層が逆転する               |
| `@RestController` を Thymeleaf 用に付ける           | テンプレートが返らず、文字列がそのまま画面に出る     |

---

## 📝 7-1-11. ソースを書くときの順番（実演）

実際に Controller 10ファイルを書いた **頭の動き** の順番：

1. **HomeController** から書く（最小形 → 動作確認の出発点）
2. **BlogListController** → ページング・複数URL対応
3. **BlogDetailController** → `@PathVariable` を体験
4. **BlogRegisterController** → `@Valid` + `BindingResult` + PRG の完全パターン
5. **BlogEditController** → 編集系の典型（GET フォーム + POST 更新）
6. **BlogDeleteController** → 「削除は必ず POST」を体に染み込ませる
7. **LoginController** → 「POST は書かない」例を覚える
8. **UserRegisterController** → 公開新規登録の典型
9. **MyPageController** → `@AuthenticationPrincipal` を体験
10. **AdminCategoryController** → `@RequestMapping` で集約型 Controller を学ぶ

各 Controller の中の書く順番（共通テンプレ）：

1. クラス宣言＋3点セット（`@Controller` / `@RequiredArgsConstructor` / 必要なら `@RequestMapping`）
2. `final` フィールドで依存 Service を宣言
3. **GET（表示）系メソッド** から書く
4. **POST（更新）系メソッド** を書く（`@Valid` + `BindingResult` + PRG）
5. 必要に応じて **削除系メソッド**

---

## ✨ 7-1-12. ソースを書くときのポイント（必読）

### ポイント①：**Controller は薄く保つ**

ロジックは Service に寄せる。Controller の役割は「受け取って」「呼んで」「画面を返す」だけ。

### ポイント②：**`@Controller` + `@RequiredArgsConstructor`** をセットで覚える

依存は `private final` でコンストラクタインジェクション。Service と同じ流儀。

### ポイント③：**`BindingResult` は `@Valid` の直後**

順番を1つ間違えるだけでエラーが取れない。**順番厳守**。

### ポイント④：**POST後は必ずリダイレクト（PRG）**

`redirect:/...` を書く。HTMLを直接返さない。F5多重送信を防ぐ。

### ポイント⑤：**再描画時にモデルを詰め直す**

GET の時にだけ詰めた値（dropdown 用カテゴリー等）は、再描画でも必要。
**忘れると画面が壊れる**。

### ポイント⑥：**削除は必ず POST**

GET 削除は禁忌。リンク踏みだけで消えてしまう。

### ポイント⑦：**ログイン中ユーザーは URL からではなく `@AuthenticationPrincipal` から**

改ざん防止の最低ライン。

### ポイント⑧：**業務エラーは `bindingResult.reject(...)` でフォームエラーに昇格**

Service が投げる `IllegalArgumentException` を try-catch して、画面に綺麗に出す。

### ポイント⑨：**フラッシュメッセージで成功通知**

リダイレクト先で1回だけ表示。トースト UI と相性が良い。

---

## ✅ 7-1-13. 第7章（前半）まとめ

✔ Controller = **HTTP受付 → Service呼び出し → 画面返却** の薄い層
✔ `@Controller` + `@RequiredArgsConstructor` + `private final` Service
✔ **URL → 値**：`@PathVariable` / `Pageable` / Form自動バインド
✔ **入力チェック**：`@Valid` + `BindingResult`（直後に置く）
✔ **PRG**：POST後は必ず `redirect:/...`
✔ **フラッシュ**：`addFlashAttribute` で1回限りメッセージ
✔ **ログイン中ユーザー**：`@AuthenticationPrincipal` から取る
✔ **削除は必ず POST**
✔ Controller は **薄く保つ**（業務は全部 Service へ）

---

## 🔜 次の章

**第7章（後半）：残りのテンプレート ＋ SecurityConfig 本格化 ＋ application.properties**

次は、Controller が返す **未作成のテンプレート群** をすべて作って、`SecurityConfig` を本格化し、
**実際にアプリが起動して動く** ところまで持っていきます。

- 残りの Thymeleaf テンプレート（blog/list, blog/detail, admin/blog/list, admin/category/list, user/profile, etc.）
- `SecurityConfig` の `SecurityFilterChain`（公開URL／認証必須URL／ログインURL／ログアウト）
- `application.properties` の DB 接続設定
- 起動確認＋つまづきやすいポイント解説

ここまで来たら **完成です**。
