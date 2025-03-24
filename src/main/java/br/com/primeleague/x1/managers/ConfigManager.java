package br.com.primeleague.x1.managers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import br.com.primeleague.x1.Main;

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
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Arquivo de mensagens
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        checkForDefaults();
    }
    
    /**
     * Verifica se existem valores padrão que precisam ser adicionados aos arquivos
     */
    private void checkForDefaults() {
        boolean needsSave = false;
        
        // Configuração principal
        if (!config.isSet("configuracoes.contagem-regressiva")) {
            config.set("configuracoes.contagem-regressiva", 3);
            needsSave = true;
        }
        
        if (!config.isSet("configuracoes.tempo-maximo-duelo")) {
            config.set("configuracoes.tempo-maximo-duelo", 300);
            needsSave = true;
        }
        
        if (!config.isSet("configuracoes.tempo-confirmacao")) {
            config.set("configuracoes.tempo-confirmacao", 30);
            needsSave = true;
        }
        
        if (!config.isSet("configuracoes.usar-kit")) {
            config.set("configuracoes.usar-kit", true);
            needsSave = true;
        }
        
        if (!config.isSet("configuracoes.usar-elo")) {
            config.set("configuracoes.usar-elo", true);
            needsSave = true;
        }
        
        if (!config.isSet("configuracoes.usar-apostas")) {
            config.set("configuracoes.usar-apostas", true);
            needsSave = true;
        }
        
        if (!config.isSet("configuracoes.quantidade-top-jogadores")) {
            config.set("configuracoes.quantidade-top-jogadores", 10);
            needsSave = true;
        }
        
        if (!config.isSet("configuracoes.distancia-maxima-duelo-local")) {
            config.set("configuracoes.distancia-maxima-duelo-local", 10);
            needsSave = true;
        }
        
        if (!config.isSet("apostas.valores-predefinidos")) {
            config.set("apostas.valores-predefinidos", Arrays.asList(100, 500, 1000, 5000, 10000));
            needsSave = true;
        }
        
        if (needsSave) {
            saveConfig();
        }
        
        // Verificar mensagens
        needsSave = false;
        
        if (!messagesConfig.isSet("prefixo")) {
            messagesConfig.set("prefixo", "&6[X1] &r");
            needsSave = true;
        }
        
        if (needsSave) {
            saveMessagesConfig();
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
        config = YamlConfiguration.loadConfiguration(configFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
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
} 