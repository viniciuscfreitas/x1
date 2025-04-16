package br.com.primeleague.x1.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.models.Duel;

/**
 * Gerenciador de tags de equipe
 */
public class TeamTagManager {
    
    private final Main plugin;
    private final Map<String, String> teamColors; // Nome da equipe -> Cor
    private final Map<String, String> teamNames; // ID da equipe -> Nome customizado
    private final Map<UUID, Scoreboard> playerScoreboards; // Scoreboards personalizados por jogador
    
    // Prefixos para evitar conflitos
    private static final String TEAM_PREFIX = "X1TEAM_";
    
    // Configurações
    private boolean useTeamPrefixes;
    private boolean showTeamTagAbove;
    private boolean showTeamTagInChat;
    private String chatFormat;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public TeamTagManager(Main plugin) {
        this.plugin = plugin;
        this.teamColors = new HashMap<>();
        this.teamNames = new HashMap<>();
        this.playerScoreboards = new HashMap<>();
        
        loadConfig();
    }
    
    /**
     * Carrega configurações do arquivo de config
     */
    public void loadConfig() {
        // Limpar mapas existentes
        teamColors.clear();
        teamNames.clear();
        
        // Carregar configurações
        useTeamPrefixes = plugin.getConfigManager().getConfig().getBoolean("equipes.usar-prefixos", true);
        showTeamTagAbove = plugin.getConfigManager().getConfig().getBoolean("equipes.mostrar-tag-acima", true);
        showTeamTagInChat = plugin.getConfigManager().getConfig().getBoolean("equipes.mostrar-tag-chat", true);
        chatFormat = plugin.getConfigManager().getConfig().getString("equipes.formato-chat", "&8[%cor%%equipe%&8] &r");
        
        // Carregar cores
        ConfigurationSection colorSection = plugin.getConfigManager().getConfig().getConfigurationSection("equipes.cores");
        if (colorSection != null) {
            for (String key : colorSection.getKeys(false)) {
                String color = colorSection.getString(key);
                teamColors.put(key, color);
            }
        }
        
        // Carregar nomes
        ConfigurationSection nameSection = plugin.getConfigManager().getConfig().getConfigurationSection("equipes.nomes");
        if (nameSection != null) {
            for (String key : nameSection.getKeys(false)) {
                String name = nameSection.getString(key);
                teamNames.put(key, name);
            }
        }
        
        plugin.getLogger().info("TeamTagManager: Carregadas " + teamColors.size() + " cores de equipe e " + teamNames.size() + " nomes de equipe.");
    }
    
