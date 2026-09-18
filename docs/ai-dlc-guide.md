# AI-Driven Development Lifecycle (AI-DLC) — ฉบับเข้าใจง่าย

เอกสารนี้สรุป **AI-DLC** แบบเข้าใจง่ายและนำไปใช้ได้จริง พร้อมตัวอย่างที่อิงกับแอป
**FoodOrder** ในโปรเจกต์นี้

> AI-DLC เป็นแนวทาง/เมธอดพัฒนาซอฟต์แวร์ที่ AWS เสนอในปี 2025 โดยเปลี่ยนบทบาท AI จาก
> "ผู้ช่วยเขียนโค้ด" ให้เป็น **"ผู้ขับเคลื่อน/ออร์เคสเตรตกระบวนการพัฒนา"** ส่วนมนุษย์รับผิดชอบ
> **บริบท การตัดสินใจสำคัญ และการตรวจสอบ**
> _(เนื้อหาเรียบเรียงใหม่จากแหล่งอ้างอิงท้ายเอกสารเพื่อให้สอดคล้องกับข้อกำหนดลิขสิทธิ์)_

---

## 1. AI-DLC คืออะไร (แก่นความคิด)

หัวใจของ AI-DLC มี 2 ข้อ:
- **AI นำกระบวนการ (AI-led):** AI ช่วยวางแผน ออกแบบ เขียนโค้ด เขียนเทสต์ และงานปฏิบัติการ
  ในทุกช่วงของวงจร ไม่ใช่แค่ช่วยเติมโค้ดทีละบรรทัด
- **มนุษย์เป็นศูนย์กลางการตัดสินใจ (human-centric):** คนให้ **บริบท** ที่ AI ไม่รู้,
  **ตรวจสอบ (validate)** ผลลัพธ์, และ **ตัดสินใจเรื่องสำคัญ** ที่มีผลกระทบสูง

เป้าหมาย: ได้ทั้ง **ความเร็ว (velocity)** และ **คุณภาพ (quality)** ไปพร้อมกัน

---

## 2. ต่างจากของเดิมอย่างไร

| แบบเดิม | "AI เป็นผู้ช่วย" (autocomplete) | **AI-DLC** |
|---------|-------------------------------|------------|
| คนทำเกือบทุกขั้น | คนนำ AI เติมโค้ดให้บางส่วน | **AI นำทั้งวงจร** คนกำกับ/ตัดสิน |
| AI แตะเฉพาะตอนเขียนโค้ด | เน้นเฉพาะการเขียนโค้ด | ครอบคลุม **วางแผน→ออกแบบ→สร้าง→ปฏิบัติการ** |
| ตรวจงานท้ายสุด | ตรวจเป็นครั้ง ๆ | มี **จุดตรวจ (gate)** ให้คนอนุมัติทุกช่วง |

จุดสำคัญ: AI-DLC ไม่ได้แปลว่า "ปล่อยให้ AI ทำเองทั้งหมด" แต่คือ **AI ทำงานหนัก + คน
ตัดสินใจที่จุดสำคัญ**

---

## 3. 3 เฟสหลักของ AI-DLC

ตามเอกสารเมธอดของ AWS วงจรแบ่งเป็น **3 เฟส** โดยแต่ละเฟสมี "จุดตรวจ/อนุมัติ" คั่นก่อนไปต่อ

```
Inception  →  Construction  →  Operations
(วางแผน)      (ออกแบบ+สร้าง)     (ปล่อย+ดูแล)
   │  gate         │  gate           │  gate
   └── คนอนุมัติ ───┴── คนอนุมัติ ─────┘
```

### เฟส 1: Inception (เริ่มต้น — วางแผน/สถาปัตยกรรม)
- **AI ทำ:** ช่วยแตกความต้องการเป็น requirement ชัด ๆ, เสนอสถาปัตยกรรม/ทางเลือก,
  ประเมินความเสี่ยง, ร่างแผนงาน
- **คนทำ:** ให้บริบททางธุรกิจ, เลือกทางเลือก, อนุมัติขอบเขต
- **ผลลัพธ์:** requirement + แผน + สถาปัตยกรรมที่ตกลงกันแล้ว

