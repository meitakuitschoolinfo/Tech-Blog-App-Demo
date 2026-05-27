// このクラスが属するパッケージを宣言する（form/blog パッケージ＝ブログ画面の入力を表すクラスを置く場所）
package com.meitaku.blog.form.blog;

// 「空文字・null・空白のみ」をすべて弾く入力チェック用アノテーションをインポートする
import jakarta.validation.constraints.NotBlank;
// 「null だけ」を弾く入力チェック用アノテーションをインポートする（数値・参照型用）
import jakarta.validation.constraints.NotNull;
// 最大／最小文字数をチェックするためのアノテーションをインポートする
import jakarta.validation.constraints.Size;

// 画面から送られてくるアップロードファイルを受け取るための型をインポートする
import org.springframework.web.multipart.MultipartFile;

// 全フィールドの getter を自動生成する Lombok アノテーションをインポートする
import lombok.Getter;
// 引数なしコンストラクタを自動生成する Lombok アノテーションをインポートする
import lombok.NoArgsConstructor;
// 全フィールドの setter を自動生成する Lombok アノテーションをインポートする
import lombok.Setter;

// 全フィールドの getter を自動生成する
@Getter
// 全フィールドの setter を自動生成する
@Setter
// Springがリフレクションで画面入力を詰めるために必要な「引数なしコンストラクタ」を自動生成する
@NoArgsConstructor
// ブログの「新規登録画面」の入力を表すフォームクラス
public class BlogRegisterForm {

    // 入力必須（空文字・null・空白だけはNG）に指定する
    @NotBlank(message = "タイトルは必須です")
    // 最大200文字までしか入力できないように指定する（Entityの @Column(length = 200) と揃える）
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    // 画面の input[name="title"] と紐づくフィールド
    private String title;

    // 入力必須（本文を空で投稿させない）
    @NotBlank(message = "本文は必須です")
    // 画面の textarea[name="content"] と紐づくフィールド（長さの上限はDB側がTEXTなので付けない）
    private String content;

    // 必ず1つ選択させる（選択リストで未選択はNG）
    @NotNull(message = "カテゴリーを選択してください")
    // 画面の select[name="categoryId"] と紐づくフィールド（Form は Entity ではなく ID を持つ）
    private Long categoryId;

    // 画面の input[type="file"][name="imageFile"] と紐づくフィールド（任意項目）
    // ※ファイルが選択されなければ isEmpty() == true で来る → Service側で null/空をチェック
    private MultipartFile imageFile;

    // 保存済み画像のURL（新規登録時は通常 null）
    // 編集兼用テンプレートでプレビューを表示するため、新規Formにも持たせている
    private String imageUrl;
}
