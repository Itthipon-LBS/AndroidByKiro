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

## 16. เพิ่มตารางเทียบ min/target/compile SDK สำหรับนักพัฒนา

**Added**
- ใน `docs/android-versions.md` เพิ่มหัวข้อ "คู่มือสำหรับนักพัฒนา":
  ความหมายของ `minSdk`/`targetSdk`/`compileSdk`, ตารางเทียบเวอร์ชัน ↔ API level
  สำหรับกรอกค่า SDK, หมายเหตุเส้นตาย target API ของ Play, ตารางค่าที่แนะนำตามโจทย์แอป
  (รวมค่าของโปรเจกต์นี้: minSdk 24 / target 34 / compile 34) และเช็กลิสต์ตอนอัปเกรด targetSdk

## 17. เสริมข้อมูลเวอร์ชันแรกสู่ผู้ใช้ (HTC Dream / T-Mobile G1)

**Added**
- ใน `docs/android-versions.md` เพิ่มกล่องหมายเหตุใต้ Android 1.0 ว่าเวอร์ชันแรกที่
  ถึงมือผู้ใช้ทั่วไปคือ Android 1.0 (ก.ย. 2008) บนเครื่อง HTC Dream / T-Mobile G1,
  ระบุรายละเอียดเครื่อง (คีย์บอร์ด QWERTY จริง, แทร็กบอล) และแยกให้ชัดระหว่าง SDK
  พรีวิวสำหรับนักพัฒนา (2007) กับเวอร์ชันสู่ผู้บริโภคจริง

## 18. อธิบายความหมายของ codename ในตารางเวอร์ชัน

**Added**
- ใน `docs/android-versions.md` เพิ่มหมายเหตุใต้ตารางสรุป อธิบายว่า "– ภายใน" คือชื่อ
  รหัสที่ใช้ภายใน Google ไม่เป็นทางการ (เช่น 1.0 = Astro Boy, 1.1 = Bender),
  ระบบชื่อขนมตามตัวอักษรเริ่มจริงที่ 1.5 Cupcake และตั้งแต่ Android 10 เลิกใช้ชื่อขนม
  ในการตลาดแต่ยังมีชื่อภายใน

## 19. ขยายความ HTML5 ในหัวข้อ Android 2.0–2.1 Eclair

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบายว่า HTML5 ใน Eclair คืออะไร: ความสามารถ
  ที่เพิ่มเข้ามา (`<video>`/`<audio>`, `<canvas>`, Geolocation, AppCache, Web Storage,
  CSS3), เหตุผลที่สำคัญกับยุคนั้น (เว็บไม่ต้องพึ่ง Flash, ปูทาง web/hybrid app) และ
  ข้อจำกัดในตอนนั้น (รองรับไม่ครบ, JS ยังช้าก่อน Froyo)

## 20. ชี้แจงระดับการรองรับ HTML5 ก่อน/หลัง Eclair

**Changed**
- ใน `docs/android-versions.md` เพิ่มกล่องชี้แจงว่า Android 1.x รองรับ HTML5
  **บางส่วน** อยู่แล้ว (ผ่านเอนจิน WebKit) ส่วน Eclair 2.0/2.1 เป็นการ **ยกระดับให้ดี
  และครบขึ้น** ไม่ใช่จุดที่มี HTML5 เป็นครั้งแรก และหมายเหตุว่า HTML5 เพิ่งเป็นมาตรฐาน
  ทางการปี 2014 (การรองรับจึงค่อย ๆ เพิ่มทีละเวอร์ชัน)

## 21. ขยายความ JIT ในหัวข้อ Android 2.2 Froyo

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย JIT (Just-In-Time): บริบท Dalvik VM
  (จาก interpret → JIT), เหตุผลที่ทำให้เร็วขึ้น และตารางเทียบ Interpret / JIT / AOT
  พร้อมหมายเหตุว่า ART ยุคหลังใช้ JIT+AOT ผสม + profile-guided compilation

