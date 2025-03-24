package br.com.primeleague.x1.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import br.com.primeleague.x1.Main;

/**
 * Gerenciador de arenas para duelos
 */
public class ArenaManager {

    private final Main plugin;
    private Location pos1;
    private Location pos2;
    private Location spectatorLocation;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        loadLocations();
    }
    
    /**
     * Carrega as localizações da arena da configuração
     */
    private void loadLocations() {
        ConfigurationSection config = plugin.getConfigManager().getConfig().getConfigurationSection("arena");
        
        if (config != null) {
            pos1 = loadLocationFromConfig(config.getConfigurationSection("pos1"));
            pos2 = loadLocationFromConfig(config.getConfigurationSection("pos2"));
            spectatorLocation = loadLocationFromConfig(config.getConfigurationSection("espectador"));
        }
    }
    
    /**
     * Carrega uma localização da seção de configuração
     * 
     * @param section Seção de configuração
     * @return Localização
     */
    private Location loadLocationFromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        String worldName = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * Salva as localizações da arena na configuração
     */
    public void saveLocations() {
        ConfigurationSection config = plugin.getConfigManager().getConfig().getConfigurationSection("arena");
        
        if (config == null) {
            config = plugin.getConfigManager().getConfig().createSection("arena");
        }
        
        if (pos1 != null) {
            saveLocationToConfig(config.createSection("pos1"), pos1);
        }
        
        if (pos2 != null) {
            saveLocationToConfig(config.createSection("pos2"), pos2);
        }
        
        if (spectatorLocation != null) {
            saveLocationToConfig(config.createSection("espectador"), spectatorLocation);
        }
        
        plugin.getConfigManager().saveConfig();
    }
    
    /**
     * Salva uma localização na seção de configuração
     * 
     * @param section Seção de configuração
     * @param location Localização
     */
    private void saveLocationToConfig(ConfigurationSection section, Location location) {
        if (section == null || location == null) {
            return;
        }
        
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }
    
    /**
     * Define a posição 1 da arena
     * 
     * @param location Localização da posição 1
     */
    public void setPos1(Location location) {
        pos1 = location.clone();
        saveLocations();
    }
    
    /**
     * Define a posição 2 da arena
     * 
     * @param location Localização da posição 2
     */
    public void setPos2(Location location) {
        pos2 = location.clone();
        saveLocations();
    }
    
    /**
     * Define a posição do local de espectador
     * 
     * @param location Localização do local de espectador
     */
    public void setSpectatorLocation(Location location) {
        spectatorLocation = location.clone();
        saveLocations();
    }
    
    /**
     * Obtém a posição 1 da arena
     * 
     * @return Posição 1 da arena
     */
    public Location getPos1() {
        return pos1 != null ? pos1.clone() : null;
    }
    
    /**
     * Obtém a posição 2 da arena
     * 
     * @return Posição 2 da arena
     */
    public Location getPos2() {
        return pos2 != null ? pos2.clone() : null;
    }
    
    /**
     * Obtém a posição do local de espectador
     * 
     * @return Posição do local de espectador
     */
    public Location getSpectatorLocation() {
        return spectatorLocation != null ? spectatorLocation.clone() : null;
    }
    
    /**
     * Verifica se a arena está configurada
     * 
     * @return true se a arena está configurada
     */
    public boolean isArenaConfigured() {
        return pos1 != null && pos2 != null;
    }
    
    /**
     * Verifica se a localização está dentro da arena
     * 
     * @param location Localização a verificar
     * @return true se a localização está dentro da arena
     */
    public boolean isInArena(Location location) {
        if (!isArenaConfigured() || location == null) {
            return false;
        }
        
        if (!location.getWorld().equals(pos1.getWorld())) {
            return false;
        }
        
        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        return location.getX() >= minX && location.getX() <= maxX &&
               location.getY() >= minY && location.getY() <= maxY &&
               location.getZ() >= minZ && location.getZ() <= maxZ;
    }
    
    /**
     * Verifica se o jogador está dentro da arena
     * 
     * @param player Jogador a verificar
     * @return true se o jogador está dentro da arena
     */
    public boolean isInArena(Player player) {
        if (player == null) {
            return false;
        }
        
        return isInArena(player.getLocation());
    }
    
    /**
     * Teleporta um jogador para a arena (posição 1)
     * 
     * @param player Jogador a teleportar
     */
    public void teleportToArena1(Player player) {
        if (player != null && pos1 != null) {
            player.teleport(pos1);
        }
    }
    
    /**
     * Teleporta um jogador para a arena (posição 2)
     * 
     * @param player Jogador a teleportar
     */
    public void teleportToArena2(Player player) {
        if (player != null && pos2 != null) {
            player.teleport(pos2);
        }
    }
    
    /**
     * Teleporta um jogador para o local de espectador
     * 
     * @param player Jogador a teleportar
     */
    public void teleportToSpectator(Player player) {
        if (player != null && spectatorLocation != null) {
            player.teleport(spectatorLocation);
        }
    }
    
    /**
     * Verifica se dois jogadores estão próximos o suficiente para um duelo local
     * 
     * @param player1 Jogador 1
     * @param player2 Jogador 2
     * @return true se os jogadores estão próximos o suficiente
     */
    public boolean arePlayersCloseEnough(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            return false;
        }
        
        if (!player1.getWorld().equals(player2.getWorld())) {
            return false;
        }
        
        double distance = player1.getLocation().distance(player2.getLocation());
        return distance <= plugin.getConfigManager().getMaxLocalDuelDistance();
    }
    
    /**
     * Verifica se dois jogadores estão próximos (alias para arePlayersCloseEnough)
     * 
     * @param player1 Jogador 1
     * @param player2 Jogador 2
     * @return true se os jogadores estão próximos
     */
    public boolean arePlayersClose(Player player1, Player player2) {
        return arePlayersCloseEnough(player1, player2);
    }
    
    /**
     * Teleporta um jogador para a posição 1 da arena
     * 
     * @param player Jogador a teleportar
     */
    public void teleportToPosition1(Player player) {
        teleportToArena1(player);
    }
    
    /**
     * Teleporta um jogador para a posição 2 da arena
     * 
     * @param player Jogador a teleportar
     */
    public void teleportToPosition2(Player player) {
        teleportToArena2(player);
    }
} 