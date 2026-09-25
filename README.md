# ECプラットフォーム（Spring Boot × Vue.js フルスタックシステム）

Spring Boot と Vue.js を用いた、前後端分離（SPA構成）アーキテクチャのECプラットフォームです。
バックエンドAPI設計からフロントエンドのUI実装、データベースモデリング、さらにインフラ展開（Nginx / Linux環境構築）を見据えた開発を行っています。

---

## 1. システム概要
* **開発目的**: フルスタック開発の流れを把握した上で、インフラエンジニアとして実務に直結する「Web/DBサーバーの構築・運用」「コンテナ化」「パフォーマンス最適化」の基盤となる実践的アプリケーションを構築。
* **主要機能**:
  * ユーザー認証（管理者・一般ユーザー権限分離、JWT/セッション制御）
  * 商品一覧・カテゴリ別絞り込み・リアルタイム検索
  * ショッピングカート・注文処理フロー
  * 管理者ダッシュボード（EChartsによる売上・統計情報の可視化、ユーザー/商品/お知らせ管理）

---

## 2. 技術スタック（Technology Stack）

### フロントエンド（Front-End）
* **Framework**: Vue.js (Vue CLI / SPA)
* **UI ライブラリ**: Element UI
* **通信・ルーティング**: Axios, Vue Router
* **可視化**: ECharts

### バックエンド（Back-End）
* **Framework**: Spring Boot
* **ORM / データアクセス**: MyBatis / Spring Data
* **ビルドツール**: Maven
* **言語 / ランタイム**: Java (JDK 17+)

### データベース & インフラ（Database & Infrastructure）
* **RDBMS**: MySQL
* **Web/リバースプロキシ**: Nginx（予定 / 検証中）
* **プラットフォーム**: Linux (Ubuntu / AlmaLinux)
* **構成管理・CI/CD**: Git / GitHub

---

## 3. ディレクトリ構成
```text
code-scaffold/
├── springboot/          # バックエンドAPIサービス（RESTful API）
├── vue/                 # フロントエンドSPAアプリケーション
├── sql/                 # データベーススキーマおよび初期投入データ
├── files/               # 静的アップロードファイルストレージ
└── .gitignore           # Git管理除外設定
