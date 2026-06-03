# 学内グラフ編集ツール

開発用のローカル Web ツールです。本番 Android アプリには組み込みません。

## 起動方法

```sh
cd tools/graph-editor
node server.js
```

起動後、ブラウザで `http://localhost:3000` を開きます。

## 仕様

- 正のデータは `app/src/main/java/com/example/prj_gifu_univ_bus_navi/data/LocalCampusGraphData.java` です。
- JSON ファイルや `campus_graph.json` は作らず、サーバーが Java ファイルを直接読み書きします。
- 保存時は `NODES` と `EDGES` の `Arrays.asList(...)` 内だけを更新します。
- 徒歩時間は `travelTimeSeconds` として秒単位で扱います。
- UI ではエッジの徒歩時間を「分」「秒」の別入力欄で編集します。
- 双方向が必要な場合は、片方向エッジを 2 本追加してください。

地図画像は `app/src/main/res/mipmap-hdpi/tatemono_no_number.png` を優先し、存在しない場合は既存の `app/src/main/res/drawable-nodpi/tatemono_no_number.png` を使います。

外部依存はありません。Node.js の標準モジュールだけで実装しています。
