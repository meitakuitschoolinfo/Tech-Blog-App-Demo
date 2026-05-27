// このクラスが属するパッケージを宣言する（controller/category パッケージ＝カテゴリー管理用 Controller を置く場所）
package com.meitaku.blog.controller.category;

// カテゴリーフォームをインポートする
import com.meitaku.blog.form.category.CategoryForm;
// カテゴリー CRUD を担う Service をインポートする
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
// クラスレベルで共通URLプレフィックスを指定するアノテーション
import org.springframework.web.bind.annotation.RequestMapping;
// リダイレクト時に1回限りのデータを渡す型
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// final フィールドを引数に取るコンストラクタを Lombok に生成させる
import lombok.RequiredArgsConstructor;

// Spring に Controller として登録する
@Controller
// このクラスのすべての @GetMapping/@PostMapping の先頭に "/admin/categories" を付ける
@RequestMapping("/admin/categories")
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// カテゴリー管理（一覧・登録・編集・削除）を1つにまとめた Controller
// ※ MDのパッケージ構成では Blog のように分割されているが、Category は機能が少ないため集約する
public class AdminCategoryController {

    // カテゴリー CRUD を担う Service
    private final CategoryService categoryService;

    // HTTP GET "/admin/categories" にマッピング：カテゴリー一覧を表示する
    @GetMapping
    public String list(Model model) {
        // 全カテゴリーをモデルに入れる
        model.addAttribute("categories", categoryService.findAll());
        // 管理画面用テンプレート（templates/admin/category/list.html）を返す
        return "admin/category/list";
    }

    // HTTP GET "/admin/categories/new" にマッピング：新規登録フォームを表示する
    @GetMapping("/new")
    public String showRegisterForm(Model model) {
        // 空の Form をモデルに入れる
        model.addAttribute("categoryForm", new CategoryForm());
        // テンプレート側で「これは編集ではない」と判定するためのフラグ
        model.addAttribute("isEdit", false);
        // カテゴリー登録／編集兼用テンプレートを返す
        return "admin/category/form";
    }

    // HTTP POST "/admin/categories" にマッピング：新規カテゴリーを登録する
    @PostMapping
    public String register(
            // フォームのバリデーションを発動させる
            @Valid CategoryForm categoryForm,
            // バリデーション結果（@Valid の直後に置く）
            BindingResult bindingResult,
            Model model,
            // 成功時のフラッシュメッセージ用
            RedirectAttributes redirectAttributes) {

        // バリデーションエラーなら再描画する
        if (bindingResult.hasErrors()) {
            // 編集ではないフラグを再度付ける
            model.addAttribute("isEdit", false);
            return "admin/category/form";
        }
        try {
            // 業務ロジックを Service に委譲する
            categoryService.register(categoryForm);
        } catch (IllegalArgumentException e) {
            // 業務ルール違反（重複など）はフォームエラーとして再描画する
            bindingResult.reject("global.error", e.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/category/form";
        }
        // 成功メッセージをフラッシュ属性に詰める
        redirectAttributes.addFlashAttribute("message", "カテゴリーを登録しました");
        // 一覧にリダイレクト
        return "redirect:/admin/categories";
    }

    // HTTP GET "/admin/categories/{id}/edit" にマッピング：編集フォームを表示する
    @GetMapping("/{id}/edit")
    public String showEditForm(
            // URL の {id} を Long で受け取る
            @PathVariable Long id,
            Model model) {

        // Service 経由で「DB の Category → CategoryForm」に詰め替えてから渡す
        model.addAttribute("categoryForm", categoryService.toEditForm(id));
        // テンプレート側で「これは編集モード」と判定するためのフラグ
        model.addAttribute("isEdit", true);
        // カテゴリー登録／編集兼用テンプレートを返す
        return "admin/category/form";
    }

    // HTTP POST "/admin/categories/{id}/edit" にマッピング：カテゴリーを更新する
    @PostMapping("/{id}/edit")
    public String update(
            // URL の {id} を受け取る
            @PathVariable Long id,
            @Valid CategoryForm categoryForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // URL の id を Form にも反映させる（hidden で来るが念のため）
        categoryForm.setId(id);

        // バリデーションエラーなら再描画する
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/category/form";
        }
        try {
            // 業務ロジックを Service に委譲する
            categoryService.update(categoryForm);
        } catch (IllegalArgumentException e) {
            // 業務ルール違反はフォームエラーとして再描画する
            bindingResult.reject("global.error", e.getMessage());
            model.addAttribute("isEdit", true);
            return "admin/category/form";
        }
        // 成功メッセージをフラッシュ属性に詰める
        redirectAttributes.addFlashAttribute("message", "カテゴリーを更新しました");
        // 一覧にリダイレクト
        return "redirect:/admin/categories";
    }

    // HTTP POST "/admin/categories/{id}/delete" にマッピング：1件削除する
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            // Service に削除を委譲する（使用中なら IllegalStateException）
            categoryService.delete(id);
            // 成功メッセージ
            redirectAttributes.addFlashAttribute("message", "カテゴリーを削除しました");
        } catch (IllegalStateException e) {
            // 使用中で削除できない場合はエラーメッセージをフラッシュに詰める
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // 結果に関わらず一覧にリダイレクト
        return "redirect:/admin/categories";
    }
}
