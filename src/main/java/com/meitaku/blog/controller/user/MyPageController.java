// このクラスが属するパッケージを宣言する（controller/user パッケージ＝ユーザー関連の Controller を置く場所）
package com.meitaku.blog.controller.user;

// 管理者ユーザーの Entity をインポートする
import com.meitaku.blog.entity.User;
// 編集兼用フォームをインポートする（UserRegisterForm を流用する）
import com.meitaku.blog.form.user.UserRegisterForm;
// 管理者取得／更新を担う Service をインポートする
import com.meitaku.blog.service.UserService;

// 入力チェックを発動させるアノテーション
import jakarta.validation.Valid;
// ログイン中ユーザーの情報を引数で受け取るためのアノテーション
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// Spring Security 上の「ログイン中ユーザーの型」をインポートする
import org.springframework.security.core.userdetails.UserDetails;
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
// 管理者「マイページ（自分のプロフィール編集）」画面を担当する Controller
public class MyPageController {

    // 管理者取得／更新を担う Service
    private final UserService userService;

    // HTTP GET "/admin/profile" にマッピング：自分のプロフィール編集フォームを表示する
    @GetMapping("/admin/profile")
    public String showForm(
            // Spring Security から「現在ログイン中のユーザー情報」を受け取る
            @AuthenticationPrincipal UserDetails principal,
            Model model) {

        // ログイン中ユーザーの email を頼りに DB から最新の User を取り直す
        User currentUser = userService.findByEmail(principal.getUsername());
        // 編集画面用に「DB の User → UserRegisterForm」に詰め替える
        UserRegisterForm form = userService.toEditForm(currentUser.getId());
        // テンプレートに渡す
        model.addAttribute("userRegisterForm", form);
        // テンプレートで id（URL組み立て用）にアクセスできるように別途渡す
        model.addAttribute("userId", currentUser.getId());
        // マイページテンプレート（templates/user/profile.html）を返す
        return "user/profile";
    }

    // HTTP POST "/admin/profile" にマッピング：自分のプロフィールを更新する
    @PostMapping("/admin/profile")
    public String update(
            // ログイン中ユーザーの情報
            @AuthenticationPrincipal UserDetails principal,
            // フォーム＋バリデーション
            @Valid UserRegisterForm userRegisterForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // ログイン中ユーザーの id を取得する（URLの id では無く必ずログイン中のIDを使う ＝ 改ざん防止）
        User currentUser = userService.findByEmail(principal.getUsername());

        // バリデーションエラーがあれば、フォームを再描画する
        if (bindingResult.hasErrors()) {
            // テンプレートで id を再表示するために再度モデルに詰める
            model.addAttribute("userId", currentUser.getId());
            return "user/profile";
        }

        try {
            // 業務ロジックを Service に委譲する（重複チェック・ハッシュ化・DB更新）
            userService.update(currentUser.getId(), userRegisterForm);
        } catch (IllegalArgumentException e) {
            // 業務ルール違反（メール重複など）はフォームエラーとして再描画する
            bindingResult.reject("global.error", e.getMessage());
            model.addAttribute("userId", currentUser.getId());
            return "user/profile";
        }

        // 成功メッセージをフラッシュ属性に詰める
        redirectAttributes.addFlashAttribute("message", "プロフィールを更新しました");
        // 同じマイページにリダイレクト（PRGパターン）
        return "redirect:/admin/profile";
    }
}
