package earth.terrarium.heracles.client.ui.modals;

import com.teamresourceful.resourcefullib.client.components.CursorWidget;
import earth.terrarium.heracles.Heracles;
import earth.terrarium.heracles.api.client.theme.ModalsTheme;
import earth.terrarium.heracles.api.quests.Quest;
import earth.terrarium.heracles.client.HeraclesClient;
import earth.terrarium.heracles.client.components.base.ListWidget;
import earth.terrarium.heracles.client.components.lists.DisplayWidgetRow;
import earth.terrarium.heracles.client.components.lists.HeadingListEntry;
import earth.terrarium.heracles.client.components.lists.ListEntry;
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
import earth.terrarium.heracles.client.utils.UIUtils;
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

import java.util.List;
import java.util.Map;

/** Centered quest popup: one body scroller for description, requirements, and rewards. */
public class QuestViewModal extends Overlay {

    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 26;
    private static final int PAD = 16;
    private static final int GUTTER = 12;
    private static final int ROW_INSET = 8;
    private static final int ICON = 20;

    private final QuestContent content;

    private int left;
    private int top;
    private int modalWidth;
    private int modalHeight;
    private int innerX;
    private int innerW;
    private int bodyTop;
    private int bodyH;
    private int tasksY;
    private int rewardsY;
    private int footerButtonX;
    private int footerButtonY;
    private int footerButtonWidth;

    private ListWidget body;

    public QuestViewModal(Screen background, QuestContent content) {
        super(background);
        this.content = content;
        ClientQuests.mergeProgress(Map.of(content.id(), content.progress()));
    }

    public QuestContent content() {
        return this.content;
    }

    public int contentGutter() {
        return GUTTER;
    }

    public int rowInset() {
        return ROW_INSET;
    }

    public int innerLeft() {
        return this.innerX;
    }

