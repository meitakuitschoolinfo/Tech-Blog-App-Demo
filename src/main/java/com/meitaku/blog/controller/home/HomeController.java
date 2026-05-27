// このクラスが属するパッケージを宣言する（controller/home パッケージ＝トップページ用）
package com.meitaku.blog.controller.home;

// このクラスを「画面を返す Controller」として Spring に認識させるためのアノテーションをインポートする
import org.springframework.stereotype.Controller;
// HTTP GET リクエストを受け取るためのアノテーションをインポートする
import org.springframework.web.bind.annotation.GetMapping;

// このクラスを Spring の DI コンテナに「画面を返す Controller」として登録する
@Controller
// アプリ全体のトップページ（/）を担当する Controller
public class HomeController {

    // HTTP GET "/" にマッピングするメソッド
    @GetMapping("/")
    public String index() {
        // "redirect:/blogs" を返すと、ブラウザは /blogs に再アクセスする（PRGパターンの先取り）
        return "redirect:/blogs";
    }
}
