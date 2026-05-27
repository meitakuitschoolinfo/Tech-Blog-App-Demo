// このクラスが属するパッケージを宣言する（controller/user パッケージ＝ユーザー関連の Controller を置く場所）
package com.meitaku.blog.controller.user;

// 管理者新規登録フォームをインポートする
import com.meitaku.blog.form.user.UserRegisterForm;
// 管理者登録処理を担う Service をインポートする
import com.meitaku.blog.service.UserService;

// 入力チェックを発動させるアノテーション
import jakarta.validation.Valid;
// このクラスを Controller として認識させるアノテーション
import org.springframework.stereotype.Controller;
// テンプレートに値を渡す入れ物
import org.springframework.ui.Model;
// バリデーション結果を受け取る型
import org.springframework.validation.BindingResult;
// HTTP GET 用アノテーション
import org.springframework.web.bind.annotation.GetMapping;
// HTTP POST 用アノテーション
import org.springframework.web.bind.annotation.PostMapping;
// リダイレクト時に1回限りのデータを渡す型
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// final フィールドを引数に取るコンストラクタを Lombok に生成させる
import lombok.RequiredArgsConstructor;

// Spring に Controller として登録する
@Controller
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// 管理者の新規登録（サインアップ）画面を担当する Controller
public class UserRegisterController {

    // 管理者登録処理を担う Service
    private final UserService userService;

    // HTTP GET "/admin/register" にマッピング：新規登録フォームを表示する
    @GetMapping("/admin/register")
    public String showForm(Model model) {
        // 空の Form をモデルに入れる（テンプレートの th:object="${userRegisterForm}" と一致させる）
        model.addAttribute("userRegisterForm", new UserRegisterForm());
        // 新規登録テンプレート（templates/user/register.html）を返す
        return "user/register";
    }

    // HTTP POST "/admin/register" にマッピング：フォーム送信内容で管理者を登録する
    @PostMapping("/admin/register")
    public String register(
            // フォームのバリデーションを発動させる
            @Valid UserRegisterForm userRegisterForm,
            // バリデーション結果（@Valid の直後に置く）
            BindingResult bindingResult,
            // 成功時のフラッシュメッセージ用
            RedirectAttributes redirectAttributes) {

        // バリデーションエラーがあれば、フォームを再描画する
        if (bindingResult.hasErrors()) {
            // 入力値とエラーは BindingResult から自動復元されるので、テンプレート名を返すだけでOK
            return "user/register";
        }

        try {
            // 業務ロジックを Service に委譲する（重複チェック・ハッシュ化・DB保存）
            userService.register(userRegisterForm);
        } catch (IllegalArgumentException e) {
            // 業務ルール違反（メール重複など）はフォームエラーとして再描画する
            bindingResult.reject("global.error", e.getMessage());
            return "user/register";
        }

        // 成功メッセージをフラッシュ属性に詰める（ログイン画面に遷移して表示する想定）
        redirectAttributes.addFlashAttribute("message", "管理者として登録しました。ログインしてください");
        // ログイン画面にリダイレクト（PRGパターン）
        return "redirect:/login";
    }
}
