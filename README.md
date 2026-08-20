# FoodOrder – แอปสั่งอาหารอย่างง่าย (MVVM)

โปรเจกต์ Android ตัวอย่างระบบสั่งอาหารอย่างง่าย เขียนด้วย Kotlin ใช้สถาปัตยกรรม
**MVVM** ตาม best practice และสร้าง View ด้วย **XML** (ViewBinding)

## ฟีเจอร์
- แสดงเมนูอาหาร (RecyclerView)
- เพิ่มอาหารลงตะกร้า / เพิ่ม-ลดจำนวน
- ดูตะกร้า พร้อมยอดรวม
- กดสั่งอาหารและเคลียร์ตะกร้า

## สถาปัตยกรรม (MVVM)

```
com.example.foodorder
├── data                 # Model layer
│   ├── model            # MenuItem, CartItem
│   ├── source           # FoodDataSource (ข้อมูลเมนูแบบ in-memory)
│   └── repository       # FoodRepository (interface) + Impl
├── di                   # ServiceLocator (manual DI)
└── ui                   # View + ViewModel
    ├── MainActivity     # Single-activity host + Navigation
    ├── order            # OrderViewModel + Factory (แชร์ระหว่างหน้าจอ)
    ├── menu             # MenuFragment + MenuAdapter
    └── cart             # CartFragment + CartAdapter
```

- **View** (Fragment/Activity): แสดงผลและรับ event เท่านั้น ไม่มี business logic
- **ViewModel** (`OrderViewModel`): เปิดเผย state เป็น `OrderUiState` ก้อนเดียว
  (single source of truth) ผ่าน `LiveData` แบบ read-only โดยค่าที่ derive ได้
  (`cartCount`, `cartTotal`) คำนวณจาก state จริงเสมอ และเปลี่ยน state ผ่าน public
  function เท่านั้น ส่วนงานแบบ one-shot (เช่น สั่งอาหารสำเร็จ) ส่งเป็น `OrderEvent`
  ผ่าน `Channel`/`Flow` ให้ UI จัดการครั้งเดียว ไม่ trigger ซ้ำตอนหมุนจอ
- **Repository**: แยก data layer ออกจาก ViewModel ทำให้สลับแหล่งข้อมูล (API/Room)
  ได้ง่าย และเทสต์ได้
- ใช้ `navGraphViewModels` เพื่อให้ `MenuFragment` และ `CartFragment` แชร์
  `OrderViewModel` ตัวเดียวกัน (ตะกร้าจึง sync กันอัตโนมัติ)

## เทคโนโลยีที่ใช้
- Kotlin, ViewBinding
- AndroidX Lifecycle (ViewModel + LiveData)
- Navigation Component (single-activity)
- Material 3, ConstraintLayout, RecyclerView (ListAdapter + DiffUtil)
- Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`)

## ความต้องการ
- Android Studio (แนะนำเวอร์ชันล่าสุด) — มาพร้อม Gradle และ Android SDK
- compileSdk 34, minSdk 24, targetSdk 34
- JDK 17

## วิธี build / run
1. เปิดโฟลเดอร์โปรเจกต์นี้ด้วย **Android Studio** แล้วรอ Gradle sync
   - ครั้งแรก Android Studio จะสร้าง Gradle wrapper (`gradle-wrapper.jar`,
     `gradlew`) ให้อัตโนมัติ เนื่องจากไฟล์ binary เหล่านี้ไม่ได้ commit มาด้วย
2. เลือก emulator หรือเชื่อมต่ออุปกรณ์จริง
3. กด **Run ▶** (โมดูล `app`)

> หมายเหตุ: หากต้องการ build ผ่าน command line ให้รัน `gradle wrapper` หนึ่งครั้ง
> (ต้องติดตั้ง Gradle) หรือให้ Android Studio สร้าง wrapper ให้ก่อน จากนั้นจึงใช้
> `./gradlew assembleDebug` ได้
