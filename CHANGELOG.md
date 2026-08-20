# Changelog

บันทึกการเปลี่ยนแปลงทั้งหมดของโปรเจกต์นี้

รูปแบบอ้างอิงจาก [Keep a Changelog](https://keepachangelog.com/)
และใช้ [Semantic Versioning](https://semver.org/)

> วิธีบันทึก: เพิ่มรายการใหม่ไว้ใต้หัวข้อ `[Unreleased]` เสมอ โดยจัดกลุ่มเป็น
> `Added` (เพิ่ม), `Changed` (แก้ไข/ปรับ), `Fixed` (แก้บั๊ก), `Removed` (ลบ),
> `Deprecated` (เตรียมเลิกใช้), `Security` (ความปลอดภัย)
> เมื่อจะออกเวอร์ชัน ให้ย้ายรายการจาก `[Unreleased]` ไปไว้ใต้หมายเลขเวอร์ชันพร้อมวันที่

## [Unreleased]

### Added
- `CHANGELOG.md` สำหรับบันทึกประวัติการเปลี่ยนแปลง (รูปแบบ Keep a Changelog)
- Steering rule `.kiro/steering/changelog.md` (`inclusion: always`) เพื่อให้
  การเปลี่ยนแปลงครั้งถัด ๆ ไปถูกบันทึกลง `CHANGELOG.md` โดยอัตโนมัติ
- Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`)
  เวอร์ชัน 8.7 เพื่อให้ build ผ่าน command line และ CI ได้ (ตั้ง execute bit
  ให้ `gradlew` แล้ว)
- `.gitattributes` คุม line endings: `gradlew` เป็น LF, `*.bat` เป็น CRLF,
  `*.jar`/รูปภาพ/keystore เป็น binary (แก้ปัญหา LF↔CRLF churn ของไฟล์ข้อความด้วย)

---

## ประวัติการพัฒนา (Development history)

รายการด้านล่างเรียงตามลำดับเวลาการทำงานจริงในโปรเจกต์ พร้อม commit hash อ้างอิง

### 1. สร้างโปรเจกต์เริ่มต้น — แอปสั่งอาหารด้วย MVVM (`f435324`)

**Added**
- โครงสร้างโปรเจกต์ Android (Kotlin) แบบ single-activity + Navigation Component
- ตั้งค่า Gradle ด้วย Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`)
  - `compileSdk 34`, `minSdk 24`, `targetSdk 34`, namespace `com.example.foodorder`
  - เปิดใช้ ViewBinding และ Navigation Safe Args
- **ชั้น data**
  - `MenuItem`, `CartItem` (models)
  - `FoodDataSource` — เมนูอาหารไทย 8 รายการแบบ in-memory
  - `FoodRepository` (interface) + `FoodRepositoryImpl`
- **DI** — `ServiceLocator` (manual DI แบบเบา)
- **ViewModel** — `OrderViewModel` + `OrderViewModelFactory`
  เปิด state แยกเป็นหลาย `LiveData` (`menu`, `cart`, `cartCount`, `cartTotal`)
- **UI (View เป็น XML)**
  - `MainActivity` (โฮสต์ NavHostFragment + Material Toolbar)
  - `MenuFragment` + `MenuAdapter`, `CartFragment` + `CartAdapter`
    (RecyclerView `ListAdapter` + `DiffUtil`)
  - Layouts, `nav_graph.xml`, ธีม Material 3, ไอคอน launcher (adaptive), ข้อความ UI ภาษาไทย
- `README.md` อธิบายสถาปัตยกรรมและวิธี build/run

**Fixed**
- แก้ import ผิด: `navGraphViewModels` อยู่ในแพ็กเกจ `androidx.navigation`
  (ไม่ใช่ `androidx.navigation.fragment`) ทำให้เกิด `Unresolved reference`

### 2. ตั้งค่า Git และ branch (`f435324`)

**Added**
- `.gitignore` สำหรับ Android (กัน `.gradle/`, `.idea/`, `build/`,
  `local.properties`, keystore ฯลฯ)
- สร้าง branch `feature/food-order-mvvm-app` แยกจาก `main`, commit และ push ขึ้น
  remote (`origin`) แล้วเปิด Pull Request เข้า `main`

### 3. Refactor: รวม UI state + one-shot event (`ec5a119`)

**Added**
- `OrderUiState` — single source of truth ก้อนเดียว โดย `cartCount`, `cartTotal`,
  `isCartEmpty` เป็น derived property (คำนวณจาก state จริง กัน sync ไม่ตรงกัน)
- `OrderEvent` (sealed interface) — event แบบครั้งเดียว เช่น `OrderPlaced`

**Changed**
- `OrderViewModel` เปิด `uiState` ก้อนเดียว และส่ง event ผ่าน `Channel`/`Flow`
- `placeOrder()` ไม่คืนค่าให้ View ตัดสินใจแล้ว แต่ยิง event ออกมา
- `MenuFragment`/`CartFragment` สังเกตการณ์ `uiState` เดียว, `CartFragment`
  รับ event ด้วย `repeatOnLifecycle(STARTED)` (ไม่ trigger ซ้ำตอนหมุนจอ)

### 4. ยกระดับเป็น production-grade: coroutines/StateFlow, SavedStateHandle, BigDecimal (`ef3c0bf`)

**Changed**
- **Data layer async** — `FoodRepository.getMenu()` เป็น `suspend fun`,
  `FoodRepositoryImpl` ใช้ `withContext(Dispatchers.Default)`
- **`OrderViewModel`** — เปิด `uiState` เป็น `StateFlow`, โหลดเมนูใน `viewModelScope`
- **รอด process death** — เก็บตะกร้า (`id → จำนวน`) ใน `SavedStateHandle`
  เป็น `LinkedHashMap` (คงลำดับ) และสร้าง ViewModel ผ่าน
  `viewModelFactory { initializer { createSavedStateHandle() } }`
- **เงินเป็น `BigDecimal`** ทั้งระบบ (`MenuItem.price`, `CartItem.lineTotal`,
  `OrderUiState.cartTotal`, `OrderEvent.OrderPlaced.total`) เลี่ยง float rounding
- `MenuFragment`/`CartFragment` เปลี่ยนไป collect `StateFlow` ด้วย `repeatOnLifecycle`
- อัปเดต `README.md` ให้ตรงกับสถาปัตยกรรมใหม่

**Added**
- Dependency `androidx.lifecycle:lifecycle-viewmodel-savedstate`

**Removed**
- `OrderViewModelFactory.kt` (แทนด้วย `OrderViewModel.Factory` แบบ DSL)
- Dependency `lifecycle-livedata-ktx` ที่ไม่ได้ใช้แล้ว

### 5. ยืนยันการทำงาน

- Build และ run บน Android Studio ผ่านเรียบร้อย

---

## สิ่งที่ยังค้าง / แผนถัดไป (Backlog)

- Commit Gradle wrapper (`gradlew`, `gradle-wrapper.jar`) เพื่อให้ build ผ่าน CLI/CI ได้
- Unit tests สำหรับ `OrderViewModel` (เพิ่ม/ลดจำนวน, ยอดรวม, event, ตะกร้าว่าง)
- GitHub Actions (CI) build + test อัตโนมัติทุก PR
- เพิ่ม loading/error state ใน `OrderUiState` เมื่อเปลี่ยนไปใช้ข้อมูลจริง
