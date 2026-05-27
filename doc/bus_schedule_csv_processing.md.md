# 一覧.csv 加工仕様

## 目的

`一覧.csv` を、Androidアプリ「岐大バスナビ」で利用しやすい時刻表データに整形する。

## 前提

- 途中停留所列は削除しない
- PDFから確認できる途中停留所の到着時刻は可能な範囲で埋める
- PDFで `∥` と表記されている停留所は通過扱いのため空欄にする
- PDFで `…` と表記されている停留所は未運行・非該当扱いのため空欄にする
- `柳戸橋` はPDFに独立した行がないため、既存CSVの値を維持する
- アプリで扱いやすいように、末尾に正規化用の列を追加する

## 使用するPDF

### 岐阜大学・病院線

対象ファイル:

- `2510_gidai.pdf`

使用する情報:

- 岐阜大学・病院線 平日ダイヤ
- 岐阜大学・病院線 土曜日・日祝日ダイヤ
- C系統の途中停留所時刻
- 連接バス
- 学休日運休
- 学休日運転
- 毎年4月から8月のみ運行、学休日運休

PDF上の主な停留所順:

- 岐阜大学病院
- 岐阜大学
- 正木マーサ前
- 北高前
- 忠節
- 西野町
- 千手堂
- 徹明町
- 名鉄岐阜
- JR岐阜

### 岐南町線

対象ファイル:

- `2604_ginancho.pdf`

使用する情報:

- 岐南町線 平日ダイヤ
- 岐南町線 土曜日・日祝日ダイヤ
- E16系統
- N系統
- N37系統
- 正木北
- さぎ山東
- 岐阜メモリアルセンター北
- 長良橋南・川原町
- 市役所・鶯谷高校口
- 徹明町
- 名鉄岐阜
- JR岐阜
- 加納附属小学校前
- 加納駅前
- 領下
- 岐南町三宅

PDF上の主な停留所順:

- 岐阜大学病院
- 岐阜大学
- 正木北
- さぎ山東
- 岐阜メモリアルセンター北
- 長良橋南・川原町
- 市役所・鶯谷高校口
- 徹明町
- 名鉄岐阜
- JR岐阜
- 加納附属小学校前
- 加納駅前
- 領下
- 岐南町三宅

## CSVで維持する既存列

以下の既存列は削除しない。

- busNo.
- 岐阜大学病院
- 柳戸橋
- 岐阜大学
- 種類
- 路線名
- option
- 岐大口
- 繰舟橋
- 正木北
- 正木マーサ前
- 則武
- 北高前
- 忠節
- 忠節橋
- 西野町
- 本郷町
- 柳ヶ瀬西口
- 千手堂
- 徹明町
- 金宝町
- 名鉄岐阜
- JR岐阜
- 正木東
- さぎ山小学校北
- 青山中学校南
- さぎ山東
- 自衛隊岐阜地本前
- 岐阜メモリアルセンター北
- 長良川国際会議場北口
- 長良橋北・鵜飼屋
- 長良橋南・川原町
- 岐阜公園・岐阜城
- 大仏南・妙照寺前
- 本町１丁目（岐阜市）
- 本町３丁目（岐阜市）
- 伊奈波通り
- 市役所・鶯谷高校口
- 今沢町
- 商工会議所前
- 柳ヶ瀬
- 加納桜道
- 加納附属小学校前
- 加納駅前
- 加納八幡町
- 茶所
- 上川手
- 領下
- 細畑
- いりのと公園前
- 岐阜女子高前
- 岐南町三宅

## 追加する列

CSV末尾に以下の列を追加する。

- id
- baseDayType
- operationRule
- operatingStartMonth
- operatingEndMonth
- mayBeArticulatedBus

## 追加列の意味

| 列名                | 内容                         |
| ------------------- | ---------------------------- |
| id                  | アプリ内部で使う一意ID       |
| baseDayType         | 平日ダイヤまたは土日祝ダイヤ |
| operationRule       | 追加運行条件                 |
| operatingStartMonth | 期間限定運行の開始月         |
| operatingEndMonth   | 期間限定運行の終了月         |
| mayBeArticulatedBus | 連接バスの可能性             |

