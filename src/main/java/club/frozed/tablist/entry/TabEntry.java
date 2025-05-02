package club.frozed.tablist.entry;

import club.frozed.tablist.skin.Skin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Ryzeon, Elb1to
 */
@Accessors(chain = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class TabEntry {

	private final int column;
	private final int row;
	private final String text;

	private int ping = 1;
	private Skin skin = Skin.DEFAULT_SKIN;

}