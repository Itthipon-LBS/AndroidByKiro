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

## 8. เพิ่ม JVM unit tests

**Added**
- Test dependencies: `kotlinx-coroutines-test`, `Turbine` (ในตัว version catalog)
- `MainDispatcherRule` — JUnit rule สลับ `Dispatchers.Main` เป็น test dispatcher
- `FakeFoodRepository` — test double ของ `FoodRepository`
- `OrderViewModelTest` — ครอบคลุมโหลดเมนู, เพิ่ม/ลดจำนวน, ยอดรวม `BigDecimal`,
  ยิง event `OrderPlaced` + เคลียร์ตะกร้า, ตะกร้าว่างไม่ยิง event,
  restore จาก `SavedStateHandle` และการ persist ตะกร้า
- `CartItemTest`, `OrderUiStateTest` — ทดสอบ derived values (lineTotal, cartCount,
  cartTotal, isCartEmpty)

_(ยังไม่ได้รันเทสต์บนเครื่องนี้ เพราะไม่มี Gradle/Android SDK — รันได้ด้วย_
_`./gradlew testDebugUnitTest` ใน Android Studio)_

## 9. บันทึกละเอียด: ขั้นตอนการเขียน unit test (ตั้งแต่ต้นจนจบ)

ส่วนนี้เล่ากระบวนการของหัวข้อ `## 8` แบบเป็นลำดับ เพื่อให้ย้อนดูได้ว่าเริ่มจากไหน
ทำอะไรต่ออะไร และทำไม

**ขั้นที่ 1 — ตัดสินใจว่าจะเทสต์อะไรและระดับไหน**
- เลือกเขียน **local unit test (รันบน JVM)** ก่อน เพราะเร็วและคุ้มสุด ไม่ต้องใช้ emulator
- เป้าหมายหลักคือ `OrderViewModel` เพราะเป็นที่รวม business logic (ตะกร้า/ยอดรวม/event)
- ทำได้ง่ายเพราะโครงสร้าง MVVM แยก layer และฉีด dependency ผ่าน constructor อยู่แล้ว

**ขั้นที่ 2 — เตรียมเครื่องมือ (dependencies)**
- เพิ่ม `kotlinx-coroutines-test` (ไว้คุม coroutine/`viewModelScope` ในเทสต์)
- เพิ่ม `Turbine` (ไว้ทดสอบ `Flow`/event ให้เขียนง่าย)
- ประกาศไว้ใน `gradle/libs.versions.toml` แล้วอ้างใน `app/build.gradle.kts` เป็น
  `testImplementation`

**ขั้นที่ 3 — สร้างโครงพื้นฐานของเทสต์ (test infrastructure)**
- `MainDispatcherRule` — เพราะ `viewModelScope` รันบน `Dispatchers.Main` ซึ่งไม่มีจริง
  ในเทสต์ JVM จึงต้องสลับเป็น test dispatcher ด้วย `Dispatchers.setMain(...)`
- `FakeFoodRepository` — ใช้ "fake" แทน mock เพื่อคืนเมนูคงที่ที่คาดเดาได้
  (`ITEM_A` = 55 บาท, `ITEM_B` = 50 บาท)

**ขั้นที่ 4 — เขียนเทสต์ `OrderViewModel` ไล่ทีละพฤติกรรม**
1. โหลดเมนูเข้า `uiState` ตอน init (ใช้ `runTest` + `advanceUntilIdle` รอ coroutine)
2. สถานะเริ่มต้น: ตะกร้าว่าง, count = 0, total = 0
3. `addToCart` เพิ่มจำนวนและอัปเดต count/total (เทียบ `BigDecimal` ด้วย `compareTo`)
4. ยอดรวมของสินค้าหลายชนิด
5. `decreaseQuantity` จนเหลือ 0 แล้วรายการหายจากตะกร้า
6. `decreaseQuantity` ตอนจำนวน > 1 แล้วรายการยังอยู่
7. `placeOrder` ตอนตะกร้าว่าง → ไม่ยิง event (ตรวจด้วย Turbine `expectNoEvents`)
8. `placeOrder` ตอนมีของ → ยิง `OrderPlaced` พร้อม total ที่ถูก แล้วเคลียร์ตะกร้า
9. restore ตะกร้าจาก `SavedStateHandle` (จำลอง process death)
10. บันทึกตะกร้าลง `SavedStateHandle` เมื่อมีการเปลี่ยนแปลง

**ขั้นที่ 5 — เขียนเทสต์ระดับ model/state**
- `CartItemTest` — `lineTotal` = ราคา × จำนวน
- `OrderUiStateTest` — derived values (`cartCount`, `cartTotal`, `isCartEmpty`)

**ขั้นที่ 6 — ปิดงาน**
- อัปเดต `CHANGELOG.md` (หัวข้อ `## 8`), commit `2f0347a`, push ขึ้น branch
- ยังไม่ได้รันจริงบนเครื่องนี้ (ไม่มี Gradle/Android SDK) — รันด้วย
  `./gradlew testDebugUnitTest` ใน Android Studio

