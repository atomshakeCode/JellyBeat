package dev.atomshake.jellybeat;
import net.minecraft.client.renderer.entity.state.*;
public final class EntityDanceTarget {
 public static DanceTarget of(EntityRenderState s){return s instanceof AvatarRenderState?DanceTarget.PLAYERS:s instanceof BoatRenderState?DanceTarget.BOATS:DanceTarget.ENTITIES;}
}
