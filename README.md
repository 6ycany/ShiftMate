# ShiftMate

飲食店・オフィス向けシフト自動生成 Android アプリ

## ビルド方法（GitHub Actions）

Android Studio **不要**。コードを GitHub にプッシュするだけで APK が自動生成されます。

### 手順

**① GitHub リポジトリを作成してプッシュ**

```bash
cd ShiftMate
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<your-username>/ShiftMate.git
git push -u origin main
```

**② GitHub Actions でビルド確認**

1. GitHubリポジトリの **Actions** タブを開く
2. 「Android Build」ワークフローが自動で実行される
3. 成功すると **Artifacts** に `debug-apk-<sha>` が表示される
4. ダウンロード → 解凍 → `app-debug.apk` をAndroid端末にインストール

### ワークフロー概要

`.github/workflows/build.yml` が以下を自動で行います：

| ステップ | 内容 |
|---------|------|
| JDK 17 セットアップ | GitHub Actions の ubuntu-latest に JDK を導入 |
| Gradle 8.9 セットアップ | `gradle/actions/setup-gradle` で直接インストール（gradlew 不要） |
| `gradle assembleDebug` | デバッグ APK をビルド |
| Artifact アップロード | APK を7日間保存 |

## 技術スタック

| カテゴリ | 採用技術 |
|---------|---------|
| 言語 | Kotlin 2.0 |
| UI | Jetpack Compose + Material3 |
| アーキテクチャ | MVVM + Clean Architecture |
| DI | Hilt |
| DB | Room |
| 非同期 | Kotlin Coroutines + Flow |
| ナビゲーション | Navigation Compose |
| minSdk | 26 (Android 8.0) |
| targetSdk | 35 |
