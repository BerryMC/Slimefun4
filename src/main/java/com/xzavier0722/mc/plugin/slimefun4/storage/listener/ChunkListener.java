package com.xzavier0722.mc.plugin.slimefun4.storage.listener;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ChunkDataLoadMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class ChunkListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        Slimefun.getDatabaseManager().getBlockDataController().loadChunk(e.getChunk(), e.isNewChunk());
    }

    /**
     * 在区块卸载后驱逐该区块的 Slimefun 数据缓存, 释放其占用的内存。
     * <p>
     * 仅在 {@link ChunkDataLoadMode#LOAD_WITH_CHUNK} 模式下生效:
     * 该模式下数据按区块按需加载, 因此缓存随区块卸载回收是安全的,
     * 区块重新加载时会由 {@link #onChunkLoad(ChunkLoadEvent)} 从数据库恢复。
     * {@link ChunkDataLoadMode#LOAD_ON_STARTUP} 模式刻意让数据常驻内存, 不做驱逐。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent e) {
        var controller = Slimefun.getDatabaseManager().getBlockDataController();
        if (controller == null
                || controller.isDestroyed()
                || !controller.isChunkDataEvictionEnabled()
                || controller.getChunkDataLoadMode() != ChunkDataLoadMode.LOAD_WITH_CHUNK) {
            return;
        }

        controller.unloadChunkData(e.getChunk());
    }

    /**
     * 在世界卸载时整体清理该世界的 Slimefun 区块数据缓存, 包括那些"已缓存但从未加载过区块"
     * 因而不会触发 {@link ChunkUnloadEvent} 的条目。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent e) {
        var controller = Slimefun.getDatabaseManager().getBlockDataController();
        if (controller == null || controller.isDestroyed()) {
            return;
        }

        controller.unloadWorldChunkData(e.getWorld());
    }
}
