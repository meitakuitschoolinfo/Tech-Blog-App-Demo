// このクラスが属するパッケージを宣言する（form パッケージ＝画面の入力1件を表すクラスを置く場所）
package com.meitaku.blog.form.category;

// 「空文字・null・空白のみ」をすべて弾く入力チェック用アノテーションをインポートする
import jakarta.validation.constraints.NotBlank;
// 最大／最小文字数をチェックするためのアノテーションをインポートする
import jakarta.validation.constraints.Size;

// 全フィールドの getter を自動生成する Lombok アノテーションをインポートする
import lombok.Getter;
// 引数なしコンストラクタを自動生成する Lombok アノテーションをインポートする（Springが画面入力を詰めるために必要）
import lombok.NoArgsConstructor;
// 全フィールドの setter を自動生成する Lombok アノテーションをインポートする
import lombok.Setter;

// 全フィールドの getter を自動生成する
@Getter
// 全フィールドの setter を自動生成する
@Setter
// Springがリフレクションで画面入力を詰めるために必要な「引数なしコンストラクタ」を自動生成する
@NoArgsConstructor
// カテゴリーの「登録／編集」両方で使われるフォームクラス
public class CategoryForm {

    // 編集時には対象カテゴリーの ID が入る／新規登録時は null（hidden input で送る想定）
    private Long id;

    // 入力必須（空文字・null・空白だけはNG）に指定する
    @NotBlank(message = "カテゴリー名は必須です")
    // 最大100文字までしか入力できないように指定する（Entityの @Column(length = 100) と揃える）
    @Size(max = 100, message = "カテゴリー名は100文字以内で入力してください")
    // 画面の input[name="name"] と紐づくフィールド（Thymeleafの th:field="*{name}" が参照）
    private String name;
}
