package in.roflmuff.ftc.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

    @Shadow @Final private boolean doBackgroundFade;
    @Shadow private long backgroundFadeStart;
    private Text puzzleText;
    private int puzzleTextWidth;
    private int yOffset = 20;
    private String titleText = "Fabulously Optimized 3.3.0-alpha.3";
    private String url = "https://example.com";

    protected MixinTitleScreen(Text title) {
        super(title);
    }
    @Inject(at = @At("TAIL"), method = "init")
    private void puzzle$init(CallbackInfo ci) {
        puzzleText = Text.of(titleText);
        this.puzzleTextWidth = this.textRenderer.getWidth(puzzleText);
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void puzzle$render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float f = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;
        textRenderer.drawWithShadow(matrices, puzzleText,2,this.height - yOffset, 16777215 | l);
        if (mouseX > 2 && mouseX < 2 + this.puzzleTextWidth && mouseY > this.height - yOffset && mouseY < this.height - yOffset + 10) {
            fill(matrices, 2, this.height - yOffset + 9, 2 + this.puzzleTextWidth, this.height - yOffset + 10, 16777215 | l);
        }
    }

    private void confirmLink(boolean open) {
        if (open) {
            Util.getOperatingSystem().open(url);
        }
        Objects.requireNonNull(this.client).setScreen(this);
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked",cancellable = true)
    private void puzzle$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (mouseX > 2 && mouseX < (double)(2 + this.puzzleTextWidth) && mouseY > (double)(this.height - yOffset) && mouseY < (double)this.height - yOffset + 10) {
            if (Objects.requireNonNull(this.client).options.chatLinksPrompt) {
                this.client.setScreen(new ConfirmChatLinkScreen(this::confirmLink, url, true));
            } else {
                Util.getOperatingSystem().open(url);
            }
            cir.setReturnValue(false);
        }
    }
}