## 22. ขยายความเอนจิน JS V8 ในหัวข้อ Android 2.2 Froyo

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย V8 (JavaScript engine ของ Google):
  บทบาทในเบราว์เซอร์ Froyo, เหตุผลที่เร็ว (JIT, GC, hidden classes/inline caching)
  และตารางแยกความต่างระหว่าง **V8 (เร่ง JS ในเบราว์เซอร์)** กับ **Dalvik JIT (เร่งโค้ดแอป)**
  ที่มาพร้อมกันใน Froyo

## 23. ขยายความ Adobe Flash ในหัวข้อ Android 2.2 Froyo

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย Adobe Flash: ช่วยเรื่องอะไรในยุคนั้น
  (ดูวิดีโอ/เกม/แอนิเมชัน/สื่อโต้ตอบบนเว็บ, จุดขาย "full web" แข่ง iPhone), ข้อเสีย
  (กินทรัพยากร/แบต, ช่องโหว่, ไม่เหมาะจอสัมผัส) และบทสรุปเชิงประวัติ (ถูกแทนที่ด้วย
  HTML5, Adobe เลิกทำมือถือปี 2011 และยุติทั้งหมดปลายปี 2020)

## 24. กำหนดสีหัวข้อในเอกสาร Android เป็น #00d1b2

**Changed**
- ใน `docs/android-versions.md` ฝัง `<style>` บนสุดของไฟล์ให้หัวข้อทุกระดับ (h1–h6)
  แสดงเป็นสี `#00d1b2` — เห็นผลใน VS Code Markdown Preview
  (หมายเหตุ: GitHub จะตัด inline style/`<style>` ออก จึงไม่แสดงสีบน GitHub)

## 25. ขยายความ "ติดตั้งแอปบน SD (App2SD)" ในหัวข้อ Froyo

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย App2SD: ย้ายแอปบางส่วนไปการ์ด SD,
  เหตุผลที่สำคัญ (หน่วยความจำภายในยุคนั้นเล็ก), การทำงาน/ข้อจำกัด (`android:installLocation`,
  widget ใช้ไม่ได้, SD ช้ากว่า, ความเสี่ยงความปลอดภัย) และบทสรุป (ต่อมาแทนด้วย
  Adoptable Storage ใน Android 6.0)

## 26. ขยายความ NFC และ SIP (VoIP) ในหัวข้อ Gingerbread

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย NFC (การสื่อสารระยะใกล้, อ่านแท็ก,
  ปูทางจ่ายเงินแบบแตะ, เครื่องแรก Nexus S) และ SIP/VoIP (โทรด้วยเสียงผ่านอินเทอร์เน็ต,
  SIP API ในตัว, ประโยชน์/ข้อจำกัด)

## 27. เพิ่มคำอธิบายช่องโหว่ยุค Gingerbread (เชิงการศึกษา/ป้องกัน)

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย **ประเภทและสาเหตุ** ของช่องโหว่ยุค
  Gingerbread (local privilege escalation เช่น GingerBreak/zergRush) แบบเชิงแนวคิด
  พร้อมผลกระทบและ **แนวทางป้องกัน/มาตรการที่ Google เพิ่มภายหลัง** (SELinux, verified
  boot, ASLR, seccomp, monthly patch, Mainline) — **ไม่มีขั้นตอน/โค้ดสำหรับโจมตี**

## 28. ขยายความ UI แบบ holographic (ธีม Holo) ในหัวข้อ Honeycomb

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบายธีม Holo: เป็น design language แรกของ
  Android (โทนมืด + ฟ้าไซแอน, ไม่เกี่ยวโฮโลแกรมจริง), ความสำคัญเชิงเทคนิค (ธีมมาตรฐาน
  ครั้งแรก ช่วยให้ UI สม่ำเสมอข้ามเครื่อง ลด fragmentation ด้านหน้าตา) และวิวัฒนาการ
  ไปสู่ Material Design ใน Lollipop

