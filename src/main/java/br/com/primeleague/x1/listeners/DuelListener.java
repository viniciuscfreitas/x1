package br.com.primeleague.x1.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelState;
import br.com.primeleague.x1.models.Duel;

/**
 * Listener de eventos específicos de duelos
 */
public class DuelListener implements Listener {

    private final Main plugin;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public DuelListener(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Evento disparado quando uma entidade é danificada por outra entidade
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player damaged = (Player) event.getEntity();
        
        // Verificar se o dano foi causado por um jogador
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            
            // Verificar se ambos estão em duelo
            if (plugin.getDuelManager().isInDuel(damager.getName()) && 
                plugin.getDuelManager().isInDuel(damaged.getName())) {
                
                Duel damagerDuel = plugin.getDuelManager().getPlayerDuel(damager.getName());
                Duel damagedDuel = plugin.getDuelManager().getPlayerDuel(damaged.getName());
                
                // Verificar se estão no mesmo duelo
                if (damagerDuel.getId().equals(damagedDuel.getId())) {
                    // Verificar se o duelo está em contagem regressiva
                    if (damagerDuel.isInCountdown()) {
                        // Cancelar dano durante a contagem regressiva
                        event.setCancelled(true);
                        plugin.getMessageManager().sendMessage(damager, "duelo.aguarde-contagem");
                        return;
                    }
                    
                    // Verificar se o duelo está em andamento
                    if (damagerDuel.getState() == DuelState.IN_PROGRESS) {
                        // Permitir o dano
                        return;
                    }
                    
                    // Duelo não está em andamento, cancelar dano
                    event.setCancelled(true);
                    return;
                }
            }
            
            // Verificar se estão na mesma equipe em um duelo em equipe
            if (plugin.getTeamManager().areInSameTeam(damager.getName(), damaged.getName())) {
                // Cancelar dano entre membros da mesma equipe
                event.setCancelled(true);
                plugin.getMessageManager().sendMessage(damager, "duelo.dano-equipe");
                return;
            }
            
            // Fora de duelo, verificar se o PvP está permitido fora da arena
            if (!isInPvPAllowedArea(damager) || !isInPvPAllowedArea(damaged)) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Verifica se um jogador está em uma área onde o PvP é permitido
     * 
     * @param player Jogador a verificar
     * @return true se o PvP é permitido na localização do jogador
     */
    private boolean isInPvPAllowedArea(Player player) {
        // Verificar se o jogador está na arena
        if (plugin.getArenaManager().isInArena(player)) {
            return true;
        }
        
        // Verificar outras áreas onde o PvP é permitido (pode ser configurado)
        // Por padrão, PvP só é permitido na arena
        return false;
    }
    
    /**
     * Evento disparado quando uma entidade recebe dano
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Verificar se o jogador está em duelo
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player.getName());
            
            // Se o duelo não estiver em andamento, cancelar o dano
            if (duel.getState() != DuelState.IN_PROGRESS) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador tenta executar um comando
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se o jogador está em duelo
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player.getName());
            
            // Verificar se o duelo está em andamento
            if (duel.getState() == DuelState.IN_PROGRESS) {
                String command = event.getMessage().split(" ")[0].toLowerCase();
                
                // Permitir comandos essenciais
                if (command.equals("/x1") || 
                    command.equals("/duel") || 
                    command.equals("/tell") || 
                    command.equals("/msg") || 
                    command.equals("/r")) {
                    return;
                }
                
                // Se o jogador tem permissão para executar comandos durante o duelo
                if (player.hasPermission("primeleaguex1.bypass.commands")) {
                    return;
                }
                
                // Impedir a execução do comando
                event.setCancelled(true);
                plugin.getMessageManager().sendMessage(player, "duelo.comando-bloqueado");
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador tenta pegar um item
     */
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se o jogador está em duelo
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(player.getName());
            
            // Verificar se o duelo está na contagem regressiva
            if (duel.getState() == DuelState.STARTING) {
                // Impedir que o jogador pegue itens durante a contagem regressiva
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador tenta dropar um item
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se o jogador está em duelo
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            // Impedir que o jogador largue itens durante o duelo
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(player, "duelo.item-bloqueado");
        }
    }
} 