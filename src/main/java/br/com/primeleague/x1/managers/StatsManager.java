package br.com.primeleague.x1.managers;

import java.util.List;

import org.bukkit.entity.Player;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.models.DuelHistory;
import br.com.primeleague.x1.models.PlayerStats;

/**
 * Gerenciador de estatísticas de jogadores
 */
public class StatsManager {

    private final Main plugin;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public StatsManager(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Obtém as estatísticas de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Estatísticas do jogador
     */
    public PlayerStats getPlayerStats(String playerName) {
        return plugin.getFileStorage().getPlayerStats(playerName);
    }
    
    /**
     * Obtém o histórico de duelos de um jogador
     * 
     * @param playerName Nome do jogador
     * @param limit Limite de duelos
     * @return Lista de duelos do jogador
     */
    public List<DuelHistory> getPlayerHistory(String playerName, int limit) {
        return plugin.getFileStorage().getPlayerHistory(playerName, limit);
    }
    
    /**
     * Obtém o top N jogadores com base nas vitórias
     * 
     * @param limit Quantidade de jogadores
     * @return Lista de estatísticas dos jogadores
     */
    public List<PlayerStats> getTopPlayers(int limit) {
        return plugin.getFileStorage().getTopPlayers(limit);
    }
    
    /**
     * Registra uma vitória para um jogador
     * 
     * @param playerName Nome do jogador
     */
    public void addVictory(String playerName) {
        try {
            System.out.println("[PrimeLeagueX1] Registrando vitória para " + playerName);
            PlayerStats stats = getPlayerStats(playerName);
            System.out.println("[PrimeLeagueX1] Estatísticas antes: V=" + stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + stats.getDraws());
            stats.incrementVictories();
            System.out.println("[PrimeLeagueX1] Estatísticas depois: V=" + stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + stats.getDraws());
            // Salvar no arquivo individual do jogador também
            plugin.getFileStorage().savePlayerStats(playerName, stats);
            // Garantir que os dados sejam salvos globalmente
            plugin.getFileStorage().saveData();
            System.out.println("[PrimeLeagueX1] Vitória registrada com sucesso para " + playerName);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao registrar vitória: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registra uma derrota para um jogador
     * 
     * @param playerName Nome do jogador
     */
    public void addDefeat(String playerName) {
        try {
            System.out.println("[PrimeLeagueX1] Registrando derrota para " + playerName);
            PlayerStats stats = getPlayerStats(playerName);
            System.out.println("[PrimeLeagueX1] Estatísticas antes: V=" + stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + stats.getDraws());
            stats.incrementDefeats();
            System.out.println("[PrimeLeagueX1] Estatísticas depois: V=" + stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + stats.getDraws());
            // Salvar no arquivo individual do jogador também
            plugin.getFileStorage().savePlayerStats(playerName, stats);
            // Garantir que os dados sejam salvos globalmente
            plugin.getFileStorage().saveData();
            System.out.println("[PrimeLeagueX1] Derrota registrada com sucesso para " + playerName);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao registrar derrota: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registra um empate para um jogador
     * 
     * @param playerName Nome do jogador
     */
    public void addDraw(String playerName) {
        try {
            System.out.println("[PrimeLeagueX1] Registrando empate para " + playerName);
            PlayerStats stats = getPlayerStats(playerName);
            System.out.println("[PrimeLeagueX1] Estatísticas antes: V=" + stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + stats.getDraws());
            stats.incrementDraws();
            System.out.println("[PrimeLeagueX1] Estatísticas depois: V=" + stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + stats.getDraws());
            // Salvar no arquivo individual do jogador também
            plugin.getFileStorage().savePlayerStats(playerName, stats);
            // Garantir que os dados sejam salvos globalmente
            plugin.getFileStorage().saveData();
            System.out.println("[PrimeLeagueX1] Empate registrado com sucesso para " + playerName);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao registrar empate: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registra uma vitória para um jogador (alias para addVictory)
     * 
     * @param playerName Nome do jogador
     */
    public void addWin(String playerName) {
        addVictory(playerName);
    }
    
    /**
     * Registra uma derrota para um jogador (alias para addDefeat)
     * 
     * @param playerName Nome do jogador
     */
    public void addLoss(String playerName) {
        addDefeat(playerName);
    }
    
    /**
     * Registra um empate para um jogador (alias para addDraw)
     * 
     * @param playerName Nome do jogador
     */
    public void registerDraw(String playerName) {
        addDraw(playerName);
    }
    
    /**
     * Atualiza o Elo de um jogador
     * 
     * @param playerName Nome do jogador
     * @param elo Novo Elo
     */
    public void updateElo(String playerName, int elo) {
        PlayerStats stats = getPlayerStats(playerName);
        stats.setElo(elo);
    }
    
    /**
     * Adiciona Elo a um jogador
     * 
     * @param playerName Nome do jogador
     * @param amount Quantidade a adicionar
     */
    public void addElo(String playerName, int amount) {
        PlayerStats stats = getPlayerStats(playerName);
        stats.addElo(amount);
    }
    
    /**
     * Remove Elo de um jogador
     * 
     * @param playerName Nome do jogador
     * @param amount Quantidade a remover
     */
    public void removeElo(String playerName, int amount) {
        PlayerStats stats = getPlayerStats(playerName);
        stats.removeElo(amount);
    }
    
    /**
     * Verifica se existem estatísticas para um jogador
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador tem estatísticas
     */
    public boolean playerExists(String playerName) {
        PlayerStats stats = getPlayerStats(playerName);
        // Se as estatísticas não forem nulas e o jogador já jogou pelo menos uma vez
        return stats != null && (stats.getVictories() > 0 || stats.getDefeats() > 0 || stats.getDraws() > 0);
    }
    
    /**
     * Atualiza o Elo de dois jogadores após um duelo
     * 
     * @param winner Nome do jogador vencedor
     * @param loser Nome do jogador perdedor
     */
    public void updateElo(String winner, String loser) {
        // Implementação básica de ajuste de Elo
        PlayerStats winnerStats = getPlayerStats(winner);
        PlayerStats loserStats = getPlayerStats(loser);
        
        int winnerElo = winnerStats.getElo();
        int loserElo = loserStats.getElo();
        
        // Calcula o ganho/perda de pontos (entre 15-25 pontos)
        int baseChange = 20;
        int eloDiff = Math.abs(winnerElo - loserElo);
        int change = baseChange;
        
        // Ajuste baseado na diferença de Elo
        if (winnerElo < loserElo) {
            // Vencedor tem menos Elo, ganha mais pontos
            change = baseChange + Math.min(10, eloDiff / 50);
        } else if (winnerElo > loserElo) {
            // Vencedor tem mais Elo, ganha menos pontos
            change = Math.max(15, baseChange - Math.min(5, eloDiff / 50));
        }
        
        // Atualiza os Elos
        winnerStats.addElo(change);
        loserStats.removeElo(Math.min(change, loserStats.getElo() - 1000)); // Não permitir que fique abaixo de 1000
    }
    
    /**
     * Exibe as estatísticas de um jogador para ele mesmo
     * 
     * @param player Jogador
     */
    public void showStats(Player player) {
        if (player == null) {
            return;
        }
        
        PlayerStats stats = getPlayerStats(player.getName());
        
        player.sendMessage(plugin.getMessageManager().getPrefix() + "§eSuas estatísticas:");
        player.sendMessage("§7Vitórias: §a" + stats.getVictories());
        player.sendMessage("§7Derrotas: §c" + stats.getDefeats());
        player.sendMessage("§7Empates: §e" + stats.getDraws());
        player.sendMessage("§7KDR: §f" + String.format("%.2f", stats.getKDR()));
        player.sendMessage("§7Taxa de vitórias: §f" + String.format("%.2f%%", stats.getWinRate()));
        player.sendMessage("§7Elo: §b" + stats.getElo());
        
        List<DuelHistory> history = getPlayerHistory(player.getName(), 5);
        if (!history.isEmpty()) {
            player.sendMessage("§eÚltimos duelos:");
            for (DuelHistory duel : history) {
                String opponent = duel.getPlayer1().equals(player.getName()) ? duel.getPlayer2() : duel.getPlayer1();
                String result;
                if (duel.isDraw()) {
                    result = "§eempate";
                } else if (duel.isWinner(player.getName())) {
                    result = "§avitória";
                } else {
                    result = "§cderrota";
                }
                player.sendMessage("§7vs " + opponent + ": " + result);
            }
        }
    }
    
    /**
     * Adiciona um duelo ao histórico
     * 
     * @param duel Duelo a ser adicionado ao histórico
     */
    public void addDuelToHistory(br.com.primeleague.x1.models.Duel duel) {
        if (duel == null) {
            return;
        }
        
        DuelHistory history = new DuelHistory(
            duel.getPlayer1(),
            duel.getPlayer2(),
            duel.getWinner(),
            duel.isDraw(),
            duel.getBetAmount(),
            duel.getStartTime(),
            duel.getEndTime()
        );
        
        plugin.getFileStorage().addDuelToHistory(history);
    }
} 