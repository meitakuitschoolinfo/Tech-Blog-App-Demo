// このインターフェースが属するパッケージを宣言する（repository パッケージ＝DB操作を行う層）
package com.meitaku.blog.repository;

// このRepositoryが扱うEntity（Category）をインポートする
import com.meitaku.blog.entity.Category;
// Spring Data JPA が提供する標準Repositoryインターフェース（CRUDメソッドを最初から持っている）
import org.springframework.data.jpa.repository.JpaRepository;

// このインターフェースを Spring の DI コンテナで使う「Bean」として明示するためのアノテーション
// （JpaRepository を継承していれば省略可能だが、研修では役割を明示するために書く）
import org.springframework.stereotype.Repository;

// このインターフェースが「Categoryテーブル専用のDB操作窓口」であることを明示する
@Repository
// JpaRepository を継承するだけで、save / findById / findAll / delete などの基本CRUDが自動で備わる
//   - 第1型引数 Category → このRepositoryが扱うEntity
//   - 第2型引数 Long     → Categoryの主キー（@Id）の型
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 「指定された name のカテゴリーが既に存在するか？」を boolean で返すメソッド
    // メソッド名 "existsByName" を Spring Data JPA が解析し、
    // 内部で  SELECT count(*) FROM categories WHERE name = ?  相当のSQLを自動生成する
    boolean existsByName(String name);
}
