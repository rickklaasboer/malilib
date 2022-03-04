package fi.dy.masa.malilib.gui.widget;

import java.util.function.IntConsumer;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.callback.IntegerSliderCallback;
import fi.dy.masa.malilib.util.data.RangedIntegerStorage;

public class IntegerEditWidget extends BaseNumberEditWidget implements RangedIntegerStorage
{
    protected final IntConsumer consumer;
    protected final int minValue;
    protected final int maxValue;
    protected int value;

    public IntegerEditWidget(int width, int height, IntConsumer consumer)
    {
        this(width, height, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, consumer);
    }

    public IntegerEditWidget(int width, int height, int originalValue, IntConsumer consumer)
    {
        this(width, height, originalValue, Integer.MIN_VALUE, Integer.MAX_VALUE, consumer);
    }

    public IntegerEditWidget(int width, int height, int originalValue,
                             int minValue, int maxValue, IntConsumer consumer)
    {
        super(width, height);

        this.consumer = consumer;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setIntegerValue(originalValue);

        this.textFieldWidget.setText(String.valueOf(originalValue));
        this.textFieldWidget.setTextValidator(new IntegerTextFieldWidget.IntValidator(minValue, maxValue));
    }

    @Override
    protected SliderWidget createSliderWidget()
    {
        return new SliderWidget(-1, this.getHeight(), new IntegerSliderCallback(this, this::updateTextField));
    }

    @Override
    protected boolean onValueAdjustButtonClick(int mouseButton)
    {
        int amount = mouseButton == 1 ? -1 : 1;
        if (BaseScreen.isShiftDown()) { amount *= 8; }
        if (BaseScreen.isAltDown()) { amount *= 4; }

        this.setIntegerValue(this.value + amount);
        this.consumer.accept(this.value);

        return true;
    }

    protected void updateTextField()
    {
        this.textFieldWidget.setText(String.valueOf(this.value));
    }

    @Override
    protected void setValueFromTextField(String str)
    {
        try
        {
            this.clampAndSetValue(Integer.parseInt(str));
            this.consumer.accept(this.value);
        }
        catch (NumberFormatException ignore) {}
    }

    protected void clampAndSetValue(int newValue)
    {
        this.value = MathHelper.clamp(newValue, this.minValue, this.maxValue);
    }

    @Override
    public boolean setIntegerValue(int newValue)
    {
        this.clampAndSetValue(newValue);
        this.updateTextField();
        return true;
    }

    @Override
    public int getIntegerValue()
    {
        return this.value;
    }

    @Override
    public int getMinIntegerValue()
    {
        return this.minValue;
    }

    @Override
    public int getMaxIntegerValue()
    {
        return this.maxValue;
    }
}