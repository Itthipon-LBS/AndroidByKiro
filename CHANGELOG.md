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

## 59. เพิ่มฟีเจอร์เด่น Android 16 (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Material 3 Expressive, Live Updates/
  progress-centric notifications, Advanced Protection, 16 KB page size, LE Audio,
  HDR screenshots, Generic Bootloader** ใน Glossary และรวมการเจาะลึกไว้ในหัวข้อ
  "ฟีเจอร์เด่นของ Android 16" ในส่วนขยายความ

## 60. เพิ่ม Trunk Stable (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Trunk Stable** ใน Glossary และเจาะลึกใน
  "ขยายความศัพท์ทางเทคนิค": โมเดลพัฒนา trunk เดียว + feature flags, ปล่อย AOSP
  ปีละ 2 รอบตั้งแต่ปี 2026, ประโยชน์ และผลต่อนักพัฒนาแอป

## 61. เพิ่ม Accessibility (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Accessibility (a11y)** ใน Glossary และเจาะลึก
  ในหัวข้อ "ขยายความศัพท์ทางเทคนิค": ฟีเจอร์ระบบ (TalkBack, ขยายฟอนต์, Live Caption,
  Switch/Voice Access), แนวทางนักพัฒนา (`contentDescription`, ขนาดแตะ, contrast,
  ฟอนต์ sp — โยงกับปุ่มในแอป FoodOrder) และหมายเหตุการทดสอบจริง/WCAG

## 62. เพิ่ม AccessibilityService (แยกจาก a11y เชิงผู้ใช้)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **AccessibilityService** ใน Glossary และเจาะลึก
  ในหัวข้อ "ขยายความศัพท์ทางเทคนิค": อธิบายความหมายเชิงเทคนิค (อ่าน/ควบคุมหน้าจอ),
  การใช้งานนอก a11y, ประเด็นความปลอดภัย (มัลแวร์หลอกให้เปิด, การคุมเข้มของ Google/
  Advanced Protection) และคำที่เกี่ยวข้อง (accessibility tree) พร้อมสรุปแยก 2 ความหมาย

## 63. เพิ่ม Accessibility APIs ฝั่งแอป + Accessibility testing tools

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Accessibility APIs (ฝั่งแอป)** และ
  **Accessibility testing tools** ใน Glossary และเจาะลึกใน "ขยายความศัพท์ทางเทคนิค":
  ฝั่งแอป (contentDescription/importantForAccessibility/labelFor/Delegate, Compose
  semantics, โยงกับ FoodOrder) และเครื่องมือ (Accessibility Scanner, Espresso checks,
  ATF, lint)

## 64. เพิ่มหมายเหตุกันสับสนความหมาย accessibility

**Added**
- ใน `docs/android-versions.md` เพิ่มหมายเหตุท้ายหัวข้อ accessibility ว่าคำนี้ยังถูกใช้ใน
  ความหมายทั่วไปทางโปรแกรมมิ่ง (code/member visibility, network accessibility) ซึ่งไม่
  เกี่ยวกับ a11y ของ Android เพื่อกันสับสน

## 65. เพิ่มความหมาย accessibility เชิงโค้ด (เฉพาะ Android/Kotlin)

**Added**
- ใน `docs/android-versions.md` เพิ่มหมายเหตุความหมาย "accessibility" เชิงโค้ดที่เกี่ยว
  กับการเขียนแอป Android: **visibility modifiers** (public/private/protected/internal
  ใน Kotlin, โยงกับ `OrderViewModel`) และ **Reflection `setAccessible`** พร้อมย้ำว่าไม่
  เกี่ยวกับ a11y

## 66. เพิ่มหัวข้อ "แนวทางพัฒนาแอปให้เข้าถึงได้ (สำหรับนักพัฒนา)"

**Added**
- ใน `docs/android-versions.md` เพิ่มหัวข้อเชิงปฏิบัติ: 2 แนวทาง (ทำแอปให้เข้าถึงได้ /
  ใช้ AccessibilityService), เช็กลิสต์ก่อนปล่อยแอป (a11y), เหตุผลที่คุ้มค่า และการโยง
  กับโปรเจกต์ FoodOrder

## 67. เพิ่ม Adaptive-first (Android 17) — Glossary + เจาะลึก

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Adaptive-first** ใน Glossary และเจาะลึกใน
  "ขยายความศัพท์ทางเทคนิค": บังคับ resizability จอใหญ่ใน Android 17, เหตุผล (จอใหญ่/
  พับได้), สิ่งที่นักพัฒนาต้องทำ (responsive layout, ไม่ล็อก orientation, state รอด
  config change) และการโยงกับ FoodOrder

## 68. เพิ่มฟีเจอร์ที่เหลือของ Android 17 (Glossary + เจาะลึก)

**Added**
- ใน `docs/android-versions.md` เพิ่ม **Floating Bubbles, Temporary location access,
  Stolen device lock, Intelligence system** ใน Glossary และรวมการเจาะลึกไว้ในหัวข้อ
  "ฟีเจอร์อื่นของ Android 17" ในส่วนขยายความ (ครบทุกไฮไลต์ของ Android 17 แล้ว)

## 69. ขยายแนวทาง accessibility: ความสามารถแพลตฟอร์ม + ประเภทแอป

**Added**
- ใน `docs/android-versions.md` (หัวข้อแนวทางพัฒนาแอปให้เข้าถึงได้) เพิ่ม:
  **ความสามารถแพลตฟอร์มสำหรับสร้างแอป accessibility** (TTS, SpeechRecognizer, haptics,
  `announceForAccessibility`, live region, custom actions, on-device ML, custom view
  a11y) และ **ประเภทแอป accessibility ที่พัฒนาได้** (AAC, สายตาเลือนราง, การได้ยิน,
  การควบคุมทางเลือก, ผู้มีภาวะพัฒนาการ) พร้อมมาตรฐาน (WCAG/EN 301 549/ADA)

**Changed**
- อัปเดตโน้ตโยงกับ FoodOrder ให้แนะนำ live region/announce, stateDescription/role

## 70. เพิ่มสรุป "มุมมองต่อ accessibility" (หลายมุม)

**Added**
- ใน `docs/android-versions.md` เพิ่มหัวข้อ "มุมมองต่อ accessibility" เป็นตารางสรุป:
  3 แกนหลัก (ผู้ใช้/โปรแกรมเมอร์/ตัวเครื่อง) + มุมเสริม (ธุรกิจ, กฎหมาย/มาตรฐาน, QA,
  นักออกแบบ, OEM, สังคม/จริยธรรม, ความปลอดภัย) พร้อมประโยคจำง่าย

## 71. จัดระเบียบเนื้อหาขยายความไปไว้ในหัวข้อศัพท์ทางเทคนิค

**Changed**
- ใน `docs/android-versions.md` ย้ายบล็อก "ขยายความ" ทั้ง 14 บล็อกที่เคยฝังอยู่ในหัวข้อ
  เวอร์ชัน (HTML5, JIT, V8, Adobe Flash, App2SD, NFC/SIP, ช่องโหว่ยุค Gingerbread, Holo,
  AOSP, การแยกสายโฟน/แท็บเล็ต, Expandable Notifications, Google Now, Immersive mode,
  ART runtime) ไปรวมไว้ต้นหัวข้อ "ขยายความศัพท์ทางเทคนิค" โดยแปลงหัวข้อตัวหนาเป็น `###`
  และเรียงตามลำดับเวอร์ชัน
