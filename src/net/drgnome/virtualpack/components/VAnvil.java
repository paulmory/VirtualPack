// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import java.lang.reflect.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.*;
import net.drgnome.virtualpack.util.*;

public class VAnvil extends ContainerAnvil implements VGUI
{
    private int l = 0;
    private String m;
    private final EntityPlayer n;
    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;

    public VAnvil(EntityPlayer entityplayer)
    {
        super(entityplayer.inventory, null, 0, 0, 0, entityplayer);
        checkReachable = false;
        this.n = entityplayer;
        for(int i = 0; i < c.size(); i++)
        {
            try
            {
                if(Class.forName("net.minecraft.server.v#MC_VERSION#.SlotAnvilResult").isInstance(c.get(i)))
                {
                    c.set(i, new VAnvilSlot(this, getInv("f"), 2, 134, 47));
                    break;
                }
            }
            catch(Throwable t)
            {
                t.printStackTrace();
                break;
            }
        }
    }

    public void a(IInventory iinventory)
    {
        super.a(iinventory);
        if(iinventory == VAnvil.a(this))
        {
            this.d();
        }
    }

    public void d()
    {
        ItemStack itemstack = VAnvil.a(this).getItem(0);
        this.a = 0;
        int i = 0;
        byte b0 = 0;
        int j = 0;
        if(itemstack == null)
        {
            getInv("f").setItem(0, (ItemStack) null);
            this.a = 0;
        }
        else
        {
            ItemStack itemstack1 = itemstack.cloneItemStack();
            ItemStack itemstack2 = VAnvil.a(this).getItem(1);
            Map map = EnchantmentManager.a(itemstack1);
            boolean flag = false;
            int k = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
            this.l = 0;
            int l;
            int i1;
            int j1;
            int k1;
            int l1;
            Iterator iterator;
            Enchantment enchantment;
            if(itemstack2 != null)
            {
                flag = itemstack2.id == Item.ENCHANTED_BOOK.id && Item.ENCHANTED_BOOK.g(itemstack2).size() > 0;
                if(itemstack1.f() && Item.byId[itemstack1.id].a(itemstack, itemstack2))
                {
                    l = Math.min(itemstack1.i(), itemstack1.k() / 4);
                    if(l <= 0)
                    {
                        getInv("f").setItem(0, (ItemStack) null);
                        this.a = 0;
                        return;
                    }
                    for(i1 = 0; l > 0 && i1 < itemstack2.count; ++i1)
                    {
                        j1 = itemstack1.i() - l;
                        itemstack1.setData(j1);
                        i += Math.max(1, l / 100) + map.size();
                        l = Math.min(itemstack1.i(), itemstack1.k() / 4);
                    }
                    this.l = i1;
                }
                else
                {
                    if(!flag && (itemstack1.id != itemstack2.id || !itemstack1.f()))
                    {
                        getInv("f").setItem(0, (ItemStack) null);
                        this.a = 0;
                        return;
                    }
                    if(itemstack1.f() && !flag)
                    {
                        l = itemstack.k() - itemstack.i();
                        i1 = itemstack2.k() - itemstack2.i();
                        j1 = i1 + itemstack1.k() * 12 / 100;
                        int i2 = l + j1;
                        k1 = itemstack1.k() - i2;
                        if(k1 < 0)
                        {
                            k1 = 0;
                        }
                        if(k1 < itemstack1.getData())
                        {
                            itemstack1.setData(k1);
                            i += Math.max(1, j1 / 100);
                        }
                    }
                    Map map1 = EnchantmentManager.a(itemstack2);
                    iterator = map1.keySet().iterator();
                    while(iterator.hasNext())
                    {
                        j1 = ((Integer)iterator.next()).intValue();
                        enchantment = Enchantment.byId[j1];
                        k1 = map.containsKey(Integer.valueOf(j1)) ? ((Integer)map.get(Integer.valueOf(j1))).intValue() : 0;
                        l1 = ((Integer)map1.get(Integer.valueOf(j1))).intValue();
                        int j2;
                        if(k1 == l1)
                        {
                            ++l1;
                            j2 = l1;
                        }
                        else
                        {
                            j2 = Math.max(l1, k1);
                        }
                        l1 = j2;
                        int k2 = l1 - k1;
                        boolean flag1 = enchantment.canEnchant(itemstack);
                        if(playerFree(this.n))
                        {
                            flag1 = true;
                        }
                        Iterator iterator1 = map.keySet().iterator();
                        while(iterator1.hasNext())
                        {
                            int l2 = ((Integer)iterator1.next()).intValue();
                            if(l2 != j1 && !enchantment.a(Enchantment.byId[l2]))
                            {
                                flag1 = false;
                                i += k2;
                            }
                        }
                        if(flag1)
                        {
                            if(l1 > enchantment.getMaxLevel())
                            {
                                l1 = enchantment.getMaxLevel();
                            }
                            map.put(Integer.valueOf(j1), Integer.valueOf(l1));
                            int i3 = 0;
                            switch(enchantment.getRandomWeight())
                            {
                                case 1:
                                    i3 = 8;
                                    break;
                                case 2:
                                    i3 = 4;
                                case 3:
                                case 4:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                default:
                                    break;
                                case 5:
                                    i3 = 2;
                                    break;
                                case 10:
                                    i3 = 1;
                            }
                            i += i3 * k2;
                        }
                    }
                }
            }
            if(this.m != null && !this.m.equalsIgnoreCase(itemstack.r()) && this.m.length() > 0)
            {
                j = itemstack.f() ? 7 : itemstack.count * 5;
                i += j;
                if(itemstack.s())
                {
                    k += j / 2;
                }
                itemstack1.c(this.m);
            }
            l = 0;
            for(iterator = map.keySet().iterator(); iterator.hasNext(); k += l + k1 * l1)
            {
                j1 = ((Integer)iterator.next()).intValue();
                enchantment = Enchantment.byId[j1];
                k1 = ((Integer)map.get(Integer.valueOf(j1))).intValue();
                l1 = 0;
                ++l;
                switch(enchantment.getRandomWeight())
                {
                    case 1:
                        l1 = 8;
                        break;
                    case 2:
                        l1 = 4;
                    case 3:
                    case 4:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    default:
                        break;
                    case 5:
                        l1 = 2;
                        break;
                    case 10:
                        l1 = 1;
                }
                if(flag)
                {
                    l1 = Math.max(1, l1 / 2);
                }
            }
            if(flag)
            {
                k = Math.max(1, k / 2);
            }
            this.a = k + i;
            if(i <= 0)
            {
                itemstack1 = null;
            }
            if(j == i && j > 0 && this.a >= 40)
            {
                this.a = 39;
            }
            if(this.a >= 40 && !playerFree(this.n))
            {
                itemstack1 = null;
            }
            if(itemstack1 != null)
            {
                i1 = itemstack1.getRepairCost();
                if(itemstack2 != null && i1 < itemstack2.getRepairCost())
                {
                    i1 = itemstack2.getRepairCost();
                }
                if(itemstack1.s())
                {
                    i1 -= 9;
                }
                if(i1 < 0)
                {
                    i1 = 0;
                }
                i1 += 2;
                itemstack1.setRepairCost(i1);
                EnchantmentManager.a(map, itemstack1);
            }
            getInv("f").setItem(0, itemstack1);
            this.b();
        }
    }

