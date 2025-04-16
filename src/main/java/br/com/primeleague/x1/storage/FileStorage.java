package br.com.primeleague.x1.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.models.DuelHistory;
import br.com.primeleague.x1.models.PlayerStats;

/**
 * Gerenciador de armazenamento em arquivos
 */
public class FileStorage {

    private final Main plugin;
    private File statsFile;
    private FileConfiguration statsConfig;
    private File historyFile;
    private FileConfiguration historyConfig;
    
    private Map<String, PlayerStats> playerStats;
    private List<DuelHistory> duelHistory;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public FileStorage(Main plugin) {
        this.plugin = plugin;
        this.playerStats = new HashMap<String, PlayerStats>();
        this.duelHistory = new ArrayList<DuelHistory>();
        loadFiles();
    }
    
    /**
     * Carrega os arquivos de dados
     */
    private void loadFiles() {
        // Arquivo de estatísticas
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo stats.yml!");
                e.printStackTrace();
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        
        // Arquivo de histórico
        historyFile = new File(plugin.getDataFolder(), "history.yml");
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo history.yml!");
                e.printStackTrace();
            }
        }
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
        
        // Carrega os dados
        loadData();
    }
    
    /**
     * Carrega os dados dos arquivos
     */
    private void loadData() {
        // Carregar estatísticas
        ConfigurationSection statsSection = statsConfig.getConfigurationSection("estatisticas");
        if (statsSection != null) {
            for (String playerName : statsSection.getKeys(false)) {
                ConfigurationSection playerSection = statsSection.getConfigurationSection(playerName);
                if (playerSection != null) {
                    PlayerStats stats = new PlayerStats(playerName);
                    stats.setVictories(playerSection.getInt("vitórias", 0));
                    stats.setDefeats(playerSection.getInt("derrotas", 0));
                    stats.setDraws(playerSection.getInt("empates", 0));
                    
                    if (plugin.getConfigManager().useElo()) {
                        stats.setElo(playerSection.getInt("elo", 1000));
                    }
                    
                    playerStats.put(playerName, stats);
                }
            }
        }
        
        // Carregar histórico
        ConfigurationSection historySection = historyConfig.getConfigurationSection("historico");
        if (historySection != null) {
            for (String key : historySection.getKeys(false)) {
                ConfigurationSection duelSection = historySection.getConfigurationSection(key);
                if (duelSection != null) {
                    DuelHistory duel = new DuelHistory(
                        duelSection.getString("jogador1"),
                        duelSection.getString("jogador2"),
                        duelSection.getString("vencedor"),
                        "X1", // Modo padrão
                        "Desconhecido", // Local padrão
                        duelSection.getLong("data"),
                        duelSection.getInt("duracao", 0),
                        duelSection.getDouble("aposta", 0.0)
                    );
                    duelHistory.add(duel);
                }
            }
        }
        
        // Carregar estatísticas individuais
        File playersFolder = new File(plugin.getDataFolder(), "players");
        if (playersFolder.exists() && playersFolder.isDirectory()) {
            int individualStats = 0;
            
            for (File playerFile : playersFolder.listFiles()) {
                if (playerFile.isFile() && playerFile.getName().endsWith(".yml")) {
                    try {
                        String playerName = playerFile.getName().replace(".yml", "");
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                        
                        if (!playerStats.containsKey(playerName)) {
                            PlayerStats stats = new PlayerStats(playerName);
                            stats.setVictories(config.getInt("stats.vitórias", 0));
                            stats.setDefeats(config.getInt("stats.derrotas", 0));
                            stats.setDraws(config.getInt("stats.empates", 0));
                            
                            if (plugin.getConfigManager().useElo()) {
                                stats.setElo(config.getInt("stats.elo", 1000));
                            }
                            
                            playerStats.put(playerName, stats);
                            individualStats++;
                        }
                    } catch (Exception e) {
                        System.out.println("[PrimeLeagueX1] Erro ao carregar arquivo individual: " + playerFile.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            if (individualStats > 0) {
                System.out.println("[PrimeLeagueX1] " + individualStats + " estatísticas individuais adicionais carregadas.");
            }
        }
    }
    
    /**
     * Salva os dados nos arquivos
     */
    public void saveData() {
        try {
            System.out.println("[PrimeLeagueX1] Salvando dados de estatísticas e histórico...");
            
            // Salva as estatísticas dos jogadores
            ConfigurationSection statsSection = statsConfig.createSection("players");
            for (Map.Entry<String, PlayerStats> entry : playerStats.entrySet()) {
                String playerName = entry.getKey();
                PlayerStats stats = entry.getValue();
                
                // Verificar se as estatísticas estão corretas antes de salvar
                System.out.println("[PrimeLeagueX1] Salvando estatísticas para " + playerName + ": V=" + 
                    stats.getVictories() + ", D=" + stats.getDefeats() + ", E=" + stats.getDraws());
                
                ConfigurationSection playerSection = statsSection.createSection(playerName);
                playerSection.set("victories", stats.getVictories());
                playerSection.set("defeats", stats.getDefeats());
                playerSection.set("draws", stats.getDraws());
                playerSection.set("streak", stats.getStreak());
                playerSection.set("elo", stats.getElo());
                
                // Também salvar no arquivo individual do jogador para garantir
                savePlayerStats(playerName, stats);
            }
            
            // Salva o histórico de duelos
            ConfigurationSection historySection = historyConfig.createSection("duels");
            for (int i = 0; i < duelHistory.size(); i++) {
                DuelHistory history = duelHistory.get(i);
                
                ConfigurationSection duelSection = historySection.createSection(String.valueOf(i));
                duelSection.set("player1", history.getPlayer1());
                duelSection.set("player2", history.getPlayer2());
                duelSection.set("winner", history.getWinner());
                duelSection.set("mode", history.getMode());
                duelSection.set("location", history.getLocation());
                duelSection.set("date", history.getDate());
                duelSection.set("duration", history.getDuration());
                duelSection.set("bet", history.getBet());
            }
            
            // Salva os arquivos
            try {
                statsConfig.save(statsFile);
                historyConfig.save(historyFile);
                
                // Verificar se os arquivos foram salvos
                if (statsFile.exists() && statsFile.length() > 0) {
                    System.out.println("[PrimeLeagueX1] Arquivo de estatísticas salvo com sucesso: " + statsFile.length() + " bytes");
                } else {
                    System.out.println("[PrimeLeagueX1] ERRO: Arquivo de estatísticas vazio ou inexistente após salvamento!");
                }
                
                if (historyFile.exists() && historyFile.length() > 0) {
                    System.out.println("[PrimeLeagueX1] Arquivo de histórico salvo com sucesso: " + historyFile.length() + " bytes");
                } else {
                    System.out.println("[PrimeLeagueX1] ERRO: Arquivo de histórico vazio ou inexistente após salvamento!");
                }
                
                System.out.println("[PrimeLeagueX1] Todos os dados salvos com sucesso!");
            } catch (IOException e) {
                System.out.println("[PrimeLeagueX1] Erro ao salvar arquivos: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro durante o processo de salvamento de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Salva todos os dados
     */
    public void saveAll() {
        saveData();
    }
    
    /**
     * Obtém as estatísticas de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Estatísticas do jogador
     */
    public PlayerStats getPlayerStats(String playerName) {
        PlayerStats stats = playerStats.get(playerName);
        if (stats == null) {
            stats = new PlayerStats(playerName);
            playerStats.put(playerName, stats);
        }
        return stats;
    }
    
    /**
     * Obtém o histórico de duelos de um jogador
     * 
     * @param playerName Nome do jogador
     * @param limit Limite de duelos (0 para todos)
     * @return Lista de duelos do jogador
     */
    public List<DuelHistory> getPlayerHistory(String playerName, int limit) {
        List<DuelHistory> playerHistory = new ArrayList<DuelHistory>();
        
        for (DuelHistory history : duelHistory) {
            if (history.getPlayer1().equals(playerName) || history.getPlayer2().equals(playerName)) {
                playerHistory.add(history);
                
                if (limit > 0 && playerHistory.size() >= limit) {
                    break;
                }
            }
        }
        
        return playerHistory;
    }
    
    /**
     * Adiciona um histórico de duelo à lista
     * 
     * @param history Histórico de duelo
     */
    public void addDuelHistory(DuelHistory history) {
        duelHistory.add(history);
        saveData();
    }
    
    /**
     * Adiciona um histórico de duelo à lista (alias para addDuelHistory)
     * 
     * @param history Histórico de duelo
     */
    public void addDuelToHistory(DuelHistory history) {
        addDuelHistory(history);
    }
    
    /**
     * Obtém os N jogadores com mais vitórias
     * 
     * @param limit Quantidade máxima de jogadores
     * @return Lista de estatísticas dos jogadores
     */
    public List<PlayerStats> getTopPlayers(int limit) {
        List<PlayerStats> topPlayers = new ArrayList<PlayerStats>(playerStats.values());
        
        // Ordena pela quantidade de vitórias
        java.util.Collections.sort(topPlayers, new java.util.Comparator<PlayerStats>() {
            @Override
            public int compare(PlayerStats stats1, PlayerStats stats2) {
                return stats2.getVictories() - stats1.getVictories();
            }
        });
        
        // Limita a quantidade de jogadores
        if (limit > 0 && topPlayers.size() > limit) {
            return topPlayers.subList(0, limit);
        }
        
        return topPlayers;
    }
    
    /**
     * Obtém o arquivo de dados de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Arquivo de dados do jogador
     */
    private File getPlayerFile(String playerName) {
        File playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
        
        return new File(playersFolder, playerName + ".yml");
    }
    
    /**
     * Salva as estatísticas de um jogador
     * 
     * @param playerName Nome do jogador
     * @param stats Estatísticas do jogador
     */
    public void savePlayerStats(String playerName, PlayerStats stats) {
        if (stats == null) {
            return;
        }
        
        try {
            File playerFile = getPlayerFile(playerName);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            
            // Verificar se houve mudanças
            boolean hasChanges = false;
            
            if (config.getInt("stats.vitórias") != stats.getVictories()) {
                config.set("stats.vitórias", stats.getVictories());
                hasChanges = true;
            }
            
            if (config.getInt("stats.derrotas") != stats.getDefeats()) {
                config.set("stats.derrotas", stats.getDefeats());
                hasChanges = true;
            }
            
            if (config.getInt("stats.empates") != stats.getDraws()) {
                config.set("stats.empates", stats.getDraws());
                hasChanges = true;
            }
            
            if (plugin.getConfigManager().useElo() && config.getInt("stats.elo") != stats.getElo()) {
                config.set("stats.elo", stats.getElo());
                hasChanges = true;
            }
            
            // Salvar apenas se houver mudanças
            if (hasChanges) {
                config.save(playerFile);
                System.out.println("[PrimeLeagueX1] Estatísticas do jogador " + playerName + " atualizadas.");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar estatísticas do jogador " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Salva o histórico de duelos
     */
    public void saveHistory() {
        try {
            // Limpar configuração anterior
            historyConfig = new YamlConfiguration();
            
            // Criar seção de histórico
            ConfigurationSection historySection = historyConfig.createSection("historico");
            
            // Salvar cada duelo
            for (int i = 0; i < duelHistory.size(); i++) {
                DuelHistory duel = duelHistory.get(i);
                ConfigurationSection duelSection = historySection.createSection(String.valueOf(i));
                
                duelSection.set("jogador1", duel.getPlayer1());
                duelSection.set("jogador2", duel.getPlayer2());
                duelSection.set("vencedor", duel.getWinner());
                duelSection.set("data", duel.getDate());
                duelSection.set("aposta", duel.getBet());
            }
            
            // Salvar arquivo
            historyConfig.save(historyFile);
            System.out.println("[PrimeLeagueX1] Histórico de duelos salvo com sucesso. Total: " + duelHistory.size());
            
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar histórico de duelos: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 