- หัวข้อเวอร์ชันจึงเหลือเฉพาะ bullet (ฟีเจอร์/ข้อดี/ข้อเสีย/ความปลอดภัย/ต่างจากก่อนหน้า) อ่านง่ายขึ้น
- เพิ่มรายการ **Google Now** ในกลุ่ม "UI & ดีไซน์" ของ Glossary

## 72. เพิ่มหัวข้อ "การสร้าง Unique ID ในแต่ละเวอร์ชัน Android"

**Added**
- ใน `docs/android-versions.md` เพิ่มหัวข้อเรื่อง identifier/Unique ID: ตารางประเภท ID
  (IMEI/MAC, ANDROID_ID, AAID, App Set ID, FID, UUID), เส้นเวลาการเปลี่ยนแปลงตามเวอร์ชัน
  (Oreo 8 → ANDROID_ID ต่อแอป, Android 10 ตัด IMEI/MAC, 12 ปิด AAID ได้, 13 ต้องขอ AD_ID),
  แนวทางเลือกใช้ตามจุดประสงค์, ตัวอย่างโค้ดสร้าง UUID เอง และข้อควรระวังด้านความเป็นส่วนตัว/
  นโยบาย Play

## 73. เพิ่มวิธีทำ Unique ID ให้รอดการติดตั้งใหม่

**Added**
- ใน `docs/android-versions.md` (หัวข้อ Unique ID) เพิ่มหัวข้อย่อย "ทำ ID ให้รอดการถอน+
  ติดตั้งใหม่ได้อย่างไร": ระบุว่าไม่มีวิธีการันตี 100%, ตัวเลือกเรียงตามความคงทน (บัญชีผู้ใช้,
  UUID+Auto Backup, Block Store, ANDROID_ID, และ IMEI/MAC ที่ทำไม่ได้แล้ว), ตารางสรุป
  และตัวอย่างโค้ด Block Store

## 74. เพิ่มหมายเหตุ IMEI ซ้ำกันได้ในหัวข้อ Unique ID

**Added**
- ใน `docs/android-versions.md` เพิ่มหมายเหตุว่า IMEI ตามสเปกไม่ซ้ำ แต่ในโลกจริงซ้ำได้
  (เครื่องปลอม/แก้ IMEI/ค่า default), dual-SIM มี 2 ค่า, emulator คืน 0 และย้ำว่าใช้เป็น
  Unique ID ไม่ได้ (เข้าถึงไม่ได้ตั้งแต่ Android 10 + เป็น PII)

## 75. เพิ่มหมายเหตุ IMEI ไม่ขึ้นกับซิม (เทียบ IMEI/IMSI/ICCID)

**Added**
- ใน `docs/android-versions.md` เพิ่มหมายเหตุว่า IMEI เป็นเลขประจำตัวเครื่อง ไม่ขึ้นกับซิม
  (ใส่/ไม่ใส่ซิมค่าเท่าเดิม) พร้อมตารางเทียบ IMEI vs IMSI vs ICCID และหมายเหตุ dual-SIM/
  เครื่อง Wi-Fi-only

## 76. แก้ถ้อยคำ IMEI + เพิ่มหัวข้อ "ทำไม dual-SIM มักมี IMEI 2 ค่า"

**Changed**
- แก้ถ้อยคำจาก "dual-SIM มี IMEI 2 ค่า" เป็น "**มักมี** IMEI 2 ค่า (ตามจำนวน radio/modem)"
  เพื่อความถูกต้อง

**Added**
- ใน `docs/android-versions.md` เพิ่มหัวข้อย่อย "ทำไม dual-SIM มักมี IMEI 2 ค่า":
  IMEI ผูกกับ radio/modem ไม่ใช่ช่องซิม, DSDS ต้องมี radio แยก, กรณี modem เดียว/eSIM
  ที่อาจมี IMEI เดียว และ `getImei(slotIndex)` บน Android

## 77. เพิ่มคู่มือการทำ Test ใน Android (เริ่มต้นจนถึง production)

**Added**
- `docs/android-testing-guide.md` — คู่มือเขียน test ฉบับเข้าใจง่ายสำหรับมือใหม่: test
  pyramid, การตั้งค่า, unit test แรก, การรันเทสต์, ทดสอบ ViewModel (MainDispatcherRule +
  fake + coroutines + Turbine), UI test (Espresso), best practices, เส้นทางสู่ production
  (CI GitHub Actions, branch protection), เช็กลิสต์ และอ้างอิงตัวอย่างจริงในโปรเจกต์ FoodOrder

## 78. เพิ่มเอกสาร AI-Driven Development Lifecycle (AI-DLC)

**Added**
- `docs/ai-dlc-guide.md` — สรุป AI-DLC แบบเข้าใจง่าย: แก่นความคิด (AI นำ + คนตัดสินใจ),
  ความต่างจากของเดิม, 3 เฟส (Inception/Construction/Operations) + gate, workflow profiles,
  ตัวอย่างจริง (เพิ่มฟีเจอร์ประวัติการสั่งอาหารใน FoodOrder), best practices, ข้อควรระวัง
  และแหล่งอ้างอิง AWS (เนื้อหาเรียบเรียงใหม่ตามข้อกำหนดลิขสิทธิ์)

## 79. เพิ่มเอกสารฟีเจอร์ AI ของ Kiro (steering/skills/specs/hooks/MCP)

**Added**
- `docs/kiro-ai-features.md` — อธิบายกลไก AI ของ Kiro แบบเขียนตามได้จริง: Steering,
  Skills, Specs, Hooks, MCP, `#` context พร้อมตัวอย่างไฟล์/โค้ดจริงของแต่ละอัน,
  ตารางเลือกใช้, ข้อควรระวัง และอ้างอิงการใช้งานในโปรเจกต์นี้

## 80. เพิ่มหัวข้อ "Skill ถูกเรียกใช้ตอนไหน"

**Added**
- ใน `docs/kiro-ai-features.md` เพิ่มหัวข้อย่อยอธิบายว่า skill ถูกเรียกเมื่อไร (on-demand):
  งานตรงคำอธิบาย/ผู้ใช้เรียกเอง, กลไก 2 ชั้น (เห็นชื่อ+คำอธิบายตลอด แต่โหลดเนื้อหาเต็ม
  เมื่อจำเป็น), ตารางเทียบกับ steering และเคล็ดลับเขียน "ใช้เมื่อ:" ให้ถูกเรียกถูกจังหวะ

## 81. เพิ่มตารางข้อดี/ข้อเสียของแต่ละฟีเจอร์ AI ของ Kiro

**Added**
- ใน `docs/kiro-ai-features.md` เพิ่มตารางข้อดี/ข้อเสียของ Steering, Skills, Specs,
  Hooks, MCP, `#` context พร้อมสรุปการเลือกใช้ให้เหมาะกับงาน

## 82. เพิ่มหัวข้อเทียบฟีเจอร์ AI ข้ามเครื่องมือ (ไม่ผูกกับ Kiro)

**Added**
- ใน `docs/kiro-ai-features.md` เพิ่มหัวข้อ "ใช้ได้เฉพาะ Kiro ไหม?": แยกระดับแนวคิด
  (ใช้ทั่วไป) กับระดับไฟล์/คอนฟิก (เฉพาะ Kiro), ตารางเทียบข้ามเครื่องมือ (Cursor,
  Copilot, Claude Code, AGENTS.md) และย้ำว่า MCP เป็นมาตรฐานกลางที่ใช้ข้ามเครื่องมือได้

