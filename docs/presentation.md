# FoodOrder — แอปสั่งอาหาร (MVVM)

Presentation

1. ภาพรวมของระบบ (คร่าว ๆ)
2. การเขียน Test แบบละเอียด ตั้งแต่ต้นจนจบ (พร้อม diagram)

> วิธีนำเสนอ: ไฟล์นี้แบ่งสไลด์ด้วยเส้น `---` และวาด diagram ด้วย Mermaid
> ดูได้ใน VS Code (Markdown Preview) หรือ GitHub และ export เป็นสไลด์ได้ด้วย
> Marp / reveal.js

---

# ส่วนที่ 1 — ภาพรวมของระบบ

---

## ระบบนี้คืออะไร

- แอป **สั่งอาหารอย่างง่าย** บน Android เขียนด้วย **Kotlin**
- สถาปัตยกรรม **MVVM** ตาม best practice
- หน้าจอ (View) เป็น **XML** + **ViewBinding**
- 2 หน้าจอหลัก: **เมนูอาหาร** และ **ตะกร้าสินค้า**

ความสามารถ
- ดูเมนู, เพิ่มลงตะกร้า, เพิ่ม/ลดจำนวน
- ดูยอดรวม, กดสั่งอาหาร แล้วเคลียร์ตะกร้า

---

## สถาปัตยกรรม MVVM

```mermaid
flowchart TD
    subgraph UI["ชั้น UI (View - XML)"]
        A[MenuFragment]
        B[CartFragment]
    end
    subgraph VML["ViewModel"]
        C["OrderViewModel<br/>uiState: StateFlow(OrderUiState)<br/>events: Flow(OrderEvent)"]
    end
    subgraph DATA["ชั้น Data"]
        D["FoodRepository<br/>(interface)"]
        E[FoodRepositoryImpl]
        F["FoodDataSource<br/>เมนู in-memory"]
    end
    A -- "observe uiState / ส่ง action" --> C
    B -- "observe uiState / ส่ง action" --> C
    C -- "getMenu() suspend" --> D
    D --> E --> F
    C -. "persist ตะกร้า" .-> G[(SavedStateHandle)]
```

- **View** แสดงผล + ส่ง event เท่านั้น ไม่มี business logic
- **ViewModel** เป็น single source of truth เปิด state ผ่าน `StateFlow`
- **Repository** ซ่อนที่มาของข้อมูล (สลับเป็น API/DB ได้)

---

## Flow การทำงานของผู้ใช้

```mermaid
flowchart LR
    S([เปิดแอป]) --> M[หน้าเมนู]
    M -->|"กดเพิ่ม"| M
    M -->|"ดูตะกร้า"| C[หน้าตะกร้า]
    C -->|"กดเพิ่ม / ลด"| C
    C -->|"กดสั่งอาหาร"| O{"ตะกร้าว่าง?"}
    O -->|"ไม่ว่าง"| T["Toast สำเร็จ แล้วกลับหน้าเมนู"]
    O -->|"ว่าง"| C
```

---

## Tech stack

| ด้าน | เทคโนโลยี |
|------|-----------|
| ภาษา | Kotlin |
| UI | XML + ViewBinding, Material 3 |
| สถาปัตยกรรม | MVVM (StateFlow + one-shot events) |
| Navigation | Navigation Component (single-activity) |
| Async | Coroutines + Flow |
| State survival | SavedStateHandle (รอด process death) |
| เงิน | BigDecimal (เลี่ยง float error) |
| Build | Gradle Kotlin DSL + Version Catalog |

---

## โครงสร้างแพ็กเกจ

```
com.example.foodorder
├── data
│   ├── model         MenuItem, CartItem
│   ├── source        FoodDataSource
│   └── repository    FoodRepository (+Impl)
├── di                ServiceLocator
└── ui
    ├── MainActivity
    ├── order         OrderViewModel, OrderUiState, OrderEvent
    ├── menu          MenuFragment, MenuAdapter
    └── cart          CartFragment, CartAdapter
```

---

# ส่วนที่ 2 — การเขียน Test (ต้นจนจบ)

---

## ทำไมต้องเทสต์ & เลือกระดับไหน

```mermaid
flowchart TD
    U["Unit tests (JVM)<br/>เร็ว • คุ้มสุด • ไม่ต้อง emulator ⬅ โฟกัส"]
    I["Integration tests"]
    E2E["UI / Instrumented tests<br/>ช้า • ต้องใช้ emulator"]
    U --> I --> E2E
```

- เริ่มที่ **Unit test (JVM)** เพราะเร็วและคุ้มที่สุด
- เป้าหมายหลัก: **`OrderViewModel`** (รวม business logic ตะกร้า/ยอดรวม/event)
- เทสต์ได้ง่ายเพราะ MVVM แยก layer และฉีด dependency ผ่าน constructor

---

## ภาพรวม 6 ขั้นตอน

```mermaid
flowchart TD
    S1["1. เลือกระดับเทสต์<br/>Unit test + เป้า OrderViewModel"]
    S2["2. เพิ่ม dependencies<br/>coroutines-test, Turbine"]
    S3["3. สร้าง test infrastructure<br/>MainDispatcherRule, FakeFoodRepository"]
    S4["4. เขียนเทสต์ OrderViewModel<br/>ไล่ทีละพฤติกรรม"]
    S5["5. เทสต์ model / state<br/>CartItem, OrderUiState"]
    S6["6. ปรับให้ black-box + ปิดงาน<br/>round-trip, commit, push"]
    S1 --> S2 --> S3 --> S4 --> S5 --> S6
```

