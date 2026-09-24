Rebirth Hourglass 2.0.0 / Minecraft 26.3 验证记录

验证日期：2026-09-24。

环境：Windows，Java 25.0.3，Minecraft 26.3，NeoForge 26.3.0.16-beta，Gradle 9.2.1，ModDevGradle 2.0.147。

执行 `build runGameTestServer` 成功。15 项 JUnit 单元测试、10 项本模组服务端 GameTest 全部通过；新版服务器还会执行内置的空测试，因此日志总数为 11。

服务端测试覆盖：

1. 多个储时不足沙漏和副手沙漏均能保留，未保护的普通物品正常掉落。
2. 死亡保护恢复主物品栏、护甲及副手，只扣一次费用；再次恢复不会复制物品或经验。
3. 附魔后以当前等级与经验条计算经验；keepInventory 不额外收费或叠加经验。
4. 后续监听器取消死亡时，不清空背包、不消耗储时。
5. 满背包保留尚未领取的物品，腾出空间后恢复。
6. MapCodec 经注册表感知的 NBT 编解码，保留物品组件、维度、经验和 long 时间戳，并复制可变数据。
7. 跨维度返回接受恰好足够的储时；不安全落点保留储时及返回资格。
8. 同维度返回到目标位置，并仅消费一次费用和返回资格。

9. 26.3 普通 dropAll 在非死亡流程中仍正常生成主物品栏与副手掉落；死亡保护收集的物品不会同时出现在地面，未保护的钻石恰好掉落一次。

本分支额外包含两个 Mixin，处理 26.3 的 Inventory.dropAll 和 EntityEquipment.dropAll 直接生成实体、绕过 NeoForge 死亡掉落收集缓冲区的变化。仅在玩家已有本模组死亡准备记录且正在收集掉落时，将真实掉落实体送回 LivingDropsEvent。普通掉落继续执行原调用，不复制库存快照。原始适配的两项死亡测试失败，修复后与新增普通掉落检查一起通过。

迁移包括 Java 25、Identifier、新版物品提示与客户端物品模型定义、背包装备槽位 API、玩家消息 API、游戏规则 API，以及注册表与数据包形式的 GameTest。

正式 JAR 不包含测试类、测试结构或测试实例；包含物品模型、动画贴图、中英文文本和配方。沙漏容量仍为 24 分钟，死亡保护默认 6 分钟、返还当前经验 50%，返回费用在 5 分钟内由 6 分钟线性衰减到零。

首轮测试服务器启动时可能记录尚不存在的 `server.properties`，随后生成默认配置并完成测试。编译时的原版过时 API 提示不影响本次构建。

未进行图形客户端人工游玩、第三方墓碑／复活／额外库存模组组合测试，也未实现旧 Forge 存档数据迁移或 Curios 适配器。GitHub Actions 的结果以仓库对应分支的运行记录为准。

参考：

- [NeoForge 26.1 迁移说明](https://neoforged.net/news/26.1release/)
- [数据附件](https://docs.neoforged.net/docs/datastorage/attachments/)
- [新版 GameTest](https://docs.neoforged.net/docs/misc/gametest/)
- [NeoForge 版本清单](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml)
