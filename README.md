# 岐大バスナビ

岐阜大学構内から岐阜駅方面へ向かうバスを探す Android アプリです。

現在地または選択した学内地点から、構内の乗車バス停までの徒歩時間、時刻表、運行日ルールを組み合わせて、利用しやすいバス候補を表示します。

## 主な機能

- 学内マップ上での現在地、出発地点候補、乗車バス停の表示
- GPS から最寄りの出発地点候補を選択
- 出発地点、目的地、余裕時間を指定したバス候補検索
- 目的地候補別の最短到着サマリー表示
- おすすめ候補と候補一覧の表示
- 学内ノード追加、よく使う出発地点、徒歩時間補正などの設定
- 雨予報または手動設定による雨天モード
- 時刻表 CSV と学年暦、祝日、学校休業日を使った運行判定

## 対応する主な行き先

アプリ内の時刻表から、JR 岐阜、名鉄岐阜などの目的地を選択できます。
乗車バス停候補は以下です。

- 岐阜大学病院
- 柳戸橋
- 岐阜大学

## 技術構成

- Android アプリケーション
- Java 11
- Gradle Kotlin DSL
- Android Gradle Plugin 9.2.1
- compileSdk 36.1
- minSdk 24
- AppCompat
- Material Components
- ConstraintLayout
- JUnit 4

## 必要な権限

`AndroidManifest.xml` で以下の権限を使用します。

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `INTERNET`

位置情報は、現在地の取得と最寄り出発地点の選択に使用します。
インターネット通信は、Open-Meteo API から雨予報を取得するために使用します。

## セットアップ

Android Studio でこのリポジトリを開き、Gradle 同期後に `app` モジュールを実行してください。

コマンドラインで確認する場合は、以下を実行します。

```sh
./gradlew test
```

Debug APK をビルドする場合は、以下を実行します。

```sh
./gradlew assembleDebug
```

## データ

時刻表データは以下に配置されています。

```text
app/src/main/assets/bus_schedule.csv
```

CSV は `BusScheduleCsvParser` で読み込まれ、便 ID、路線名、乗車バス停、目的地、運行日種別、運行ルールなどに変換されます。

学内の地点と経路は `LocalCampusGraphData`、乗車バス停は `LocalBusStopData`、祝日や学年暦は `LocalHolidayData`、`LocalAcademicCalendarData`、`LocalSchoolHolidayData` で管理されています。

## 画面

アプリは主に以下の画面で構成されています。

- `HOME`: 地図、出発地点、目的地、余裕時間、雨天モード、検索ボタン
- `RESULT`: 目的地候補別サマリー、おすすめ候補、候補一覧
- `SETTINGS`: よく使う出発地点、学内地点追加、徒歩時間補正
- `ADD_NODE`: ユーザー定義地点の追加
- `EDIT_TRAVEL_TIME`: 経路別の徒歩時間調整
- `TRAVEL_TIME_PROFILE`: 実測値から徒歩時間プロフィールを保存

## 検索ロジック

検索時は、選択中の出発地点から各乗車バス停までの最短経路を計算します。
その徒歩時間と現在時刻、余裕時間、時刻表、運行日判定を組み合わせて、乗車可能な便を抽出します。

乗車可能な便がない場合でも、各乗車バス停までの徒歩時間と到着予定時刻はサマリーに表示されます。

## テスト

主な回帰テストは以下にあります。

```text
app/src/test/java/com/example/prj_gifu_univ_bus_navi/JavaMigrationRegressionTest.java
app/src/test/java/com/example/prj_gifu_univ_bus_navi/ui/MapCoordinateTransformerTest.java
```

テストでは、時刻表 CSV の読み込み、運行日判定、推薦結果、学内経路、UI フィルタ、地図座標変換などを確認しています。

## ディレクトリ構成

```text
app/src/main/java/com/example/prj_gifu_univ_bus_navi/
├── data/   ローカルデータ、CSV パーサー、設定、天気取得
├── logic/  推薦、運行日判定、最短経路、経路構築
├── model/  バス、地点、経路、推薦結果などのモデル
├── ui/     ViewModel、地図ビュー、画面状態、座標変換
└── MainActivity.java
```

## 開発用グラフ編集ツール

学内ノードとエッジの追加・編集を支援するため、Android アプリ本体とは別に開発用の Web グラフ編集ツールを用意する方針です。

このツールでは、地図画像の上にノードとエッジを重ねて表示し、以下を行えるようにします。

- `LocalCampusGraphData` 読み込み時の現在状況サマリー表示
- 既存ノードとエッジの可視化
- GPS 値を使ったノードの追加、移動、名称変更、種別変更
- 2 ノード選択と徒歩時間入力によるエッジの追加、削除、徒歩時間の編集（秒単位対応）
- 孤立ノード、重複エッジ、片方向エッジの検出
- 編集内容の `LocalCampusGraphData.java` への保存
- Android 側のローカルデータへ反映しやすい形式でのエクスポート

ツール起動時は `LocalCampusGraphData.java` を読み込み、既存のノード数、エッジ数、孤立ノード数、片方向エッジ数、重複エッジ数、未定義ノードを参照しているエッジ数などを現在状況として表示します。新しいノードとエッジは、既存データに追記する形で扱います。

追加・編集操作は、右側の表と左側の地図を連動させる方式にします。右側のノード表に GPS 値、ノード ID、表示名、種別を追加すると、左側の地図上にそのノードが即時表示されます。

エッジ追加は、地図またはノード表から 2 つのノードを選択し、その間の徒歩時間を入力して追加する方式にします。徒歩時間は分だけでなく秒も扱えるようにします。エッジを追加すると、左側の地図上に 2 ノード間の線が即時表示され、エッジ表にも from、to、徒歩時間が追加されます。

Web 上でノードやエッジを追加・編集した後は、保存操作によって `LocalCampusGraphData.java` の内容を更新できるようにします。JSON を中間データとして挟まず、開発用ローカルサーバーが `LocalCampusGraphData.java` を読み書きします。

このプロジェクトでは、開発用ローカルサーバー方式を採用します。ブラウザ側はノード表、エッジ表、地図表示を担当し、保存時はローカルサーバーの保存 API 経由で Java ファイルを更新します。

徒歩時間を秒単位で扱うため、既存の分単位前提のモデル、エッジ定義、経路計算、表示ロジックは必要に応じて改修します。保存形式は既存の `LocalCampusGraphData` の構造に従い、既存データへ追記しやすい形を優先します。
