package br.com.primeleague.x1.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelState;
import br.com.primeleague.x1.models.Duel;

/**
 * Listener de eventos relacionados a jogadores
 */
public class PlayerListener implements Listener {

    private final Main plugin;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Evento disparado quando um jogador se conecta ao servidor
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Carregar estatísticas do jogador
        plugin.getStatsManager().getPlayerStats(player.getName());
    }
    
    /**
     * Evento disparado quando um jogador se desconecta do servidor
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // Verificar se o jogador estava em duelo
        if (plugin.getDuelManager().isInDuel(playerName)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(playerName);
            
            if (duel != null && duel.getState() != DuelState.ENDED) {
                // Jogador desconectou durante o duelo, vitória para o oponente
                String opponent = duel.getPlayer1().equals(playerName) ? duel.getPlayer2() : duel.getPlayer1();
                plugin.getDuelManager().endDuel(duel, opponent);
                
                // Notificar o oponente
                Player opponentPlayer = plugin.getServer().getPlayerExact(opponent);
                if (opponentPlayer != null && opponentPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(opponentPlayer, "duelo.desconectado", "%jogador%", playerName);
                }
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador morre
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String playerName = player.getName();
        
        // Verificar se o jogador estava em duelo
        if (plugin.getDuelManager().isInDuel(playerName)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(playerName);
            
            if (duel != null && duel.getState() == DuelState.IN_PROGRESS) {
                // Jogador morreu durante o duelo, vitória para o oponente
                String opponent = duel.getPlayer1().equals(playerName) ? duel.getPlayer2() : duel.getPlayer1();
                plugin.getDuelManager().endDuel(duel, opponent);
                
                // Evitar drop de itens
                event.getDrops().clear();
                
                // Remover mensagem de morte
                event.setDeathMessage(null);
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador se move
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Não vamos verificar movimentos mínimos (apenas rotação da cabeça)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // Verificar se o jogador está em duelo
        if (plugin.getDuelManager().isInDuel(playerName)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(playerName);
            
            // Verificar se o duelo está em andamento
            if (duel != null && duel.getState() == DuelState.IN_PROGRESS) {
                // Para duelos na ARENA, não é necessário verificar a saída da área
                if (duel.getType() == br.com.primeleague.x1.enums.DuelType.ARENA) {
                    // Verificamos apenas se o jogador está na arena
                    if (plugin.getArenaManager().isArenaConfigured() && 
                        !plugin.getArenaManager().isInArena(player)) {
                        
                        // Teleportar de volta para a arena
                        if (duel.getPlayer1().equals(playerName)) {
                            plugin.getArenaManager().teleportToArena1(player);
                        } else {
                            plugin.getArenaManager().teleportToArena2(player);
                        }
                        
                        // Notificar o jogador
                        plugin.getMessageManager().sendMessage(player, "duelo.saiu-area");
                    }
                }
                // Para duelos LOCAIS, o jogador só pode se mover dentro de um raio da posição inicial
                else if (duel.getType() == br.com.primeleague.x1.enums.DuelType.LOCAL) {
                    // Obter o oponente
                    String opponentName = duel.getPlayer1().equals(playerName) ? duel.getPlayer2() : duel.getPlayer1();
                    Player opponent = plugin.getServer().getPlayerExact(opponentName);
                    
                    // Verificar se o oponente está online
                    if (opponent != null && opponent.isOnline()) {
                        // Verificar se os jogadores estão muito distantes
                        double distance = player.getLocation().distance(opponent.getLocation());
                        double maxDistance = plugin.getConfigManager().getMaxLocalDuelDistance();
                        
                        if (distance > maxDistance) {
                            // Cancelar o movimento
                            event.setCancelled(true);
                            
                            // Notificar o jogador
                            plugin.getMessageManager().sendMessage(player, "duelo.muito-longe-oponente");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador se teleporta
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // Verificar se o jogador está em duelo
        if (plugin.getDuelManager().isInDuel(playerName)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(playerName);
            
            // Verificar se o duelo está em andamento
            if (duel != null && duel.getState() == DuelState.IN_PROGRESS) {
                // Para duelos na ARENA, não permitimos teleportes para fora da arena
                if (duel.getType() == br.com.primeleague.x1.enums.DuelType.ARENA) {
                    // Verificar se a arena está configurada
                    if (plugin.getArenaManager().isArenaConfigured() && 
                        !plugin.getArenaManager().isInArena(event.getTo())) {
                        
                        // Cancelar o teleporte
                        event.setCancelled(true);
                        plugin.getMessageManager().sendMessage(player, "duelo.teleporte-cancelado");
                    }
                } 
                // Para duelos LOCAIS, só permitimos teleportes dentro da distância máxima
                else if (duel.getType() == br.com.primeleague.x1.enums.DuelType.LOCAL) {
                    // Obter o oponente
                    String opponentName = duel.getPlayer1().equals(playerName) ? duel.getPlayer2() : duel.getPlayer1();
                    Player opponent = plugin.getServer().getPlayerExact(opponentName);
                    
                    // Verificar se o oponente está online
                    if (opponent != null && opponent.isOnline()) {
                        // Verificar se o teleporte é para longe do oponente
                        double distance = event.getTo().distance(opponent.getLocation());
                        double maxDistance = plugin.getConfigManager().getMaxLocalDuelDistance();
                        
                        if (distance > maxDistance) {
                            // Cancelar o teleporte
                            event.setCancelled(true);
                            plugin.getMessageManager().sendMessage(player, "duelo.teleporte-muito-longe");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador ressuscita
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // Verificar se o jogador estava em duelo
        if (plugin.getDuelManager().isInDuel(playerName)) {
            Duel duel = plugin.getDuelManager().getPlayerDuel(playerName);
            
            if (duel != null && duel.getState() == DuelState.ENDED) {
                // O duelo já foi encerrado, teleportar para o local original
                event.setRespawnLocation(plugin.getServer().getWorlds().get(0).getSpawnLocation());
            }
        }
    }
    
    /**
     * Evento disparado quando um jogador entra no jogo.
     * Usamos isto para garantir que os jogadores possam se ver em duelos
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinForVisibility(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        
        // Executar após alguns ticks para garantir que todas as entidades estejam carregadas
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // Fazer outros jogadores verem este jogador
                    for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
                        if (!otherPlayer.equals(player)) {
                            otherPlayer.showPlayer(player);
                            player.showPlayer(otherPlayer);
                        }
                    }
                }
            }
        }, 10L); // 10 ticks = 0.5 segundos
    }
} 