## 29. ขยายความการไม่เปิด AOSP ของ Honeycomb

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย AOSP (Android Open Source Project),
  เหตุผลที่ Google ไม่ปล่อยซอร์สของ Honeycomb ในช่วงนั้น (ทำเร่งด่วนเพื่อแท็บเล็ต,
  ยังไม่พร้อมสำหรับอุปกรณ์ทั่วไป), ผลกระทบ (ถูกวิจารณ์เรื่องความโปร่งใส/ชุมชนเข้าไม่ถึง)
  และการคลี่คลายเมื่อรวมเข้ากับ ICS 4.0

## 30. ขยายความ "แยกจากเวอร์ชันโฟน" ในหัวข้อ Honeycomb

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบายว่าในปี 2011 Android แตกเป็น 2 สาย
  (โฟน 2.3 Gingerbread / แท็บเล็ต 3.x Honeycomb) พร้อมตารางเทียบ, เหตุผลที่เป็น
  ข้อเสีย (สับสน, ภาระนักพัฒนา, fragmentation) และการรวมกลับเป็น codebase เดียวใน ICS 4.0

## 31. ขยายความ "การแจ้งเตือนแบบขยาย" ในหัวข้อ Jelly Bean

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย Expandable/Rich Notifications:
  สไตล์ BigText/BigPicture/Inbox, action buttons, ประโยชน์ และวิวัฒนาการต่อไปสู่
  direct reply, notification channels, bundled notifications, Conversations/Bubbles

## 32. ขยายความ "Google Now" ในหัวข้อ Jelly Bean

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย Google Now: ผู้ช่วยอัจฉริยะเชิงรุก
  ยุคแรก (Now Cards ตามบริบท), ความต่างจาก Siri (เชิงรุก vs ถาม-ตอบ) และวิวัฒนาการ
  สู่ Now on Tap → Google Assistant → Google Discover

## 33. ขยายความ "Immersive mode" ในหัวข้อ KitKat

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย Immersive mode: ซ่อน status/navigation
  bar พร้อมกันเพื่อเต็มจอ, 2 แบบ (Immersive vs Immersive Sticky), ความต่างจากโหมดเดิม
  (lean back/low profile) และหมายเหตุนักพัฒนา (system UI flags → WindowInsetsController)

## 34. ขยายความ "ART runtime" ในหัวข้อ KitKat

**Added**
- ใน `docs/android-versions.md` เพิ่มคำอธิบาย ART (Android Runtime): เป็น runtime ใหม่
  แทน Dalvik, ตารางเทียบ Dalvik (JIT) vs ART (AOT), ข้อดี/ข้อแลกเปลี่ยน, วิวัฒนาการ
  (Nougat ใช้ JIT+AOT+profile-guided, อัปเดตผ่าน Mainline) และลิงก์โยงกับหัวข้อ Froyo

## 35. เพิ่มหัวข้อ "ศัพท์ทางเทคนิค" (Glossary) ในเอกสาร Android

**Added**
- ใน `docs/android-versions.md` เพิ่มหัวข้อ Glossary รวมคำศัพท์ที่ปรากฏในเอกสาร
  จัดกลุ่มเป็น: Runtime & ประสิทธิภาพ, UI & ดีไซน์, การเชื่อมต่อ & สื่อ, สถาปัตยกรรม
  ระบบ & การอัปเดต, สิทธิ์ & ความเป็นส่วนตัว, และความปลอดภัย

## 36. ขยายความ AOT ใน Glossary

**Changed**
- ใน `docs/android-versions.md` ขยายรายการ **AOT** ใน Glossary: อธิบายการคอมไพล์
  ล่วงหน้าตอนติดตั้ง (ไฟล์ `.oat`/`.odex`), ข้อดี/ข้อเสีย, การเทียบกับ JIT/Interpret,
  บริบทใน ART/Nougat และตัวอย่างนอก Android (Kotlin/Native, GraalVM, Flutter)

