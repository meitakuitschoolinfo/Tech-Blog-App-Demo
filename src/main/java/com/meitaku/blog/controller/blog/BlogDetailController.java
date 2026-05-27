// このクラスが属するパッケージを宣言する（controller/blog パッケージ＝ブログ関連の Controller を置く場所）
package com.meitaku.blog.controller.blog;

// ブログ Entity をインポートする
import com.meitaku.blog.entity.Blog;
// ブログ取得を担う Service をインポートする
import com.meitaku.blog.service.BlogService;
// markdown→HTML 変換を担う Service をインポートする
import com.meitaku.blog.service.MarkdownService;

// このクラスを「画面を返す Controller」として Spring に認識させるためのアノテーション
import org.springframework.stereotype.Controller;
// テンプレートに値を渡すための入れ物（Model）をインポートする
import org.springframework.ui.Model;
// HTTP GET リクエストを受け取るためのアノテーションをインポートする
import org.springframework.web.bind.annotation.GetMapping;
// URL の中の {id} のような値を引数にバインドするためのアノテーション
import org.springframework.web.bind.annotation.PathVariable;

// final フィールドを引数に取るコンストラクタを Lombok に生成させる
import lombok.RequiredArgsConstructor;

// このクラスを Spring に「画面を返す Controller」として登録する
@Controller
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// ブログ詳細画面（ユーザー向け）を担当する Controller
public class BlogDetailController {

    // ブログ取得処理を担う Service
    private final BlogService blogService;
    // 本文の markdown を HTML に変換する Service（@RequiredArgsConstructor がコンストラクタを生成）
    private final MarkdownService markdownService;

    // HTTP GET "/blogs/{id}" にマッピング：1件の記事詳細を表示する
    @GetMapping("/blogs/{id}")
    public String detail(
            // URL の {id} 部分を Long に変換して受け取る
            @PathVariable Long id,
            // テンプレートへ値を渡す入れ物
            Model model) {

        // Service から1件取得する（無ければ ResourceNotFoundException → HTTP 404）
        Blog blog = blogService.findById(id);
        // テンプレートで使えるように blog をモデルに入れる
        model.addAttribute("blog", blog);
        // 本文の markdown を HTML に変換してモデルに入れる
        // テンプレートでは th:utext="${contentHtml}" で「エスケープせず」描画する
        model.addAttribute("contentHtml", markdownService.toHtml(blog.getContent()));
        // ユーザー向け詳細テンプレート（templates/blog/detail.html）を返す
        return "blog/detail";
    }
}
