// このクラスが属するパッケージを宣言する（config パッケージ＝Spring の設定クラスを置く場所）
package com.meitaku.blog.config;

// このクラスを「設定クラス」として認識させるためのアノテーションをインポート
import org.springframework.context.annotation.Configuration;
// Spring MVC の各種設定（リソースハンドラ等）を拡張するためのインターフェース
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// 上記の親インターフェース。実装するとSpringがMVCの設定を拾ってくれる
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Spring に「Spring MVCの追加設定があります」と伝える
@Configuration
// アップロードされた画像（プロジェクト直下 uploads/ フォルダ）を /uploads/** で配信できるようにする設定クラス
public class WebMvcConfig implements WebMvcConfigurer {

    // ブラウザからの静的リソース要求と、サーバー上のフォルダのマッピングを定義する
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** へのリクエストを uploads/ フォルダの中身に紐づける
        registry.addResourceHandler("/uploads/**")
                // "file:" 接頭辞でファイルシステム上のパスを指定する（末尾の "/" は必須）
                .addResourceLocations("file:./uploads/");
    }
}
