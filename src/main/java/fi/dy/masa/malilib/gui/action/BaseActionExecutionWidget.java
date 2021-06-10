package fi.dy.masa.malilib.gui.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.action.ActionContext;
import fi.dy.masa.malilib.action.ActionRegistry;
import fi.dy.masa.malilib.action.NamedAction;
import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.icon.IconRegistry;
import fi.dy.masa.malilib.gui.util.EdgeInt;
import fi.dy.masa.malilib.gui.util.ScreenContext;
import fi.dy.masa.malilib.gui.widget.ContainerWidget;
import fi.dy.masa.malilib.render.text.StyledTextLine;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;

public abstract class BaseActionExecutionWidget extends ContainerWidget
{
    protected final List<StyledTextLine> widgetHoverText = new ArrayList<>(1);
    protected final List<StyledTextLine> combinedHoverText = new ArrayList<>(1);
    @Nullable protected NamedAction action;
    @Nullable protected String hoverText;
    @Nullable protected ActionWidgetContainer container;
    protected String name = "";
    protected EdgeInt editedBorderColor = new EdgeInt(0xFFFF8000);
    protected boolean dragging;
    protected boolean resizing;
    protected boolean selected;
    protected float iconScaleX = 1.0F;
    protected float iconScaleY = 1.0F;

    public BaseActionExecutionWidget()
    {
        super(40, 20);

        this.setNormalBorderWidth(1);
        this.setHoveredBorderWidth(2);
        this.setNormalBorderColor(0xFFFFFFFF);
        this.setHoveredBorderColor(0xFFE0E020);
        this.setNormalBackgroundColor(0x00000000);
        this.setHoveredBackgroundColor(0x00000000);

        this.setRenderNormalBorder(true);
        this.setRenderNormalBackground(true);
        this.setRenderHoverBackground(true);

        this.getHoverInfoFactory().setTextLineProvider("widget_hover_tip", this::getActionWidgetHoverTextLines);
    }

    public void setAction(@Nullable NamedAction action)
    {
        this.action = action;
    }

    public void setContainer(@Nullable ActionWidgetContainer container)
    {
        this.container = container;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;

        if (org.apache.commons.lang3.StringUtils.isBlank(name) == false)
        {
            this.setText(StyledTextLine.of(name));

            int width = this.text.renderWidth + 10;

            if (width > this.getWidth())
            {
                this.setWidth(width);
            }
        }
        else
        {
            this.setText(null);
        }
    }

    public float getIconScaleX()
    {
        return this.iconScaleX;
    }

    public float getIconScaleY()
    {
        return this.iconScaleY;
    }

    public void setIconScaleX(float iconScaleX)
    {
        this.iconScaleX = iconScaleX;
    }

    public void setIconScaleY(float iconScaleY)
    {
        this.iconScaleY = iconScaleY;
    }

    protected boolean isEditMode()
    {
        return this.container != null && this.container.isEditMode();
    }

    protected int getGridSize()
    {
        return this.container != null ? this.container.getGridSize() : -1;
    }

    @Nullable
    public String getActionWidgetHoverTextString()
    {
        return this.hoverText;
    }

    protected List<StyledTextLine> getActionWidgetHoverTextLines()
    {
        if (this.isEditMode() == false)
        {
            return this.widgetHoverText;
        }
        else if (BaseScreen.isCtrlDown() == false && BaseScreen.isShiftDown() == false)
        {
            return this.combinedHoverText;
        }

        return Collections.emptyList();
    }

    public void setActionWidgetHoverText(@Nullable String hoverText)
    {
        if (org.apache.commons.lang3.StringUtils.isBlank(hoverText))
        {
            hoverText = null;
        }

        this.hoverText = hoverText;
        this.updateHoverTexts();
    }

    protected void updateHoverTexts()
    {
        this.widgetHoverText.clear();
        this.combinedHoverText.clear();

        if (this.hoverText != null)
        {
            this.widgetHoverText.add(StyledTextLine.translate(this.hoverText));
            this.combinedHoverText.addAll(this.widgetHoverText);
        }

        if (this.action != null)
        {
            if (this.combinedHoverText.isEmpty() == false)
            {
                this.combinedHoverText.add(StyledTextLine.EMPTY);
            }

            this.combinedHoverText.addAll(this.action.getHoverInfo());
        }
    }

    public boolean isSelected()
    {
        return this.selected;
    }

    public void toggleSelected()
    {
        this.setSelected(! this.selected);
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;

        if (this.selected == false)
        {
            this.dragging = false;
        }
    }

    protected void notifyChange()
    {
        if (this.container != null)
        {
            this.container.notifyWidgetEdited();
        }
    }

