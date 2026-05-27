// このインターフェースが属するパッケージを宣言する（repository パッケージ＝DB操作を行う層）
package com.meitaku.blog.repository;

// このRepositoryが扱うEntity（User＝管理者ユーザー）をインポートする
import com.meitaku.blog.entity.User;
// Spring Data JPA が提供する標準Repositoryインターフェース
import org.springframework.data.jpa.repository.JpaRepository;
// このインターフェースを Spring の DI コンテナで使う「Bean」として明示するためのアノテーション
import org.springframework.stereotype.Repository;

// 「値が存在するかしないか」を型レベルで表現する Optional をインポートする（findByEmail の戻り値で使う）
import java.util.Optional;

// このインターフェースが「admin_users テーブル専用のDB操作窓口」であることを明示する
@Repository
// JpaRepository<User, Long> → 第1型引数=Entity、第2型引数=主キーの型
public interface UserRepository extends JpaRepository<User, Long> {

    // 「メールアドレスから管理者を1件取得する」検索メソッド
    // メソッド名 "findByEmail" を Spring Data JPA が解析して、
    //    SELECT * FROM admin_users WHERE email = ?
    // 相当のSQLを自動生成する
    // 戻り値が Optional<User> なのは「該当ユーザーが存在しない可能性がある」ため（PDF 3-7 参照）
    Optional<User> findByEmail(String email);

    // 「指定された email の管理者が既に存在するか？」を boolean で返す存在チェック専用メソッド
    // 新規登録時の重複チェック用（DBの UNIQUE 制約と組み合わせて「二段構え」で使う）
    boolean existsByEmail(String email);
}
