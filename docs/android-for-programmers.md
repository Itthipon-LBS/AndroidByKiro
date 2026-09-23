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

### B2.3 สรุป: เลือกตัวไหนดี?

- **เริ่มโปรเจกต์ใหม่ →** เลือก **Jetpack Compose** เป็นค่าเริ่มต้น เพราะเป็นทิศทางหลักของ
  Google โค้ดสั้นและจัดการ state ง่ายกว่า
- **มีโค้ดเบส XML อยู่แล้ว →** ใช้ **XML ต่อไป** ได้ ไม่ต้องรีบรื้อ ค่อย ๆ ย้ายทีละหน้าจอ
  แบบ interop เมื่อพร้อม
- **ต้องรองรับเครื่องเก่ามาก (ต่ำกว่า API 21) หรือทีมยังไม่พร้อมเรียนของใหม่ →** **XML**
  ปลอดภัยกว่าในตอนนี้

> โดยรวม: **มือใหม่/โปรเจกต์ใหม่เริ่มที่ Compose** ได้เลย ส่วนงานที่มีของเดิมอยู่แล้ว
> ไม่จำเป็นต้องเปลี่ยนทันที

### B2.4 แนวโน้มการเลือกใช้ในอนาคต

- **Compose กำลังกลายเป็นค่าเริ่มต้น** — Google แนะนำ Compose สำหรับโปรเจกต์ใหม่, เอกสาร/
  โค้ดตัวอย่างทางการ, template ใน Android Studio และฟีเจอร์ใหม่ ๆ มุ่งไปที่ Compose ก่อน
- **XML ยังไม่หายไปในเร็ว ๆ นี้** — โค้ดเบสจำนวนมหาศาลในโลกยังเป็น XML และยังได้รับการ
  ดูแล (maintenance) ต่อ แต่ **ฟีเจอร์ใหม่จะมาช้ากว่า** และตัวอย่างใหม่ ๆ จะเน้น Compose
- **ทักษะที่ตลาดต้องการเปลี่ยนทิศ** — ตำแหน่งงานใหม่และไลบรารีสมัยใหม่มองหา Compose
  มากขึ้น การรู้ Compose จึงคุ้มค่าในระยะยาว
- **Compose ขยายข้ามแพลตฟอร์ม** — **Compose Multiplatform** (ผ่าน Kotlin Multiplatform)
  ทำให้ UI แบบ Compose ใช้ข้าม Android/iOS/Desktop/Web ได้ → เพิ่มน้ำหนักให้ Compose
  ในระยะยาว
- **ทั้งสองอยู่ร่วมกันได้** — interop ระหว่าง View กับ Compose ยังถูกดูแลต่อ ทีมจึงย้ายแบบ
  ค่อยเป็นค่อยไปได้ ไม่ต้องเลือกแบบสุดขั้ว

> **แนวโน้มโดยรวม:** ทิศทางระยะยาวเอียงไป **Compose** ชัดเจน — ถ้าเริ่มเรียนหรือเริ่ม
> โปรเจกต์ใหม่ตอนนี้ ลงทุนกับ Compose คุ้มกว่า ส่วน XML ยังเป็นทักษะที่จำเป็นสำหรับดูแล
> โค้ดเบสเดิมไปอีกหลายปี

## B3. องค์ประกอบระบบหลัก (4 App Components)
Android สร้างแอปจาก "องค์ประกอบ" ที่ระบบรู้จักและเรียกใช้ได้:
- **Activity** — หนึ่งหน้าจอ/จุดเข้าใช้งาน UI
- **Service** — งานเบื้องหลังที่ไม่มี UI (เช่น เล่นเพลง, ซิงก์ข้อมูล)
- **BroadcastReceiver** — รับ event จากระบบ/แอปอื่น (เช่น แบตต่ำ, บูตเครื่องเสร็จ)
- **ContentProvider** — แชร์ข้อมูลข้ามแอปอย่างมีการควบคุม

องค์ประกอบเสริมที่พบบ่อย: **Fragment** (ส่วน UI ย่อยใน Activity), **Intent**
(ข้อความสั่งให้เปิด component/ส่งข้อมูลระหว่างกัน), **AndroidManifest.xml**
(ประกาศ component, permission, ข้อมูลแอป)

