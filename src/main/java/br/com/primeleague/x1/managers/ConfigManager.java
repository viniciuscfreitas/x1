package br.com.primeleague.x1.managers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.utils.FileUtils;

/**
 * Gerenciador de configurações do plugin
 */
public class ConfigManager {

    private final Main plugin;
    
    private File configFile;
    private FileConfiguration config;
    
    private File messagesFile;
    private FileConfiguration messagesConfig;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        setup();
    }
    
    /**
     * Configura os arquivos de configuração
     */
    private void setup() {
        // Configuração principal
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("config.yml", false);
        }
        
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar config.yml: " + e.getMessage());
            plugin.saveResource("config.yml", true);
            config = YamlConfiguration.loadConfiguration(configFile);
        }
        
        // Arquivo de mensagens
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
            plugin.getLogger().info("O arquivo messages.yml foi criado pela primeira vez.");
        }
        
        // Verificar se o arquivo contém caracteres Unicode problemáticos
        if (FileUtils.containsProblematicUnicodeCharacters(messagesFile)) {
            plugin.getLogger().warning("O arquivo messages.yml contém caracteres Unicode que podem causar problemas!");
            plugin.getLogger().warning("Tentando remover caracteres problemáticos...");
            
            if (FileUtils.removeUnicodeCharacters(messagesFile)) {
                plugin.getLogger().info("Caracteres Unicode problemáticos foram removidos do arquivo messages.yml.");
            } else {
                plugin.getLogger().severe("Não foi possível remover caracteres Unicode problemáticos. Recriando o arquivo...");
                plugin.saveResource("messages.yml", true);
            }
        }
        
        try {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            
            // Verificar se foi carregado corretamente
            if (messagesConfig.getKeys(true).isEmpty()) {
                plugin.getLogger().warning("O arquivo messages.yml foi carregado, mas está vazio.");
                plugin.saveResource("messages.yml", true);
                messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar messages.yml: " + e.getMessage());
            // Tentar recriar o arquivo
            plugin.saveResource("messages.yml", true);
            try {
                messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            } catch (Exception e2) {
                plugin.getLogger().severe("Falha ao recarregar messages.yml. Usando configuração padrão vazia.");
                messagesConfig = new YamlConfiguration();
                // Adicionar pelo menos o prefixo padrão
                messagesConfig.set("prefixo", "&7[&bX1&7] ");
            }
        }
        
        // Verificar se o arquivo de mensagens foi carregado corretamente
        if (messagesConfig == null) {
            plugin.getLogger().severe("O arquivo messages.yml não foi carregado corretamente!");
            messagesConfig = new YamlConfiguration();
            messagesConfig.set("prefixo", "&7[&bX1&7] ");
        }
        
        // Verificar valores padrão apenas uma vez
        checkForDefaults();
    }
    
    /**
     * Verifica se existem valores padrão que precisam ser adicionados aos arquivos
     */
    private void checkForDefaults() {
        boolean needsSave = false;
        
        // Verificar config.yml
        if (!config.isSet("configuracoes.versao")) {
            config.set("configuracoes.versao", "1.0");
            needsSave = true;
        }
        
        // Verificar messages.yml
        if (!messagesConfig.isSet("prefixo")) {
            messagesConfig.set("prefixo", "&6[X1] &r");
            needsSave = true;
        }
        
        // Salvar apenas se houver mudanças
        if (needsSave) {
            try {
                config.save(configFile);
                messagesConfig.save(messagesFile);
                plugin.getLogger().info("Valores padrão adicionados aos arquivos de configuração.");
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao salvar valores padrão: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Salva o arquivo de configuração principal
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar o arquivo de configuração!");
            e.printStackTrace();
        }
    }
    
    /**
     * Salva o arquivo de mensagens
     */
    public void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar o arquivo de mensagens!");
            e.printStackTrace();
        }
    }
    
    /**
     * Recarrega todos os arquivos de configuração
     */
    public void reloadConfigs() {
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao recarregar config.yml: " + e.getMessage());
        }
        
        try {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            if (messagesConfig.getKeys(true).isEmpty()) {
                plugin.getLogger().warning("O arquivo messages.yml foi recarregado, mas está vazio!");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao recarregar messages.yml: " + e.getMessage());
        }
    }
    
    /**
     * Obtém a configuração principal
     * 
     * @return Configuração principal
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Obtém a configuração de mensagens
     * 
     * @return Configuração de mensagens
     */
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    /**
     * Obtém a configuração de mensagens
     * Alias para getMessagesConfig para compatibilidade
     * 
     * @return Configuração de mensagens
     */
    public FileConfiguration getMessages() {
        return messagesConfig;
    }
    
    /**
     * Verifica se deve usar contagem regressiva
     * 
     * @return true se deve usar contagem regressiva
     */
    public boolean useCountdown() {
        return config.getBoolean("configuracoes.contagem-regressiva", true);
    }
    
    /**
     * Obtém o tempo de contagem regressiva em segundos
     * 
     * @return Tempo de contagem regressiva
     */
    public int getDuelCountdown() {
        return config.getInt("configuracoes.contagem-regressiva", 3);
    }
    
    /**
     * Obtém o tempo máximo de duelo em segundos
     * 
     * @return Tempo máximo de duelo
     */
    public int getDuelMaxTime() {
        return config.getInt("configuracoes.tempo-maximo-duelo", 300);
    }
    
    /**
     * Obtém o tempo para confirmar um desafio em segundos
     * 
     * @return Tempo para confirmar desafio
     */
    public int getChallengeTimeout() {
        return config.getInt("configuracoes.tempo-confirmacao", 30);
    }
    
    /**
     * Verifica se deve usar o kit de duelo
     * 
     * @return true se deve usar o kit
     */
    public boolean useDuelKit() {
        return config.getBoolean("configuracoes.usar-kit", true);
    }
    
    /**
     * Verifica se deve usar o sistema de Elo
     * 
     * @return true se deve usar Elo
     */
    public boolean useElo() {
        return config.getBoolean("configuracoes.usar-elo", true);
    }
    
    /**
     * Verifica se deve permitir apostas
     * 
     * @return true se apostas estão habilitadas
     */
    public boolean useBets() {
        return config.getBoolean("configuracoes.usar-apostas", true);
    }
    
    /**
     * Obtém a quantidade de jogadores a mostrar no ranking
     * 
     * @return Quantidade de jogadores no ranking
     */
    public int getTopPlayersAmount() {
        return config.getInt("configuracoes.quantidade-top-jogadores", 10);
    }
    
    /**
     * Obtém a distância máxima para duelos locais
     * 
     * @return Distância máxima em blocos
     */
    public double getMaxLocalDuelDistance() {
        return config.getDouble("configuracoes.distancia-maxima-duelo-local", 10.0);
    }
    
    /**
     * Verifica se deve checar se o inventário do jogador está vazio antes de fornecer kit
     * 
     * @return true se deve verificar inventário vazio
     */
    public boolean checkEmptyInventory() {
        return config.getBoolean("configuracoes.checar-inventario-vazio", true);
    }
    
    /**
     * Verifica se deve usar nametags personalizadas durante duelos
     * 
     * @return true se deve usar nametags
     */
    public boolean useCustomNametags() {
        boolean useNametags = config.getBoolean("configuracoes.usar-nametags", true);
        System.out.println("[PrimeLeagueX1] Configuração usar-nametags: " + useNametags);
        return useNametags;
    }
    
    /**
     * Verifica se deve permitir PvP em áreas protegidas pelo WorldGuard para jogadores em duelo
     * 
     * @return true se deve permitir
     */
    public boolean allowPvPInProtectedAreas() {
        return config.getBoolean("integracao.worldguard.permitir-pvp-em-areas-protegidas", true);
    }
    
    /**
     * Obtém o som configurado pelo nome
     * 
     * @param soundName Nome do som na configuração
     * @return Nome do som Bukkit ou null se não existir
     */
    public String getSound(String soundName) {
        return config.getString("sons." + soundName, "");
    }
    
    /**
     * Salva as alterações no arquivo de mensagens
     */
    public void saveMessages() {
        try {
            // Verificar se a configuração e o arquivo existem
            if (messagesConfig == null || messagesFile == null) {
                plugin.getLogger().severe("Não foi possível salvar as mensagens: configuração ou arquivo inexistente!");
                return;
            }
            
            // Salvar as alterações no arquivo
            messagesConfig.save(messagesFile);
            plugin.getLogger().info("Arquivo de mensagens salvo com sucesso!");
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao salvar arquivo de mensagens: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 