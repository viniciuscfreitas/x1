package br.com.primeleague.x1.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import br.com.primeleague.x1.Main;

/**
 * Gerenciador de nametags para jogadores em duelo
 * Permite destacar o nome do adversário e indicar que os jogadores estão em X1
 */
public class NametagManager {

    private final Main plugin;
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, String> originalTeams;
    private final Map<UUID, Scoreboard> playerScoreboards;
    
    // Nomes das equipes
    private static final String DUEL_PREFIX_TEAM = "X1_";
    private static final String OPPONENT_PREFIX_TEAM = "OPP_";
    
    // Cores
    private static final ChatColor DEFAULT_COLOR = ChatColor.GRAY;
    private static final ChatColor DUEL_COLOR = ChatColor.RED;
    private static final ChatColor OPPONENT_COLOR = ChatColor.DARK_RED;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public NametagManager(Main plugin) {
        this.plugin = plugin;
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.originalTeams = new HashMap<>();
        this.playerScoreboards = new HashMap<>();
        
        // Inicializar o sistema de nametags
        setupSystem();
    }
    
    /**
     * Configura o sistema de nametags
     */
    private void setupSystem() {
        try {
            System.out.println("[PrimeLeagueX1] Inicializando sistema de nametags...");
            
            // Verificar se o scoreboard principal já tem times X1
            Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
            
            // Remover times antigos de outros usos do plugin
            for (Team team : mainScoreboard.getTeams()) {
                if (team.getName().startsWith(DUEL_PREFIX_TEAM) || 
                    team.getName().startsWith(OPPONENT_PREFIX_TEAM)) {
                    try {
                        team.unregister();
                        System.out.println("[PrimeLeagueX1] Time antigo " + team.getName() + " removido");
                    } catch (Exception e) {
                        System.out.println("[PrimeLeagueX1] Erro ao remover time antigo: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("[PrimeLeagueX1] Sistema de nametags inicializado");
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO ao inicializar sistema de nametags: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Aplica as nametags para dois jogadores em duelo
     * 
     * @param player1 Primeiro jogador
     * @param player2 Segundo jogador
     */
    public void applyDuelTags(Player player1, Player player2) {
        System.out.println("[PrimeLeagueX1] Aplicando nametags para " + player1.getName() + " e " + player2.getName());
        
        // Limpar possíveis times existentes antes de aplicar
        cleanupExistingTeams(player1);
        cleanupExistingTeams(player2);
        
        // Salvar os times originais dos jogadores
        saveOriginalTeam(player1);
        saveOriginalTeam(player2);
        
        // Criar scoreboards personalizados para cada jogador
        setupDuelScoreboard(player1, player2);
        setupDuelScoreboard(player2, player1);
        
        // Aplicar visualização para todos os outros jogadores
        setupGlobalDuelView(player1);
        setupGlobalDuelView(player2);
        
        System.out.println("[PrimeLeagueX1] Nametags aplicadas com sucesso");
    }
    
    /**
     * Aplica as nametags para todos os membros de duas equipes em duelo
     * 
     * @param team1Players Lista de jogadores da equipe 1
     * @param team2Players Lista de jogadores da equipe 2
     */
    public void applyTeamDuelTags(List<Player> team1Players, List<Player> team2Players) {
        if (team1Players == null || team2Players == null || team1Players.isEmpty() || team2Players.isEmpty()) {
            System.out.println("[PrimeLeagueX1] Erro: listas de equipes nulas ou vazias");
            return;
        }
        
        System.out.println("[PrimeLeagueX1] Aplicando nametags para duelo em equipe");
        
        // Aplicar nametags para cada jogador da equipe 1 com todos da equipe 2
        for (Player player1 : team1Players) {
            if (player1 != null && player1.isOnline()) {
                // Limpar possíveis times existentes antes de aplicar
                cleanupExistingTeams(player1);
                
                // Salvar os times originais dos jogadores
                saveOriginalTeam(player1);
                
                // Configurar visualização global
                setupGlobalDuelView(player1);
                
                // Criar scoreboard personalizado para o jogador ver todos os adversários
                Scoreboard playerScoreboard = scoreboardManager.getNewScoreboard();
                playerScoreboards.put(player1.getUniqueId(), playerScoreboard);
                
                // Criar time para os oponentes
                Team opponentTeam = playerScoreboard.registerNewTeam(OPPONENT_PREFIX_TEAM + player1.getName());
                opponentTeam.setPrefix(OPPONENT_COLOR + "");
                
                // Adicionar todos os jogadores da equipe 2 como adversários
                for (Player opponent : team2Players) {
                    if (opponent != null && opponent.isOnline()) {
                        opponentTeam.addPlayer(opponent);
                    }
                }
                
                // Criar time para os aliados
                Team allyTeam = playerScoreboard.registerNewTeam("ALLY_" + player1.getName());
                allyTeam.setPrefix(ChatColor.GREEN + "");
                
                // Adicionar todos os jogadores da equipe 1 como aliados (exceto o próprio jogador)
                for (Player ally : team1Players) {
                    if (ally != null && ally.isOnline() && !ally.equals(player1)) {
                        allyTeam.addPlayer(ally);
                    }
                }
                
                // Aplicar o scoreboard
                player1.setScoreboard(playerScoreboard);
            }
        }
        
        // Aplicar nametags para cada jogador da equipe 2 com todos da equipe 1
        for (Player player2 : team2Players) {
            if (player2 != null && player2.isOnline()) {
                // Limpar possíveis times existentes antes de aplicar
                cleanupExistingTeams(player2);
                
                // Salvar os times originais dos jogadores
                saveOriginalTeam(player2);
                
                // Configurar visualização global
                setupGlobalDuelView(player2);
                
                // Criar scoreboard personalizado para o jogador ver todos os adversários
                Scoreboard playerScoreboard = scoreboardManager.getNewScoreboard();
                playerScoreboards.put(player2.getUniqueId(), playerScoreboard);
                
                // Criar time para os oponentes
                Team opponentTeam = playerScoreboard.registerNewTeam(OPPONENT_PREFIX_TEAM + player2.getName());
                opponentTeam.setPrefix(OPPONENT_COLOR + "");
                
                // Adicionar todos os jogadores da equipe 1 como adversários
                for (Player opponent : team1Players) {
                    if (opponent != null && opponent.isOnline()) {
                        opponentTeam.addPlayer(opponent);
                    }
                }
                
                // Criar time para os aliados
                Team allyTeam = playerScoreboard.registerNewTeam("ALLY_" + player2.getName());
                allyTeam.setPrefix(ChatColor.GREEN + "");
                
                // Adicionar todos os jogadores da equipe 2 como aliados (exceto o próprio jogador)
                for (Player ally : team2Players) {
                    if (ally != null && ally.isOnline() && !ally.equals(player2)) {
                        allyTeam.addPlayer(ally);
                    }
                }
                
                // Aplicar o scoreboard
                player2.setScoreboard(playerScoreboard);
            }
        }
        
        System.out.println("[PrimeLeagueX1] Nametags para duelo em equipe aplicadas com sucesso");
    }
    
    /**
     * Limpa times existentes para um jogador
     * 
     * @param player Jogador
     */
    private void cleanupExistingTeams(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Limpar no scoreboard principal
        Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
        String teamName = DUEL_PREFIX_TEAM + player.getName();
        Team team = mainScoreboard.getTeam(teamName);
        if (team != null) {
            try {
                team.unregister();
                System.out.println("[PrimeLeagueX1] Time " + teamName + " removido do scoreboard principal");
            } catch (Exception e) {
                System.out.println("[PrimeLeagueX1] Erro ao remover time: " + e.getMessage());
            }
        }
        
        // Garantir que o jogador não está em nenhum time que poderia causar conflito
        for (Team t : mainScoreboard.getTeams()) {
            if (t.getPlayers().contains(player)) {
                try {
                    t.removePlayer(player);
                    System.out.println("[PrimeLeagueX1] Jogador " + player.getName() + " removido do time " + t.getName());
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao remover jogador do time: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Restaura as nametags originais dos jogadores após o duelo
     * 
     * @param player1 Primeiro jogador
     * @param player2 Segundo jogador
     */
    public void removeDuelTags(Player player1, Player player2) {
        // Restaurar scoreboards originais
        restoreOriginalScoreboard(player1);
        restoreOriginalScoreboard(player2);
        
        // Restaurar visualização global
        restoreGlobalView(player1);
        restoreGlobalView(player2);
    }
    
    /**
     * Remove as nametags de todos os membros de ambas as equipes
     * 
     * @param team1Players Lista de jogadores da equipe 1
     * @param team2Players Lista de jogadores da equipe 2
     */
    public void removeTeamDuelTags(List<Player> team1Players, List<Player> team2Players) {
        if (team1Players == null || team2Players == null) {
            return;
        }
        
        // Restaurar scoreboards originais para equipe 1
        for (Player player : team1Players) {
            if (player != null && player.isOnline()) {
                restoreOriginalScoreboard(player);
                restoreGlobalView(player);
            }
        }
        
        // Restaurar scoreboards originais para equipe 2
        for (Player player : team2Players) {
            if (player != null && player.isOnline()) {
                restoreOriginalScoreboard(player);
                restoreGlobalView(player);
            }
        }
    }
    
    /**
     * Salva o time original de um jogador
     * 
     * @param player Jogador
     */
    private void saveOriginalTeam(Player player) {
        if (player != null && player.isOnline()) {
            // Para o caso de plugins que já modificam o scoreboard
            Team currentTeam = null;
            try {
                for (Team team : player.getScoreboard().getTeams()) {
                    if (team.getPlayers().contains(player)) {
                        currentTeam = team;
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignorar exceções ao tentar obter o time atual
            }
            
            // Salvar o nome do time atual, ou uma string vazia se não tiver
            originalTeams.put(player.getUniqueId(), currentTeam != null ? currentTeam.getName() : "");
        }
    }
    
    /**
     * Configura um scoreboard personalizado para o jogador em duelo
     * 
     * @param player Jogador
     * @param opponent Adversário
     */
    private void setupDuelScoreboard(Player player, Player opponent) {
        if (player == null || !player.isOnline() || opponent == null || !opponent.isOnline()) {
            System.out.println("[PrimeLeagueX1] Erro: jogador ou oponente nulo/offline");
            return;
        }
        
        try {
            System.out.println("[PrimeLeagueX1] Configurando scoreboard de duelo para " + player.getName());
            
            // Criar um novo scoreboard para o jogador
            Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
            playerScoreboards.put(player.getUniqueId(), scoreboard);
            
            // Remover time antigo se existir
            String opponentTeamName = "O" + Math.abs(opponent.getName().hashCode() % 1000); // Nome mais curto e único
            Team existingTeam = scoreboard.getTeam(opponentTeamName);
            if (existingTeam != null) {
                try {
                    existingTeam.unregister();
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao remover time existente: " + e.getMessage());
                }
            }
            
            // Criar time para o adversário (destacado)
            Team opponentTeam = scoreboard.registerNewTeam(opponentTeamName);
            
            // Configurar o time - usar prefixo e cores
            opponentTeam.setPrefix(OPPONENT_COLOR + "" + ChatColor.BOLD + "X1 " + ChatColor.RESET);
            opponentTeam.setSuffix(" " + OPPONENT_COLOR + "!");
            
            // Adicionar jogador ao time
            opponentTeam.addPlayer(opponent);
            
            // Aplicar o scoreboard ao jogador
            player.setScoreboard(scoreboard);
            
            System.out.println("[PrimeLeagueX1] Scoreboard aplicado para " + player.getName() + 
                    " com oponente " + opponent.getName() + " no time " + opponentTeamName);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO ao configurar scoreboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Configura a visualização global do jogador em duelo para outros jogadores
     * 
     * @param player Jogador em duelo
     */
    private void setupGlobalDuelView(Player player) {
        if (player == null || !player.isOnline()) {
            System.out.println("[PrimeLeagueX1] Erro: jogador nulo/offline");
            return;
        }
        
        try {
            System.out.println("[PrimeLeagueX1] Configurando visualização global para " + player.getName());
            
            // Obter o scoreboard principal
            Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
            
            // Remover time antigo se existir
            String teamName = "D" + Math.abs(player.getName().hashCode() % 1000); // Nome mais curto e único
            Team existingTeam = mainScoreboard.getTeam(teamName);
            if (existingTeam != null) {
                try {
                    existingTeam.unregister();
                    System.out.println("[PrimeLeagueX1] Time existente " + teamName + " removido");
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao remover time existente: " + e.getMessage());
                }
            }
            
            // Criar time para jogadores em duelo
            Team duelTeam = mainScoreboard.registerNewTeam(teamName);
            System.out.println("[PrimeLeagueX1] Time " + teamName + " criado no scoreboard principal");
            
            // Configurar o prefixo para mostrar que está em X1
            duelTeam.setPrefix(DUEL_COLOR + "" + ChatColor.BOLD + "X1 " + ChatColor.RESET);
            duelTeam.setSuffix(" " + DUEL_COLOR + "#");
            
            // Adicionar jogador ao time
            duelTeam.addPlayer(player);
            
            // Aplicar a todos os jogadores que não estão no duelo
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.equals(player) || isInDuel(onlinePlayer)) {
                    continue;
                }
                
                // Apenas aplicar se o jogador não estiver em um duelo
                try {
                    onlinePlayer.setScoreboard(mainScoreboard);
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao aplicar scoreboard para " + 
                            onlinePlayer.getName() + ": " + e.getMessage());
                }
            }
            
            System.out.println("[PrimeLeagueX1] Visualização global aplicada para outros jogadores");
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO ao configurar visualização global: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Restaura o scoreboard original do jogador
     * 
     * @param player Jogador
     */
    private void restoreOriginalScoreboard(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Remover o scoreboard personalizado
        playerScoreboards.remove(player.getUniqueId());
        
        // Restaurar o scoreboard principal
        player.setScoreboard(scoreboardManager.getMainScoreboard());
    }
    
    /**
     * Restaura a visualização global do jogador após o duelo
     * 
     * @param player Jogador
     */
    private void restoreGlobalView(Player player) {
        if (player == null) {
            return;
        }
        
        System.out.println("[PrimeLeagueX1] Restaurando visualização global para " + player.getName());
        
        try {
            // Remover o jogador do time de duelo
            Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
            
            // O nome do time pode estar em dois formatos diferentes
            // 1. Formato antigo: DUEL_PREFIX_TEAM + player.getName()
            // 2. Formato novo: "D" + Math.abs(player.getName().hashCode() % 1000)
            
            // Tentativa 1: Verificar pelo formato novo (hash)
            String hashTeamName = "D" + Math.abs(player.getName().hashCode() % 1000);
            Team hashTeam = mainScoreboard.getTeam(hashTeamName);
            
            if (hashTeam != null) {
                System.out.println("[PrimeLeagueX1] Removendo jogador do time com hash: " + hashTeamName);
                try {
                    hashTeam.removePlayer(player);
                    hashTeam.unregister();
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao remover jogador do time hash: " + e.getMessage());
                }
            }
            
            // Tentativa 2: Verificar pelo formato antigo
            String legacyTeamName = DUEL_PREFIX_TEAM + player.getName();
            Team legacyTeam = mainScoreboard.getTeam(legacyTeamName);
            
            if (legacyTeam != null) {
                System.out.println("[PrimeLeagueX1] Removendo jogador do time legacy: " + legacyTeamName);
                try {
                    legacyTeam.removePlayer(player);
                    legacyTeam.unregister();
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao remover jogador do time legacy: " + e.getMessage());
                }
            }
            
            // Restaurar time original, se existir
            String originalTeamName = originalTeams.get(player.getUniqueId());
            if (originalTeamName != null && !originalTeamName.isEmpty()) {
                Team originalTeam = mainScoreboard.getTeam(originalTeamName);
                if (originalTeam != null) {
                    try {
                        originalTeam.addPlayer(player);
                        System.out.println("[PrimeLeagueX1] Restaurado time original " + originalTeamName + " para " + player.getName());
                    } catch (Exception e) {
                        System.out.println("[PrimeLeagueX1] Erro ao restaurar time original: " + e.getMessage());
                    }
                }
            }
            
            // Remover da lista de times originais
            originalTeams.remove(player.getUniqueId());
            
            // Garantir que todos os outros jogadores possam ver o jogador normalmente
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(player)) {
                    try {
                        // Forçar atualização da nametag
                        onlinePlayer.hidePlayer(player);
                        onlinePlayer.showPlayer(player);
                    } catch (Exception e) {
                        System.out.println("[PrimeLeagueX1] Erro ao atualizar visibilidade: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("[PrimeLeagueX1] Restauração de nametags concluída para " + player.getName());
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO GRAVE ao restaurar visualização: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Remove todas as nametags de duelo para um jogador
     * 
     * @param player Jogador
     */
    public void removeAllDuelTags(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            System.out.println("[PrimeLeagueX1] Removendo todas as nametags de duelo para " + player.getName());
            
            // Restaurar scoreboard original
            restoreOriginalScoreboard(player);
            
            // Restaurar visualização global
            restoreGlobalView(player);
            
            // Limpar entradas do mapa para este jogador
            originalTeams.remove(player.getUniqueId());
            playerScoreboards.remove(player.getUniqueId());
            
            System.out.println("[PrimeLeagueX1] Nametags de duelo removidas para " + player.getName());
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO ao remover nametags: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Limpa todas as nametags personalizadas
     * Deve ser chamado quando o plugin é desabilitado
     */
    public void clearAllTags() {
        // Restaurar todos os scoreboards
        for (UUID playerId : playerScoreboards.keySet()) {
            Player player = getPlayerById(playerId);
            if (player != null && player.isOnline()) {
                player.setScoreboard(scoreboardManager.getMainScoreboard());
            }
        }
        
        // Limpar as coleções
        playerScoreboards.clear();
        originalTeams.clear();
        
        System.out.println("[PrimeLeagueX1] Limpando TODOS os times de nametag");
        
        // Tentar remover todos os times do duelo
        Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
        for (Team team : mainScoreboard.getTeams()) {
            // Verificar todos os possíveis formatos de time
            String teamName = team.getName();
            
            if (teamName.startsWith(DUEL_PREFIX_TEAM) || 
                teamName.startsWith(OPPONENT_PREFIX_TEAM) ||
                teamName.startsWith("D") || 
                teamName.startsWith("O")) {
                
                try {
                    team.unregister();
                    System.out.println("[PrimeLeagueX1] Time removido: " + teamName);
                } catch (Exception e) {
                    System.out.println("[PrimeLeagueX1] Erro ao remover time " + teamName + ": " + e.getMessage());
                }
            }
        }
        
        // Forçar atualização para todos os jogadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.setScoreboard(scoreboardManager.getMainScoreboard());
            } catch (Exception e) {
                System.out.println("[PrimeLeagueX1] Erro ao redefinir scoreboard: " + e.getMessage());
            }
        }
        
        System.out.println("[PrimeLeagueX1] Limpeza de nametags concluída");
    }
    
    /**
     * Obtém um jogador pelo ID
     * 
     * @param playerId ID do jogador
     * @return Jogador ou null se não estiver online
     */
    private Player getPlayerById(UUID playerId) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(playerId)) {
                return player;
            }
        }
        return null;
    }
    
    /**
     * Verifica se um jogador está em duelo
     * 
     * @param player Jogador
     * @return true se estiver em duelo
     */
    private boolean isInDuel(Player player) {
        if (player == null) {
            return false;
        }
        
        // Usar o DuelManager para verificar corretamente se o jogador está em duelo
        return plugin.getDuelManager().isInDuel(player.getName());
    }
} 