## baseDayType の変換ルール

| CSVの種類      | baseDayType     |
| -------------- | --------------- |
| 平日           | WEEKDAY         |
| 土曜日・日祝日 | WEEKEND_HOLIDAY |

## operationRule の変換ルール

| optionの内容                                  | operationRule                              |
| --------------------------------------------- | ------------------------------------------ |
| 空欄                                          | NONE                                       |
| 学休日運休                                    | SCHOOL_HOLIDAY_EXCLUDED                    |
| 学休日運転                                    | SCHOOL_HOLIDAY_ONLY                        |
| 毎年4月～8月のみ運行                          | LIMITED_PERIOD                             |
| 毎年4月～8月のみ運行。学休日は運休。          | LIMITED_PERIOD_AND_SCHOOL_HOLIDAY_EXCLUDED |
| 連接バス                                      | NONE                                       |
| 連接バス/学休日運休                           | SCHOOL_HOLIDAY_EXCLUDED                    |
| 連接バス/学休日運転                           | SCHOOL_HOLIDAY_ONLY                        |
| 連接バス/毎年4月～8月のみ運行。学休日は運休。 | LIMITED_PERIOD_AND_SCHOOL_HOLIDAY_EXCLUDED |

## operatingStartMonth / operatingEndMonth の変換ルール

期間限定運行でない場合:

| operatingStartMonth | operatingEndMonth |
| ------------------- | ----------------- |
| 空欄                | 空欄              |

毎年4月から8月のみ運行の場合:

| operatingStartMonth | operatingEndMonth |
| ------------------- | ----------------- |
| 4                   | 8                 |

## mayBeArticulatedBus の変換ルール

| optionの内容       | mayBeArticulatedBus |
| ------------------ | ------------------- |
| 連接バスを含む     | true                |
| 連接バスを含まない | false               |

## id の命名規則

idは以下の形式にする。

- 平日C系統: `weekday_c_001`
- 平日E16系統: `weekday_e16_001`
- 平日N系統: `weekday_n_001`
- 土日祝C系統: `weekend_holiday_c_001`
- 土日祝E16系統: `weekend_holiday_e16_001`
- 土日祝N系統: `weekend_holiday_n_001`

同じ系統内では、時刻表上の順番に3桁連番を振る。

## C系統で埋める列

C系統については、PDFから確認できる範囲で以下を埋める。

- 岐阜大学病院
- 岐阜大学
- 正木マーサ前
- 北高前
- 忠節
- 西野町
- 千手堂
- 徹明町
- 名鉄岐阜
- JR岐阜

PDFで通過扱いになっている停留所は空欄にする。

## E16 / N 系統で埋める列

E16またはN系統については、PDFから確認できる範囲で以下を埋める。

- 岐阜大学病院
- 岐阜大学
- 正木北
- さぎ山東
- 岐阜メモリアルセンター北
- 長良橋南・川原町
- 市役所・鶯谷高校口
- 徹明町
- 名鉄岐阜
- JR岐阜
- 加納附属小学校前
- 加納駅前
- 領下
- 岐南町三宅

PDFで通過または未運行になっている停留所は空欄にする。

## 柳戸橋列の扱い

`柳戸橋` は、今回参照したPDFに独立した停留所行として掲載されていない。

そのため、以下の方針にする。

- 既存CSVに値がある場合は維持する
- PDFから推測して新規補完しない
- 今後、柳戸橋を含む公式時刻表データが取得できた場合に差し替える

## 既知の修正対象

既存CSVには以下のような不自然な時刻があるため、PDFに基づいて修正する。

### 土日祝C系統 27番

修正前:

- JR岐阜: 16:00

修正後:

- JR岐阜: 17:00

### 土日祝C系統 30番

修正前:

- JR岐阜: 17:00

修正後:

- JR岐阜: 18:00

## CSV整形時の注意

- 途中停留所列は削除しない
- 列順は基本的に現状維持する
- 追加列は末尾に置く
- 1行は1便を表す
- 空欄は空欄のままにする
- `∥` や `…` はCSVに入れず、空欄にする
- アプリ側で `LocalTime?` として扱えるように、停車しない停留所は空欄にする
- 時刻は `H:mm` または `HH:mm` 形式で統一する
- 24時を超える表記は今回扱わない

## アプリ側への変換対応

CSVの各行は、アプリ側では `BusTrip` に変換する。

対応関係は以下。

| CSV列               | BusTrip側               |
| ------------------- | ----------------------- |
| id                  | id                      |
| 路線名              | routeName               |
| 岐阜大学病院        | hospitalDepartureTime   |
| 柳戸橋              | yanagidoDepartureTime   |
| 岐阜大学            | universityDepartureTime |
| JR岐阜              | jrGifuArrivalTime       |
| 名鉄岐阜            | meitetsuGifuArrivalTime |
| baseDayType         | baseDayType             |
| operationRule       | operationRule           |
| operatingStartMonth | operatingStartMonth     |
| operatingEndMonth   | operatingEndMonth       |
| mayBeArticulatedBus | mayBeArticulatedBus     |

## Kotlin側での注意

CSVをKotlinデータへ変換する場合、以下を守る。

- 空欄の時刻は `null` にする
- `baseDayType` は `BaseDayType` enumへ変換する
- `operationRule` は `OperationRule` enumへ変換する
- `mayBeArticulatedBus` は Booleanへ変換する
- `operatingStartMonth` と `operatingEndMonth` は空欄なら null にする

## 検証項目

CSV加工後、以下を確認する。

- ヘッダー列数と全行の列数が一致している
- `id` が全行で一意である
- `baseDayType` が空欄になっていない
- `operationRule` が空欄になっていない
- `mayBeArticulatedBus` が空欄になっていない
- 平日C系統が存在する
- 土日祝C系統が存在する
- 平日E16系統が存在する
- 土日祝E16系統が存在する
- JR岐阜時刻がある便は `jrGifuArrivalTime` として使える
- 名鉄岐阜時刻が空欄の便でも、JR岐阜時刻があれば候補にできる
- 連接バスが `mayBeArticulatedBus=true` になっている
- 学休日運休が `SCHOOL_HOLIDAY_EXCLUDED` になっている
- 学休日運転が `SCHOOL_HOLIDAY_ONLY` になっている
- 4月から8月限定運行がある場合、`operatingStartMonth=4`、`operatingEndMonth=8` になっている

## Codexへの作業依頼文

以下の内容でCodexへ依頼する。

このリポジトリの `一覧.csv` を加工してください。

目的は、PDF時刻表から確認できる途中停留所の到着時刻を埋め、Androidアプリ「岐大バスナビ」で使いやすい形式に整形することです。

対象ファイル:

- `一覧.csv`

参照PDF:

- `2510_gidai.pdf`
- `2604_ginancho.pdf`

作業内容:

1. 途中停留所列は削除しない
2. 既存の列順は維持する
3. PDFから確認できる途中停留所の時刻を埋める
4. PDFで `∥` または `…` の停留所は空欄にする
5. 柳戸橋列はPDFに独立行がないため、既存値を維持する
6. 末尾に以下の列を追加する
   - id
   - baseDayType
   - operationRule
   - operatingStartMonth
   - operatingEndMonth
   - mayBeArticulatedBus
7. `種類` から `baseDayType` を生成する
8. `option` から `operationRule` と `mayBeArticulatedBus` を生成する
9. 期間限定運行の場合は `operatingStartMonth=4`、`operatingEndMonth=8` を設定する
10. idは系統・曜日区分ごとに一意な文字列にする
11. 加工後、全行の列数がヘッダー列数と一致しているか検証する
12. `id` が一意であることを検証する

CSV以外のファイルは変更しないでください。