## 37. เพิ่ม Project Volta ใน Glossary

**Added**
- ใน `docs/android-versions.md` เพิ่มรายการ **Project Volta** (JobScheduler,
  Battery Saver, Battery Historian) และ **JobScheduler/WorkManager** ในหัวข้อ
  ศัพท์ทางเทคนิค พร้อมโยงวิวัฒนาการสายประหยัดแบต (Doze → Background limits →
  Adaptive Battery → WorkManager)

## 38. เพิ่มหัวข้อ "ขยายความศัพท์ทางเทคนิค" + ข้อมูล Stagefright

**Added**
- ใน `docs/android-versions.md` เพิ่มหัวข้อ "ขยายความศัพท์ทางเทคนิค" (เจาะลึกศัพท์จาก
  Glossary) และใส่ข้อมูล **Stagefright** แบบเชิงการศึกษา/ป้องกัน: คืออะไร, ทำไมร้ายแรง
  (RCE ผ่านไฟล์มีเดีย), ขอบเขต (2.2–5.1), ผลสืบเนื่อง (แพตช์รายเดือน, แยก media server)
  และการป้องกัน — ไม่มีขั้นตอน/โค้ดโจมตี

## 39. จัดระเบียบ Glossary: ย้ายคำอธิบายยาวไป "ขยายความศัพท์ทางเทคนิค"

**Changed**
- ใน `docs/android-versions.md` ปรับ Glossary ให้ทุกรายการเป็นคำอธิบายสั้น เข้าใจง่าย
  และย้ายคำอธิบายแบบยาวของ **AOT** และ **Project Volta** ไปไว้ในหัวข้อ "ขยายความ
  ศัพท์ทางเทคนิค" (ยังคงหัวข้อย่อยเดิมใน Glossary พร้อมโน้ตให้ไปดูส่วนขยายความ)

## 40. เพิ่ม SMS/MMS ใน Glossary

**Added**
- ใน `docs/android-versions.md` เพิ่มรายการ **SMS / MMS** ในกลุ่ม "การเชื่อมต่อ & สื่อ"
  ของ Glossary (SMS = ตัวอักษรล้วน, MMS = แนบสื่อได้, ต่อมาถูกแทนด้วยแอปแชต/RCS)

## 41. เพิ่มการเจาะลึก SELinux ใน "ขยายความศัพท์ทางเทคนิค"

**Added**
- ใน `docs/android-versions.md` เพิ่มการเจาะลึก **SELinux**: คืออะไร, MAC vs DAC,
  โหมด permissive/enforcing, ไทม์ไลน์บน Android (4.3 permissive → 4.4 บางส่วน →
  5.0 enforcing เต็มรูป) และเหตุผลที่สำคัญ

**Changed**
- ย่อรายการ SELinux ใน Glossary พร้อมโน้ตให้ไปดูส่วนขยายความ

## 42. เพิ่ม App Standby (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **App Standby** ใน Glossary (พร้อมความต่างจาก
  Doze) และเพิ่มการเจาะลึก **App Standby & Doze** ในหัวข้อ "ขยายความศัพท์ทางเทคนิค"
  (หลักการ, ต่างจาก Doze ระดับเครื่อง vs รายแอป, ข้อยกเว้น, วิวัฒนาการสู่ App Standby
  Buckets ใน Android 9)

**Changed**
- ปรับรายการ Doze ใน Glossary ให้ระบุว่าเป็นระดับ "ทั้งเครื่อง" เพื่อเทียบกับ App Standby

## 43. เพิ่ม Auto Backup (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Auto Backup** ใน Glossary และเจาะลึกใน
  "ขยายความศัพท์ทางเทคนิค": หลักการ (สำรองขึ้น Drive/กู้คืนอัตโนมัติ, ~25MB, ตอนชาร์จ+
  Wi-Fi), การควบคุมของนักพัฒนา (`allowBackup`, backup rules) และประเด็นความปลอดภัย
  (ยกเว้นข้อมูลอ่อนไหว, E2E encryption)

