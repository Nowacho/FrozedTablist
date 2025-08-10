package club.frozed.tablist.adapter;

import club.frozed.tablist.entry.TabEntry;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Ryzeon, Elb1to
 */
public interface TabProvider {

	/**
	 * Get the tab header lines for a player.
	 *
	 * @param player the player
	 * @return list of strings for header lines
	 */
	List<String> getHeader(Player player);

	/**
	 * Get the tab footer lines for a player.
	 *
	 * @param player the player
	 * @return list of strings for footer lines
	 */
	List<String> getFooter(Player player);

	/**
	 * Get the tab lines for a player.
	 *
	 * @param player the player
	 * @return list of tab entries
	 */
	List<TabEntry> getLines(Player player);
}
