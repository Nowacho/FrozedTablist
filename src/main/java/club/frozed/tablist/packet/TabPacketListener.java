package club.frozed.tablist.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;

/**
 * @author Biquaternions, Elb1to
 */
public class TabPacketListener implements PacketListener {

	@Override
	public void onPacketSend(PacketSendEvent event) {
		if (event.getPacketType() == PacketType.Play.Server.TEAMS) {
			WrapperPlayServerTeams scoreboardTeam = new WrapperPlayServerTeams(event);
			if (scoreboardTeam.getTeamMode() == WrapperPlayServerTeams.TeamMode.REMOVE && !scoreboardTeam.getTeamName().equalsIgnoreCase("tab")) {
				scoreboardTeam.setTeamMode(WrapperPlayServerTeams.TeamMode.ADD_ENTITIES);
				scoreboardTeam.setTeamName("tab");
				scoreboardTeam.getTeamInfo().ifPresent(info -> info.setDisplayName(Component.text("tab")));
			}
		}
	}
}
