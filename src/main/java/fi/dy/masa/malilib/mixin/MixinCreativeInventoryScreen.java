package fi.dy.masa.malilib.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import fi.dy.masa.malilib.event.RenderEventHandler;

@Mixin(CreativeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen
{
    private void onRenderTooltip(MatrixStack matrixStack, ItemStack stack, int x, int y, CallbackInfo ci)
    {
        ((RenderEventHandler) RenderEventHandler.getInstance()).onRenderTooltipLast(matrixStack, stack, x, y);
    }
}
