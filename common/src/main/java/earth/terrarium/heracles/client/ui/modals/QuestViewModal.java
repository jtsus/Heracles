package earth.terrarium.heracles.client.ui.modals;

import com.teamresourceful.resourcefullib.client.components.CursorWidget;
import earth.terrarium.heracles.Heracles;
import earth.terrarium.heracles.api.quests.Quest;
import earth.terrarium.heracles.client.HeraclesClient;
import earth.terrarium.heracles.client.components.lists.rewards.RewardsList;
import earth.terrarium.heracles.client.components.lists.tasks.TasksList;
import earth.terrarium.heracles.client.components.quest.QuestError;
import earth.terrarium.heracles.client.components.quest.editor.parser.MarkdownBodyParser;
import earth.terrarium.heracles.client.components.string.TextWidget;
import earth.terrarium.heracles.client.components.widgets.buttons.SpriteButton;
import earth.terrarium.heracles.client.handlers.ClientQuests;
import earth.terrarium.heracles.client.ui.Overlay;
import earth.terrarium.heracles.client.ui.QuestChrome;
import earth.terrarium.heracles.client.ui.UIConstants;
import earth.terrarium.heracles.common.constants.ConstantComponents;
import earth.terrarium.heracles.common.menus.quest.QuestContent;
import earth.terrarium.hermes.HermesWidget;
import earth.terrarium.hermes.api.rendering.HtmlRenderer;
import earth.terrarium.hermes.api.rendering.HtmlStyle;
import earth.terrarium.hermes.elements.Parser;
import earth.terrarium.hermes.libs.minemark.elements.MineMarkElement;
import net.minecraft.Optionull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/** FTB Quests-style centered popup: description, requirements, and rewards over the map. */
public class QuestViewModal extends Overlay {

    private static final int HEADER_HEIGHT = 32;
    private static final int FOOTER_HEIGHT = 26;
    private static final int PAD = 8;
    private static final int ICON = 20;
    private static final int SECTION_LABEL = 12;
    private static final int MIN_WIDTH = 360;
    private static final int MIN_HEIGHT = 240;

    private final QuestContent content;

    private int left;
    private int top;
    private int modalWidth;
    private int modalHeight;
    private int footerButtonX;
    private int footerButtonY;
    private int footerButtonWidth;

    private TasksList tasks;
    private RewardsList rewards;

    public QuestViewModal(Screen background, QuestContent content) {
        super(background);
        this.content = content;
        ClientQuests.mergeProgress(Map.of(content.id(), content.progress()));
    }

    public QuestContent content() {
        return this.content;
    }

    public int footerButtonX() {
        return this.footerButtonX;
    }

    public int footerButtonY() {
        return this.footerButtonY;
    }

    public int footerButtonWidth() {
        return this.footerButtonWidth;
    }

    public void addExtraWidget(AbstractWidget widget) {
        addRenderableWidget(widget);
    }

    public Quest quest() {
        return Optionull.map(ClientQuests.get(this.content.id()).orElse(null), ClientQuests.QuestEntry::value);
    }

    public void updateProgress() {
        var progress = ClientQuests.getProgress(this.content.id());
        if (progress != null) {
            this.content.progress().copyFrom(progress);
        }
        this.init();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.modalWidth = Math.min(this.width - 24, Math.max(MIN_WIDTH, Math.round(this.width * 0.62f)));
        this.modalHeight = Math.min(this.height - 24, Math.max(MIN_HEIGHT, Math.round(this.height * 0.78f)));
        this.left = (this.width - this.modalWidth) / 2;
        this.top = (this.height - this.modalHeight) / 2;

        int innerX = this.left + PAD;
        int innerW = this.modalWidth - PAD * 2;
        int descY = this.top + HEADER_HEIGHT + PAD;
        int bodyH = this.modalHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PAD * 2;
        int descH = Math.max(52, bodyH / 3);
        int colsY = descY + descH + 6;
        int colsH = Math.max(48, bodyH - descH - 6);
        int colW = (innerW - 6) / 2;
        int listY = colsY + SECTION_LABEL;
        int listH = Math.max(32, colsH - SECTION_LABEL);

        this.footerButtonWidth = Math.min(140, colW);
        this.footerButtonX = innerX;
        this.footerButtonY = this.top + this.modalHeight - FOOTER_HEIGHT + 3;

        SpriteButton close = SpriteButton.create(11, 11, UIConstants.MODAL_CLOSE, this::onClose)
            .withTooltip(ConstantComponents.CLOSE);
        close.setPosition(this.left + this.modalWidth - PAD - 11, this.top + (HEADER_HEIGHT - 11) / 2);
        addRenderableWidget(close);

        addRenderableWidget(createDescription(innerX, descY, innerW, descH));
        this.tasks = addRenderableWidget(new TasksList(this.tasks, colW, listH, this.content));
        this.tasks.setPosition(innerX, listY);
        this.rewards = addRenderableWidget(new RewardsList(this.rewards, colW, listH, this.content));
        this.rewards.setPosition(innerX + colW + 6, listY);
    }

