- Preon: 定义一个 POJO 类，并加上 @Bound 注解来描述二进制布局。它会自动生成编解码逻辑
- 基于 DSL 脚本：JBBP (Java Binary Block Parser)
-  Java 21+ 原生黑科技：Project Panama (MemoryLayout)
  ```java
    import java.lang.foreign.*;
    import static java.lang.foreign.ValueLayout.*;
    
    // 声明式定义内存布局
    GroupLayout modbusLayout = MemoryLayout.structLayout(
        JAVA_SHORT.withOrder(ByteOrder.BIG_ENDIAN).withName("transId"),
        JAVA_SHORT.withOrder(ByteOrder.BIG_ENDIAN).withName("protoId"),
        JAVA_SHORT.withOrder(ByteOrder.BIG_ENDIAN).withName("length"),
        JAVA_BYTE.withName("unitId"),
        JAVA_BYTE.withName("funcCode")
        // Payload 需要手动处理，因为是变长的
    );
    
    // 解析 (基于 MemorySegment)
    MemorySegment segment = MemorySegment.ofArray(byteArray);
    short tId = (short) modbusLayout.varHandle(PathElement.groupElement("transId")).get(segment);
  ```
- 工业级大杀器：Kaitai Struct (跨语言) 配合在线IDE https://ide.kaitai.io/
