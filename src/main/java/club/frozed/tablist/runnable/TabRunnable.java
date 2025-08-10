package club.frozed.tablist.runnable;

import club.frozed.tablist.adapter.TabProvider;
import club.frozed.tablist.entry.TabEntry;
import club.frozed.tablist.latency.TabLatency;
import club.frozed.tablist.layout.TabLayout;
import club.frozed.tablist.skin.Skin;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Ryzeon, Elb1to
 */
@AllArgsConstructor
public class TabRunnable implements Runnable {

	private final TabProvider provider;

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (TabLayout.getLayoutMapping().containsKey(player.getUniqueId())) {
				TabLayout layout = TabLayout.getLayoutMapping().get(player.getUniqueId());

				for (TabEntry entry : provider.getLines(player)) {
					layout.update(entry.getColumn(), entry.getRow(), entry.getText(), entry.getPing(), entry.getSkin());
				}

				for (int row = 0; row < 20; row++) {
					for (int column = 0; column < 3; column++) {
						if (layout.getByLocation(provider.getLines(player), column, row) == null) {
							layout.update(column, row, "", TabLatency.NO_BAR.getValue(), Skin.DEFAULT_SKIN);
						}
					}
				}

				layout.setHeaderAndFooter();
			}
		}
	}
}