## 83. เพิ่มเอกสาร "Android สำหรับโปรแกรมเมอร์" (เริ่มด้วย Native vs Cross-Platform)

**Added**
- `docs/android-for-programmers.md` — สรุปสั้น เข้าใจง่าย: Native vs Cross-Platform
  (Flutter/React Native/KMP/.NET MAUI), ตารางเทียบ, แนวทางเลือก, พื้นฐาน Android Native
  ที่ต้องรู้ และเครื่องมือ (มีหัวข้อถัดไปไว้ต่อยอด)

## 84. เพิ่มหัวข้อต่อในเอกสาร Android สำหรับโปรแกรมเมอร์

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ: โครงสร้างโปรเจกต์, Lifecycle
  (จุดพลาดบ่อย + ViewModel/SavedStateHandle/viewLifecycleOwner), การจัดการ state/data
  (StateFlow, event, DataStore/Room, Retrofit), permissions & ความปลอดภัย และลิงก์อ่านต่อ

## 85. ขยายความ Native vs Cross-Platform (ข้อแตกต่าง/ข้อดี-เสีย/ปัญหาที่พบบ่อย)

**Added**
- ใน `docs/android-for-programmers.md` เจาะลึกหัวข้อ Native vs Cross-Platform:
  ข้อแตกต่างเชิงเทคนิค (การรัน UI, การเข้าถึง API, ขนาดแอป, การอัปเดต OS), ข้อดี/ข้อเสีย
  ของแต่ละแบบ, และปัญหาที่พบบ่อยแยกฝั่งโปรแกรมเมอร์และฝั่งผู้ใช้งานแอป

## 86. เพิ่มมุมเปรียบเทียบ Native vs Cross-Platform (ธุรกิจ/ทีม/ระยะยาว)

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มมุมเปรียบเทียบเชิงธุรกิจ/ทีม/ระยะยาว:
  การจ้างงาน, time-to-market/OTA, ecosystem/plugin, learning curve, การทดสอบ, ความยั่งยืน
  ของ framework, รูปแบบอุปกรณ์, ต้นทุนระยะยาว และมุมผู้ใช้เพิ่มเติม (a11y/integration/warm-up)

## 87. เพิ่มหัวข้อ "ทำไม Cross-Platform บางทีกดปุ่มไม่ติด"

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้ออธิบายอาการกดปุ่มแล้วทำงานบ้างไม่ทำงาน
  บ้างใน Cross-Platform: สาเหตุ (bridge/JS thread, UI thread บล็อก, touch/gesture ชน,
  hit area/overlay, debounce/state, async ไม่รอผล), ทำไม native เจอน้อยกว่า และวิธีแก้/เลี่ยง

## 88. เพิ่มหัวข้อ "กดปุ่มไม่ติดถาวรจนต้องรีสตาร์ทแอป"

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้ออาการปุ่มตายสนิทจนต้องรีสตาร์ท (stuck
  state): สาเหตุ (loading/async ค้าง, main thread บล็อก, state ไม่รีเซ็ต, exception เงียบ,
  leak), จุดสังเกตแยกสาเหตุ, วิธีหาสาเหตุ และวิธีแก้ด้วย `try/finally` + `withTimeout`

## 89. เพิ่มตารางปัญหาเชิงเทคนิคที่พบบ่อยใน Cross-Platform

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มตารางปัญหาเชิงเทคนิคที่พบบ่อยใน Cross-Platform
  15 ข้อ (memory leak, jank, OOM, back stack, keyboard, state หาย, deep link, platform bug,
  race condition, bridge, dependency hell, แบต/ร้อน, startup, ANR, offline) พร้อมอาการและ
  วิธีเลี่ยงสั้น ๆ + ธีมร่วมของปัญหา

## 90. จัดระเบียบเอกสาร android-for-programmers + ตารางเทียบปัญหา Native vs Cross-Platform

**Changed**
- เขียน `docs/android-for-programmers.md` ใหม่ทั้งไฟล์ให้เป็นระเบียบและอ่านง่ายขึ้น: แบ่ง
  เป็น 2 ส่วนชัดเจน — **ส่วน A: Native vs Cross-Platform** (คืออะไร, ตารางเทียบภาพรวม,
  ข้อดี/ข้อเสีย, แนวทางเลือก) และ **ส่วน B: พื้นฐาน Android Native** (สแตก, โครงสร้าง,
  lifecycle, state/data, permissions, เครื่องมือ) พร้อมสารบัญด้านบน
- ยุบเนื้อหาซ้ำซ้อนเดิม (เจาะข้อแตกต่าง/มุมธุรกิจ/มุมผู้ใช้หลายบล็อก) ให้กระชับ และย่อ
  หัวข้อเจาะลึกอาการ "กดปุ่มไม่ทำงาน" 2 กรณีให้สั้นลงแต่คงโค้ดตัวอย่าง `try/finally` + timeout

**Added**
- ตาราง **"ปัญหาเชิงเทคนิค: Native vs Cross-Platform"** เทียบปัญหาเดียวกัน 17 แถว
  (กดปุ่มไม่ติด/ค้าง, ANR, memory leak, jank, OOM, back stack, keyboard, state หาย,
  deep link, platform bug, race condition, debug crash, dependency, แบต/ร้อน, ฟีเจอร์ OS,
  a11y) ว่าแต่ละแนวทางเจอมาก/น้อยและเพราะอะไร

## 91. ขยายข้อดี/ข้อเสีย Native vs Cross-Platform แยกมุมนักพัฒนา/ผู้ใช้

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ A3.1 (มุมนักพัฒนา) และ A3.2 (มุมผู้ใช้งาน)
  ขยายข้อดี/ข้อเสียของทั้ง Native และ Cross-Platform อย่างละเอียด: ฝั่งนักพัฒนาครอบคลุม
  เครื่องมือ/การเข้าถึง API/debug/การซ้ำโค้ด/dependency/ความยั่งยืน; ฝั่งผู้ใช้ครอบคลุม
  ความลื่น/ความรู้สึกเป็นของระบบ/ขนาดไฟล์/เวลาเปิด/a11y/แบต พร้อมประโยคสรุปปิดท้าย

## 92. เพิ่มหัวข้อแนวโน้มในอนาคต Native vs Cross-Platform

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ A4.1 "แนวโน้มข้อดี/ข้อเสียในอนาคต":
  ทิศทางฝั่ง Native (Compose/SwiftUI, KMP, AI ช่วยเขียน, อุปกรณ์รูปแบบใหม่) และฝั่ง
  Cross-Platform (Impeller/New Architecture, ช่องว่าง performance แคบลง, ยังตามหลัง
  ฟีเจอร์ OS, ความเสี่ยงผู้ดูแล) พร้อมตาราง "สเปกตรัมการแชร์โค้ด" (Native → KMP →
  Compose Multiplatform → Flutter/RN) และข้อสรุปว่าเส้นแบ่งเบลอลง

## 93. เขียนส่วน B (พื้นฐาน Android Native) ใหม่ให้เป็นภาพรวมทั้งแพลตฟอร์ม

