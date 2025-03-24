package br.com.primeleague.x1.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelState;
import br.com.primeleague.x1.enums.DuelType;
import br.com.primeleague.x1.models.Duel;
import br.com.primeleague.x1.utils.InventoryUtils;

/**
 * Gerenciador de duelos
 */
public class DuelManager {

    private final Main plugin;
    
    // Mapas de duelos ativos
    private final Map<UUID, Duel> activeDuels;
    private final Map<String, Duel> playersDuels;
    private final Map<String, String> challenges;
    private final Map<String, Double> betAmounts;
    private final Map<String, DuelType> challengeTypes; // Armazena o tipo de duelo do desafio
    
    // Cache de estado de jogadores
    private final Map<String, ItemStack[]> inventoryCache;
    private final Map<String, ItemStack[]> armorCache;
    private final Map<String, Integer> expCache;
    private final Map<String, Integer> healthCache;
    private final Map<String, Integer> foodCache;
    private final Map<String, GameMode> gameModeCache;
    private final Map<String, Location> locationCache; // Novo cache para localização
    
    // Temporizadores
    private final Map<UUID, BukkitTask> duelTimers;
    private final Map<String, BukkitTask> challengeTimers;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public DuelManager(Main plugin) {
        this.plugin = plugin;
        this.activeDuels = new HashMap<>();
        this.playersDuels = new HashMap<>();
        this.challenges = new HashMap<>();
        this.betAmounts = new HashMap<>();
        this.challengeTypes = new HashMap<>();
        
        this.inventoryCache = new HashMap<>();
        this.armorCache = new HashMap<>();
        this.expCache = new HashMap<>();
        this.healthCache = new HashMap<>();
        this.foodCache = new HashMap<>();
        this.gameModeCache = new HashMap<>();
        this.locationCache = new HashMap<>(); // Inicializa o novo cache
        
        this.duelTimers = new HashMap<>();
        this.challengeTimers = new HashMap<>();
    }
    
