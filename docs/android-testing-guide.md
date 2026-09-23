# คู่มือการทำ Test ใน Android (ฉบับเริ่มต้นจนถึง Production)

เอกสารนี้อธิบายการเขียน test บน Android แบบ **เข้าใจง่าย ทำตามได้จริง** สำหรับคนที่
**ไม่เคยเขียน test มาก่อน** ตั้งแต่พื้นฐานจนถึงการใช้ในโปรเจกต์จริง (production) พร้อม
โค้ดตัวอย่างที่อ้างอิงจากแอป **FoodOrder** ในโปรเจกต์นี้

> ทำไมต้องเขียน test? เพื่อ **จับบั๊กก่อนถึงผู้ใช้**, กล้าแก้โค้ด (refactor) โดยไม่พัง,
> และเป็น "เอกสารมีชีวิต" ที่บอกว่าโค้ดควรทำงานอย่างไร

---

## 1. ภาพรวม: test มีกี่แบบ

Android แบ่ง test เป็น 3 ระดับ (Test Pyramid) — เขียนระดับล่างเยอะสุด เพราะเร็วและถูก

```
        /\        UI tests (E2E)   ← ช้า, ต้องมีเครื่อง/emulator, เขียนน้อย
       /  \
      /----\      Integration      ← ปานกลาง
     /------\
    /--------\    Unit tests (JVM)  ← เร็วมาก, เขียนเยอะสุด ⬅ เริ่มที่นี่
```

| ระดับ | รันที่ไหน | โฟลเดอร์ | เร็ว | ใช้ทดสอบอะไร |
|-------|-----------|----------|:----:|--------------|
| **Unit test** | JVM (เครื่องคุณ) | `src/test/` | ⚡เร็วมาก | logic ล้วน ๆ (ViewModel, การคำนวณ) |
| **Instrumented / UI** | emulator/มือถือ | `src/androidTest/` | 🐢ช้า | UI, การกดปุ่ม, navigation, ฐานข้อมูลจริง |

**หลักการ:** เริ่มจาก unit test ก่อนเสมอ เพราะเขียนง่าย รันเร็ว ไม่ต้องมีอุปกรณ์

---

## 2. เตรียมโปรเจกต์ (ครั้งเดียว)

เพิ่ม dependencies ใน `app/build.gradle.kts`:

```kotlin
dependencies {
    // ----- Unit test (รันบน JVM) -----
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")   // ทดสอบ Flow ง่ายขึ้น

    // ----- Instrumented / UI test (รันบนอุปกรณ์) -----
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
```

> โปรเจกต์ FoodOrder ตั้งค่าพวกนี้ไว้แล้วผ่าน **version catalog** (`gradle/libs.versions.toml`)

**โครงโฟลเดอร์ test:**

```
app/src/
├── main/         โค้ดแอปจริง
├── test/         ← Unit test (JVM) เขียนที่นี่เป็นหลัก
└── androidTest/  ← UI/Instrumented test
```

---

## 3. Unit test ตัวแรก (แบบง่ายสุด)

เริ่มจากทดสอบ "logic ล้วน ๆ" ก่อน เช่นการคำนวณราคาใน `CartItem`

**โค้ดที่จะทดสอบ** (`main`):
```kotlin
data class CartItem(val menuItem: MenuItem, val quantity: Int) {
    val lineTotal: BigDecimal
        get() = menuItem.price.multiply(BigDecimal(quantity))
}
```

**เขียน test** (`app/src/test/java/.../CartItemTest.kt`):
```kotlin
class CartItemTest {

    @Test
    fun `lineTotal = ราคา คูณ จำนวน`() {
        // Arrange (เตรียม)
        val item = MenuItem(1, "ผัดกะเพรา", "เผ็ด", BigDecimal("55"))
        val cartItem = CartItem(item, quantity = 3)

        // Act (ลงมือ) + Assert (ตรวจผล)
        assertEquals(0, BigDecimal("165").compareTo(cartItem.lineTotal))
    }
}
```

**อธิบายทีละส่วน:**
- `@Test` = บอกว่าฟังก์ชันนี้เป็นเทสต์
- ชื่อฟังก์ชันใช้ **backtick** เขียนเป็นประโยคได้ อ่านง่าย
- โครง **AAA**: Arrange (เตรียมข้อมูล) → Act (เรียกสิ่งที่จะทดสอบ) → Assert (ตรวจว่าผลถูก)
- `assertEquals(expected, actual)` = ต้องเท่ากัน (BigDecimal ใช้ `compareTo` กันปัญหาทศนิยม)

