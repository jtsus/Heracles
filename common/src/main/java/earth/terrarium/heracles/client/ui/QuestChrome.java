package earth.terrarium.heracles.client.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Grey inset panels for the quest details modal. */
public final class QuestChrome {

    public static final int INNER = 0xF1242428;
    public static final int EDGE = 0xFF585659;
    public static final int TITLE = 0xFEFEFE;
    public static final int MUTED = 0xFFA0A0A0;
    public static final int SCRIM = 0xA0000000;
    public static final int SECTION = 0xFFE8E8E8;

    private QuestChrome() {
    }

    public static void inset(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, INNER);
        graphics.fill(x, y, x + width, y + 1, 0x66000000);
        graphics.fill(x, y + height - 1, x + width, y + height, EDGE);
        graphics.fill(x, y, x + 1, y + height, EDGE);
        graphics.fill(x + width - 1, y, x + width, y + height, EDGE);
    }
}
