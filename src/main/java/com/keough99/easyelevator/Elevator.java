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
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class Elevator implements Runnable {
   public EasyElevator plugin;
   private Sign s;
   private Block attached;
   private World world;
   private int highestPoint;
   private int lowestPoint;
   private int xLow;
   private int xHigh;
   private int zLow;
   private int zHigh;
   private int maxFloors = -1;
   private int maxPerimeter = -1;
   private List<Integer> stops = new ArrayList();
   public Floor currentFloor = null;
   private String Direction = "";
   private boolean isMoving = false;
   private boolean hasOpenDoor = false;
   private boolean isInitialized = false;
   private List<Floor> floors = new ArrayList();
   private Platform platform;
   int lcount = 0;

   public Elevator(EasyElevator elev, Sign s) {
      this.plugin = elev;
      this.world = s.getWorld();
      this.s = s;
      this.maxFloors = elev.getMaxFloors();
      this.maxPerimeter = elev.getMaxPerimeter();
      org.bukkit.material.Sign signData = (org.bukkit.material.Sign)s.getData();
      this.attached = s.getBlock().getRelative(signData.getAttachedFace());
      this.initializeLift();
   }

   private void initializeLift() {
      int count = 0;
      int low = -1;
      int high = -1;

      int i;
      Block b2;
      for(i = this.s.getY(); i >= 0; --i) {
         b2 = this.world.getBlockAt(this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
         if (this.isBorder(b2)) {
            low = i;
            i = -1;
         }
      }

      for(i = this.s.getY(); i < this.world.getMaxHeight(); ++i) {
         b2 = this.world.getBlockAt(this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
         if (this.isBorder(b2)) {
            high = i;
            i = this.world.getMaxHeight();
         }
      }

      if (low != -1 && high != -1) {
         this.highestPoint = high;
         this.lowestPoint = low;
         Block b1 = null;
         b2 = null;

         for(i = low; i < high; ++i) {
            Location currLoc = new Location(this.world, (double)this.attached.getLocation().getBlockX(), (double)i, (double)this.attached.getLocation().getBlockZ());
            Block target = this.world.getBlockAt(currLoc);
            if (this.isFloor(target)) {
               int dirChange = 0;
               String dir = "";
               List<Block> blocks = new ArrayList();
               Block Start = target;
               Block t = null;
               b2 = null;
               b1 = null;

               do {
                  Block temp = null;
                  if (t == null) {
                     if (temp == null) {
                        temp = this.checkForIron(Start, Start.getRelative(0, 0, 1), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "East")) {
                              ++dirChange;
                           }

                           dir = "East";
                        }
                     }

                     if (temp == null) {
                        temp = this.checkForIron(Start, Start.getRelative(0, 0, -1), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "West")) {
                              ++dirChange;
                           }

                           dir = "West";
                        }
                     }

                     if (temp == null) {
                        temp = this.checkForIron(Start, Start.getRelative(1, 0, 0), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "North")) {
                              ++dirChange;
                           }

                           dir = "North";
                        }
                     }

                     if (temp == null) {
                        temp = this.checkForIron(Start, Start.getRelative(-1, 0, 0), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "South")) {
                              ++dirChange;
                           }

                           dir = "South";
                        }
                     }
                  } else if (t != null) {
                     if (temp == null) {
                        temp = this.checkForIron(Start, t.getRelative(0, 0, 1), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "East")) {
                              ++dirChange;
                           }

                           dir = "East";
                        }
                     }

                     if (temp == null) {
                        temp = this.checkForIron(Start, t.getRelative(0, 0, -1), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "West")) {
                              ++dirChange;
                           }

                           dir = "West";
                        }
                     }

                     if (temp == null) {
                        temp = this.checkForIron(Start, t.getRelative(1, 0, 0), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "North")) {
                              ++dirChange;
                           }

                           dir = "North";
                        }
                     }

                     if (temp == null) {
                        temp = this.checkForIron(Start, t.getRelative(-1, 0, 0), blocks);
                        if (temp != null) {
                           t = temp;
                           if (this.dirChanged(dir, "South")) {
                              ++dirChange;
                           }

                           dir = "South";
                        }
                     }
                  }

                  if (temp == null) {
                     return;
                  }

                  if (dirChange == 1 && b1 == null) {
                     b1 = (Block)blocks.get(blocks.size() - 1);
                  }

                  if (dirChange == 3 && b2 == null) {
                     b2 = (Block)blocks.get(blocks.size() - 1);
                  }

                  blocks.add(temp);
               } while(!Start.equals(t));

               if (blocks.size() > this.maxPerimeter) {
                  return;
               }

               if (!blocks.contains(target)) {
                  return;
               }

               if (b1 == null || b2 == null) {
                  return;
               }

               if (dirChange != 4 && dirChange != 3) {
                  return;
               }

               Sign callSign = this.getCallSign(b1.getLocation(), b2.getLocation());
               if (callSign != null) {
                  Floor floor = new Floor(this, b1.getLocation(), b2.getLocation(), callSign, count + 1);
                  this.floors.add(floor);
                  ++count;
               }
            }
         }

         if (this.floors.size() <= this.maxFloors) {
            this.platform = new Platform(this.plugin, b1.getLocation(), b2.getLocation(), ((Floor)this.floors.get(0)).getHeight(), ((Floor)this.floors.get(this.floors.size() - 1)).getHeight());
            if (this.platform.isInitialized()) {
               this.isInitialized = true;
               System.out.println("[EasyElevator] An elevator has been initialized");
            }
         }
      }
   }

   private Sign getCallSign(Location l1, Location l2) {
      BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
      int x1 = l1.getBlockX();
      int z1 = l1.getBlockZ();
      int x2 = l2.getBlockX();
      int z2 = l2.getBlockZ();
      int xStart = 0;
      int xEnd = 0;
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

      this.xLow = xStart;
      this.xHigh = xEnd;
      this.zLow = zStart;
      this.zHigh = zEnd;
      --xStart;
      ++xEnd;
      --zStart;
      ++zEnd;

      for(int i = l1.getBlockY() + 2; i <= l1.getBlockY() + 3; ++i) {
         for(int x = xStart; x <= xEnd; ++x) {
            for(int z = zStart; z <= zEnd; ++z) {
               Block tempBlock = this.world.getBlockAt(x, i, z);
               Sign sign;
               if (x != xStart && x != xEnd) {
                  if ((z == zStart || z == zEnd) && tempBlock.getType().equals(Material.WALL_SIGN)) {
                     sign = (Sign)tempBlock.getState();
                     return sign;
                  }
               } else if (tempBlock.getType().equals(Material.WALL_SIGN)) {
                  sign = (Sign)tempBlock.getState();
                  return sign;
               }
            }
         }
      }

      return null;
   }

   private boolean dirChanged(String dir, String newDir) {
      if (dir.equals("")) {
         return false;
      } else {
         return !dir.equals(newDir);
      }
   }

   private Block checkForIron(Block Start, Block t, List<Block> blocks) {
      if (this.isFloor(t) || this.isOutputDoor(t) || this.isOutputFloor(t)) {
         if (Start.equals(t) && blocks.size() <= 4) {
            return null;
         }

         if (!blocks.contains(t)) {
            return t;
         }
      }

      return null;
   }

   public void addStops(int Floor) {
      int height = -1;

      for(int i = 0; i < this.floors.size(); ++i) {
         if (((Floor)this.floors.get(i)).getFloor() == Floor) {
            height = ((Floor)this.floors.get(i)).getHeight();
         }
      }

      this.addStopsFromHeight(height);
   }

   public void addStopsFromHeight(int height) {
      if (height != -1 && !this.stops.contains(height)) {
         this.stops.add(height);
         if (!this.isMoving) {
            this.isMoving = true;
            this.run();
         }
      }

   }

   public void Call(int height) {
      boolean hasHeight = false;
      Floor f = null;

      for(int i = 0; i < this.floors.size(); ++i) {
         f = (Floor)this.floors.get(i);
         if (f.getSignHeight() == height) {
            hasHeight = true;
            f.setCalled(true);
            i = this.floors.size();
         }
      }

      if (hasHeight) {
         this.addStopsFromHeight(f.getHeight());
      }

   }

   public void StopAt(int floor) {
      Iterator var3 = this.floors.iterator();

      while(var3.hasNext()) {
         Floor f = (Floor)var3.next();
         if (f.getFloor() == floor) {
            this.Call(f.getSignHeight());
            return;
         }
      }

   }

   public void run() {
      if (this.lcount == 6) {
         this.lcount = 0;
      }

      this.updateDirection();
      this.updateFloorIndicator();
      if (!this.hasOpenDoor) {
         if (!this.platform.isStuck()) {
            if (this.stops.contains(this.platform.getHeight())) {
               Iterator var2 = this.floors.iterator();

               while(var2.hasNext()) {
                  Floor f = (Floor)var2.next();
                  if (f.getHeight() == this.platform.getHeight()) {
                     this.currentFloor = f;
                  }
               }

               if (this.currentFloor != null) {
                  if (this.plugin.getArrivalSound()) {
                     this.currentFloor.playOpenSound();
                  }

                  this.currentFloor.switchRedstoneFloorOn(true);
                  this.currentFloor.OpenDoor();
                  this.hasOpenDoor = true;
                  this.currentFloor.setCalled(false);
                  this.platform.stopTeleport();
                  this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 100L);
               }
            } else {
               if (!this.Direction.equals("") && this.currentFloor != null) {
                  this.currentFloor.switchRedstoneFloorOn(false);
                  this.currentFloor = null;
               }

               if (this.Direction.equals("DOWN")) {
                  this.platform.moveDown(this.lcount);
                  this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                  ++this.lcount;
               } else if (!this.Direction.equals("UP")) {
                  this.isMoving = false;
                  return;
               }

               if (this.Direction.equals("UP")) {
                  this.platform.moveUp(this.lcount);
                  this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                  ++this.lcount;
               } else if (!this.Direction.equals("DOWN")) {
                  this.isMoving = false;
               }
            }
         } else {
            if (this.Direction.equals("UP")) {
               this.Direction = "DOWN";
            } else {
               this.Direction = "UP";
            }

            this.stops.clear();
            this.addStops(this.getFloorNumberFromHeight(this.getNextFloorHeight_2()));
            this.platform.isStuck(false);
            this.platform.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator is stuck. Resetting...");
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 50L);
         }
      } else if (this.currentFloor != null) {
         this.currentFloor.CloseDoor();
         this.hasOpenDoor = false;
         this.removeCurrentFloor();
         this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 5L);
      }

   }

   public void changeFloor() {
      int curr = Integer.parseInt(this.platform.getSign().getLine(1));
      int next = curr + 1;
      if (next > this.floors.size()) {
         next = 1;
      }

      this.platform.writeSign(1, "" + next);
   }

   public int getFloorNumberFromHeight(int hight) {
      int floor = -1;
      Iterator var4 = this.floors.iterator();

      while(var4.hasNext()) {
         Floor f = (Floor)var4.next();
         if (f.getHeight() == hight) {
            return f.getFloor();
         }
      }

      return floor;
   }

   public int getNextFloorHeight_2() {
      int next = -1;
      int current = this.platform.getHeight();
      int i;
      int t;
      if (this.Direction.equals("UP")) {
         for(i = 0; i < this.floors.size(); ++i) {
            t = ((Floor)this.floors.get(i)).getHeight();
            if (next == -1 && t > current) {
               next = t;
            }

            if (t > current && t < next) {
               next = t;
            }
         }

         return next;
      } else if (this.Direction.equals("DOWN")) {
         for(i = 0; i < this.floors.size(); ++i) {
            t = ((Floor)this.floors.get(i)).getHeight();
            if (next == -1 && t < current) {
               next = t;
            }

            if (t < current && t > next) {
               next = t;
            }
         }

         return next;
      } else {
         return this.Direction.equals("") ? this.platform.getHeight() : -1;
      }
   }

   public int getNextFloorHeight() {
      if (this.currentFloor == null) {
         return -1;
      } else {
         int next = -1;
         int current = this.currentFloor.getHeight();
         int i;
         int t;
         if (this.Direction.equals("UP")) {
            for(i = 0; i < this.stops.size(); ++i) {
               t = (Integer)this.stops.get(i);
               if (next == -1 && t > current) {
                  next = t;
               }

               if (t > current && t < next) {
                  next = t;
               }
            }
         }

         if (this.Direction.equals("DOWN")) {
            for(i = 0; i < this.stops.size(); ++i) {
               t = (Integer)this.stops.get(i);
               if (next == -1 && t < current) {
                  next = t;
               }

               if (t < current && t > next) {
                  next = t;
               }
            }
         }

         return next;
      }
   }

   public Platform getPlatform() {
      return this.platform;
   }

   public Floor getMainFloor() {
      return (Floor)this.floors.get(0);
   }

   public boolean isInitialized() {
      return this.isInitialized;
   }

   private void removeCurrentFloor() {
      for(int i = 0; i < this.stops.size(); ++i) {
         if ((Integer)this.stops.get(i) == this.platform.getHeight()) {
            this.stops.remove(i);
         }
      }

   }

   private void updateDirection() {
      int height = this.platform.getHeight();
      Iterator localIterator = this.stops.iterator();

      int i;
      do {
         if (!localIterator.hasNext()) {
            if (this.stops.size() > 0) {
               if (this.Direction.equals("DOWN")) {
                  this.Direction = "UP";
                  return;
               }

               if (this.Direction.equals("UP")) {
                  this.Direction = "DOWN";
               }
            } else {
               this.Direction = "";
            }

            return;
         }

         i = (Integer)localIterator.next();
         if (this.Direction.equals("DOWN") && i < height) {
            return;
         }

         if (this.Direction.equals("UP") && i > height) {
            return;
         }
      } while(!this.Direction.equals(""));

      if (i > height) {
         this.Direction = "UP";
      } else {
         this.Direction = "DOWN";
      }

   }

   private void updateFloorIndicator() {
      int curr = this.getCurrentFloor();

      int i;
      for(i = 0; i < this.floors.size(); ++i) {
         if (curr != -1) {
            ((Floor)this.floors.get(i)).writeSign(2, "" + curr);
         } else {
            if (this.Direction.equals("UP")) {
               ((Floor)this.floors.get(i)).writeSign(2, "/\\");
            }

            if (this.Direction.equals("DOWN")) {
               ((Floor)this.floors.get(i)).writeSign(2, "\\/");
            }
         }
      }

      if (curr != -1) {
         this.platform.writeSign(2, "" + curr);
      } else {
         if (this.Direction.equals("UP")) {
            this.platform.writeSign(2, "/\\");
         }

         if (this.Direction.equals("DOWN")) {
            this.platform.writeSign(2, "\\/");
         }
      }

      i = this.getFloorNumberFromHeight(this.getNextFloorHeight());
      if (i != -1) {
         this.platform.writeSign(3, "" + i);
      } else {
         this.platform.writeSign(3, "-");
      }

   }

   public int getCurrentFloor() {
      if (this.isFloor(this.platform.getHeight())) {
         for(int i = 0; i < this.floors.size(); ++i) {
            if (this.platform.getHeight() == ((Floor)this.floors.get(i)).getHeight()) {
               return ((Floor)this.floors.get(i)).getFloor();
            }
         }
      }

      return -1;
   }

   public boolean isPartOfElevator(Location loc) {
      int x = loc.getBlockX();
      int y = loc.getBlockY();
      int z = loc.getBlockZ();
      return y > this.lowestPoint && y < this.highestPoint && x >= this.xLow && x <= this.xHigh && z >= this.zLow && z <= this.zHigh;
   }

   public boolean isFloorSign(Sign sign) {
      for(int i = 0; i < this.floors.size(); ++i) {
         Floor f = (Floor)this.floors.get(i);
         if (f.getSign().equals(sign)) {
            return true;
         }
      }

      return false;
   }

   public boolean isPlatformSign(Sign sign) {
      return this.platform.getSign().equals(sign);
   }

   public boolean isInElevator(Player player) {
      return this.platform.hasPlayer(player);
   }

   public boolean isFloor(int floorHeight) {
      for(int i = 0; i < this.floors.size(); ++i) {
         if (floorHeight == ((Floor)this.floors.get(i)).getHeight()) {
            return true;
         }
      }

      return false;
   }

   public List<Floor> getFloors() {
      return this.floors;
   }

   public boolean isBorder(Block b) {
      try {
         String border = this.plugin.getBlockBorder();
         int data = -1;
         int id;
         if (border.contains(":")) {
            id = Integer.parseInt(border.split(":")[0]);
            data = Integer.parseInt(border.split(":")[1]);
         } else {
            id = Integer.parseInt(border);
         }

         if (data != -1) {
            if (data == b.getData() && id == b.getTypeId()) {
               return true;
            }
         } else if (id == b.getTypeId()) {
            return true;
         }
      } catch (Exception var5) {
         ;
      }

      return false;
   }

   public boolean isFloor(Block b) {
      try {
         String border = this.plugin.getBlockFloor();
         int data = -1;
         int id;
         if (border.contains(":")) {
            id = Integer.parseInt(border.split(":")[0]);
            data = Integer.parseInt(border.split(":")[1]);
         } else {
            id = Integer.parseInt(border);
         }

         if (data != -1) {
            if (data == b.getData() && id == b.getTypeId()) {
               return true;
            }
         } else if (id == b.getTypeId()) {
            return true;
         }
      } catch (Exception var5) {
         ;
      }

      return false;
   }

   public boolean isOutputFloor(Block b) {
      try {
         String border = this.plugin.getBlockOutputFloor();
         int data = -1;
         int id;
         if (border.contains(":")) {
            id = Integer.parseInt(border.split(":")[0]);
            data = Integer.parseInt(border.split(":")[1]);
         } else {
            id = Integer.parseInt(border);
         }

         if (data != -1) {
            if (data == b.getData() && id == b.getTypeId()) {
               return true;
            }
         } else if (id == b.getTypeId()) {
            return true;
         }
      } catch (Exception var5) {
         ;
      }

      return false;
   }

   public boolean isOutputDoor(Block b) {
      try {
         String border = this.plugin.getBlockOutputDoor();
         int data = -1;
         int id;
         if (border.contains(":")) {
            id = Integer.parseInt(border.split(":")[0]);
            data = Integer.parseInt(border.split(":")[1]);
         } else {
            id = Integer.parseInt(border);
         }

         if (data != -1) {
            if (data == b.getData() && id == b.getTypeId()) {
               return true;
            }
         } else if (id == b.getTypeId()) {
            return true;
         }
      } catch (Exception var5) {
         ;
      }

      return false;
   }
}