    /**
     * Aplica as tags de equipe para um duelo em equipe
     * 
     * @param duel Duelo em equipe
     */
    public void applyTeamTags(Duel duel) {
        if (!useTeamPrefixes || !duel.isTeamDuel()) {
            return;
        }
        
        try {
            // Preparar informações das equipes
            String team1Id = "equipe1";
            String team2Id = "equipe2";
            
            String team1Color = teamColors.getOrDefault(team1Id, "&c");
            String team2Color = teamColors.getOrDefault(team2Id, "&9");
            
            String team1Name = teamNames.getOrDefault(team1Id, "Vermelho");
            String team2Name = teamNames.getOrDefault(team2Id, "Azul");
            
            // Aplicar tags para cada jogador
            for (String playerName : duel.getTeam1()) {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    applyTeamTag(player, duel.getTeam1(), duel.getTeam2(), team1Color, team2Color, team1Name, team2Name);
                }
            }
            
            for (String playerName : duel.getTeam2()) {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    applyTeamTag(player, duel.getTeam2(), duel.getTeam1(), team2Color, team1Color, team2Name, team1Name);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao aplicar tags de equipe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Aplica tag de equipe para um jogador específico
     * 
     * @param player Jogador para aplicar a tag
     * @param myTeam Lista de jogadores da equipe do jogador
     * @param enemyTeam Lista de jogadores da equipe adversária
     * @param myColor Cor da equipe do jogador
     * @param enemyColor Cor da equipe adversária
     * @param myTeamName Nome da equipe do jogador
     * @param enemyTeamName Nome da equipe adversária
     */
    private void applyTeamTag(Player player, List<String> myTeam, List<String> enemyTeam, 
            String myColor, String enemyColor, String myTeamName, String enemyTeamName) {
        
        try {
            // Criar scoreboard personalizado
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            playerScoreboards.put(player.getUniqueId(), scoreboard);
            
            // Criar time para aliados
            Team allyTeam = scoreboard.registerNewTeam(TEAM_PREFIX + "ALLY");
            String allyPrefix = ChatColor.translateAlternateColorCodes('&', myColor) + "[" + myTeamName + "] ";
            
            // Configurar prefixo para aliados (visível acima dos jogadores)
            if (showTeamTagAbove) {
                allyTeam.setPrefix(allyPrefix);
            }
            
            // Adicionar todos os membros da equipe como aliados
            for (String memberName : myTeam) {
                if (!memberName.equals(player.getName())) {
                    Player member = Bukkit.getPlayerExact(memberName);
                    if (member != null && member.isOnline()) {
                        allyTeam.addPlayer(member);
                    }
                }
            }
            
            // Criar time para inimigos
            Team enemyScoreboardTeam = scoreboard.registerNewTeam(TEAM_PREFIX + "ENEMY");
            String enemyPrefix = ChatColor.translateAlternateColorCodes('&', enemyColor) + "[" + enemyTeamName + "] ";
            
            // Configurar prefixo para inimigos (visível acima dos jogadores)
            if (showTeamTagAbove) {
                enemyScoreboardTeam.setPrefix(enemyPrefix);
            }
            
            // Adicionar todos os membros da equipe adversária como inimigos
            for (String memberName : enemyTeam) {
                Player member = Bukkit.getPlayerExact(memberName);
                if (member != null && member.isOnline()) {
                    enemyScoreboardTeam.addPlayer(member);
                }
            }
            
            // Aplicar scoreboard ao jogador
            player.setScoreboard(scoreboard);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao aplicar tag para " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Remove as tags de equipe para um jogador
     * 
     * @param player Jogador para remover as tags
     */
    public void removeTeamTags(Player player) {
        try {
            // Verificar se o jogador tem scoreboard personalizado
            if (playerScoreboards.containsKey(player.getUniqueId())) {
                // Resetar scoreboard para o padrão do servidor
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                playerScoreboards.remove(player.getUniqueId());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao remover tags para " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Remove tags de equipe para todos os jogadores em um duelo
     * 
     * @param duel Duelo para remover as tags
     */
    public void removeTeamTags(Duel duel) {
        if (!useTeamPrefixes || !duel.isTeamDuel()) {
            return;
        }
        
        try {
            // Remover tags da equipe 1
            for (String playerName : duel.getTeam1()) {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    removeTeamTags(player);
                }
            }
            
            // Remover tags da equipe 2
            for (String playerName : duel.getTeam2()) {
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    removeTeamTags(player);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao remover tags de equipe: " + e.getMessage());
        }
    }
    
    /**
     * Formata uma mensagem de chat de um jogador em duelo de equipe
     * 
     * @param player Jogador que enviou a mensagem
     * @param message Mensagem original
     * @return Mensagem formatada com tag de equipe ou a mensagem original
     */
    public String formatChatMessage(Player player, String message) {
        // Verificar se os prefixos no chat estão habilitados
        if (!useTeamPrefixes || !showTeamTagInChat) {
            return message;
        }
        
        // Verificar se o jogador está em um duelo
        if (!plugin.getDuelManager().isInDuel(player.getName())) {
            return message;
        }
        
        Duel duel = plugin.getDuelManager().getPlayerDuel(player.getName());
        
        // Verificar se é um duelo em equipe
        if (duel == null || !duel.isTeamDuel()) {
            return message;
        }
        
        try {
            String teamId;
            String teamName;
            String teamColor;
            
            // Identificar a equipe do jogador
            if (duel.isInTeam1(player.getName())) {
                teamId = "equipe1";
                teamName = teamNames.getOrDefault(teamId, "Vermelho");
                teamColor = teamColors.getOrDefault(teamId, "&c");
            } else if (duel.isInTeam2(player.getName())) {
                teamId = "equipe2";
                teamName = teamNames.getOrDefault(teamId, "Azul");
                teamColor = teamColors.getOrDefault(teamId, "&9");
            } else {
                return message;
            }
            
            // Formatar mensagem com prefixo de equipe
            String prefix = chatFormat
                    .replace("%cor%", teamColor)
                    .replace("%equipe%", teamName);
            
            // Converter cores e retornar a mensagem formatada
            return ChatColor.translateAlternateColorCodes('&', prefix) + message;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao formatar mensagem de chat: " + e.getMessage());
            return message;
        }
    }
    
    /**
     * Verifica se as tags de equipe estão habilitadas
     * 
     * @return true se as tags estão habilitadas
     */
    public boolean areTeamTagsEnabled() {
        return useTeamPrefixes;
    }
    
    /**
     * Verifica se o jogador tem uma scoreboard personalizada
     * 
     * @param player Jogador para verificar
     * @return true se o jogador tem uma scoreboard personalizada
     */
    public boolean hasCustomScoreboard(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }
} 