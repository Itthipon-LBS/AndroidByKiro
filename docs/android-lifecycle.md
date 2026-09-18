# Lifecycle ใน Android (ฉบับสมบูรณ์)

เอกสารนี้สรุปเรื่อง **Lifecycle (วงจรชีวิต)** บน Android แบบครบถ้วน อ่านจบแล้วเข้าใจได้โดย
**ไม่ต้องหาข้อมูลเพิ่ม** — ครอบคลุม lifecycle ของ Activity, Fragment, View, ViewModel,
Service, Process, Compose รวมถึง lifecycle-aware components, การจัดการ state, ข้อดี/ข้อเสีย,
การเลือกใช้, **ความแตกต่างในแต่ละเวอร์ชัน Android**, ข้อผิดพลาดที่พบบ่อย และแนวโน้มในอนาคต

> **สรุปสั้นสุด (อ่านแค่นี้ก็เข้าใจแก่น):** ทุกอย่างใน Android มี "วงจรชีวิต" ที่ระบบ
> สร้าง/ทำลายให้ — หน้าที่ของเราคือ **ทำงานให้ถูกช่วง** (เริ่มตอน start, หยุดตอน stop),
> **เก็บ state ที่ต้องรอด** (ViewModel + SavedStateHandle) และ **ปล่อย resource ให้ตรงเวลา**
> เพื่อกัน memory leak / crash / เปลืองแบต

---

