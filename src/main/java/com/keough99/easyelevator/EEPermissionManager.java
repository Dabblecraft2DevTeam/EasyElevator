/**
 * This code is licensed under the GNU GPLv3 as viewable on https://dev.bukkit.org/projects/easyelevator
 *
 * It was modified on the 27th of July 2017 by AlvinB
 */
package com.keough99.easyelevator;

import org.bukkit.entity.Player;

public class EEPermissionManager {
   Player player;
   String admin = "easyelevator.admin";

   public EEPermissionManager(Player p) {
      this.player = p;
   }

   public boolean has(String permission) {
      return this.isAdmin() ? true : this.player.hasPermission(permission);
   }

   private boolean isAdmin() {
      return this.player.isOp() ? true : this.player.hasPermission(this.admin);
   }
}
