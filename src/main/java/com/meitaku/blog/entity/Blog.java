// このクラスが属するパッケージを宣言する（entity パッケージ＝DBテーブルに対応するクラスを置く場所）
package com.meitaku.blog.entity;

// DBカラムとフィールドの対応を細かく指定するためのアノテーションをインポートする
import jakarta.persistence.Column;
// このクラスが「JPAエンティティ」であることを示すアノテーションをインポートする
import jakarta.persistence.Entity;
// 関連先（Category）を「いつ取りに行くか（即時/遅延）」を指定する enum をインポートする
import jakarta.persistence.FetchType;
// 主キーを「どう採番するか」を指定するためのアノテーションをインポートする
import jakarta.persistence.GeneratedValue;
// 主キー採番戦略の種類（IDENTITY/SEQUENCEなど）を表す enum をインポートする
import jakarta.persistence.GenerationType;
// 主キー（PRIMARY KEY）であることを示すアノテーションをインポートする
import jakarta.persistence.Id;
// 外部キー（FOREIGN KEY）のカラム名を指定するためのアノテーションをインポートする
import jakarta.persistence.JoinColumn;
// 「多対1」の関連を表すアノテーションをインポートする（Blog 多 ─ 1 Category）
import jakarta.persistence.ManyToOne;
// INSERT直前に自動で呼ばれるメソッドを指定するアノテーションをインポートする
import jakarta.persistence.PrePersist;
// UPDATE直前に自動で呼ばれるメソッドを指定するアノテーションをインポートする
import jakarta.persistence.PreUpdate;
// マッピング先のテーブル名を指定するためのアノテーションをインポートする
import jakarta.persistence.Table;

// 日付・時刻を扱う標準クラス LocalDateTime をインポートする
import java.time.LocalDateTime;

// 全フィールドの getter を自動生成する Lombok アノテーションをインポートする
import lombok.Getter;
// 引数なしコンストラクタを自動生成する Lombok アノテーションをインポートする（JPA仕様で必須）
import lombok.NoArgsConstructor;
// 全フィールドの setter を自動生成する Lombok アノテーションをインポートする
import lombok.Setter;

// このクラスが JPA エンティティであることを Spring に伝える
@Entity
// マッピング先テーブル名を "blogs" に指定する（クラス名と異なるので明示する）
@Table(name = "blogs")
// 全フィールドの getter を自動生成する
@Getter
// 全フィールドの setter を自動生成する
@Setter
// JPA仕様で必要な「引数なしコンストラクタ」を自動生成する
@NoArgsConstructor
// blogs テーブルの1行を表すクラスを宣言する
public class Blog {

    // このフィールドが主キーであることを JPA に伝える
    @Id
    // 主キーをDB側で自動採番させる（PostgreSQL の BIGSERIAL に対応）
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // DBカラム "id" に対応付ける
    @Column(name = "id")
    // ブログID（自動採番のため、新規作成時は null のままでよい）
    private Long id;

    // DBカラム "title" に対応付け、NOT NULL かつ最大200文字を指定する
    @Column(name = "title", nullable = false, length = 200)
    // ブログ記事のタイトル
    private String title;

    // DBカラム "content" に対応付け、NOT NULL かつ PostgreSQL の TEXT 型として作成させる
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    // ブログ記事の本文（長文を入れるため TEXT 型を使う）
    private String content;

    // DBカラム "image_url" に対応付け、NULL 許可かつ最大500文字を指定する
    @Column(name = "image_url", length = 500)
    // サムネイル画像のURL（任意項目なので NOT NULL にしない）
    private String imageUrl;

    // 「Blog 多 ─ 1 Category」の関連を宣言し、必要になったときだけ Category を取得する（LAZY）
    @ManyToOne(fetch = FetchType.LAZY)
    // 外部キーのカラム名を "category_id" に指定し、NOT NULL を強制する
    @JoinColumn(name = "category_id", nullable = false)
    // この記事が属するカテゴリー（categories テーブルへの参照）
    private Category category;

    // DBカラム "published_at" に対応付け、NOT NULL を指定する
    @Column(name = "published_at", nullable = false)
    // 画面に表示する「公開日時」（ユーザーが手動で変えられる）
    private LocalDateTime publishedAt;

    // DBカラム "created_at" に対応付け、NOT NULL かつ「更新不可」にする（登録時に一度だけ書き込む）
    @Column(name = "created_at", nullable = false, updatable = false)
    // レコード作成日時（システムが自動で入れる）
    private LocalDateTime createdAt;

    // DBカラム "updated_at" に対応付け、NOT NULL を指定する
    @Column(name = "updated_at", nullable = false)
    // レコード最終更新日時（システムが自動で入れる）
    private LocalDateTime updatedAt;

    // INSERT 直前に JPA から自動的に呼び出されるメソッドであることを宣言する
    @PrePersist
    // 新規保存（INSERT）の直前に日時系カラムを自動でセットするメソッド
    protected void onCreate() {
        // 現在時刻を1回だけ取得して、複数フィールドで同じ値を使うようにする
        LocalDateTime now = LocalDateTime.now();
        // 公開日時が画面側から指定されていなければ「いま」を入れる（指定されていれば尊重する）
        if (this.publishedAt == null) {
            // 公開日時に現在時刻をセットする
            this.publishedAt = now;
        }
        // 作成日時に現在時刻をセットする
        this.createdAt = now;
        // 更新日時にも現在時刻をセットする（INSERT 直後は作成日時と同じ値になる）
        this.updatedAt = now;
    }

    // UPDATE 直前に JPA から自動的に呼び出されるメソッドであることを宣言する
    @PreUpdate
    // 更新（UPDATE）の直前に updated_at だけを現在時刻で上書きするメソッド
    protected void onUpdate() {
        // 更新日時を「いま」に書き換える（作成日時・公開日時は触らない）
        this.updatedAt = LocalDateTime.now();
    }
}