    public void b(EntityHuman entityhuman)
    {
        for(int i = 0; i < VAnvil.a(this).getSize(); ++i)
        {
            ItemStack itemstack = VAnvil.a(this).splitWithoutUpdate(i);
            if(itemstack != null)
            {
                entityhuman.drop(itemstack);
            }
        }
    }

    public ItemStack b(EntityHuman entityhuman, int i)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.c.get(i);
        if(slot != null && slot.d())
        {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            if(i == 2)
            {
                if(!this.a(itemstack1, 3, 39, true))
                {
                    return null;
                }
                slot.a(itemstack1, itemstack);
            }
            else if(i != 0 && i != 1)
            {
                if(i >= 3 && i < 39 && !this.a(itemstack1, 0, 2, false))
                {
                    return null;
                }
            }
            else if(!this.a(itemstack1, 3, 39, false))
            {
                return null;
            }
            if(itemstack1.count == 0)
            {
                slot.set((ItemStack) null);
            }
            else
            {
                slot.e();
            }
            if(itemstack1.count == itemstack.count)
            {
                return null;
            }
            slot.a(entityhuman, itemstack1);
        }
        return itemstack;
    }

    public void a(String s)
    {
        this.m = s;
        if(this.getSlot(2).d())
        {
            this.getSlot(2).getItem().c(this.m);
        }
        this.d();
    }

    public static IInventory a(VAnvil anvil)
    {
        return getInv(anvil, "g");
    }

    public static int b(VAnvil anvil)
    {
        return anvil.l;
    }
    
    public IInventory getInv(String name)
    {
        return getInv(this, name);
    }
    
    public static IInventory getInv(VAnvil anvil, String name)
    {
        try
        {
            Field f = ContainerAnvil.class.getDeclaredField(name);
            f.setAccessible(true);
            return (IInventory)f.get(anvil);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return null;
        }
    }
    
    public void updatePlayerInventory()
    {
        this.n.updateInventory(this.n.activeContainer);
    }
    
    public static boolean playerFree(EntityHuman entityhuman)
    {
        return (entityhuman.abilities.canInstantlyBuild || Perm.has(entityhuman.world.getWorld().getName(), entityhuman.name, "vpack.use.anvil.free"));
    }
}