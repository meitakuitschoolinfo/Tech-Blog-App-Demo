// このクラスが属するパッケージを宣言する（controller/blog パッケージ＝ブログ関連の Controller を置く場所）
package com.meitaku.blog.controller.blog;

// ブログのEntityをインポートする
import com.meitaku.blog.entity.Blog;
// ブログ取得を担う Service をインポートする
import com.meitaku.blog.service.BlogService;

// ページング結果（Page）を表す型をインポートする
import org.springframework.data.domain.Page;
// ページング条件（Pageable）を表す型をインポートする
import org.springframework.data.domain.Pageable;
// ページングのデフォルト値（1ページ件数など）を指定するアノテーションをインポートする
import org.springframework.data.web.PageableDefault;
// このクラスを「画面を返す Controller」として Spring に認識させるためのアノテーション
import org.springframework.stereotype.Controller;
// テンプレートに値を渡すための入れ物（Model）をインポートする
import org.springframework.ui.Model;
// HTTP GET リクエストを受け取るためのアノテーションをインポートする
import org.springframework.web.bind.annotation.GetMapping;

// final フィールドを引数に取るコンストラクタを Lombok に生成させる（コンストラクタインジェクション用）
import lombok.RequiredArgsConstructor;

// このクラスを Spring に「画面を返す Controller」として登録する
@Controller
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// ブログ一覧画面（ユーザー向け / 管理画面向け）を担当する Controller
public class BlogListController {

    // ブログ取得処理を担う Service（コンストラクタインジェクションで注入される）
    private final BlogService blogService;

    // HTTP GET "/blogs" にマッピング：一般ユーザー向けブログ一覧画面
    @GetMapping("/blogs")
    public String listForUser(
            // クエリパラメータ ?page=N&size=M をマッピング。指定なしなら1ページ9件
            @PageableDefault(size = 9) Pageable pageable,
            // テンプレートへ値を渡すための入れ物
            Model model) {

        // Service 経由で「公開日時の新しい順」に1ページ分取得する
        Page<Blog> page = blogService.findLatest(pageable);
        // テンプレートで使えるように Page<Blog> をモデルに入れる（テンプレートで page.content を回す）
        model.addAttribute("page", page);
        // ユーザー向けテンプレートのファイルパス（templates/blog/list.html）を返す
        return "blog/list";
    }

    // HTTP GET "/admin/blogs" にマッピング：管理者向けブログ一覧画面
    @GetMapping("/admin/blogs")
    public String listForAdmin(
            // 管理画面は1ページ10件に変更（ユーザー画面とは別運用）
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        // 管理画面でも同じ Service メソッドを使う（並び順は同じ）
        Page<Blog> page = blogService.findLatest(pageable);
        // モデルに渡す
        model.addAttribute("page", page);
        // 管理画面用テンプレートのファイルパス（templates/admin/blog/list.html）を返す
        return "admin/blog/list";
    }
}
