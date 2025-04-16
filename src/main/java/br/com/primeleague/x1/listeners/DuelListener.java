package br.com.primeleague.x1.listeners;

import java.util.UUID;

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
import br.com.primeleague.x1.managers.X1ScoreboardManager;

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
            
            // Caso 1: Atacante está em duelo, vítima não
            if (plugin.getDuelManager().isInDuel(damager.getName()) && 
                !plugin.getDuelManager().isInDuel(damaged.getName())) {
                // Impedir jogadores em duelo de atacar jogadores fora do duelo
                event.setCancelled(true);
                return;
            }
            
            // Caso 2: Vítima está em duelo, atacante não
            if (!plugin.getDuelManager().isInDuel(damager.getName()) && 
                plugin.getDuelManager().isInDuel(damaged.getName())) {
                // Impedir jogadores externos de atacar jogadores em duelo
                event.setCancelled(true);
                return;
            }
            
            // Caso 3: Ambos estão em duelo
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
                    
                    // Verificar se é um duelo em equipe e se os jogadores são da mesma equipe
                    if (damagerDuel.isTeamDuel() && damagerDuel.areInSameTeam(damager.getName(), damaged.getName())) {
                        // Cancelar dano entre membros da mesma equipe
                        event.setCancelled(true);
                        plugin.getMessageManager().sendMessage(damager, "duelo.dano-equipe");
                        return;
                    }
                    
                    // Verificar se o duelo está em andamento
                    if (damagerDuel.getState() == DuelState.IN_PROGRESS) {
                        // Permitir o dano, ignorando proteções do WorldGuard
                        event.setCancelled(false);
                        
                        // Obter a vida atual do jogador antes do dano
                        final double vidaAntes = damaged.getHealth();
                        final double danoBruto = event.getDamage();
                        
                        // Programar uma tarefa para rodar no próximo tick após o dano ser aplicado
                        // para capturar o valor real da vida perdida
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                // Verificar se os jogadores ainda estão online
                                if (damaged.isOnline() && damager.isOnline()) {
                                    // Calcular o dano real através da diferença de vida
                                    double vidaDepois = damaged.getHealth();
                                    double danoEfetivo = vidaAntes - vidaDepois;
                                    
                                    // Se o jogador não perdeu vida (seja por armadura, poções ou bloqueio)
                                    // usar uma estimativa baixa para não zerar o dano
                                    if (danoEfetivo <= 0.1) {
                                        // Usar 5% do dano bruto como mínimo
                                        danoEfetivo = Math.max(0.1, danoBruto * 0.05);
                                    }
                                    
                                    // Usar Math.round para garantir valores inteiros consistentes
                                    double danoArredondado = Math.round(danoEfetivo);
                                    
                                    // Log para debug - remover em produção
                                    if (plugin.getConfigManager().getConfig().getBoolean("debug-mode", false)) {
                                        System.out.println("[DEBUG X1] Dano de " + damager.getName() + " -> " + damaged.getName() + 
                                                           " | Bruto: " + danoBruto + " | Real: " + danoArredondado + 
                                                           " | Vida antes: " + vidaAntes + " -> depois: " + vidaDepois);
                                    }
                                    
                                    // Registrar nos sistemas de estatísticas
                                    X1ScoreboardManager.registrarDano(damager.getName(), danoArredondado);
                                    X1ScoreboardManager.registrarDanoRecebido(damaged.getName(), danoArredondado);
                                    
                                    // Registrar dano significativo se estiver usando o sistema de logs
                                    if (damagerDuel.isTeamDuel() && plugin.isDuelLoggingEnabled() && danoArredondado >= 2.0) {
                                        try {
                                            UUID logId = plugin.getDuelManager().getDuelLogId(damagerDuel);
                                            if (logId != null) {
                                                plugin.getDuelLogManager().logDamage(logId, damaged, damager, danoArredondado);
                                            }
                                        } catch (Exception e) {
                                            // Ignore erros de logging
                                        }
                                    }
                                }
                            }
                        }, 1L); // Executar 1 tick após o dano ser aplicado
                        
                        return;
                    }
                    
                    // Duelo não está em andamento, cancelar dano
                    event.setCancelled(true);
                    return;
                } else {
                    // Estão em duelos diferentes, cancelar dano
                    event.setCancelled(true);
                    return;
                }
            }
            
            // Verificar se estão na mesma equipe fora de duelos
            if (plugin.getTeamManager().areInSameTeam(damager.getName(), damaged.getName())) {
                // Cancelar dano entre membros da mesma equipe
                event.setCancelled(true);
                plugin.getMessageManager().sendMessage(damager, "duelo.dano-equipe");
                return;
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
    
    /**
     * Manipula evento de dano entre jogadores especificamente para registrar nos logs de duelo
     * Este método é focado apenas no registro dos danos para o sistema de replay
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageForLogging(EntityDamageByEntityEvent event) {
        // Verificar se é dano entre jogadores
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player damaged = (Player) event.getEntity();
        
        // Verificar se o jogador danificado está em um duelo
        String damagedName = damaged.getName();
        if (!plugin.getDuelManager().isInDuel(damagedName)) {
            return; // O jogador não está em duelo
        }
        
        Duel duel = plugin.getDuelManager().getPlayerDuel(damagedName);
        if (duel == null) {
            return;
        }
        
        // Verificar se o atacante é outro jogador
        Player damager = null;
        Entity damagerEntity = event.getDamager();
        
        if (damagerEntity instanceof Player) {
            damager = (Player) damagerEntity;
        } else {
            // No Spigot 1.5.2, não podemos usar instanceof Arrow e getShooter diretamente
            // como nas versões mais recentes, portanto vamos ignorar projéteis por enquanto
            return;
        }
        
        // Verificar se os jogadores estão no mesmo duelo
        if (!plugin.getDuelManager().isInDuel(damager.getName()) || 
            !duel.getId().equals(plugin.getDuelManager().getPlayerDuel(damager.getName()).getId())) {
            return;
        }
        
        // Verificar se o duelo está em andamento
        if (duel.getState() != DuelState.IN_PROGRESS) {
            return;
        }
        
        // Verificar se o sistema de log está ativo
        if (!plugin.isDuelLoggingEnabled()) {
            return;
        }
        
        // Obter ID do log e verificar se existe
        UUID logId = plugin.getDuelManager().getDuelLogId(duel);
        if (logId == null) {
            return;
        }
        
        // Registrar dano para o log do duelo - em 1.5.2 usamos getDamage()
        double damage = event.getDamage();
        System.out.println("[PrimeLeagueX1] Registrando dano para log: " + damager.getName() + " -> " + 
                           damaged.getName() + " (" + damage + ")");
        
        try {
            plugin.getDuelLogManager().logDamage(logId, damaged, damager, damage);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao registrar dano: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 