# Service ใน Android (ฉบับสมบูรณ์)

เอกสารนี้สรุปเรื่อง **Service** บน Android แบบครบถ้วน อ่านจบแล้วเข้าใจได้โดย **ไม่ต้อง
หาข้อมูลเพิ่ม** — ครอบคลุมว่า Service คืออะไร, ประเภททั้งหมด, lifecycle, วิธีเขียน, ข้อดี/
ข้อเสีย, การเลือกใช้, ทางเลือกยุคใหม่ (WorkManager/coroutine), **ความแตกต่างในแต่ละเวอร์ชัน
Android**, ข้อผิดพลาดที่พบบ่อย และแนวโน้มในอนาคต

> **สรุปสั้นสุด (อ่านแค่นี้ก็พอเริ่มได้):** ในการพัฒนาสมัยใหม่ **ส่วนใหญ่ไม่ต้องเขียน
> Service เองแล้ว** — งานเลื่อนเวลาได้ใช้ **WorkManager**, งานผูกหน้าจอใช้ **coroutine**,
> เหลือ **Foreground Service** ไว้เฉพาะงานที่ผู้ใช้เห็นและต้องรันต่อเนื่อง (เล่นเพลง/นำทาง)

---

## สารบัญ
1. [Service คืออะไร](#1-service-คืออะไร)
2. [ประเภทของ Service](#2-ประเภทของ-service)
3. [Service Lifecycle](#3-service-lifecycle)
4. [วิธีเขียน Service (โค้ดตัวอย่าง)](#4-วิธีเขียน-service-โค้ดตัวอย่าง)
5. [Foreground Service เจาะลึก](#5-foreground-service-เจาะลึก)
6. [Bound Service เจาะลึก](#6-bound-service-เจาะลึก)
7. [Service เฉพาะทาง](#7-service-เฉพาะทาง)
8. [ทางเลือกยุคใหม่แทน Service](#8-ทางเลือกยุคใหม่แทน-service)
9. [เลือกใช้อะไรดี (Decision Guide)](#9-เลือกใช้อะไรดี-decision-guide)
10. [ข้อดี / ข้อเสีย](#10-ข้อดี--ข้อเสีย)
11. [ความแตกต่างในแต่ละเวอร์ชัน Android](#11-ความแตกต่างในแต่ละเวอร์ชัน-android)
12. [ข้อผิดพลาดที่พบบ่อย](#12-ข้อผิดพลาดที่พบบ่อย)
13. [ความปลอดภัยของ Service](#13-ความปลอดภัยของ-service)
14. [การทดสอบ Service](#14-การทดสอบ-service)
15. [แนวโน้มในอนาคต](#15-แนวโน้มในอนาคต)
16. [ศัพท์ทางเทคนิค (Glossary)](#16-ศัพท์ทางเทคนิค-glossary)

---

## 1. Service คืออะไร

**Service** คือหนึ่งใน 4 App Component ของ Android (คู่กับ Activity, BroadcastReceiver,
ContentProvider) สำหรับงานที่ **ไม่มีหน้าตา (UI)** และต้องทำต่อเนื่องหรือทำเบื้องหลัง เช่น
เล่นเพลง, ซิงก์ข้อมูล, ดาวน์โหลดไฟล์

**สิ่งที่คนเข้าใจผิดบ่อย:**
- ❌ Service ไม่ได้รันบน thread แยกอัตโนมัติ — โดยดีฟอลต์ **รันบน main thread** ของแอป
  ถ้าทำงานหนักต้องย้ายไป thread/coroutine เอง ไม่งั้นแอปค้าง (ANR)
- ❌ Service ไม่ใช่ process แยก — อยู่ใน process เดียวกับแอป (เว้นแต่ตั้ง `android:process`)
- ❌ Service ไม่ได้รับประกันว่าจะรันตลอด — ระบบ kill ได้เมื่อ RAM ไม่พอ

**ต้องประกาศใน `AndroidManifest.xml` เสมอ:**
```xml
<service
    android:name=".MyService"
    android:exported="false" />
```

---

## 2. ประเภทของ Service

Service แบ่งตาม **วิธีเริ่มทำงาน** ได้ 3 แบบหลัก (ซ้อนกันได้):

| ประเภท | เริ่มด้วย | อายุ | ใช้ทำอะไร |
|--------|----------|------|-----------|
| **Started (unbounded)** | `startService()` / `startForegroundService()` | รันจนสั่งหยุดเอง (`stopSelf`/`stopService`) | งานที่ทำครั้งเดียวจบ |
| **Bound** | `bindService()` | อยู่จนไม่มี client bind | ให้ component อื่นเรียกใช้แบบ client-server |
| **Foreground** | `startForeground()` (หลังเริ่มแล้ว) | รันต่อเนื่อง มี notification | งานที่ผู้ใช้ต้องรับรู้ (เล่นเพลง/นำทาง) |

**Background service** = started service ที่ไม่ใช่ foreground → **ถูกจำกัดหนักตั้งแต่
Android 8** (ดูหัวข้อ 11)

---

## 3. Service Lifecycle

Service มี 2 เส้นทาง lifecycle ต่างจาก Activity:

```
Started service:
  onCreate() → onStartCommand() → (รันงาน) → onDestroy()

Bound service:
  onCreate() → onBind() → (client ใช้งาน) → onUnbind() → onDestroy()
```

**callback สำคัญ:**
- **`onCreate()`** — เรียกครั้งเดียวตอนสร้าง Service
- **`onStartCommand(intent, flags, startId)`** — เรียกทุกครั้งที่มี `startService()`
  ค่า return บอกระบบว่าจะทำยังไงถ้า Service ถูก kill:
  - `START_STICKY` — สร้างใหม่แต่ intent เป็น null (งานต่อเนื่อง เช่นเล่นเพลง)
  - `START_NOT_STICKY` — ไม่สร้างใหม่ (งานที่ไม่จำเป็นต้องทำต่อ)
  - `START_REDELIVER_INTENT` — สร้างใหม่พร้อม intent เดิม (งานที่ต้องทำให้เสร็จ)
- **`onBind()`** — return `IBinder` ให้ client (ถ้าเป็น started service ล้วน return null)
- **`onDestroy()`** — เก็บกวาด (ปิด thread, ปล่อย resource)

---

## 4. วิธีเขียน Service (โค้ดตัวอย่าง)

### 4.1 Started Service (พื้นฐาน)
```kotlin
class UploadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val fileUri = intent?.getStringExtra("fileUri")
        scope.launch {
            uploadFile(fileUri)     // งานหนักต้องอยู่บน thread แยก (ไม่ใช่ main)
            stopSelf(startId)       // เสร็จแล้วหยุดตัวเอง
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? = null   // ไม่ใช่ bound service

    override fun onDestroy() {
        scope.cancel()             // กัน coroutine รั่ว
        super.onDestroy()
    }
}
```
เริ่มจากที่อื่น: `startService(Intent(this, UploadService::class.java))`

> **หมายเหตุ:** โค้ดข้างบนเป็นตัวอย่างเชิงแนวคิด — ในงานจริงงานอัปโหลดแบบนี้ **ควรใช้
> WorkManager** (ดูหัวข้อ 8) เพราะ background service อิสระถูกจำกัดตั้งแต่ Android 8

---

## 5. Foreground Service เจาะลึก

**Foreground Service** คือ service ที่ผู้ใช้ "รับรู้ได้" ผ่าน **notification ถาวร** ระบบจึง
ยอมให้รันต่อเนื่องแม้แอปไม่ได้อยู่หน้าจอ (ต่างจาก background service ที่ถูกจำกัด)

**ใช้เมื่อ:** เล่นเพลง/พอดแคสต์, นำทาง GPS, อัดเสียง/หน้าจอ, ติดตามการออกกำลังกาย,
โอนไฟล์ขนาดใหญ่ที่ผู้ใช้กำลังรอ

```kotlin
class MusicService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("กำลังเล่นเพลง")
            .setSmallIcon(R.drawable.ic_music)
            .build()

        // ตั้งแต่ Android 10+ ควรระบุ foregroundServiceType
        startForeground(NOTIF_ID, notification)
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
```

**ข้อกำหนดตามเวอร์ชัน (สำคัญมาก):**
- **Android 8 (API 26):** ถ้าจะเริ่มตอน background ต้องใช้ `startForegroundService()` แล้ว
  **ต้องเรียก `startForeground()` ภายใน ~5 วินาที** ไม่งั้นแอปแครช (ANR)
- **Android 9 (API 28):** ต้องขอ permission `FOREGROUND_SERVICE`
- **Android 10 (API 29):** เพิ่มแนวคิด **foregroundServiceType** (`location`, `camera`,
  `microphone` ...)
- **Android 12 (API 31):** **ห้ามเริ่ม** foreground service จาก background ในหลายกรณี
  (ต้องใช้ WorkManager expedited job แทน)
- **Android 14 (API 34):** **บังคับ** ประกาศ `foregroundServiceType` ใน Manifest + ขอ
  permission ต่อ type ให้ตรง มิฉะนั้นเริ่มไม่ได้/แครช

```xml
<!-- Android 14: ต้องประกาศ type -->
<service
    android:name=".MusicService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="false" />
```

**foregroundServiceType ที่มี:** `camera`, `connectedDevice`, `dataSync`, `health`,
`location`, `mediaPlayback`, `mediaProjection`, `microphone`, `phoneCall`,
`remoteMessaging`, `shortService`, `specialUse`, `systemExempted`

---

## 6. Bound Service เจาะลึก

ให้ component อื่น (Activity/Service) **bind** เข้ามาเรียกเมธอดแบบ client-server และ
disconnect เมื่อเลิกใช้ Service จะอยู่ตราบเท่าที่ยังมี client อย่างน้อยหนึ่งตัว

```kotlin
class CounterService : Service() {
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): CounterService = this@CounterService
    }
    override fun onBind(intent: Intent?): IBinder = binder
    fun getCount(): Int = 42
}
```
```kotlin
// ฝั่ง client
val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as CounterService.LocalBinder
        val count = binder.getService().getCount()
    }
    override fun onServiceDisconnected(name: ComponentName?) {}
}
bindService(Intent(this, CounterService::class.java), connection, Context.BIND_AUTO_CREATE)
```

- **bind ใน process เดียวกัน** → ใช้ `Binder` ธรรมดา (ตัวอย่างบน)
- **bind ข้าม process (IPC)** → ต้องใช้ **AIDL** (Android Interface Definition Language)
- **Messenger** — ทางเลือก IPC ที่ง่ายกว่า AIDL เมื่อไม่ต้องการ multi-thread

---

## 7. Service เฉพาะทาง

subclass ของ Service สำหรับงานเฉพาะ — ใช้เมื่อทำฟีเจอร์นั้นโดยตรงเท่านั้น:

- **MediaBrowserService / MediaSessionService** — คุมการเล่นมีเดีย + เชื่อม Android Auto/
  Wear OS/ปุ่มบนหูฟัง
- **TileService** — ปุ่มใน Quick Settings (แถบดึงลงบนสุด)
- **InputMethodService** — ทำคีย์บอร์ด (IME) ของตัวเอง
- **AccessibilityService** — บริการช่วยการเข้าถึง / automation (screen reader, ควบคุมแทนผู้ใช้)
- **NotificationListenerService** — อ่าน/จัดการ notification ของทั้งระบบ
- **VpnService** — ทำแอป VPN
- **HostApduService** — NFC จ่ายเงิน (HCE)
- **DreamService** — screen saver ("Daydream")
- **JobService** — คู่กับ JobScheduler (ปกติใช้ผ่าน WorkManager แทน)

**⚠️ เลิกใช้แล้ว (deprecated):**
- **IntentService** — service ที่รันงานทีละคิวบน background thread → ใช้ **WorkManager** แทน
- **JobIntentService** — ตัวแทน IntentService ยุคเปลี่ยนผ่าน → ใช้ **WorkManager** แทน

---

## 8. ทางเลือกยุคใหม่แทน Service

ในการพัฒนาสมัยใหม่ งานเบื้องหลังส่วนใหญ่ **ไม่ควรเป็น Service** — ใช้เครื่องมือเหล่านี้:

### 8.1 WorkManager (แนะนำที่สุดสำหรับงาน background)
เหมาะกับงานที่ **เลื่อนเวลาได้** และ **ต้องรับประกันว่าจะเสร็จ** แม้แอปปิด/รีบูต
```kotlin
class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try { syncData(); Result.success() }
        catch (e: Exception) { Result.retry() }
    }
}
// ตั้งเงื่อนไข: ทำเมื่อต่อเน็ต + กำลังชาร์จ
val request = OneTimeWorkRequestBuilder<SyncWorker>()
    .setConstraints(Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresCharging(true).build())
    .build()
WorkManager.getInstance(context).enqueue(request)
```
- รองรับงาน one-time / periodic / chained, retry, constraints
- เบื้องหลังเลือกใช้ JobScheduler/AlarmManager ให้เองตามเวอร์ชัน

### 8.2 Coroutine (viewModelScope / lifecycleScope)
งานที่ผูกกับหน้าจอ (โหลดข้อมูลมาแสดง) — ทำใน ViewModel ไม่ต้องมี Service
```kotlin
viewModelScope.launch { val data = repository.load() }   // ยกเลิกเองเมื่อ ViewModel ตาย
```

### 8.3 กลไกระบบระดับล่าง
- **JobScheduler** — API ระบบ schedule งานตามเงื่อนไข (WorkManager ใช้ตัวนี้เบื้องหลัง)
- **AlarmManager** — ปลุกงาน **ตามเวลาเป๊ะ** (นาฬิกาปลุก, เตือนตามเวลา) ใช้เมื่อ WorkManager
  ไม่แม่นพอเรื่องเวลา (`setExactAndAllowWhileIdle` สำหรับเวลาแม่นแม้ Doze)

---

## 9. เลือกใช้อะไรดี (Decision Guide)

ถามตัวเองตามลำดับ:

```
งานนี้ผูกกับหน้าจอที่เปิดอยู่ไหม?
  └─ ใช่ → coroutine ใน viewModelScope / lifecycleScope   (ไม่ต้องมี Service)

ต้องรันตอนผู้ใช้ไม่ได้เปิดแอปไหม?
  ├─ ไม่ → coroutine ก็พอ
  └─ ใช่ ↓

ผู้ใช้ต้องเห็น/รับรู้ว่างานกำลังทำอยู่ และต้องรันต่อเนื่องทันทีไหม?
  ├─ ใช่ (เล่นเพลง/นำทาง/อัดเสียง) → Foreground Service (+ notification + type)
  └─ ไม่ (ซิงก์/อัปโหลด/backup เลื่อนเวลาได้) → WorkManager

ต้องตรงเวลาเป๊ะ ๆ (นาฬิกาปลุก)? → AlarmManager
ต้องให้ component อื่นเรียกใช้แบบ interface? → Bound Service
```

| สถานการณ์ | เครื่องมือที่ถูกต้อง |
|-----------|---------------------|
| โหลดข้อมูลมาแสดงบนหน้าจอ | coroutine (viewModelScope) |
| อัปโหลด/ซิงก์/backup (เลื่อนเวลาได้) | **WorkManager** |
| เล่นเพลง/พอดแคสต์ | **Foreground Service** (`mediaPlayback`) |
| นำทาง GPS | **Foreground Service** (`location`) |
| อัดเสียง/บันทึกหน้าจอ | **Foreground Service** (`microphone`/`mediaProjection`) |
| งานตามเวลา/รอบ (ทุกวัน) | **WorkManager** (periodic) |
| นาฬิกาปลุก/เตือนเวลาเป๊ะ | **AlarmManager** |
| ให้ Activity อื่นเรียกเมธอด | **Bound Service** |

---

## 10. ข้อดี / ข้อเสีย

**ข้อดีของการใช้ Service (Foreground)**
- รันงานต่อเนื่องได้แม้แอปไม่อยู่หน้าจอ (สำหรับงานที่ผู้ใช้เห็น)
- เป็น component ที่ระบบรู้จัก จัดการ lifecycle/priority ให้
- Bound service เปิดทางให้ component/แอปอื่นเรียกใช้แบบมีโครงสร้าง (IPC)

**ข้อเสีย / ข้อควรระวัง**
- รันบน **main thread** โดยดีฟอลต์ → เขียนพลาดทำแอปค้าง (ANR) ง่าย
- **จัดการ lifecycle เอง** (ต้อง stopSelf/หยุด thread) — ลืมแล้ว resource รั่ว/เปลืองแบต
- **ถูกจำกัดหนักตั้งแต่ Android 8** — background service อิสระแทบใช้ไม่ได้แล้ว
- Foreground service **บังคับมี notification** + ประกาศ type (Android 14) — setup เยอะ
- ทดสอบยากกว่า coroutine/WorkManager

> **สรุปเชิงเลือกใช้:** ข้อดีของ Service ถูกแทนที่ด้วย WorkManager/coroutine ได้เกือบหมด
> ยกเว้นงาน "ผู้ใช้เห็น + ต่อเนื่องทันที" ที่ยังต้องใช้ Foreground Service

---

## 11. ความแตกต่างในแต่ละเวอร์ชัน Android

ตารางไล่ผลกระทบต่อ Service ตามเวอร์ชัน (จุดเปลี่ยนสำคัญ):

| เวอร์ชัน | API | สิ่งที่เปลี่ยนเกี่ยวกับ Service |
|---------|-----|-------------------------------|
| Android 5 | 21 | เพิ่ม **JobScheduler** — ทางเลือกแรกแทน background service สำหรับงานตามเงื่อนไข |
| Android 6 | 23 | **Doze & App Standby** — เครื่องนิ่ง/แอปไม่ใช้ ระบบเลื่อนงาน background/หยุด network |
| **Android 8** | 26 | **จุดเปลี่ยนใหญ่:** จำกัด **background service** — แอปที่อยู่ background เริ่ม service ปกติไม่ได้ ต้องใช้ `startForegroundService()` (แล้วเรียก `startForeground()` ใน 5 วิ) หรือ JobScheduler; จำกัด implicit broadcast |
| Android 9 | 28 | ต้องขอ permission **`FOREGROUND_SERVICE`**; App Standby Buckets (จัดกลุ่มความถี่ที่แอปได้รันตามพฤติกรรมผู้ใช้) |
| Android 10 | 29 | เพิ่ม **`foregroundServiceType`** (location/camera/microphone); จำกัดการเริ่ม activity จาก background |
| Android 11 | 30 | foreground service ที่ใช้ตำแหน่ง/กล้อง/ไมค์ ต้องมี type ตรง + ขอ permission ให้ครบ |
| Android 12 | 31 | **ห้ามเริ่ม foreground service จาก background** ในหลายกรณี → ใช้ **WorkManager expedited job** แทน; เพิ่ม exact alarm permission |
| Android 13 | 33 | ต้องขอ permission **`POST_NOTIFICATIONS`** (กระทบ notification ของ FGS); ผู้ใช้ปิด FGS ได้จาก task manager |
| **Android 14** | 34 | **บังคับประกาศ `foregroundServiceType`** ใน Manifest + ขอ permission ต่อ type ให้ตรง มิฉะนั้นแครช; เพิ่ม type ใหม่ ๆ |
| Android 15 | 35 | เข้มขึ้นอีก: จำกัดเวลารวมของ `dataSync`/`mediaProcessing` FGS, กติกาการเริ่ม FGS ละเอียดขึ้น |

**บทสรุปเชิงประวัติ:** ทิศทางของทุกเวอร์ชันคือ **บีบ background service ให้แคบลงเรื่อย ๆ**
เพื่อประหยัดแบตและ RAM แล้วผลักให้นักพัฒนาไปใช้ **WorkManager / Foreground Service ที่มี
type ชัดเจน** แทน

---

## 12. ข้อผิดพลาดที่พบบ่อย

- **ทำงานหนักบน main thread ใน Service** → ANR • แก้: ใช้ coroutine/thread แยก
- **ลืม `stopSelf()`** → Service ค้าง เปลืองแบต/RAM
- **ลืม `startForeground()` ภายใน 5 วิ หลัง `startForegroundService()`** → แครช
  (`ForegroundServiceDidNotStartInTimeException`)
- **ไม่ประกาศ `foregroundServiceType` บน Android 14+** → เริ่ม service ไม่ได้
- **พยายามเริ่ม background service จาก background บน Android 8+** → `IllegalStateException`
- **coroutine ใน Service ไม่ถูก cancel ใน `onDestroy()`** → รั่ว
- **ใช้ Service สำหรับงานที่ควรเป็น WorkManager** → เจอข้อจำกัด background แล้วงานไม่รัน

---

## 13. ความปลอดภัยของ Service

- ตั้ง **`android:exported="false"`** ถ้าไม่ต้องการให้แอปอื่นเรียก (ดีฟอลต์ที่ปลอดภัย)
- ถ้า export ต้องกำหนด **permission** ป้องกันการเรียกโดยไม่ได้รับอนุญาต
- ระวัง **Intent ที่รับเข้ามา** — ตรวจสอบ/validate ข้อมูลก่อนใช้ (untrusted input)
- Bound service ข้าม process (AIDL) ต้องตรวจสิทธิ์ผู้เรียก (`checkCallingPermission`)
- อย่าใส่ข้อมูลลับใน notification ของ foreground service (ผู้ใช้/แอปอื่นอาจเห็น)

---

## 14. การทดสอบ Service

- **Robolectric / `ServiceController`** — ทดสอบ lifecycle ของ Service บน JVM
- **`ServiceTestRule`** (AndroidX Test) — start/bind service ใน instrumented test
- **แนวทางที่ดีกว่า:** แยก business logic ออกจาก Service ไปไว้ใน class ธรรมดา/Repository
  แล้วเทสต์ตรงนั้น (unit test) — เหลือให้ Service เป็นแค่ "ตัวห่อ" ที่บางที่สุด
- WorkManager มี **`WorkManagerTestInitHelper`** + `TestListenableWorkerBuilder` ทดสอบ
  Worker ได้ง่ายกว่าการทดสอบ Service มาก (อีกเหตุผลที่ควรเลือก WorkManager)

---

## 15. แนวโน้มในอนาคต

- **Service (โดยเฉพาะ background) จะยิ่งถูกจำกัด** — ทุกเวอร์ชันใหม่บีบให้แคบลงเพื่อแบต/
  ความเป็นส่วนตัว แนวโน้มนี้ชัดเจนและต่อเนื่อง
- **WorkManager เป็นมาตรฐานงาน background** — Google ผลักดันเป็น "ทางเข้าเดียว" ที่จัดการ
  ความต่างของแต่ละเวอร์ชันให้ นักพัฒนาไม่ต้องแตะ JobScheduler/AlarmManager ตรง ๆ
- **Foreground Service ต้อง "ชัดเจนและมีเหตุผล" มากขึ้น** — type ที่บังคับ (Android 14+)
  และการจำกัดเวลารวม (Android 15) สะท้อนว่า Google อยากให้ FGS ใช้เท่าที่จำเป็นจริง ๆ
- **coroutine/Flow เป็น async model หลัก** — งาน async ที่ผูกกับ UI แทบไม่แตะ Service เลย
- **สรุปทิศทาง:** ในอีกไม่กี่ปี นักพัฒนาแอปทั่วไป **จะเขียน Service เองน้อยลงมาก** เหลือ
  เฉพาะแอปประเภทมีเดีย/นำทาง/สุขภาพ/เครื่องมือระบบ ที่จำเป็นต้องใช้ Foreground/Specialized
  Service จริง ๆ

---

## 16. ศัพท์ทางเทคนิค (Glossary)

- **ANR (Application Not Responding)** — แอปค้างเพราะ main thread ถูกบล็อกนานเกินไป
- **Doze mode** — โหมดประหยัดพลังงานเมื่อเครื่องนิ่งนาน ระบบเลื่อนงาน background/network
- **App Standby (Buckets)** — ระบบจัดกลุ่มแอปตามความถี่การใช้ เพื่อจำกัดสิทธิ์รัน background
- **Foreground Service Type** — ประเภทงานของ FGS (location/camera/mediaPlayback ...) ที่
  Android 14+ บังคับประกาศ
- **IBinder / Binder** — อินเทอร์เฟซสำหรับสื่อสารกับ bound service
- **AIDL** — ภาษาประกาศอินเทอร์เฟซสำหรับ IPC ข้าม process
- **Messenger** — วิธี IPC ที่ง่ายกว่า AIDL (สื่อสารผ่าน Message/Handler)
- **JobScheduler** — API ระบบ schedule งาน background ตามเงื่อนไข (ตั้งแต่ Android 5)
- **WorkManager** — ไลบรารี Jetpack สำหรับงาน background ที่รับประกันการทำงาน
- **AlarmManager** — บริการปลุกงานตามเวลา (รองรับเวลาแม่นแม้ใน Doze)
- **expedited job** — งาน WorkManager ด่วนที่ระบบพยายามรันทันที (แทนการเริ่ม FGS จาก background)
- **START_STICKY / START_NOT_STICKY / START_REDELIVER_INTENT** — ค่า return ของ
  `onStartCommand` ที่บอกพฤติกรรมเมื่อ Service ถูก kill แล้วสร้างใหม่

---

## อ่านต่อ
- ภาพรวม Android สำหรับโปรแกรมเมอร์: `docs/android-for-programmers.md` (หัวข้อ B3.4)
- ประวัติเวอร์ชัน + ศัพท์เทคนิค: `docs/android-versions.md`
- การทำ test: `docs/android-testing-guide.md`
