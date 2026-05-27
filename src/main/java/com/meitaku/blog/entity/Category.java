// このクラスが属するパッケージを宣言する（entity パッケージ＝DBテーブルに対応するクラスを置く場所）
package com.meitaku.blog.entity;

// DBカラムとフィールドの対応を細かく指定するためのアノテーションをインポートする
import jakarta.persistence.Column;
// このクラスが「JPAエンティティ（テーブル1行を表すクラス）」であることを示すアノテーションをインポートする
import jakarta.persistence.Entity;
// 主キーを「どう採番するか」を指定するためのアノテーションをインポートする
import jakarta.persistence.GeneratedValue;
// 主キー採番戦略の種類（IDENTITY/SEQUENCEなど）を表すenumをインポートする
import jakarta.persistence.GenerationType;
// 主キー（PRIMARY KEY）であることを示すアノテーションをインポートする
import jakarta.persistence.Id;
// INSERT直前に自動で呼ばれるメソッドを指定するアノテーションをインポートする
import jakarta.persistence.PrePersist;
// UPDATE直前に自動で呼ばれるメソッドを指定するアノテーションをインポートする
import jakarta.persistence.PreUpdate;
// マッピング先のテーブル名を指定するためのアノテーションをインポートする
import jakarta.persistence.Table;

// 日付・時刻を扱う標準クラス LocalDateTime をインポートする（created_at / updated_at で使用）
import java.time.LocalDateTime;

// 全フィールドのgetterを自動生成するLombokアノテーションをインポートする
import lombok.Getter;
// 引数なしコンストラクタを自動生成するLombokアノテーションをインポートする（JPA仕様で必須）
import lombok.NoArgsConstructor;
// 全フィールドのsetterを自動生成するLombokアノテーションをインポートする
import lombok.Setter;

// このクラスがJPAエンティティであることをSpringに伝える（=DBテーブルと結びつくクラス）
@Entity
// マッピング先テーブル名を "categories" に指定する（クラス名Categoryと異なるので明示する）
@Table(name = "categories")
// 全フィールドのgetterをコンパイル時に自動生成する
@Getter
// 全フィールドのsetterをコンパイル時に自動生成する
@Setter
// JPA仕様で必要な「引数なしコンストラクタ」をコンパイル時に自動生成する
@NoArgsConstructor
// categoriesテーブルの1行を表すクラスを宣言する
public class Category {

    // このフィールドが主キー（PRIMARY KEY）であることをJPAに伝える
    @Id
    // 主キーをDB側で自動採番させる（PostgreSQLのBIGSERIALに対応）
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // DBカラム "id" に対応付ける
    @Column(name = "id")
    // カテゴリーID（自動採番のため、新規作成時は null のままでよい）
    private Long id;

    // DBカラム "name" に対応付け、NOT NULL・UNIQUE・最大100文字の制約を指定する
    @Column(name = "name", nullable = false, unique = true, length = 100)
    // カテゴリー名（例: Frontend / Backend / AI / Machine Learning / Design）
    private String name;

    // DBカラム "created_at" に対応付け、NOT NULL かつ「更新不可」にする（登録時に一度だけ書き込む）
    @Column(name = "created_at", nullable = false, updatable = false)
    // レコード作成日時
    private LocalDateTime createdAt;

    // DBカラム "updated_at" に対応付け、NOT NULL を指定する
    @Column(name = "updated_at", nullable = false)
    // レコード最終更新日時
    private LocalDateTime updatedAt;

    // INSERT直前にJPAから自動的に呼び出されるメソッドであることを宣言する
    @PrePersist
    // 新規保存（INSERT）の直前に created_at / updated_at をセットするメソッド
    protected void onCreate() {
        // 現在時刻を1回だけ取得して、両カラムで同じ値を使うようにする
        LocalDateTime now = LocalDateTime.now();
        // 作成日時に現在時刻をセットする
        this.createdAt = now;
        // 更新日時にも現在時刻をセットする（INSERT直後は作成日時と同じ値になる）
        this.updatedAt = now;
    }

    // UPDATE直前にJPAから自動的に呼び出されるメソッドであることを宣言する
    @PreUpdate
    // 更新（UPDATE）の直前に updated_at だけを現在時刻で上書きするメソッド
    protected void onUpdate() {
        // 更新日時を「いま」に書き換える（作成日時は触らない）
        this.updatedAt = LocalDateTime.now();
    }
}
