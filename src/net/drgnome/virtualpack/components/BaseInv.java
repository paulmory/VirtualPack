// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.entity.HumanEntity;
import net.drgnome.virtualpack.util.*;
import net.drgnome.virtualpack.item.ComparativeItemStack;

public abstract class BaseInv implements Inventory
{
    protected String _name;
    protected ItemStack[] _contents;
    
    public BaseInv(String name, int size)
    {
        _name = name;
        _contents = new ItemStack[size];
    }
    
    public String getName()
    {
        return _name;
    }
    
    public String getTitle()
    {
        return getName();
    }
    
    public int getSize()
    {
        return _contents.length;
    }
    
    public int getMaxStackSize()
    {
        return 64;
    }
    
    public void setMaxStackSize(int size)
    {
    }
    
    public ItemStack getItem(int index)
    {
        if((index < 0) || (index >= _contents.length))
        {
            return null;
        }
        return _contents[index];
    }
    
    public void setItem(int index, ItemStack item)
    {
        if((index >= 0) && (index < _contents.length))
        {
            _contents[index] = item;
        }
    }
    
    public ItemStack getItemCopy(int index)
    {
        return Util.copy(getItem(index));
    }
    
    public void setItemCopy(int index, ItemStack item)
    {
        setItem(index, Util.copy(item));
    }
    
    public int first(int id)
    {
        return first(id, (short)-1);
    }
    
    public int first(int id, short meta)
    {
        return first(new ComparativeItemStack(id, meta));
    }
    
    public int first(ComparativeItemStack stack)
    {
        for(int i = 0; i < _contents.length; i++)
        {
            if(stack.matches(_contents[i]))
            {
                return i;
            }
        }
        return -1;
    }
    
    public int first(Material material)
    {
        return first(material, (short)-1);
    }
    
    public int first(Material material, short meta)
    {
        return first(material == null ? 0 : material.getId(), meta);
    }
    
    public int first(ItemStack item)
    {
        return first(new ComparativeItemStack(item));
    }
    
    public int firstEmpty()
    {
        return first(0);
    }
    
    public void remove(int id)
    {
        remove(id, (short)-1);
    }
    
    public void remove(int id, short meta)
    {
        remove(new ComparativeItemStack(id, meta));
    }
    
    public void remove(ComparativeItemStack stack)
    {
        for(int i = 0; i < _contents.length; i++)
        {
            if(stack.matches(_contents[i]))
            {
                _contents[i] = null;
            }
        }
    }
    
    public void remove(Material material)
    {
        remove(material, (short)-1);
    }
    
    public void remove(Material material, short meta)
    {
        remove(material == null ? 0 : material.getId(), meta);
    }
    
    public void remove(ItemStack item)
    {
        remove(new ComparativeItemStack(item));
    }
    
    public void clear()
    {
        _contents = new ItemStack[_contents.length];
    }
    
    public void clear(int index)
    {
        setItem(index, null);
    }
    
    public List<HumanEntity> getViewers()
    {
        return new ArrayList<HumanEntity>();
    }
    
    public InventoryHolder getHolder()
    {
        return null;
    }
    
    public ListIterator<ItemStack> iterator()
    {
        return getContentsList().listIterator();
    }
    
    public ListIterator<ItemStack> iterator(int index)
    {
        return getContentsList().listIterator(index);
    }
    
    private List<ItemStack> getContentsList()
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        for(ItemStack item : _contents)
        {
            if(item != null)
            {
                list.add(item);
            }
        }
        return list;
    }
    
    public HashMap<Integer, ItemStack> all(int id)
    {
        return all(id, (short)-1);
    }
    
    public HashMap<Integer, ItemStack> all(int id, short meta)
    {
        return all(new ComparativeItemStack(id, meta));
    }
    
    public HashMap<Integer, ItemStack> all(Material material)
    {
        return all(material, (short)-1);
    }
    
    public HashMap<Integer, ItemStack> all(Material material, short meta)
    {
        return all(material == null ? 0 : material.getId(), meta);
    }
    
    public HashMap<Integer, ItemStack> all(ItemStack item)
    {
        return all(new ComparativeItemStack(item));
    }
    
    public HashMap<Integer, ItemStack> all(ComparativeItemStack stack)
    {
        HashMap<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
        for(int i = 0; i < _contents.length; i++)
        {
            if(stack.matches(_contents[i]))
            {
                map.put(i, _contents[i]);
            }
        }
        return map;
    }
    
    /*public abstract boolean contains(int id)
    {
        return contains(id, 1);
    }
    
    public abstract boolean contains(int id, int amount)
    {
        return contains(id, amount, (short)-1);
    }
    
    public abstract boolean contains(int id, int amount)
    {
        return contains(id, amount, (short)-1);
    }
    
    public abstract boolean contains(Material material)
    {
        
    }
    
    public abstract boolean contains(ItemStack item)
    {
        
    }
    
    public abstract boolean contains(Material paramMaterial, int amount)
    {
        
    }
    
    public abstract boolean contains(ItemStack paramItemStack, int amount)
    {
        
    }
    
    public abstract boolean containsAtLeast(ItemStack paramItemStack, int amount)
    {
        
    }*/
    
    public void onClose(HumanEntity player)
    {
    }
}