package dev.atomshake.jellybeat.mixin;
import dev.atomshake.jellybeat.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(EntityRenderState.class)
public class VoiceStateMixin implements VoiceRenderState {
 @Unique private VoiceBeats.Sample jellybeat$sample=VoiceBeats.Sample.NONE;
 public VoiceBeats.Sample jellybeat$voice(){return jellybeat$sample;}
 public void jellybeat$voice(VoiceBeats.Sample sample){jellybeat$sample=sample;}
}