    private AbstractWidget createDescription(int x, int y, int width, int height) {
        Quest quest = quest();
        AbstractWidget widget;
        if (quest == null) {
            widget = new QuestError(width, height, "Quest not found");
        } else {
            try {
                var lines = quest.display().description();
                if (lines == null || lines.isEmpty() || String.join("", lines).isBlank()) {
                    widget = new TextWidget(
                        width, height,
                        Component.translatable("gui.heracles.quests.no_description"),
                        this.font
                    ).alignLeft().alignTop().setColor(QuestChrome.MUTED);
                } else {
                    String desc = String.join("", MarkdownBodyParser.parse(lines));
                    MineMarkElement<HtmlStyle, HtmlRenderer> parsed = new Parser(HeraclesClient.getCurrentStyle()).parse(desc);
                    widget = new QuestDocument(width, height, parsed);
                }
            } catch (Throwable e) {
                Heracles.LOGGER.error("Error parsing quest description: ", e);
                widget = new QuestError(width, height, e);
            }
        }
        widget.setPosition(x, y);
        return widget;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, QuestChrome.SCRIM);
        QuestChrome.panel(graphics, this.left, this.top, this.modalWidth, this.modalHeight);
        QuestChrome.headerBar(graphics, this.left, this.top, this.modalWidth, HEADER_HEIGHT);

        int innerX = this.left + PAD;
        int innerW = this.modalWidth - PAD * 2;
        int descY = this.top + HEADER_HEIGHT + PAD;
        int bodyH = this.modalHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PAD * 2;
        int descH = Math.max(52, bodyH / 3);
        int colsY = descY + descH + 6;
        int colsH = Math.max(48, bodyH - descH - 6);
        int colW = (innerW - 6) / 2;

        QuestChrome.inset(graphics, innerX, descY, innerW, descH);
        QuestChrome.inset(graphics, innerX, colsY + SECTION_LABEL - 2, colW, colsH - SECTION_LABEL + 2);
        QuestChrome.inset(graphics, innerX + colW + 6, colsY + SECTION_LABEL - 2, colW, colsH - SECTION_LABEL + 2);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        renderWidgets(graphics, mouseX, mouseY, partialTick);
        renderForeground(graphics, mouseX, mouseY, partialTick);
    }

    private void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Quest quest = quest();
        int titleX = this.left + PAD;
        int titleY = this.top + 6;
        if (quest != null) {
            quest.display().icon().render(graphics, titleX, titleY, ICON, ICON);
            titleX += ICON + 6;
            graphics.drawString(this.font, quest.display().title(), titleX, titleY + 1, QuestChrome.CREAM, false);
            Component subtitle = quest.display().subtitle();
            if (subtitle != null && !subtitle.getString().isBlank()) {
                graphics.drawString(this.font, subtitle, titleX, titleY + 11, QuestChrome.MUTED, false);
            }
        } else {
            graphics.drawString(this.font, Component.literal(this.content.id()), titleX, titleY + 6, QuestChrome.CREAM, false);
        }

        int innerX = this.left + PAD;
        int innerW = this.modalWidth - PAD * 2;
        int descY = this.top + HEADER_HEIGHT + PAD;
        int bodyH = this.modalHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PAD * 2;
        int descH = Math.max(52, bodyH / 3);
        int colsY = descY + descH + 6;
        int colW = (innerW - 6) / 2;

        graphics.drawString(
            this.font,
            Component.translatable("gui.heracles.quests.requirements"),
            innerX + 2, colsY - 1,
            QuestChrome.ACCENT, false
        );
        graphics.drawString(
            this.font,
            ConstantComponents.Rewards.TITLE,
            innerX + colW + 8, colsY - 1,
            QuestChrome.ACCENT, false
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < this.left || mouseX > this.left + this.modalWidth
            || mouseY < this.top || mouseY > this.top + this.modalHeight) {
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static class QuestDocument extends HermesWidget implements CursorWidget {

        public QuestDocument(int width, int height, MineMarkElement<HtmlStyle, HtmlRenderer> parsed) {
            super(0, 0, width, height, parsed);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.DEFAULT;
        }
    }
}