## 10. ปรับเทสต์ให้เป็น black-box มากขึ้น + เพิ่มเคสขอบ

**Changed**
- แปลงเทสต์ persist จากการอ่าน key ภายใน (`"cart_quantities"`) เป็น **round-trip
  test**: สร้าง ViewModel ใหม่จาก `SavedStateHandle` ตัวเดิมแล้วเช็คว่าตะกร้าถูก
  restore กลับมา (ทดสอบพฤติกรรมจริง ไม่ผูกกับ implementation)

**Added**
- เคส `decreaseQuantity` กับสินค้าที่ไม่มีในตะกร้า → ต้องเป็น no-op (ไม่พัง)
- เคสตรวจว่าตะกร้าคงลำดับการเพิ่มสินค้า (insertion order)

_(ยังไม่ได้รันเทสต์บนเครื่องนี้ — รันด้วย `./gradlew testDebugUnitTest`)_

## 11. เพิ่มไฟล์ presentation

**Added**
- `docs/presentation.md` — สไลด์นำเสนอ (Markdown + Mermaid diagrams) ครอบคลุม
  ภาพรวมระบบ (สถาปัตยกรรม MVVM, flow ผู้ใช้, tech stack) และขั้นตอนการเขียน test
  แบบละเอียดตั้งแต่ต้นจนจบ พร้อม diagram ประกอบ (test pyramid, 6 ขั้นตอน,
  test infrastructure, round-trip sequence)

## 12. เพิ่ม presentation แบบ HTML (slide deck)

**Added**
- `docs/presentation.html` — สไลด์แบบ reveal.js (โหลดผ่าน CDN) เนื้อหาเดียวกับ
  `presentation.md` แต่เปิดในเบราว์เซอร์แล้วนำเสนอได้ทันที (กดลูกศรเลื่อนสไลด์)
  diagram เรนเดอร์ด้วย Mermaid, ใช้ฟอนต์ไทย Sarabun
  หมายเหตุ: ต้องต่ออินเทอร์เน็ตเพื่อโหลด reveal.js/Mermaid/ฟอนต์จาก CDN

## 13. แก้ Mermaid syntax error ในไฟล์ presentation

**Fixed**
- แก้ "Syntax error in text" ของ Mermaid (10.9.x) ทั้งใน `presentation.md` และ
  `presentation.html`:
  - ใส่เครื่องหมายคำพูดให้ label ของเส้นทุกเส้นที่มีอักขระพิเศษ (`/`, `+`, `?`)
    เช่น `-->|"กดเพิ่ม / ลด"|` และ decision node `O{"ตะกร้าว่าง?"}`
  - ปรับ sequence diagram: เอา `->` และวงเล็บใน alias/ข้อความออก ใช้คำอธิบายแทน
    เพื่อไม่ให้ชนกับไวยากรณ์ของ Mermaid

## 14. ขยายส่วน Test ใน presentation.html + เพิ่ม Data Flow Diagram

**Changed**
- เขียน `docs/presentation.html` ใหม่ ขยายส่วนการเขียน test ให้ละเอียดขึ้นมาก:
  เพิ่มตัวอย่างโค้ดจริง (`MainDispatcherRule`, `FakeFoodRepository`, เทสต์ AAA,
  Turbine), sequence diagram การควบคุม coroutine (`advanceUntilIdle`) และการทดสอบ
  event, โครงไฟล์เทสต์, ตารางแมปเทสต์กับพฤติกรรม, best practices และวิธีรันเทสต์

**Added**
- **Data Flow Diagram (DFD)** 2 ภาพ: ภาพรวม data flow ของแอป (ผู้ใช้ ↔ View ↔
  ViewModel ↔ data store) และ DFD แสดงขอบเขตที่ unit test ครอบคลุม

## 15. เพิ่มเอกสารประวัติเวอร์ชัน Android

**Added**
- `docs/android-versions.md` — เอกสารอธิบายทุกเวอร์ชัน Android ตั้งแต่ 1.0 ถึง 17
  (ฟีเจอร์/จุดเด่น, ข้อดี-ข้อเสีย, ประเด็นความปลอดภัย/ช่องโหว่ที่เคยมี, และความ
  แตกต่างของแต่ละเวอร์ชัน) พร้อมตารางสรุป, วิวัฒนาการด้านความปลอดภัย และแหล่งอ้างอิง
  สำหรับ Android 16–17 (ข้อมูลปี 2025–2026)

## สิ่งที่ยังค้าง / แผนถัดไป (Backlog)

- GitHub Actions (CI) build + test + validate Gradle wrapper อัตโนมัติทุก PR
- (ตัวเลือก) เปลี่ยนไปใช้ assertion library เช่น Truth เพื่อ error message ที่อ่านง่ายขึ้น
- เพิ่ม loading/error state ใน `OrderUiState` เมื่อเปลี่ยนไปใช้ข้อมูลจริง
