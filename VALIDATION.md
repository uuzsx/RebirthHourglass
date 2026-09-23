Rebirth Hourglass 2.0.0 验证记录

日期：2026-09-24。

环境：Windows，Java 21.0.8，Minecraft 1.21.2，NeoForge 21.2.1-beta，Gradle 9.2.1，ModDevGradle 2.0.147。

执行 `build runGameTestServer` 成功。

| 验证 | 结果 |
|---|---|
| Java 编译、资源处理、JAR 打包 | 通过 |
| ChargeRulesTest | 11 项通过 |
| ExperienceMathTest | 4 项通过 |
| Minecraft 服务端 GameTest | 9 项全部通过 |
| 最终一轮日志中的 ERROR / 资源解析错误 | 未发现 |

GameTest 使用 Minecraft 的服务端玩家和正常死亡/重生、存档及传送 API，覆盖：
1. 多个储时不足的沙漏，包括副手沙漏，均保留；普通物品正常掉落。
2. 充足与不足沙漏混排时，全部受保护物品恢复原槽位，护甲与副手保留，只扣一次死亡费用。
3. 附魔后按当前经验结算；再次调用恢复不会重复返还物品或经验。
4. keepInventory 不额外扣费、加经验或复制物品。
5. 本模组准备之后、其他监听器取消死亡，物品和储时保持不变，临时记录被清理。
6. 满背包下保留待领取物品；腾出空间后只返还一次。这也覆盖了原版 Inventory.add 在创造模式下吞掉未插入物品的边界。
7. 恢复记录序列化保留物品数据组件、维度、经验及超过 int 范围的时间戳，并独立复制列表。
8. 跨维度传送接受恰好足够的储时，成功后消耗地点；不安全落点不扣费、不清除地点。

9. 同维度返回到安全落点，恰好支付费用并消费返回资格。

注意：上面按测试目的展开，实际测试方法数为 9，部分方法覆盖多个检查点。

正式 JAR 排除了 GameTest 类和测试结构。源码工程保留测试、Gradle Wrapper 和 CI 工作流。每个版本分支均包含 GitHub Actions 构建、单元测试、服务端 GameTest 和 JAR 产物上传流程；远端运行结果以仓库 Actions 页面为准。

未进行图形客户端的人工游玩验收，也未测试墓碑、复活或额外库存 Mod 的组合。没有实现旧 Forge 存档数据迁移，未包含 Curios 适配器；这些不属于本次“已通过”的范围。

用户明确确认：最大储时 24 分钟。其余默认规则采用讨论中的推荐方案，并可在服务端配置里更改：死亡保护 6 分钟、当前经验返还 50%、传送初始费用 6 分钟且 5 分钟内线性降至零。

框架参考：
- [官方 1.21.2 MDK](https://github.com/NeoForgeMDKs/MDK-1.21.2-ModDevGradle/tree/438f902fa80ab5e30d1a7c35c3c1708ccc7e98f6)
- [NeoForge 1.21.2–1.21.3 数据组件](https://docs.neoforged.net/docs/1.21.3/items/datacomponents/)
- [NeoForge 1.21.2–1.21.3 数据附件](https://docs.neoforged.net/docs/1.21.3/datastorage/attachments/)
