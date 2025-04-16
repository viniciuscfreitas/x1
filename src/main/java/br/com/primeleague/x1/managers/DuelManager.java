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
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.ChatColor;
import org.bukkit.inventory.PlayerInventory;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelState;
import br.com.primeleague.x1.enums.DuelType;
import br.com.primeleague.x1.models.Duel;
import br.com.primeleague.x1.utils.InventoryUtils;
import br.com.primeleague.x1.utils.ColorUtils;
import br.com.primeleague.x1.models.PlayerStats;
import br.com.primeleague.x1.managers.X1ScoreboardManager;
import br.com.primeleague.x1.utils.EffectsUtils;
import br.com.primeleague.x1.rival.RivalData;

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
    
    // Mapa para armazenar IDs de logs de duelo
    private final Map<UUID, UUID> duelLogsMap;
    
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
        
        // Inicializar mapa de logs
        this.duelLogsMap = new HashMap<>();
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
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", challengedName);
            plugin.getMessageManager().sendMessage(challenger, "duelo.muito-longe", placeholders);
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
        System.out.println("[PrimeLeagueX1] Desafio enviado por " + challengerName + " para " + challengedName + 
                " do tipo " + type.name() + " (usesKit: " + type.usesKit() + ")");
        
        // Enviar mensagens
        if (type.isTeamDuel()) {
            Map<String, String> teamPlaceholders = new HashMap<>();
            teamPlaceholders.put("jogador", challengedName);
            teamPlaceholders.put("tipo", type.getFormattedName());
            plugin.getMessageManager().sendMessage(challenger, "equipes.desafio-enviado", teamPlaceholders);
            plugin.getMessageManager().sendMessage(challenged, "equipes.desafio-recebido", teamPlaceholders);
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", challengedName);
            placeholders.put("tipo", type.getFormattedName());
            plugin.getMessageManager().sendMessage(challenger, "duelo.desafio-enviado", placeholders);
            
            placeholders.clear();
            placeholders.put("player", challengerName);
            plugin.getMessageManager().sendMessage(challenged, "duelo.recebeu-desafio", placeholders);
        }
        
        // Informar sobre a aposta, se houver
        if (betAmount > 0) {
            Map<String, String> betPlaceholders = new HashMap<>();
            betPlaceholders.put("valor", plugin.getEconomyManager().formatMoney(betAmount));
            plugin.getMessageManager().sendMessage(challenger, "duelo.aposta-info", betPlaceholders);
            plugin.getMessageManager().sendMessage(challenged, "duelo.aposta-info", betPlaceholders);
        }
        
        // Inicia o temporizador para o desafio expirar
        int timeout = plugin.getConfigManager().getChallengeTimeout();
        challengeTimers.put(challengerName, Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (challenges.containsKey(challengerName)) {
                Map<String, String> placeholders1 = new HashMap<>();
                placeholders1.put("player", challengedName);
                plugin.getMessageManager().sendMessage(challenger, "duelo.desafio-expirado", placeholders1);
                
                Map<String, String> placeholders2 = new HashMap<>();
                placeholders2.put("player", challengerName);
                plugin.getMessageManager().sendMessage(challenged, "duelo.desafio-expirado-alvo", placeholders2);
                
                System.out.println("[PrimeLeagueX1] Desafio de " + challengerName + " expirou. Limpando dados.");
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
        try {
            if (!challenges.containsKey(challenger)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", challenger);
                plugin.getMessageManager().sendMessage(challenged, "duelo.sem-desafio-jogador", placeholders);
                return false;
            }
            
            if (!challenges.get(challenger).equals(challenged.getName())) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", challenger);
                plugin.getMessageManager().sendMessage(challenged, "duelo.sem-desafio-jogador", placeholders);
                return false;
            }
            
            // Verificar se os jogadores estão online
            Player challengerPlayer = Bukkit.getPlayerExact(challenger);
            if (challengerPlayer == null || !challengerPlayer.isOnline()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", challenger);
                plugin.getMessageManager().sendMessage(challenged, "duelo.desafiante-offline", placeholders);
                
                // Remove o desafio
                challenges.remove(challenger);
                betAmounts.remove(challenger);
                challengeTypes.remove(challenger);
                
                return false;
            }
            
            // Verificar valor da aposta
            double betAmount = 0.0; // Inicializar com zero em vez de null
            if (betAmounts.containsKey(challenger)) {
                betAmount = betAmounts.get(challenger);
                
                // Verificar se os jogadores têm dinheiro suficiente
                if (betAmount > 0) {
                    if (!plugin.getEconomyManager().hasMoney(challengerPlayer, betAmount)) {
                plugin.getMessageManager().sendMessage(challenged, "duelo.desafiante-sem-dinheiro", 
                        "%player%", challenger);
                
                        // Remove o desafio
                challenges.remove(challenger);
                betAmounts.remove(challenger);
                        challengeTypes.remove(challenger);
                        
                return false;
            }
            
                    if (!plugin.getEconomyManager().hasMoney(challenged, betAmount)) {
                plugin.getMessageManager().sendMessage(challenged, "economia.sem-dinheiro", 
                                "%valor%", String.format("%.2f", betAmount));
                return false;
            }
        }
        }
        
            // Obter o tipo de duelo do desafio
        DuelType type = challengeTypes.getOrDefault(challenger, DuelType.ARENA);
            System.out.println("[PrimeLeagueX1] Tipo de duelo obtido: " + (type != null ? type.name() : "null"));
            
            // Se o tipo for null, usar ARENA como padrão
            if (type == null) {
                System.out.println("[PrimeLeagueX1] AVISO: Tipo de duelo é null, usando ARENA como padrão");
                type = DuelType.ARENA;
            }
            
            // Verificar se o tipo de duelo usa kit e se os inventários estão vazios
            if (type.usesKit()) {
                // Verificar inventário do desafiante
                if (challengerPlayer.getInventory().getContents().length > 0 && hasItems(challengerPlayer.getInventory())) {
                    plugin.getMessageManager().sendMessage(challengerPlayer, "duelo.inventario-nao-vazio");
                    plugin.getMessageManager().sendMessage(challenged, "duelo.oponente-inventario-nao-vazio", "%player%", challenger);
                    return false;
                }
                
                // Verificar inventário do desafiado
                if (challenged.getInventory().getContents().length > 0 && hasItems(challenged.getInventory())) {
                    plugin.getMessageManager().sendMessage(challenged, "duelo.inventario-nao-vazio");
                    plugin.getMessageManager().sendMessage(challengerPlayer, "duelo.oponente-inventario-nao-vazio", "%player%", challenged.getName());
                    return false;
                }
            }
            
            // Verificar se é duelo em equipe
            if (type.isTeamDuel()) {
                return acceptTeamChallenge(challenger, challenged, type, betAmount);
            }
            
            // Criar o duelo
        Duel duel = new Duel(UUID.randomUUID(), challenger, challenged.getName(), type);
            
            // Definir valor da aposta
            if (betAmount > 0) {
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
        
            // Agora que o duelo foi criado, podemos remover o tipo do mapa
            challengeTypes.remove(challenger);
            
            System.out.println("[PrimeLeagueX1] Duelo criado com sucesso entre " + challenger + 
                    " e " + challenged.getName() + " do tipo " + duel.getType().name() + 
                    " (usesKit: " + duel.getType().usesKit() + ")");
            
        // Inicia o duelo
        startDuel(duel, challengerPlayer, challenged);
        
        return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Aceita um desafio de duelo em equipe
     * 
     * @param challenger Nome do jogador desafiante (líder da equipe 1)
     * @param challenged Jogador desafiado (líder da equipe 2)
     * @param type Tipo de duelo
     * @param betAmount Valor da aposta
     * @return true se o desafio foi aceito, false caso contrário
     */
    private boolean acceptTeamChallenge(String challenger, Player challenged, DuelType type, Double betAmount) {
        try {
            // Obter as equipes
            List<String> team1 = plugin.getTeamManager().getTeam(challenger);
            List<String> team2 = plugin.getTeamManager().getTeam(challenged.getName());
            
            if (team1 == null || team2 == null) {
                plugin.getMessageManager().sendMessage(challenged, "equipes.erro-equipes");
                return false;
            }
            
            // Verificar se todos os jogadores estão online
            for (String memberName : team1) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer == null || !memberPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(challenged, "equipes.membro-offline", 
                            "%jogador%", memberName);
                    return false;
                }
            }
            
            for (String memberName : team2) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer == null || !memberPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(challenged, "equipes.membro-offline", 
                            "%jogador%", memberName);
                    return false;
                }
            }
            
            // Verificar tamanho das equipes
            int requiredTeamSize = type.getTeamSize();
            if (team1.size() < requiredTeamSize || team2.size() < requiredTeamSize) {
                plugin.getMessageManager().sendMessage(challenged, "equipes.equipe-muito-pequena");
                return false;
            }
            
            // Criar o duelo
            Duel duel = new Duel(UUID.randomUUID(), challenger, challenged.getName(), team1, team2, type);
            
            // Definir valor da aposta
            if (betAmount != null && betAmount > 0) {
                duel.setBetAmount(betAmount);
            }
            
            // Registra o duelo
            activeDuels.put(duel.getId(), duel);
            
            // Registrar todos os jogadores no mapa de duelos
            for (String memberName : team1) {
                playersDuels.put(memberName, duel);
            }
            
            for (String memberName : team2) {
                playersDuels.put(memberName, duel);
            }
            
            // Remove o desafio
            challenges.remove(challenger);
            betAmounts.remove(challenger);
            challengeTypes.remove(challenger);
            
            // Envia mensagens
            Player challengerPlayer = Bukkit.getPlayerExact(challenger);
            
            if (challengerPlayer != null && challengerPlayer.isOnline()) {
                plugin.getMessageManager().sendMessage(challengerPlayer, "equipes.desafio-aceito", 
                        "%jogador%", challenged.getName(),
                        "%tipo%", type.getFormattedName());
                
                // Notificar membros da equipe 1
                for (String memberName : team1) {
                    if (!memberName.equals(challenger)) {
                        Player memberPlayer = Bukkit.getPlayerExact(memberName);
                        if (memberPlayer != null && memberPlayer.isOnline()) {
                            plugin.getMessageManager().sendMessage(memberPlayer, "equipes.desafio-aceito-membro", 
                                    "%jogador%", challenged.getName(),
                                    "%tipo%", type.getFormattedName());
                        }
                    }
                }
            }
            
            plugin.getMessageManager().sendMessage(challenged, "equipes.desafio-aceito", 
                    "%jogador%", challenger,
                    "%tipo%", type.getFormattedName());
            
            // Notificar membros da equipe 2
            for (String memberName : team2) {
                if (!memberName.equals(challenged.getName())) {
                    Player memberPlayer = Bukkit.getPlayerExact(memberName);
                    if (memberPlayer != null && memberPlayer.isOnline()) {
                        plugin.getMessageManager().sendMessage(memberPlayer, "equipes.desafio-aceito-membro", 
                                "%jogador%", challenger,
                                "%tipo%", type.getFormattedName());
                    }
                }
            }
            
            System.out.println("[PrimeLeagueX1] Duelo em equipe criado com sucesso entre " + challenger + 
                    " e " + challenged.getName() + " do tipo " + duel.getType().name());
            
            // Inicia o duelo em equipe usando uma tarefa agendada para evitar recursão infinita
            final UUID duelId = duel.getId();
            Bukkit.getScheduler().runTask(plugin, () -> {
                Duel activeDuel = activeDuels.get(duelId);
                if (activeDuel != null) {
                    startTeamDuel(activeDuel);
                }
            });
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
     * Inicia um duelo entre dois jogadores
     * 
     * @param duel Duelo a iniciar
     * @param player1 Jogador 1
     * @param player2 Jogador 2
     */
    private void startDuel(Duel duel, Player player1, Player player2) {
        // Registrar o duelo como ativo
        UUID duelId = duel.getId();
        activeDuels.put(duelId, duel);
        playersDuels.put(player1.getName(), duel);
        playersDuels.put(player2.getName(), duel);
        
        // Salvar os estados dos jogadores
        savePlayerState(player1);
        savePlayerState(player2);
        
        // Adicionar jogadores à lista de PvP permitido no WorldGuard
        plugin.getWorldGuardHook().addPvPPlayer(player1.getName());
        plugin.getWorldGuardHook().addPvPPlayer(player2.getName());
        
        // Aplicar tags personalizadas para destacar os jogadores
        boolean useNametags = plugin.getConfigManager().useCustomNametags();
        System.out.println("[PrimeLeagueX1] Aplicando nametags: " + useNametags);
        if (useNametags) {
            plugin.getNametagManager().applyDuelTags(player1, player2);
            
            // Enviar mensagem informativa sobre as nametags
            plugin.getMessageManager().sendMessage(player1, "duelo.nametag-info");
            plugin.getMessageManager().sendMessage(player2, "duelo.nametag-info");
        }
        
        DuelType duelType = duel.getType();
        System.out.println("[PrimeLeagueX1] Iniciando duelo entre " + player1.getName() + " e " + 
                player2.getName() + " do tipo " + duelType + " (usesKit: " + 
                (duelType != null && duelType.usesKit()) + ")");
        
        // Teleportar para a arena, se necessário
        if (duelType.isArena()) {
                        teleportPlayersToArena(player1, player2);
                    }
        
        // Preparar os jogadores com equipamentos
        if (duelType.usesKit()) {
            System.out.println("[PrimeLeagueX1] Duelo usa kit. Preparando jogadores...");
            preparePlayer(player1, duelType);
            preparePlayer(player2, duelType);
        } else {
            System.out.println("[PrimeLeagueX1] Duelo não usa kit. Continuando sem configurar kits.");
        }
        
        // Garantir que os jogadores podem se ver mutuamente
                    ensurePlayersCanSeeEachOther(player1, player2);
        
        // Definir o duelo como em progresso
        duel.setState(DuelState.STARTING);
        
        // Envia mensagens
        Map<String, String> placeholders1 = new HashMap<>();
        placeholders1.put("player", player2.getName());
        placeholders1.put("tipo", duel.getType().getFormattedName());
        plugin.getMessageManager().sendMessage(player1, "duelo.iniciando", placeholders1);
        
        Map<String, String> placeholders2 = new HashMap<>();
        placeholders2.put("player", player1.getName());
        placeholders2.put("tipo", duel.getType().getFormattedName());
        plugin.getMessageManager().sendMessage(player2, "duelo.iniciando", placeholders2);
        
        // Contagem regressiva
        int countdown = plugin.getConfigManager().getDuelCountdown();
        duel.setStartTime(System.currentTimeMillis() + (countdown * 1000L));
        
        // Adicionar uma flag indicando que o duelo está em contagem regressiva
        // Os jogadores não poderão se atacar durante esse período
        duel.setInCountdown(true);
        
        // Mostrar scoreboard de pré-duelo para ambos os jogadores
        X1ScoreboardManager.mostrarPreDuel(player1, duel);
        X1ScoreboardManager.mostrarPreDuel(player2, duel);
        
        duelTimers.put(duelId, Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int count = countdown;
            
            @Override
            public void run() {
                if (count > 0) {
                    // Envia mensagem de contagem regressiva usando apenas mensagens normais
                    Map<String, String> placeholders1 = new HashMap<>();
                    placeholders1.put("tempo", String.valueOf(count));
                    placeholders1.put("player", player2.getName());
                    plugin.getMessageManager().sendMessage(player1, "duelo.contagem", placeholders1);
                    
                    Map<String, String> placeholders2 = new HashMap<>();
                    placeholders2.put("tempo", String.valueOf(count));
                    placeholders2.put("player", player1.getName());
                    plugin.getMessageManager().sendMessage(player2, "duelo.contagem", placeholders2);
                    
                    count--;
                } else {
                    // Contagem regressiva terminou, inicia o duelo
                    BukkitTask task = duelTimers.get(duelId);
                    if (task != null) {
                        task.cancel();
                    }
                    
                    // Remover a flag de contagem regressiva
                    duel.setInCountdown(false);
                    duel.setState(DuelState.IN_PROGRESS);
                    
                    // Enviar mensagem normal
                    plugin.getMessageManager().sendMessage(player1, "duelo.comecou");
                    plugin.getMessageManager().sendMessage(player2, "duelo.comecou");
                    
                    // Mostrar scoreboard de duelo em andamento
                    X1ScoreboardManager.mostrarDuel(player1, duel);
                    X1ScoreboardManager.mostrarDuel(player2, duel);
                    
                    // Tocar som de início de duelo
                    String sound = plugin.getConfigManager().getConfig().getString("sons.iniciar-duelo", "NOTE_PLING");
                    player1.playSound(player1.getLocation(), org.bukkit.Sound.valueOf(sound), 1.0f, 1.0f);
                    player2.playSound(player2.getLocation(), org.bukkit.Sound.valueOf(sound), 1.0f, 1.0f);
                    
                    // Define o tempo máximo do duelo
                    int maxTime = plugin.getConfigManager().getDuelMaxTime();
                    duelTimers.put(duelId, Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
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
        
        // Iniciar registro do duelo
        if (plugin.isDuelLoggingEnabled()) {
            UUID logId = plugin.getDuelLogManager().startLogging(duel);
            if (logId != null) {
                duelLogsMap.put(duel.getId(), logId);
            }
        }
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
     * @param winner Nome do jogador vencedor (null se empate)
     */
    public void endDuel(Duel duel, String winner) {
        if (duel == null) {
            return;
        }
        
        try {
            UUID duelId = duel.getId();
            String player1 = duel.getPlayer1();
            String player2 = duel.getPlayer2();
            
            // Definir estado do duelo
            duel.setState(DuelState.ENDED);
            
            // Registrar o vencedor
            duel.setWinner(winner);
            
            // Verificar se o duelo terminou em empate
            boolean isDraw = (winner == null);
            
            // Obter instâncias dos jogadores (podem ser null se offline)
            Player player1Instance = Bukkit.getPlayerExact(player1);
            Player player2Instance = Bukkit.getPlayerExact(player2);
            
            // Restaurar estados dos jogadores
            if (player1Instance != null) {
                // Exibir scoreboard de resultado
                boolean p1IsWinner = duel.getPlayer1().equals(winner);
                X1ScoreboardManager.mostrarResultado(player1Instance, duel, p1IsWinner);
                
                player1Instance.setHealth(player1Instance.getMaxHealth());
                restorePlayerState(player1Instance);
                
                if (winner != null) {
                    if (duel.getPlayer1().equals(winner)) {
                        plugin.getMessageManager().sendMessage(player1Instance, "duelo.vitoria");
                    } else {
                        plugin.getMessageManager().sendMessage(player1Instance, "duelo.derrota");
                    }
                } else {
                    plugin.getMessageManager().sendMessage(player1Instance, "duelo.empate");
                }
                
                // Processar estatísticas para o jogador 1
                boolean player1IsWinner = duel.getPlayer1().equals(winner);
                System.out.println("[PrimeLeagueX1] Processando estatísticas para " + duel.getPlayer1() + 
                                  " (Vencedor: " + player1IsWinner + ", Empate: " + isDraw + ")");
                processStats(duel, duel.getPlayer1(), player1IsWinner);
            }
            
            if (player2Instance != null) {
                // Exibir scoreboard de resultado
                boolean p2IsWinner = duel.getPlayer2().equals(winner);
                X1ScoreboardManager.mostrarResultado(player2Instance, duel, p2IsWinner);
                
                player2Instance.setHealth(player2Instance.getMaxHealth());
                restorePlayerState(player2Instance);
                
                if (winner != null) {
                    if (duel.getPlayer2().equals(winner)) {
                        plugin.getMessageManager().sendMessage(player2Instance, "duelo.vitoria");
                    } else {
                        plugin.getMessageManager().sendMessage(player2Instance, "duelo.derrota");
                    }
                } else {
                    plugin.getMessageManager().sendMessage(player2Instance, "duelo.empate");
                }
                
                // Processar estatísticas para o jogador 2
                boolean player2IsWinner = duel.getPlayer2().equals(winner);
                System.out.println("[PrimeLeagueX1] Processando estatísticas para " + duel.getPlayer2() + 
                                  " (Vencedor: " + player2IsWinner + ", Empate: " + isDraw + ")");
                processStats(duel, duel.getPlayer2(), player2IsWinner);
            }
            
            // Atualizar sistema de rivalidades
            if (!isDraw && winner != null) {
                boolean rivalActivated = plugin.getRivalManager().updateRivalry(player1, player2, winner);
                
                // Se é um duelo 1v1, verificar e mostrar mensagens de rivalidade
                if (!duel.isTeamDuel() && player1Instance != null && player2Instance != null) {
                    if (rivalActivated) {
                        // Nova rivalidade detectada!
                        plugin.getRivalManager().sendRivalMessage(player1Instance, player2Instance, "detectada");
                    } else if (plugin.getRivalManager().isRivalry(player1, player2)) {
                        // Rivais já existentes, mostrar mensagem de fim de duelo
                        plugin.getRivalManager().showRivalDuelEndMessage(player1Instance, player2Instance, winner);
                    }
                }
            }
            
            // Remover o duelo da lista de duelos ativos
            activeDuels.remove(duelId);
            playersDuels.remove(duel.getPlayer1());
            playersDuels.remove(duel.getPlayer2());
            
            // Salvar o duelo no histórico
            try {
                System.out.println("[PrimeLeagueX1] Salvando duelo no histórico");
                plugin.getStatsManager().addDuelToHistory(duel);
            } catch (Exception e) {
                System.out.println("[PrimeLeagueX1] Erro ao salvar duelo no histórico: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Verificar se o sistema de logs está habilitado
            System.out.println("[PrimeLeagueX1] Verificando sistema de logs: " + plugin.isDuelLoggingEnabled());
            System.out.println("[PrimeLeagueX1] Duelo na lista de logs: " + duelLogsMap.containsKey(duel.getId()));
            
            // Finalizar registro do duelo
            if (plugin.isDuelLoggingEnabled() && duelLogsMap.containsKey(duel.getId())) {
                UUID logId = duelLogsMap.get(duel.getId());
                
                // Se não é time, vencedor é um jogador
                List<String> winnerTeam = null;
                if (winner != null) {
                    // Criar uma lista para armazenar todo o time vencedor
                    if (duel.isInTeam1(winner)) {
                        // Se o vencedor é da equipe 1, registrar toda a equipe 1
                        winnerTeam = new ArrayList<>(duel.getTeam1());
                        System.out.println("[PrimeLeagueX1] Finalizando duelo com vencedor da equipe 1: " + winner);
                    } else if (duel.isInTeam2(winner)) {
                        // Se o vencedor é da equipe 2, registrar toda a equipe 2
                        winnerTeam = new ArrayList<>(duel.getTeam2());
                        System.out.println("[PrimeLeagueX1] Finalizando duelo com vencedor da equipe 2: " + winner);
                    } else {
                        // Caso inesperado, mas vamos tratá-lo
                        winnerTeam = new ArrayList<>();
                        winnerTeam.add(winner);
                        System.out.println("[PrimeLeagueX1] Finalizando duelo com vencedor: " + winner + " (equipe não identificada)");
                    }
                } else {
                    System.out.println("[PrimeLeagueX1] Finalizando duelo com empate");
                }
                
                // Salvar o log
                try {
                    boolean saved = plugin.getDuelLogManager().stopLogging(logId, winnerTeam);
                    if (saved) {
                        System.out.println("[PrimeLeagueX1] Replay do duelo salvo com sucesso: " + logId);
                    } else {
                        System.out.println("[PrimeLeagueX1] Erro ao salvar replay do duelo: " + logId);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Erro ao salvar replay do duelo: " + e.getMessage());
                    e.printStackTrace();
                }
                
                duelLogsMap.remove(duel.getId());
            } else if (plugin.isDuelLoggingEnabled()) {
                System.out.println("[PrimeLeagueX1] Duelo não tinha log associado: " + duel.getId());
            }
            
            System.out.println("[PrimeLeagueX1] Finalização do duelo concluída");
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao finalizar duelo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Finaliza um duelo em equipe
     * 
     * @param duel Duelo a ser finalizado
     * @param winningPlayer Nome do jogador vencedor (será usado para determinar a equipe vencedora)
     */
    private void endTeamDuel(Duel duel, String winningPlayer) {
        try {
            // Cancelar o temporizador
            UUID duelId = duel.getId();
            BukkitTask task = duelTimers.get(duelId);
            if (task != null) {
                task.cancel();
                duelTimers.remove(duelId);
            }
            
            // Definir o estado do duelo
            duel.setState(DuelState.ENDED);
            duel.setEndTime(System.currentTimeMillis());
            
            // Remover tags de equipe, se disponível
            if (plugin.getTeamTagManager() != null) {
                plugin.getTeamTagManager().removeTeamTags(duel);
            }
            
            // Obter as equipes
            List<String> team1 = duel.getTeam1();
            List<String> team2 = duel.getTeam2();
            
            // Determinar equipe vencedora
            List<String> winnerTeam = null;
            boolean isDraw = winningPlayer == null || winningPlayer.isEmpty();
            
            if (!isDraw) {
                winnerTeam = duel.isInTeam1(winningPlayer) ? 
                        new ArrayList<>(duel.getTeam1()) : 
                        new ArrayList<>(duel.getTeam2());
            }
            
            // Remover tags personalizadas para todos os jogadores
            boolean useNametags = plugin.getConfigManager().useCustomNametags();
            
            // Coletar jogadores online das duas equipes
            List<Player> team1Players = new ArrayList<>();
            List<Player> team2Players = new ArrayList<>();
            
            for (String memberName : team1) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer != null && memberPlayer.isOnline()) {
                    team1Players.add(memberPlayer);
                    
                    // Remover da lista de PvP no WorldGuard
                    plugin.getWorldGuardHook().removePvPPlayer(memberName);
                    
                    // Resetar tag personalizada para este jogador
                    if (useNametags && plugin.getNametagManager() != null) {
                        // Remover tags personalizadas
                        resetPlayerNametags(memberPlayer);
                    }
                }
            }
            
            for (String memberName : team2) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer != null && memberPlayer.isOnline()) {
                    team2Players.add(memberPlayer);
                    
                    // Remover da lista de PvP no WorldGuard
                    plugin.getWorldGuardHook().removePvPPlayer(memberName);
                    
                    // Resetar tag personalizada para este jogador
                    if (useNametags && plugin.getNametagManager() != null) {
                        // Remover tags personalizadas
                        resetPlayerNametags(memberPlayer);
                    }
                }
            }
            
            // Calcular estatísticas
            int duelDuration = duel.getDuration();
            int team1Kills = duel.getTeamKills(1);
            int team2Kills = duel.getTeamKills(2);
            int totalKills = team1Kills + team2Kills;
            
            // Recompensar jogadores (experiência, dinheiro, etc.)
            // Devolve jogadores para seus locais e estados anteriores
            if (isDraw) {
                // Caso de empate
                for (Player player : team1Players) {
                    // Exibir scoreboard de resultado
                    X1ScoreboardManager.mostrarResultado(player, duel, false);
                    
                    player.setHealth(player.getMaxHealth());
                    restorePlayerState(player);
                    
                    plugin.getMessageManager().sendMessage(player, "equipes.duelo-empate");
                    
                    // Mostrar estatísticas
                    sendTeamDuelStats(player, duel, null, isDraw);
                    
                    // Processar estatísticas
                    processStats(duel, player.getName(), false);
                }
                
                for (Player player : team2Players) {
                    // Exibir scoreboard de resultado
                    X1ScoreboardManager.mostrarResultado(player, duel, false);
                    
                    player.setHealth(player.getMaxHealth());
                    restorePlayerState(player);
                    
                    plugin.getMessageManager().sendMessage(player, "equipes.duelo-empate");
                    
                    // Mostrar estatísticas
                    sendTeamDuelStats(player, duel, null, isDraw);
                    
                    // Processar estatísticas
                    processStats(duel, player.getName(), false);
                }
            } else {
                // Caso de vitória/derrota
                for (Player player : team1Players) {
                    player.setHealth(player.getMaxHealth());
                    restorePlayerState(player);
                    
                    if (winnerTeam == team1) {
                        // Exibir scoreboard de resultado para o vencedor
                        X1ScoreboardManager.mostrarResultado(player, duel, true);
                        plugin.getMessageManager().sendMessage(player, "equipes.duelo-vitoria");
                    } else {
                        // Exibir scoreboard de resultado para o perdedor
                        X1ScoreboardManager.mostrarResultado(player, duel, false);
                        plugin.getMessageManager().sendMessage(player, "equipes.duelo-derrota");
                    }
                    
                    // Mostrar estatísticas
                    sendTeamDuelStats(player, duel, winnerTeam, isDraw);
                    
                    // Processar estatísticas
                    boolean won = (winnerTeam == team1);
                    processStats(duel, player.getName(), won);
                }
                
                for (Player player : team2Players) {
                    player.setHealth(player.getMaxHealth());
                    restorePlayerState(player);
                    
                    if (winnerTeam == team2) {
                        // Exibir scoreboard de resultado para o vencedor
                        X1ScoreboardManager.mostrarResultado(player, duel, true);
                        plugin.getMessageManager().sendMessage(player, "equipes.duelo-vitoria");
                    } else {
                        // Exibir scoreboard de resultado para o perdedor
                        X1ScoreboardManager.mostrarResultado(player, duel, false);
                        plugin.getMessageManager().sendMessage(player, "equipes.duelo-derrota");
                    }
                    
                    // Mostrar estatísticas
                    sendTeamDuelStats(player, duel, winnerTeam, isDraw);
                    
                    // Processar estatísticas
                    boolean won = (winnerTeam == team2);
                    processStats(duel, player.getName(), won);
                }
            }
            
            // Processa apostas, se houver
            double betAmount = duel.getBetAmount();
            if (betAmount > 0 && !isDraw) {
                processTeamBets(team1, team2, winnerTeam, betAmount);
            } else if (betAmount > 0) {
                // Devolve apostas em caso de empate
                refundTeamBets(team1, team2, betAmount);
            }
            
            // Remover o duelo da lista de duelos ativos
            activeDuels.remove(duelId);
            
            // Remover jogadores do mapa de duelos
            for (String memberName : team1) {
                playersDuels.remove(memberName);
            }
            
            for (String memberName : team2) {
                playersDuels.remove(memberName);
            }
            
            // Salvar o duelo no histórico
            try {
                // Salvar o duelo no histórico
                plugin.getStatsManager().addDuelToHistory(duel);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao salvar duelo no histórico: " + e.getMessage());
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao finalizar duelo em equipe: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Finalizar registro do duelo
        if (plugin.isDuelLoggingEnabled() && duelLogsMap.containsKey(duel.getId())) {
            UUID logId = duelLogsMap.get(duel.getId());
            
            // Se não é time, vencedor é um jogador
            List<String> winnerTeam = null;
            if (winningPlayer != null) {
                winnerTeam = new ArrayList<>();
                winnerTeam.add(winningPlayer);
                System.out.println("[PrimeLeagueX1] Finalizando duelo com vencedor: " + winningPlayer);
            } else {
                System.out.println("[PrimeLeagueX1] Finalizando duelo com empate");
            }
            
            // Salvar o log
            try {
                boolean saved = plugin.getDuelLogManager().stopLogging(logId, winnerTeam);
                if (saved) {
                    System.out.println("[PrimeLeagueX1] Replay do duelo salvo com sucesso: " + logId);
                } else {
                    System.out.println("[PrimeLeagueX1] Erro ao salvar replay do duelo: " + logId);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao salvar replay do duelo: " + e.getMessage());
                e.printStackTrace();
            }
            
            duelLogsMap.remove(duel.getId());
        } else if (plugin.isDuelLoggingEnabled()) {
            System.out.println("[PrimeLeagueX1] Duelo não tinha log associado: " + duel.getId());
        }
    }
    
    /**
     * Envia estatísticas do duelo em equipe para um jogador
     * 
     * @param player Jogador que receberá as estatísticas
     * @param duel Duelo com as estatísticas
     * @param winnerTeam Equipe vencedora (null se for empate)
     * @param isDraw true se o duelo terminou em empate
     */
    private void sendTeamDuelStats(Player player, Duel duel, List<String> winnerTeam, boolean isDraw) {
        // Título
        plugin.getMessageManager().sendMessage(player, "equipes.estatisticas-titulo");
        
        // Equipe vencedora
        if (!isDraw) {
            String winnerLeader = winnerTeam.get(0);
            plugin.getMessageManager().sendMessage(player, "equipes.estatisticas-equipe-vencedora",
                    "%equipe%", winnerLeader);
        }
        
        // Duração
        plugin.getMessageManager().sendMessage(player, "equipes.estatisticas-duracao",
                "%tempo%", String.valueOf(duel.getDuration()));
        
        // Total de eliminações
        int team1Kills = duel.getTeamKills(1);
        int team2Kills = duel.getTeamKills(2);
        plugin.getMessageManager().sendMessage(player, "equipes.estatisticas-eliminacoes",
                "%kills%", String.valueOf(team1Kills + team2Kills));
        
        // Eliminações por jogador
        Map<String, Integer> allKills = duel.getAllKills();
        for (Map.Entry<String, Integer> entry : allKills.entrySet()) {
            if (entry.getValue() > 0) {
                plugin.getMessageManager().sendMessage(player, "equipes.estatisticas-jogador-kills",
                        "%jogador%", entry.getKey(),
                        "%kills%", String.valueOf(entry.getValue()));
            }
        }
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
     * @param player Jogador a ser preparado
     * @param duelType Tipo de duelo
     */
    private void preparePlayer(Player player, DuelType duelType) {
        try {
            System.out.println("[PrimeLeagueX1] Preparando " + player.getName() + " para duelo do tipo " + 
                    (duelType != null ? duelType.name() : "null"));
            
            // Se o duelo utiliza kit, dá o kit de duelo para o jogador
            if (duelType != null && duelType.usesKit()) {
                boolean inventoryEmpty = false;
                
                try {
                    inventoryEmpty = plugin.getConfigManager().checkEmptyInventory();
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao verificar configuração de inventário vazio: " + e.getMessage());
                }
                
                // Se a verificação de inventário vazio estiver ativada, verifica se o inventário está vazio
                if (inventoryEmpty && !InventoryUtils.isInventoryEmpty(player)) {
                    try {
                        plugin.getMessageManager().sendMessage(player, "duelo.inventario-nao-vazio");
                    } catch (Exception e) {
                        player.sendMessage("§cSeu inventário precisa estar vazio para receber o kit!");
                    }
                }
                
                // Fornecer o kit de duelo de qualquer forma - o jogador aceitou o duelo
                giveKit(player, duelType);
            } else {
                System.out.println("[PrimeLeagueX1] Jogador " + player.getName() + " usará seus próprios itens.");
                // Não precisa verificar se o inventário está vazio quando usar kit próprio
            }
            
            // Outras preparações antes do duelo
            player.setFireTicks(0);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20);
            
            System.out.println("[PrimeLeagueX1] Jogador " + player.getName() + " preparado com sucesso.");
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO ao preparar jogador " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Tentar pelo menos fazer as preparações básicas
            try {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setFireTicks(0);
            } catch (Exception ex) {
                System.out.println("[PrimeLeagueX1] Não foi possível fazer nem preparações básicas: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Fornece o kit de duelo para um jogador
     * 
     * @param player Jogador para receber o kit
     * @param duelType Tipo de duelo
     */
    private void giveKit(Player player, DuelType duelType) {
        try {
            System.out.println("[PrimeLeagueX1] Fornecendo kit para " + player.getName() + " do tipo " + duelType.name());
            
            // Verificação de segurança para tipo nulo
            if (duelType == null) {
                System.out.println("[PrimeLeagueX1] ALERTA: Tipo de duelo nulo ao dar kit para " + player.getName() + ". Usando ARENA_KIT como padrão.");
                duelType = DuelType.ARENA_KIT;
            }
            
            // Limpar o inventário e preparar para o kit
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.SURVIVAL);
            
            // Fornece o kit de duelo configurado
            if (!InventoryUtils.giveDuelKit(player, plugin, duelType)) {
                // Falha ao fornecer kit configurado, usar kit direto
                System.out.println("[PrimeLeagueX1] Falha ao fornecer kit configurado. Usando kit básico para " + player.getName());
                
                // Kit básico de diamante
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                
                // Espada
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                player.getInventory().setItem(0, sword);
                
                // Maçãs
                ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 10);
                player.getInventory().setItem(1, apples);
                
                player.updateInventory();
            }
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO GRAVE ao fornecer kit: " + e.getMessage());
            e.printStackTrace();
            
            try {
                // Kit de emergência
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                
                player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                player.getInventory().setItem(0, sword);
                
                ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 5);
                player.getInventory().setItem(1, apples);
                
                player.updateInventory();
            } catch (Exception ex) {
                System.out.println("[PrimeLeagueX1] Não foi possível nem fornecer kit de emergência: " + ex.getMessage());
            }
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

    /**
     * Inicia um duelo em equipe
     * 
     * @param duel Duelo a ser iniciado
     */
    private void startTeamDuel(Duel duel) {
        try {
            plugin.getLogger().info("Iniciando duelo em equipe: " + duel.getId());
            
            // Registrar o duelo como ativo
            UUID duelId = duel.getId();
            activeDuels.put(duelId, duel);
            
            // Obter as listas de jogadores
            List<String> team1 = duel.getTeam1();
            List<String> team2 = duel.getTeam2();
            
            List<Player> team1Players = new ArrayList<>();
            List<Player> team2Players = new ArrayList<>();
            
            // Converter nomes de jogadores em objetos Player
            for (String memberName : team1) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer != null && memberPlayer.isOnline()) {
                    team1Players.add(memberPlayer);
                    
                    // Salvar estado do jogador
                    savePlayerState(memberPlayer);
                    
                    // Adicionar jogador à lista de PvP permitido no WorldGuard
                    plugin.getWorldGuardHook().addPvPPlayer(memberName);
                }
            }
            
            for (String memberName : team2) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer != null && memberPlayer.isOnline()) {
                    team2Players.add(memberPlayer);
                    
                    // Salvar estado do jogador
                    savePlayerState(memberPlayer);
                    
                    // Adicionar jogador à lista de PvP permitido no WorldGuard
                    plugin.getWorldGuardHook().addPvPPlayer(memberName);
                }
            }
            
            // Registra o duelo
            for (Player player : team1Players) {
                playersDuels.put(player.getName(), duel);
            }
            
            for (Player player : team2Players) {
                playersDuels.put(player.getName(), duel);
            }
            
            // Preparar jogadores
            DuelType duelType = duel.getType();
            
            for (Player player : team1Players) {
                preparePlayer(player, duelType);
            }
            
            for (Player player : team2Players) {
                preparePlayer(player, duelType);
            }
            
            // Teleportar jogadores ou verificar distância
            if (duelType.isArena()) {
                // Teleportar para a arena
                Location pos1 = plugin.getArenaManager().getPos1();
                Location pos2 = plugin.getArenaManager().getPos2();
                
                for (Player player : team1Players) {
                    player.teleport(pos1);
                }
                
                for (Player player : team2Players) {
                    player.teleport(pos2);
                }
            } else {
                // Verificar distância entre os jogadores
                if (team1Players.size() > 0 && team2Players.size() > 0) {
                    Player leader1 = team1Players.get(0);
                    Player leader2 = team2Players.get(0);
                    
                    if (!plugin.getArenaManager().arePlayersCloseEnough(leader1, leader2)) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", leader2.getName());
                        plugin.getMessageManager().sendMessage(leader1, "duelo.muito-longe", placeholders);
                        cancelTeamDuel(duel);
                        return;
                    }
                }
            }
            
            // Dar kits se necessário
            if (duelType.usesKit()) {
                for (Player player : team1Players) {
                    giveKit(player, duelType);
                }
                
                for (Player player : team2Players) {
                    giveKit(player, duelType);
                }
            }
            
            // Iniciar contagem regressiva
            int countdown = plugin.getConfigManager().getDuelCountdown();
            
            // Enviar mensagem de início
            for (Player player : team1Players) {
                plugin.getMessageManager().sendMessage(player, "equipes.duelo-iniciando", 
                        "%equipe%", team2.get(0),
                        "%tipo%", duelType.getFormattedName());
            }
            
            for (Player player : team2Players) {
                plugin.getMessageManager().sendMessage(player, "equipes.duelo-iniciando", 
                        "%equipe%", team1.get(0),
                        "%tipo%", duelType.getFormattedName());
            }
            
            // Aplicar nametags se configurado
            if (plugin.getConfigManager().useCustomNametags()) {
                if (plugin.getNametagManager() != null) {
                    try {
                        // Usar o novo método para aplicar nametags a todos os membros da equipe
                        plugin.getNametagManager().applyTeamDuelTags(team1Players, team2Players);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao aplicar nametags de equipe: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            duel.setState(DuelState.STARTING);
            
            // Iniciar a contagem regressiva para o duelo
            BukkitTask duelTimer = new BukkitRunnable() {
                int count = countdown;
                
                @Override
                public void run() {
                    if (count > 0) {
                        // Enviar mensagem de contagem regressiva
                        for (Player player : team1Players) {
                            if (player.isOnline()) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("tempo", String.valueOf(count));
                                plugin.getMessageManager().sendMessage(player, "duelo.contagem", placeholders);
                            }
                        }
                        
                        for (Player player : team2Players) {
                            if (player.isOnline()) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("tempo", String.valueOf(count));
                                plugin.getMessageManager().sendMessage(player, "duelo.contagem", placeholders);
                            }
                        }
                        
                        count--;
                    } else {
                        // Iniciar o duelo
                        for (Player player : team1Players) {
                            if (player.isOnline()) {
                                plugin.getMessageManager().sendMessage(player, "duelo.comecou");
                            }
                        }
                            
                        for (Player player : team2Players) {
                            if (player.isOnline()) {
                                plugin.getMessageManager().sendMessage(player, "duelo.comecou");
                            }
                        }
                            
                        duel.setState(DuelState.IN_PROGRESS);
                            
                        // Configurar tags de equipe, se disponível
                        if (plugin.getTeamTagManager() != null) {
                            plugin.getTeamTagManager().applyTeamTags(duel);
                        }
                            
                        // Inicia o temporizador para duração máxima do duelo
                        startDuelMaxTimeTimer(duel);
                            
                        // Cancelar esta tarefa
                        duelTimers.remove(duelId);
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
            
            duelTimers.put(duelId, duelTimer);
            
            // Iniciar registro do duelo
            if (plugin.isDuelLoggingEnabled()) {
                UUID logId = plugin.getDuelLogManager().startLogging(duel);
                if (logId != null) {
                    duelLogsMap.put(duel.getId(), logId);
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao iniciar duelo em equipe: " + e.getMessage());
            e.printStackTrace();
            
            // Cancelar o duelo em caso de erro
            if (duel != null) {
                cancelTeamDuel(duel);
            }
        }
    }

    /**
     * Cancela um duelo em equipe
     */
    private void cancelTeamDuel(Duel duel) {
        plugin.getLogger().info("Cancelando duelo em equipe: " + duel.getId());
        
        // Remover o duelo das listas
        activeDuels.remove(duel.getId());
        
        List<String> team1 = duel.getTeam1();
        List<String> team2 = duel.getTeam2();
        
        // Coletar jogadores online das duas equipes
        List<Player> team1Players = new ArrayList<>();
        List<Player> team2Players = new ArrayList<>();
        
        // Restaurar estados dos jogadores da equipe 1
        for (String memberName : team1) {
            Player memberPlayer = Bukkit.getPlayerExact(memberName);
            if (memberPlayer != null && memberPlayer.isOnline()) {
                team1Players.add(memberPlayer);
                playersDuels.remove(memberPlayer.getName());
                restorePlayerState(memberPlayer);
                plugin.getWorldGuardHook().removePvPPlayer(memberName);
            }
        }
        
        // Restaurar estados dos jogadores da equipe 2
        for (String memberName : team2) {
            Player memberPlayer = Bukkit.getPlayerExact(memberName);
            if (memberPlayer != null && memberPlayer.isOnline()) {
                team2Players.add(memberPlayer);
                playersDuels.remove(memberPlayer.getName());
                restorePlayerState(memberPlayer);
                plugin.getWorldGuardHook().removePvPPlayer(memberName);
            }
        }
        
        // Remover nametags de todos os jogadores de uma vez
        if (plugin.getConfigManager().useCustomNametags() && plugin.getNametagManager() != null) {
            try {
                plugin.getNametagManager().removeTeamDuelTags(team1Players, team2Players);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao remover nametags de equipe: " + e.getMessage());
            }
        }
        
        // Cancelar temporizadores
        if (duelTimers.containsKey(duel.getId())) {
            duelTimers.get(duel.getId()).cancel();
            duelTimers.remove(duel.getId());
        }
    }

    /**
     * Inicia o temporizador para duração máxima do duelo
     */
    private void startDuelMaxTimeTimer(Duel duel) {
        int maxDuelTime = plugin.getConfigManager().getDuelMaxTime();
        
        if (maxDuelTime <= 0) {
            return; // Sem limite de tempo
        }
        
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (duel.getState() == DuelState.IN_PROGRESS) {
                    // Encerrar o duelo como empate se ainda estiver em andamento
                    endDuel(duel, null);
                }
            }
        }, maxDuelTime * 20L);
        
        duelTimers.put(duel.getId(), task);
    }

    /**
     * Envia um desafio de duelo em equipe
     * 
     * @param leaderName Nome do líder da equipe desafiante
     * @param targetLeaderName Nome do líder da equipe desafiada
     * @param type Tipo de duelo
     * @param betAmount Valor da aposta
     * @return true se o desafio foi enviado com sucesso
     */
    public boolean sendTeamChallenge(String leaderName, String targetLeaderName, DuelType type, double betAmount) {
        try {
            // Verificar parâmetros básicos
            if (leaderName == null || targetLeaderName == null || type == null) {
                plugin.getLogger().warning("Desafio em equipe inválido: parâmetros nulos");
                return false;
            }
            
            // Verificar se os jogadores são líderes de equipe
            if (!plugin.getTeamManager().isTeamLeader(leaderName)) {
                plugin.getLogger().warning("Desafio em equipe inválido: " + leaderName + " não é líder");
                return false;
            }
            
            if (!plugin.getTeamManager().isTeamLeader(targetLeaderName)) {
                plugin.getLogger().warning("Desafio em equipe inválido: " + targetLeaderName + " não é líder");
                return false;
            }
            
            // Verificar se o tipo de duelo é para equipes
            if (!type.isTeamDuel()) {
                plugin.getLogger().warning("Desafio em equipe inválido: tipo de duelo não é para equipes");
                return false;
            }
            
            // Obter as equipes
            List<String> team1 = plugin.getTeamManager().getTeam(leaderName);
            List<String> team2 = plugin.getTeamManager().getTeam(targetLeaderName);
            
            if (team1 == null || team2 == null) {
                plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "equipes.erro-equipes");
                return false;
            }
            
            // Verificar se as equipes têm o mesmo tamanho
            if (team1.size() != team2.size()) {
                plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "equipes.tamanho-diferente");
                return false;
            }
            
            // Verificar se o tamanho das equipes corresponde ao tipo de duelo
            int requiredTeamSize = type.getTeamSize();
            if (team1.size() != requiredTeamSize) {
                plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "equipes.tamanho-invalido");
                return false;
            }
            
            // Verificar se todos os jogadores estão online
            for (String memberName : team1) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer == null || !memberPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "equipes.membro-offline", 
                            "%jogador%", memberName);
                    return false;
                }
            }
            
            for (String memberName : team2) {
                Player memberPlayer = Bukkit.getPlayerExact(memberName);
                if (memberPlayer == null || !memberPlayer.isOnline()) {
                    plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "equipes.membro-offline", 
                            "%jogador%", memberName);
                    return false;
                }
            }
            
            // Verificar se algum jogador já está em duelo
            for (String memberName : team1) {
                if (isInDuel(memberName)) {
                    plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "equipes.membro-em-duelo", 
                            "%jogador%", memberName);
                    return false;
                }
            }
            
            for (String memberName : team2) {
                if (isInDuel(memberName)) {
                    plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "equipes.membro-em-duelo", 
                            "%jogador%", memberName);
                    return false;
                }
            }
            
            // Verificar se já existe um desafio pendente para o jogador alvo
            if (challenges.containsKey(leaderName) && challenges.get(leaderName).equals(targetLeaderName)) {
                // Já existe um desafio pendente, não enviar novamente
                plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "duelo.desafio-ja-enviado", 
                        "%jogador%", targetLeaderName);
                return false;
            }
            
            // Verificar apostas
            if (betAmount > 0) {
                if (!plugin.getEconomyManager().isEconomyEnabled()) {
                    plugin.getMessageManager().sendMessage(Bukkit.getPlayerExact(leaderName), "apostas.economia-desativada");
                    return false;
                }
                
                // Verificar se o jogador tem dinheiro suficiente
                Player leader = Bukkit.getPlayerExact(leaderName);
                if (!plugin.getEconomyManager().hasMoney(leader, betAmount)) {
                    plugin.getEconomyManager().sendNotEnoughMoneyMessage(leader, betAmount);
                    return false;
                }
            }
            
            // Registrar o desafio
            challenges.put(leaderName, targetLeaderName);
            challengeTypes.put(leaderName, type);
            
            if (betAmount > 0) {
                betAmounts.put(leaderName, betAmount);
            }
            
            // Enviar mensagens para equipe desafiante
            Player challenger = Bukkit.getPlayerExact(leaderName);
            if (challenger != null && challenger.isOnline()) {
                if (betAmount > 0) {
                    plugin.getMessageManager().sendMessage(challenger, "equipes.desafio-enviado-aposta", 
                            "%jogador%", targetLeaderName, 
                            "%tipo%", type.getFormattedName(),
                            "%valor%", plugin.getEconomyManager().formatMoney(betAmount));
                } else {
                    plugin.getMessageManager().sendMessage(challenger, "equipes.desafio-enviado", 
                            "%jogador%", targetLeaderName, 
                            "%tipo%", type.getFormattedName());
                }
                
                // Notificar membros da equipe 1
                for (String memberName : team1) {
                    if (!memberName.equals(leaderName)) {
                        Player memberPlayer = Bukkit.getPlayerExact(memberName);
                        if (memberPlayer != null && memberPlayer.isOnline()) {
                            if (betAmount > 0) {
                                plugin.getMessageManager().sendMessage(memberPlayer, "equipes.desafio-enviado-membro-aposta", 
                                        "%jogador%", targetLeaderName,
                                        "%tipo%", type.getFormattedName(),
                                        "%valor%", plugin.getEconomyManager().formatMoney(betAmount));
                            } else {
                                plugin.getMessageManager().sendMessage(memberPlayer, "equipes.desafio-enviado-membro", 
                                        "%jogador%", targetLeaderName,
                                        "%tipo%", type.getFormattedName());
                            }
                        }
                    }
                }
            }
            
            // Enviar mensagens para equipe desafiada
            Player target = Bukkit.getPlayerExact(targetLeaderName);
            if (target != null && target.isOnline()) {
                if (betAmount > 0) {
                    plugin.getMessageManager().sendMessage(target, "equipes.desafio-recebido-aposta", 
                            "%jogador%", leaderName, 
                            "%tipo%", type.getFormattedName(),
                            "%valor%", plugin.getEconomyManager().formatMoney(betAmount),
                            "%comando%", "/x1 equipe aceitar");
                } else {
                    plugin.getMessageManager().sendMessage(target, "equipes.desafio-recebido", 
                            "%jogador%", leaderName, 
                            "%tipo%", type.getFormattedName(),
                            "%comando%", "/x1 equipe aceitar");
                }
                
                // Notificar membros da equipe 2
                for (String memberName : team2) {
                    if (!memberName.equals(targetLeaderName)) {
                        Player memberPlayer = Bukkit.getPlayerExact(memberName);
                        if (memberPlayer != null && memberPlayer.isOnline()) {
                            if (betAmount > 0) {
                                plugin.getMessageManager().sendMessage(memberPlayer, "equipes.desafio-recebido-membro-aposta", 
                                        "%jogador%", leaderName,
                                        "%tipo%", type.getFormattedName(),
                                        "%valor%", plugin.getEconomyManager().formatMoney(betAmount),
                                        "%lider%", targetLeaderName);
                            } else {
                                plugin.getMessageManager().sendMessage(memberPlayer, "equipes.desafio-recebido-membro", 
                                        "%jogador%", leaderName,
                                        "%tipo%", type.getFormattedName(),
                                        "%lider%", targetLeaderName);
                            }
                        }
                    }
                }
            }
            
            // Agendar expiração do desafio
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (challenges.containsKey(leaderName) && challenges.get(leaderName).equals(targetLeaderName)) {
                    challenges.remove(leaderName);
                    challengeTypes.remove(leaderName);
                    betAmounts.remove(leaderName);
                    
                    Player challengerPlayer = Bukkit.getPlayerExact(leaderName);
                    if (challengerPlayer != null && challengerPlayer.isOnline()) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", targetLeaderName);
                        plugin.getMessageManager().sendMessage(challengerPlayer, "duelo.desafio-expirado", placeholders);
                    }
                    
                    Player targetPlayer = Bukkit.getPlayerExact(targetLeaderName);
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", leaderName);
                        plugin.getMessageManager().sendMessage(targetPlayer, "duelo.desafio-expirado-alvo", placeholders);
                    }
                }
            }, 20 * 60); // 60 segundos
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao enviar desafio de equipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Processa as estatísticas de um jogador após um duelo
     * 
     * @param duel Duelo que terminou
     * @param playerName Nome do jogador
     * @param isWinner true se o jogador venceu, false se perdeu ou empatou
     */
    private void processStats(Duel duel, String playerName, boolean isWinner) {
        try {
            System.out.println("[PrimeLeagueX1] Processando estatísticas para " + playerName + ": " + 
                              (isWinner ? "vitória" : (duel.isDraw() ? "empate" : "derrota")));
                
            if (duel.isDraw()) {
                // Registrar empate nas estatísticas
                System.out.println("[PrimeLeagueX1] Chamando addDraw para " + playerName);
                plugin.getStatsManager().addDraw(playerName);
                System.out.println("[PrimeLeagueX1] Empate registrado para " + playerName);
            } else if (isWinner) {
                // Registrar vitória nas estatísticas
                System.out.println("[PrimeLeagueX1] Chamando addVictory para " + playerName);
                plugin.getStatsManager().addVictory(playerName);
                System.out.println("[PrimeLeagueX1] Vitória registrada para " + playerName);
                
                // Se estiver usando Elo, atualizar
                if (plugin.getConfigManager().useElo()) {
                    String opponent = duel.getPlayer1().equals(playerName) ? duel.getPlayer2() : duel.getPlayer1();
                    plugin.getStatsManager().updateElo(playerName, opponent);
                }
            } else {
                // Registrar derrota nas estatísticas
                System.out.println("[PrimeLeagueX1] Chamando addDefeat para " + playerName);
                plugin.getStatsManager().addDefeat(playerName);
                System.out.println("[PrimeLeagueX1] Derrota registrada para " + playerName);
            }
            
            // Verificar se as estatísticas foram atualizadas corretamente
            PlayerStats stats = plugin.getStatsManager().getPlayerStats(playerName);
            System.out.println("[PrimeLeagueX1] Estatísticas atuais de " + playerName + ": V=" + 
                              stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + 
                              stats.getDraws() + ", Elo=" + stats.getElo());
            
            // Garantir que os dados sejam salvos globalmente
            System.out.println("[PrimeLeagueX1] Forçando salvamento de dados após atualizar estatísticas");
            plugin.getFileStorage().saveData();
        } catch (Exception e) {
            // Em caso de erro, apenas logar a falha
            plugin.getLogger().warning("Erro ao processar estatísticas para " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Processa apostas para duelos em equipe
     * 
     * @param team1 Lista de jogadores da equipe 1
     * @param team2 Lista de jogadores da equipe 2
     * @param winnerTeam Lista de jogadores da equipe vencedora
     * @param betAmount Valor da aposta por jogador
     */
    private void processTeamBets(List<String> team1, List<String> team2, List<String> winnerTeam, double betAmount) {
        boolean team1Won = (winnerTeam == team1);
        
        // Processar apostas para equipe 1
        for (String playerName : team1) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                if (team1Won) {
                    // Equipe 1 ganhou
                    double winAmount = betAmount * 2.0 / team1.size();
                    plugin.getEconomyManager().processDuelWin(player, winAmount);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("valor", plugin.getEconomyManager().formatMoney(winAmount));
                    plugin.getMessageManager().sendMessage(player, "economia.aposta-vencida", placeholders);
                } else {
                    // Equipe 1 perdeu
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("valor", plugin.getEconomyManager().formatMoney(betAmount));
                    plugin.getMessageManager().sendMessage(player, "economia.aposta-perdida", placeholders);
                }
            }
        }
        
        // Processar apostas para equipe 2
        for (String playerName : team2) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                if (!team1Won) {
                    // Equipe 2 ganhou
                    double winAmount = betAmount * 2.0 / team2.size();
                    plugin.getEconomyManager().processDuelWin(player, winAmount);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("valor", plugin.getEconomyManager().formatMoney(winAmount));
                    plugin.getMessageManager().sendMessage(player, "economia.aposta-vencida", placeholders);
                } else {
                    // Equipe 2 perdeu
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("valor", plugin.getEconomyManager().formatMoney(betAmount));
                    plugin.getMessageManager().sendMessage(player, "economia.aposta-perdida", placeholders);
                }
            }
        }
    }
    
    /**
     * Devolve as apostas para duelos em equipe em caso de empate
     * 
     * @param team1 Lista de jogadores da equipe 1
     * @param team2 Lista de jogadores da equipe 2
     * @param betAmount Valor da aposta por jogador
     */
    private void refundTeamBets(List<String> team1, List<String> team2, double betAmount) {
        // Devolver apostas para equipe 1
        for (String playerName : team1) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                plugin.getEconomyManager().returnBet(player, betAmount);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("valor", plugin.getEconomyManager().formatMoney(betAmount));
                plugin.getMessageManager().sendMessage(player, "economia.aposta-devolvida", placeholders);
            }
        }
        
        // Devolver apostas para equipe 2
        for (String playerName : team2) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                plugin.getEconomyManager().returnBet(player, betAmount);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("valor", plugin.getEconomyManager().formatMoney(betAmount));
                plugin.getMessageManager().sendMessage(player, "economia.aposta-devolvida", placeholders);
            }
        }
    }

    /**
     * Reseta as nametags de um jogador
     * 
     * @param player Jogador para resetar as nametags
     */
    private void resetPlayerNametags(Player player) {
        try {
            // Tentar usar o método removePlayerTags se disponível
            plugin.getNametagManager().getClass().getMethod("removePlayerTags", Player.class)
                .invoke(plugin.getNametagManager(), player);
        } catch (Exception e) {
            // Em caso de erro, apenas logar a falha
            plugin.getLogger().warning("Erro ao resetar nametags para " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Registra eliminação em um duelo
     * 
     * @param duel Duelo
     * @param killed Jogador eliminado
     * @param killer Jogador que eliminou
     */
    public void logKill(Duel duel, Player killed, Player killer) {
        if (plugin.isDuelLoggingEnabled() && duelLogsMap.containsKey(duel.getId())) {
            UUID logId = duelLogsMap.get(duel.getId());
            plugin.getDuelLogManager().logKill(logId, killed, killer);
        }
    }

    /**
     * Registra dano significativo em um duelo
     * 
     * @param duel Duelo
     * @param damaged Jogador que recebeu dano
     * @param damager Jogador que causou dano
     * @param damage Quantidade de dano
     */
    public void logDamage(Duel duel, Player damaged, Player damager, double damage) {
        if (plugin.isDuelLoggingEnabled() && 
                plugin.getConfigManager().getConfig().getBoolean("registro-duelos.eventos.danos-significativos", true) &&
                duelLogsMap.containsKey(duel.getId())) {
            UUID logId = duelLogsMap.get(duel.getId());
            plugin.getDuelLogManager().logDamage(logId, damaged, damager, damage);
        }
    }

    /**
     * Obtém o ID do log de duelo associado a um duelo específico
     * 
     * @param duel Duelo para obter o ID do log
     * @return UUID do log do duelo ou null se não existir
     */
    public UUID getDuelLogId(Duel duel) {
        return duelLogsMap.get(duel.getId());
    }

    /**
     * Obtém o UUID do duelo em que o jogador está participando
     * 
     * @param playerName Nome do jogador
     * @return UUID do duelo ou null se o jogador não estiver em nenhum duelo
     */
    public UUID getPlayerDuelId(String playerName) {
        Duel duel = playersDuels.get(playerName);
        if (duel != null) {
            return duel.getId();
        }
        return null;
    }

    /**
     * Inicia um duelo
     * 
     * @param duel Duelo a ser iniciado
     */
    public void startDuel(Duel duel) {
        if (duel == null) {
            plugin.getLogger().warning("Tentativa de iniciar duelo nulo!");
            return;
        }
        
        // Verificar se ambas as equipes têm jogadores
        if (duel.getTeam1().isEmpty() || duel.getTeam2().isEmpty()) {
            plugin.getLogger().warning("Tentativa de iniciar duelo com equipe vazia!");
            return;
        }
        
        try {
            // Definir estado
            duel.setState(DuelState.STARTING);
            
            // Registrar duelo
            recordDuelStart(duel);
            
            // Preparar jogadores
            // (já foi feito no método startDuelBetweenPlayers)
            
            // Iniciar countdown
            startDuelCountdown(duel);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao iniciar duelo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registra o início de um duelo
     * 
     * @param duel Duelo que está sendo iniciado
     */
    private void recordDuelStart(Duel duel) {
        // Registrar horário de início do duelo
        duel.setStartTime(System.currentTimeMillis());
        
        // Iniciar registro de log, se ativado
        if (plugin.isDuelLoggingEnabled()) {
            try {
                UUID logId = plugin.getDuelLogManager().startLogging(duel);
                if (logId != null) {
                    duelLogsMap.put(duel.getId(), logId);
                    plugin.getLogger().info("Iniciando registro de log para duelo: " + duel.getId());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao iniciar log do duelo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Inicia a contagem regressiva para um duelo
     * 
     * @param duel Duelo para iniciar a contagem
     */
    private void startDuelCountdown(Duel duel) {
        // Marcar que o duelo está em contagem regressiva
        duel.setInCountdown(true);
        
        // Buscar jogadores das equipes
        List<Player> team1Players = new ArrayList<>();
        List<Player> team2Players = new ArrayList<>();
        
        for (String playerName : duel.getTeam1()) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                team1Players.add(player);
            }
        }
        
        for (String playerName : duel.getTeam2()) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                team2Players.add(player);
            }
        }
        
        // Obter configuração de contagem
        final int countdownSeconds = plugin.getConfigManager().getDuelCountdown();
        
        // Definir tag de equipe, se disponível
        if (plugin.getTeamTagManager() != null) {
            plugin.getTeamTagManager().applyTeamTags(duel);
        }
        
        // Iniciar contagem regressiva
        new BukkitRunnable() {
            int count = countdownSeconds;
            
            @Override
            public void run() {
                // Verificar se o duelo ainda está ativo
                if (!activeDuels.containsKey(duel.getId()) || duel.getState() != DuelState.STARTING) {
                    this.cancel();
                    return;
                }
                
                // Verificar se todos os jogadores estão online
                boolean allOnline = true;
                for (Player player : team1Players) {
                    if (!player.isOnline()) {
                        allOnline = false;
                        break;
                    }
                }
                
                for (Player player : team2Players) {
                    if (!player.isOnline()) {
                        allOnline = false;
                        break;
                    }
                }
                
                if (!allOnline) {
                    this.cancel();
                    endDuel(duel, null);
                    return;
                }
                
                // Enviar contagem para todos os jogadores
                if (count > 0) {
                    String message = plugin.getMessageManager().getMessage("duelo.contagem")
                            .replace("%segundos%", String.valueOf(count));
                    
                    for (Player player : team1Players) {
                        player.sendMessage(ColorUtils.colorize(message));
                        
                        // Enviar título para o segundo final
                        if (count <= 3) {
                            EffectsUtils.sendTitle(player, "§c" + count, "", 5, 15, 5);
                            EffectsUtils.playSound(player, "CLICK");
                        }
                    }
                    
                    for (Player player : team2Players) {
                        player.sendMessage(ColorUtils.colorize(message));
                        
                        // Enviar título para o segundo final
                        if (count <= 3) {
                            EffectsUtils.sendTitle(player, "§c" + count, "", 5, 15, 5);
                            EffectsUtils.playSound(player, "CLICK");
                        }
                    }
                    
                    count--;
                } else {
                    // Contagem finalizada, iniciar o duelo
                    this.cancel();
                    
                    // Mudar estado do duelo
                    duel.setState(DuelState.IN_PROGRESS);
                    duel.setInCountdown(false);
                    
                    // Notificar os jogadores
                    String message = plugin.getMessageManager().getMessage("duelo.iniciado");
                    
                    for (Player player : team1Players) {
                        player.sendMessage(ColorUtils.colorize(message));
                        EffectsUtils.sendTitle(player, "§a§lVAMOS!", "", 5, 20, 5);
                        EffectsUtils.playSound(player, "LEVEL_UP");
                    }
                    
                    for (Player player : team2Players) {
                        player.sendMessage(ColorUtils.colorize(message));
                        EffectsUtils.sendTitle(player, "§a§lVAMOS!", "", 5, 20, 5);
                        EffectsUtils.playSound(player, "LEVEL_UP");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Verifica se um duelo ocorre entre rivais e anuncia quando apropriado
     * 
     * @param duel Duelo a ser verificado
     */
    private void checkForRivalDuel(Duel duel) {
        // Verificar apenas para duelos 1v1
        if (duel.isTeamDuel()) {
            return;
        }
        
        List<String> team1 = duel.getTeam1();
        List<String> team2 = duel.getTeam2();
        
        if (team1.size() == 1 && team2.size() == 1) {
            String player1Name = team1.get(0);
            String player2Name = team2.get(0);
            
            // Verificar se existe rivalidade
            if (plugin.getConfigManager().getConfig().getBoolean("rivalidade.ativar", true) &&
                    plugin.getRivalManager().isRivalry(player1Name, player2Name)) {
                // Anunciar duelo entre rivais
                plugin.getRivalManager().broadcastRivalDuel(player1Name, player2Name);
                
                // Verificar se devemos aplicar efeitos visuais especiais
                if (plugin.getConfigManager().getConfig().getBoolean("rivalidade.efeitos_especiais", true)) {
                    Player player1 = Bukkit.getPlayerExact(player1Name);
                    Player player2 = Bukkit.getPlayerExact(player2Name);
                    
                    // Título especial para rivais
                    if (player1 != null && player1.isOnline()) {
                        EffectsUtils.sendTitle(player1, "§c§lRIVALIDADE", "§fVS §e" + player2Name, 10, 60, 20);
                    }
                    
                    if (player2 != null && player2.isOnline()) {
                        EffectsUtils.sendTitle(player2, "§c§lRIVALIDADE", "§fVS §e" + player1Name, 10, 60, 20);
                    }
                }
            }
        }
    }
    
    /**
     * Prepara e inicia o duelo
     * 
     * @param challenger Jogador desafiante
     * @param challenged Jogador desafiado
     * @param type Tipo de duelo
     * @param betAmount Valor da aposta
     * @return true se o duelo foi iniciado com sucesso
     */
    public boolean startDuelBetweenPlayers(Player challenger, Player challenged, DuelType type, double betAmount) {
        try {
            // Criar duelo
            Duel duel = createDuel(challenger, challenged, type);
            
            // Definir aposta se houver
            if (betAmount > 0) {
                duel.setBetAmount(betAmount);
            }
            
            // Verificar se é um duelo entre rivais
            checkForRivalDuel(duel);
            
            // Preparar jogadores
            savePlayerState(challenger);
            savePlayerState(challenged);
            
            // Preparar jogadores (teleporte, kits, etc)
            preparePlayer(challenger, type);
            preparePlayer(challenged, type);
            
            // Iniciar o duelo
            startDuel(duel);
            
            // Enviar mensagens aos jogadores
            plugin.getMessageManager().sendMessage(challenger, "duelo.iniciando", 
                    "%player%", challenged.getName(),
                    "%tipo%", type.getFormattedName());
            
            plugin.getMessageManager().sendMessage(challenged, "duelo.iniciando", 
                    "%player%", challenger.getName(),
                    "%tipo%", type.getFormattedName());
            
            // Iniciar temporizador para tempo máximo do duelo
            startDuelMaxTimeTimer(duel);
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao iniciar duelo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envia mensagens de atualização de rivalidade para um jogador
     * 
     * @param player Jogador que receberá a mensagem
     * @param opponent Nome do rival
     * @param data Dados da rivalidade
     * @param result Resultado do último duelo
     */
    private void sendRivalryUpdateMessage(Player player, String opponent, RivalData data, String result) {
        player.sendMessage("§6Rivalidade atual contra §e" + opponent + "§6:");
        player.sendMessage(" §aVitórias suas: §f" + data.getVictories(player.getName()));
        player.sendMessage(" §cVitórias do rival: §f" + data.getVictories(opponent));
        player.sendMessage(" §7Último resultado: §f" + result);
    }
    
    /**
     * Cria um novo duelo entre dois jogadores
     * 
     * @param p1 Primeiro jogador
     * @param p2 Segundo jogador
     * @param type Tipo de duelo
     * @return O objeto Duel criado
     */
    public Duel createDuel(Player p1, Player p2, DuelType type) {
        Duel duel = new Duel(UUID.randomUUID(), p1.getName(), p2.getName(), type);
        
        // Adicionar aos mapas de duelos ativos
        activeDuels.put(duel.getId(), duel);
        playersDuels.put(p1.getName(), duel);
        playersDuels.put(p2.getName(), duel);
        
        return duel;
    }

    /**
     * Verifica se o inventário tem algum item
     * @param inventory Inventário para verificar
     * @return true se tiver algum item, false caso contrário
     */
    private boolean hasItems(PlayerInventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                return true;
            }
        }
        return false;
    }
}