## สารบัญ
1. [Lifecycle คืออะไร ทำไมต้องสนใจ](#1-lifecycle-คืออะไร-ทำไมต้องสนใจ)
2. [Activity Lifecycle](#2-activity-lifecycle)
3. [Fragment Lifecycle](#3-fragment-lifecycle)
4. [View Lifecycle ของ Fragment (จุดพลาดบ่อยสุด)](#4-view-lifecycle-ของ-fragment-จุดพลาดบ่อยสุด)
5. [ViewModel Lifecycle](#5-viewmodel-lifecycle)
6. [Process Lifecycle & Process Death](#6-process-lifecycle--process-death)
7. [Lifecycle-Aware Components](#7-lifecycle-aware-components)
8. [Service / Compose / Application lifecycle](#8-service--compose--application-lifecycle)
9. [การจัดการ State ให้รอดแต่ละกรณี](#9-การจัดการ-state-ให้รอดแต่ละกรณี)
10. [การเลือกใช้ (เก็บอะไรไว้ที่ไหน)](#10-การเลือกใช้-เก็บอะไรไว้ที่ไหน)
11. [ข้อดี / ข้อเสีย ของโมเดล lifecycle](#11-ข้อดี--ข้อเสีย-ของโมเดล-lifecycle)
12. [ความแตกต่างในแต่ละเวอร์ชัน Android](#12-ความแตกต่างในแต่ละเวอร์ชัน-android)
13. [ข้อผิดพลาดที่พบบ่อย](#13-ข้อผิดพลาดที่พบบ่อย)
14. [การทดสอบเกี่ยวกับ lifecycle](#14-การทดสอบเกี่ยวกับ-lifecycle)
15. [แนวโน้มในอนาคต](#15-แนวโน้มในอนาคต)
16. [ศัพท์ทางเทคนิค (Glossary)](#16-ศัพท์ทางเทคนิค-glossary)

---

## 1. Lifecycle คืออะไร ทำไมต้องสนใจ

**Lifecycle** คือลำดับ "สถานะ" ที่ component ผ่านตั้งแต่ถูกสร้างจนถูกทำลาย โดย **ระบบ
(Android framework) เป็นคนเรียก callback** เหล่านี้ให้ ไม่ใช่เรา

ทำไมต้องเข้าใจ:
- **หมุนจอ / เปลี่ยนภาษา / ปรับขนาดหน้าต่าง** → Activity/Fragment ถูก **ทำลายและสร้างใหม่**
  ตัวแปรที่ถือไว้ตรง ๆ จะหาย
- **สลับแอป / รับสาย** → แอปไป background ระบบอาจ **kill process** เพื่อคืน RAM
- ทำงาน/ปล่อย resource **ผิดช่วง** → memory leak, crash, เปลืองแบต, กล้อง/ไมค์ค้าง

> หลักคิดหลัก: **"ทำงานให้เริ่ม-หยุดตามสถานะ"** และ **"อย่าถือ reference ข้ามช่วงชีวิต"**

---

## 2. Activity Lifecycle

```
   onCreate()        ← สร้าง UI, init (ครั้งเดียวต่อการสร้าง)
      ↓
   onStart()         ← มองเห็นบนจอ (ยังโต้ตอบไม่ได้)
      ↓
   onResume()        ← อยู่หน้าสุด โต้ตอบได้ (foreground)
      ↓
   [ ผู้ใช้ใช้งาน ]
      ↓
   onPause()         ← กำลังจะเสียโฟกัส (มีอย่างอื่นบัง)
      ↓
   onStop()          ← มองไม่เห็นแล้ว
      ↓
   onDestroy()       ← ถูกทำลาย (จบ activity หรือ config change)
```

**callback สำคัญและควรทำอะไร:**
| callback | เกิดเมื่อ | ควรทำ |
|----------|----------|-------|
| `onCreate()` | สร้าง Activity | inflate layout, init ViewModel, กู้ state |
| `onStart()` | เริ่มมองเห็น | เริ่ม observe ข้อมูล/animation |
| `onResume()` | อยู่ foreground | เริ่มกล้อง/เซ็นเซอร์/งานที่ต้องเห็นจอ |
| `onPause()` | กำลังเสียโฟกัส | หยุดงานเบา ๆ (อย่าทำงานหนัก—ต้องเร็ว) |
| `onStop()` | มองไม่เห็น | หยุด/ปล่อยกล้อง-เซ็นเซอร์, บันทึกข้อมูล |
| `onDestroy()` | ถูกทำลาย | เก็บกวาดสุดท้าย |

**Configuration change (หมุนจอ ฯลฯ):**
```
onPause → onStop → onDestroy → onCreate → onStart → onResume  (สร้างใหม่ทั้งหมด!)
```
- ค่าใน property ของ Activity **หายหมด** → ต้องเก็บใน **ViewModel** (รอด) หรือ
  **`onSaveInstanceState()`** (Bundle เล็ก ๆ)

**Multi-window / Multi-resume (Android 10+):** หลาย Activity อาจอยู่สถานะ RESUMED พร้อมกัน
ได้ในโหมดหลายหน้าต่าง — อย่าคิดว่ามีแค่ตัวเดียว resume เสมอ

---

## 3. Fragment Lifecycle

Fragment มี callback มากกว่า Activity เพราะมีทั้ง lifecycle ของตัว Fragment **และของ View**

```
onAttach()            ← ผูกกับ Activity
   ↓
onCreate()            ← init (ยังไม่มี view)
   ↓
onCreateView()        ← สร้าง view hierarchy
   ↓
onViewCreated()       ← view พร้อม → setup UI/observe ที่นี่
   ↓
onStart() → onResume()  ← เหมือน Activity
   ↓
[ ใช้งาน ]
   ↓
onPause() → onStop()
   ↓
onDestroyView()       ← ทำลาย view (แต่ Fragment ยังอยู่!)
   ↓
onDestroy() → onDetach()
```

**จุดต่างสำคัญ:** เมื่อ Fragment อยู่ใน back stack แล้วถูกแทนที่ → เรียก **`onDestroyView()`
แต่ไม่ `onDestroy()`** (ตัว Fragment ยังมีชีวิต) พอกลับมาจะ **`onCreateView()` ใหม่** โดยไม่
`onCreate()` ใหม่ → นี่คือที่มาของบั๊ก ViewBinding รั่ว (หัวข้อ 4)

---

## 4. View Lifecycle ของ Fragment (จุดพลาดบ่อยสุด)

Fragment มี 2 lifecycle ที่ **อายุไม่เท่ากัน**:
- **Fragment lifecycle** — ยาวกว่า (อยู่ตั้งแต่ onCreate ถึง onDestroy)
- **View lifecycle** — สั้นกว่า (onCreateView ถึง onDestroyView) และ **เกิดใหม่ได้หลายรอบ**

**ผลที่ตามมา:**
1. **observe ข้อมูลต้องใช้ `viewLifecycleOwner`** ไม่ใช่ `this` (ตัว Fragment) มิฉะนั้นเมื่อ
   view ถูกทำลายแล้วสร้างใหม่ จะมี observer ซ้อนหลายตัว → อัปเดต UI หลายรอบ/ค้าง reference
```kotlin
// ✅ ถูก
viewModel.state.observe(viewLifecycleOwner) { render(it) }
// ❌ ผิด — จะรั่ว/ซ้อน observer
viewModel.state.observe(this) { render(it) }
```
2. **ViewBinding ต้องเคลียร์ใน `onDestroyView()`** มิฉะนั้น Fragment (ที่อายุยาวกว่า) จะถือ
   reference ของ view เก่าไว้ → **memory leak**
```kotlin
private var _binding: FragmentHomeBinding? = null
private val binding get() = _binding!!

override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
    _binding = FragmentHomeBinding.inflate(i, c, false)
    return binding.root
}
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null          // ปล่อย view เพื่อกัน leak
}
```
3. **เก็บ coroutine ผูกกับ view** ใช้ `viewLifecycleOwner.lifecycleScope` +
   `repeatOnLifecycle(STARTED)` เพื่อให้หยุด/เริ่มตามการมองเห็น

---

## 5. ViewModel Lifecycle

**ViewModel อยู่ยาวกว่า Activity/Fragment** — รอดจาก configuration change (หมุนจอ) โดยไม่
ถูกสร้างใหม่ จึงเหมาะเก็บ **UI state / ผลลัพธ์ที่ไม่อยากโหลดซ้ำ**

```
Activity สร้าง → ViewModel สร้าง
หมุนจอ: Activity ทำลาย+สร้างใหม่ → ViewModel "ตัวเดิม" ถูกส่งต่อ (ไม่สร้างใหม่)
ออกจากหน้าจริง (finish): → onCleared() ของ ViewModel ถูกเรียก → เก็บกวาด
```

- **`onCleared()`** — เรียกเมื่อ ViewModel ถูกทิ้งจริง (เจ้าของหายถาวร) → ปิด resource
- **`viewModelScope`** — coroutine scope ที่ถูกยกเลิกอัตโนมัติเมื่อ `onCleared()` (ไม่รั่ว)
- **ขอบเขต (scope) ของ ViewModel** เลือกได้:
  - ผูกกับ Fragment → `by viewModels()`
  - แชร์ทั้ง Activity → `by activityViewModels()`
  - แชร์ระดับ nav graph → `by navGraphViewModels(R.id.graph)` (import `androidx.navigation`)

> **ข้อควรจำ:** ViewModel **รอด config change แต่ไม่รอด process death** — ข้อมูลที่ต้องรอด
> ทั้งสองกรณีต้องใช้ **SavedStateHandle** (ดูหัวข้อ 9)

---

## 6. Process Lifecycle & Process Death

เมื่อแอปอยู่ background นาน ระบบอาจ **kill process** เพื่อคืน RAM ให้แอปอื่น เรียกว่า
**process death** — พอผู้ใช้กลับมา ระบบสร้างแอปใหม่ทั้งหมด (ViewModel ก็หายด้วย)

- ระบบจัดลำดับความสำคัญของ process: **foreground > visible > service > cached** — cached
  process (แอปที่อยู่ background) ถูก kill ก่อน
- **จำลองทดสอบ:** เปิดแอป → กด home → ใช้ "Don't keep activities" (Developer options) หรือ
  สั่ง kill จาก Android Studio → กลับเข้าแอป ดูว่า state กลับมาครบไหม
- **ป้องกันข้อมูลหาย:** เก็บ state สำคัญใน **SavedStateHandle** / `onSaveInstanceState()`
  (ข้อมูลเล็ก) หรือ persist ลง **DataStore/Room** (ข้อมูลใหญ่/ถาวร)

**`ProcessLifecycleOwner`** — ให้ lifecycle ระดับทั้งแอป (รู้ว่าแอปทั้งตัวเข้า foreground/
background) เหมาะกับงานอย่าง "แอปกลับมา foreground แล้วรีเฟรช token"

---

## 7. Lifecycle-Aware Components

Jetpack ให้เครื่องมือที่ "รู้จัก lifecycle" เพื่อไม่ต้องเขียน callback เองทุกจุด:

- **`LifecycleObserver` / `DefaultLifecycleObserver`** — ให้ component (เช่น analytics,
  ตัวจับตำแหน่ง) ทำงานตาม lifecycle ของเจ้าของโดยไม่ยัด logic ไว้ใน Activity
```kotlin
class LocationTracker : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) { startUpdates() }
    override fun onStop(owner: LifecycleOwner) { stopUpdates() }
}
// ใน Activity/Fragment
lifecycle.addObserver(LocationTracker())
```
- **`lifecycleScope`** — coroutine scope ผูกกับ lifecycle (ยกเลิกเมื่อ destroy)
- **`repeatOnLifecycle(Lifecycle.State.STARTED)`** — วิธี **แนะนำ** ในการ collect Flow ให้
  หยุดตอน background และเริ่มใหม่ตอนกลับมาเห็น (ประหยัดทรัพยากร/กัน crash)
```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { render(it) }
    }
}
```
- **`LiveData`** — lifecycle-aware โดยธรรมชาติ (ส่งค่าเฉพาะตอน active)

---

## 8. Service / Compose / Application lifecycle

**Service lifecycle** (ย่อ — ดูละเอียดใน `docs/android-services.md`):
```
Started: onCreate → onStartCommand → onDestroy
Bound:   onCreate → onBind → onUnbind → onDestroy
```

**Compose lifecycle** (ต่างจาก View):
- Composable ไม่มี onCreate/onDestroy แต่มี **enter/leave composition**
- **`remember`** — เก็บค่าไว้ข้าม recomposition • **`rememberSaveable`** — รอด config
  change/process death (คล้าย SavedStateHandle)
- **`DisposableEffect`** — ทำงานตอนเข้า composition และ **คืน `onDispose {}`** ตอนออก
  (ใช้แทน onStop/onDestroy)
- **`LaunchedEffect`** — รัน coroutine ผูกกับ composition
- ยัง observe lifecycle จริงได้ผ่าน `LocalLifecycleOwner`

**Application lifecycle:**
- `Application.onCreate()` — เรียกครั้งเดียวตอน process เกิด (init library ที่นี่)
- ใช้ **App Startup** library หรือ `ProcessLifecycleOwner` สำหรับงานระดับแอป

---

## 9. การจัดการ State ให้รอดแต่ละกรณี

มี 3 "ระดับการรอด" ที่ต้องแยกให้ชัด:

| กรณี | ViewModel | onSaveInstanceState / SavedStateHandle | DataStore/Room |
|------|-----------|----------------------------------------|----------------|
| หมุนจอ / config change | ✅ รอด | ✅ รอด | ✅ รอด |
| process death (ระบบ kill) | ❌ หาย | ✅ รอด (Bundle) | ✅ รอด |
| ปิดแอปเอง/รีบูต | ❌ หาย | ❌ หาย | ✅ รอด (persistent) |

**SavedStateHandle** (แนะนำ) — เก็บ state เล็ก ๆ ที่ต้องรอดทั้ง config change และ process
death ผ่าน ViewModel โดยตรง:
```kotlin
class SearchViewModel(private val state: SavedStateHandle) : ViewModel() {
    val query: StateFlow<String> = state.getStateFlow("query", "")
    fun setQuery(q: String) { state["query"] = q }   // เก็บอัตโนมัติ
}
```
- ใช้เก็บของเล็ก (query, ตำแหน่ง scroll, id ที่เลือก) — **ห้ามเก็บของใหญ่** (list/bitmap)
- ของใหญ่/ถาวร → **Room/DataStore**; ของชั่วคราวใหญ่ → เก็บ id แล้วโหลดใหม่จาก repository

---

## 10. การเลือกใช้ (เก็บอะไรไว้ที่ไหน)

```
ข้อมูลนี้ต้องอยู่ถาวร (ปิดแอป/รีบูตแล้วยังอยู่) ไหม?
  └─ ใช่ → DataStore (preference) / Room (ข้อมูลมีโครงสร้าง)

ต้องรอด process death (ระบบ kill ตอน background) ไหม?
  └─ ใช่ + เป็นข้อมูลเล็ก → SavedStateHandle
     ใช่ + ข้อมูลใหญ่ → เก็บ id/คีย์ใน SavedStateHandle แล้วโหลดจาก repository

แค่ต้องรอดหมุนจอ (config change) ก็พอ ไหม?
  └─ ใช่ → ViewModel (StateFlow/LiveData)

เป็นแค่ค่าชั่วคราวใน UI ระหว่าง recomposition (Compose)?
  └─ remember (ไม่ต้องรอด) / rememberSaveable (รอด config+process death)
```

**observe ข้อมูลใน UI:**
- View system → `repeatOnLifecycle(STARTED)` + `viewLifecycleOwner` (แนะนำ) หรือ LiveData
- Compose → `collectAsStateWithLifecycle()`

---

## 11. ข้อดี / ข้อเสีย ของโมเดล lifecycle

**ข้อดี**
- ระบบจัดการ resource ให้อัตโนมัติ (คืน RAM, ประหยัดแบต) — แอปหลายตัวอยู่ร่วมกันได้ลื่น
- callback ชัดเจนว่า "ควรทำอะไรตอนไหน" ทำให้จัดการกล้อง/เซ็นเซอร์/network ถูกจังหวะ
- lifecycle-aware components (lifecycleScope, repeatOnLifecycle, LiveData) ลด boilerplate
  และลดบั๊กเรื่อง leak

**ข้อเสีย / ความยาก**
- **ซับซ้อน** โดยเฉพาะ Fragment 2 lifecycle — เป็นแหล่งบั๊กยอดฮิต (leak, observer ซ้อน)
- **config change สร้างใหม่ทั้งหมด** — ต้องวางแผนเก็บ state ตั้งแต่ต้น ไม่งั้นข้อมูลหาย
- **process death มองไม่เห็นตอน dev** — ลืมเทสต์แล้วเจอบั๊กตอน production
- callback เยอะ ทำให้มือใหม่สับสนว่าควรวาง logic ตรงไหน

> Compose พยายามลดความเจ็บปวดนี้ด้วยโมเดล state-driven + effect API แต่ก็ยังต้องเข้าใจ
> lifecycle ที่อยู่ข้างล่างอยู่ดี

---

## 12. ความแตกต่างในแต่ละเวอร์ชัน Android

| เวอร์ชัน | API | สิ่งที่เปลี่ยนเกี่ยวกับ lifecycle |
|---------|-----|----------------------------------|
| Android 3 | 11 | เพิ่ม **Fragment** เข้ามาในระบบ (แยก UI ย่อย + lifecycle ของตัวเอง) |
| Android 6 | 23 | **Doze & App Standby** — background lifecycle ถูกจำกัดเมื่อเครื่องนิ่ง/แอปไม่ถูกใช้ |
| Android 7 | 24 | **Multi-window (split-screen)** — Activity เข้า/ออก resumed บ่อยขึ้น, config change จากปรับขนาดหน้าต่าง |
| Android 8 | 26 | จำกัด background execution — แอปที่ไป background ถูกหยุดงานเร็วขึ้น กระทบการทำงานหลัง onStop |
| Android 9 | 28 | เพิ่มการ์ดความเป็นส่วนตัว: แอป background เข้าถึงกล้อง/ไมค์/เซ็นเซอร์ไม่ได้ → ต้องหยุดใน onStop |
| **Android 10** | 29 | **Multi-resume** — หลาย Activity เป็น RESUMED พร้อมกันได้ในหลายหน้าต่าง; จำกัดการเริ่ม Activity จาก background |
| Android 11 | 30 | one-time permission → สถานะ permission เปลี่ยนกลางคันได้ ต้องเช็คใหม่ทุกครั้งที่กลับ resumed |
| Android 12 | 31 | splash screen API ใหม่กระทบช่วง start; ปรับพฤติกรรม back/task |
| Android 13 | 33 | **Predictive back (ทดลอง)** — ต้องจัดการ back ผ่าน `OnBackPressedCallback`/`OnBackInvokedCallback` แทนของเดิม |
| Android 14 | 34 | **Predictive back** เสถียรขึ้น → ควรย้ายมาใช้ back API ใหม่; ปรับพฤติกรรม lifecycle เมื่อ predictive back animation |
| Android 15/16 | 35+ | edge-to-edge/หน้าต่างปรับขนาดเป็นค่าเริ่มต้นมากขึ้น → config change/ปรับขนาดเกิดบ่อยขึ้น ต้องรองรับให้ดี |

**บทสรุปเชิงประวัติ:** ทิศทางคือ **หน้าต่าง/สถานะยืดหยุ่นขึ้น** (multi-window, multi-resume,
predictive back, resizable) ทำให้ config change และการสลับสถานะ **เกิดบ่อยขึ้น** — โค้ดที่
เก็บ state ถูกวิธีตั้งแต่ต้นจะได้เปรียบ

---

## 13. ข้อผิดพลาดที่พบบ่อย

- **observe ด้วย `this` แทน `viewLifecycleOwner` ใน Fragment** → observer ซ้อน/รั่ว
- **ลืมเคลียร์ ViewBinding ใน `onDestroyView()`** → memory leak
- **เก็บ Context/View/Activity ไว้ใน ViewModel** → leak (ViewModel อายุยาวกว่า)
- **ทำงานหนักใน `onCreate`/`onResume`** → เปิดหน้าจอช้า/กระตุก
- **ไม่จัดการ config change** → ข้อมูลหายตอนหมุนจอ (อย่าแก้ด้วย `android:configChanges`
  แบบมักง่าย — ควรเก็บใน ViewModel)
- **ไม่เทสต์ process death** → ผู้ใช้กลับมาแล้วแอปว่างเปล่า/แครช
- **collect Flow โดยไม่ใช้ `repeatOnLifecycle`** → collect ต่อตอน background เปลืองทรัพยากร/
  อัปเดต UI ที่มองไม่เห็น
- **ทำงานหนักใน `onPause()`** → หน่วง transition (onPause ต้องเร็ว)

---

## 14. การทดสอบเกี่ยวกับ lifecycle

- **หมุนจอ / config change:** เปลี่ยน orientation แล้วดูว่า state ยังอยู่ (ViewModel ทำงาน)
- **process death:** เปิด "Don't keep activities" หรือใช้ Logcat/terminal สั่ง kill process
  แล้วกลับเข้าแอป
- **Robolectric / `ActivityScenario` / `FragmentScenario`** — ขับ lifecycle ในเทสต์:
```kotlin
val scenario = launchActivity<MainActivity>()
scenario.moveToState(Lifecycle.State.CREATED)   // จำลองสถานะต่าง ๆ
scenario.recreate()                             // จำลอง config change
```
- **`TestLifecycleOwner`** (androidx) — ทดสอบ lifecycle observer ที่เราเขียนเอง
- **LeakCanary** — จับ memory leak จากการถือ reference ข้าม lifecycle อัตโนมัติตอน dev

---

## 15. แนวโน้มในอนาคต

- **Compose ลดการแตะ lifecycle ตรง ๆ** — โมเดล state-driven + `rememberSaveable` +
  effect API ทำให้จัดการ state/side-effect เป็นธรรมชาติขึ้น แต่ยังต้องเข้าใจ lifecycle
  ที่อยู่ข้างใต้
- **`collectAsStateWithLifecycle` / `repeatOnLifecycle` เป็นมาตรฐาน** — การ observe แบบ
  lifecycle-aware กลายเป็น default ที่ Google แนะนำ
- **หน้าต่างยืดหยุ่นเป็นค่าเริ่มต้น** — พับได้/แท็บเล็ต/เดสก์ท็อป/หลายหน้าต่าง ทำให้ config
  change เกิดบ่อย โค้ดต้อง "ไม่กลัวการสร้างใหม่" ตั้งแต่ออกแบบ
- **Predictive back** — การจัดการ back เปลี่ยนมาใช้ callback API ใหม่ ควรย้ายออกจาก
  `onBackPressed()` เดิม
- **สรุปทิศทาง:** เครื่องมือช่วยจัดการ lifecycle จะ "อัตโนมัติ" ขึ้นเรื่อย ๆ แต่ **หลักการ
  พื้นฐาน** (สร้างใหม่ได้เสมอ, เก็บ state ถูกที่, ปล่อย resource ตรงเวลา) จะยังจำเป็นเสมอ

---

## 16. ศัพท์ทางเทคนิค (Glossary)

- **Configuration change** — การเปลี่ยนค่าระบบ (หมุนจอ, ภาษา, ขนาดหน้าต่าง, ธีม) ที่ทำให้
  Activity/Fragment ถูกสร้างใหม่
- **Process death** — ระบบ kill process ของแอป (ตอน background) เพื่อคืน RAM
- **LifecycleOwner** — สิ่งที่มี lifecycle (Activity, Fragment, viewLifecycleOwner, process)
- **viewLifecycleOwner** — LifecycleOwner ของ **view** ใน Fragment (อายุสั้นกว่าตัว Fragment)
- **SavedStateHandle** — ที่เก็บ state ใน ViewModel ที่รอดทั้ง config change และ process death
- **onSaveInstanceState** — callback เก็บ state ลง Bundle (รอด process death, ของเล็ก)
- **ViewModel scope (viewModelScope)** — coroutine scope ที่ยกเลิกเมื่อ ViewModel ถูก clear
- **repeatOnLifecycle** — API ให้ collect Flow เฉพาะช่วงที่ lifecycle อย่างน้อย STARTED
- **ProcessLifecycleOwner** — lifecycle ระดับทั้งแอป (foreground/background ของทั้งโปรเซส)
- **DisposableEffect / LaunchedEffect (Compose)** — effect API ที่ผูกกับ composition
- **recomposition (Compose)** — การวาด composable ใหม่เมื่อ state เปลี่ยน
- **Multi-resume** — หลาย Activity เป็น RESUMED พร้อมกันได้ (Android 10+, หลายหน้าต่าง)

---

## อ่านต่อ
- ภาพรวม Android สำหรับโปรแกรมเมอร์: `docs/android-for-programmers.md` (หัวข้อ B5)
- Service ใน Android: `docs/android-services.md`
- ประวัติเวอร์ชัน + ศัพท์เทคนิค: `docs/android-versions.md`
- การทำ test: `docs/android-testing-guide.md`
