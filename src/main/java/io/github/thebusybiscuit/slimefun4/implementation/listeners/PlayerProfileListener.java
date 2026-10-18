package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This {@link Listener} removes a {@link PlayerProfile} from memory if the corresponding {@link Player}
 * has left the {@link Server} or was kicked.
 *
 * @author TheBusyBiscuit
 * @author SoSeDiK
 *
 */
public class PlayerProfileListener implements Listener {

    public PlayerProfileListener(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDisconnect(PlayerQuitEvent e) {
        var uuid = e.getPlayer().getUniqueId();
        Optional<PlayerProfile> profile = PlayerProfile.find(e.getPlayer());

        profile.ifPresent(p -> {
            p.markForDeletion();

            // 落盘未保存的 waypoint 等数据, 避免直接丢弃内存档案造成数据丢失
            if (p.isDirty()) {
                p.save();
            }
        });

        Slimefun.getDatabaseManager().getProfileDataController().invalidateCache(uuid.toString());

        // 数据已落盘(或本无脏数据), 从全局档案注册表移除该玩家, 立即释放内存, 无需等待自动保存周期
        profile.ifPresent(p -> Slimefun.getRegistry().getPlayerProfiles().remove(uuid, p));
    }
}
