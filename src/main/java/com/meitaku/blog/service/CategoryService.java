// このクラスが属するパッケージを宣言する（service パッケージ＝業務ロジック層）
package com.meitaku.blog.service;

// このサービスが扱う Entity（Category）をインポートする
import com.meitaku.blog.entity.Category;
// 「探したけど見つからなかった」場合に投げるカスタム例外をインポートする
import com.meitaku.blog.exception.ResourceNotFoundException;
// 画面の入力1件を表す Form クラスをインポートする
import com.meitaku.blog.form.category.CategoryForm;
// ブログ件数チェックに使う BlogRepository をインポートする
import com.meitaku.blog.repository.BlogRepository;
// Category の CRUD を担う CategoryRepository をインポートする
import com.meitaku.blog.repository.CategoryRepository;

// Spring がこのクラスを「Service の Bean」として認識するためのアノテーションをインポートする
import org.springframework.stereotype.Service;
// メソッド単位でトランザクションを張るためのアノテーションをインポートする
import org.springframework.transaction.annotation.Transactional;

// final フィールドを引数に取るコンストラクタを Lombok に自動生成させる（コンストラクタインジェクション用）
import lombok.RequiredArgsConstructor;

// 戻り値として複数件を返すための List をインポートする
import java.util.List;

// このクラスを Spring の DI コンテナに登録し、Service 層の Bean として扱わせる
@Service
// クラスレベルで「読み取り専用トランザクション」を既定値にする（参照系メソッドの省略コードを減らす）
@Transactional(readOnly = true)
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// カテゴリーに関する業務ロジックを担当するサービスクラス
public class CategoryService {

    // CategoryRepository をフィールドとして注入する（final だから @RequiredArgsConstructor の対象）
    private final CategoryRepository categoryRepository;
    // BlogRepository を注入する（削除前の使用件数チェックに使う）
    private final BlogRepository blogRepository;

    // 全カテゴリーを取得して返す（管理画面のドロップダウンや一覧で使う）
    public List<Category> findAll() {
        // JpaRepository が提供する findAll() をそのまま返す
        return categoryRepository.findAll();
    }

    // ID 指定で1件取得する。見つからなければ ResourceNotFoundException を投げる
    public Category findById(Long id) {
        // findById は Optional を返す → orElseThrow で「無い場合は例外」を明示する（PDF 3-7）
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません: id=" + id));
    }

    // 編集画面の初期表示用に「DB の Category」を「画面の CategoryForm」に詰め替えるユーティリティ
    public CategoryForm toEditForm(Long id) {
        // まず対象カテゴリーを取得する（無ければ例外）
        Category category = findById(id);
        // Form を新規生成する
        CategoryForm form = new CategoryForm();
        // 編集対象を区別するために id を入れる（hidden で画面に保持される）
        form.setId(category.getId());
        // 画面の name 入力欄を埋める
        form.setName(category.getName());
        // 完成した Form を返す
        return form;
    }

    // 新規登録：書き込み系なのでデフォルトの readOnly を上書きする
    @Transactional
    public Category register(CategoryForm form) {
        // 同名カテゴリーが既にあれば登録させない（Service レベルの重複チェック）
        if (categoryRepository.existsByName(form.getName())) {
            // 業務ルール違反は IllegalArgumentException で表現する
            throw new IllegalArgumentException("同じ名前のカテゴリーが既に存在します: " + form.getName());
        }
        // 空の Entity を生成する
        Category category = new Category();
        // Form の値を Entity に詰め替える
        category.setName(form.getName());
        // save() で INSERT 文が発行される（id は自動採番）
        return categoryRepository.save(category);
    }

    // 更新：書き込み系なので @Transactional を明示する
    @Transactional
    public Category update(CategoryForm form) {
        // 既存のレコードを取得する（無ければ ResourceNotFoundException）
        Category category = findById(form.getId());
        // 名前を変更する場合のみ、新しい名前の重複をチェックする
        if (!category.getName().equals(form.getName())
                && categoryRepository.existsByName(form.getName())) {
            // 業務ルール違反は IllegalArgumentException
            throw new IllegalArgumentException("同じ名前のカテゴリーが既に存在します: " + form.getName());
        }
        // 値を上書きする
        category.setName(form.getName());
        // save() で UPDATE 文が発行される（id が既にあるので UPDATE になる）
        // Entity の @PreUpdate で updated_at も自動更新される
        return categoryRepository.save(category);
    }

    // 削除：書き込み系なので @Transactional を明示する
    @Transactional
    public void delete(Long id) {
        // 対象を取得（無ければ ResourceNotFoundException）
        Category category = findById(id);
        // このカテゴリーを使っているブログ件数を数える
        long usageCount = blogRepository.countByCategory(category);
        // 1件でも使われていれば削除を拒否する（業務ルール）
        if (usageCount > 0) {
            // 業務ルール違反だが「状態が原因で実行不可」なので IllegalStateException を使う
            throw new IllegalStateException(
                    "このカテゴリーは " + usageCount + " 件のブログで使われているため削除できません");
        }
        // 上記チェックを通過したら削除を実行する（DELETE 文が発行される）
        categoryRepository.delete(category);
    }
}