### B3.1 เลือกใช้ Activity หรือ Fragment ดี?

ทั้งคู่เป็น "หน้าจอ/ส่วน UI" แต่บทบาทต่างกัน:

| | Activity | Fragment |
|---|----------|----------|
| คืออะไร | จุดเข้าใช้งานที่ **ระบบ** รู้จัก (มีใน Manifest) | ส่วน UI ย่อยที่ **ต้องอยู่ในโฮสต์** (Activity/Fragment อื่น) |
| ใช้เอง | ใช้ลำพังได้ | ต้องมี Activity โฮสต์เสมอ |
| lifecycle | ชุดเดียว | มี 2 ชั้น (Fragment + view ของมัน) |
| back stack | ระบบจัดการผ่าน task | จัดการเองด้วย FragmentManager/Navigation |
| เปิดจากภายนอก | ได้ (deep link, share, launcher) | ไม่ได้ตรง ๆ ต้องผ่าน Activity |

**ใช้ Activity เมื่อ**
- เป็น **จุดเข้าแอป** (หน้า launcher, หน้าที่เปิดจาก deep link/notification/แชร์)
- ต้องประกาศให้ระบบรู้จัก (เช่น ต้องรับ Intent จากแอปอื่น)
- หน้าจอที่แยกขาดจริง ๆ และต้องการ task/back stack ระดับระบบ

**ใช้ Fragment เมื่อ**
- เป็น **หน้าจอทั่วไปภายในแอป** ที่สลับไปมาด้วย Navigation
- อยากแชร์/นำ UI กลับมาใช้ซ้ำ หรือทำ **layout ตอบสนองหลายขนาดจอ** (เช่น master-detail
  บนแท็บเล็ต: 2 Fragment ในจอเดียว)
- ต้องการให้ ViewModel ผูกกับช่วงชีวิตของหน้าจอย่อยได้ยืดหยุ่น

**แนวทางที่นิยมปัจจุบัน — single-activity**
- ใช้ **Activity เดียว** เป็นโฮสต์ แล้วทำแต่ละหน้าจอเป็น **Fragment** (หรือ Compose
  destination) สลับด้วย **Navigation Component**
- ข้อดี: back stack/การส่งข้อมูลเป็นระบบเดียว, แชร์ ViewModel ระดับ graph ได้, transition
  ระหว่างหน้าลื่นกว่า

> **สรุปสั้น:** ใช้ **Activity ให้น้อย** (เท่าที่จำเป็นเป็นจุดเข้า) แล้วทำหน้าจอส่วนใหญ่เป็น
> **Fragment** ภายใต้ single-activity • ถ้าใช้ **Compose** ล้วน อาจเหลือ Activity เดียว
> โดยไม่ต้องใช้ Fragment เลย (ใช้ composable + Navigation-Compose แทน)

### B3.2 เมื่อไร single-activity ใช้ไม่ได้ / ไม่เหมาะ

single-activity เป็นแนวทางที่ดีสำหรับ "ในแอปเดียว" แต่ **มีหลายกรณีที่ต้องมีหลาย Activity**
เพราะ Activity คือหน่วยที่ระบบรู้จัก บางอย่างจึง "ต้องเป็น Activity เท่านั้น":

- **จุดเข้าแยกที่แอปอื่น/ระบบเรียก** — แต่ละ entry point ที่ประกาศใน Manifest ต้องเป็น
  Activity เช่น
  - เปิดจาก **deep link / App Link** หลายเส้นทาง
  - รับ **Intent จากแอปอื่น** (share sheet, `ACTION_VIEW`, `ACTION_SEND`)
  - **ทางลัด (shortcut)** / รายการใน launcher ที่พาไปหน้าจอเฉพาะ
- **หน้าที่ต้องแยก task/หน้าต่างจริง** — เช่นเปิดใน task แยก, ใช้ `launchMode`/
  `taskAffinity`, หรือแสดงแบบ **หลายหน้าต่าง/free-form/split-screen** พร้อมกัน
- **ต้องคืนผลลัพธ์ให้แอปอื่น** — เช่นเป็น picker ที่แอปอื่นเรียกแล้วรอ `setResult` กลับ
- **flow ที่แยกขาดจริง ๆ ด้านความปลอดภัย/บริบท** — เช่นหน้า **ล็อกอิน/ยืนยันตัวตน**,
  หน้าชำระเงินของ SDK ภายนอก, หรือ **onboarding** ที่ต้องแยก back stack ชัดเจน
