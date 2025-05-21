# Shop Information SQLite 實作指南

本專案實作了 Android 中的 SQLite 資料庫功能，用於管理商店資訊 (ShopInfo)。透過 SQLite，應用程式可以永久保存商店資料，即使應用程式重新啟動也不會遺失。

## 資料庫架構

### 主要元件

1. **Room 資料庫**
   - `AppDatabase`: 整個應用程式的主要資料庫類別
   - `ShopInfoDao`: 資料存取對象，定義了對 ShopInfo 表的操作方法
   - `ShopInfoRepository`: 封裝資料存取邏輯，提供乾淨的 API 給 ViewModel 使用

2. **原生 SQLite 實作**
   - `ShopDatabaseHelper`: 直接使用 SQLiteOpenHelper 的實作，作為替代方案

## 關鍵功能

### 商店資訊 CRUD 操作

1. **建立 (Create)**
   - 透過 `ShopInfoViewModel.addNewShop()` 方法新增商店資訊
   - 資料會被保存到 SQLite 資料庫中

2. **讀取 (Read)**
   - 使用 `ShopInfoViewModel.shopInfoList` 流獲取所有商店資料
   - 資料會即時更新到 UI

3. **更新 (Update)**
   - 透過 `ShopInfoViewModel.updateShopInfo()` 更新現有商店資訊
   - 變更會立即反映在資料庫中

4. **刪除 (Delete)**
   - 使用 `ShopInfoViewModel.deleteShop()` 刪除指定商店
   - 刪除後會自動更新所有相依畫面

## 技術實現

### Room 資料庫

1. **Entity**
   - `@Entity` 註解定義 ShopInfo 類別為資料表
   - `@PrimaryKey` 指定 ID 為主鍵

2. **DAO**
   - 使用 `@Query`, `@Insert`, `@Update`, `@Delete` 等註解定義操作
   - 支援 Kotlin Flow 和協程

3. **Repository**
   - 提供單一資料來源，處理資料邏輯
   - 透過 Flow 實現響應式 UI 更新

### 資料庫初始化

- `DatabaseInitializer` 負責首次安裝時初始化資料庫
- 預設新增一個示範商店資料

## 使用說明

### 查看所有商店
1. 打開應用程式
2. 進入商店列表畫面
3. 所有商店資訊會顯示在列表中

### 新增商店
1. 點擊右上角的 "+" 按鈕
2. 填寫商店資訊表單
3. 點擊確認儲存

### 修改商店資訊
1. 在商店列表中點擊要編輯的商店
2. 修改商店資料
3. 點擊確認儲存變更

### 刪除商店
1. 在商店列表中長按要刪除的商店
2. 確認刪除操作