---

## 4. รันเทสต์

**ใน Android Studio:** คลิกลูกศร ▶ ข้างชื่อ class/ฟังก์ชันเทสต์ หรือคลิกขวาโฟลเดอร์
`test` → Run

**Command line:**
```bash
# รัน unit test ทั้งหมด
./gradlew testDebugUnitTest

# รัน UI test (ต้องมี emulator/มือถือเสียบอยู่)
./gradlew connectedDebugAndroidTest
```

ผลลัพธ์ (รายงาน HTML): `app/build/reports/tests/`

---

## 5. ทดสอบ ViewModel (หัวใจของแอป MVVM)

ViewModel มี logic เยอะสุด → คุ้มที่จะเทสต์ แต่มี 2 อุปสรรคที่ต้องจัดการ:
1. ViewModel ใช้ **coroutines** (`viewModelScope`) — ต้องคุมเวลาในเทสต์
2. ViewModel พึ่ง **Repository** — ต้องใช้ตัวปลอม (fake) แทนของจริง

### 5.1 สร้าง "ตัวช่วย" 2 ตัว

**(ก) MainDispatcherRule** — สลับ `Dispatchers.Main` เป็นตัวทดสอบ (เพราะ Main ไม่มีจริงบน JVM):
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(d: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(d: Description) = Dispatchers.resetMain()
}
```

**(ข) FakeFoodRepository** — ตัวปลอมของ Repository คืนข้อมูลคงที่ (ใช้ **fake** ดีกว่า mock):
```kotlin
class FakeFoodRepository(
    private val menu: List<MenuItem> = DEFAULT_MENU
) : FoodRepository {
    override suspend fun getMenu(): List<MenuItem> = menu

    companion object {
        val ITEM_A = MenuItem(1, "ผัดกะเพรา", "เผ็ด", BigDecimal("55"))
        val DEFAULT_MENU = listOf(ITEM_A)
    }
}
```

### 5.2 เขียนเทสต์ ViewModel

```kotlin
class OrderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel() =
        OrderViewModel(FakeFoodRepository(), SavedStateHandle())

    @Test
    fun `addToCart เพิ่มจำนวนและคำนวณยอดรวมถูกต้อง`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()                    // รอโหลดเมนูเสร็จ

            viewModel.addToCart(FakeFoodRepository.ITEM_A)
            viewModel.addToCart(FakeFoodRepository.ITEM_A)

            val state = viewModel.uiState.value   // อ่าน state ปัจจุบัน
            assertEquals(2, state.cart.first().quantity)
            assertEquals(0, BigDecimal("110").compareTo(state.cartTotal))
        }
}
```

**จุดสำคัญ:**
- `@get:Rule` ใช้ MainDispatcherRule กับทุกเทสต์ในคลาส
- `runTest { }` = รัน coroutine ในสภาพแวดล้อมทดสอบ
- `advanceUntilIdle()` = สั่งให้ coroutine ที่ค้าง (เช่นโหลดเมนู) ทำงานจนจบก่อนตรวจผล

### 5.3 ทดสอบ event (Flow) ด้วย Turbine

เมื่อ ViewModel ส่ง event (เช่น "สั่งอาหารสำเร็จ") ผ่าน `Flow` ใช้ **Turbine** ทดสอบ:
```kotlin
@Test
fun `placeOrder ยิง event OrderPlaced`() =
    runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.addToCart(FakeFoodRepository.ITEM_A)

        viewModel.events.test {                 // Turbine
            viewModel.placeOrder()
            val event = awaitItem()
            assertTrue(event is OrderEvent.OrderPlaced)
            cancelAndConsumeRemainingEvents()
        }
    }
```

---

## 6. UI test (ทดสอบบนอุปกรณ์จริง)

เมื่อ logic แน่นแล้ว ค่อยเขียน UI test ทดสอบ "ผู้ใช้กดแล้วเกิดอะไร" ด้วย **Espresso**
(อยู่ใน `src/androidTest/`)

```kotlin
@RunWith(AndroidJUnit4::class)
class MenuFragmentTest {

