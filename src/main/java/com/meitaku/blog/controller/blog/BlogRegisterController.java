// このクラスが属するパッケージを宣言する（controller/blog パッケージ＝ブログ関連の Controller を置く場所）
package com.meitaku.blog.controller.blog;

// 新規登録画面の入力を表す Form クラスをインポートする
import com.meitaku.blog.form.blog.BlogRegisterForm;
// カテゴリー一覧取得を担う Service をインポートする（プルダウン選択肢を出すため）
import com.meitaku.blog.service.CategoryService;
// ブログ登録処理を担う Service をインポートする
import com.meitaku.blog.service.BlogService;

// このクラスを「画面を返す Controller」として Spring に認識させるためのアノテーション
import org.springframework.stereotype.Controller;
// テンプレートに値を渡すための入れ物
import org.springframework.ui.Model;
// 入力チェック（jakarta.validation のアノテーションを発動させる）を有効化するアノテーション
import jakarta.validation.Valid;
// バリデーション結果（エラーの有無）を受け取る型をインポートする
import org.springframework.validation.BindingResult;
// HTTP GET リクエスト用アノテーション
import org.springframework.web.bind.annotation.GetMapping;
// HTTP POST リクエスト用アノテーション
import org.springframework.web.bind.annotation.PostMapping;
// モデル属性名を明示的に指定するためのアノテーション（テンプレートの ${blogForm} と紐づける）
import org.springframework.web.bind.annotation.ModelAttribute;
// リダイレクト先にフラッシュメッセージ（1回限りのデータ）を渡すための型
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// final フィールドを引数に取るコンストラクタを Lombok に生成させる
import lombok.RequiredArgsConstructor;

// このクラスを Spring に Controller として登録する
@Controller
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// ブログ新規登録（フォーム表示 ＋ 登録処理）を担当する Controller
public class BlogRegisterController {

    // ブログ登録処理を担う Service
    private final BlogService blogService;
    // カテゴリー一覧取得を担う Service（フォームの選択肢用）
    private final CategoryService categoryService;

    // HTTP GET "/admin/blogs/new" にマッピング：新規登録フォームを表示する
    @GetMapping("/admin/blogs/new")
    public String showForm(Model model) {
        // 空の Form をモデルに入れる（テンプレートの th:object="${blogForm}" と一致させる）
        model.addAttribute("blogForm", new BlogRegisterForm());
        // カテゴリー選択肢用に全カテゴリーをモデルに入れる
        model.addAttribute("categories", categoryService.findAll());
        // テンプレート側で「これは編集ではない」と判定できるようにフラグを渡す
        model.addAttribute("isEdit", false);
        // ブログ登録／編集兼用テンプレートを返す
        return "admin/blog/form";
    }

    // HTTP POST "/admin/blogs" にマッピング：フォームから送られてきた値で新規登録する
    @PostMapping("/admin/blogs")
    public String register(
            // ★ @ModelAttribute("blogForm") を必ず付ける ★
            //   付けないと Spring はクラス名から "blogRegisterForm" として model に入れてしまい、
            //   テンプレートの th:object="${blogForm}" と名前がずれて再描画時に NullPointerException 相当のエラーになる
            // @Valid を付けると Form クラスのバリデーションアノテーション（@NotBlank等）が発動する
            @Valid @ModelAttribute("blogForm") BlogRegisterForm blogForm,
            // バリデーション結果は @Valid の直後の引数に置く（順番が大事）
            BindingResult bindingResult,
            // モデル（エラー時に再表示するため）
            Model model,
            // リダイレクト先に成功メッセージを渡すための型
            RedirectAttributes redirectAttributes) {

        // バリデーションエラーがあれば、フォームを再描画する（404でも500でもなく入力画面に戻す）
        if (bindingResult.hasErrors()) {
            // カテゴリー選択肢を再度モデルに詰める（再描画でドロップダウンを空にしないため）
            model.addAttribute("categories", categoryService.findAll());
            // 編集ではないフラグも再度付ける
            model.addAttribute("isEdit", false);
            // ブログ登録／編集兼用テンプレートを再表示する（入力値とエラーは bindingResult から自動で復元される）
            return "admin/blog/form";
        }

        try {
            // 業務ロジックを Service に委譲する
            blogService.register(blogForm);
        } catch (IllegalArgumentException e) {
            // Service が「業務ルール違反（例：重複）」を投げてきたら、フォームエラーとして再描画する
            bindingResult.reject("global.error", e.getMessage());
            // 同じくカテゴリー選択肢を再度モデルに詰める
            model.addAttribute("categories", categoryService.findAll());
            // 編集ではないフラグも再度付ける
            model.addAttribute("isEdit", false);
            // 再描画する
            return "admin/blog/form";
        }

        // 成功メッセージをフラッシュ属性に詰める（次の画面で1回だけ取れる）
        redirectAttributes.addFlashAttribute("message", "ブログを登録しました");
        // 一覧画面にリダイレクトする（POST後リダイレクト = PRGパターン。F5で多重送信を防ぐ）
        return "redirect:/admin/blogs";
    }
}
