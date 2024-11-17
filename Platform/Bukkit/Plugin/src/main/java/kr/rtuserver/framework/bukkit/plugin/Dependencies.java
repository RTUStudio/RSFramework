//package kr.rtuserver.framework.plugin;
//
//import com.comphenix.protocol.ProtocolLibrary;
//import com.comphenix.protocol.ProtocolManager;
//import lombok.Getter;
//import net.luckperms.api.LuckPerms;
//import net.milkbowl.vault.economy.Economy;
//import org.bukkit.Bukkit;
//import org.bukkit.plugin.RegisteredServiceProvider;
//import org.bukkit.plugin.ServicesManager;
//
//@Getter
//public class Dependencies {
//
//    private Economy economy;
//    private LuckPerms luckPerms;
//    private ProtocolManager protocol;
//
//    public Dependencies(RSFramework plugin) {
//        ServicesManager manager = Bukkit.getServicesManager();
//        if (plugin.isEnabledDependency("Vault")) {
//            RegisteredServiceProvider<Economy> provider = manager.getRegistration(Economy.class);
//            if (provider != null) economy = provider.getProvider();
//        }
//        if (plugin.isEnabledDependency("LuckPerms")) {
//            RegisteredServiceProvider<LuckPerms> provider = manager.getRegistration(LuckPerms.class);
//            if (provider != null) luckPerms = provider.getProvider();
//        }
//        if (plugin.isEnabledDependency("ProtocolLib")) {
//            protocol = ProtocolLibrary.getProtocolManager();
//        }
//    }
//}
