// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.util.*;

public class VAnvilSlot extends Slot
{
    private VAnvil _anvil;

    VAnvilSlot(VAnvil anvil, IInventory iinventory, int i, int j, int k)
    {
        super(iinventory, i, j, k);
        _anvil = anvil;
    }

    public boolean isAllowed(ItemStack itemstack)
    {
        return false;
    }

    public boolean a(EntityHuman entityhuman)
    {
        return (VAnvil.playerFree(entityhuman) || entityhuman.expLevel >= _anvil.a) && (_anvil.a > 0) && this.d();
    }

    public void a(EntityHuman entityhuman, ItemStack itemstack)
    {
        entityhuman.levelDown(VAnvil.playerFree(entityhuman) ? 0 : (-_anvil.a));
        VAnvil.a(_anvil).setItem(0, null);
        if(VAnvil.b(_anvil) > 0)
        {
            ItemStack itemstack1 = VAnvil.a(_anvil).getItem(1);
            if(itemstack1 != null && itemstack1.count > VAnvil.b(_anvil))
            {
                itemstack1.count -= VAnvil.b(_anvil);
                VAnvil.a(_anvil).setItem(1, itemstack1);
            }
            else
            {
                VAnvil.a(_anvil).setItem(1, null);
            }
        }
        else
        {
            VAnvil.a(_anvil).setItem(1, null);
        }
        _anvil.a = 0;
        _anvil.updatePlayerInventory();
    }
}