**Changed**
- ใน `docs/android-for-programmers.md` เขียนส่วน B ใหม่ให้เป็นภาพรวม Android ทั้งหมด
  ไม่อ้างอิงโปรเจกต์นี้: ตัดโครงสร้างโฟลเดอร์เฉพาะโปรเจกต์และโน้ต "FoodOrder/โปรเจกต์นี้ใช้"
  ออก แล้วขยายเป็น 10 หัวข้อกลาง ๆ — สแตก/ภาษา, สองแนวทาง UI (ตาราง XML vs Compose),
  4 App Components, สถาปัตยกรรม (MVVM/MVI), lifecycle, state/data (+DI), Navigation,
  permissions & security (+Scoped Storage/Keystore), testing (test pyramid) และเครื่องมือ/
  การเผยแพร่ (AAB/Play Console/target API)
- ปรับลิงก์ในสารบัญและตัดลิงก์ "โครงสร้างโปรเจกต์จริง (README)" ที่ผูกกับโปรเจกต์ออก

## 94. เพิ่มข้อดี/ข้อเสีย XML vs Compose + สิ่งที่ต้องคำนึงก่อนเลือก

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อย่อยใต้ B2: B2.1 ข้อดี/ข้อเสียของ
  XML+View และ Jetpack Compose (แยกเป็นข้อ ✅/❌ ของแต่ละแนวทาง) และ B2.2 สิ่งที่ต้อง
  คำนึงก่อนเลือกใช้ (โค้ดเบสเดิม, ทักษะทีม, ความใหม่โปรเจกต์, min SDK, ความซับซ้อน UI,
  library, ระยะยาว) พร้อมแนวทางปฏิบัติ (โปรเจกต์ใหม่ใช้ Compose, โปรเจกต์เก่าค่อย ๆ ย้าย
  แบบ interop)

## 95. เพิ่มสรุปทิ้งท้าย B2 ว่าควรเลือก XML หรือ Compose

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ B2.3 "สรุป: เลือกตัวไหนดี?" สรุปสั้น ๆ
  ว่าโปรเจกต์ใหม่ควรเริ่มด้วย Compose, โปรเจกต์เก่า XML ใช้ต่อและค่อย ๆ ย้าย, และเคสที่
  XML ยังปลอดภัยกว่า (เครื่องต่ำกว่า API 21 / ทีมยังไม่พร้อม)

## 96. เพิ่มแนวโน้มการเลือกใช้ XML vs Compose ในอนาคต

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ B2.4 "แนวโน้มการเลือกใช้ในอนาคต":
  Compose กำลังเป็นค่าเริ่มต้น, XML ยังอยู่ต่อแต่ฟีเจอร์ใหม่มาช้ากว่า, ทักษะตลาดเอียงไป
  Compose, Compose Multiplatform ข้ามแพลตฟอร์ม, และ interop อยู่ร่วมกันได้ พร้อมข้อสรุป
  ว่าทิศทางระยะยาวเอียงไป Compose

## 97. เพิ่มหัวข้อการเลือกใช้ Activity vs Fragment ใน B3

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ B3.1 "เลือกใช้ Activity หรือ Fragment
  ดี?": ตารางเทียบบทบาท (จุดเข้าระบบ/โฮสต์, lifecycle, back stack, เปิดจากภายนอก),
  เกณฑ์ว่าเมื่อไรใช้ Activity เมื่อไรใช้ Fragment, แนวทาง single-activity + Navigation
  Component และหมายเหตุกรณีใช้ Compose ล้วน (Activity เดียวไม่ต้องมี Fragment)

## 98. เพิ่มหัวข้อสถานการณ์ที่ single-activity ใช้ไม่ได้ ใน B3

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ B3.2 "เมื่อไร single-activity ใช้ไม่ได้
  / ไม่เหมาะ": รวมกรณีที่ต้องมีหลาย Activity — จุดเข้าที่ระบบ/แอปอื่นเรียก (deep link/
  share/shortcut), หน้าที่ต้องแยก task/หลายหน้าต่าง, การคืนผลลัพธ์ให้แอปอื่น, flow ที่แยก
  ขาดด้านความปลอดภัย (ล็อกอิน/ชำระเงิน/onboarding), Activity จาก SDK ภายนอก, TWA/กล้อง
  เต็มจอ และแอป legacy/feature module พร้อมสรุปว่าแอปจริงมักเป็นแบบผสม

## 99. แยกกรณี single-activity "ในแอปเราเอง" ใน B3.2

**Added**
- ใน `docs/android-for-programmers.md` (B3.2) เพิ่มส่วนเฉพาะกรณีที่ไม่มีแอปภายนอก/SDK
  เข้ามาเกี่ยว: ระบุว่ากรณีที่ single-activity ทำไม่ได้จริงเหลือแค่เรื่องระดับหน้าต่าง/task
  ของระบบ (หลายหน้าต่างพร้อมกัน/จอเสริม, window-task attribute ต่างกัน, แยก task ใน
  recents, สเกล/feature module หลายทีม) พร้อมรายการที่ "เข้าใจผิดบ่อย" ว่าต้องแยก Activity
  แต่จริง ๆ single-activity ทำได้ (หน้าจอเยอะ, nested nav, dialog, onboarding, ล็อกอิน,
  แท็บ+drawer)

## 100. เพิ่มตัวอย่าง Drawer + Bottom Nav + หน้า detail ใน B3.3

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ B3.3 ตัวอย่างจริง: หน้าหลักมี Navigation
  Drawer + Bottom Navigation แล้วกดปุ่มเปิดหน้า detail ที่มีแค่ toolbar + back ควรใช้
  single-activity — พร้อมโครงสร้าง NavHost, โค้ด `MainActivity` ที่ setup Navigation
  Component (AppBarConfiguration + top-level destinations) และซ่อน bottom nav/ล็อก drawer
  ตาม destination ที่เปลี่ยน พร้อมหมายเหตุว่าเมื่อไรจึงจะพิจารณาแยก Activity

## 101. เพิ่มตารางเทียบ 2 Activity vs single-activity ใน B3.3

**Added**
- ใน `docs/android-for-programmers.md` (B3.3) เพิ่มส่วนตอบคำถาม "แยก 2 Activity ไม่ง่าย
  กว่าเหรอ": ยอมรับว่าดูง่ายกว่าตอนเริ่ม แต่มีตารางเทียบ 7 ประเด็น (ส่งข้อมูลไป/กลับ,
  แชร์ state, transition, back stack, ความสม่ำเสมอ UI, การเพิ่มหน้าใหม่) ที่ single-activity
  ได้เปรียบ พร้อมชี้ว่า logic ซ่อน chrome เขียนครั้งเดียว และสรุปว่าเมื่อไร 2 Activity ก็โอเค
  (หน้า detail น้อย/ไม่ส่งข้อมูลกลับ) กับเมื่อไร single-activity คุ้มกว่า

## 102. เพิ่มหัวข้อ B3.4 Service (งานเบื้องหลัง)

**Added**
- ใน `docs/android-for-programmers.md` เพิ่มหัวข้อ B3.4 "Service (งานเบื้องหลัง)" ขนาด
  กลางใต้ B3: อธิบาย Service 3 แบบ (started/bound/foreground) สั้น ๆ, เน้นว่าส่วนใหญ่ไม่ต้อง
  เขียน Service ตรง ๆ แล้ว พร้อมตาราง "งานแบบนี้ → ใช้อะไร" (WorkManager/coroutine/
  Foreground Service), ข้อจำกัด background ยุคใหม่ และ foreground service type ตั้งแต่
  Android 14

## 103. เพิ่ม Service เฉพาะทาง + กลไกจัดตารางงานใน B3.4

