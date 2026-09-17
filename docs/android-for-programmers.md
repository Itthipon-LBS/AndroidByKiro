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

## หัวข้อถัดไป (จะเพิ่มภายหลัง)

- โครงสร้างโปรเจกต์ Android
- Lifecycle ของ Activity/Fragment
- การจัดการ state และ data
- การทำ test (ดู `docs/android-testing-guide.md`)
- ความปลอดภัย & permissions

> เอกสารนี้เน้นเฉพาะเรื่องสำคัญ — รายละเอียดเวอร์ชัน/ศัพท์เทคนิคดูที่ `docs/android-versions.md`
