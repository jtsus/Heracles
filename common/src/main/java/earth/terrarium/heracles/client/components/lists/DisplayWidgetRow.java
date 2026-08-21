package earth.terrarium.heracles.client.components.lists;

import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack;
import earth.terrarium.heracles.api.client.DisplayWidget;
import earth.terrarium.heracles.client.components.base.ListWidget;
import earth.terrarium.olympus.client.components.base.BaseWidget;
import net.minecraft.client.gui.GuiGraphics;

/** A {@link DisplayWidget} as a non-scrolling list row. */
public class DisplayWidgetRow extends BaseWidget implements ListWidget.Item {

    private final DisplayWidget widget;
    private final int inset;

    public DisplayWidgetRow(int width, DisplayWidget widget) {
        this(width, widget, 0);
    }

    public DisplayWidgetRow(int width, DisplayWidget widget, int inset) {
        super(width, widget.getHeight(Math.max(1, width - inset * 2)));
        this.widget = widget;
        this.inset = Math.max(0, inset);
    }

    public DisplayWidget widget() {
        return this.widget;
    }

    public int inset() {
        return this.inset;
    }

    private int innerWidth() {
        return Math.max(1, getWidth() - this.inset * 2);
    }

    @Override
    public void setListItemWidth(int width) {
        setWidth(width);
        this.height = this.widget.getHeight(Math.max(1, width - this.inset * 2));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int innerWidth = innerWidth();
        this.widget.render(
            graphics,
            new ScissorBoxStack(),
            getX() + this.inset,
            getY(),
            innerWidth,
            mouseX,
            mouseY,
            isHovered(),
            partialTick
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.widget.mouseClicked(mouseX - getX() - this.inset, mouseY - getY(), button, innerWidth());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }
}