---

## ขั้นที่ 1–2: ตัดสินใจ & เตรียมเครื่องมือ

**1. ตัดสินใจ**
- ระดับ: local unit test (JVM)
- เป้า: `OrderViewModel` + models/state

**2. เพิ่ม dependencies** (`libs.versions.toml` + `build.gradle.kts`)
- `kotlinx-coroutines-test` — คุม coroutine / `viewModelScope`
- `Turbine` — ทดสอบ `Flow` / event ให้เขียนง่าย

---

## ขั้นที่ 3: สร้าง Test Infrastructure

```mermaid
flowchart LR
    T[OrderViewModelTest] --> VM[OrderViewModel]
    T --> R[MainDispatcherRule]
    R -. "setMain(testDispatcher)" .-> Main[Dispatchers.Main]
    VM --> Repo["FoodRepository (interface)"]
    Fake[FakeFoodRepository] -. "ใช้แทน" .-> Repo
    T --> Fake
    T --> Tur["Turbine (ทดสอบ event)"]
```

- **`MainDispatcherRule`** — สลับ `Dispatchers.Main` เป็น test dispatcher
  (เพราะ `viewModelScope` รันบน Main ซึ่งไม่มีจริงในเทสต์ JVM)
- **`FakeFoodRepository`** — ใช้ **fake** แทน mock คืนเมนูคงที่ (ITEM_A=55, ITEM_B=50)

---

## ขั้นที่ 4: เขียนเทสต์ OrderViewModel

โครงแต่ละเทสต์แบบ **AAA**

```mermaid
flowchart LR
    A["Arrange<br/>สร้าง ViewModel + fake"] --> B["Act<br/>เรียก addToCart / placeOrder"]
    B --> C["Assert<br/>ตรวจ uiState / event"]
```

เทคนิคที่ใช้
- `runTest { ... }` + `advanceUntilIdle()` รอ coroutine ทำงาน
- อ่านสถานะจาก `uiState.value`
- ตรวจ event ด้วย **Turbine** (`awaitItem`, `expectNoEvents`)
- เทียบเงิน `BigDecimal` ด้วย `compareTo` (กันปัญหา scale)

---

## ขั้นที่ 4: เคสที่ครอบคลุม (12 เคส)

- โหลดเมนูเข้า `uiState` ตอน init
- สถานะเริ่มต้น: ตะกร้าว่าง / count / total = 0
- `addToCart` เพิ่มจำนวน + อัปเดต count/total
- ยอดรวมของสินค้าหลายชนิด
- `decreaseQuantity` จนเหลือ 0 → รายการหาย
- `decreaseQuantity` ตอน > 1 → ยังอยู่
- `placeOrder` ตะกร้าว่าง → ไม่ยิง event
- `placeOrder` มีของ → ยิง `OrderPlaced` + เคลียร์ตะกร้า
- restore จาก `SavedStateHandle`
- **round-trip** สร้าง ViewModel ใหม่แล้วตะกร้ายังอยู่
- `decreaseQuantity` สินค้าที่ไม่มี → no-op
- คงลำดับการเพิ่มสินค้า (insertion order)

---

## ขั้นที่ 5: เทสต์ระดับ Model / State

- **`CartItemTest`** — `lineTotal = ราคา × จำนวน`
- **`OrderUiStateTest`** — derived values:
  - `cartCount` (ผลรวมจำนวน)
  - `cartTotal` (ผลรวม lineTotal)
  - `isCartEmpty`

เทสต์พวกนี้เป็น pure logic ไม่ต้อง mock ใด ๆ

---

## ขั้นที่ 6: ปรับให้เป็น Black-box (round-trip)

**ก่อน:** เทสต์ persist ไปอ่าน key ภายใน (`"cart_quantities"`) → ผูกกับ implementation

**หลัง:** ทดสอบพฤติกรรมจริงของการ restore

```mermaid
sequenceDiagram
    participant T as Test
    participant VM1 as OrderViewModel ตัวแรก
    participant SSH as SavedStateHandle
    participant VM2 as OrderViewModel สร้างใหม่
    T->>VM1: addToCart A สองครั้ง และ addToCart B
    VM1->>SSH: persist ตะกร้า (id และ จำนวน)
    T->>VM2: สร้างใหม่ด้วย SSH ตัวเดิม
    SSH-->>VM2: อ่านตะกร้าที่บันทึกไว้
    VM2-->>T: uiState.cart ถูก restore
```

---

## สรุป

- โครงสร้าง MVVM ที่แยก layer ทำให้ **เทสต์ง่ายและเร็ว**
- ครอบคลุมพฤติกรรมหลัก + เคสขอบ รวม **12 เคส** ใน `OrderViewModel`
  และเทสต์ model/state แยกต่างหาก
- ใช้แนวทางมาตรฐาน: **fake แทน mock**, `MainDispatcherRule`, **Turbine**,
  เทสต์ที่พฤติกรรม (black-box)

**ก้าวต่อไป**
- GitHub Actions (CI): รัน build + test + validate Gradle wrapper อัตโนมัติทุก PR
- (ตัวเลือก) ใช้ assertion library เช่น Truth

---

# ขอบคุณครับ / Q&A
