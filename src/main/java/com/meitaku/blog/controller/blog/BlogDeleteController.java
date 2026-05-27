// このクラスが属するパッケージを宣言する（controller/blog パッケージ＝ブログ関連の Controller を置く場所）
package com.meitaku.blog.controller.blog;

// ブログ削除を担う Service をインポートする
import com.meitaku.blog.service.BlogService;

// このクラスを Controller として認識させるアノテーション
import org.springframework.stereotype.Controller;
// URL パスから値を取り出すアノテーション
import org.springframework.web.bind.annotation.PathVariable;
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
// ブログ削除処理を担当する Controller
// ※ 削除は安全のため必ず POST で受ける（GET だと URL を踏むだけで消せてしまう）
public class BlogDeleteController {

    // ブログ削除を担う Service
    private final BlogService blogService;

    // HTTP POST "/admin/blogs/{id}/delete" にマッピング：1件削除する
    @PostMapping("/admin/blogs/{id}/delete")
    public String delete(
            // URL の {id} を Long で受け取る
            @PathVariable Long id,
            // 成功メッセージのフラッシュ用
            RedirectAttributes redirectAttributes) {

        // Service に削除を委譲する（無ければ ResourceNotFoundException → HTTP 404）
        blogService.delete(id);
        // 成功メッセージをフラッシュ属性に詰める
        redirectAttributes.addFlashAttribute("message", "ブログを削除しました");
        // 一覧画面にリダイレクトする
        return "redirect:/admin/blogs";
    }
}
