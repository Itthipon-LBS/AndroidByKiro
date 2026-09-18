# Android สำหรับโปรแกรมเมอร์ (ฉบับสรุปสั้น)

สรุป **เฉพาะเรื่องสำคัญ** ที่โปรแกรมเมอร์ควรรู้ก่อนพัฒนาแอป Android — สั้น กระชับ เข้าใจง่าย

**สารบัญ**
- ส่วน A — [Native vs Cross-Platform](#a-native-vs-cross-platform)
- ส่วน B — [พื้นฐาน Android Native (ภาพรวม)](#b-พื้นฐาน-android-native-ภาพรวม)

---

# A. Native vs Cross-Platform

## A1. สองแนวทางคืออะไร

**Native — เขียนเจาะจงแต่ละแพลตฟอร์ม**
- Android: Kotlin (หรือ Java) + Android SDK
- iOS: Swift + iOS SDK
- แยกโค้ด 2 ชุด

**Cross-Platform — เขียนครั้งเดียว รันหลายแพลตฟอร์ม**
- **Flutter** (Dart) — วาด UI เองด้วย engine, ลื่น, นิยมมาก
- **React Native** (JS/TS) — map เป็น native view จริง (มี bridge)
- **Kotlin Multiplatform (KMP)** — แชร์ logic ด้วย Kotlin, UI native ได้
- **.NET MAUI** (C#) — สาย Microsoft

## A2. ตารางเปรียบเทียบภาพรวม

| ประเด็น | Native | Cross-Platform |
|---------|--------|----------------|
| โค้ด | 2 ชุด (Android/iOS) | ชุดเดียวใช้หลายที่ |
| ประสิทธิภาพ | สูงสุด | ดี (พอสำหรับแอปทั่วไป) |
| เข้าถึงฟีเจอร์เครื่อง/API ใหม่ | ได้ทันที | รอ plugin/bridge |
| ความเร็วพัฒนา | ช้ากว่า | เร็วกว่า |
| ต้นทุน/ทีม | ต้องมีสาย Android + iOS | ทีมเล็กกว่าได้ |
| UI ตรงตามแพลตฟอร์ม | เป๊ะสุด | ใกล้เคียง |
| ขนาดแอป/startup | เล็ก/เร็ว | ใหญ่/ช้ากว่า (แบก engine) |
| การอัปเดต | ต้องส่งสโตร์ | มี OTA/hot update บางส่วน |
| ความยั่งยืนระยะยาว | OS vendor ดูแล | ขึ้นกับผู้ดูแล framework |

## A3. ข้อดี / ข้อเสีย (สรุป)

**Native** — ✅ ประสิทธิภาพสูงสุด, เข้าถึง API ใหม่ก่อน, UX เป๊ะ, debug ลึก
&nbsp;&nbsp;❌ เขียน 2 ชุด, ต้นทุน/เวลา/ทีมมากกว่า

**Cross-Platform** — ✅ โค้ดชุดเดียว, พัฒนาเร็ว, ทีมเล็กได้, UI สม่ำเสมอ
&nbsp;&nbsp;❌ ประสิทธิภาพ/ฟีเจอร์ลึกสู้ native ไม่ได้บางเคส, พึ่ง framework/plugin, ไฟล์ใหญ่

## A3.1 ข้อดี / ข้อเสีย เจาะลึก — มุมนักพัฒนา

**Native — มุมนักพัฒนา**

✅ ข้อดี
- **เครื่องมือทางการครบ** — Android Studio/Xcode, profiler, debugger, emulator, เอกสารทางการ
- **เข้าถึง API/ฮาร์ดแวร์ใหม่ทันที** — ไม่ต้องรอใครมาห่อให้ (กล้อง, เซ็นเซอร์, AR, NFC)
- **debug ตรงจุด** — stack trace ชัด ไม่ต้องข้ามชั้น bridge/engine
- **คุม performance ระดับ low-level** — จัดการ thread/memory/rendering ได้ละเอียด
- **ทำตาม behavior change ของ OS ได้ตรง** — ปรับ target SDK/permission ได้เอง

❌ ข้อเสีย
- **เขียน logic ซ้ำ 2 ฝั่ง** (Android + iOS) → แก้ 2 ที่, เสี่ยงพฤติกรรมไม่ตรงกัน
- **ต้องมีทักษะ 2 สาย** (Kotlin + Swift) หรือ 2 ทีม
- **build config ซับซ้อน** (Gradle/signing) สำหรับมือใหม่
- **ออกฟีเจอร์ช้ากว่า** เพราะทำทีละแพลตฟอร์ม

**Cross-Platform — มุมนักพัฒนา**

✅ ข้อดี
- **โค้ดชุดเดียวออกได้หลายแพลตฟอร์ม** → เขียน/แก้ครั้งเดียว, logic ไม่ซ้ำ
- **พัฒนาเร็ว** — hot reload, ทีมเล็กทำได้, จ้างง่ายถ้าทีมมีสายนั้น (web → React Native)
- **UI สม่ำเสมอข้ามเครื่อง** (โดยเฉพาะ Flutter ที่วาดเอง)
- **OTA/hot update บางส่วน** — แก้ JS/Dart โดยไม่ต้องส่งสโตร์ใหม่ (ภายใต้เงื่อนไขสโตร์)

❌ ข้อเสีย
- **ต้องเขียน native module เอง** เมื่อ plugin ไม่รองรับ → เสียเวลาที่ตั้งใจจะประหยัด
- **debug ข้าม 2 ชั้น** (JS/Dart + native) ยากกว่า, native crash หา stack ยาก
- **dependency/framework ตกรุ่นบ่อย** — อัปเวอร์ชันแล้วเจ็บ (RN โดยเฉพาะ)
- **ฟีเจอร์ OS ใหม่มาช้า** — ต้องรอ framework/plugin อัปเดต
- **ยังต้องเข้าใจความต่างของแต่ละแพลตฟอร์มอยู่ดี** เมื่อเจอ platform-specific bug
- **ความยั่งยืนขึ้นกับผู้ดูแล framework** — ถ้าถูกลดบทบาท/เลิกพัฒนา = ความเสี่ยงระยะยาว

## A3.2 ข้อดี / ข้อเสีย เจาะลึก — มุมผู้ใช้งาน

**Native — มุมผู้ใช้**

✅ ข้อดี
- **ลื่น/ตอบสนองไว** — โดยเฉพาะจอหนัก ๆ, list ยาว, animation
- **รู้สึก "เป็นของระบบ"** — gesture, keyboard, animation ตรงตามแพลตฟอร์ม
- **ไฟล์เล็ก โหลด/เปิดเร็ว** (ไม่แบก engine)
- **ฟีเจอร์ OS ใหม่มาถึงเร็ว** (dark mode, widget, ระบบแชร์)
- **accessibility & integration เนียนกว่า** (TalkBack, shortcut, widget)

❌ ข้อเสีย
- **แอปอาจออกไม่พร้อมกัน 2 แพลตฟอร์ม** — ฝั่งหนึ่งได้ก่อน อีกฝั่งรอ (ทีมทำทีละฝั่ง)
- ฟีเจอร์บางอย่างอาจ **ต่างกันเล็กน้อยระหว่าง Android/iOS** (คนละทีมทำ)

**Cross-Platform — มุมผู้ใช้**

✅ ข้อดี
- **ได้ใช้ทั้ง 2 แพลตฟอร์มพร้อมกัน** และมักได้อัปเดตแก้บั๊กเร็ว (OTA)
- **หน้าตา/ฟีเจอร์เหมือนกันทั้ง Android/iOS** — ประสบการณ์สม่ำเสมอ

❌ ข้อเสีย
- **ไฟล์แอปใหญ่กว่า** → กินพื้นที่, โหลดนาน
- **เปิดครั้งแรกช้ากว่า** (warm-up engine/runtime)
- บางแอป **รู้สึก "ไม่เหมือนของระบบ"** — animation/gesture/keyboard เพี้ยนเล็กน้อย
- **สะดุด/แลค** ในหน้าจอหนักหรือ list ยาว (ถ้า optimize ไม่ดี)
- **ฟีเจอร์ใหม่ของ OS มาช้ากว่า** (รอ framework)
- **accessibility อาจไม่ครบเท่า native**, การผสานระบบ (share/widget/shortcut) บางทีไม่เนียน
- **เปลืองแบตกว่า** ในบางกรณี

> สรุป: **Native ชนะเรื่องคุณภาพ/ความลึกทั้งฝั่งนักพัฒนาและผู้ใช้ แต่แพงและช้ากว่า** •
> **Cross-Platform ชนะเรื่องความเร็ว/ต้นทุน/ความสม่ำเสมอ แต่แลกด้วยการพึ่ง framework,
> ไฟล์ใหญ่ และงานลึกบางอย่าง** — เลือกตามขนาดทีม, งบ, ความซับซ้อนของแอป และแผนระยะยาว

## A4. เลือกอย่างไร

- **Native** — ต้องการประสิทธิภาพสูงสุด, ใช้ฮาร์ดแวร์/API ใหม่หนัก (กล้อง/AR/เซ็นเซอร์/กราฟิก), UX ต้องเป๊ะ
- **Cross-Platform** — อยากออก Android+iOS เร็ว/ประหยัด, แอปทั่วไป (ธุรกิจ/ฟอร์ม/CRUD), ทีมเล็ก
- **KMP (สายกลาง)** — แชร์ logic แต่ทำ UI native แต่ละฝั่ง

## A4.1 แนวโน้มข้อดี/ข้อเสียในอนาคต

ทั้ง 2 ฝั่งกำลังพัฒนาเพื่อ **ลดจุดอ่อนของตัวเอง** ทำให้เส้นแบ่งเบลอลงเรื่อย ๆ

**Native กำลังไปทางไหน**
- **UI แบบ declarative เป็นมาตรฐาน** — Jetpack Compose (Android) + SwiftUI (iOS) โตขึ้น
  → เขียน UI เร็วขึ้น **ช่องว่างเรื่อง "พัฒนาช้า" แคบลง**
- **KMP โตแรง (Google หนุนทางการ)** — แชร์ logic ข้าม Android/iOS แต่ยังทำ UI native
  → ได้ความเร็วแบบ cross-platform + คุณภาพแบบ native
- **AI ช่วยเขียนโค้ด** — ลดภาระ "เขียน 2 ชุด" ของ native (สร้างโค้ดคู่ขนานเร็วขึ้น)
- **อุปกรณ์รูปแบบใหม่** (พับได้/XR/Auto/Wear) — native ยังได้เปรียบเข้าถึงฟีเจอร์ใหม่ก่อน

**Cross-Platform กำลังไปทางไหน**
- **engine เร็วขึ้น** — Flutter → Impeller (แทน Skia), React Native → New Architecture
  (JSI/Fabric แทน bridge เดิม) → **ลดคอขวดเรื่องปุ่ม/latency** ที่เคยเป็นจุดอ่อน
- **ช่องว่าง performance แคบลง** — ฮาร์ดแวร์แรงขึ้น + engine ดีขึ้น แอปทั่วไปแทบไม่ต่าง
- **ยังตามหลังเรื่องฟีเจอร์ OS ใหม่** — ต้องรอ plugin/bridge (ข้อเสียนี้ไม่หายง่าย)
- **ความเสี่ยงเรื่องผู้ดูแล** — ขึ้นกับบริษัทแม่ (Google/Meta) ยังเป็นปัจจัยระยะยาว

**ภาพรวม: อนาคตเป็น "สเปกตรัม" ไม่ใช่เลือกข้างเด็ดขาด**

| ระดับการแชร์โค้ด | ตัวอย่าง | แชร์อะไร |
|------------------|----------|----------|
| Native เต็มตัว | Kotlin + Swift | ไม่แชร์ |
| KMP | Kotlin Multiplatform | แชร์ logic, UI native |
| Compose Multiplatform | Compose ข้ามแพลตฟอร์ม | แชร์ logic + UI |
| Flutter / React Native | Dart / JS | แชร์เกือบทั้งหมด |

> **แนวโน้มสำคัญ:** AI + tooling ทำให้ต้นทุนทำ native หลายแพลตฟอร์มถูกลง → จุดขาย
> "ประหยัดเวลา/ต้นทุน" ของ cross-platform **ได้เปรียบน้อยลง**; ส่วน cross-platform ก็
> **ไล่ปิดช่องว่าง performance** ได้เรื่อย ๆ — สุดท้ายการเลือกจะขึ้นกับ **ทีมถนัดอะไร +
> ความต้องการเฉพาะของแอป** มากกว่าข้อจำกัดทางเทคนิคล้วน ๆ

## A5. ปัญหาเชิงเทคนิค: Native vs Cross-Platform

ตารางนี้เทียบ **ปัญหาเดียวกันว่าแต่ละแนวทางเจอมาก/น้อยและเพราะอะไร** (หลายปัญหาเจอได้ทั้ง
คู่ แต่ cross-platform มักเจอบ่อยกว่า/แก้ยากกว่าเพราะมีชั้น bridge/engine + ต้องดูแล 2 ฝั่ง)

| ปัญหา | Native | Cross-Platform |
|-------|--------|----------------|
| **กดปุ่มไม่ติด (event หลุดชั่วคราว)** | เจอน้อย (touch ระบบเดียว) | เจอบ่อยกว่า (bridge/JS thread หน่วง) |
| **ปุ่มค้างถาวรจนรีสตาร์ท** | เจอได้ (state/loading ค้าง) | เจอได้ + debug ยากกว่า (ข้ามชั้น) |
| **Main thread บล็อก / ANR** | เจอได้ | เจอได้ (งานหนักบน JS/UI thread) |
| **Memory leak** | เจอได้ | เจอบ่อยกว่า (refs ข้าม bridge/JS) |
| **List ยาวกระตุก (jank)** | คุม low-level ได้ | ขึ้นกับ engine/การ optimize |
| **รูปกินแรม (OOM)** | คุม decode/cache เองได้ | ต้องพึ่ง image library |
| **Navigation/back stack เพี้ยน** | ระบบ native ตรง | ข้ามชั้น ซับซ้อนกว่า |
| **Keyboard บัง input** | จัดการตรง | ต้องเผื่อ 2 แพลตฟอร์ม |
| **State หายตอน kill/rotate** | SavedStateHandle | ต้อง persist/restore เอง |
| **Deep link / push ผิดหน้า** | API ตรง | ผ่าน plugin |
| **Platform-specific bug** | เขียนเจาะจงอยู่แล้ว | โค้ดเดียวแต่พฤติกรรมต่าง 2 ฝั่ง |
| **Race condition (async ซ้อน)** | structured concurrency (coroutine) | ต้องจัดการเอง |
| **Debug native crash** | stack ชัด | ข้าม JS/Dart + native → ยาก |
| **Dependency/เวอร์ชันพัง** | Gradle/SPM | framework + plugin ตกรุ่นบ่อย |
| **เปลืองแบต/ร้อน** | คุม background ละเอียด | re-render/engine เพิ่ม overhead |
| **ฟีเจอร์ OS ใหม่มาช้า** | ได้ทันที | รอ framework อัปเดต |
| **Accessibility / integration ระบบ** | เนียน | อาจไม่ครบเท่า |

> **ธีมร่วมของเกือบทุกปัญหา:** อย่าบล็อก main/UI thread • จัดการ state/async ให้ครบทุกกรณี
> • ทดสอบบนอุปกรณ์จริงทั้ง Android + iOS (cross-platform ไม่ได้แปลว่าไม่ต้องทดสอบ 2 ที่)

## A6. เจาะลึก: อาการ "กดปุ่มไม่ทำงาน" (พบบ่อย)

### กรณีที่ 1 — กดบ้างไม่กดบ้าง (event หลุดชั่วคราว)

**สาเหตุ:** bridge/JS thread ยุ่ง, main thread บล็อก, touch/gesture ชนกัน, hit area/overlay
บัง, กดซ้ำระหว่าง disable, async ไม่แสดง loading

**แก้:** ย้ายงานหนักออก UI thread, ใส่ loading + debounce, จัดการ gesture/`hitSlop`,
ให้ปุ่มมี feedback ทันที (ripple/opacity)

### กรณีที่ 2 — ปุ่มตายถาวรจนต้องรีสตาร์ท (stuck state)

ปุ่มตายสนิท หายเมื่อรีสตาร์ทเท่านั้น = แอปติดใน **สถานะพังค้าง** ที่ถูกล้างตอนเปิดใหม่

**สาเหตุ (เรียงตามโอกาส):**
1. **Loading/async ค้าง** (พบมากสุด) — ตั้ง `isLoading=true` แต่งานค้าง/พังเงียบ/ไม่มี
   timeout → ไม่เคยตั้งกลับ `false` → ปุ่ม disable ถาวร
2. Main thread บล็อกถาวร (deadlock/loop) — มักเลื่อนจอไม่ได้ด้วย
3. State ไม่รีเซ็ต (overlay/gesture ค้าง)
4. Exception ถูก catch เงียบ
5. Memory/resource leak (เป็นหลังใช้นาน ๆ)

**จุดสังเกต:** เลื่อนจอไม่ได้เลย → ข้อ 2 • เลื่อนได้แต่บางปุ่มตาย → ข้อ 1/3/4 •
เกิดหลัง action ที่พึ่งเน็ต → ข้อ 1

**วิธีแก้ตรงจุด (ข้อ 1): รีเซ็ต state ใน `finally` เสมอ + ใส่ timeout**
```kotlin
fun onPlaceOrderClick() {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            withTimeout(15_000) { repository.placeOrder() }   // กันงานค้าง
        } catch (e: Exception) {
            _error.value = e.message                          // ไม่กลืน error เงียบ
        } finally {
            _isLoading.value = false                          // ปลดล็อกปุ่มเสมอ
        }
    }
}
```
> หลักกันพลาด: อย่าตั้ง state กลับ "เฉพาะตอนสำเร็จ", ห้ามบล็อก main thread, อย่า catch เงียบ

---

# B. พื้นฐาน Android Native (ภาพรวม)

ภาพรวมสิ่งที่ประกอบกันเป็นแอป Android — เพื่อให้เห็นว่าแพลตฟอร์มนี้มีอะไรบ้าง

## B1. สแตกและภาษา
- **ภาษา:** **Kotlin** (ภาษาทางการที่แนะนำ), ยังรองรับ **Java** (โปรเจกต์เก่าจำนวนมาก)
- **SDK/แพลตฟอร์ม:** Android SDK + Jetpack (ชุดไลบรารีทางการของ Google)
- **Build system:** **Gradle** (Groovy หรือ Kotlin DSL) + Version Catalog สำหรับจัดการ
  เวอร์ชัน dependency รวมศูนย์
- **รูปแบบแพ็กเกจ:** APK (ไฟล์ติดตั้ง) และ **AAB (Android App Bundle)** ที่ Play ใช้ส่งแอป

## B2. สองแนวทางเขียน UI
Android มี 2 แนวทางหลัก (เลือกได้ตามทีม/โปรเจกต์):

| | XML + View | Jetpack Compose |
|---|-----------|-----------------|
| รูปแบบ | เขียน layout เป็น XML แยกจากโค้ด | เขียน UI เป็น Kotlin แบบ declarative |
| อายุ | ดั้งเดิม, โปรเจกต์เก่าส่วนใหญ่ใช้ | แนวทางใหม่ที่ Google ผลักดัน |
| ผูกค่ากับ view | `findViewById` / **ViewBinding** | ไม่มี view object แยก (state → UI) |
| จุดเด่น | ทรัพยากร/ตัวอย่างเยอะ | โค้ดสั้น, จัดการ state ตรงไปตรงมา |

### B2.1 ข้อดี / ข้อเสีย ของแต่ละแนวทาง

**XML + View**

✅ ข้อดี
- **สุกงอม/มีมานาน** — ตัวอย่าง, ไลบรารี, คำตอบ Stack Overflow เยอะมาก
- **ทีม/โปรเจกต์เก่าคุ้นเคย** — โค้ดเบสส่วนใหญ่ในโลกยังเป็น XML
- **แยก UI ออกจาก logic ชัดเจน** — ดีไซเนอร์/นักพัฒนาแก้ layout ได้โดยไม่ยุ่งโค้ด
- **Layout Editor** — ลากวางดูตัวอย่างได้ทันที

❌ ข้อเสีย
- **โค้ดเยอะ (boilerplate)** — ต้องผูก view, sync state กับ UI เอง
- **เสี่ยงพลาดเรื่อง view/lifecycle** — เช่น ViewBinding ค้าง = memory leak
- **อัปเดต UI ตาม state ด้วยมือ** — พลาดง่ายเมื่อ state ซับซ้อน
- **สองไฟล์ต่อหน้าจอ** (XML + โค้ด) ต้องดูแลควบคู่

**Jetpack Compose**

✅ ข้อดี
- **declarative** — บอกว่า "UI ควรเป็นอย่างไรตาม state" ระบบวาดให้เอง ลดบั๊ก sync
- **โค้ดสั้น/รวมที่เดียว** — UI + logic อยู่ใน Kotlin ไฟล์เดียว
- **จัดการ state ตรงไปตรงมา** — state เปลี่ยน → UI อัปเดตอัตโนมัติ (recomposition)
- **ทิศทางที่ Google ผลักดัน** — ฟีเจอร์/ไลบรารีใหม่ ๆ ออกมาให้ Compose ก่อน
- **preview/animation ทำง่ายกว่า**

❌ ข้อเสีย
- **ยังใหม่กว่า** — ตัวอย่างเก่าบางเรื่องน้อยกว่า, บาง library ยังตาม
- **ต้องเข้าใจแนวคิดใหม่** — recomposition, state hoisting, `remember` (มือใหม่งงช่วงแรก)
- **เสี่ยง performance ถ้าเขียนผิด** — recomposition เกินจำเป็นถ้าไม่ระวัง
- **ต้องใช้ Kotlin เท่านั้น** (ไม่มีสำหรับ Java)

### B2.2 สิ่งที่ต้องคำนึงก่อนเลือกใช้

- **โค้ดเบสเดิม** — โปรเจกต์เก่าที่เป็น XML อยู่แล้ว การรื้อทั้งหมดมีต้นทุน (ทั้งสองอยู่
  ร่วมกันได้ ค่อย ๆ ย้ายทีละหน้าจอผ่าน interop ได้)
- **ทักษะทีม** — ทีมถนัด XML อยู่แล้วต้องเผื่อเวลาเรียนรู้ Compose
- **ความใหม่ของโปรเจกต์** — เริ่มใหม่ (greenfield) เลือก Compose ได้เลยเพราะเป็นทิศทางหลัก
- **min SDK** — Compose รองรับตั้งแต่ API 21 (Android 5.0) ขึ้นไป; ถ้าต้องซัพพอร์ตต่ำกว่านั้น XML ปลอดภัยกว่า
- **ความซับซ้อน UI** — จอที่ state เปลี่ยนบ่อย/ซับซ้อน Compose จัดการง่ายกว่า; จอ static
  หรือ layout ที่มีตัวอย่างสำเร็จเยอะ XML ก็ยังสะดวก
- **library ที่ต้องใช้** — เช็คว่ามี Compose version ครบไหม บาง SDK ภายนอกยังให้แต่ View
- **ระยะยาว** — ฟีเจอร์ใหม่ของ Google ออกให้ Compose ก่อน ระยะยาวมีแนวโน้มเป็นค่าเริ่มต้น

> **แนวทางปฏิบัติ:** โปรเจกต์ใหม่ → เริ่มด้วย **Compose** • โปรเจกต์เก่า XML → อยู่ต่อได้
> และค่อย ๆ ย้ายทีละส่วนแบบ interop โดยไม่ต้องรื้อทั้งหมดในครั้งเดียว

## B3. องค์ประกอบระบบหลัก (4 App Components)
Android สร้างแอปจาก "องค์ประกอบ" ที่ระบบรู้จักและเรียกใช้ได้:
- **Activity** — หนึ่งหน้าจอ/จุดเข้าใช้งาน UI
- **Service** — งานเบื้องหลังที่ไม่มี UI (เช่น เล่นเพลง, ซิงก์ข้อมูล)
- **BroadcastReceiver** — รับ event จากระบบ/แอปอื่น (เช่น แบตต่ำ, บูตเครื่องเสร็จ)
- **ContentProvider** — แชร์ข้อมูลข้ามแอปอย่างมีการควบคุม

องค์ประกอบเสริมที่พบบ่อย: **Fragment** (ส่วน UI ย่อยใน Activity), **Intent**
(ข้อความสั่งให้เปิด component/ส่งข้อมูลระหว่างกัน), **AndroidManifest.xml**
(ประกาศ component, permission, ข้อมูลแอป)

## B4. สถาปัตยกรรมที่แนะนำ
- **แยกชั้น (layered):** UI layer → Domain (ตัวเลือก) → Data layer
- **รูปแบบยอดนิยม:** **MVVM** (Model-View-ViewModel) และ **MVI** (state ก้อนเดียว)
- **หลักการ:** UI สังเกต state จาก ViewModel; ViewModel คุย Repository; Repository
  เป็นแหล่งความจริงเดียวที่รวมข้อมูลจาก DB/network
- **ทิศทางข้อมูล:** `UI ← observe ← ViewModel ← Repository ← (DB / Network)`

## B5. Lifecycle (จุดพลาดบ่อย)
```
onCreate → onStart → onResume → (ใช้งาน) → onPause → onStop → onDestroy
```
- **หมุนจอ/เปลี่ยน config = สร้าง Activity/Fragment ใหม่** → ตัวแปรที่ถือไว้ตรง ๆ หาย
- **แก้:** เก็บ state ที่ต้องรอดใน **ViewModel** (รอด config change) +
  **SavedStateHandle** (รอด process death เมื่อระบบ kill แอป)
- **Fragment มี 2 lifecycle:** ตัว Fragment และ view ของมัน — observe ข้อมูลด้วย
  `viewLifecycleOwner` และเคลียร์ ViewBinding ใน `onDestroyView` เพื่อกัน memory leak

## B6. การจัดการ State และข้อมูล
- **UI state:** เปิดจาก ViewModel เป็น **StateFlow** (หรือ LiveData) แบบ read-only
- **One-shot event** (toast/navigate): ใช้ **Channel/SharedFlow** ไม่ใช่ state ค้าง
- **Async:** **Coroutines + Flow** (มาตรฐานปัจจุบันของ Kotlin/Android)
- **ข้อมูลในเครื่อง:** **DataStore** (คู่ key-value/preference), **Room** (ฐานข้อมูล SQLite
  แบบ type-safe)
- **ข้อมูลจากเครือข่าย:** **Retrofit** หรือ **Ktor** (+ OkHttp) คู่กับ coroutines
- **Dependency Injection:** **Hilt/Dagger** (ทางการ) หรือ **Koin** — ลดการผูกกันแน่น

## B7. Navigation
- **Navigation Component** (Jetpack) — จัดการการย้ายหน้าและ back stack, มี Safe Args
  ส่งข้อมูลแบบ type-safe
- **single-activity architecture** — ใช้ Activity เดียวเป็นโฮสต์ แล้วสลับ Fragment/
  Compose destination ข้างใน เป็นแนวทางที่นิยม

## B8. Permissions & ความปลอดภัย
- ขอ **runtime permission** ตอนใช้งานจริง (กล้อง/ตำแหน่ง ฯลฯ) — ผู้ใช้ปฏิเสธได้ ต้องเผื่อ
- ขอ **เท่าที่จำเป็น** + อธิบายเหตุผล; ตำแหน่งเลือก "ขณะใช้แอป"/approximate เมื่อพอ
- **Scoped Storage** — เข้าถึงไฟล์แบบจำกัดขอบเขต (ตั้งแต่ Android 10)
- อย่าเก็บ **ข้อมูลลับ** (token/key) แบบ plain text; ใช้ **HTTPS** และที่จัดเก็บที่ปลอดภัย
  (EncryptedSharedPreferences/Keystore)
- ระวังข้อมูลอ่อนไหวตอนทำ **Auto Backup**

## B9. การทดสอบ (Testing)
- **Unit test (JVM):** JUnit + MockK/Mockito + coroutines-test — เร็ว ไม่ต้องใช้อุปกรณ์
- **UI/Instrumented test:** **Espresso** (View) / **Compose UI test** — รันบน emulator/
  เครื่องจริง
- แนวคิด **test pyramid:** unit เยอะสุด → integration → UI น้อยสุด

## B10. เครื่องมือและการเผยแพร่
- **Android Studio** — IDE หลัก (มาพร้อม SDK, emulator, profiler, layout inspector)
- **Emulator / เครื่องจริง** — ทดสอบแอป
- **Gradle** — build system • **Git** — version control
- **การเผยแพร่:** เซ็นแอป (signing) → สร้าง **AAB** → อัปโหลดขึ้น **Google Play Console**
  (มี track ทดสอบภายใน/ปิด/เปิด ก่อนปล่อย production); ต้องทำตามเส้นตาย **target API** ของ Play

---

## อ่านต่อ
- ประวัติเวอร์ชัน + ศัพท์เทคนิค: `docs/android-versions.md`
- การทำ test: `docs/android-testing-guide.md`