**Added**
- ใน `docs/android-for-programmers.md` (B3.4) เพิ่ม 2 ส่วน: "Service เฉพาะทางที่พบบ่อย"
  (MediaBrowserService/MediaSessionService, TileService, InputMethodService,
  AccessibilityService, NotificationListenerService, VpnService/HostApduService/
  DreamService และหมายเหตุว่า IntentService/JobIntentService deprecated) และ "กลไกจัด
  ตารางงานที่เกี่ยวข้อง" (WorkManager, JobScheduler, AlarmManager)

## 104. สร้างเอกสาร Service ใน Android ฉบับสมบูรณ์

**Added**
- สร้าง `docs/android-services.md` เอกสารเรื่อง Service โดยเฉพาะ ครบถ้วนแบบอ่านจบไม่ต้อง
  หาข้อมูลเพิ่ม 16 หัวข้อ: Service คืออะไร (+เข้าใจผิดบ่อย), ประเภท (started/bound/
  foreground), lifecycle (onStartCommand/START_* flags), โค้ดตัวอย่าง, Foreground Service
  เจาะลึก (+ข้อกำหนดตามเวอร์ชัน/foregroundServiceType), Bound Service (+AIDL/Messenger),
  Service เฉพาะทาง, ทางเลือกยุคใหม่ (WorkManager/coroutine/JobScheduler/AlarmManager),
  decision guide, ข้อดี/ข้อเสีย, **ความแตกต่างตามเวอร์ชัน Android 5→15**, ข้อผิดพลาดที่พบ
  บ่อย, ความปลอดภัย, การทดสอบ, แนวโน้มในอนาคต และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B3.4) เพิ่มลิงก์ชี้ไปเอกสาร Service ฉบับเจาะลึก

## 105. สร้างเอกสาร Lifecycle ใน Android ฉบับสมบูรณ์

**Added**
- สร้าง `docs/android-lifecycle.md` เอกสารเรื่อง Lifecycle โดยเฉพาะ ครบถ้วนแบบอ่านจบไม่ต้อง
  หาข้อมูลเพิ่ม 16 หัวข้อ: Lifecycle คืออะไร, Activity lifecycle, Fragment lifecycle,
  View lifecycle ของ Fragment (viewLifecycleOwner/ViewBinding leak), ViewModel lifecycle
  (viewModelScope/scope), Process death, lifecycle-aware components (repeatOnLifecycle/
  lifecycleScope), Service/Compose/Application lifecycle, การจัดการ state 3 ระดับ
  (ViewModel/SavedStateHandle/DataStore-Room), decision guide, ข้อดี/ข้อเสีย, **ความแตกต่าง
  ตามเวอร์ชัน Android 3→16** (multi-window/multi-resume/predictive back), ข้อผิดพลาดที่พบ
  บ่อย, การทดสอบ (ActivityScenario/process death/LeakCanary), แนวโน้ม และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B5) เพิ่มลิงก์ชี้ไปเอกสาร Lifecycle ฉบับเจาะลึก
- ใน `docs/android-services.md` เพิ่มลิงก์อ่านต่อไปเอกสาร Lifecycle

## 106. สร้างเอกสาร HTML การเชื่อม Google Calendar จาก Android

**Added**
- สร้าง `docs/google-calendar-integration.html` เอกสาร standalone HTML (มีสารบัญด้านข้าง,
  ธีมสี #00d1b2, โค้ด syntax-highlight) เรื่องการเชื่อม Google Calendar จาก Android ครบ
  ทั้ง 3 วิธี: (1) Intent, (2) CalendarContract/CalendarProvider, (3) Google Calendar API
  (OAuth) — พร้อมโค้ดตัวอย่างที่รันได้จริงของแต่ละวิธี (สร้าง/อ่าน/แก้/ลบ event, reminder,
  เชิญ guest, recurring, Google Meet), ตารางเทียบ 3 วิธี, ข้อดี/ข้อเสียแต่ละวิธี, การเลือกใช้,
  สิ่งที่ต้องคำนึงก่อนใช้, ความปลอดภัย, ข้อผิดพลาดที่พบบ่อย, การทดสอบ, แนวโน้มในอนาคต
  (Credential Manager) และ glossary

## 107. เพิ่มหัวข้อสร้าง/แก้/ลบ event แต่ละวิธีในเอกสาร Google Calendar

**Added**
- ใน `docs/google-calendar-integration.html` เพิ่มหัวข้อ "สร้าง / แก้ไข / ลบ ของแต่ละวิธี":
  ตารางสรุปความสามารถ (Create/Update/Delete + สิ่งที่ต้องมีก่อนแก้/ลบ) ของทั้ง 3 วิธี พร้อม
  โค้ดตัวอย่างการแก้ไข/ลบของแต่ละวิธี (Intent ACTION_EDIT/ACTION_VIEW, CalendarContract
  update/delete, Calendar API get→update/delete) โดยแต่ละบล็อกระบุ "สิ่งที่ต้องใช้ + ได้มา
  จากไหน" และมี comment อธิบายโค้ดทีละบรรทัด พร้อมเพิ่มลิงก์ในสารบัญด้านข้าง

## 108. อธิบาย "ล็อกอิน Google" (OAuth) ในเอกสาร Google Calendar

**Added**
- ใน `docs/google-calendar-integration.html` เพิ่มหัวข้อ 3.0 "ล็อกอิน Google ที่ว่า หมายถึง
  แบบไหน": อธิบายว่าเป็น OAuth 2.0 (เลือกบัญชี + ยินยอม scope) ไม่ใช่กรอกรหัสผ่านในแอป,
  flow ที่ผู้ใช้เห็น 3 ขั้น, กล่องเตือนสิ่งที่เข้าใจผิดบ่อย, ตารางแยก Authentication
  (Credential Manager) vs Authorization (AuthorizationClient) และหมายเหตุว่าวิธี 1/2 ไม่มี
  การล็อกอินในแอป พร้อมเพิ่มลิงก์ในสารบัญ

## 109. เพิ่มหัวข้อการตั้งการเตือน (reminder) ทั้ง 3 วิธีในเอกสาร Google Calendar

**Added**
- ใน `docs/google-calendar-integration.html` เพิ่มหัวข้อ "การตั้งการเตือน (reminder) ของ
  ทั้ง 3 วิธี": อธิบายว่าตั้งเตือนได้ทั้ง 3 วิธี, ถ้าไม่กำหนดเองจะใช้ default reminder ของ
  ปฏิทิน (ผู้ใช้ตั้งได้/อาจไม่มี), พร้อมโค้ดแต่ละวิธี — Intent (extra MINUTES),
  CalendarContract (HAS_ALARM=1 + insert Reminders + METHOD_ALERT/EMAIL), Calendar API
  (Event.Reminders + setUseDefault/setOverrides popup/email) และตารางสรุปเทียบการตั้งเตือน
  พร้อมเพิ่มลิงก์ในสารบัญ

## 110. เพิ่มหัวข้อการป้องกัน event ซ้ำในเอกสาร Google Calendar

**Added**
- ใน `docs/google-calendar-integration.html` เพิ่มหัวข้อ "การป้องกัน event ซ้ำ": อธิบายว่า
  Calendar ไม่กันซ้ำให้ + เมื่อไรที่เกิดซ้ำ (กดรัว/retry/หมุนจอ/sync หลายเครื่อง) พร้อมโค้ด
  3 แนวทางที่มี comment อธิบายทีละบรรทัด — (1) query เช็คก่อน insert (วิธี 2), (2) กำหนด
  event.id เอง + ดัก 409 Conflict (วิธี 3, เด็ดขาดสุด), (3) เก็บ mapping id ฝั่งแอปด้วย Room
  แล้ว upsert — และการกันกดซ้ำที่ UI (disable ปุ่ม) พร้อมตารางสรุปเลือกแนวทาง + ลิงก์สารบัญ

## 111. ขยายหัวข้อป้องกัน event ซ้ำ — ตัวเช็ค + event_id + จัดเรียบเรียง

**Added**
- ใน `docs/google-calendar-integration.html` (หัวข้อป้องกัน event ซ้ำ) เพิ่มส่วน "จะใช้อะไร
  เป็นตัวเช็ค" พร้อมตารางเทียบ 3 ตัวเช็ค (title+DTSTART / orderId / event_id) + กล่องอธิบาย
  ว่า event_id ใช้เช็คได้เฉพาะหลังสร้างแล้ว (ไก่กับไข่)
- เพิ่มโค้ด `eventStillExists(eventId)` (วิธี 2) สำหรับเช็คว่า event ที่เคยสร้างยังอยู่ไหม
  พร้อม comment และการรวมกับ mapping

**Changed**
- ขยายกล่องข้อจำกัดของแนวทาง 1 (title+DTSTART เป็น heuristic: false positive/negative,
  ต้องตรง millisecond, race condition) + แนะนำเช็คด้วย orderId (custom field/_SYNC_ID)
- ปรับตารางสรุปเลือกแนวทางให้มีคอลัมน์ "ใช้อะไรเป็นตัวเช็ค" และเพิ่มข้อสรุปเรื่องตัวเช็ค

## 112. เพิ่มหัวข้อกันซ้ำในสถานการณ์จริงในเอกสาร Google Calendar

**Added**
- ใน `docs/google-calendar-integration.html` เพิ่มหัวข้อใหญ่ "การกันซ้ำในสถานการณ์จริง
  (หลายเครื่อง / หลายบัญชี / sync)" รวบองค์ความรู้จากคำถามหลายรอบ: 2 หลักคิด (ปฏิทินอยู่
  cloud, กันซ้ำที่จุดกลาง), เคส sync race หลายเครื่องบัญชีเดียวกัน (พร้อม timeline), เคส
  หลายเครื่องคนละบัญชี, เคสสร้างก่อนล็อกอิน Google, ตารางว่าเคสไหนกันได้ด้วยอะไร, ตาราง
  sync delay, การเลือกปฏิทินปลายทาง (mock dialog + loadCalendars + เก็บ ACCOUNT_NAME),
  เรื่องล็อกอินแอป≠ปฏิทินในเครื่อง (match default), และสถาปัตยกรรม 2 เป้าหมาย (A: วิธี 3 +
  backend server-authoritative + UNIQUE(appUserId,orderId); B: วิธี 2 + backend + mapping
  local) พร้อม reconciliation และตารางสรุปเลือกสถาปัตยกรรม + ลิงก์สารบัญ

