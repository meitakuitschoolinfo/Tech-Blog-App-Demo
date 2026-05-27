// このクラスが属するパッケージを宣言する（form/blog パッケージ＝ブログ画面の入力を表すクラスを置く場所）
package com.meitaku.blog.form.blog;

// 「null だけ」を弾く入力チェック用アノテーションをインポートする
import jakarta.validation.constraints.NotNull;
// 「空文字・null・空白のみ」をすべて弾く入力チェック用アノテーションをインポートする
import jakarta.validation.constraints.NotBlank;
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
// ブログの「編集画面」の入力を表すフォームクラス（新規登録フォームに id ＋ 既存imageUrlが加わった形）
public class BlogEditForm {

    // 編集対象のブログIDは必須（hidden inputで送信される）
    @NotNull(message = "編集対象のブログIDが取得できませんでした")
    // 画面の input[type="hidden"][name="id"] と紐づくフィールド
    private Long id;

    // 入力必須（空文字・null・空白だけはNG）に指定する
    @NotBlank(message = "タイトルは必須です")
    // 最大200文字までしか入力できないように指定する（Entity の @Column(length = 200) と揃える）
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    // 画面の input[name="title"] と紐づくフィールド
    private String title;

    // 入力必須（本文を空で更新させない）
    @NotBlank(message = "本文は必須です")
    // 画面の textarea[name="content"] と紐づくフィールド
    private String content;

    // 必ず1つ選択させる（選択リストで未選択はNG）
    @NotNull(message = "カテゴリーを選択してください")
    // 画面の select[name="categoryId"] と紐づくフィールド
    private Long categoryId;

    // 画面の input[type="file"][name="imageFile"] と紐づくフィールド（任意項目）
    // ※新しい画像をアップロードしたい時だけ値が入る。空なら既存imageUrlを維持する
    private MultipartFile imageFile;

    // 既存の画像URL（編集画面表示時にプレビュー用に表示／hidden inputで往復する）
    // 新しいファイルがアップロードされなければ、このURLをそのまま維持する
    private String imageUrl;
}