## 44. เพิ่ม Bundled Notifications & Direct Reply (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Bundled notifications** และ **Direct Reply**
  ใน Glossary และเจาะลึกใน "ขยายความศัพท์ทางเทคนิค" (จัดกลุ่มแจ้งเตือนของแอปเดียวกัน
  + ตอบข้อความจากแจ้งเตือน, API `setGroup()`/`RemoteInput`, ต่อยอดสู่ channels/Bubbles)

## 45. เพิ่มการเจาะลึก Vulkan + OpenGL ES ใน Glossary

**Added**
- ใน `docs/android-versions.md` เพิ่มการเจาะลึก **Vulkan** ในหัวข้อ "ขยายความศัพท์
  ทางเทคนิค" (ต่างจาก OpenGL ES, overhead ต่ำ, มัลติเธรด, ข้อแลกเปลี่ยน, ANGLE,
  เทียบ Metal/DirectX 12) และเพิ่มรายการ **OpenGL ES** ใน Glossary

**Changed**
- ย่อ/ปรับรายการ Vulkan ใน Glossary พร้อมโน้ตให้ดูส่วนขยายความ

## 46. เพิ่ม FBE/FDE (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **FBE** และ **FDE** ใน Glossary และเจาะลึก
  **File-Based Encryption vs FDE** ในหัวข้อ "ขยายความศัพท์ทางเทคนิค": เข้ารหัสรายไฟล์,
  เทียบ FDE, Direct Boot (โซน DE/CE), ประโยชน์ และหมายเหตุนักพัฒนา (`directBootAware`)

## 47. เพิ่ม Data Saver (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Data Saver** ใน Glossary และเจาะลึกใน
  "ขยายความศัพท์ทางเทคนิค": หลักการ (จำกัดเน็ตเบื้องหลัง), allowlist, API
  (`getRestrictBackgroundStatus`) และความต่างจาก App Standby/Doze (เน็ต vs แบต,
  ผู้ใช้เปิดเอง vs ระบบตัดสิน)

## 48. เพิ่มการเจาะลึก Project Treble

**Added**
- ใน `docs/android-versions.md` เพิ่มการเจาะลึก **Project Treble** ในหัวข้อ "ขยายความ
  ศัพท์ทางเทคนิค": ปัญหาเดิม (framework ผูกกับ vendor), VINTF/HAL (HIDL→AIDL),
  ประโยชน์ (อัปเดตเร็วขึ้น), GSI และการต่อยอดสู่ Project Mainline

**Changed**
- ใส่โน้ตให้รายการ Project Treble ใน Glossary ไปดูส่วนขยายความ

## 49. เพิ่ม PiP และ Autofill (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **PiP (Picture-in-Picture)** และ **Autofill
  Framework** ใน Glossary และเจาะลึกใน "ขยายความศัพท์ทางเทคนิค" (PiP: หน้าต่างวิดีโอ
  ลอย, `enterPictureInPictureMode`; Autofill: เติมฟอร์มระดับ OS, `autofillHints`)

## 50. เพิ่ม Background execution limits และ Adaptive icons (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Background execution limits** และ
  **Adaptive icons** ใน Glossary และเจาะลึกใน "ขยายความศัพท์ทางเทคนิค"
  (background limits: หยุด service เบื้องหลัง → ใช้ WorkManager/foreground service;
  adaptive icons: ไอคอน foreground/background + mask, โยงกับไอคอนของโปรเจกต์นี้)

## 51. เพิ่ม KRACK (Glossary + เจาะลึก เชิงการศึกษา/ป้องกัน)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **KRACK** ใน Glossary และเจาะลึกใน "ขยายความ
  ศัพท์ทางเทคนิค": คืออะไร (Key Reinstallation Attack บน WPA2 4-way handshake),
  ทำไมสำคัญ (ระดับโปรโตคอล กระทบหลายแพลตฟอร์ม), ผลกับ Android และการป้องกัน
  (แพตช์, HTTPS/VPN, WPA3) — ไม่มีขั้นตอน/โค้ดโจมตี

