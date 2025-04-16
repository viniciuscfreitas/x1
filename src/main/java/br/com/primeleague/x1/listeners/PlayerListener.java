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
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelState;
import br.com.primeleague.x1.models.Duel;
import br.com.primeleague.x1.enums.DuelType;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
                
                // Verificar se o oponente ainda está online
                Player opponentPlayer = plugin.getServer().getPlayerExact(opponent);
                if (opponentPlayer != null && opponentPlayer.isOnline()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("jogador", playerName);
                    plugin.getMessageManager().sendMessage(opponentPlayer, "duelo.desconectado", placeholders);
                    
                    // Finalizar o duelo com vitória para o oponente
                    plugin.getDuelManager().endDuel(duel, opponent);
                    
                    // Registrar estatísticas
                    plugin.getStatsManager().addWin(opponent);
                    plugin.getStatsManager().addLoss(playerName);
                    
                    // Processar apostas se houver
                    double betAmount = duel.getBetAmount();
                    if (betAmount > 0) {
                        plugin.getEconomyManager().giveMoney(opponent, betAmount * 2);
                    }
                } else {
                    // Se o oponente também estiver offline, finalizar como empate
                    plugin.getDuelManager().endDuel(duel, null);
                    
                    // Registrar estatísticas
                    plugin.getStatsManager().addDraw(playerName);
                    plugin.getStatsManager().addDraw(opponent);
                    
                    // Devolver apostas se houver
                    double betAmount = duel.getBetAmount();
                    if (betAmount > 0) {
                        plugin.getEconomyManager().giveMoney(playerName, betAmount);
                        plugin.getEconomyManager().giveMoney(opponent, betAmount);
                    }
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
                // Evitar drop de itens
                event.getDrops().clear();
                
                // Remover mensagem de morte
                event.setDeathMessage(null);
                
                // Verificar o jogador que matou (se houver)
                Player killer = player.getKiller();
                String killerName = (killer != null) ? killer.getName() : null;
                
                // Se houver um assassino e ele também estiver no mesmo duelo, registrar a eliminação
                if (killerName != null && plugin.getDuelManager().isInDuel(killerName)) {
                    Duel killerDuel = plugin.getDuelManager().getPlayerDuel(killerName);
                    
                    if (killerDuel.getId().equals(duel.getId())) {
                        // Registrar eliminação
                        duel.addKill(killerName);
                        
                        // Enviar mensagem para o assassino
                        if (duel.isTeamDuel()) {
                            plugin.getMessageManager().sendMessage(killer, "duelo.matou-jogador-equipe", 
                                    "%player%", playerName);
                        } else {
                            plugin.getMessageManager().sendMessage(killer, "duelo.matou-jogador", 
                                    "%player%", playerName);
                        }
                        
                        // Enviar mensagem para todos os outros jogadores no duelo
                        for (String member : duel.getTeam1()) {
                            if (!member.equals(killerName) && !member.equals(playerName)) {
                                Player memberPlayer = Bukkit.getPlayerExact(member);
                                if (memberPlayer != null && memberPlayer.isOnline()) {
                                    plugin.getMessageManager().sendMessage(memberPlayer, "duelo.jogador-matou", 
                                            "%killer%", killerName,
                                            "%player%", playerName);
                                }
                            }
                        }
                        
                        for (String member : duel.getTeam2()) {
                            if (!member.equals(killerName) && !member.equals(playerName)) {
                                Player memberPlayer = Bukkit.getPlayerExact(member);
                                if (memberPlayer != null && memberPlayer.isOnline()) {
                                    plugin.getMessageManager().sendMessage(memberPlayer, "duelo.jogador-matou", 
                                            "%killer%", killerName,
                                            "%player%", playerName);
                                }
                            }
                        }
                    }
                }
                
                // Verificar se é um duelo em equipe
                if (duel.isTeamDuel()) {
                    // Em duelo de equipe, apenas marcar o jogador como derrotado
                    // e verificar se toda a equipe foi derrotada
                    
                    List<String> team1 = duel.getTeam1();
                    List<String> team2 = duel.getTeam2();
                    
                    List<String> playerTeam;
                    List<String> enemyTeam;
                    
                    if (duel.isInTeam1(playerName)) {
                        playerTeam = team1;
                        enemyTeam = team2;
                    } else {
                        playerTeam = team2;
                        enemyTeam = team1;
                    }
                    
                    // Verificar se todos os outros jogadores da equipe estão mortos ou offline
                    boolean allTeamDefeated = true;
                    for (String memberName : playerTeam) {
                        if (!memberName.equals(playerName)) {
                            Player member = Bukkit.getPlayerExact(memberName);
                            if (member != null && member.isOnline() && !member.isDead()) {
                                allTeamDefeated = false;
                                break;
                            }
                        }
                    }
                    
                    if (allTeamDefeated) {
                        // Toda a equipe foi derrotada, dar vitória para a equipe adversária
                        // Selecionar um jogador vivo da equipe adversária como o vencedor
                        String winner = null;
                        for (String memberName : enemyTeam) {
                            Player member = Bukkit.getPlayerExact(memberName);
                            if (member != null && member.isOnline() && !member.isDead()) {
                                winner = memberName;
                                break;
                            }
                        }
                        
                        // Se não encontrar nenhum jogador vivo, usar o primeiro da lista
                        if (winner == null && !enemyTeam.isEmpty()) {
                            winner = enemyTeam.get(0);
                        }
                        
                        // Finalizar o duelo com a equipe adversária como vencedora
                        plugin.getDuelManager().endDuel(duel, winner);
                    } else {
                        // Alguns jogadores da equipe ainda estão vivos
                        // Enviar mensagem informando que o jogador morreu mas o duelo continua
                        for (String memberName : playerTeam) {
                            Player member = Bukkit.getPlayerExact(memberName);
                            if (member != null && member.isOnline() && !memberName.equals(playerName)) {
                                plugin.getMessageManager().sendMessage(member, "equipes.membro-morto", 
                                        "%jogador%", playerName);
                            }
                        }
                    }
                } else {
                    // Duelo normal (1v1)
                    // Jogador morreu durante o duelo, vitória para o oponente
                    String opponent = duel.getPlayer1().equals(playerName) ? duel.getPlayer2() : duel.getPlayer1();
                    plugin.getDuelManager().endDuel(duel, opponent);
                }
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
                // Para duelos LOCAIS, o jogador só pode se mover dentro de um raio da posição inicial
                if (duel.getType() == br.com.primeleague.x1.enums.DuelType.LOCAL) {
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
                // Para duelos LOCAIS, só permitimos teleportes dentro da distância máxima
                if (duel.getType() == br.com.primeleague.x1.enums.DuelType.LOCAL) {
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
    
    /**
     * Evento disparado quando um jogador envia uma mensagem no chat
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Verificar se o jogador está esperando input para o desafio
        if (plugin.getGUIManager().isWaitingForPlayerInput(player.getName())) {
            event.setCancelled(true);
            
            // Processar o nome do jogador
            Bukkit.getScheduler().runTask(plugin, () -> {
                processPlayerNameInput(player, message);
            });
        }
        // Verificar se o jogador está esperando input para o valor da aposta
        else if (plugin.getGUIManager().isWaitingForBetInput(player.getName())) {
            event.setCancelled(true);
            
            // Processar o valor da aposta
            Bukkit.getScheduler().runTask(plugin, () -> {
                processBetAmountInput(player, message);
            });
        }
    }
    
    /**
     * Processa a entrada do valor da aposta
     * 
     * @param player Jogador
     * @param input Entrada do jogador
     */
    private void processBetAmountInput(Player player, String input) {
        // Limpar o status de espera de input
        plugin.getGUIManager().clearWaitingForBetInput(player.getName());
        
        // Obter o jogador selecionado
        String opponent = plugin.getGUIManager().getSelectedPlayer(player);
        
        if (opponent == null) {
            player.sendMessage("§cOcorreu um erro ao processar sua aposta. Tente novamente.");
            return;
        }
        
        Player target = plugin.getServer().getPlayerExact(opponent);
        
        // Verificar se o jogador está online
        if (target == null || !target.isOnline()) {
            plugin.getMessageManager().sendMessage(player, "geral.jogador-offline");
            return;
        }
        
        // Cancelar entrada "0"
        if (input.equals("0")) {
            player.sendMessage("§cAposta cancelada.");
            plugin.getGUIManager().openBetMenu(player, opponent);
            return;
        }
        
        // Tentar converter o valor para número
        double betAmount;
        try {
            betAmount = Double.parseDouble(input.replace(",", "."));
        } catch (NumberFormatException e) {
            player.sendMessage("§cValor inválido. Use apenas números. Exemplo: 100 ou 100.50");
            plugin.getGUIManager().openBetMenu(player, opponent);
            return;
        }
        
        // Verificar se o valor é positivo
        if (betAmount <= 0) {
            player.sendMessage("§cO valor da aposta deve ser maior que zero.");
            plugin.getGUIManager().openBetMenu(player, opponent);
            return;
        }
        
        // Verificar se o jogador tem dinheiro suficiente
        if (!plugin.getEconomyManager().hasEnoughMoney(player, betAmount)) {
            plugin.getEconomyManager().sendNotEnoughMoneyMessage(player, betAmount);
            plugin.getGUIManager().openBetMenu(player, opponent);
            return;
        }
        
        // Enviar desafio com aposta personalizada
        DuelType duelType = plugin.getGUIManager().getSelectedDuelType(player);
        plugin.getDuelManager().sendChallenge(player, target, duelType, betAmount);
        
        player.sendMessage("§aDesafio enviado com aposta de §f" + plugin.getEconomyManager().formatMoney(betAmount) + "§a!");
    }
    
    /**
     * Processa a entrada do nome do jogador
     * 
     * @param player Jogador
     * @param input Entrada do jogador
     */
    private void processPlayerNameInput(Player player, String input) {
        // Buscar jogador pelo nome (mesmo que parcial)
        Player target = null;
        String targetName = input.trim();
        
        // Verificar se o jogador digitou um nome específico
        if (!targetName.isEmpty()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(targetName.toLowerCase())) {
                    target = online;
                    break;
                }
            }
        }
        
        // Verificar se encontrou um jogador
        if (target == null) {
            player.sendMessage("§cJogador offline.");
            plugin.getGUIManager().clearWaitingForPlayerInput(player.getName());
            return;
        }
        
        // Verificar se o jogador tentou desafiar a si mesmo
        if (player.getName().equals(target.getName())) {
            player.sendMessage("§cVocê não pode desafiar a si mesmo.");
            plugin.getGUIManager().clearWaitingForPlayerInput(player.getName());
            return;
        }
        
        // Verificar se o jogador já está em duelo
        if (plugin.getDuelManager().isInDuel(player.getName())) {
            player.sendMessage("§cDuelo em andamento.");
            plugin.getGUIManager().clearWaitingForPlayerInput(player.getName());
            return;
        }
        
        // Verificar se o alvo já está em duelo
        if (plugin.getDuelManager().isInDuel(target.getName())) {
            player.sendMessage("§cOpponente em duelo.");
            plugin.getGUIManager().clearWaitingForPlayerInput(player.getName());
            return;
        }
        
        // Abrir menu de confirmação de desafio
        plugin.getGUIManager().clearWaitingForPlayerInput(player.getName());
        plugin.getGUIManager().openChallengeConfirmationMenu(player, target.getName());
    }
} 