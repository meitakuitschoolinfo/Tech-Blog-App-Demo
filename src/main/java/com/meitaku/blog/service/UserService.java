// このクラスが属するパッケージを宣言する（service パッケージ＝業務ロジック層）
package com.meitaku.blog.service;

// このサービスが扱う Entity（User）をインポートする
import com.meitaku.blog.entity.User;
// 「探したけど見つからなかった」場合に投げるカスタム例外をインポートする
import com.meitaku.blog.exception.ResourceNotFoundException;
// 画面の入力1件を表す Form クラス（登録・編集兼用）をインポートする
import com.meitaku.blog.form.user.UserRegisterForm;
// User の CRUD を担う Repository をインポートする
import com.meitaku.blog.repository.UserRepository;

// Spring Security に「認証時のユーザー情報の型」として返す UserDetails をインポートする
import org.springframework.security.core.userdetails.UserDetails;
// Spring Security の「ユーザー読み込み窓口」インターフェース
import org.springframework.security.core.userdetails.UserDetailsService;
// 「ユーザーが見つからなかった」ことを示す Security 用の標準例外
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// パスワードのハッシュ化／検証を担う Spring Security のインターフェース
import org.springframework.security.crypto.password.PasswordEncoder;
// このクラスを Spring の Service Bean として認識させるためのアノテーションをインポートする
import org.springframework.stereotype.Service;
// メソッド／クラス単位でトランザクションを張るためのアノテーションをインポートする
import org.springframework.transaction.annotation.Transactional;

// final フィールドを引数に取るコンストラクタを Lombok に自動生成させる（コンストラクタインジェクション用）
import lombok.RequiredArgsConstructor;

// 戻り値として複数件を返すための List をインポートする
import java.util.List;

// このクラスを Spring の DI コンテナに登録し、Service 層として扱わせる
@Service
// クラスレベルで「読み取り専用トランザクション」を既定値にする
@Transactional(readOnly = true)
// 依存先（final フィールド）を引数に取るコンストラクタを Lombok に生成させる
@RequiredArgsConstructor
// 管理者ユーザーに関する業務ロジックを担うサービスクラス
// 加えて Spring Security の UserDetailsService を実装し、ログイン時の認証情報供給も担う
public class UserService implements UserDetailsService {

    // User の CRUD を行うリポジトリ（コンストラクタインジェクションで注入される）
    private final UserRepository userRepository;
    // パスワードのハッシュ化に使う PasswordEncoder（SecurityConfig で @Bean 定義済み）
    private final PasswordEncoder passwordEncoder;

    // Spring Security がログイン時に「email から管理者情報を読みに来る」窓口
    // 戻り値を Spring 標準の UserDetails に詰め替えて返す
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // findByEmail は Optional<User> を返す（PDF 3-7）→ 無ければ Security 用の例外を投げる
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("管理者が見つかりません: " + email));
        // Spring Security 側の User クラス（我々の Entity と同名のため必ずフルパスで参照する）
        return org.springframework.security.core.userdetails.User.builder()
                // ログインIDとして email を使う
                .username(user.getEmail())
                // DBに保存されている「BCryptハッシュ済み」のパスワードをそのまま渡す（生に戻さない）
                .password(user.getPassword())
                // この管理者に付与する権限（ロール）。今は全員 ADMIN として扱う
                .authorities("ROLE_ADMIN")
                // 上記設定で UserDetails を組み立てて返す
                .build();
    }

    // 全管理者を取得する（管理画面の一覧表示で使う）
    public List<User> findAll() {
        // JpaRepository が提供する findAll() をそのまま返す
        return userRepository.findAll();
    }

    // ID 指定で1件取得する。見つからなければ ResourceNotFoundException を投げる
    public User findById(Long id) {
        // Optional<User> を orElseThrow で開けて、無ければ例外
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません: id=" + id));
    }

    // メールアドレスから管理者を取得する（プロフィール画面や Security 連携の補助で使う）
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません: " + email));
    }

    // 編集画面の初期表示用に「DB の User」を「画面の UserRegisterForm（兼用）」に詰め替える
    // ※ 編集時にパスワード欄を空にしたままにしないため、画面側で再入力を求める運用とする
    public UserRegisterForm toEditForm(Long id) {
        // 対象ユーザーを取得（無ければ例外）
        User user = findById(id);
        // 空の Form を生成する
        UserRegisterForm form = new UserRegisterForm();
        // 名前を埋める
        form.setName(user.getName());
        // メールアドレスを埋める
        form.setEmail(user.getEmail());
        // パスワードはハッシュなので Form には埋めない（画面で再入力させる）
        return form;
    }

    // 新規登録：書き込み系なので @Transactional を明示する
    @Transactional
    public User register(UserRegisterForm form) {
        // 同一 email が既に登録されていれば登録させない（Service レベルの重複チェック）
        if (userRepository.existsByEmail(form.getEmail())) {
            // 業務ルール違反は IllegalArgumentException で表現する
            throw new IllegalArgumentException("このメールアドレスは既に登録されています: " + form.getEmail());
        }
        // 空の User Entity を生成する
        User user = new User();
        // Form の値を Entity に詰め替える
        user.setName(form.getName());
        // メールアドレスをセットする
        user.setEmail(form.getEmail());
        // ★最重要★ 生パスワードを BCrypt でハッシュ化してからセットする（生のまま保存しない）
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        // INSERT 文が発行される（id は自動採番）
        return userRepository.save(user);
    }

    // 更新：書き込み系なので @Transactional を明示する
    // 編集対象の id は URL パスから受け取る想定（Form は UserRegisterForm を流用）
    @Transactional
    public User update(Long id, UserRegisterForm form) {
        // 既存ユーザーを取得（無ければ例外）
        User user = findById(id);
        // メールアドレスを変更する場合のみ、新しいアドレスの重複をチェックする
        if (!user.getEmail().equals(form.getEmail())
                && userRepository.existsByEmail(form.getEmail())) {
            // 業務ルール違反は IllegalArgumentException
            throw new IllegalArgumentException("このメールアドレスは既に登録されています: " + form.getEmail());
        }
        // 名前を上書きする
        user.setName(form.getName());
        // メールアドレスを上書きする
        user.setEmail(form.getEmail());
        // パスワードを再ハッシュしてセットする（毎回ハッシュ → 再ログインしても矛盾しない）
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        // UPDATE 文が発行され、@PreUpdate で updated_at が自動更新される
        return userRepository.save(user);
    }

    // 削除：書き込み系なので @Transactional を明示する
    @Transactional
    public void delete(Long id) {
        // 対象を取得（無ければ例外）
        User user = findById(id);
        // DELETE 文を発行する
        userRepository.delete(user);
    }
}