## 52. เพิ่มฟีเจอร์เด่น Android 9 Pie (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Gesture navigation, Adaptive Battery/
  Brightness, Digital Wellbeing, Display cutout/notch, App Actions/Slices** ใน
  Glossary และรวมการเจาะลึกไว้ในหัวข้อ "ฟีเจอร์เด่นของ Android 9 Pie" ในส่วนขยายความ

## 53. เพิ่มการเจาะลึก Scoped Storage และ Project Mainline (Android 10)

**Added**
- ใน `docs/android-versions.md` เพิ่มการเจาะลึก **Scoped Storage** (โมเดลเข้าถึงไฟล์
  จำกัด, MediaStore/SAF/Photo Picker, ผลกระทบต่อนักพัฒนา) และ **Project Mainline**
  (อัปเดตโมดูลระบบผ่าน Play, APEX, ต่างจาก Treble) ในหัวข้อ "ขยายความศัพท์ทางเทคนิค"

**Changed**
- ใส่โน้ตให้รายการ Scoped Storage และ Project Mainline ใน Glossary ไปดูส่วนขยายความ

## 54. เพิ่ม OEM และ Live Caption

**Added**
- ใน `docs/android-versions.md` เพิ่ม **OEM** ใน Glossary (ผู้ผลิตอุปกรณ์ที่นำ Android
  ไปใส่เครื่อง) และเพิ่ม **Live Caption** ทั้งใน Glossary และเจาะลึกใน "ขยายความศัพท์
  ทางเทคนิค" (คำบรรยายอัตโนมัติ on-device, accessibility, ทิศทาง on-device AI)

## 55. เพิ่ม Approximate location (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Approximate location** ใน Glossary และเจาะลึก
  ใน "ขยายความศัพท์ทางเทคนิค": ตัวเลือก Precise/Approximate, คู่ permission
  FINE/COARSE, เหตุผลด้านความเป็นส่วนตัว และสายวิวัฒนาการ location (10 → 12 → 17)

## 56. เพิ่ม Themed app icons (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Themed app icons** ใน Glossary และเจาะลึกใน
  "ขยายความศัพท์ทางเทคนิค": เปลี่ยนสีไอคอนตาม Material You, ต่างจาก adaptive icons
  (สี vs รูปทรง), การใส่ `<monochrome>` และการเชื่อมกับไอคอนของโปรเจกต์นี้

## 57. เพิ่ม Predictive back, Regional preferences, Passkeys

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Predictive back, Regional preferences,
  Passkeys** ใน Glossary และเจาะลึกใน "ขยายความศัพท์ทางเทคนิค" (predictive back:
  พรีวิวย้อนกลับ + API; regional prefs: ภูมิภาคแยกจากภาษา; passkeys: ล็อกอินไร้รหัสผ่าน
  FIDO/WebAuthn + Credential Manager)

## 58. เพิ่มฟีเจอร์เด่น Android 15 (Private Space, partial screen sharing, satellite)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Private Space, Partial screen sharing,
  Satellite connectivity** ใน Glossary และรวมการเจาะลึกไว้ในหัวข้อ "ฟีเจอร์เด่นของ
  Android 15" ในส่วนขยายความ

## สิ่งที่ยังค้าง / แผนถัดไป (Backlog)

- GitHub Actions (CI) build + test + validate Gradle wrapper อัตโนมัติทุก PR
- (ตัวเลือก) เปลี่ยนไปใช้ assertion library เช่น Truth เพื่อ error message ที่อ่านง่ายขึ้น
- เพิ่ม loading/error state ใน `OrderUiState` เมื่อเปลี่ยนไปใช้ข้อมูลจริง
