// このインターフェースが属するパッケージを宣言する（repository パッケージ＝DB操作を行う層）
package com.meitaku.blog.repository;

// このRepositoryが扱うEntity（Blog）をインポートする
import com.meitaku.blog.entity.Blog;
// このRepositoryがメソッド引数で受け取るEntity（Category）をインポートする
import com.meitaku.blog.entity.Category;
// ページ単位の検索結果（1ページ分のList＋総件数＋総ページ数）を表す型をインポートする
import org.springframework.data.domain.Page;
// ページング条件（ページ番号・1ページ件数・並び順）を表す型をインポートする
import org.springframework.data.domain.Pageable;
// Spring Data JPA が提供する標準Repositoryインターフェース
import org.springframework.data.jpa.repository.JpaRepository;
// このインターフェースを Spring の DI コンテナで使う「Bean」として明示するためのアノテーション
import org.springframework.stereotype.Repository;

// このインターフェースが「blogs テーブル専用のDB操作窓口」であることを明示する
@Repository
// JpaRepository<Blog, Long> → 第1型引数=Entity、第2型引数=主キーの型
// JpaRepository は内部で findAll(Pageable) も提供しているため、ページング検索もすぐ使える
public interface BlogRepository extends JpaRepository<Blog, Long> {

    // 「公開日時の新しい順」でページング検索する標準的なブログ一覧用メソッド
    // メソッド名 "findAllByOrderByPublishedAtDesc" を Spring Data JPA が解析し、
    //    SELECT * FROM blogs ORDER BY published_at DESC LIMIT ? OFFSET ?
    // 相当のSQLを自動生成する
    Page<Blog> findAllByOrderByPublishedAtDesc(Pageable pageable);

    // 指定カテゴリーに属するブログを「公開日時の新しい順」でページング検索するメソッド
    // メソッド名 "findByCategoryOrderByPublishedAtDesc" を Spring Data JPA が解析し、
    //    SELECT * FROM blogs WHERE category_id = ? ORDER BY published_at DESC LIMIT ? OFFSET ?
    // 相当のSQLを自動生成する
    // 引数の category は Entityオブジェクトを渡す（JPAが内部で .id を取り出してくれる）
    Page<Blog> findByCategoryOrderByPublishedAtDesc(Category category, Pageable pageable);

    // 「指定カテゴリーに紐づくブログが何件あるか」を返す件数メソッド
    // カテゴリー削除前に「使われていないか？」をチェックする用途で使う
    //    SELECT count(*) FROM blogs WHERE category_id = ?
    long countByCategory(Category category);
}
