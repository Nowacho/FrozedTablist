package club.frozed.tablist;

import club.frozed.tablist.adapter.TabAdapter;
import club.frozed.tablist.layout.TabLayout;
import club.frozed.tablist.listener.TabListener;
import club.frozed.tablist.packet.TabPacketListener;
import club.frozed.tablist.runnable.TabRunnable;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Ryzeon, Elb1to
 */
@Getter
public class FrozedTablist {

	private final TabAdapter adapter;

	public FrozedTablist(JavaPlugin plugin, TabAdapter adapter, int delay, int period) {
		this.adapter = adapter;

		PacketEvents.getAPI().getEventManager().registerListener(new TabPacketListener(), PacketListenerPriority.NORMAL);

		plugin.getServer().getPluginManager().registerEvents(new TabListener(this), plugin);
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new TabRunnable(adapter), delay, period);
	}

	public void onDisable() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			removePlayer(player);
		}
	}

	public void removePlayer(Player player) {
		boolean continueAt = TabLayout.getLayoutMapping().containsKey(player.getUniqueId());
		if (continueAt) {
			TabLayout.getLayoutMapping().remove(player.getUniqueId());
		}
	}
}