## 113. เพิ่มโค้ดเลือกปฏิทินปลายทางครั้งแรก (วิธี 2) ในเอกสาร Google Calendar

**Added**
- ใน `docs/google-calendar-integration.html` เพิ่มหัวข้อ 2.3.1 "เลือกปฏิทินปลายทางครั้งแรก
  (โค้ดเต็ม)" ในวิธีที่ 2 พร้อม comment อธิบายทีละบรรทัด 4 ขั้น: (1) `loadWritableCalendars`
  query ปฏิทินที่เขียนได้ (กรอง CAL_ACCESS_CONTRIBUTOR), (2) `showCalendarPicker` แสดง
  AlertDialog แบบ single-choice + ตั้ง default อัจฉริยะ (match อีเมลแอป → primary → อันแรก)
  + ข้ามถ้ามีปฏิทินเดียว, (3) `saveSelectedCalendar` เก็บ accountName (ไม่ใช่ id), (4)
  `resolveTargetCalendarId` แปลง accountName → CALENDAR_ID ของเครื่องปัจจุบันตอนเขียน

## 114. เพิ่มโค้ดเลือกปฏิทินอัตโนมัติ (วิธี 2) ในเอกสาร Google Calendar

**Added**
- ใน `docs/google-calendar-integration.html` เพิ่มหัวข้อ 2.3.2 "เลือกปฏิทินอัตโนมัติ
  (ไม่ถามผู้ใช้)" พร้อม comment: ลำดับความสำคัญการเลือก (อีเมลแอป → primary → Google ตัวแรก
  → local → สร้างใหม่), `autoPickCalendarId()`, `createLocalCalendar()` สร้าง local calendar
  ของแอปผ่าน sync-adapter URI (กรณีไม่มีปฏิทินเลย), และ `ensureCalendarId()` รวมทุกอย่าง
  (ใช้ที่เก็บไว้ → เลือกอัตโนมัติ → สร้างใหม่) พร้อมข้อแนะนำ auto-default + เปลี่ยนได้ในตั้งค่า

## 115. สร้างเอกสาร HTML เรื่อง Lifecycle ฉบับสมบูรณ์

**Added**
- สร้าง `docs/android-lifecycle.html` เอกสาร standalone HTML (sidebar sticky, ธีม #00d1b2,
  โค้ด syntax-highlight, diagram box) เรื่อง Lifecycle ครบ 16 หัวข้อ: Lifecycle คืออะไร,
  Activity/Fragment lifecycle (พร้อม diagram + ตาราง callback), View lifecycle ของ Fragment
  (viewLifecycleOwner/ViewBinding/repeatOnLifecycle), ViewModel lifecycle, Process death,
  lifecycle-aware components, Service/Compose/Application lifecycle, การจัดการ state 3 ระดับ,
  decision guide, ข้อดี/ข้อเสีย, ความแตกต่างตามเวอร์ชัน Android 3→16, ข้อผิดพลาดที่พบบ่อย,
  การทดสอบ (ActivityScenario/LeakCanary), แนวโน้ม และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B5) เพิ่มลิงก์ไปฉบับ HTML

## 116. สร้างเอกสาร HTML เรื่องสถาปัตยกรรมที่แนะนำ

**Added**
- สร้าง `docs/android-architecture.html` เอกสาร standalone HTML (sidebar, ธีม #00d1b2,
  โค้ด syntax-highlight, diagram) เรื่อง App Architecture ครบ 16 หัวข้อ: ทำไมต้องมี
  สถาปัตยกรรม, หลักการ (SSOT/UDF/separation), การแบ่งชั้น (UI/Domain/Data + dependency rule),
  UI Layer (ViewModel/UI state), Domain Layer (Use Case), Data Layer (Repository/DataSource),
  UDF, รูปแบบ MVVM/MVI/MVP/MVC (+ MVVM vs MVI), DI (Hilt/Dagger/Koin), Modularization,
  ตัวอย่างโค้ดเต็มทุกชั้น, ข้อดี/ข้อเสีย, การเลือกใช้ตามขนาดโปรเจกต์, ข้อผิดพลาดที่พบบ่อย,
  แนวโน้ม (Compose+MVI/KMP) และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B4) เพิ่มลิงก์ไปฉบับ HTML

## 117. สร้างเอกสาร HTML เรื่อง Clean Architecture + เทียบกับ Google Guide

