/**
 * This code is licensed under the GNU GPLv3 as viewable on https://dev.bukkit.org/projects/easyelevator
 *
 * It was modified on the 27th of July 2017 by AlvinB
 */
package com.keough99.easyelevator;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

public class EEConfiguration {
   private String folder = "plugins/EasyElevator";
   private File configFile;
   private YamlConfiguration config;

   public EEConfiguration() {
      this.configFile = new File(this.folder + File.separator + "config.yml");
   }

   private YamlConfiguration loadConfig() {
      try {
         YamlConfiguration config = new YamlConfiguration();
         config.load(this.configFile);
         return config;
      } catch (Exception var2) {
         System.out.println("[EasyElevator] An error occured! Please delete your SkyDiver folder an reload this Plugin!");
         return null;
      }
   }

   public void createConfig() {
      (new File(this.folder)).mkdir();
      if (!this.configFile.exists()) {
         try {
            System.out.println("[EasyElevator] Creating Config...");
            this.configFile.createNewFile();
            this.config = this.loadConfig();
            this.config.set("MaxPerimeter", 25);
            this.config.set("MaxFloors", 10);
            this.config.set("Arrival.Sound", true);
            this.config.set("Arrival.Message", true);
            this.config.set("Blocks.Border", "41");
            this.config.set("Blocks.Floor", "42");
            this.config.set("Blocks.OutputDoor", "35:14");
            this.config.set("Blocks.OutputFloor", "35:1");
            this.config.save(this.configFile);
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      } else {
         this.config = this.loadConfig();
         this.addNewNodes();
         this.config = this.loadConfig();
      }

   }

   public int getMaxPerimeter() {
      this.config = this.loadConfig();
      return this.config.getInt("MaxPerimeter");
   }

   public int getMaxFloors() {
      this.config = this.loadConfig();
      return this.config.getInt("MaxFloors");
   }

   public boolean getArrivalSound() {
      this.config = this.loadConfig();
      return this.config.getBoolean("Arrival.Sound");
   }

   public boolean getArrivalMessage() {
      this.config = this.loadConfig();
      return this.config.getBoolean("Arrival.Message");
   }

   public String getBlock(String Block) {
      this.config = this.loadConfig();
      if (this.config.contains("Blocks." + Block)) {
         return this.config.getString("Blocks." + Block);
      } else {
         System.out.println("[EasyElevator] An error occured in your config. Please check for errors! (" + Block + ")");
         return "ERROR";
      }
   }

   public void addNewNodes() {
      try {
         if (this.config.contains("PlayElevatorSound")) {
            this.config.set("Arrival.Sound", this.config.get("PlayElevatorSound"));
            this.config.set("PlayElevatorSound", (Object)null);
            System.out.println("[EasyElevator] Added Arrival.Sound Node to Configuration");
            System.out.println("[EasyElevator] Removed PlayElevatorSound Node from Configuration");
         }

         if (!this.config.contains("Arrival.Sound")) {
            this.config.set("Arrival.Sound", true);
            System.out.println("[EasyElevator] Added Arrival.Sound Node to Configuration");
         }

         if (!this.config.contains("Arrival.Message")) {
            this.config.set("Arrival.Message", true);
            System.out.println("[EasyElevator] Added Arrival.Message Node to Configuration");
         }

         if (!this.config.contains("Blocks.Border")) {
            this.config.set("Blocks.Border", "41");
            System.out.println("[EasyElevator] Added Blocks.Border Node to Configuration");
         }

         if (!this.config.contains("Blocks.Floor")) {
            this.config.set("Blocks.Floor", "42");
            System.out.println("[EasyElevator] Added Blocks.Floor Node to Configuration");
         }

         if (!this.config.contains("Blocks.OutputDoor")) {
            this.config.set("Blocks.OutputDoor", "35:14");
            System.out.println("[EasyElevator] Added Blocks.OutputDoor Node to Configuration");
         }

         if (!this.config.contains("Blocks.OutputFloor")) {
            this.config.set("Blocks.OutputFloor", "35:1");
            System.out.println("[EasyElevator] Added Blocks.OutputFloor Node to Configuration");
         }

         this.config.save(this.configFile);
      } catch (Exception var2) {
         ;
      }

   }
}
