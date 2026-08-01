# Spring Boot Project

## 技術スタック

- Spring Boot 3.x
- Java 21+ (Records 活用)
- Gradle (Kotlin DSL)
- Spring Data JPA
- Checkstyle + Spotless (linter/formatter)

## ディレクトリ構成

```
src/main/java/com/example/app/
  controller/                   # REST Controller
    UserController.java
  service/                      # ビジネスロジック
    UserService.java
  repository/                   # データアクセス
    UserRepository.java
  model/
    entity/                     # JPA Entity
      User.java
    dto/                        # リクエスト/レスポンス DTO
      CreateUserRequest.java
      UserResponse.java
  config/                       # 設定クラス
    SecurityConfig.java
  exception/                    # 例外定義 + ハンドラー
    ResourceNotFoundException.java
    GlobalExceptionHandler.java

src/test/java/com/example/app/
  controller/                   # Controller テスト (@WebMvcTest)
  service/                      # Service テスト (単体テスト)
  repository/                   # Repository テスト (@DataJpaTest)
  integration/                  # 統合テスト (@SpringBootTest)
```

## 設計ルール

### レイヤーの責務と依存方向

```
Controller → Service → Repository → Entity
    ↓            ↓
   DTO          DTO
```

- **Controller**: HTTPリクエスト/レスポンスの変換、バリデーション、Service呼び出し。ビジネスロジックを書かない
- **Service**: ビジネスロジックの実装。トランザクション管理。Controller や Repository の関心を持たない
- **Repository**: データアクセスのみ。Spring Data JPA のインターフェースを基本とする
- **依存方向は上から下のみ**: Controller → Service → Repository。逆方向・同レイヤー間の依存は禁止

### SOLID原則の適用

- Controller = ルーティング、Service = ビジネスロジック、Repository = データアクセス。責務を跨がない（SRP）
- 新しいビジネスルールは新 Service や Strategy で追加。既存 Service への条件分岐追加を避ける（OCP）
- Service はインターフェースを定義し、実装を差し替え可能にする（LSP/DIP）
- Repository に複数の関心を詰め込まない（ISP）

```java
// Service のインターフェース分離例
public interface UserService {
    UserResponse findById(Long id);
    UserResponse create(CreateUserRequest request);
}

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponse findById(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        return UserResponse.from(userRepository.save(request.toEntity()));
    }
}
```

### DTO 設計

- Controller の入出力には必ず DTO を使う。Entity を直接返さない
- リクエスト DTO: バリデーションアノテーション付き。`toEntity()` メソッドで Entity 変換
- レスポンス DTO: `from(Entity)` static factory method で Entity から変換
- Java Record を積極的に使う

```java
// Request DTO
public record CreateUserRequest(
        @NotBlank String name,
        @Email String email
) {
    public User toEntity() {
        return User.builder().name(name).email(email).build();
    }
}

// Response DTO
public record UserResponse(Long id, String name, String email) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
```

### 命名規則

| 対象 | 規則 | 例 |
|---|---|---|
| Controller | `XxxController` | `UserController` |
| Service IF | `XxxService` | `UserService` |
| Service 実装 | `XxxServiceImpl` | `UserServiceImpl` |
| Repository | `XxxRepository` | `UserRepository` |
| Entity | 単数形名詞 | `User`, `Order` |
| Request DTO | `XxxRequest` | `CreateUserRequest` |
| Response DTO | `XxxResponse` | `UserResponse` |
| 例外 | `XxxException` | `ResourceNotFoundException` |
| 設定 | `XxxConfig` | `SecurityConfig` |
| テスト | `XxxTest` | `UserServiceTest` |

### エンドポイント設計

- RESTful な URL 設計: `/api/v1/users`, `/api/v1/users/{id}`
- Controller には `@RequestMapping("/api/v1/xxx")` でベースパスを設定
- レスポンスは `ResponseEntity` で返す

### 例外ハンドリング

- ビジネスエラーはカスタム例外をスローし、`@RestControllerAdvice` で一元処理する
- Controller で try-catch しない

### Linter / Formatter

- **Checkstyle**: `config/checkstyle/checkstyle.xml` で静的解析ルールを定義。Google Java Style をベースにカスタマイズ
- **Spotless**: Google Java Format (AOSP) でコードフォーマットを自動化。import 順序も `config/spotless/java.importorder` で統一
- **EditorConfig**: `.editorconfig` でエディタ間の基本設定（インデント4スペース等）を統一
- Gradle での適用方法: `apply(from = "gradle/lint.gradle.kts")` を `build.gradle.kts` に追加
- フォーマットは Spotless に任せ、Checkstyle はコード品質ルールに集中する
- メソッド長30行、パラメータ数5個を上限としている

### Do / Don't

- **Do**: Service にインターフェースを定義する
- **Do**: Entity と DTO を厳格に分離する
- **Do**: `@Transactional` は Service 層に付ける
- **Don't**: Controller にビジネスロジックを書かない
- **Don't**: Entity を Controller のレスポンスとして返さない
- **Don't**: Repository に複雑なロジックを書かない（クエリのみ）
- **Don't**: 同レイヤー内で依存しない（Service が別の Service を呼ぶのは許容、ただし循環依存は禁止）

## CI/CD

### CI パイプライン (.github/workflows/ci.yml)

PR・push 時に以下が並列実行され、すべて通過後に Build が走る:

1. **Lint & Format** — `./gradlew spotlessCheck`
2. **Unit Test** — `./gradlew test`
3. **Integration Test** — `./gradlew integrationTest`（PostgreSQL サービスコンテナ付き）
4. **Build** — `./gradlew build -x test -x integrationTest`（上記3ジョブ通過後）

### CD パイプライン (.github/workflows/cd.yml)

main ブランチで CI 成功後、Docker イメージをビルドし GHCR にプッシュする。

### 必須 Gradle 設定

CI/CD が期待する Gradle タスク:

- `spotlessCheck` — Spotless によるフォーマットチェック
- `test` — `src/test/` 配下のユニットテスト
- `integrationTest` — `src/test/.../integration/` 配下の統合テスト（カスタムタスク定義が必要）
- `bootJar` — 実行可能 JAR のビルド

```kotlin
// build.gradle.kts — integrationTest タスク例
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

tasks.register<Test>("integrationTest") {
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}
```

### Docker

- `Dockerfile`: multi-stage ビルド（builder → runtime）
- Eclipse Temurin 21 ベース
- `.dockerignore` で不要ファイルを除外済み