    public int bodyLeft() {
        return this.body == null ? this.innerX + GUTTER : this.body.getX();
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

    public int tasksTop() {
        return this.tasksY;
    }

    public int rewardsTop() {
        return this.rewardsY;
    }

    public boolean hasNestedQuestLists() {
        for (var child : children()) {
            if (child instanceof TasksList || child instanceof RewardsList) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRequirementsAndRewardsSections() {
        if (this.body == null) {
            return false;
        }
        boolean requirements = false;
        boolean rewards = false;
        for (ListWidget.Item item : this.body.items()) {
            if (item instanceof QuestModalSection section) {
                if (QuestModalSection.REQUIREMENTS.equals(section.sectionId())) {
                    requirements = true;
                } else if (QuestModalSection.REWARDS.equals(section.sectionId())) {
                    rewards = true;
                }
            }
        }
        return requirements && rewards;
    }

    public boolean hasRewardStatusTags() {
        if (this.body == null) {
            return false;
        }
        String available = Component.translatable("quest.heracles.available").getString();
        String claimed = Component.translatable("quest.heracles.claimed").getString();
        for (ListWidget.Item item : this.body.items()) {
            if (item instanceof DisplayWidgetRow row && row.widget() instanceof HeadingListEntry<?> heading) {
                String title = heading.title().getString();
                if (available.equals(title) || claimed.equals(title)) {
                    return true;
                }
            }
        }
        return false;
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

    private void computeLayout() {
        this.modalWidth = Math.min(this.width - 32, Math.max(420, Math.round(this.width * 0.5f)));
        this.modalHeight = Math.min(this.height - 28, Math.max(300, Math.round(this.height * 0.82f)));
        this.left = (this.width - this.modalWidth) / 2;
        this.top = (this.height - this.modalHeight) / 2;
        this.innerX = this.left + PAD;
        this.innerW = this.modalWidth - PAD * 2;
        this.bodyTop = this.top + HEADER_HEIGHT + PAD;
        this.bodyH = this.modalHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PAD * 2;
        int contentX = this.innerX + GUTTER;
        int contentW = Math.max(1, this.innerW - GUTTER * 2);
        this.footerButtonWidth = Math.min(160, contentW);
        this.footerButtonX = contentX;
        this.footerButtonY = this.top + this.modalHeight - FOOTER_HEIGHT + 3;
    }

    private boolean hasDescription() {
        Quest quest = quest();
        if (quest == null) {
            return false;
        }
        List<String> lines = quest.display().description();
        return lines != null && !lines.isEmpty() && !String.join("", lines).isBlank();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        computeLayout();

        SpriteButton close = SpriteButton.create(11, 11, UIConstants.MODAL_CLOSE, this::onClose)
            .withTooltip(ConstantComponents.CLOSE);
        close.setPosition(this.left + this.modalWidth - PAD - 11, this.top + (HEADER_HEIGHT - 11) / 2);
        addRenderableWidget(close);

        ListWidget previous = this.body;
        int contentX = this.innerX + GUTTER;
        int contentW = Math.max(1, this.innerW - GUTTER * 2);
        this.body = addRenderableWidget(new ListWidget(contentW, this.bodyH));
        this.body.setPosition(contentX, this.bodyTop);

        AbstractWidget description = createDescription(contentW);
        this.body.add((ListWidget.Item) description);

        QuestModalSection requirements = new QuestModalSection(
            contentW,
            QuestModalSection.REQUIREMENTS,
            Component.translatable("gui.heracles.quests.requirements")
        );
        this.body.add(requirements);

        for (ListEntry<?> entry : TasksList.modalEntries(this.content)) {
            this.body.add(new DisplayWidgetRow(contentW, entry, ROW_INSET));
        }

        QuestModalSection rewards = new QuestModalSection(
            contentW,
            QuestModalSection.REWARDS,
            ConstantComponents.Rewards.TITLE
        );
        this.body.add(rewards);

        for (ListEntry<?> entry : RewardsList.modalEntries(this.content)) {
            this.body.add(new DisplayWidgetRow(contentW, entry, ROW_INSET));
        }

        int offset = 0;
        this.tasksY = this.bodyTop;
        this.rewardsY = this.bodyTop;
        for (ListWidget.Item item : this.body.items()) {
            if (item instanceof QuestModalSection section) {
                if (QuestModalSection.REQUIREMENTS.equals(section.sectionId())) {
                    this.tasksY = this.bodyTop + offset;
                } else if (QuestModalSection.REWARDS.equals(section.sectionId())) {
                    this.rewardsY = this.bodyTop + offset;
                }
            }
            offset += item.getHeight();
        }

        this.body.update(previous);
    }

    private AbstractWidget createDescription(int width) {
        Quest quest = quest();
        if (quest == null) {
            return new QuestError(width, 40, "Quest not found");
        }
        if (!hasDescription()) {
            return new BodyText(width, 18, Component.translatable("gui.heracles.quests.no_description"), this.font)
                .alignLeft()
                .alignTop()
                .setColor(QuestChrome.MUTED);
        }
        try {
            List<String> lines = quest.display().description();
            String desc = String.join("", MarkdownBodyParser.parse(lines));
            MineMarkElement<HtmlStyle, HtmlRenderer> parsed = new Parser(HeraclesClient.getCurrentStyle()).parse(desc);
            return new QuestDocument(width, estimateDescriptionHeight(width, lines), parsed);
        } catch (Throwable e) {
            Heracles.LOGGER.error("Error parsing quest description: ", e);
            return new QuestError(width, 48, e);
        }
    }

    private int estimateDescriptionHeight(int width, List<String> lines) {
        String joined = String.join("\n", lines);
        String stripped = joined.replaceAll("<[^>]+>", " ").replaceAll("[#*_>`\\[\\]()]", "");
        return Math.max(this.font.lineHeight + 8, this.font.wordWrapHeight(stripped, Math.max(8, width - 4)) + 12);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, QuestChrome.SCRIM);
        UIUtils.blitWithEdge(graphics, UIConstants.MODAL, this.left, this.top, this.modalWidth, this.modalHeight, 3);
        UIUtils.blitWithEdge(graphics, UIConstants.MODAL_HEADER, this.left, this.top, this.modalWidth, HEADER_HEIGHT, 3);
        QuestChrome.inset(graphics, this.innerX, this.bodyTop, this.innerW, this.bodyH);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        renderWidgets(graphics, mouseX, mouseY, partialTick);
        renderForeground(graphics);
    }

    private void renderForeground(GuiGraphics graphics) {
        Quest quest = quest();
        int titleX = this.left + PAD;
        int titleY = this.top + 6;
        if (quest != null) {
            quest.display().icon().render(graphics, titleX, titleY, ICON, ICON);
            titleX += ICON + 6;
            graphics.drawString(this.font, quest.display().title(), titleX, titleY + 1, ModalsTheme.getTitle(), false);
            Component subtitle = quest.display().subtitle();
            if (subtitle != null && !subtitle.getString().isBlank()) {
                graphics.drawString(this.font, subtitle, titleX, titleY + 12, QuestChrome.MUTED, false);
            }
        } else {
            graphics.drawString(this.font, Component.literal(this.content.id()), titleX, titleY + 6, ModalsTheme.getTitle(), false);
        }
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

    private static class BodyText extends TextWidget implements ListWidget.Item {

        public BodyText(int width, int height, Component component, net.minecraft.client.gui.Font font) {
            super(width, height, component, font);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return false;
        }
    }

    private static class QuestDocument extends HermesWidget implements CursorWidget, ListWidget.Item {

        public QuestDocument(int width, int height, MineMarkElement<HtmlStyle, HtmlRenderer> parsed) {
            super(0, 0, width, height, parsed);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.DEFAULT;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return false;
        }
    }
}
