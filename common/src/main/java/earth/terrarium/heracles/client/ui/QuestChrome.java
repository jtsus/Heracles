package earth.terrarium.heracles.client.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Cobblemon-adjacent navy/cyan chrome for quest screens and the quest view modal. */
public final class QuestChrome {

    public static final int NAVY = 0xF2182230;
    public static final int NAVY_DEEP = 0xF0101820;
    public static final int HEADER = 0xF2243348;
    public static final int INNER = 0xF01C2A38;
    public static final int ACCENT = 0xFF3DD6E8;
    public static final int ACCENT_DIM = 0xFF1A6F7A;
    public static final int CREAM = 0xFFF3EEE4;
    public static final int MUTED = 0xFF8FB4BA;
    public static final int SCRIM = 0xC0081018;

    private QuestChrome() {
    }

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, NAVY);
        graphics.fill(x, y, x + width, y + 2, ACCENT);
        graphics.fill(x, y + height - 1, x + width, y + height, ACCENT_DIM);
        graphics.fill(x, y, x + 1, y + height, ACCENT_DIM);
        graphics.fill(x + width - 1, y, x + width, y + height, ACCENT_DIM);
    }

    public static void headerBar(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, HEADER);
        graphics.fill(x, y + height - 1, x + width, y + height, ACCENT);
    }

    public static void inset(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, INNER);
        graphics.fill(x, y, x + width, y + 1, 0x66000000);
        graphics.fill(x, y + height - 1, x + width, y + height, ACCENT_DIM);
    }

    public static void sidebar(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, NAVY_DEEP);
        graphics.fill(x + width - 1, y, x + width, y + height, ACCENT_DIM);
    }
}