    public void onAdded(BaseScreen screen)
    {
        this.updateHoverTexts();
    }

    @Override
    protected boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.isEditMode())
        {
            if (mouseButton == 0)
            {
                this.startDragging(mouseX, mouseY);
            }
            else if (mouseButton == 1)
            {
                if (BaseScreen.isShiftDown())
                {
                    this.startResize(mouseX, mouseY);
                }

                return true;
            }

            return false;
        }
        else if (mouseButton == 0 && this.action != null)
        {
            // Close the current screen first, in case the action opens another screen
            if (this.container != null && this.container.shouldCloseScreenOnExecute())
            {
                BaseScreen.openScreen(null);
            }

            this.executeAction();
        }

        return true;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        this.dragging = false;
        this.resizing = false;
        super.onMouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onMouseMoved(int mouseX, int mouseY)
    {
        if (this.dragging)
        {
            this.moveWidget(mouseX, mouseY);
        }
        else if (this.resizing)
        {
            this.resizeWidget(mouseX, mouseY);
        }

        return false;
    }

    @Override
    public void setSize(int width, int height)
    {
        if (this.text != null)
        {
            width = Math.max(width, this.text.renderWidth + 6);
            height = Math.max(height, 10);
        }

        super.setSize(width, height);
    }

    @Override
    public void setWidth(int width)
    {
        if (this.text != null)
        {
            width = Math.max(width, this.text.renderWidth + 6);
        }

        super.setWidth(width);
    }

    @Override
    public void setHeight(int height)
    {
        height = Math.max(height, 10);
        super.setHeight(height);
    }

    protected abstract Type getType();

    public void executeAction()
    {
        if (this.action != null)
        {
            this.action.getAction().execute(new ActionContext());
        }
    }

    public void startDragging(int mouseX, int mouseY)
    {
        this.dragging = true;
        this.notifyChange();
    }

    protected abstract void startResize(int mouseX, int mouseY);

    public abstract void moveWidget(int mouseX, int mouseY);

    protected abstract void resizeWidget(int mouseX, int mouseY);

    @Override
    protected EdgeInt getNormalBorderColorForRender()
    {
        if (this.dragging || this.resizing || this.selected)
        {
            return this.editedBorderColor;
        }

        return super.getNormalBorderColorForRender();
    }

    @Override
    protected EdgeInt getHoveredBorderColorForRender()
    {
        if (this.dragging || this.resizing || this.selected)
        {
            return this.editedBorderColor;
        }

        return super.getHoveredBorderColorForRender();
    }

    @Override
    public boolean shouldRenderHoverInfo(ScreenContext ctx)
    {
        if (this.dragging || this.resizing || BaseScreen.isShiftDown() || BaseScreen.isCtrlDown())
        {
            return false;
        }

        return super.shouldRenderHoverInfo(ctx);
    }

    @Override
    protected void renderIcon(int x, int y, float z, boolean enabled, boolean hovered, ScreenContext ctx)
    {
        if (this.icon != null)
        {
            x = this.getIconPositionX(x, this.icon.getWidth());
            y = this.getIconPositionY(y, this.icon.getHeight());
            int xSize = (int) (this.icon.getWidth() * this.iconScaleX);
            int ySize = (int) (this.icon.getHeight() * this.iconScaleY);

            this.icon.renderScaledAt(x, y, z + 0.025f, xSize, ySize, true, false);
        }
    }

    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();

        obj.addProperty("type", this.getType().name().toLowerCase(Locale.ROOT));

        if (org.apache.commons.lang3.StringUtils.isBlank(this.name) == false)
        {
            obj.addProperty("name", this.name);
        }

        if (this.icon != null)
        {
            obj.addProperty("icon_name", IconRegistry.getKeyForIcon(this.icon));
        }

        obj.addProperty("name_color", this.defaultNormalTextColor);
        obj.addProperty("name_color_hovered", this.defaultHoveredTextColor);
        obj.addProperty("bg_color", this.normalBackgroundColor);
        obj.addProperty("bg_color_hover", this.hoveredBackgroundColor);
        obj.addProperty("name_centered_x", this.centerTextHorizontally);
        obj.addProperty("name_centered_y", this.centerTextVertically);
        obj.addProperty("name_x_offset", this.textOffsetX);
        obj.addProperty("name_y_offset", this.textOffsetY);
        obj.addProperty("icon_centered_x", this.centerIconHorizontally);
        obj.addProperty("icon_centered_y", this.centerIconVertically);
        obj.addProperty("icon_x_offset", this.iconOffsetX);
        obj.addProperty("icon_y_offset", this.iconOffsetY);
        obj.addProperty("icon_scale_x", this.iconScaleX);
        obj.addProperty("icon_scale_y", this.iconScaleY);
        obj.add("border_color", this.normalBorderColor.toJson());
        obj.add("border_color_hover", this.hoveredBorderColor.toJson());

        if (this.action != null)
        {
            obj.addProperty("action_name", this.action.getRegistryName());

            JsonObject actionData = this.action.toJson();

            if (actionData != null)
            {
                obj.add("action_data", actionData);
            }
        }

        if (org.apache.commons.lang3.StringUtils.isBlank(this.hoverText) == false)
        {
            obj.addProperty("hover_text", this.hoverText);
        }

        return obj;
    }

    protected void fromJson(JsonObject obj)
    {
        this.setName(JsonUtils.getStringOrDefault(obj, "name", ""));

        if (JsonUtils.hasString(obj, "icon_name"))
        {
            this.setIcon(IconRegistry.INSTANCE.getIconByKey(JsonUtils.getStringOrDefault(obj, "icon_name", "")));
        }

        this.setActionWidgetHoverText(JsonUtils.getString(obj, "hover_text"));

        this.defaultNormalTextColor = JsonUtils.getIntegerOrDefault(obj, "name_color", this.defaultNormalTextColor);
        this.defaultHoveredTextColor = JsonUtils.getIntegerOrDefault(obj, "name_color_hovered", this.defaultHoveredTextColor);
        this.normalBackgroundColor = JsonUtils.getIntegerOrDefault(obj, "bg_color", this.normalBackgroundColor);
        this.hoveredBackgroundColor = JsonUtils.getIntegerOrDefault(obj, "bg_color_hover", this.hoveredBackgroundColor);

        this.centerTextHorizontally = JsonUtils.getBooleanOrDefault(obj, "name_centered_x", this.centerTextHorizontally);
        this.centerTextVertically = JsonUtils.getBooleanOrDefault(obj, "name_centered_y", this.centerTextVertically);
        this.textOffsetX = JsonUtils.getIntegerOrDefault(obj, "name_x_offset", this.textOffsetX);
        this.textOffsetY = JsonUtils.getIntegerOrDefault(obj, "name_y_offset", this.textOffsetY);

        this.centerIconHorizontally = JsonUtils.getBooleanOrDefault(obj, "icon_centered_x", this.centerIconHorizontally);
        this.centerIconVertically = JsonUtils.getBooleanOrDefault(obj, "icon_centered_y", this.centerIconVertically);
        this.iconOffsetX = JsonUtils.getIntegerOrDefault(obj, "icon_x_offset", this.iconOffsetX);
        this.iconOffsetY = JsonUtils.getIntegerOrDefault(obj, "icon_y_offset", this.iconOffsetY);
        this.iconScaleX = JsonUtils.getFloatOrDefault(obj, "icon_scale_x", this.iconScaleX);
        this.iconScaleY = JsonUtils.getFloatOrDefault(obj, "icon_scale_y", this.iconScaleY);

        JsonUtils.readArrayIfPresent(obj, "border_color", this.normalBorderColor::fromJson);
        JsonUtils.readArrayIfPresent(obj, "border_color_hover", this.hoveredBorderColor::fromJson);

        // FIXME
        NamedAction action = ActionRegistry.INSTANCE.getAction(JsonUtils.getStringOrDefault(obj, "action_name", "?"));

        if (action != null)
        {
            JsonUtils.readObjectIfPresent(obj, "action_data", action::fromJson);
            this.setAction(action);
        }
    }

    @Nullable
    public static BaseActionExecutionWidget createFromJson(JsonElement el)
    {
        if (el.isJsonObject() == false)
        {
            return null;
        }

        JsonObject obj = el.getAsJsonObject();
        Type type = JsonUtils.getStringOrDefault(obj, "type", "").equals("radial") ? Type.RADIAL : Type.RECTANGULAR;
        BaseActionExecutionWidget widget = type.create();

        widget.fromJson(obj);

        return widget;
    }

    public enum Type
    {
        RECTANGULAR ("malilib.label.action_execution_widget.type.rectangular",  RectangularActionExecutionWidget::new),
        RADIAL      ("malilib.label.action_execution_widget.type.radial",       RadialActionExecutionWidget::new);

        public static final ImmutableList<Type> VALUES = ImmutableList.copyOf(values());

        private final Supplier<BaseActionExecutionWidget> factory;
        private final String translationKey;

        Type(String translationKey, Supplier<BaseActionExecutionWidget> factory)
        {
            this.translationKey = translationKey;
            this.factory = factory;
        }

        public String getDisplayName()
        {
            return StringUtils.translate(this.translationKey);
        }

        public BaseActionExecutionWidget create()
        {
            return this.factory.get();
        }
    }
}
