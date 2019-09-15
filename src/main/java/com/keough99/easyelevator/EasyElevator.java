/**
 * This code is licensed under the GNU GPLv3 as viewable on https://dev.bukkit.org/projects/easyelevator
 *
 * It was modified on the 27th of July 2017 by AlvinB
 */
package com.keough99.easyelevator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyElevator extends JavaPlugin {
   private EEPlayerListener pl = new EEPlayerListener(this);
   private EEConfiguration config = new EEConfiguration();
   public List<Elevator> elevators = new ArrayList<>();
   private int MaxPerimeter = 24;
   private int MaxFloors = 10;
   private boolean ArrivalSound = true;
   private boolean ArrivalMessage = true;
   private String BlockBorder = "41";
   private String BlockFloor = "42";
   private String BlockOutputDoor = "35:14";
   private String BlockOutputFloor = "35:1";

   public void onEnable() {
      PluginManager pm = this.getServer().getPluginManager();
      pm.registerEvents(this.pl, this);
      this.config.createConfig();
      this.MaxPerimeter = this.config.getMaxPerimeter();
      this.MaxFloors = this.config.getMaxFloors();
      this.ArrivalSound = this.config.getArrivalSound();
      this.ArrivalMessage = this.config.getArrivalMessage();
      this.BlockBorder = this.config.getBlock("Border");
      this.BlockFloor = this.config.getBlock("Floor");
      this.BlockOutputFloor = this.config.getBlock("OutputFloor");
      this.BlockOutputDoor = this.config.getBlock("OutputDoor");
      this.getLogger().info("This plugin was modified by AlvinB on the 27th of July 2017"); // Added by AlvinB
   }

   public void onDisable() {
      Iterator var2 = this.elevators.iterator();

      while(var2.hasNext()) {
         Elevator e = (Elevator)var2.next();
         if (e.currentFloor != null) {
            e.currentFloor.switchRedstoneFloorOn(false);
         }
      }

   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel1, String[] args) {
      if ((commandLabel1.equals("elv") || commandLabel1.equals("eelevator")) && sender instanceof Player) {
         Player player = (Player)sender;
         EEPermissionManager pm = new EEPermissionManager(player);
         int target;
         if (args.length == 1) {
            if (args[0].equals("reload")) {
               if (!pm.has("easyelevator.reload")) {
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
               } else {
                  this.MaxPerimeter = this.config.getMaxPerimeter();
                  this.MaxFloors = this.config.getMaxFloors();
                  this.ArrivalSound = this.config.getArrivalSound();
                  this.ArrivalMessage = this.config.getArrivalMessage();
                  this.BlockBorder = this.config.getBlock("Border");
                  this.BlockFloor = this.config.getBlock("Floor");
                  this.BlockOutputFloor = this.config.getBlock("OutputFloor");
                  this.BlockOutputDoor = this.config.getBlock("OutputDoor");
                  Iterator var8 = this.elevators.iterator();

                  while(var8.hasNext()) {
                     Elevator e = (Elevator)var8.next();
                     if (e.currentFloor != null) {
                        e.currentFloor.switchRedstoneFloorOn(false);
                     }
                  }

                  this.elevators.clear();
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The plugin has been reloaded");
               }
            }

            if (args[0].equals("call")) {
               if (!pm.has("easyelevator.call.cmd") && !pm.has("easyelevator.call.*")) {
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
               } else {
                  boolean success = this.Call(player);
                  if (success) {
                     player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator has been called");
                  } else {
                     player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "No Elevator in range");
                  }
               }
            }

            if (args[0].equals("stop")) {
               if (!pm.has("easyelevator.stop.cmd") && !pm.has("easyelevator.stop.*")) {
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
               } else {
                  for(target = 0; target < this.elevators.size(); ++target) {
                     Elevator e = (Elevator)this.elevators.get(target);
                     if (e.isInElevator(player)) {
                        int target1 = e.getFloorNumberFromHeight(e.getNextFloorHeight_2());
                        if (target1 != -1) {
                           e.addStops(target);
                           player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + target);
                           return true;
                        }
                     }
                  }
               }
            }
         }

         if (args.length == 2) {
            if (!args[0].equals("stop")) {
               player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
            } else if (pm.has("easyelevator.stop.cmd") || pm.has("easyelevator.stop.*")) {
               try {
                  target = Integer.parseInt(args[1]);

                  for(int i = 0; i < this.elevators.size(); ++i) {
                     Elevator e = (Elevator)this.elevators.get(i);
                     if (e.isInElevator(player)) {
                        if (target > e.getFloors().size() || target < 1) {
                           player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Floor '" + target + "' is not in range");
                           return true;
                        }

                        e.addStops(target);
                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + target);
                        i = this.elevators.size();
                     }
                  }
               } catch (Exception var10) {
                  player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Floor '" + args[1] + "' is not a valid value");
                  return true;
               }
            }
         }
      }

      return true;
   }

   private boolean Call(Player player) {
      Sign sign = this.getSurroundingElevatorSign(player);
      if (sign != null) {
         Elevator e = this.getElevator(sign);
         if (e != null) {
            e.Call(sign.getY());
            return true;
         }
      }

      return false;
   }

   private Sign getSurroundingElevatorSign(Player player) {
      Block tempBlock = null;
      World world = player.getWorld();
      Location loc = player.getLocation();
      Location l1 = null;
      Location l2 = null;
      int x1 = loc.getBlockX();
      int y1 = loc.getBlockY();
      int z1 = loc.getBlockZ();
      int x2 = loc.getBlockX();
      int y2 = loc.getBlockY();
      int z2 = loc.getBlockZ();
      int xStart = 0;
      int xEnd = 0;
      int yStart = 0;
      int yEnd = 0;
      int zStart = 0;
      int zEnd = 0;
      if (x1 < x2) {
         xStart = x1;
         xEnd = x2;
      }

      if (x1 > x2) {
         xStart = x2;
         xEnd = x1;
      }

      if (x1 == x2) {
         xStart = x1;
         xEnd = x1;
      }

      if (z1 < z2) {
         zStart = z1;
         zEnd = z2;
      }

      if (z1 > z2) {
         zStart = z2;
         zEnd = z1;
      }

      if (z1 == z2) {
         zStart = z1;
         zEnd = z1;
      }

      if (y1 < y2) {
         yStart = y1;
         yEnd = y2;
      }

      if (y1 > y2) {
         yStart = y2;
         yEnd = y1;
      }

      if (y1 == y2) {
         yStart = y1;
         yEnd = y1;
      }

      xStart -= 5;
      yStart += 0;
      zStart -= 5;
      xEnd += 5;
      yEnd += 2;
      zEnd += 5;

      for(int i = xStart; i <= xEnd; ++i) {
         int x = i;

         for(int j = yStart; j <= yEnd; ++j) {
            int y = j;

            for(int k = zStart; k <= zEnd; ++k) {
               tempBlock = world.getBlockAt(x, y, k);
               if (tempBlock.getType().equals(Material.WALL_SIGN) || tempBlock.getType().equals(Material.SIGN) || tempBlock.getType().equals(Material.SIGN_POST)) {
                  Sign sign = (Sign)tempBlock.getState();
                  if (sign.getLine(0).equals(ChatColor.DARK_GRAY + "[EElevator]")) {
                     boolean isPS = false;
                     Iterator var28 = this.elevators.iterator();

                     while(var28.hasNext()) {
                        Elevator e = (Elevator)var28.next();
                        if (e.getPlatform().getSign().equals(sign)) {
                           isPS = true;
                        }
                     }

                     if (!isPS) {
                        return (Sign)tempBlock.getState();
                     }
                  }
               }

               tempBlock = null;
            }
         }
      }

      return null;
   }

   public Elevator getElevator(Sign sign) {
      if (sign.getLine(0).equals("[EElevator]") || sign.getLine(0).equals(ChatColor.DARK_GRAY + "[EElevator]")) {
         Elevator e = null;

         for(int i = 0; i < this.elevators.size(); ++i) {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign)sign.getData();
            Block attached = sign.getBlock().getRelative(signData.getAttachedFace());
            if (((Elevator)this.elevators.get(i)).isPartOfElevator(attached.getLocation()) && (((Elevator)this.elevators.get(i)).isFloorSign(sign) || ((Elevator)this.elevators.get(i)).isPlatformSign(sign))) {
               e = (Elevator)this.elevators.get(i);
               i = this.elevators.size();
            }
         }

         if (e == null) {
            e = new Elevator(this, sign);
         }

         if (e != null && e.isInitialized()) {
            if (!this.elevators.contains(e)) {
               this.elevators.add(e);
            }

            return e;
         }
      }

      return null;
   }

   public int getMaxPerimeter() {
      return this.MaxPerimeter;
   }

   public int getMaxFloors() {
      return this.MaxFloors;
   }

   public boolean getArrivalSound() {
      return this.ArrivalSound;
   }

   public boolean getArrivalMessage() {
      return this.ArrivalMessage;
   }

   public String getBlockBorder() {
      return this.BlockBorder;
   }

   public String getBlockFloor() {
      return this.BlockFloor;
   }

   public String getBlockOutputDoor() {
      return this.BlockOutputDoor;
   }

   public String getBlockOutputFloor() {
      return this.BlockOutputFloor;
   }
}
