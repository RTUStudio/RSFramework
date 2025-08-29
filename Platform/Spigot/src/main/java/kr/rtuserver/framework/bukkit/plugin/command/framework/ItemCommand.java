package kr.rtuserver.framework.bukkit.plugin.command.framework;

import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtuserver.framework.bukkit.api.registry.CustomItems;
import kr.rtuserver.framework.bukkit.plugin.RSFramework;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

public class ItemCommand extends RSCommand<RSFramework> {

    public ItemCommand(RSFramework plugin) {
        super(plugin, "item", PermissionDefault.OP);
    }

    @Override
    public boolean execute(RSCommandData data) {
        Player player = player();
        if (player == null) {
            chat().announce(message().getCommon(player(), MessageTranslation.ONLY_PLAYER));
            return true;
        }
        if (data.length(1)) {
            EntityEquipment equipment = player.getEquipment();
            if (equipment != null) {
                ItemStack itemStack = equipment.getItemInMainHand();
                String id = CustomItems.to(itemStack);
                chat().announce(message().get(player(), "command.item").replace("{id}", id));
            } else {
                chat().announce(message().getCommon(player(), MessageTranslation.NOT_FOUND_ITEM));
                return true;
            }
        } else {
            String id = data.args(1);
            ItemStack itemStack = CustomItems.from(id);
            if (itemStack == null) {
                chat().announce(message().getCommon(player(), MessageTranslation.NOT_FOUND_ITEM));
                return true;
            }
            player().getInventory().addItem(itemStack);
        }
        return true;
    }
}
