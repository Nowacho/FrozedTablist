package club.frozed.tablist.layout;

import club.frozed.tablist.FrozedTablist;
import club.frozed.tablist.entry.TabEntry;
import club.frozed.tablist.skin.Skin;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static club.frozed.tablist.skin.Skin.TEXTURE_KEY;

/**
 * @author Elb1to, Biquaternions
 */
public class TabLayout {

	@Getter
	private static final Map<UUID, TabLayout> layoutMapping = Maps.newHashMap();
	private final Map<Integer, Integer> pingMapping = Maps.newHashMap();
	private final Map<Integer, GameProfile> profileMapping = Maps.newHashMap();
	private final Map<Integer, Skin> skinMapping = Maps.newHashMap();
	private final FrozedTablist instance;
	private final PacketEventsAPI<?> packetAPI = PacketEvents.getAPI();

	private final Player player;

	public TabLayout(FrozedTablist instance, Player player) {
		this.instance = instance;
		this.player = player;
	}

	public void setHeaderAndFooter() {
		boolean continueAt = this.packetAPI.getPlayerManager().getClientVersion(player).isNewerThanOrEquals(ClientVersion.V_1_8);
		if (continueAt) {
			List<String> headerLines = instance.getAdapter().getHeader(player);
			List<String> footerLines = instance.getAdapter().getFooter(player);

			String header = headerLines.stream()
					.map(line -> ChatColor.translateAlternateColorCodes('&', line))
					.collect(Collectors.joining("\n"));

			String footer = footerLines.stream()
					.map(line -> ChatColor.translateAlternateColorCodes('&', line))
					.collect(Collectors.joining("\n"));

			WrapperPlayServerPlayerListHeaderAndFooter packet = new WrapperPlayServerPlayerListHeaderAndFooter(
					Component.text(header),
					Component.text(footer)
			);

			this.packetAPI.getPlayerManager().sendPacket(player, packet);
		}
	}

	public void update(int column, int row, String text, int ping, Skin skin) {
		if (row > 19) throw new RuntimeException("Row is above 19 " + row);
		if (column > 4) throw new RuntimeException("Column is above 4 " + column);

		text = ChatColor.translateAlternateColorCodes('&', text);
		String prefix = text;
		String suffix = "";

		if (text.length() > 16) {
			prefix = text.substring(0, 16);

			if (prefix.charAt(15) == ChatColor.COLOR_CHAR) {
				prefix = prefix.substring(0, 15);
				suffix = text.substring(15);
			} else if (prefix.charAt(14) == ChatColor.COLOR_CHAR) {
				prefix = prefix.substring(0, 14);
				suffix = text.substring(14);
			} else {
				suffix = ChatColor.getLastColors(prefix) + text.substring(16);
			}
		}

		if (suffix.length() > 16) {
			suffix = suffix.substring(0, 16);
		}

		String teamName = "$" + getTeamAt(row, column);
		WrapperPlayServerTeams packet = new WrapperPlayServerTeams(
				teamName,
				WrapperPlayServerTeams.TeamMode.UPDATE,
				new WrapperPlayServerTeams.ScoreBoardTeamInfo(
						Component.text(teamName),
						Component.text(prefix),
						Component.text(suffix),
						WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
						WrapperPlayServerTeams.CollisionRule.NEVER,
						NamedTextColor.WHITE,
						WrapperPlayServerTeams.OptionData.NONE
				)
		);
		this.packetAPI.getPlayerManager().sendPacket(player, packet);

		int index = row + column * 20;
		GameProfile gameProfile = profileMapping.get(index);

		fetchPing(index, ping);
		fetchSkin(index, gameProfile, skin);
	}

	private void fetchPing(int index, int ping) {
		int lastConnection = pingMapping.get(index);
		if (Objects.equals(lastConnection, ping)) {
			return;
		}

		WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
				WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY,
				new WrapperPlayServerPlayerInfo.PlayerData(
						Component.text(player.getDisplayName()),
						new UserProfile(player.getUniqueId(), player.getName()),
						GameMode.ADVENTURE,
						ping
				)
		);

