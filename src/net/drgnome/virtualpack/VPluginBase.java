// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.sql.*;
import java.net.*;

import net.minecraft.server.*;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.configuration.file.*;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.virtualpack.Config.*;
import static net.drgnome.virtualpack.Lang.*;
import static net.drgnome.virtualpack.Util.*;

public abstract class VPluginBase extends JavaPlugin
{
    public static final String version = "1.0.5.1";
    protected HashMap<String, VPack> packs;
    private int saveTick;
    private int upTick;
    private boolean update;
    private VThreadSave saveThread;
    private boolean loadRequested;
    private boolean waitForPlugin;

    public void onEnable()
    {
        waitForPlugin = false;
        try
        {
            if(!org.anjocaido.groupmanager.GroupManager.isLoaded())
            {
                waitForPlugin = true;
            }
            else
            {
                init();
            }
        }
        catch(java.lang.NoClassDefFoundError e)
        {
            init();
        }
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new VThread(this), 0L, 1L);
    }
    
    private void init()
    {
        log.info("Enabling VirtualPack " + version);
        waitForPlugin = false;
        saveTick = 0;
        update = false;
        upTick = 60 * 60 * 20;
        loadRequested = false;
        packs = new HashMap<String, VPack>();
        checkFiles();
        initLang(getDataFolder());
        reloadConf(getConfig());
        saveConfig();
        reloadLang();
        if(!initPerms())
        {
            getPluginLoader().disablePlugin(this);
            return;
        }
        economyDisabled = getConfigString("economy-disabled").equalsIgnoreCase("true") ? true : false;
        if(!initEconomy())
        {
            getPluginLoader().disablePlugin(this);
            return;
        }
        loadUserData();
        log.info(lang("vpack.enable", new String[]{version}));
    }

    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        if(!waitForPlugin)
        {
            log.info(lang("vpack.startdisable", new String[]{version}));
            saveUserData();
            log.info(lang("vpack.disable", new String[]{version}));
        }
    }
    
    public synchronized void tick()
    {
        if(waitForPlugin)
        {
            try
            {
                if(org.anjocaido.groupmanager.GroupManager.isLoaded())
                {
                    init();
                }
                else
                {
                    return;
                }
            }
            catch(Exception e)
            {
                init();
            }
        }
        Object values[] = packs.values().toArray();
        for(int i = 0; i < values.length; i++)
        {
            if(values[i] == null)
            {
                continue;
            }
            ((VPack)values[i]).tick();
        }
        if(!update)
        {
            upTick++;
            if(upTick >= 60 * 60 * 20)
            {
                checkForUpdate();
                upTick = 0;
            }
        }
        if(loadRequested)
        {
            loadUserData();
        }
        if(getConfigInt("save-interval") > 0)
        {
            saveTick++;
            if(saveTick >= getConfigInt("save-interval") * 20)
            {
                log.info("[VirtualPack] Saving user data...");
                saveUserData();
                loadUserData();
                saveTick = 0;
            }
        }
    }
    
    private void checkForUpdate()
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection)(new URL("http://dev.drgnome.net/version.php?t=vpack")).openConnection();            
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");                        
            con.setRequestProperty("Pragma", "no-cache");
            con.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            StringBuilder stringb = new StringBuilder();
            if((line = reader.readLine()) != null)
            {
                stringb.append(line);
            }
            String vdigits[] = this.version.toLowerCase().split("\\.");
            String cdigits[] = stringb.toString().toLowerCase().split("\\.");
            int max = vdigits.length > cdigits.length ? cdigits.length : vdigits.length;
            for(int i = 0; i < max; i++)
            {
                try
                {
                    if(Integer.parseInt(cdigits[i]) > Integer.parseInt(vdigits[i]))
                    {
                        update = true;
                        break;
                    }
                    else if(Integer.parseInt(cdigits[i]) < Integer.parseInt(vdigits[i]))
                    {
                        update = false;
                        break;
                    }
                    else if((i == max - 1) && (cdigits.length > vdigits.length))
                    {
                        update = true;
                        break;
                    }
                }
                catch(Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        reloadConf(getConfig());
        saveConfig();
        reloadLang();
    }
    
    private void checkFiles()
    {
        try
        {
            File file = getDataFolder();
            if(!file.exists())
            {
                file.mkdirs();
            }
            String files[] = {"config.yml", "data.db"};
            for(int i = 0; i < files.length; i++)
            {
                File data = new File(file, files[i]);
                if(!data.exists())
                {
                    PrintStream writer = new PrintStream(new FileOutputStream(data));
                    writer.close();
                }
            }
        }
        catch(Exception e)
        {
        }
    }
    
    protected synchronized void loadUserData()
    {
        if((saveThread != null) && !(saveThread.done()))
        {
            loadRequested = true;
            return;
        }
        try
		{
            /*if(getConfigString("db.use").equalsIgnoreCase("true"))
            {
                Connection = DriverManager.getConnection(getConfigString("db.url"), getConfigString("db.user"), getConfigString("db.pw"));
            }
            else
            {*/
                BufferedReader file = new BufferedReader(new FileReader(new File(getDataFolder(), "data.db")));
                String line;
                String data[];
                while((line = file.readLine()) != null)
                {
                    data = line.split(separator[0]);
                    if(data.length >= 2)
                    {
                        putPack(data[0], new VPack(data[0].toLowerCase(), data, 1));
                    }
                }
                file.close();
            //}
		}
		catch(Exception e)
		{
            warn();
            e.printStackTrace();
		}
        loadRequested = false;
    }
    
    protected synchronized void saveUserData()
    {
        saveThread = new VThreadSave(new File(getDataFolder(), "data.db"), packs);
        saveThread.run();
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(waitForPlugin)
        {
            sender.sendMessage("VirtualPack is waiting for GroupManager.");
            return true;
        }
        if(update && (!(sender instanceof Player) || (sender.hasPermission("vpack.update"))))
        {
            sendMessage(sender, lang("update.msg"), ChatColor.GREEN);
            sendMessage(sender, lang("update.link"), ChatColor.RED);
        }
        if((args.length <= 0) || ((args.length >= 1) && (args[0].equals("help"))))
        {
            cmdHelp(sender, args);
            return true;
        }
        args[0] = longname(args[0]);
        if(args[0].equals("version"))
        {
            sendMessage(sender, lang("version", new String[]{version}), ChatColor.BLUE);
            return true;
        }
        else if(args[0].equals("update"))
        {
            if(!sender.hasPermission("vpack.update"))
            {
                sendMessage(sender, lang("update.perm"), ChatColor.RED);
                return true;
            }
            checkForUpdate();
            if(update)
            {
                sendMessage(sender, lang("update.msg"), ChatColor.GREEN);
                sendMessage(sender, lang("update.link"), ChatColor.RED);
            }
            else
            {
                sendMessage(sender, lang("update.no"), ChatColor.GREEN);
            }
            return true;
        }
        else if((args[0].equals("stats")) && !(sender instanceof Player))
        {
            cmdConsoleStats(sender, args);
            return true;
        }
        else if(args[0].equals("admin"))
        {
            cmdAdmin(sender, args);
            if((args.length < 2) || (!args[1].equals("use")))
            {
                return true;
            }
        }
        else if(!(sender instanceof Player))
        {
            sendMessage(sender, lang("use.player"), ChatColor.RED);
            return true;
        }
        else if(!sender.hasPermission("vpack.use"))
        {
            sendMessage(sender, lang("use.perm"), ChatColor.RED);
            return true;
        }
        try
        {
            if(args[0].equals("admin"))
            {
                cmdAdminUse(sender, args);
            }
            else if(args[0].equals("stats"))
            {
                cmdStats(sender, args);
            }
            else if(args[0].equals("price"))
            {
                cmdPrices(sender, args);
            }
            else if(args[0].equals("workbench"))
            {
                cmdWorkbench(sender, args);
            }
            else if(args[0].equals("uncrafter"))
            {
                cmdUncrafter(sender, args);
            }
            else if(args[0].equals("enchanttable"))
            {
                cmdEnchanttable(sender, args);
            }
            else if(args[0].equals("chest"))
            {
                cmdChest(sender, args);
            }
            else if(args[0].equals("furnace"))
            {
                cmdFurnace(sender, args);
            }
            else if(args[0].equals("brewingstand"))
            {
                cmdBrewingstand(sender, args);
            }
            else if(args[0].equals("trash"))
            {
                cmdTrash(sender, args);
            }
            else if(args[0].equals("debug"))
            {
                cmdDebug(sender, args);
            }
            else
            {
                sendMessage(sender, lang("argument.unknown"), ChatColor.RED);
            }
        }
        catch(Exception e)
        {
            sendMessage(sender, lang("argument.error"), ChatColor.RED);
            warn();
            e.printStackTrace();
        }
        return true;
    }
    
    protected abstract void cmdHelp(CommandSender sender, String[] args);
    protected abstract void cmdConsoleStats(CommandSender sender, String[] args);
    protected abstract void cmdAdmin(CommandSender sender, String[] args);
    protected abstract void cmdAdminUse(CommandSender sender, String[] args);
    protected abstract void cmdStats(CommandSender sender, String[] args);
    protected abstract void cmdPrices(CommandSender sender, String[] args);
    protected abstract void cmdWorkbench(CommandSender sender, String[] args);
    protected abstract void cmdEnchanttable(CommandSender sender, String[] args);
    protected abstract void cmdUncrafter(CommandSender sender, String[] args);
    protected abstract void cmdChest(CommandSender sender, String[] args);
    protected abstract void cmdFurnace(CommandSender sender, String[] args);
    protected abstract void cmdBrewingstand(CommandSender sender, String[] args);
    protected abstract void cmdTrash(CommandSender sender, String[] args);
    protected abstract void cmdDebug(CommandSender sender, String[] args);
    
    public String longname(String s)
    {
        s = s.toLowerCase().trim();
        if(s.length() > 2)
        {
            return s;
        }
        if(s.equals("a"))
        {
            return "admin";
        }
        if(s.equals("w"))
        {
            return "workbench";
        }
        if(s.equals("uc"))
        {
            return "uncrafter";
        }
        if(s.equals("e"))
        {
            return "enchanttable";
        }
        if(s.equals("c"))
        {
            return "chest";
        }
        if(s.equals("f"))
        {
            return "furnace";
        }
        if(s.equals("b"))
        {
            return "brewingstand";
        }
        if(s.equals("l"))
        {
            return "link";
        }
        if(s.equals("u"))
        {
            return "unlink";
        }
        if(s.equals("t"))
        {
            return "trash";
        }
        if(s.equals("v"))
        {
            return "version";
        }
        if(s.equals("s"))
        {
            return "stats";
        }
        if(s.equals("p"))
        {
            return "price";
        }
        if(s.equals("d"))
        {
            return "debug";
        }
        if(s.equals("up"))
        {
            return "update";
        }
        return s;
    }
    
    public boolean hasPack(String name)
    {
        name = name.toLowerCase();
        return !(packs.get(name) == null);
    }
    
    public VPack getPack(String name)
    {
        name = name.toLowerCase();
        VPack pack = packs.get(name);
        if(pack == null)
        {
            pack = new VPack(name);
            putPack(name, pack);
        }
        return pack;
    }
    
    public void putPack(String name, VPack pack)
    {
        name = name.toLowerCase();
        packs.put(name, pack);
    }
}