### เฟส 2: Construction (สร้าง — ออกแบบ/เขียนโค้ด)
- **AI ทำ:** ออกแบบรายละเอียด, เขียนโค้ด, เขียนเทสต์, refactor, ทำเอกสาร
- **คนทำ:** รีวิว design/โค้ด, ตัดสินใจ trade-off, ตรวจว่าตรงเจตนา
- **ผลลัพธ์:** โค้ดที่ทำงานได้ + เทสต์ + เอกสาร

### เฟส 3: Operations (ปฏิบัติการ — ปล่อย/ดูแล)
- **AI ทำ:** ช่วยตั้ง CI/CD, สคริปต์ deploy, monitoring/alert, วิเคราะห์ปัญหา
- **คนทำ:** อนุมัติการ deploy production, ตัดสินใจเรื่องความเสี่ยง/rollback
- **ผลลัพธ์:** ระบบที่ปล่อยจริง + การเฝ้าระวัง

> หมายเหตุ: มีเวอร์ชัน workflow แบบขยายที่ AWS เปิดซอร์ส ซึ่งซอย 3 เฟสนี้ออกเป็นหลาย
> stage ย่อยพร้อม verification gate ที่ชัดเจนขึ้น (ดูแหล่งอ้างอิง)

---

## 4. Workflow profiles (เลือกความหนักให้เหมาะกับงาน)

AI-DLC ไม่บังคับให้ทุกงานผ่านวงจรเต็มรูปแบบ — มี **โปรไฟล์** ให้เลือกตามชนิดงาน เช่น
- **Full feature** — ฟีเจอร์ใหญ่ ผ่านครบทุกเฟส
- **Quick bugfix** — แก้บั๊กเล็ก ข้ามขั้นที่ไม่จำเป็น
- **Infrastructure change** — งานโครงสร้างพื้นฐาน
- **Express** — งานเล็กมาก ทำแบบเบา ๆ

หลักคิด: **scope เป็นตัวกำหนดว่าจะทำ stage ไหนบ้าง** ไม่ต้องทำครบทุกอย่างเสมอ

---

## 5. ตัวอย่างจริง: เพิ่มฟีเจอร์ "ประวัติการสั่งอาหาร" ใน FoodOrder

สมมติจะเพิ่มหน้าจอ "ประวัติการสั่งอาหาร" ในแอป FoodOrder โดยใช้ AI-DLC

### เฟส 1 — Inception
**Prompt ให้ AI:** "ช่วยแตกความต้องการของฟีเจอร์ประวัติการสั่งอาหาร + เสนอสถาปัตยกรรม
บนโครง MVVM ที่มีอยู่"

AI เสนอ:
- Requirement: เก็บรายการที่สั่ง, แสดงย้อนหลัง, ล้างประวัติได้
- สถาปัตยกรรม: เพิ่ม `OrderHistoryRepository` (Room), `OrderHistoryViewModel`,
  `OrderHistoryFragment`

**คนตัดสินใจ (gate):** "เอา Room, เก็บ 30 รายการล่าสุดพอ, ไม่ต้อง sync cloud" → อนุมัติ

### เฟส 2 — Construction
**Prompt:** "สร้าง entity/DAO/Repository + ViewModel + Fragment ตามที่ตกลง พร้อม unit test"

AI สร้าง (ตัวอย่างย่อ):
```kotlin
@Entity(tableName = "order_history")
data class OrderRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val total: String,          // BigDecimal เก็บเป็น String
    val placedAt: Long
)

class OrderHistoryViewModel(
    private val repo: OrderHistoryRepository
) : ViewModel() {
    val history: StateFlow<List<OrderRecord>> = repo.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```
+ เขียนเทสต์ `OrderHistoryViewModelTest` ด้วย fake repository

**คนรีวิว (gate):** ตรวจว่า design ตรงเจตนา, โค้ดสะอาด, เทสต์ครอบคลุม → อนุมัติ merge

### เฟส 3 — Operations
**Prompt:** "ตั้ง GitHub Actions ให้รัน unit test ทุก PR + เพิ่มบันทึกใน CHANGELOG"

AI สร้าง workflow CI + อัปเดตเอกสาร

**คนอนุมัติ (gate):** ตรวจ CI เขียว → กด merge → ปล่อยเวอร์ชันใหม่

