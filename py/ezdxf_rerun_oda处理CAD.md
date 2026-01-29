### 使用exdxf 提取cad中的所有line 并使用 rerun 渲染
**井工矿中地测科索要导线点坐标，可能涉密 **
1. 使用 ODA file converter 将 dwg 转为dxf(注意一般格式需要选择 rx2000_ascii 或 rx2010_2000)
2. 在py项目中使用 uv 包管理工具安装 ezdxf 及 rerun
3. 示例代码
```python
import ezdxf
import rerun as rr


def main():
    dxf_file = r"D:\dxf\oda_output\x.dxf"
    # dxf_file = r"D:\dxf\oda_output\y.dxf"
    # dxf_file = r"D:\dxf\oda_output\z.dxf"
    doc = ezdxf.readfile(dxf_file)
    msp = doc.modelspace()
    lines = []
    for e in msp.query("LINE"):
        lines.append([e.dxf.start, e.dxf.end])
    # print(lines)

    if lines:
        rr.init("my_app_name")  # 若加了 spawn=true，则会自动打开reruun
        rr.log("world/tunnel_lines", rr.LineStrips3D(lines, colors=[[255, 0, 0]]))
        rr.save(
            r"D:\dxf\oda_output\x.rrd"
        )  # 保存为rerun文件，方便后续查阅,若本地调试则不需要rr.save 直接 rr.init添加 spawn即可


if __name__ == "__main__":
    main()

```
   