**Added**
- สร้าง `docs/android-clean-architecture.html` เอกสาร standalone HTML ครบ 16 หัวข้อ: Clean
  Architecture คืออะไร, **Dependency Rule** (หัวใจ + diagram วงกลมซ้อน), 4 ชั้น (Entities/
  UseCases/Interface Adapters/Frameworks), แต่ละชั้นพร้อมโค้ด, การแมปลงโครง Android, ตัวอย่าง
  โค้ดเต็มไล่ทุกชั้น, โครงสร้างแพ็กเกจ/โมดูล (multi/single), ข้อดี/ข้อเสีย, **หัวข้อเปรียบเทียบ
  Clean Architecture vs Google Guide** (ตารางเทียบ + การแมป + ความสัมพันธ์), การเลือกใช้,
  ข้อผิดพลาดที่พบบ่อย, แนวโน้ม (KMP) และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B4) และ `docs/android-architecture.html` เพิ่มลิงก์
  ไปเอกสาร Clean Architecture

## 118. ทำการเปรียบเทียบ Clean vs Google Guide ให้เห็นภาพง่ายขึ้น

**Added**
- ใน `docs/android-clean-architecture.html` (หัวข้อ 12) เพิ่มส่วนอธิบายเข้าใจง่าย: อุปมา
  "การจัดบ้าน" (3 โซน vs 4 ชั้น), ตาราง "แก่นความต่างจริง ๆ มีแค่ 3 อย่าง" (กี่ชั้น/Use Case
  บังคับ/ความเข้ม) + กล่องว่านอกนั้นเหมือนกันหมด, diagram สเปกตรัมความเข้ม (Google Guide →
  +Domain/UseCase → Clean = "อันเดียวกันคนละความเข้ม"), การจับคู่ชั้น และกฎเลือกใช้ง่าย ๆ
  โดยคงตารางเทียบละเอียดเดิมไว้ด้านล่าง

## 119. เพิ่มการเทียบ Clean vs Google Guide ด้วยโค้ดจริง + สถานการณ์ใช้งาน

**Added**
- ใน `docs/android-clean-architecture.html` (หัวข้อ 12) เพิ่มการเปรียบเทียบเชิงปฏิบัติ:
  โค้ดฟีเจอร์เดียวกัน ("กดสั่งอาหาร + ยอดขั้นต่ำ 100 บาท") เขียน 2 แบบ (Google Guide 2 ไฟล์
  logic ใน ViewModel / Clean 4 ไฟล์ logic ใน Entity+UseCase) + ตารางชี้ความต่าง + 3
  สถานการณ์ใช้งานจริง (logic ใช้ซ้ำ 2 ที่, การเขียน test, แอปเล็กที่ไม่ใช้ซ้ำ) พร้อมจุด
  ตัดสินใจง่าย ๆ (logic ใช้ซ้ำ/ซับซ้อน/เทสต์หนัก → Clean, ไม่ → Google Guide)

## 120. เพิ่มภาคปฏิบัติ Clean Architecture step-by-step ในเอกสาร

**Added**
- ใน `docs/android-clean-architecture.html` เพิ่มหัวข้อ 9.1 "ภาคปฏิบัติ: สร้างฟีเจอร์จริง
  step-by-step" เดินสร้างฟีเจอร์ "หน้าแสดงเมนูอาหาร (API + cache DB)" ตั้งแต่ศูนย์ 9 ขั้น:
  วางโครงโฟลเดอร์, Entity, Repository interface, UseCase (กรอง/เรียง), Data sources (DTO/
  Retrofit/Room entity/DAO), Mapper 3 ชั้น (+เหตุผลว่าทำไมต้องแยกโมเดล), RepositoryImpl
  (SSOT/cache pattern), Presentation (UiState/ViewModel + error handling ข้ามชั้นด้วย
  toUiMessage), DI, และ testing แต่ละชั้น (UseCase pure + Repository mock) พร้อม diagram
  สรุป flow ทั้งหมด + เพิ่มลิงก์สารบัญ

## 121. สร้างเอกสาร HTML เรื่องการจัดการ State และข้อมูล

**Added**
- สร้าง `docs/android-state-data.html` เอกสาร standalone HTML ครบ 16 หัวข้อ ทั้งภาคทฤษฎีและ
  ภาคปฏิบัติ: State คืออะไร, ประเภท state (UI element/screen/app), state holders (ViewModel/
  SavedStateHandle/remember), StateFlow vs LiveData, state vs one-shot event (Channel),
  ระดับการรอด (config change/process death/ถาวร), DataStore vs Room, ข้อมูลเครือข่าย, SSOT+UDF,
  decision guide (เก็บที่ไหน), ข้อดี/ข้อเสียแต่ละตัว, **ภาคปฏิบัติสร้างหน้า "ค้นหาเมนู" จริง**
  (SavedStateHandle+StateFlow+debounce+Channel+DataStore ครบทุกประเภท), ข้อผิดพลาดที่พบบ่อย,
  การทดสอบ (Turbine/coroutines-test), แนวโน้ม และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B6) เพิ่มลิงก์ไปฉบับ HTML

## 122. สร้างเอกสาร HTML เรื่อง Navigation

**Added**
- สร้าง `docs/android-navigation.html` เอกสาร standalone HTML ครบ 16 หัวข้อ ทฤษฎี+ปฏิบัติ:
  Navigation คืออะไร, แนวคิดหลัก (graph/host/controller), back stack (popUpTo/inclusive/
  launchSingleTop), Safe Args, single-activity, XML vs Navigation-Compose, deep link, nested
  graph + bottom nav, การเลือกใช้, ข้อดี/ข้อเสีย, **ภาคปฏิบัติสร้างจริง step-by-step ทั้ง XML**
  (dependency→graph→NavHost→NavController→navigate/navArgs) **และ Compose** (NavHost/composable/
  route), ข้อผิดพลาดที่พบบ่อย, การทดสอบ (TestNavHostController), แนวโน้ม (type-safe route/
  predictive back) และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B7) เพิ่มลิงก์ไปฉบับ HTML

## 123. สร้างเอกสาร HTML เรื่อง Permissions & ความปลอดภัย

**Added**
- สร้าง `docs/android-permissions-security.html` เอกสาร standalone HTML ครบ 17 หัวข้อ ทฤษฎี+
  ปฏิบัติ: Permission คืออะไร, ประเภท (normal/dangerous/signature/special), runtime permission
  flow (Activity Result API + rationale + ปฏิเสธถาวร), special permissions, ตำแหน่ง (fine/
  coarse/background/approximate), Scoped Storage (Photo Picker), เก็บข้อมูลปลอดภัย
  (EncryptedSharedPreferences/Keystore), ความปลอดภัยเครือข่าย (HTTPS/Network Security Config/
  cert pinning), ป้องกัน component (exported), ความปลอดภัยอื่น (Biometric/Play Integrity/
  FLAG_SECURE), **ตารางเทียบตามเวอร์ชัน Android 6→16**, ข้อดี/ข้อเสีย, การเลือกใช้, ภาคปฏิบัติ
  ขอ permission กล้องครบ flow, ข้อผิดพลาดที่พบบ่อย, แนวโน้ม และ glossary

**Changed**
- ใน `docs/android-for-programmers.md` (B8) เพิ่มลิงก์ไปฉบับ HTML

## 124. เพิ่มโค้ด permission ครบทุกกลุ่มในภาคปฏิบัติ (Permissions doc)

