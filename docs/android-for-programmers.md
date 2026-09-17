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
