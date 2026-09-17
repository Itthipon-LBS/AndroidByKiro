# Android สำหรับโปรแกรมเมอร์ (ฉบับสรุปสั้น)

เอกสารนี้สรุป **เฉพาะเรื่องสำคัญ** ที่โปรแกรมเมอร์ควรรู้ก่อนพัฒนาแอป Android — สั้น กระชับ
เข้าใจง่าย

---

## 1. Native vs Cross-Platform: เลือกทางไหน

สิ่งแรกที่ต้องตัดสินใจคือจะเขียนแอปแบบไหน

### Native (เขียนเจาะจงแต่ละแพลตฟอร์ม)
- **Android:** Kotlin (หรือ Java) + Android SDK
- **iOS:** Swift + iOS SDK
- แยกโค้ด 2 ชุด (Android/iOS) เขียนคนละที

### Cross-Platform (เขียนครั้งเดียว รันหลายแพลตฟอร์ม)
- **Flutter** (ภาษา Dart) — วาด UI เอง, ลื่น, นิยมมาก
- **React Native** (JavaScript/TypeScript) — ใช้ React, เหมาะทีมสาย web
- **Kotlin Multiplatform (KMP)** — แชร์ logic ด้วย Kotlin, ทำ UI native ได้ (Compose Multiplatform)
- **.NET MAUI** (C#) — สาย Microsoft

### เทียบให้เห็นภาพ

| ประเด็น | Native | Cross-Platform |
|---------|--------|----------------|
| โค้ด | 2 ชุด (Android/iOS) | ชุดเดียวใช้หลายที่ |
| ประสิทธิภาพ | สูงสุด | ดี (ส่วนใหญ่พอสำหรับแอปทั่วไป) |
| เข้าถึงฟีเจอร์เครื่อง | ครบ/เร็วที่สุด (API ใหม่มาทันที) | ต้องรอ plugin/bridge บางที |
| ความเร็วในการพัฒนา | ช้ากว่า (ทำ 2 แพลตฟอร์ม) | เร็วกว่า (เขียนครั้งเดียว) |
| ต้นทุน/ทีม | ต้องมีคนสาย Android + iOS | ทีมเล็กกว่าได้ |
| UI ตรงตามแพลตฟอร์ม | เป๊ะที่สุด | ใกล้เคียง (Flutter วาดเอง) |

### เลือกอย่างไร (สรุปตัดสินใจ)
- **เลือก Native เมื่อ:** ต้องการประสิทธิภาพสูงสุด, ใช้ฮาร์ดแวร์/API ใหม่หนัก (กล้อง, AR,
  เซ็นเซอร์, กราฟิก), UX ต้องเป๊ะตามแพลตฟอร์ม
- **เลือก Cross-Platform เมื่อ:** อยากออกทั้ง Android+iOS เร็ว/ประหยัด, แอปทั่วไป
  (ธุรกิจ/ฟอร์ม/CRUD), ทีมเล็ก
- **ทางสายกลาง:** **KMP** แชร์ logic (network/business) แต่ทำ UI native แต่ละฝั่ง

### เจาะข้อแตกต่างที่ชัดเจน (เชิงเทคนิค)

- **การรัน UI:**
  - Native — ใช้ UI toolkit ของแพลตฟอร์มตรง ๆ (Android View/Compose, iOS UIKit/SwiftUI)
  - Flutter — **วาด UI เองด้วย engine (Skia/Impeller)** ไม่ใช้ native widget → หน้าตา
    เหมือนกันทุกเครื่อง แต่ไม่ได้ใช้ของระบบ
  - React Native — เขียน JS แล้ว **map ไปเป็น native view จริง** (มี bridge/JSI คั่น)
- **การเข้าถึงฮาร์ดแวร์/API ใหม่:**
  - Native — ได้ทันทีที่ Google/Apple ปล่อย
  - Cross-Platform — ต้อง **รอ plugin/bridge** หรือเขียน native module เอง
- **ขนาดแอป:** Native มักเล็กกว่า; Cross-Platform แบก runtime/engine → ไฟล์ใหญ่ขึ้น
- **การอัปเดต OS:** Native ปรับตาม behavior change ได้ตรง; Cross-Platform ต้องรอ
  framework/plugin อัปเดตตาม

### ข้อดี / ข้อเสีย

**Native**
- ✅ ประสิทธิภาพสูงสุด, เข้าถึง API/ฮาร์ดแวร์ใหม่ได้ก่อน, UX เป๊ะตามแพลตฟอร์ม, เครื่องมือ
  /เอกสารครบ, debug/profile ลึก
- ❌ ต้องเขียน 2 ชุด (Android/iOS) → ต้นทุน/เวลา/ทีมมากกว่า, logic ซ้ำซ้อน 2 ฝั่ง

**Cross-Platform**
- ✅ โค้ดชุดเดียวออกได้หลายแพลตฟอร์ม, พัฒนาเร็ว, ทีมเล็กได้, UI สม่ำเสมอ (โดยเฉพาะ Flutter)
- ❌ ประสิทธิภาพ/ฟีเจอร์ลึกอาจสู้ native ไม่ได้, พึ่ง framework/plugin (ถ้าเลิก maintain
  ลำบาก), ไฟล์ใหญ่, บางเคสต้องเขียน native เสริมอยู่ดี

### ปัญหาที่พบบ่อย — ฝั่งโปรแกรมเมอร์

**Native**
- โค้ด logic **ซ้ำ 2 ฝั่ง** ต้องแก้ 2 ที่ (Android + iOS) → เพี้ยนกันได้
- ตาม behavior change ของ OS ใหม่ทุกปี (target SDK, permission เข้มขึ้น)
- Gradle/build config ซับซ้อนสำหรับมือใหม่

**Cross-Platform**
- **ต้องเขียน native module เอง** เมื่อ plugin ไม่รองรับฟีเจอร์ที่ต้องการ (เสียเวลาที่ตั้งใจจะประหยัด)
- **dependency พัง/ตกรุ่น** เมื่อ framework อัปเกรด (React Native อัปเวอร์ชันแล้วเจ็บบ่อย)
- debug ข้าม 2 ชั้น (JS/Dart + native) ยากกว่า
- ปัญหาเฉพาะแพลตฟอร์มยังโผล่ (ต้องทดสอบทั้ง Android+iOS อยู่ดี)

### ปัญหาที่พบบ่อย — ฝั่งผู้ใช้งานแอป

**Native** — ปัญหาน้อยด้าน UX/ประสิทธิภาพ แต่แอปอาจ**ออกช้าไม่พร้อมกัน** 2 แพลตฟอร์ม
(ทีมทำทีละฝั่ง)

**Cross-Platform** (สิ่งที่ผู้ใช้อาจรู้สึกได้)
- **ไฟล์แอปใหญ่กว่า** (กินพื้นที่/โหลดนาน)
- บางแอปรู้สึก **"ไม่เหมือนของระบบ"** (animation/gesture/keyboard เพี้ยนเล็กน้อย)
- **สะดุด/แลค** ในหน้าจอหนัก ๆ หรือ list ยาว (ขึ้นกับการทำ optimize)
- ฟีเจอร์ใหม่ของ OS **มาช้ากว่า** (รอ framework อัปเดต)
- ใช้แบตมากกว่าในบางกรณี

### มุมเพิ่มเติม (ธุรกิจ/ทีม/ระยะยาว)

- **การจ้างงาน/ทีม:** Native ต้องมีคน Kotlin + Swift (2 สาย); Cross-Platform ใช้คนชุดเดียว
  (Dart/JS/C#) — จ้างง่ายถ้าทีมมีสายนั้นอยู่แล้ว (เช่น web → React Native)
- **เวลาออกตลาด & อัปเดต:** Cross-Platform ออก 2 แพลตฟอร์มพร้อมกัน + RN/Flutter มี
  **OTA/hot update บางส่วน** (แก้ JS/Dart โดยไม่ต้องส่งสโตร์ใหม่ ภายใต้เงื่อนไขนโยบายสโตร์);
  Native ทุกการแก้ต้องส่งสโตร์และรออนุมัติ
- **Ecosystem/ไลบรารี:** Native ครบ/ทางการ/อายุยืน; Cross-Platform พึ่ง plugin ชุมชน —
  คุณภาพไม่สม่ำเสมอ, **plugin ร้างได้**
- **Learning curve:** Native เอกสารแน่นแต่ต้องเรียน 2 แพลตฟอร์ม; Cross-Platform เรียน
  กรอบเดียว แต่สุดท้ายต้องเข้าใจความต่างของแต่ละแพลตฟอร์มอยู่ดีเมื่อเจอปัญหา
- **การทดสอบ:** Native มีเครื่องมือทางการ (JUnit/Espresso, XCTest); Cross-Platform มี
  test framework ของตัวเอง แต่ยังต้องทดสอบบนอุปกรณ์จริงทั้ง 2 ฝั่ง
- **ความยั่งยืน (longevity):** Native ดูแลโดย OS vendor อยู่ยาวแน่นอน; Cross-Platform
  **ขึ้นกับผู้ดูแล framework** → มีความเสี่ยงระยะยาวถ้าถูกลดบทบาท/เลิกพัฒนา
- **รูปแบบอุปกรณ์:** Native รองรับจอพับ/แท็บเล็ต/Wear/TV/Auto ตรงและเร็ว; Cross-Platform
  ฟอร์มแฟกเตอร์แปลก ๆ อาจช้า/ไม่ครบ
- **ต้นทุนระยะยาว:** Cross-Platform เริ่มถูก/เร็ว แต่ถ้าแอปโตและต้องเขียน native module
  เยอะ **ต้นทุนอาจไล่ทันหรือแซง native**

### มุมผู้ใช้เพิ่มเติม

- Cross-Platform: เปิดครั้งแรกอาจช้ากว่า (warm-up engine), **accessibility อาจไม่ครบ
  เท่า native**, การผสานกับระบบ (share, widget, shortcut) บางทีไม่เนียนเท่า
- Native: a11y และ integration กับระบบมักเนียนกว่า, ฟีเจอร์ใหม่ของ OS มาถึงผู้ใช้เร็วกว่า

> สรุป: **Native ชนะเรื่องคุณภาพ/ความลึก แต่แพงและช้ากว่า** • **Cross-Platform ชนะเรื่อง
> ความเร็ว/ต้นทุน แต่แลกด้วยการพึ่ง framework และงานลึกบางอย่าง** — เลือกตาม **ขนาดทีม,
> งบ, ความซับซ้อนของแอป และแผนระยะยาว**

> โปรเจกต์ **FoodOrder** ในที่นี้เป็น **Native Android (Kotlin + MVVM + XML)**

---

## 2. พื้นฐาน Android Native ที่ต้องรู้ (สั้น ๆ)

- **ภาษา:** Kotlin (ทางการ), รองรับ Java
- **UI 2 แนวทาง:**
  - **XML + View** (ดั้งเดิม, ViewBinding) — โปรเจกต์นี้ใช้แบบนี้
  - **Jetpack Compose** (UI แบบ declarative, แนวทางใหม่ที่แนะนำ)
- **สถาปัตยกรรมแนะนำ:** **MVVM** (+ Repository) — แยก UI / logic / data
- **องค์ประกอบหลัก:** Activity, Fragment, ViewModel, Navigation
- **Async:** Coroutines + Flow
- **Build:** Gradle (Kotlin DSL) + Version Catalog
- **min/target/compile SDK:** กำหนดช่วงเวอร์ชันที่รองรับ (ดู `docs/android-versions.md`)

---

## 3. เครื่องมือ

- **Android Studio** — IDE หลัก (มาพร้อม SDK, emulator, profiler)
- **Emulator / เครื่องจริง** — ทดสอบแอป
- **Gradle** — build system
- **Git** — version control

---

## 4. โครงสร้างโปรเจกต์ Android (สั้น ๆ)

```
app/
├── build.gradle.kts          ตั้งค่า build ของโมดูล app
└── src/main/
    ├── AndroidManifest.xml    ประกาศ Activity, permission, ไอคอน
    ├── java/<package>/        โค้ด Kotlin (แบ่งเป็น data / ui / di ...)
    └── res/                   ทรัพยากร (ไม่ใช่โค้ด)
        ├── layout/            ไฟล์ UI (XML)
        ├── values/            strings, colors, themes
        ├── drawable/          ไอคอน/รูป vector
        └── navigation/        nav_graph.xml
```

- **แยกโค้ด (`java/`) ออกจากทรัพยากร (`res/`)** เสมอ
- จัดแพ็กเกจตามชั้น MVVM: `data`, `ui`, `di` (ดูโครงจริงใน `README.md`)

## 5. Lifecycle (วงจรชีวิต) — เรื่องที่พลาดบ่อย

**Activity/Fragment มีวงจรชีวิต** ที่ระบบเรียกตามสถานะ:
```
onCreate → onStart → onResume → (ใช้งาน) → onPause → onStop → onDestroy
```
- **หมุนจอ/เปลี่ยน config = สร้างใหม่** → ตัวแปรใน Activity/Fragment หาย
- **ทางแก้:** เก็บ state ที่ต้องรอดใน **ViewModel** (รอด config change) และ
  **SavedStateHandle** (รอด process death)
- **Fragment มี 2 lifecycle:** ตัว Fragment กับ **view ของมัน** (`viewLifecycleOwner`) —
  observe ข้อมูลด้วย `viewLifecycleOwner` และเคลียร์ ViewBinding ใน `onDestroyView`

## 6. การจัดการ State และ Data

- **UI state:** เก็บใน ViewModel เปิดเป็น **`StateFlow`** (หรือ LiveData) แบบ read-only
- **One-shot event** (toast/navigate): ใช้ **`Channel`/`SharedFlow`** ไม่ใช่ state
- **ข้อมูลถาวรในเครื่อง:**
  - **DataStore** — เก็บค่า preference/คู่ key-value (แทน SharedPreferences)
  - **Room** — ฐานข้อมูล SQLite แบบมี type-safe
- **ข้อมูลจากเน็ต:** **Retrofit** (REST) หรือ **Ktor** + coroutines
- **หลักการ:** UI ← observe ← ViewModel ← Repository ← (DB/Network)

## 7. Permissions & ความปลอดภัย (สั้น ๆ)

- ขอ **runtime permission** ตอนใช้งานจริง (กล้อง, ตำแหน่ง ฯลฯ) — ผู้ใช้ปฏิเสธได้ ต้องเผื่อ
- ขอ **เท่าที่จำเป็น** + อธิบายเหตุผล; ตำแหน่งใช้ "ขณะใช้แอป"/approximate เมื่อพอ
- อย่าเก็บ **ข้อมูลลับ** (token/key) แบบ plain text; ใช้ที่จัดเก็บที่ปลอดภัยและ HTTPS
- ระวังข้อมูลอ่อนไหวตอนทำ Auto Backup (ดู `docs/android-versions.md`)

## 8. อ่านต่อ

- **ประวัติเวอร์ชัน + ศัพท์เทคนิค:** `docs/android-versions.md`
- **การทำ test:** `docs/android-testing-guide.md`
- **โครงสร้างโปรเจกต์จริง (MVVM):** `README.md`

> เอกสารนี้เน้นเฉพาะเรื่องสำคัญ ให้เห็นภาพรวมเร็ว — เจาะลึกแต่ละเรื่องดูเอกสารที่อ้างถึง