**Added**
- ใน `docs/android-permissions-security.html` (ภาคปฏิบัติ) เพิ่มหัวข้อ 14.1 "permission อื่น ๆ
  ส่วนใหญ่เหมือนกัน แค่เปลี่ยนชื่อ" (ตารางรายการ dangerous permission ที่ใช้ flow เดียวกับ
  กล้อง: ไมค์/ผู้ติดต่อ/ปฏิทิน/โทร/SMS/เซ็นเซอร์) และ 14.2 "กรณีที่ต้องเขียนต่าง" พร้อมโค้ด
  + comment ชี้จุดที่ต่าง: หลาย permission (RequestMultiplePermissions/Map), background
  location (ขอ 2 สเต็ป), special permission (เช็คเมธอดเฉพาะ + Settings intent + ตารางแต่ละตัว),
  ไฟล์/รูป (Photo Picker ไม่ต้องขอ), POST_NOTIFICATIONS (เช็ค API 33+) + ตารางสรุปว่าต่างตรงไหน

## 125. สร้างเอกสาร HTML เรื่อง Secure Coding เชิงลึก

**Added**
- สร้าง `docs/android-secure-coding.html` เอกสาร standalone HTML ครบ 17 หัวข้อ เน้นความ
  ปลอดภัยเชิงการเขียนโค้ด พร้อมโค้ด ผิด/ถูก ทุกหัวข้อ: secure coding คืออะไร, โมเดลภัยคุกคาม
  (attack surface), input validation & injection (SQL/path traversal), WebView security,
  Intent security (redirection/explicit), deep link & App Links, IPC/Broadcast/Provider,
  PendingIntent (IMMUTABLE), secret & API key management, เก็บข้อมูล & backup rules, logging
  & debuggable/R8, anti-tamper/root (Play Integrity), dependency/supply chain,
  clipboard/screenshot/FLAG_SECURE, security checklist ก่อนปล่อย production, แนวโน้ม และ glossary

**Changed**
- ใน `docs/android-permissions-security.html` และ `docs/android-for-programmers.md` (B8)
  เพิ่มลิงก์ชี้ไปเอกสาร secure coding

## 126. เพิ่มตัวอย่างภัยจริงในตารางโมเดลภัยคุกคาม (Secure Coding doc)

**Changed**
- ใน `docs/android-secure-coding.html` (หัวข้อ 2 โมเดลภัยคุกคาม) เพิ่มคอลัมน์ "ตัวอย่างภัยจริง"
  ให้แต่ละ attack surface เห็นภาพว่าเกิดอะไรขึ้นจริง (เช่น SQL injection ลบตาราง, intent
  redirection เปลี่ยนอีเมลเหยื่อ, WebView bridge อ่าน token, HTTP บน WiFi สาธารณะโดนแก้ยอด,
  decompile อ่าน secret/patch bypass การจ่ายเงิน) + เพิ่มแถว clipboard/หน้าจอ

## 127. เพิ่มตารางค่าดีฟอลต์ WebSettings ในหัวข้อ WebView (Secure Coding doc)

**Added**
- ใน `docs/android-secure-coding.html` (หัวข้อ 4 WebView) เพิ่มหัวข้อ 4.1 "ค่าดีฟอลต์ของ
  WebSettings" ตารางค่าเริ่มต้นของแต่ละ setting + ระบุว่าปลอดภัยไหม (javaScriptEnabled=false,
  addJavascriptInterface ไม่มี, allowFileAccess ต่างตามเวอร์ชัน, mixedContentMode=NEVER_ALLOW
  ฯลฯ) พร้อมกล่องเตือนเรื่อง allowFileAccess ที่ดีฟอลต์ต่างกัน API ≤29 vs 30+ และข้อสรุปว่า
  ช่องโหว่ส่วนใหญ่มาจากการเปิดค่าเพิ่มเอง ควรตั้งค่าความปลอดภัยชัดเจน

## 128. เพิ่มหัวข้อสิ่งที่ควรตั้งทุกครั้งของ WebView (Secure Coding doc)

**Added**
- ใน `docs/android-secure-coding.html` (หัวข้อ 4 WebView) เพิ่มหัวข้อ 4.2 "สิ่งที่ควรตั้ง
  ทุกครั้ง" แบ่ง 3 กลุ่มพร้อมเหตุผล: กลุ่ม 1 ควรตั้งทุกครั้ง (allowFileAccess/allowContentAccess/
  fileURLs = false เพื่อ explicit + กันดีฟอลต์ต่างเวอร์ชัน), กลุ่ม 2 ต้องมีเมื่อโหลด content
  ภายนอก (จำกัด domain ผ่าน shouldOverrideUrlLoading), กลุ่ม 3 เปิดเฉพาะเมื่อจำเป็น (JS/
  domStorage/addJavascriptInterface + ข้อควรระวัง) พร้อมเทมเพลต setupSecureWebView() ที่นำไปใช้ได้

## 129. สร้างเอกสาร HTML เรื่องการเก็บ Key & Secret

**Added**
- สร้าง `docs/android-secrets-management.html` เอกสาร standalone HTML ครบ 17 หัวข้อ เจาะลึก
  การเก็บ key/secret: กฎเหล็ก (APK ถอดได้เสมอ), จำแนกประเภท secret ตามความเสียหาย/restrict ได้,
  ตารางที่เก็บทั้งหมด (hardcode→backend เรียงตามความปลอดภัย), ที่ห้ามเก็บเด็ดขาด, Android
  Keystore, EncryptedSharedPreferences, backend proxy (diagram), token ผู้ใช้, **ตารางเจาะจง
  ว่าแต่ละ key เก็บที่ไหน** (Google Maps/Firebase/Facebook App ID vs Secret/Stripe pk vs sk/
  OAuth/DB credential), การ restrict key (package+SHA-1), BuildConfig/gradle/git, ข้อดี/ข้อเสีย
  แต่ละที่, ขั้นตอนเมื่อ key รั่ว, checklist, แนวโน้ม (BFF/short-lived token/secret scanning)
  และ glossary

**Changed**
- ใน `docs/android-secure-coding.html` (หัวข้อ 9) เพิ่มลิงก์ไปเอกสารการเก็บ secret

## 130. เพิ่มส่วน "ตอบเร็ว + วิธีเก็บจริง" ในเอกสาร Secrets Management

**Added**
- ใน `docs/android-secrets-management.html` เพิ่ม 2 หัวข้อไว้ต้นเอกสาร (หลังกฎเหล็ก) เพื่อให้
  เห็นภาพชัดว่าควรเก็บที่ไหน: "⚡ ตอบเร็ว (ฟันธง)" — ตารางแมป "ของที่มี → เก็บที่ไหน → วิธี"
  พร้อมวิธีคิด 10 วินาที (2 คำถาม); และ "🔧 วิธีเก็บจริง (โค้ดก๊อปได้)" 5 วิธี: A) token →
  EncryptedSharedPreferences (SecureStorage class เต็ม), B) ค่าฝังได้ → local.properties→
  BuildConfig, C) restrict key ใน Cloud Console (package+SHA-1), D) secret สำคัญ → backend
  proxy (diagram + Retrofit), E) Keystore + เพิ่มลิงก์สารบัญ

## สิ่งที่ยังค้าง / แผนถัดไป (Backlog)

- GitHub Actions (CI) build + test + validate Gradle wrapper อัตโนมัติทุก PR
- (ตัวเลือก) เปลี่ยนไปใช้ assertion library เช่น Truth เพื่อ error message ที่อ่านง่ายขึ้น
- เพิ่ม loading/error state ใน `OrderUiState` เมื่อเปลี่ยนไปใช้ข้อมูลจริง
