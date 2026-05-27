// このクラスが属するパッケージを宣言する（form/user パッケージ＝ユーザー画面の入力を表すクラスを置く場所）
package com.meitaku.blog.form.user;

// メールアドレスの形式チェック用アノテーションをインポートする
import jakarta.validation.constraints.Email;
// 「空文字・null・空白のみ」をすべて弾く入力チェック用アノテーションをインポートする
import jakarta.validation.constraints.NotBlank;

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
// 管理者ログイン画面の入力を表すフォームクラス
public class LoginForm {

    // 入力必須に指定する
    @NotBlank(message = "メールアドレスを入力してください")
    // メールアドレスの形式（@を含む等）をチェックする
    @Email(message = "メールアドレスの形式が不正です")
    // 画面の input[type="email"][name="email"] と紐づくフィールド
    private String email;

    // 入力必須に指定する
    @NotBlank(message = "パスワードを入力してください")
    // 画面の input[type="password"][name="password"] と紐づくフィールド
    private String password;
}