> สังเกต: **AI ทำงานหนักในทุกเฟส แต่คนอนุมัติที่ทุก gate** — นี่คือหัวใจของ AI-DLC

---

## 6. เชื่อมกับเครื่องมือจริง

- AI-DLC ใช้ได้กับผู้ช่วย AI ที่ทำงานเป็น agent ได้ เช่น **Amazon Q Developer** หรือ IDE
  แบบ agentic
- **โปรเจกต์นี้เอง** ก็พัฒนาด้วยแนวทางคล้าย AI-DLC: มีการแตก requirement, ตัดสินใจ
  design (เช่นเลือก StateFlow/SavedStateHandle), ให้ AI เขียนโค้ด+เทสต์+เอกสาร แล้ว
  **คนอนุมัติทีละขั้น** (ผ่านการ review/commit) และมี `CHANGELOG.md` เป็นบันทึกทุกการเปลี่ยนแปลง

---

## 7. แนวทางใช้ให้ได้ผล (best practices)

- **ให้บริบทเยอะ ๆ กับ AI** — เป้าหมายทางธุรกิจ, ข้อจำกัด, มาตรฐานทีม (AI ไม่รู้เอง)
- **อย่าข้าม gate** — ตรวจ/อนุมัติทุกเฟส โดยเฉพาะการตัดสินใจที่กระทบสูง (สถาปัตยกรรม,
  security, production)
- **เลือก profile ให้เหมาะงาน** — งานเล็กไม่ต้องผ่านวงจรเต็ม
- **ให้ AI เขียนเทสต์คู่กับโค้ดเสมอ** — คุณภาพต้องมาพร้อมความเร็ว
- **เก็บ artifact** — requirement, design, decision log, CHANGELOG เพื่อย้อนดูได้
- **คนรับผิดชอบผลลัพธ์สุดท้าย** — AI ช่วยได้ แต่การตัดสินใจสำคัญและความถูกต้องเป็นของคน

## 8. ข้อควรระวัง

- **อย่าเชื่อ AI 100%** — ตรวจสอบ logic/ความปลอดภัย/ผลข้างเคียงเสมอ
- **เรื่อง production ต้องรอบคอบเป็นพิเศษ** — deploy, ข้อมูลผู้ใช้, security ควรมีคนยืนยัน
- **บริบทจำกัด** — AI อาจไม่รู้กฎภายในองค์กร/ข้อมูลลับ ต้องป้อนให้ชัด

---

## สรุปสั้น

- **AI-DLC = AI นำกระบวนการทั้งวงจร + คนตัดสินใจที่จุดสำคัญ**
- 3 เฟส: **Inception → Construction → Operations** มี gate ให้คนอนุมัติทุกช่วง
- เลือก **workflow profile** ตามขนาดงาน
- ได้ทั้ง **ความเร็ว + คุณภาพ** ถ้าคนยังคุมบริบทและการตรวจสอบ

---

## แหล่งอ้างอิง

- [Building with AI-DLC using Amazon Q Developer (AWS DevOps Blog)](https://aws.amazon.com/blogs/devops/building-with-ai-dlc-using-amazon-q-developer/)
- [Open-Sourcing Adaptive Workflows for AI-DLC (AWS DevOps Blog)](https://aws.amazon.com/blogs/devops/open-sourcing-adaptive-workflows-for-ai-driven-development-life-cycle-ai-dlc/)
- [AI-DLC Workflows — awslabs](https://awslabs.github.io/aidlc-workflows/)
- [AIDLC Collaborative Documentation — aws-samples](https://aws-samples.github.io/sample-collaborative-ai-dlc/getting-started/methodology/)
- [What is the AI-Driven Development Lifecycle? — Jellyfish](https://jellyfish.co/library/ai-driven-development-lifecycle/)

> เนื้อหาในเอกสารนี้ถูกเรียบเรียง/สรุปใหม่จากแหล่งข้างต้นเพื่อให้เข้าใจง่ายและสอดคล้องกับ
> ข้อกำหนดด้านลิขสิทธิ์ รายละเอียดล่าสุดของเมธอดควรอ้างอิงจากเอกสารทางการของ AWS
