package fi.dy.masa.malilib.config;

public interface IConfigBoolean extends IConfigValue
{
    boolean getBooleanValue();

    boolean getDefaultBooleanValue();

    void setBooleanValue(boolean value);
}
