# 工业 CAD 转 Unity：开源免费工具链指南

Pixyz 确实是行业标杆，但价格昂贵。如果你想减少成本，完全可以用 **FreeCAD + Blender** 搭建一套免费且强大的自动化处理管线。

虽然这一套方案没有 Pixyz 那么“傻瓜式一键化”，但它的上限极高，且完全可控。

---

## 1. 核心思路：两步走

工业模型（NURBS / B-Rep）不能直接进 Unity，必须经历两个过程：
1.  **Tessellation (镶嵌/网格化)**：把数学曲面变成三角形面片 (CAD -> High Poly Mesh)。
2.  **Decimation (减面/LOD)**：把几亿个三角形变成几万个，同时保持外形不变 (High Poly -> Low Poly)。

---

## 2. 工具选型 (The Open Source Stack)

### A. 格式转换层：FreeCAD (基于 OpenCascade)
*   **作用**：替代 Pixyz 的“导入”功能。
*   **能力**：能完美读取 **STEP (.stp), IGES (.igs), BREP** 等通用工业格式。
*   **用法**：
    *   打开 STEP 文件。
    *   选中部件 -> File -> Export -> 导出为 **OBJ** 格式。
    *   **关键技巧**：在导出设置中，你可以控制“精度 (Deviation)”。精度越低，生成的面越少，但圆孔可能会变成多边形。

### B. 减面优化层：Blender
*   **作用**：替代 Pixyz 的“优化”功能。
*   **能力**：拥有业界最强的 **Decimate (减面)** 修改器。
*   **核心操作 (Decimate Modifier)**：
    1.  导入 FreeCAD 导出的 OBJ。
    2.  添加 `Decimate` 修改器。
    3.  选择 `Collapse` (塌陷) 模式。
    4.  调整 `Ratio` (比率)。例如设为 `0.1`，就是只保留 10% 的面数。Blender 会自动计算哪些角是平的（可以删面），哪些是利用率高的（不仅要保留还要加面）。

### C. 自动化层：Python 脚本
Pixyz 的卖点是自动化。其实 **FreeCAD 和 Blender 都内置了极其强大的 Python API**。
你可以写一个 Python 脚本，实现“Headless”处理：
*   **脚本逻辑**：`遍历文件夹` -> `调 FreeCAD 转 OBJ` -> `调 Blender 减面` -> `导出 FBX`。
*   这样你就可以批量处理几千个零件，完全不需要人工点点点。

---

## 3. 面向 BIM (建筑) 的特殊方案：IfcOpenShell
如果你是做建筑（Revit / IFC），FreeCAD 可能不够好用。
*   **开源神器**：**IfcOpenShell (IfcConvert)**
*   **作用**：命令行工具，直接把 `.ifc` 文件转换成 `.obj` 或 `.dae`。
*   **命令**：`IfcConvert source.ifc target.obj --use-element-guids`

---

## 4. 方案对比：Pixyz vs 开源方案

| 维度 | Pixyz (付费) | 开源方案 (FreeCAD + Blender) |
| :--- | :--- | :--- |
| **面数控制** | **智能**。能根据“视觉重要性”自动减面。 | **半手动**。需要调整 Decimate 参数，有时会把螺丝孔减没了。 |
| **拓扑修复** | **强**。自动补面、修复法线反转。 | **弱**。Blender 有 `Remesh`，但处理烂面有时很头疼。 |
| **UV 展开** | **自动**。有一键展 UV 功能。 | **手动**。Blender 的 Smart UV Project 需要人工点一下。 |
| **BIM 属性** | **保留**。能把材质、元数据一起带过去。 | **丢失**。转成 OBJ 后，元数据通常就丢了，只剩几何体。 |

### 总结建议
**“FreeCAD (读) + Blender (减)”** 是目前最成熟的开源路径。
*   如果你的模型是**机械零件**（齿轮、发动机），这套流程效果非常好。
*   如果你的模型是**全要素建筑**（带管线、BIM信息），且必须保留属性，开源方案会比较折腾，这时候 Pixyz 或 Unity Reflect 的钱可能还是得花。
