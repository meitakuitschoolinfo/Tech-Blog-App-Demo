// このクラスが属するパッケージを宣言する（controller/auth パッケージ＝認証関連の Controller を置く場所）
package com.meitaku.blog.controller.auth;

// 画面の入力を表すフォームクラスをインポートする
import com.meitaku.blog.form.user.LoginForm;

// このクラスを Controller として認識させるアノテーション
import org.springframework.stereotype.Controller;
// テンプレートに値を渡す入れ物
import org.springframework.ui.Model;
// HTTP GET 用アノテーション
import org.springframework.web.bind.annotation.GetMapping;

// Spring に Controller として登録する
@Controller
// ログイン画面の表示を担当する Controller
// ※ POST /login は Spring Security が直接処理する（SecurityConfig で設定）→ ここには書かない
public class LoginController {

    // HTTP GET "/login" にマッピング：ログインフォームを表示する
    @GetMapping("/login")
    public String showForm(Model model) {
        // 空の Form をモデルに入れる（テンプレートの th:object="${loginForm}" と一致させる）
        model.addAttribute("loginForm", new LoginForm());
        // ログイン画面テンプレート（templates/auth/login.html）を返す
        return "auth/login";
    }
}