- **ฝัง Activity ของ library/SDK** — หลาย SDK (แผนที่, กล้อง, chrome custom tabs,
  โฆษณา, payment) มาเป็น Activity ของตัวเอง เรียกใช้ตรง ๆ ผ่าน Intent
- **หน้าจอพิเศษที่ต้องเป็น Activity** — เช่น **Trusted Web Activity**, หน้า AR/กล้อง
  เต็มจอบางแบบ, หน้าที่ต้องปรับ window flag ระดับ Activity
- **แอปเก่า/โมดูลใหญ่** — legacy ที่เป็น multi-activity อยู่แล้ว หรือแยก **feature module**
  ที่ต่างทีมดูแล การรวมเป็น Activity เดียวได้ไม่คุ้ม

> **สรุป:** single-activity คุมได้แค่ "การนำทางภายในแอปของเราเอง" — แต่ **จุดที่ระบบหรือ
> แอปอื่นเข้ามา, การแยก task/หน้าต่าง, การคืนผลลัพธ์ และ Activity จาก SDK ภายนอก ยังต้อง
> เป็นหลาย Activity** • ในทางปฏิบัติแอปจริงมักเป็นแบบ **ผสม**: Activity หลักหนึ่งตัวสำหรับ
> flow หลัก + Activity เพิ่มเฉพาะจุดเข้า/กรณีพิเศษข้างต้น

**เฉพาะกรณี "ในแอปเราเอง" (ไม่มีแอปภายนอก/SDK เข้ามาเกี่ยว)**

ถ้าตัดเรื่องแอปนอกออก กรณีที่ single-activity **ทำไม่ได้จริง** เหลือน้อยมาก และส่วนใหญ่
เป็นเรื่อง "ระดับหน้าต่าง/task ของระบบ" ที่ Fragment ทำแทนไม่ได้:

- **ต้องแสดงหลายหน้าต่างพร้อมกันจริง ๆ** — เช่น 2 หน้าต่างแยกใน split-screen/free-form
  หรือบน **จอเสริม (external display)** ที่แต่ละหน้าต่างเป็นคนละบริบท (1 Activity = 1 หน้าต่าง)
- **หน้าจอที่ต้องการ window/task attribute ต่างกันชัด** — เช่นล็อก orientation เฉพาะหน้า,
  `windowSoftInputMode`/ธีม/window flag หรือ `launchMode`/`taskAffinity` ที่ต่างจากส่วนอื่น
  มาก → จัดการระดับ Activity ตรงและสะอาดกว่า
- **อยากให้ back stack แยกเป็นคนละ task** — เช่นให้หน้าจอกลุ่มหนึ่งโผล่แยกใน recents เป็น
  task ของตัวเอง (พฤติกรรมระดับ Activity/task, Fragment แทนไม่ได้)
- **แอปโตมาก / แยก feature module หลายทีม** — เชิงเทคนิคยังทำ single-activity ได้ แต่การ
  แชร์ Activity เดียวทำให้ผูกกันแน่น/บิลด์ช้า หลายทีมจึงให้แต่ละ feature มี Activity เอง
  (นี่คือ **"ไม่คุ้ม" มากกว่า "ทำไม่ได้"**)

**เข้าใจผิดบ่อย — พวกนี้ single-activity ทำได้ ไม่ต้องแยก Activity:**
หน้าจอเยอะมาก, back stack ซับซ้อน, nested navigation, dialog/bottom sheet, onboarding,
ล็อกอิน (เมื่อไม่เกี่ยวแอปนอก), แท็บ + drawer — จัดการได้ด้วย Navigation Component +
Fragment/Compose ทั้งหมด

> **สรุปเฉพาะในแอปเราเอง:** ที่ single-activity **ทำไม่ได้จริง** เหลือแค่เรื่องระดับ
> **หน้าต่าง/task ของระบบ** (หลายหน้าต่างพร้อมกัน, แยก task ใน recents, window attribute
> ระดับ Activity) ส่วนที่เหลือเป็นเรื่อง **สเกล/โครงสร้างทีม** ที่เลือกหลาย Activity เพราะ
> คุ้มกว่า ไม่ใช่เพราะจำเป็น

