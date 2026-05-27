// このクラスが属するパッケージを宣言する（service パッケージ＝業務ロジック層）
package com.meitaku.blog.service;

// このサービスが扱う Entity（Blog）をインポートする
import com.meitaku.blog.entity.Blog;
// 関連先 Entity（Category）をインポートする（外部キー解決で使う）
import com.meitaku.blog.entity.Category;
// 「探したけど見つからなかった」場合に投げるカスタム例外をインポートする
import com.meitaku.blog.exception.ResourceNotFoundException;
// ブログ編集画面の入力を表す Form クラスをインポートする
import com.meitaku.blog.form.blog.BlogEditForm;
// ブログ新規登録画面の入力を表す Form クラスをインポートする
import com.meitaku.blog.form.blog.BlogRegisterForm;
// Blog の CRUD を担う Repository をインポートする
import com.meitaku.blog.repository.BlogRepository;
// Category の取得を担う Repository をインポートする
import com.meitaku.blog.repository.CategoryRepository;

// ページ単位の検索結果を表す型をインポートする
import org.springframework.data.domain.Page;
// ページング条件（ページ番号・1ページ件数・並び順）を表す型をインポートする
import org.springframework.data.domain.Pageable;
// このクラスを Spring の Service Bean として認識させるためのアノテーションをインポートする
import org.springframework.stereotype.Service;
// メソッド／クラス単位でトランザクションを張るためのアノテーションをインポートする
import org.springframework.transaction.annotation.Transactional;

// final フィールドを引数に取るコンストラクタを Lombok に自動生成させる（コンストラクタインジェクション用）
import lombok.RequiredArgsConstructor;

// このクラスを Spring の DI コンテナに登録し、Service 層として扱わせる
@Service
// クラスレベルで「読み取り専用トランザクション」を既定値にする（参照系メソッドの省略コードを減らす）
@Transactional(readOnly = true)
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// ブログ記事に関する業務ロジックを担当するサービスクラス
public class BlogService {

    // Blog の CRUD を行うリポジトリ（コンストラクタインジェクションで注入される）
    private final BlogRepository blogRepository;
    // Category を取得するためのリポジトリ（categoryId を Category Entity に解決する用）
    private final CategoryRepository categoryRepository;
    // アップロードされた画像をローカルディスクに保存して公開URLを返すサービス
    private final FileStorageService fileStorageService;

    // 公開日時の新しい順に1ページ分のブログを取得する（公開画面・管理画面の両方で利用）
    public Page<Blog> findLatest(Pageable pageable) {
        // メソッド名規約で SQL が自動生成される（PDF 3-10 / 第4章 4-7 参照）
        return blogRepository.findAllByOrderByPublishedAtDesc(pageable);
    }

    // 指定カテゴリーに属するブログを公開日時の新しい順に1ページ分取得する
    public Page<Blog> findByCategory(Category category, Pageable pageable) {
        // @ManyToOne フィールドを引数に取り、内部で category.getId() を SQL に渡してくれる
        return blogRepository.findByCategoryOrderByPublishedAtDesc(category, pageable);
    }

    // ID 指定で1件取得する。見つからなければ ResourceNotFoundException を投げる
    public Blog findById(Long id) {
        // findById の戻り値 Optional<Blog> を orElseThrow で開けて、無ければ例外を投げる
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ブログ記事が見つかりません: id=" + id));
    }

    // 編集画面の初期表示用に「DB の Blog」を「画面の BlogEditForm」に詰め替えるユーティリティ
    public BlogEditForm toEditForm(Long id) {
        // 対象ブログを取得（無ければ例外）
        Blog blog = findById(id);
        // 空の Edit Form を生成する
        BlogEditForm form = new BlogEditForm();
        // hidden 用に id を入れる
        form.setId(blog.getId());
        // 入力欄を埋める
        form.setTitle(blog.getTitle());
        // 本文も埋める
        form.setContent(blog.getContent());
        // 任意項目の画像URLも埋める
        form.setImageUrl(blog.getImageUrl());
        // 関連先 Category から id だけを取り出して Form に入れる（Form は Entity を持たない）
        form.setCategoryId(blog.getCategory().getId());
        // 完成した Form を返す
        return form;
    }

    // 新規登録：書き込み系なので readOnly を上書きする
    @Transactional
    public Blog register(BlogRegisterForm form) {
        // 画面から来た categoryId を本物の Category Entity に解決する（無ければ例外）
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません: id=" + form.getCategoryId()));

        // 空の Blog Entity を生成する
        Blog blog = new Blog();
        // Form の値を Entity に詰め替える
        blog.setTitle(form.getTitle());
        // 本文も詰める
        blog.setContent(form.getContent());
        // アップロードされた画像があれば保存し、公開URL (/uploads/xxx.jpg) を Entity にセットする
        // ファイル未選択なら store() は null を返す → imageUrl も null（任意項目）
        blog.setImageUrl(fileStorageService.store(form.getImageFile()));
        // 解決済みの Category Entity をセットする（@ManyToOne の関連付け）
        blog.setCategory(category);
        // publishedAt は Entity の @PrePersist が null のときに自動で「今」を入れる

        // save() で INSERT 文が発行される（id は自動採番）
        return blogRepository.save(blog);
    }

    // 更新：書き込み系なので @Transactional を明示する
    @Transactional
    public Blog update(BlogEditForm form) {
        // 既存ブログを取得（無ければ例外）
        Blog blog = findById(form.getId());
        // 新しい categoryId を本物の Category Entity に解決する（無ければ例外）
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません: id=" + form.getCategoryId()));

        // 受け取った値で各フィールドを上書きする
        blog.setTitle(form.getTitle());
        // 本文も上書きする
        blog.setContent(form.getContent());
        // 画像の扱い：
        //   ・新しいファイルがアップロードされていれば保存して新URLで上書き
        //   ・選択されていなければ、画面から hidden で送られてきた既存URLをそのまま使う
        String newUploadedUrl = fileStorageService.store(form.getImageFile());
        if (newUploadedUrl != null) {
            // 新しい画像がアップロードされたので Entity の imageUrl を新URLで上書きする
            blog.setImageUrl(newUploadedUrl);
        } else {
            // 新しいファイルが無ければ、Form の hidden に入っていた既存URLをそのまま維持する
            blog.setImageUrl(form.getImageUrl());
        }
        // カテゴリーも上書きする
        blog.setCategory(category);

        // save() で UPDATE 文が発行され、Entity の @PreUpdate が updated_at を自動で更新する
        return blogRepository.save(blog);
    }

    // 削除：書き込み系なので @Transactional を明示する
    @Transactional
    public void delete(Long id) {
        // 対象を取得（無ければ例外）
        Blog blog = findById(id);
        // DELETE 文を発行する
        blogRepository.delete(blog);
    }
}
