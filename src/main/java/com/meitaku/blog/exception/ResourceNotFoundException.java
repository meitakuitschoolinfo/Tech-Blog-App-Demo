// このクラスが属するパッケージを宣言する（exception パッケージ＝カスタム例外を置く場所）
package com.meitaku.blog.exception;

// HTTPステータスコードを表す enum をインポートする
import org.springframework.http.HttpStatus;
// 例外クラスに「HTTPレスポンスのステータスを紐づける」アノテーションをインポートする
import org.springframework.web.bind.annotation.ResponseStatus;

// この例外がスローされたら、Spring が自動でHTTP 404 (Not Found) を返すように指定する
@ResponseStatus(HttpStatus.NOT_FOUND)
// 「探したリソース（DBの1件）が見つからなかった」ことを表すカスタム実行時例外
public class ResourceNotFoundException extends RuntimeException {

    // メッセージ付きでスローできるようにするコンストラクタ
    public ResourceNotFoundException(String message) {
        // 親クラス（RuntimeException）にメッセージを渡す
        super(message);
    }
}
