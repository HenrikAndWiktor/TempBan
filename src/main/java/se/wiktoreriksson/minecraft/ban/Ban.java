package se.wiktoreriksson.minecraft.ban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public final class Ban extends JavaPlugin implements Listener {
    /**
     * Menu of ban options
     */
    public static Inventory banmenu = Bukkit.createInventory(null, 27, ChatColor.RED+""+ChatColor.BOLD+"Ban Menu");
    // The first parameter, is the inventory owner. I make it null to let everyone use it.
    //The second parameter, is the slots in a inventory. Must be a multiple of 9. Can be up to 54.
    //The third parameter, is the inventory name. This will accept chat colors.
    private YamlConfiguration ban;
    private HashMap<String,OfflinePlayer> bancmd;


    static {
        ItemStack cancel = new ItemStack(Material.BARRIER,1);
        ItemMeta cim = cancel.getItemMeta();
        cim.setDisplayName(ChatColor.RED+"Cancel");
        cancel.setItemMeta(cim);
        banmenu.setItem(26,cancel);
        ItemStack killaura = new ItemStack(Material.DIAMOND_SWORD, 7);
        ItemMeta kaim = killaura.getItemMeta();
        kaim.setDisplayName(ChatColor.BLUE+"PvP Hacks (7 day, Reason:\"PvP Hacks used\"");
        kaim.addEnchant(Enchantment.MENDING,1,true);
        kaim.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        killaura.setItemMeta(kaim);
        banmenu.setItem(0, killaura);
        ItemStack xray = new ItemStack(Material.DIAMOND_PICKAXE, 4);
        ItemMeta xrayim = xray.getItemMeta();
        xrayim.setDisplayName(ChatColor.AQUA +"XRay (4 day, Reason:\"XRay Used\"");
        xrayim.addEnchant(Enchantment.MENDING,1,true);
        xrayim.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        xray.setItemMeta(xrayim);
        banmenu.setItem(1, xray);
        ItemStack reach = new ItemStack(Material.STICK, 2);
        ItemMeta rim = xray.getItemMeta();
        rim.setDisplayName(ChatColor.YELLOW +"Reach (2 day, Reason:\"Reach Used\"");
        rim.addEnchant(Enchantment.MENDING,1,true);
        rim.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        reach.setItemMeta(rim);
        banmenu.setItem(2, reach);
        ItemStack fly = new ItemStack(Material.FEATHER, 7);
        ItemMeta flyim = xray.getItemMeta();
        flyim.setDisplayName(ChatColor.YELLOW +"Fly (7 day, Reason:\"Fly Hacks Used\"");
        flyim.addEnchant(Enchantment.MENDING,1,true);
        flyim.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        fly.setItemMeta(flyim);
        banmenu.setItem(3, fly);
    }
    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this,this);
        try {
            //noinspection ResultOfMethodCallIgnored
            new File("bans.yml").createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ban = YamlConfiguration.loadConfiguration(new File("bans.yml"));
        bancmd=new HashMap<>();
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked(); // The player that clicked the item
        ItemStack clicked = event.getCurrentItem(); // The item that was clicked
        Inventory inventory = event.getInventory(); // The inventory that was clicked in
        if (inventory.getName().equals(banmenu.getName())) { // The inventory is our custom Inventory
            if (clicked.getType() == Material.DIAMOND_SWORD) { // PVP
                event.setCancelled(true); // Make it so the sword is back in its original spot
                player.closeInventory();
                banPlayer(new BanData(System.currentTimeMillis()+604800000,"PvP Hacks Used",bancmd.get(player.getName())));
            }
            if (clicked.getType() == Material.DIAMOND_PICKAXE) { // XRay
                event.setCancelled(true); // Make it so the pickaxe is back in its original spot
                player.closeInventory();
                banPlayer(new BanData(System.currentTimeMillis()+345600000,"XRay Used",bancmd.get(player.getName())));
            }
            if (clicked.getType() == Material.STICK) { // Reach
                event.setCancelled(true); // Make it so the stick is back in its original spot
                player.closeInventory();
                banPlayer(new BanData(System.currentTimeMillis()+172800000,"Reach Used",bancmd.get(player.getName())));
            }
            if (clicked.getType() == Material.FEATHER) { // Fly
                event.setCancelled(true); // Make it so the feather is back in its original spot
                player.closeInventory();
                banPlayer(new BanData(System.currentTimeMillis()+604800000,"Fly Hacks Used",bancmd.get(player.getName())));
            }
            if (clicked.getType() == Material.BARRIER) { // Cancel
                event.setCancelled(true); // Make it so the barrier is back in its original spot
                player.closeInventory();
            }
            bancmd.remove(player.getName());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)||!sender.isOp())return false;
        if (label.equalsIgnoreCase("tempban")) {
            bancmd.put(sender.getName(),getServer().getOfflinePlayer(args[0]));
            ((Player) sender).openInventory(banmenu);
        } else if (label.equalsIgnoreCase("unban")) {
            ban.set("ban."+getServer().getOfflinePlayer(args[0]).getUniqueId().toString().replace("-","")+".timestamp",-1);
        }
        return true;
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent pje) {
        BanData data;
        if ((data=isBanned(pje.getPlayer()))!=null) {
            final long milliseconds = data.getTimestampexpire()-System.currentTimeMillis();
            if (milliseconds<0) ban.set("ban."+data.getPlayer().getUniqueId().toString().replace("-","")+".timestamp",-1);
            final long day = TimeUnit.MILLISECONDS.toDays(milliseconds);
            final long hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
                    - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(milliseconds));
            final long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                    - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));
            final long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
            System.out.println("seconds = " + seconds);
            System.out.println("minutes = " + minutes);
            System.out.println("hours = " + hours);
            System.out.println("day = " + day);
            System.out.println("milliseconds = " + milliseconds);
            System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
            pje.getPlayer().kickPlayer(
                    "You are banned from this server for:\n"+
                    (day != 0 ? day+" days " : "")+
                    (hours != 0 ? hours+" hours " : "")+
                    (minutes != 0 ? minutes+" minutes " : "")+
                    (seconds != 0 ? seconds+" seconds " : "")+
                    "Reason: "+data.getReason()
            );
        }

    }

    private BanData isBanned(OfflinePlayer player) {
        String reason;
        long timestamp;
        if ((timestamp=ban.getLong("ban."+player.getUniqueId().toString().replace("-","")+".timestamp",-1))==-1) return null;
        if ((reason=ban.getString("ban."+player.getUniqueId().toString().replace("-","")+".reason",null))==null) return null;
        return new BanData(timestamp,reason,player);
    }

    private void banPlayer(BanData banData) {
        ban.set("ban."+banData.getPlayer().getUniqueId().toString().replace("-","")+".timestamp",banData.getTimestampexpire());
        ban.set("ban."+banData.getPlayer().getUniqueId().toString().replace("-","")+".reason",banData.getReason());
        final long milliseconds = banData.getTimestampexpire()-System.currentTimeMillis();
        final long day = TimeUnit.MILLISECONDS.toDays(milliseconds);
        final long hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(milliseconds));
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
        System.out.println("seconds = " + seconds);
        System.out.println("minutes = " + minutes);
        System.out.println("hours = " + hours);
        System.out.println("day = " + day);
        System.out.println("milliseconds = " + milliseconds);
        System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
        Player p;
        if ((p=banData.getPlayer().getPlayer())!=null) {
                p.kickPlayer(
                "You are banned from this server for:\n"+
                        (day != 0 ? day+" days " : "")+
                        (hours != 0 ? hours+" hours " : "")+
                        (minutes != 0 ? minutes+" minutes " : "")+
                        (seconds != 0 ? seconds+" seconds " : "")+
                        "\n\n\nReason: "+banData.getReason()
            );
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            ban.save("bans.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