		this.packetAPI.getPlayerManager().sendPacket(player, packet);
	}

	private void fetchSkin(int index, GameProfile gameProfile, Skin skin) {
		boolean continueAt = this.packetAPI.getPlayerManager().getClientVersion(player).isNewerThanOrEquals(ClientVersion.V_1_8);
		if (!continueAt) {
			return;
		}

		if (skin == null) {
			skin = Skin.DEFAULT_SKIN;
		}

		Skin lastSkin = skinMapping.get(index);
		if (Objects.equals(skin, lastSkin)) {
			return;
		}

		GameProfile newGameProfile = new GameProfile(gameProfile.getId(), gameProfile.getName());
		newGameProfile.getProperties().put(TEXTURE_KEY, getSkinProperty(skin));

		WrapperPlayServerPlayerInfo.PlayerData playerData = new WrapperPlayServerPlayerInfo.PlayerData(
				Component.text(player.getDisplayName()),
				new UserProfile(player.getUniqueId(), player.getName()),
				GameMode.ADVENTURE,
				0
		);
		this.packetAPI.getPlayerManager().sendPacket(player, new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, playerData));
		this.packetAPI.getPlayerManager().sendPacket(player, new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, playerData));

		profileMapping.put(index, newGameProfile);
		skinMapping.put(index, skin);
	}

	private Property getSkinProperty(Skin skin) {
		return new Property(TEXTURE_KEY, skin.getValue(), skin.getSignature());
	}

	public void create() {
		WrapperPlayServerPlayerInfo packetInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY);
		List<WrapperPlayServerPlayerInfo.PlayerData> infoData = new ArrayList<>(Collections.emptyList());
		GameProfile gameProfile;

		for (int row = 0; row < 20; row++) {
			for (int column = 0; column < 3; column++) {
				int index = row + column * 20;
				Skin defualtSkin = Skin.DEFAULT_SKIN;
				skinMapping.put(index, defualtSkin);

				Property property = getSkinProperty(skinMapping.get(index));
				gameProfile = new GameProfile(UUID.randomUUID(), getTeamAt(row, column));
				gameProfile.getProperties().put(TEXTURE_KEY, property);

				infoData.add(new WrapperPlayServerPlayerInfo.PlayerData(
						Component.text(player.getDisplayName()),
						new UserProfile(gameProfile.getId(), gameProfile.getName()),
						GameMode.SURVIVAL,
						0
				));

				pingMapping.put(index, 0);
				profileMapping.put(index, gameProfile);
			}
		}

		for (int index = 60; index < 80; index++) {
			Skin defualtSkin = Skin.DEFAULT_SKIN;
			skinMapping.put(index, defualtSkin);

			Property property = getSkinProperty(skinMapping.get(index));

			gameProfile = new GameProfile(UUID.randomUUID(), getTeamAt(index));
			gameProfile.getProperties().put(TEXTURE_KEY, property);

			infoData.add(new WrapperPlayServerPlayerInfo.PlayerData(
					Component.text(player.getDisplayName()),
					new UserProfile(gameProfile.getId(), gameProfile.getName()),
					GameMode.SURVIVAL,
					0
			));

			pingMapping.put(index, 0);
			profileMapping.put(index, gameProfile);
		}

		Collection<String> players = Lists.newArrayList();
		for (Player other : Bukkit.getOnlinePlayers()) {
			players.add(other.getName());
		}

		Bukkit.getLogger().info("Sending info data to " + player.getName() + " with " + infoData.size() + " entries");
		packetInfo.setPlayerDataList(infoData);
		this.packetAPI.getPlayerManager().sendPacket(player, packetInfo);
		this.packetAPI.getPlayerManager().sendPacket(player, createServerTeams("tab", players));

		for (int row = 0; row < 20; row++) {
			for (int column = 0; column < 4; column++) {
				String teamName = "$" + getTeamAt(row, column);
				this.packetAPI.getPlayerManager().sendPacket(player, createServerTeams(teamName, Collections.singleton(getTeamAt(row, column))));
			}
		}

		WrapperPlayServerTeams scoreboardTeam = new WrapperPlayServerTeams(
				"team",
				WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
				new WrapperPlayServerTeams.ScoreBoardTeamInfo(
						Component.text("team"),
						Component.empty(),
						Component.empty(),
						WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
						WrapperPlayServerTeams.CollisionRule.NEVER,
						NamedTextColor.WHITE,
						WrapperPlayServerTeams.OptionData.NONE
				),
				player.getName()
		);

		for (Player target : Bukkit.getOnlinePlayers()) {
			this.packetAPI.getPlayerManager().sendPacket(target, scoreboardTeam);
		}
	}

	private @NotNull WrapperPlayServerTeams createServerTeams(String teamName, Collection<String> players) {
		return new WrapperPlayServerTeams(
				teamName,
				WrapperPlayServerTeams.TeamMode.CREATE,
				new WrapperPlayServerTeams.ScoreBoardTeamInfo(
						Component.text(teamName),
						Component.empty(),
						Component.empty(),
						WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
						WrapperPlayServerTeams.CollisionRule.NEVER,
						NamedTextColor.WHITE,
						WrapperPlayServerTeams.OptionData.NONE
				),
				players
		);
	}

	public TabEntry getByLocation(List<TabEntry> entries, int column, int row) {
		for (TabEntry entry : entries) {
			if (entry.getColumn() == column && entry.getRow() == row) {
				return entry;
			}
		}

		return null;
	}

	private String getTeamAt(int row, int column) {
		return getTeamAt(row + column * 20);
	}

	private String getTeamAt(int index) {
		return (ChatColor.BOLD.toString() + ChatColor.GREEN + ChatColor.UNDERLINE + ChatColor.YELLOW + (index >= 10
				? ChatColor.COLOR_CHAR + String.valueOf(index / 10) + ChatColor.COLOR_CHAR + index % 10
				: ChatColor.BLACK.toString() + ChatColor.COLOR_CHAR + index) + ChatColor.RESET
		);
	}
}