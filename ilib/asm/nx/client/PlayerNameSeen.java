package ilib.asm.nx.client;

import roj.asm.nixim.Copy;
import roj.asm.nixim.Inject;
import roj.asm.nixim.Nixim;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;

/**
 * @author Roj234
 * @since 2022/9/13 0013 12:55
 */
@Nixim("/")
abstract class PlayerNameSeen extends RenderPlayer {
	PlayerNameSeen() {super(null);}

	@Inject(value = "/", at = Inject.At.INVOKE, param = {"net.minecraft.client.renderer.entity.Render.func_188296_a", "wohoo"})
	protected void renderEntityName(AbstractClientPlayer entity, double x, double y, double z, String name, double distanceSq) {}

	@Copy(unique = true)
	private void wohoo(Entity entityIn, double x, double y, double z, String name, int distanceSq) {
		renderLivingLabel((AbstractClientPlayer) entityIn, name, x, y, z, 1024);
	}
}