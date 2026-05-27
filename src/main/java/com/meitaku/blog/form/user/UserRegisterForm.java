// このクラスが属するパッケージを宣言する（form/user パッケージ＝ユーザー画面の入力を表すクラスを置く場所）
package com.meitaku.blog.form.user;

// メールアドレスの形式（〇〇@〇〇.〇〇）をチェックするアノテーションをインポートする
import jakarta.validation.constraints.Email;
// 「空文字・null・空白のみ」をすべて弾く入力チェック用アノテーションをインポートする
import jakarta.validation.constraints.NotBlank;
// 最大／最小文字数をチェックするためのアノテーションをインポートする
import jakarta.validation.constraints.Size;

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
// 管理者「新規登録（サインアップ）画面」の入力を表すフォームクラス
public class UserRegisterForm {

    // 入力必須（空文字・null・空白だけはNG）に指定する
    @NotBlank(message = "管理者名は必須です")
    // 最大100文字までしか入力できないように指定する（Entity の @Column(length = 100) と揃える）
    @Size(max = 100, message = "管理者名は100文字以内で入力してください")
    // 画面の input[name="name"] と紐づくフィールド
    private String name;

    // 入力必須に指定する
    @NotBlank(message = "メールアドレスは必須です")
    // メールアドレスの形式（@を含む等）をチェックする
    @Email(message = "メールアドレスの形式が不正です")
    // 最大255文字までしか入力できないように指定する（Entity の @Column(length = 255) と揃える）
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    // 画面の input[type="email"][name="email"] と紐づくフィールド
    private String email;

    // 入力必須に指定する
    @NotBlank(message = "パスワードは必須です")
    // 安全のため最低8文字を要求し、上限はDBサイズ（255）に合わせる
    @Size(min = 8, max = 255, message = "パスワードは8文字以上で入力してください")
    // 画面の input[type="password"][name="password"] と紐づくフィールド（※生パスワード。Service側でハッシュ化する）
    private String password;
}
