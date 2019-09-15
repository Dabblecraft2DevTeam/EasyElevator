/**
 * This code is licensed under the GNU GPLv3 as viewable on https://dev.bukkit.org/projects/easyelevator
 *
 * It was modified on the 27th of July 2017 by AlvinB
 */
package com.keough99.easyelevator;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EEPlayerListener implements Listener {
   EasyElevator plugin;

   public EEPlayerListener(EasyElevator pl) {
      this.plugin = pl;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player;
      EEPermissionManager pm;
      Block clicked;
      Sign sign;
      Elevator e;
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         player = event.getPlayer();
         pm = new EEPermissionManager(player);
         clicked = event.getClickedBlock();
         if (clicked.getState() instanceof Sign) {
            sign = (Sign)clicked.getState();
            e = this.plugin.getElevator(sign);
            if (e != null) {
               if ((pm.has("easyelevator.call.sign") || pm.has("easyelevator.call.*")) && e.isFloorSign(sign)) {
                  e.Call(sign.getY());
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator has been called");
                  return;
               }

               if (e.isPlatformSign(sign)) {
                  e.changeFloor();
                  return;
               }
            }
         }
      }

      if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
         player = event.getPlayer();
         pm = new EEPermissionManager(player);
         clicked = event.getClickedBlock();
         if (clicked.getState() instanceof Sign) {
            sign = (Sign)clicked.getState();
            e = this.plugin.getElevator(sign);
            if (e != null) {
               if (!pm.has("easyelevator.stop.sign") && !pm.has("easyelevator.stop.*")) {
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
               } else if (e.isPlatformSign(sign)) {
                  e.StopAt(Integer.parseInt(e.getPlatform().getSign().getLine(1)));
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + Integer.parseInt(e.getPlatform().getSign().getLine(1)));
               }
            }
         }
      }

   }

   @EventHandler
   public void onBlockPlace(BlockRedstoneEvent event) {
   }
}
