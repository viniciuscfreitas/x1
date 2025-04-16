package br.com.primeleague.x1.integration;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import br.com.primeleague.x1.Main;

/**
 * Classe para integração com o WorldGuard
 * Gerencia permissões de PvP para jogadores em duelo
 */
public class WorldGuardHook implements Listener {

    private final Main plugin;
    private boolean worldGuardEnabled = false;
    private Plugin worldGuard;
    private final Set<String> pvpEnabledPlayers;
    
    public WorldGuardHook(Main plugin) {
        this.plugin = plugin;
        this.pvpEnabledPlayers = new HashSet<>();
        
        // Verificar se o WorldGuard está disponível
        Plugin worldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null && worldGuardPlugin.isEnabled()) {
            worldGuard = worldGuardPlugin;
            plugin.getLogger().info("WorldGuard detectado! A integração foi ativada.");
            worldGuardEnabled = true;
            
            // Registrar este objeto como listener para eventos do WorldGuard
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } else {
            plugin.getLogger().info("WorldGuard não detectado. PvP em áreas protegidas não estará disponível.");
        }
    }
    
    /**
     * Adiciona um jogador à lista de jogadores com PvP ativado
     * 
     * @param playerName Nome do jogador
     */
    public void addPvPPlayer(String playerName) {
        if (!worldGuardEnabled) return;
        
        // Verificar se o jogador já está na lista para evitar logs duplicados
        if (!pvpEnabledPlayers.contains(playerName)) {
            pvpEnabledPlayers.add(playerName);
            // Comentado temporariamente para evitar loops de log
            // plugin.getLogger().fine("Jogador " + playerName + " adicionado à lista de PvP");
        }
    }
    
    /**
     * Remove um jogador da lista de jogadores com PvP ativado
     * 
     * @param playerName Nome do jogador
     */
    public void removePvPPlayer(String playerName) {
        if (!worldGuardEnabled) return;
        
        // Verificar se o jogador está na lista para evitar logs desnecessários
        if (pvpEnabledPlayers.contains(playerName)) {
            pvpEnabledPlayers.remove(playerName);
            // Comentado temporariamente para evitar loops de log
            // plugin.getLogger().fine("Jogador " + playerName + " removido da lista de PvP");
        }
    }
    
    /**
     * Verifica se um jogador está na lista de jogadores com PvP ativado
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador está na lista
     */
    public boolean isPvPPlayer(String playerName) {
        return worldGuardEnabled && pvpEnabledPlayers.contains(playerName);
    }
    
    /**
     * Verifica se dois jogadores podem lutar entre si, mesmo em áreas protegidas
     * 
     * @param player1 Nome do primeiro jogador
     * @param player2 Nome do segundo jogador
     * @return true se podem lutar
     */
    public boolean canPvP(String player1, String player2) {
        if (!worldGuardEnabled || !plugin.getConfigManager().allowPvPInProtectedAreas()) return false;
        
        return isPvPPlayer(player1) && isPvPPlayer(player2) && 
               plugin.getDuelManager().isInDuel(player1) && 
               plugin.getDuelManager().isInDuel(player2) &&
               plugin.getDuelManager().getPlayerDuel(player1).getId().equals(
                   plugin.getDuelManager().getPlayerDuel(player2).getId());
    }
    
    /**
     * Verifica se um jogador pode atacar outro jogador em uma área protegida
     * Este método é chamado pelo evento de dano entre entidades
     * 
     * @param attacker Jogador atacante
     * @param defender Jogador defensor
     * @return true se o ataque é permitido, false caso contrário
     */
    public boolean canHurtPlayer(Player attacker, Player defender) {
        if (!worldGuardEnabled || !plugin.getConfigManager().allowPvPInProtectedAreas()) return false;
        
        // Se ambos estão em duelo, permitir o ataque
        return canPvP(attacker.getName(), defender.getName());
    }
    
    /**
     * Limpa a lista de jogadores com PvP ativado
     */
    public void clearPvPPlayers() {
        if (!worldGuardEnabled) return;
        
        pvpEnabledPlayers.clear();
    }
    
    /**
     * Verifica se o WorldGuard está ativado
     * 
     * @return true se o WorldGuard está ativado
     */
    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }
    
    /**
     * Obtém a instância do WorldGuard
     * 
     * @return Instância do Plugin ou null se não estiver disponível
     */
    public Plugin getWorldGuard() {
        return worldGuard;
    }
} 