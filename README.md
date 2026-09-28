# 学生向けフリマ・ECプラットフォーム基盤 (Campus Marketplace)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)]()
[![Vue.js](https://img.shields.io/badge/Vue.js-2%2F3-blue.svg)]()
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)]()

本プロジェクトは、学生間の不要品取引（フリマ）を円滑化することを目的とした、堅牢なWebアプリケーション基盤です。  
単なる機能実装にとどまらず、**「実務を意識した保守性の高いバックエンド設計」**および**「Docker・Nginxを活用したコンテナ仮想化と自動デプロイ基盤」**に重点を置いて構築しています。

---

## 📌 主な特徴と技術的なこだわり（アピールポイント）

### 1. インフラ・運用を意識したコンテナ仮想化（Docker & Nginx）
* **マルチステージビルド・コンテナ化**: バックエンド（Spring Boot）およびフロントエンド（Vue.js）の各層に `Dockerfile` を用意し、イメージの軽量化と依存関係の隔離を実現。
* **Nginxによるリバースプロキシ**: フロントエンド配信およびバックエンドAPIへのリバースプロキシルーティングを `nginx.conf` にて一括管理。
* **一元管理（Docker Compose）**: データベース（MySQL）、フロントエンド、バックエンドを `docker-compose.yml` 経由でワンコマンド起動・環境構築が可能。

### 2. 堅牢で拡張性の高いバックエンドアーキテクチャ
* **厳格なレイヤードアーキテクチャ**: `Controller` -> `Service` -> `Mapper` -> `Database` の責務分離を徹底。
* **グローバル例外ハンドリング**: `@RestControllerAdvice` による統一例外処理基盤（`exception/`）を構築し、予期せぬエラー時もクライアントへ標準化されたJSONレスポンスを返却。
* **共通レスポンス基盘**: `common/` パッケージにて汎用Resultオブジェクトやユーティリティを共通化し、API仕様の均一性を担保。

### 3. 多言語対応・管理画面を備えたフロントエンド
* **国際化（i18n）**: `i18n/` を導入し、グローバル利用を想定した動的多言語切り替えに対応。
* **権限分離（Front / Manager）**: 一般ユーザー向け取引画面（`front/`）と、管理者向けのシステム・商品・ユーザー管理画面（`manager/`）を独立設計。

---

## 🛠 技術スタック（Technology Stack）

| レイヤー | 技術 / ツール | 役割 |
| :--- | :--- | :--- |
| **Infrastructure / DevOps** | **Docker / Docker Compose** | コンテナ仮想化・マルチコンテナオーケストレーション |
| | **Nginx** | Webサーバ、リバースプロキシ、静的リソース配信 |
| **Backend** | **Java 17 / Spring Boot** | コアAPIサービス・ビジネスロジック基盤 |
| | **MyBatis / MySQL** | ORMデータマッピング・リレーショナルデータベース |
| | **Maven** | 依存関係管理・ビルド自動化 |
| **Frontend** | **Vue.js / Vue Router** | SPAフロントエンドUI・ルーティング管理 |
| | **vue-i18n** | 多言語ローカライゼーション基盤 |
| | **Axios** | 非同期HTTP通信 |

---

## 📂 ディレクトリ構成（Project Structure）

```text
code-scaffold/
├── .idea/                      # IDE設定ファイル
├── files/                      # アップロードファイル・静的アセット保管
├── sql/                        # データベース初期化DDL・DMLスクリプト
├── springboot/                 # バックエンドAPIサービス（Spring Boot）
│   ├── src/main/java/com/example/springboot/
│   │   ├── common/             # 共通レスポンス・定数
│   │   ├── controller/         # REST APIコントローラー層
│   │   ├── entity/             # データベースエンティティ
│   │   ├── exception/          # グローバル例外処理・エラーハンドリング
│   │   ├── mapper/             # MyBatisマッパーインターフェース
│   │   ├── service/            # 業務ロジック層（インターフェース / impl）
│   │   └── utils/              # 共通ユーティリティクラス
│   ├── src/main/resources/
│   │   ├── mapper/             # MyBatis XMLマッピングファイル
│   │   └── application.yml     # アプリケーション設定プロパティ
│   ├── Dockerfile              # バックエンドビルド用Docker定義
│   └── pom.xml                 # Maven構成ファイル
├── vue/                        # フロントエンドSPA（Vue.js）
│   ├── src/
│   │   ├── assets/             # 静的スタイル・画像リソース
│   │   ├── components/         # 共通UIコンポーネント
│   │   ├── i18n/               # 多言語対応設定リソース
│   │   ├── router/             # ルーティング設定
│   │   └── views/
│   │       ├── front/          # 一般ユーザー向けUI（商品閲覧・取引等）
│   │       └── manager/        # 管理者向け管理コンソール（Login, 404, CRUD）
│   ├── nginx.conf              # 本番配信用Nginxリバースプロキシ設定
│   └── Dockerfile              # フロントエンドビルド・配信Docker定義
└── docker-compose.yml          # 全サービス一括起動用オーケストレーション設定