    /**
     * Envia um desafio de duelo
     * 
     * @param challenger Jogador desafiante
     * @param challenged Jogador desafiado
     * @param type Tipo de duelo
     * @param betAmount Valor da aposta
     * @return true se o desafio foi enviado, false caso contrário
     */
    public boolean sendChallenge(Player challenger, Player challenged, DuelType type, double betAmount) {
        String challengerName = challenger.getName();
        String challengedName = challenged.getName();
        
        // Verificações gerais
        if (challengerName.equals(challengedName)) {
            plugin.getMessageManager().sendMessage(challenger, "duelo.desafiar-proprio");
            return false;
        }
        
        if (isInDuel(challengerName)) {
            plugin.getMessageManager().sendMessage(challenger, "duelo.ja-em-duelo");
            return false;
        }
        
        if (isInDuel(challengedName)) {
            plugin.getMessageManager().sendMessage(challenger, "duelo.alvo-em-duelo");
            return false;
        }
        
        if (challenges.containsKey(challengerName)) {
            plugin.getMessageManager().sendMessage(challenger, "duelo.ja-desafiou");
            return false;
        }
        
        if (challenges.containsValue(challengerName)) {
            plugin.getMessageManager().sendMessage(challenger, "duelo.ja-foi-desafiado");
            return false;
        }
        
        // Verificar apostas
        if (betAmount > 0) {
            if (!plugin.getConfigManager().useBets()) {
                plugin.getMessageManager().sendMessage(challenger, "duelo.apostas-desativadas");
                return false;
            }
            
            if (!plugin.getEconomyManager().hasEnoughMoney(challenger, betAmount)) {
                plugin.getEconomyManager().sendNotEnoughMoneyMessage(challenger, betAmount);
                return false;
            }
        }
        
        // Verificar tipo local
        if (type.isLocal() && !plugin.getArenaManager().arePlayersCloseEnough(challenger, challenged)) {
            plugin.getMessageManager().sendMessage(challenger, "duelo.muito-longe", 
                    "%player%", challengedName);
            return false;
        }
        
        // Registrar o desafio
        challenges.put(challengerName, challengedName);
        
        // Armazenar o valor da aposta
        if (betAmount > 0) {
            betAmounts.put(challengerName, betAmount);
        }
        
        // Armazenar o tipo de duelo
        challengeTypes.put(challengerName, type);
        
        // Enviar mensagens
        plugin.getMessageManager().sendMessage(challenger, "duelo.desafio-enviado", 
                "%player%", challengedName,
                "%tipo%", type.getFormattedName());
        
        plugin.getMessageManager().sendMessage(challenged, "duelo.recebeu-desafio", 
                "%player%", challengerName,
                "%tipo%", type.getFormattedName());
        
        // Informar sobre a aposta, se houver
        if (betAmount > 0) {
            plugin.getMessageManager().sendMessage(challenger, "duelo.aposta-info", 
                    "%valor%", plugin.getEconomyManager().formatMoney(betAmount));
            
            plugin.getMessageManager().sendMessage(challenged, "duelo.aposta-info", 
                    "%valor%", plugin.getEconomyManager().formatMoney(betAmount));
        }
        
        // Inicia o temporizador para o desafio expirar
        int timeout = plugin.getConfigManager().getChallengeTimeout();
        challengeTimers.put(challengerName, Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (challenges.containsKey(challengerName)) {
                plugin.getMessageManager().sendMessage(challenger, "duelo.desafio-expirou", 
                        "%player%", challengedName);
                plugin.getMessageManager().sendMessage(challenged, "duelo.desafio-expirou-alvo", 
                        "%player%", challengerName);
                
                challenges.remove(challengerName);
                betAmounts.remove(challengerName);
                challengeTypes.remove(challengerName);
                challengeTimers.remove(challengerName);
            }
        }, timeout * 20L));
        
        return true;
    }
    
    /**
     * Aceita um desafio de duelo
     * 
     * @param challenger Nome do jogador desafiante
     * @param challenged Jogador desafiado
     * @return true se o desafio foi aceito, false caso contrário
     */
    public boolean acceptChallenge(String challenger, Player challenged) {
        // Verifica se o desafio existe
        if (!challenges.containsKey(challenger) || !challenges.get(challenger).equals(challenged.getName())) {
            plugin.getMessageManager().sendMessage(challenged, "duelo.sem-desafio", 
                    "%player%", challenger);
            return false;
        }
        
        Player challengerPlayer = Bukkit.getPlayer(challenger);
        if (challengerPlayer == null || !challengerPlayer.isOnline()) {
            plugin.getMessageManager().sendMessage(challenged, "duelo.desafiante-offline", 
                    "%player%", challenger);
            challenges.remove(challenger);
            betAmounts.remove(challenger);
            return false;
        }
        
        // Verificações para apostas
        Double betAmount = betAmounts.get(challenger);
        if (betAmount != null && betAmount > 0) {
            if (!plugin.getEconomyManager().hasEnoughMoney(challengerPlayer, betAmount)) {
                plugin.getMessageManager().sendMessage(challenged, "duelo.desafiante-sem-dinheiro", 
                        "%player%", challenger);
                plugin.getMessageManager().sendMessage(challengerPlayer, "economia.sem-dinheiro", 
                        "%valor%", plugin.getEconomyManager().formatMoney(betAmount));
                
                challenges.remove(challenger);
                betAmounts.remove(challenger);
                return false;
            }
            
            if (!plugin.getEconomyManager().hasEnoughMoney(challenged, betAmount)) {
                plugin.getMessageManager().sendMessage(challenged, "economia.sem-dinheiro", 
                        "%valor%", plugin.getEconomyManager().formatMoney(betAmount));
                
                plugin.getMessageManager().sendMessage(challengerPlayer, "duelo.desafiado-sem-dinheiro", 
                        "%player%", challenged.getName());
                
                challenges.remove(challenger);
                betAmounts.remove(challenger);
                return false;
            }
            
            // Retira o dinheiro dos jogadores
            plugin.getEconomyManager().withdrawMoney(challengerPlayer, betAmount);
            plugin.getEconomyManager().withdrawMoney(challenged, betAmount);
        }
        
        // Cancela o temporizador do desafio
        if (challengeTimers.containsKey(challenger)) {
            challengeTimers.get(challenger).cancel();
            challengeTimers.remove(challenger);
        }
        
        // Recupera o tipo de duelo armazenado
        DuelType type = challengeTypes.getOrDefault(challenger, DuelType.ARENA);
        
        // Cria um novo duelo
        Duel duel = new Duel(UUID.randomUUID(), challenger, challenged.getName(), type);
        if (betAmount != null && betAmount > 0) {
            duel.setBetAmount(betAmount);
        }
        
        // Registra o duelo
        activeDuels.put(duel.getId(), duel);
        playersDuels.put(challenger, duel);
        playersDuels.put(challenged.getName(), duel);
        
        // Remove o desafio
        challenges.remove(challenger);
        betAmounts.remove(challenger);
        
        // Envia mensagens
        plugin.getMessageManager().sendMessage(challengerPlayer, "duelo.desafio-aceito", 
                "%player%", challenged.getName());
        
        plugin.getMessageManager().sendMessage(challenged, "duelo.desafio-aceito", 
                "%player%", challenger);
        
        // Inicia o duelo
        startDuel(duel, challengerPlayer, challenged);
        
        return true;
    }
    
    /**
     * Rejeita um desafio de duelo
     * 
     * @param challenger Nome do jogador desafiante
     * @param challenged Jogador desafiado
     * @return true se o desafio foi rejeitado, false caso contrário
     */
    public boolean rejectChallenge(String challenger, Player challenged) {
        // Verifica se o desafio existe
        if (!challenges.containsKey(challenger) || !challenges.get(challenger).equals(challenged.getName())) {
            plugin.getMessageManager().sendMessage(challenged, "duelo.sem-desafio", 
                    "%player%", challenger);
            return false;
        }
        
        // Cancela o temporizador do desafio
        if (challengeTimers.containsKey(challenger)) {
            challengeTimers.get(challenger).cancel();
            challengeTimers.remove(challenger);
        }
        
        // Busca o jogador desafiante (pode estar offline)
        Player challengerPlayer = Bukkit.getPlayer(challenger);
        
        // Remove o desafio
        challenges.remove(challenger);
        betAmounts.remove(challenger);
        challengeTypes.remove(challenger);
        
        // Envia mensagens
        plugin.getMessageManager().sendMessage(challenged, "duelo.desafio-recusado-voce", 
                "%player%", challenger);
        
        if (challengerPlayer != null && challengerPlayer.isOnline()) {
            plugin.getMessageManager().sendMessage(challengerPlayer, "duelo.desafio-recusado", 
                    "%player%", challenged.getName());
        }
        
        return true;
    }
    
    /**
     * Inicia um duelo
     * 
     * @param duel Duelo a ser iniciado
     * @param player1 Jogador 1
     * @param player2 Jogador 2
     */
    private void startDuel(Duel duel, Player player1, Player player2) {
        duel.setState(DuelState.STARTING);
        
        // Guarda os dados dos jogadores
        savePlayerState(player1);
        savePlayerState(player2);
        
        // Prepara os jogadores para o duelo
        preparePlayer(player1, duel.getType());
        preparePlayer(player2, duel.getType());
        
        // Garantir que os jogadores possam se ver
        ensurePlayersCanSeeEachOther(player1, player2);
        
        // Teleporta os jogadores para a arena se for duelo de arena
        // Teleportar antes da contagem regressiva para evitar bugs
        if (duel.getType().isArena()) {
            if (!plugin.getArenaManager().isArenaConfigured()) {
                // Se a arena não estiver configurada, cancela o duelo
                plugin.getMessageManager().sendMessage(player1, "arena.nao-configurada");
                plugin.getMessageManager().sendMessage(player2, "arena.nao-configurada");
                
                endDuel(duel, null);
                return;
            }
            
            // Primeira tentativa de teleporte
            teleportPlayersToArena(player1, player2);
            
            // Segunda tentativa após 5 ticks
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!plugin.getArenaManager().isInArena(player1) || !plugin.getArenaManager().isInArena(player2)) {
                        teleportPlayersToArena(player1, player2);
                    }
                    ensurePlayersCanSeeEachOther(player1, player2);
                }
            }, 5L);
            
            // Terceira tentativa após 10 ticks
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!plugin.getArenaManager().isInArena(player1) || !plugin.getArenaManager().isInArena(player2)) {
                        teleportPlayersToArena(player1, player2);
                    }
                    ensurePlayersCanSeeEachOther(player1, player2);
                }
            }, 10L);
            
            // Verificação final após 20 ticks
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    // Se algum jogador ainda não estiver na arena, cancela o duelo
                    if (!plugin.getArenaManager().isInArena(player1) || !plugin.getArenaManager().isInArena(player2)) {
                        plugin.getMessageManager().sendMessage(player1, "duelo.erro-teleporte");
                        plugin.getMessageManager().sendMessage(player2, "duelo.erro-teleporte");
                        endDuel(duel, null);
                        return;
                    }
                    ensurePlayersCanSeeEachOther(player1, player2);
                }
            }, 20L);
        }
        
        // Envia mensagens
        plugin.getMessageManager().sendMessage(player1, "duelo.iniciando", 
                "%player%", player2.getName(),
                "%tipo%", duel.getType().getFormattedName());
        
        plugin.getMessageManager().sendMessage(player2, "duelo.iniciando", 
                "%player%", player1.getName(),
                "%tipo%", duel.getType().getFormattedName());
        
        // Contagem regressiva
        int countdown = plugin.getConfigManager().getDuelCountdown();
        duel.setStartTime(System.currentTimeMillis() + (countdown * 1000L));
        
        // Adicionar uma flag indicando que o duelo está em contagem regressiva
        // Os jogadores não poderão se atacar durante esse período
        duel.setInCountdown(true);
        
        duelTimers.put(duel.getId(), Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int count = countdown;
            
            @Override
            public void run() {
                if (count > 0) {
                    // Envia mensagem de contagem regressiva usando apenas mensagens normais
                    plugin.getMessageManager().sendMessage(player1, "duelo.contagem", 
                            "%tempo%", String.valueOf(count),
                            "%player%", player2.getName());
                    
                    plugin.getMessageManager().sendMessage(player2, "duelo.contagem", 
                            "%tempo%", String.valueOf(count),
                            "%player%", player1.getName());
                    
                    count--;
                } else {
                    // Contagem regressiva terminou, inicia o duelo
                    BukkitTask task = duelTimers.get(duel.getId());
                    if (task != null) {
                        task.cancel();
                    }
                    
                    // Remover a flag de contagem regressiva
                    duel.setInCountdown(false);
                    duel.setState(DuelState.IN_PROGRESS);
                    
                    // Enviar mensagem normal
                    plugin.getMessageManager().sendMessage(player1, "duelo.comecou");
                    plugin.getMessageManager().sendMessage(player2, "duelo.comecou");
                    
                    // Tocar som de início de duelo
                    String sound = plugin.getConfigManager().getConfig().getString("sons.iniciar-duelo", "NOTE_PLING");
                    player1.playSound(player1.getLocation(), org.bukkit.Sound.valueOf(sound), 1.0f, 1.0f);
                    player2.playSound(player2.getLocation(), org.bukkit.Sound.valueOf(sound), 1.0f, 1.0f);
                    
                    // Define o tempo máximo do duelo
                    int maxTime = plugin.getConfigManager().getDuelMaxTime();
                    duelTimers.put(duel.getId(), Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (duel.getState() == DuelState.IN_PROGRESS) {
                                // Duelo terminou por tempo
                                plugin.getMessageManager().sendMessage(player1, "duelo.tempo-esgotado");
                                plugin.getMessageManager().sendMessage(player2, "duelo.tempo-esgotado");
                                
                                endDuel(duel, null); // Empate
                            }
                        }
                    }, maxTime * 20L));
                }
            }
        }, 0L, 20L));
    }
    
    /**
     * Teleporta os jogadores para a arena
     * 
     * @param player1 Jogador 1
     * @param player2 Jogador 2
     */
    private void teleportPlayersToArena(Player player1, Player player2) {
        if (player1 != null && player1.isOnline()) {
            // Carrega os chunks antes do teleporte
            Location pos1 = plugin.getArenaManager().getPos1();
            if (pos1 != null) {
                pos1.getChunk().load(true);
                player1.teleport(pos1);
            }
        }
        
        if (player2 != null && player2.isOnline()) {
            // Carrega os chunks antes do teleporte
            Location pos2 = plugin.getArenaManager().getPos2();
            if (pos2 != null) {
                pos2.getChunk().load(true);
                player2.teleport(pos2);
            }
        }
    }
    
    /**
     * Finaliza um duelo
     * 
     * @param duel Duelo a ser finalizado
     * @param winner Jogador vencedor (null para empate)
     */
    public void endDuel(Duel duel, String winner) {
        if (duel.getState() == DuelState.ENDED) {
            return; // Duelo já finalizado
        }
        
        duel.setState(DuelState.ENDED);
        duel.setEndTime(System.currentTimeMillis());
        
        // Cancela o temporizador do duelo
        if (duelTimers.containsKey(duel.getId())) {
            duelTimers.get(duel.getId()).cancel();
            duelTimers.remove(duel.getId());
        }
        
        Player player1 = Bukkit.getPlayer(duel.getPlayer1());
        Player player2 = Bukkit.getPlayer(duel.getPlayer2());
        
        // Atualiza estatísticas e gerencia apostas
        if (winner == null) {
            // Empate
            duel.setDraw(true);
            
            if (player1 != null && player1.isOnline()) {
                plugin.getStatsManager().addDraw(player1.getName());
            }
            
            if (player2 != null && player2.isOnline()) {
                plugin.getStatsManager().addDraw(player2.getName());
            }
            
            // Devolve a aposta em caso de empate
            if (duel.getBetAmount() > 0) {
                if (player1 != null && player1.isOnline()) {
                    plugin.getEconomyManager().depositMoney(player1, duel.getBetAmount());
                    plugin.getMessageManager().sendMessage(player1, "economia.aposta-devolvida", 
                            "%valor%", plugin.getEconomyManager().formatMoney(duel.getBetAmount()));
                }
                
                if (player2 != null && player2.isOnline()) {
                    plugin.getEconomyManager().depositMoney(player2, duel.getBetAmount());
                    plugin.getMessageManager().sendMessage(player2, "economia.aposta-devolvida", 
                            "%valor%", plugin.getEconomyManager().formatMoney(duel.getBetAmount()));
                }
            }
        } else {
            Player winnerPlayer = Bukkit.getPlayer(winner);
            Player loserPlayer = winner.equals(duel.getPlayer1()) ? player2 : player1;
            String loserName = winner.equals(duel.getPlayer1()) ? duel.getPlayer2() : duel.getPlayer1();
            
            duel.setWinner(winner);
            
            // Atualiza estatísticas
            if (winnerPlayer != null && winnerPlayer.isOnline()) {
                plugin.getStatsManager().addVictory(winner);
                
                // Atualiza Elo se configurado
                if (plugin.getConfigManager().useElo() && loserPlayer != null && loserPlayer.isOnline()) {
                    plugin.getStatsManager().updateElo(winner, loserName);
                }
            }
            
            if (loserPlayer != null && loserPlayer.isOnline()) {
                plugin.getStatsManager().addDefeat(loserName);
            }
            
            // Gerencia apostas
            if (duel.getBetAmount() > 0 && winnerPlayer != null && winnerPlayer.isOnline()) {
                double totalWin = duel.getBetAmount() * 2;
                plugin.getEconomyManager().depositMoney(winnerPlayer, totalWin);
                
                plugin.getMessageManager().sendMessage(winnerPlayer, "economia.aposta-vencida", 
                        "%valor%", plugin.getEconomyManager().formatMoney(totalWin));
                
                if (loserPlayer != null && loserPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(loserPlayer, "economia.aposta-perdida", 
                            "%valor%", plugin.getEconomyManager().formatMoney(duel.getBetAmount()));
                }
            }
        }
        
        // Restaura os jogadores e envia mensagens
        if (player1 != null && player1.isOnline()) {
            restorePlayerState(player1);
            
            if (winner == null) {
                plugin.getMessageManager().sendMessage(player1, "duelo.empate", 
                        "%player%", duel.getPlayer2());
            } else if (winner.equals(player1.getName())) {
                plugin.getMessageManager().sendMessage(player1, "duelo.vitoria", 
                        "%player%", duel.getPlayer2());
            } else {
                plugin.getMessageManager().sendMessage(player1, "duelo.derrota", 
                        "%player%", duel.getPlayer2());
            }
        }
        
        if (player2 != null && player2.isOnline()) {
            restorePlayerState(player2);
            
            if (winner == null) {
                plugin.getMessageManager().sendMessage(player2, "duelo.empate", 
                        "%player%", duel.getPlayer1());
            } else if (winner.equals(player2.getName())) {
                plugin.getMessageManager().sendMessage(player2, "duelo.vitoria", 
                        "%player%", duel.getPlayer1());
            } else {
                plugin.getMessageManager().sendMessage(player2, "duelo.derrota", 
                        "%player%", duel.getPlayer1());
            }
        }
        
        // Remove o duelo das listas
        playersDuels.remove(duel.getPlayer1());
        playersDuels.remove(duel.getPlayer2());
        activeDuels.remove(duel.getId());
        
        // Armazena o duelo no histórico
        plugin.getStatsManager().addDuelToHistory(duel);
    }
    
    /**
     * Verifica se um jogador está em duelo
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador está em duelo, false caso contrário
     */
    public boolean isInDuel(String playerName) {
        return playersDuels.containsKey(playerName);
    }
    
    /**
     * Verifica se um jogador enviou um desafio
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador enviou um desafio, false caso contrário
     */
    public boolean hasChallenge(String playerName) {
        return challenges.containsKey(playerName);
    }
    
    /**
     * Obtém o duelo de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Duelo do jogador ou null se não estiver em duelo
     */
    public Duel getPlayerDuel(String playerName) {
        return playersDuels.get(playerName);
    }
    
    /**
     * Obtém o jogador desafiado por um jogador
     * 
     * @param challenger Nome do jogador desafiante
     * @return Nome do jogador desafiado ou null se não houver desafio
     */
    public String getChallenged(String challenger) {
        return challenges.get(challenger);
    }
    
    /**
     * Verifica se um jogador foi desafiado por outro
     * 
     * @param challenged Nome do jogador desafiado
     * @param challenger Nome do jogador desafiante
     * @return true se o jogador foi desafiado pelo jogador especificado, false caso contrário
     */
    public boolean isChallengedBy(String challenged, String challenger) {
        return challenges.containsKey(challenger) && challenges.get(challenger).equals(challenged);
    }
    
    /**
     * Salva o estado de um jogador
     * 
     * @param player Jogador
     */
    private void savePlayerState(Player player) {
        String playerName = player.getName();
        
        inventoryCache.put(playerName, player.getInventory().getContents());
        armorCache.put(playerName, player.getInventory().getArmorContents());
        expCache.put(playerName, player.getTotalExperience());
        healthCache.put(playerName, (int)player.getHealth());
        foodCache.put(playerName, player.getFoodLevel());
        gameModeCache.put(playerName, player.getGameMode());
        locationCache.put(playerName, player.getLocation().clone()); // Salva a localização original
    }
    
    /**
     * Prepara um jogador para o duelo
     * 
     * @param player Jogador
     * @param duelType Tipo de duelo
     */
    private void preparePlayer(Player player, DuelType duelType) {
        // Se não for duelo com kit, mantém os itens do jogador
        if (!duelType.usesKit()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            return;
        }
        
        // Limpa o inventário e prepara para kit
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        
        // Fornece o kit de duelo se configurado
        if (plugin.getConfigManager().useDuelKit()) {
            InventoryUtils.giveDuelKit(player, plugin, duelType);
        }
    }
    
    /**
     * Garante que dois jogadores possam se ver mutuamente
     * 
     * @param player1 Jogador 1
     * @param player2 Jogador 2
     */
    private void ensurePlayersCanSeeEachOther(final Player player1, final Player player2) {
        if (player1 == null || player2 == null || !player1.isOnline() || !player2.isOnline()) {
            return;
        }
        
        // Fazer com que os jogadores possam se ver
        player1.showPlayer(player2);
        player2.showPlayer(player1);
        
        // Técnica 1: Teleportar os jogadores para sua própria localização para forçar a atualização das entidades
        final Location loc1 = player1.getLocation().clone();
        final Location loc2 = player2.getLocation().clone();
        
        // Primeira tentativa após 5 ticks
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (player1 != null && player2 != null && player1.isOnline() && player2.isOnline()) {
                    // Técnica 1: Teleportar para a mesma localização
                    player1.teleport(loc1);
                    player2.teleport(loc2);
                    
                    // Técnica 2: Mostrar novamente
                    player1.showPlayer(player2);
                    player2.showPlayer(player1);
                    
                    // Técnica 3: Atualizar o inventário
                    player1.updateInventory();
                    player2.updateInventory();
                    
                    // Técnica 4: Recarregar chunks
                    player1.getLocation().getChunk().load(true);
                    player2.getLocation().getChunk().load(true);
                }
            }
        }, 5L);
        
        // Segunda tentativa após 10 ticks
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (player1 != null && player2 != null && player1.isOnline() && player2.isOnline()) {
                    player1.showPlayer(player2);
                    player2.showPlayer(player1);
                    
                    // Técnica adicional: Recarregar entidades próximas
                    for (Entity entity : player1.getNearbyEntities(16, 16, 16)) {
                        if (entity instanceof Player) {
                            ((Player) entity).showPlayer(player1);
                            player1.showPlayer((Player) entity);
                        }
                    }
                    
                    for (Entity entity : player2.getNearbyEntities(16, 16, 16)) {
                        if (entity instanceof Player) {
                            ((Player) entity).showPlayer(player2);
                            player2.showPlayer((Player) entity);
                        }
                    }
                }
            }
        }, 10L);
        
        // Terceira tentativa após 20 ticks (1 segundo)
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (player1 != null && player2 != null && player1.isOnline() && player2.isOnline()) {
                    player1.showPlayer(player2);
                    player2.showPlayer(player1);
                }
            }
        }, 20L);
    }
    
    /**
     * Restaura o estado de um jogador
     * 
     * @param player Jogador
     */
    private void restorePlayerState(Player player) {
        String playerName = player.getName();
        
        if (inventoryCache.containsKey(playerName)) {
            player.getInventory().setContents(inventoryCache.get(playerName));
            inventoryCache.remove(playerName);
        }
        
        if (armorCache.containsKey(playerName)) {
            player.getInventory().setArmorContents(armorCache.get(playerName));
            armorCache.remove(playerName);
        }
        
        if (expCache.containsKey(playerName)) {
            player.setTotalExperience(expCache.get(playerName));
            expCache.remove(playerName);
        }
        
        if (healthCache.containsKey(playerName)) {
            int health = healthCache.get(playerName);
            player.setHealth(Math.min(health, (int)player.getMaxHealth()));
            healthCache.remove(playerName);
        }
        
        if (foodCache.containsKey(playerName)) {
            int foodLevel = foodCache.get(playerName);
            player.setFoodLevel(foodLevel);
            foodCache.remove(playerName);
        }
        
        if (gameModeCache.containsKey(playerName)) {
            player.setGameMode(gameModeCache.get(playerName));
            gameModeCache.remove(playerName);
        }
        
        // Teleporta o jogador de volta à sua posição original
        if (locationCache.containsKey(playerName)) {
            Location originalLocation = locationCache.get(playerName);
            player.teleport(originalLocation);
            locationCache.remove(playerName);
        }
    }
    
    /**
     * Limpa todos os dados temporários
     */
    public void clearAll() {
        // Limpa todos os duelos ativos
        for (UUID duelId : activeDuels.keySet()) {
            BukkitTask task = duelTimers.get(duelId);
            if (task != null) {
                task.cancel();
            }
        }
        
        // Limpa os desafios
        for (String challenger : challenges.keySet()) {
            BukkitTask task = challengeTimers.get(challenger);
            if (task != null) {
                task.cancel();
            }
        }
        
        // Limpa todos os mapas
        activeDuels.clear();
        playersDuels.clear();
        challenges.clear();
        betAmounts.clear();
        challengeTypes.clear();
        duelTimers.clear();
        challengeTimers.clear();
        
        // Cache de jogadores é mantido para restauração de estados
    }
    
    /**
     * Cancela todos os duelos ativos
     */
    public void cancelAllDuels() {
        // Finaliza todos os duelos como empate
        for (Duel duel : new ArrayList<>(activeDuels.values())) {
            if (duel.getState() != DuelState.ENDED) {
                endDuel(duel, null);
            }
        }
    }
} 