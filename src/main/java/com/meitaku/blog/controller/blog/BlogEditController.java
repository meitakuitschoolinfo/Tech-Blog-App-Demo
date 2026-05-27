// このクラスが属するパッケージを宣言する（controller/blog パッケージ＝ブログ関連の Controller を置く場所）
package com.meitaku.blog.controller.blog;

// 編集画面の入力を表す Form クラスをインポートする
import com.meitaku.blog.form.blog.BlogEditForm;
// ブログ取得／更新を担う Service をインポートする
import com.meitaku.blog.service.BlogService;
// カテゴリー一覧取得を担う Service をインポートする
import com.meitaku.blog.service.CategoryService;

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
// URL パスから値を取り出すアノテーション
import org.springframework.web.bind.annotation.PathVariable;
// HTTP POST 用アノテーション
import org.springframework.web.bind.annotation.PostMapping;
// モデル属性名を明示的に指定するためのアノテーション（テンプレートの ${blogForm} と紐づける）
import org.springframework.web.bind.annotation.ModelAttribute;
// リダイレクト時に1回限りのデータを渡す型
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// final フィールドを引数に取るコンストラクタを Lombok に生成させる
import lombok.RequiredArgsConstructor;

// Spring に Controller として登録する
@Controller
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// ブログ編集（フォーム表示 ＋ 更新処理）を担当する Controller
public class BlogEditController {

    // ブログ取得／更新処理を担う Service
    private final BlogService blogService;
    // カテゴリー一覧取得を担う Service（フォームの選択肢用）
    private final CategoryService categoryService;

    // HTTP GET "/admin/blogs/{id}/edit" にマッピング：編集フォームを表示する
    @GetMapping("/admin/blogs/{id}/edit")
    public String showForm(
            // URL の {id} 部分を受け取る
            @PathVariable Long id,
            Model model) {

        // Service 経由で「DB の Blog → BlogEditForm」に詰め替えてから渡す
        model.addAttribute("blogForm", blogService.toEditForm(id));
        // カテゴリー選択肢
        model.addAttribute("categories", categoryService.findAll());
        // テンプレート側で「これは編集モード」と判定するためのフラグ
        model.addAttribute("isEdit", true);
        // ブログ登録／編集兼用テンプレート
        return "admin/blog/form";
    }

    // HTTP POST "/admin/blogs/{id}/edit" にマッピング：フォーム送信内容で更新する
    @PostMapping("/admin/blogs/{id}/edit")
    public String update(
            // URL の {id} を受け取る（Form 内の id と一致するはずだが、URL のものを正とする）
            @PathVariable Long id,
            // ★ @ModelAttribute("blogForm") を必ず付ける ★
            //   付けないと Spring はクラス名から "blogEditForm" として model に入れてしまい、
            //   テンプレートの th:object="${blogForm}" と名前がずれて再描画時にエラーになる
            // フォームのバリデーションを発動させる
            @Valid @ModelAttribute("blogForm") BlogEditForm blogForm,
            // バリデーション結果（@Valid の直後に置く）
            BindingResult bindingResult,
            Model model,
            // 成功時のフラッシュメッセージ用
            RedirectAttributes redirectAttributes) {

        // URL の id を Form にも反映させる（hidden で送られてくるが念のため）
        blogForm.setId(id);

        // バリデーションエラーなら再描画する
        if (bindingResult.hasErrors()) {
            // 選択肢を再度モデルに詰める
            model.addAttribute("categories", categoryService.findAll());
            // 編集モードのフラグを再度立てる
            model.addAttribute("isEdit", true);
            // 兼用テンプレートを再描画する（入力値とエラーは BindingResult から自動復元）
            return "admin/blog/form";
        }

        try {
            // 業務ロジックを Service に委譲する
            blogService.update(blogForm);
        } catch (IllegalArgumentException e) {
            // 業務ルール違反はフォームエラーとして再描画する
            bindingResult.reject("global.error", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/blog/form";
        }

        // 成功メッセージをフラッシュ属性に詰める
        redirectAttributes.addFlashAttribute("message", "ブログを更新しました");
        // 一覧画面にリダイレクトする（PRGパターン）
        return "redirect:/admin/blogs";
    }
}
