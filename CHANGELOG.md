# Changelog

บันทึกการเปลี่ยนแปลงทั้งหมดของโปรเจกต์นี้ เรียงตามลำดับเวลาแบบต่อเนื่อง

> วิธีบันทึก: เพิ่มรายการใหม่ **ต่อท้ายไฟล์เสมอ (ล่าสุดอยู่ล่างสุด)** อย่าแทรกกลางไฟล์
> แต่ละรายการใช้หัวข้อ `## N. <ชื่อสั้น ๆ>` (ต่อเลขจากรายการก่อนหน้า) ระบุ commit hash
> ได้ถ้ามี และจัดกลุ่มรายละเอียดด้วย `Added` / `Changed` / `Fixed` / `Removed` /
> `Deprecated` / `Security` เขียนสั้น กระชับ เน้น "อะไรเปลี่ยนและทำไม"

---

## 1. สร้างโปรเจกต์เริ่มต้น — แอปสั่งอาหารด้วย MVVM (`f435324`)

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

## 2. ตั้งค่า Git และ branch (`f435324`)

**Added**
- `.gitignore` สำหรับ Android (กัน `.gradle/`, `.idea/`, `build/`,
  `local.properties`, keystore ฯลฯ)
- สร้าง branch `feature/food-order-mvvm-app` แยกจาก `main`, commit และ push ขึ้น
  remote (`origin`) แล้วเปิด Pull Request เข้า `main`

## 3. Refactor: รวม UI state + one-shot event (`ec5a119`)

**Added**
- `OrderUiState` — single source of truth ก้อนเดียว โดย `cartCount`, `cartTotal`,
  `isCartEmpty` เป็น derived property (คำนวณจาก state จริง กัน sync ไม่ตรงกัน)
- `OrderEvent` (sealed interface) — event แบบครั้งเดียว เช่น `OrderPlaced`

**Changed**
- `OrderViewModel` เปิด `uiState` ก้อนเดียว และส่ง event ผ่าน `Channel`/`Flow`
- `placeOrder()` ไม่คืนค่าให้ View ตัดสินใจแล้ว แต่ยิง event ออกมา
- `MenuFragment`/`CartFragment` สังเกตการณ์ `uiState` เดียว, `CartFragment`
  รับ event ด้วย `repeatOnLifecycle(STARTED)` (ไม่ trigger ซ้ำตอนหมุนจอ)

## 4. ยกระดับ production-grade: coroutines/StateFlow, SavedStateHandle, BigDecimal (`ef3c0bf`)

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

_(ยืนยัน: build และ run บน Android Studio ผ่านเรียบร้อยหลังรอบนี้)_

## 5. เพิ่มระบบบันทึก Changelog (`0a858bd`)

**Added**
- `CHANGELOG.md` สำหรับบันทึกประวัติการเปลี่ยนแปลง
- Steering rule `.kiro/steering/changelog.md` (`inclusion: always`) เพื่อให้
  การเปลี่ยนแปลงครั้งถัด ๆ ไปถูกบันทึกลง `CHANGELOG.md` โดยอัตโนมัติ

## 6. Commit Gradle wrapper + .gitattributes (`6991a0f`)

**Added**
- Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`)
  เวอร์ชัน 8.7 เพื่อให้ build ผ่าน command line และ CI ได้ (ตั้ง execute bit
  ให้ `gradlew`) — ดึงจาก repo ทางการ `gradle/gradle` tag `v8.7.0` และตรวจสอบแล้ว
  ว่าเป็น jar แท้ (มีคลาส `GradleWrapperMain`)
- `.gitattributes` คุม line endings: `gradlew` เป็น LF, `*.bat` เป็น CRLF,
  `*.jar`/รูปภาพ/keystore เป็น binary (แก้ปัญหา LF↔CRLF churn ของไฟล์ข้อความด้วย)

## 7. ปรับรูปแบบ Changelog เป็นแบบ append ต่อท้าย

**Changed**
- เปลี่ยนโครงสร้าง `CHANGELOG.md` จากแบบ Keep a Changelog (`[Unreleased]` ด้านบน)
  เป็นบันทึกเรียงตามเวลาแบบต่อเนื่อง เพิ่มรายการใหม่ต่อท้ายไฟล์เสมอ (ล่าสุดอยู่ล่างสุด)
  เพื่ออ่านไล่จากบนลงล่างได้ง่ายและไม่ต้องแทรกกลางไฟล์
- ปรับ steering rule `.kiro/steering/changelog.md` ให้สอดคล้องกับรูปแบบใหม่

## สิ่งที่ยังค้าง / แผนถัดไป (Backlog)

- Unit tests สำหรับ `OrderViewModel` (เพิ่ม/ลดจำนวน, ยอดรวม, event, ตะกร้าว่าง)
- GitHub Actions (CI) build + test + validate Gradle wrapper อัตโนมัติทุก PR
- เพิ่ม loading/error state ใน `OrderUiState` เมื่อเปลี่ยนไปใช้ข้อมูลจริง
