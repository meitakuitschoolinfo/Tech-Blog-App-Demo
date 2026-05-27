// このクラスが属するパッケージを宣言する（service パッケージ＝業務ロジック層）
package com.meitaku.blog.service;

// CommonMark の拡張機能の基底型をインポート（複数拡張をListで渡すために使う）
import org.commonmark.Extension;
// GFM形式の表（| col1 | col2 |）をサポートする拡張をインポート
import org.commonmark.ext.gfm.tables.TablesExtension;
// パース結果（構文木のルート）を表す型をインポート
import org.commonmark.node.Node;
// markdown文字列を構文木に変換するパーサーをインポート
import org.commonmark.parser.Parser;
// 構文木をHTMLに変換するレンダラーをインポート
import org.commonmark.renderer.html.HtmlRenderer;
// このクラスを Spring の Service Bean として認識させるためのアノテーション
import org.springframework.stereotype.Service;

// 複数の拡張をまとめて渡すために List を使う
import java.util.List;

// Spring の DI コンテナに登録する
@Service
// markdown文字列を HTML 文字列に変換するサービス
// 画面表示時に Controller から呼ばれる（テンプレートでは th:utext で出力する）
public class MarkdownService {

    // markdown のパース・レンダリングは「設定済みインスタンス」を使い回す方が安全＆高速なので final で保持する
    private final Parser parser;
    private final HtmlRenderer renderer;

    // 起動時に1度だけ Parser / HtmlRenderer を構築する
    public MarkdownService() {
        // 使う拡張機能のリスト（今は表だけだが、必要に応じて autolink / strikethrough 等を追加できる）
        List<Extension> extensions = List.of(TablesExtension.create());
        // 拡張を組み込んでパーサーを構築する
        this.parser = Parser.builder()
                .extensions(extensions)
                .build();
        // 拡張を組み込んでレンダラーを構築する
        this.renderer = HtmlRenderer.builder()
                .extensions(extensions)
                // markdown 内の <script> や <div> 等の生HTMLをエスケープする（XSS対策）
                // 管理者が信頼できる前提でも、習慣としてONにしておくと安全
                .escapeHtml(true)
                .build();
    }

    // markdown 文字列を HTML 文字列に変換して返す
    // null や空文字なら空文字を返す（テンプレートで分岐させる手間を減らす）
    public String toHtml(String markdown) {
        // null・空文字ガード
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        // markdown をパースして構文木（Node）に変換する
        Node document = parser.parse(markdown);
        // 構文木をHTML文字列に変換して返す
        return renderer.render(document);
    }
}