    @Test
    fun กดปุ่มเพิ่มแล้วปุ่มดูตะกร้าอัปเดตจำนวน() {
        // เปิดหน้าจอ
        launchActivity<MainActivity>()

        // หา element แล้วโต้ตอบ
        onView(withId(R.id.buttonAdd)).perform(click())

        // ตรวจผลบนหน้าจอ
        onView(withId(R.id.buttonViewCart))
            .check(matches(withText(containsString("1"))))
    }
}
```

**สูตร Espresso จำง่าย:** `onView(ตัวชี้).perform(การกระทำ)` แล้ว `.check(matches(สิ่งที่คาดหวัง))`
- `withId(...)`, `withText(...)` = วิธีชี้ element
- `perform(click(), typeText(...))` = การกระทำ
- `check(matches(isDisplayed()))` = ตรวจผล

> UI test ช้าและเปราะกว่า unit test — เขียนเฉพาะ **flow สำคัญ** (เช่น เพิ่มของ → สั่งอาหาร)
> ไม่ต้องเทสต์ทุกปุ่ม

---

## 7. แนวทางที่ดี (best practices)

- **ตั้งชื่อเทสต์ให้อ่านรู้เรื่อง** เป็นประโยค (พฤติกรรมที่คาดหวัง)
- **1 เทสต์ = 1 พฤติกรรม** (assert เรื่องเดียวเป็นหลัก)
- ใช้ **fake แทน mock** เมื่อทำได้ (เปราะน้อยกว่า)
- **ทดสอบพฤติกรรม (black-box)** ไม่ใช่รายละเอียดภายใน — เปลี่ยนโค้ดข้างในแล้วเทสต์ไม่ควรพัง
- **อย่าใช้เวลา/สุ่ม/เครือข่ายจริง** ในเทสต์ — ฉีดตัวปลอมเข้าไปแทน
- เทสต์ต้อง **รันซ้ำได้ผลเดิมเสมอ** (deterministic)

---

## 8. เส้นทางสู่ production (นำไปใช้จริง)

ทำตามลำดับนี้จะได้ test ที่ใช้งานได้จริงในทีม:

1. **เริ่มเล็ก** — เขียน unit test ให้ logic สำคัญ (ViewModel, การคำนวณ) ก่อน
2. **เพิ่มทีละส่วน** — ทุกครั้งที่แก้บั๊ก ให้เขียนเทสต์ที่ "จับบั๊กนั้น" ไว้กันเกิดซ้ำ
3. **ตั้งเป้า coverage ที่สมเหตุผล** — เน้น logic สำคัญ ไม่ต้อง 100%
4. **รันอัตโนมัติด้วย CI** — ให้ทุก Pull Request รันเทสต์เอง (ดูตัวอย่าง GitHub Actions ด้านล่าง)
5. **บังคับผ่านก่อน merge** — ตั้ง branch protection: เทสต์ไม่ผ่าน = merge ไม่ได้
6. **UI test เฉพาะ flow หลัก** — รันบน CI ด้วย emulator เป็นระยะ (ช้า จึงไม่รันทุกครั้งก็ได้)

### ตัวอย่าง CI (GitHub Actions) — รัน unit test ทุก PR

สร้างไฟล์ `.github/workflows/android-ci.yml`:
```yaml
name: Android CI
on:
  pull_request:
  push:
    branches: [ main ]

jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest
```

- ทุกครั้งที่เปิด PR GitHub จะรัน `testDebugUnitTest` ให้อัตโนมัติ
- ถ้าเทสต์ล้ม PR จะขึ้นสถานะแดง → รู้ทันทีว่ามีอะไรพัง

---

## 9. เช็กลิสต์สำหรับมือใหม่

- [ ] เพิ่ม dependencies (junit, coroutines-test, turbine, espresso)
- [ ] เขียน unit test แรกให้ logic ง่าย ๆ (เช่นการคำนวณ) ให้รันผ่าน
- [ ] สร้าง `MainDispatcherRule` + fake repository
- [ ] เขียนเทสต์ ViewModel (เพิ่ม/ลบ/คำนวณ/event)
- [ ] เขียน UI test 1–2 ตัวสำหรับ flow หลัก
- [ ] ตั้ง CI ให้รันเทสต์ทุก PR
- [ ] เปิด branch protection ให้ต้องผ่านเทสต์ก่อน merge

---

## 10. อ้างอิงในโปรเจกต์นี้

โปรเจกต์ **FoodOrder** มีตัวอย่างจริงให้ดู:
- `app/src/test/java/com/example/foodorder/util/MainDispatcherRule.kt`
- `app/src/test/java/com/example/foodorder/data/repository/FakeFoodRepository.kt`
- `app/src/test/java/com/example/foodorder/ui/order/OrderViewModelTest.kt` (12 เคส)
- `app/src/test/java/com/example/foodorder/data/model/CartItemTest.kt`
- `app/src/test/java/com/example/foodorder/ui/order/OrderUiStateTest.kt`

รันดูได้ด้วย: `./gradlew testDebugUnitTest`
