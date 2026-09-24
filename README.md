Rebirth Hourglass / 重生沙漏

版本与分支：

| 分支 | Minecraft | NeoForge | 用途 |
|---|---|---|---|
| [`main`](https://github.com/uuzsx/RebirthHourglass/tree/main) | 1.21.1 | 21.1.251 | 默认分支、主要维护版本 |
| [`1.21.2`](https://github.com/uuzsx/RebirthHourglass/tree/1.21.2) | 1.21.2 | 21.2.1-beta | 版本适配分支 |
| [`26.1.1`](https://github.com/uuzsx/RebirthHourglass/tree/26.1.1) | 26.1.1 | 26.1.1.15-beta | 版本适配分支 |
| [`26.1.2`](https://github.com/uuzsx/RebirthHourglass/tree/26.1.2) | 26.1.2 | 26.1.2.109 | 版本适配分支 |
| [`26.2`](https://github.com/uuzsx/RebirthHourglass/tree/26.2) | 26.2 | 26.2.0.88 | 版本适配分支 |
| [`26.3`](https://github.com/uuzsx/RebirthHourglass/tree/26.3) | 26.3 | 26.3.0.16-beta | 版本适配分支 |
| [`26.4`](https://github.com/uuzsx/RebirthHourglass/tree/26.4) | 26.4 | 尚未发布 | 仅预留分支，未支持、无安装包 |

后续所有其他 Minecraft 版本均使用独立版本分支；`main` 保持 1.21.1。通用修复先进入 `main`，再移植到需要的版本分支。下载或编译前请确认分支与游戏版本一致。

当前分支面向 Minecraft **26.1.1 + NeoForge 26.1.1.15-beta + Java 25** 的重新实现。模组 ID 为 `rebirth_hourglass`。需要在客户端和服务端同时安装。

源码使用物品 Data Components 保存储时、NeoForge Data Attachments 与 MapCodec 保存玩家恢复记录。沿用旧项目的沙漏动画贴图和基础配方，业务实现重新编写。

玩法：

- 四颗钻石围绕一个不死图腾，合成一个沙漏。
- 玩家存活、在线且携带沙漏时，每 20 游戏刻增加 1 秒储时。默认上限 **24 分钟**，离线不充能；服务器低 TPS 时充能随游戏刻变慢。
- 沙漏不可堆叠，支持主物品栏与副手。多个沙漏独立充能；一次死亡只使用储时最多且足够支付费用的一个，同储时按库存/槽位顺序选择。
- 死亡保护默认花费 6 分钟，保护原版主物品栏、护甲和副手，并返还死亡时**当前剩余经验点数**的 50%。剩余经验丢失，不再额外生成经验球。
- 没有沙漏达到保护费用时，默认仍保留所有实际掉落的沙漏，其他物品正常掉落。消失诅咒仍按原版规则处理。
- 开启 keepInventory 时，由原版保留物品与经验，沙漏不额外扣费或增加经验。
- 成功保护的死亡会留下一个属于玩家、可使用一次的返回地点。下一次实际死亡会清除旧地点；如果新死亡受到保护，则替换为新地点。keepInventory 下，携带符合保护储时要求的沙漏也能记录返回点。
- 右键空气或普通方块返回，支持跨维度。费用默认从 6 分钟线性下降，在死亡后的 5 分钟世界运行时间内降到 0；服务器停止时不计时，玩家离线而服务器继续运行时会继续衰减。
- 储时恰好等于实际费用也可以使用。传送成功后才扣费、消费返回资格；维度不存在、传送被取消或没有安全落点时，保留资格和储时。
- 落点在死亡位置周围水平 8 格、垂直 16 格内搜索，检查地面、碰撞、世界边界、液体和常见伤害方块。找不到落点就明确失败。
- 背包空间不足时，尚未返还的物品保存在玩家数据中，腾出空间后再返还；不会覆盖其他模组发放的物品。恢复记录可跨死亡、保存和重新登录。
- 模组不取消玩家死亡，不会绕过极限模式规则。

服务端配置位于世界目录的 `serverconfig/rebirth_hourglass-server.toml`，由服务器同步给客户端：

| 配置项 | 默认值 | 含义 |
|---|---:|---|
| capacitySeconds | 1440 | 最大储时，秒 |
| deathCostSeconds | 360 | 单次死亡保护费用，秒 |
| retainedExperienceFraction | 0.5 | 当前剩余经验的保留比例，0–1 |
| teleportCostSeconds | 360 | 返回地点的基础费用，秒 |
| teleportDecaySeconds | 300 | 费用降至零所需世界运行时间；0 表示固定费用 |
| safeLandingRadius | 8 | 安全落点水平搜索半径，格 |
| keepUnchargedHourglasses | true | 储时不足时是否仍保留沙漏 |

费用设置超过储时上限时，该收费功能可能不可用；传送费用仍可能随时间衰减至可支付。配置支持 100% 经验保留和固定传送费用，不需要改源码。

构建：

```text
Windows：设置 JAVA_HOME 指向 JDK 25，然后执行 gradlew.bat build
Linux/macOS：使用 JDK 25，然后执行 ./gradlew build
服务端集成测试：gradlew.bat runGameTestServer
开发客户端：gradlew.bat runClient
开发服务端：gradlew.bat runServer
```

主模组 JAR 输出到 `build/libs/`。文件名含 `sources` 的 JAR 是源码，不要作为模组安装。构建固定使用 Gradle 9.2.1、ModDevGradle 2.0.147、NeoForge 26.1.1.15-beta。

测试包含费用边界、经验分段公式，以及真实游戏服务端中的多沙漏、副手、护甲、死亡取消、keepInventory、恢复幂等性、存档序列化、满背包与跨维度传送。GameTest 类、测试结构和测试实例定义不会进入正式模组 JAR。首次运行需要下载 Minecraft 开发环境及资源。

此分支固定使用 NeoForge 26.1.1.15-beta（beta 构建）。

兼容性与范围：

- 严格针对 Minecraft 26.1.1；请使用对应分支的 JAR，不将它当成其他 Minecraft 版本的通用包。
- 当前未包含 Curios 适配器；沙漏放在原版主物品栏或副手时有效。
- 额外库存可以通过 `api.InventoryAdapters.register(Identifier, InventoryAdapter)` 接入。适配器负责实时读取、插入和返回未插入部分；不能重复暴露同一槽位。注册应在 common setup 时完成。
- 保护仅接管正常死亡掉落事件中、与死亡前库存匹配的物品；不会凭空复原已经被其他机制保存的物品。其他模组提前取消掉落事件时，本模组放弃接管并撤回自身对经验球的抑制。
- 墓碑、复活和修改掉落/经验的模组组合仍需单独验证。没有宣称对任意事件优先级和第三方私有库存流程通用兼容。
- 没有实现旧版 Forge Capability / ItemStack NBT 的自动数据迁移；请把它作为新版本使用，旧存档升级前应自行保留存档副本。

实现与验证记录见 `VALIDATION.md`。模组沿用 All Rights Reserved；MDK 模板许可保留在 `TEMPLATE_LICENSE.txt`。
