// このクラスが属するパッケージを宣言する（service パッケージ＝業務ロジック層）
package com.meitaku.blog.service;

// 起動時にディレクトリ作成等を一度だけ走らせるためのアノテーションをインポート
import jakarta.annotation.PostConstruct;
// このクラスを Spring の Service Bean として認識させるためのアノテーション
import org.springframework.stereotype.Service;
// 画面からアップロードされたファイルを受け取るための型をインポート
import org.springframework.web.multipart.MultipartFile;

// 入出力例外（IOException）をインポート
import java.io.IOException;
// IOException を非チェック例外に包んで投げ直すための型をインポート
import java.io.UncheckedIOException;
// ファイルシステム操作の標準API（Files）をインポート
import java.nio.file.Files;
// ファイル／ディレクトリのパスを表す型をインポート
import java.nio.file.Path;
// 文字列からPathを作るためのファクトリをインポート
import java.nio.file.Paths;
// 衝突しないファイル名を作るためにUUIDをインポート
import java.util.UUID;

// このクラスを Spring の DI コンテナに「Service Bean」として登録する
@Service
// アップロードされた画像をローカルディスクに保存して、配信用URLを返すサービス
public class FileStorageService {

    // アップロードファイルの保存先ディレクトリ（プロジェクト直下の uploads/ フォルダ）
    private final Path uploadDir = Paths.get("uploads");

    // Bean生成直後に一度だけ呼ばれる初期化メソッド
    @PostConstruct
    public void init() {
        try {
            // 保存先ディレクトリが存在しなければ作成する（既にあっても例外は出ない）
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            // ディレクトリ作成に失敗したら起動を止める意味で例外を投げ直す
            throw new UncheckedIOException("アップロード用ディレクトリの作成に失敗しました", e);
        }
    }

    // アップロードされたファイルを保存し、ブラウザから閲覧するための公開URL（例: /uploads/xxx.jpg）を返す
    public String store(MultipartFile file) {
        // 空ファイル／未選択の場合は何もせず null を返す（呼び出し側で null チェックする）
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            // 元ファイル名から拡張子（.jpg .png 等）を抜き出す（無ければ空文字）
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            // 衝突しないファイル名を作る：UUID ＋ 拡張子（例: 5f0b...e3.jpg）
            String filename = UUID.randomUUID() + ext;
            // 保存先のフルパスを組み立てる（uploads/5f0b...e3.jpg）
            Path target = uploadDir.resolve(filename);
            // 受け取った MultipartFile を上記パスに保存する
            file.transferTo(target.toAbsolutePath());
            // ブラウザがアクセスするためのURLを返す（WebMvcConfig で /uploads/** が公開される）
            return "/uploads/" + filename;
        } catch (IOException e) {
            // 保存に失敗したら呼び出し側で扱いやすいよう非チェック例外に包む
            throw new UncheckedIOException("画像の保存に失敗しました", e);
        }
    }
}
