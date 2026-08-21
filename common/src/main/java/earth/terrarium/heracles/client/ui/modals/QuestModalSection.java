package earth.terrarium.heracles.client.ui.modals;

import earth.terrarium.heracles.client.components.base.ListWidget;
import earth.terrarium.heracles.client.ui.QuestChrome;
import earth.terrarium.olympus.client.components.base.BaseWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Section heading inside the quest modal body scroller. */
public class QuestModalSection extends BaseWidget implements ListWidget.Item {

    public static final String REQUIREMENTS = "requirements";
    public static final String REWARDS = "rewards";
    public static final int MARGIN_TOP = 10;
    public static final int MARGIN_BOTTOM = 6;

    private final Component title;
    private final String id;

    public QuestModalSection(int width, String id, Component title) {
        super(width, MARGIN_TOP + 9 + MARGIN_BOTTOM);
        this.id = id;
        this.title = title;
    }

    public String sectionId() {
        return this.id;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(
            Minecraft.getInstance().font,
            this.title,
            getX() + 4,
            getY() + MARGIN_TOP,
            QuestChrome.SECTION,
            false
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }
}