### B3.3 ตัวอย่าง: Drawer + Bottom Nav + หน้า detail (ซ่อน chrome)

**โจทย์:** หน้าหลักมี Navigation Drawer (hamburger) + Bottom Navigation; กดปุ่มแล้วเปิด
"หน้า detail" ที่มีแค่ toolbar + ปุ่ม back (ไม่มี bottom nav / drawer)

**คำตอบ: ใช้ single-activity** — เพราะเป็นการนำทาง **ภายในแอปเราเอง** ล้วน ไม่มีแอปนอก/
task พิเศษ/หลายหน้าต่างเข้ามาเกี่ยว จึงไม่เข้าเงื่อนไขต้องแยก Activity (ดู B3.2)

**โครงสร้าง**
```
MainActivity (Activity เดียว)
 └─ NavHostFragment            ← Navigation Component
     ├─ กลุ่มหน้าหลัก (top-level ของ bottom nav)
     │   ├─ HomeFragment
     │   ├─ SearchFragment
     │   └─ ProfileFragment
     └─ DetailFragment         ← หน้าที่กดปุ่มแล้วเปิด (มีแค่ toolbar + back)
```

**หัวใจ: ซ่อน bottom nav + ล็อก drawer ตามหน้า** โดยฟัง destination ที่เปลี่ยน
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment).navController

        // top-level = ไม่มีปุ่ม back, เปิด drawer ได้ (แสดงเป็น hamburger)
        val topLevel = setOf(R.id.homeFragment, R.id.searchFragment, R.id.profileFragment)
        val appBarConfig = AppBarConfiguration(topLevel, binding.drawerLayout)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)
        binding.navigationView.setupWithNavController(navController)

        // อยู่หน้า detail → ซ่อน bottom nav + ล็อก drawer (toolbar จะโชว์ลูกศร back เอง)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in topLevel
            binding.bottomNav.isVisible = isTopLevel
            binding.drawerLayout.setDrawerLockMode(
                if (isTopLevel) DrawerLayout.LOCK_MODE_UNLOCKED
                else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
        }
    }
}
```
- **เปิดหน้า detail:** `findNavController().navigate(R.id.action_home_to_detail)`
- **ปุ่ม back:** `setupWithNavController` + `AppBarConfiguration` จัดการให้เอง — หน้า
  top-level โชว์ hamburger, หน้า detail โชว์ลูกศร back อัตโนมัติ

> **จะพิจารณาแยก Activity เฉพาะเมื่อ** หน้า detail ต้องเปิด **แยก task** ใน recents,
> แสดง **หน้าต่างแยก** พร้อมหน้าหลัก, หรือถูกเรียกจาก **แอปอื่น/deep link** เป็นจุดเข้า
> อิสระ — เคสนี้ไม่มีสักข้อ จึงใช้ single-activity ได้เต็มที่

**"แยกเป็น 2 Activity ไม่ง่ายกว่าเหรอ?"** — ดูเหมือนง่ายกว่าตอนเริ่ม (แยกไฟล์ชัด, หน้า
detail ไม่มี bottom nav/drawer ตั้งแต่แรกจึงไม่ต้องเขียน logic ซ่อน) แต่แลกด้วยงาน manual
ที่โผล่ทีหลัง:

| ประเด็น | 2 Activity | single-activity + Fragment |
|--------|-----------|---------------------------|
| ส่งข้อมูลไปหน้า detail | Intent extras (manual, ไม่ type-safe) | **Safe Args** (type-safe) |
| ส่งผลลัพธ์กลับ | Activity Result API (ยุ่งกว่า) | shared ViewModel / FragmentResult |
| แชร์ state กับหน้าหลัก | ทำไม่ได้ตรง ๆ (คนละ ViewModelStore) | ได้เลย (nav-graph scoped ViewModel) |
| transition ระหว่างหน้า | shared element ข้าม Activity ยุ่งกว่า | animation ในตัว ลื่นกว่า |
| back stack / ผสม flow | ระบบจัดการ แต่คุมลำดับยากกว่า | คุมรวมศูนย์ที่ nav graph |
| ความสม่ำเสมอ UI/ธีม | setup toolbar/ธีมซ้ำทุก Activity | setup ครั้งเดียว |
| เพิ่มหน้าใหม่ | Activity ใหม่ + ประกาศ Manifest ทุกครั้ง | เพิ่ม destination ใน graph |

- **logic ซ่อน chrome ไม่ได้ยาก:** เขียน listener **ครั้งเดียว** ที่ `MainActivity`
  (ดูโค้ดข้างบน) ครอบคลุมทุกหน้า detail ในอนาคต ไม่ต้องเขียนซ้ำ
- **ข้อได้เปรียบจริงข้อเดียวของ 2 Activity** ในเคสนี้คือ "ไม่ต้องเขียน listener ซ่อน chrome"
  ซึ่งเป็นต้นทุนครั้งเดียวเท่านั้น

> **เมื่อไร 2 Activity ก็โอเค:** หน้า detail แค่ 1-2 หน้า ง่าย ๆ **ไม่ส่งข้อมูลกลับ ไม่แชร์
> state ไม่ต้องการ transition** — ใช้ 2 Activity ได้ ไม่ผิด • **แต่ถ้าแอปมีแนวโน้มโต / หลาย
> หน้า detail / ส่งข้อมูลไป-กลับ / อยากได้ transition** → single-activity คุ้มกว่าเพราะจ่าย
> ค่า setup แค่ครั้งเดียว (นี่คือเหตุผลที่ Google แนะนำ single-activity เป็น default)

### B3.4 Service (งานเบื้องหลัง)

Service คือ component สำหรับงานที่ **ไม่มี UI** เช่นเล่นเพลง/ซิงก์ข้อมูล แต่ในการพัฒนา
สมัยใหม่ **ความสำคัญของ Service ลดลงมาก** เพราะ Google จำกัด background execution เข้มขึ้น
ทุกเวอร์ชัน (ตั้งแต่ Android 8) และมีทางเลือกที่ดีกว่ามาแทน

**Service มี 3 แบบ (รู้ไว้พอ)**
- **Started service** — สั่งเริ่มแล้วรันต่อเอง (เดิมใช้ทำงานเบื้องหลัง — ปัจจุบันเลี่ยง)
- **Bound service** — component อื่น bind เข้ามาเรียกใช้แบบ client-server แล้วปล่อยเมื่อเลิกใช้
- **Foreground service** — งานที่ผู้ใช้เห็นและต้องรันต่อเนื่อง ต้องมี **notification ถาวร**

**สำคัญที่สุด: ส่วนใหญ่ไม่ต้องเขียน Service ตรง ๆ อีกแล้ว** — เลือกเครื่องมือตามงาน:

| งานแบบนี้ | ใช้อะไร (แนะนำ) |
|-----------|-----------------|
| งานเลื่อนเวลาได้ ต้องรับประกันว่าเสร็จ (อัปโหลด, ซิงก์, backup) | **WorkManager** |
| งานผูกกับหน้าจอ/ช่วงชีวิต (โหลดข้อมูลมาแสดง) | **coroutine** ใน viewModelScope/lifecycleScope |
| งานที่ผู้ใช้เห็นและต้องรันต่อเนื่องจริง ๆ (เล่นเพลง, นำทาง, อัดเสียง, ออกกำลังกาย) | **Foreground Service** |
| งานตามเวลา/เงื่อนไข (ทุกวัน, เมื่อต่อ WiFi/ชาร์จ) | **WorkManager** (constraints) |

**ข้อจำกัด background ยุคใหม่ (ต้องรู้)**
- แอปที่อยู่ background **ถูกจำกัด/หยุด** ไม่ให้รัน background service อิสระได้นาน
- ถ้าต้องรันตอนผู้ใช้ไม่ได้เปิดแอป → ต้องเป็น **Foreground Service** (มี notification) หรือ
  **WorkManager**
- ตั้งแต่ **Android 14** foreground service ต้องประกาศ **type** (เช่น `mediaPlayback`,
  `location`, `dataSync`) ใน Manifest + ขอ permission ให้ตรง มิฉะนั้นแอปถูกปฏิเสธ/แครช

> **สรุป:** อย่าเริ่มด้วย Service — ถามก่อนว่า "งานนี้ผู้ใช้ต้องเห็นและรันต่อเนื่องไหม?"
> ถ้าไม่ → **WorkManager หรือ coroutine** • ถ้าใช่ → **Foreground Service** (พร้อม
> notification + ประกาศ type ตั้งแต่ Android 14) • **Bound service** ใช้เมื่อต้องให้
> component อื่นเรียกใช้แบบ interface เท่านั้น

**Service เฉพาะทางที่พบบ่อย** (subclass ของ Service สำหรับงานเฉพาะ — ใช้เมื่อทำฟีเจอร์นั้น
โดยตรงเท่านั้น)
- **MediaBrowserService / MediaSessionService** — คุมการเล่นมีเดีย + เชื่อม Android Auto/Wear/ปุ่มหูฟัง
- **TileService** — ปุ่มใน Quick Settings (แถบดึงลง)
- **InputMethodService** — ทำคีย์บอร์ด (IME)
- **AccessibilityService** — บริการช่วยการเข้าถึง / automation (เช่น screen reader)
- **NotificationListenerService** — อ่าน/จัดการ notification ของระบบ
- **VpnService**, **HostApduService** (NFC จ่ายเงิน), **DreamService** (screen saver) — งานเฉพาะทางมาก
- ⚠️ **IntentService / JobIntentService** — **เลิกใช้แล้ว (deprecated)** ให้ใช้ **WorkManager** แทน

**กลไกจัดตารางงานที่เกี่ยวข้อง** (มักใช้แทน/คู่กับ Service)
- **WorkManager** — ตัวเลือกหลักสำหรับงาน background ที่เลื่อนเวลาได้ (ครอบ JobScheduler ให้)
- **JobScheduler** — API ระบบสำหรับ schedule งานตามเงื่อนไข (WorkManager ใช้ตัวนี้เบื้องหลัง —
  ปกติไม่ต้องเรียกเอง)
- **AlarmManager** — ปลุกงาน **ตามเวลาเป๊ะ ๆ** (นาฬิกาปลุก, เตือนตามเวลา) ใช้เมื่อ WorkManager
  ไม่แม่นพอเรื่องเวลา

> **เจาะลึกเรื่อง Service ทั้งหมด** (ประเภท, lifecycle, โค้ด, การเลือกใช้, ความแตกต่างตาม
> เวอร์ชัน, แนวโน้ม): ดู `docs/android-services.md`

## B4. สถาปัตยกรรมที่แนะนำ
- **แยกชั้น (layered):** UI layer → Domain (ตัวเลือก) → Data layer
- **รูปแบบยอดนิยม:** **MVVM** (Model-View-ViewModel) และ **MVI** (state ก้อนเดียว)
- **หลักการ:** UI สังเกต state จาก ViewModel; ViewModel คุย Repository; Repository
  เป็นแหล่งความจริงเดียวที่รวมข้อมูลจาก DB/network
- **ทิศทางข้อมูล:** `UI ← observe ← ViewModel ← Repository ← (DB / Network)`

> **เจาะลึกเรื่องสถาปัตยกรรมทั้งหมด** (layers, UI/Domain/Data, UDF, MVVM/MVI/MVP/MVC, DI,
> modularization, การเลือกใช้, แนวโน้ม): ดูฉบับ HTML `docs/android-architecture.html`

## B5. Lifecycle (จุดพลาดบ่อย)
```
onCreate → onStart → onResume → (ใช้งาน) → onPause → onStop → onDestroy
```
- **หมุนจอ/เปลี่ยน config = สร้าง Activity/Fragment ใหม่** → ตัวแปรที่ถือไว้ตรง ๆ หาย
- **แก้:** เก็บ state ที่ต้องรอดใน **ViewModel** (รอด config change) +
  **SavedStateHandle** (รอด process death เมื่อระบบ kill แอป)
- **Fragment มี 2 lifecycle:** ตัว Fragment และ view ของมัน — observe ข้อมูลด้วย
  `viewLifecycleOwner` และเคลียร์ ViewBinding ใน `onDestroyView` เพื่อกัน memory leak

> **เจาะลึกเรื่อง Lifecycle ทั้งหมด** (Activity/Fragment/View/ViewModel/Process/Compose,
> การเก็บ state, ความแตกต่างตามเวอร์ชัน, แนวโน้ม): ดู `docs/android-lifecycle.md`
> (หรือฉบับ HTML อ่านง่าย `docs/android-lifecycle.html`)

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
