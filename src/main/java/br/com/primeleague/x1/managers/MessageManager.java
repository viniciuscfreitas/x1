package br.com.primeleague.x1.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.utils.ColorUtils;

/**
 * Gerenciador de mensagens para o plugin
 */
public class MessageManager {

    private final Main plugin;
    private FileConfiguration messages;
    private final Map<String, String> defaultMessages;
    private Map<String, List<String>> messagesList;
    private final String prefix;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public MessageManager(Main plugin) {
        this.plugin = plugin;
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.defaultMessages = new HashMap<>();
        this.messagesList = new HashMap<>();
        String rawPrefix = plugin.getConfigManager().getMessagesConfig().getString("prefixo", "&6[X1] &r");
        this.prefix = ColorUtils.colorize(rawPrefix);
        setupDefaultMessages();
        validateMessages();
    }
    
    /**
     * Configura as mensagens padrão
     */
    private void setupDefaultMessages() {
        // Prefixos e gerais
        defaultMessages.put("plugin.prefix", "&6[PrimeLeagueX1]&r");
        defaultMessages.put("erro.permissao", "&cVocê não tem permissão para isso.");
        defaultMessages.put("erro.console", "&cApenas jogadores podem usar este comando.");
        defaultMessages.put("erro.jogador-offline", "&cJogador não encontrado ou offline.");
        
        // Scoreboard
        defaultMessages.put("scoreboard.enabled", "&aScoreboard ativado.");
        defaultMessages.put("scoreboard.disabled", "&cScoreboard desativado.");
        defaultMessages.put("scoreboard.titulo", "&eDuelo X1");
        defaultMessages.put("scoreboard.aguardando", "&eAguardando duelo");
        defaultMessages.put("scoreboard.vitoria", "&aVitória de: &b%vencedor%");
        defaultMessages.put("scoreboard.derrota", "&cDerrota de: &7%perdedor%");
        defaultMessages.put("scoreboard.dano", "&eDano Causado: &f%dano%");
        defaultMessages.put("scoreboard.versus", "&fContra: &b%oponente%");
        
        // GUI
        defaultMessages.put("gui.item-voltar", "&cVoltar");
        defaultMessages.put("gui.desc-voltar", "&7Clique para voltar ao menu anterior.");
        defaultMessages.put("gui.item-cancelar", "&cCancelar");
        defaultMessages.put("gui.desc-cancelar", "&7Clique para cancelar.");
        defaultMessages.put("gui.item-apostar", "&aApostar %valor% Dollars");
        defaultMessages.put("gui.item-apostar-personalizado", "&bValor personalizado");
        defaultMessages.put("gui.item-sem-aposta", "&7Desafiar sem aposta");
        
        // Title Messages
        defaultMessages.put("x1.title.inicio", "&e3... 2... 1... LUTE!");
        defaultMessages.put("x1.title.vitoria", "&aVitória!");
        defaultMessages.put("x1.title.derrota", "&cDerrota!");
        defaultMessages.put("x1.title.empate", "&eEmpate!");
        
        // Sistema de duelo
        defaultMessages.put("duelo.iniciado", "&aDuelo iniciado entre &b%jogador1% &ae &b%jogador2%&a!");
        defaultMessages.put("duelo.cancelado", "&cO duelo foi cancelado.");
        defaultMessages.put("duelo.fora-limite", "&cVocê saiu da área do duelo!");
        defaultMessages.put("duelo.pendente", "&eVocê já tem um convite pendente.");
        defaultMessages.put("duelo.vitoria", "&aVocê venceu o duelo!");
        defaultMessages.put("duelo.derrota", "&cVocê perdeu o duelo.");
        defaultMessages.put("duelo.stats.atualizado", "&aEstatísticas atualizadas com sucesso!");
        
        // Sistema de apostas
        defaultMessages.put("aposta.valor-invalido", "&cValor inválido. Use apenas números.");
        defaultMessages.put("aposta.sem-dinheiro", "&cVocê não tem dinheiro suficiente.");
        defaultMessages.put("aposta.enviada", "&aAposta enviada para %jogador% no valor de %valor% Dollars.");
        defaultMessages.put("aposta.aceita", "&aAposta aceita com sucesso!");
        defaultMessages.put("aposta.cancelada", "&cAposta cancelada.");
        
        // Mensagens de ranking
        defaultMessages.put("ranking.top-header", "&6Top 10 Jogadores");
        defaultMessages.put("ranking.linha", "&e%pos%. &b%jogador% &7- &a%vitórias% vitórias");
        
        // Mensagens de erro genéricas
        defaultMessages.put("erro.geral", "&cOcorreu um erro. Tente novamente.");
        defaultMessages.put("erro.duelo-existente", "&cVocê já está em um duelo.");
        defaultMessages.put("erro.jogador-ocupado", "&cO jogador está ocupado.");
    }
    
    /**
     * Valida e adiciona mensagens ausentes
     */
    private void validateMessages() {
        int addedMessages = 0;
        
        for (Map.Entry<String, String> entry : defaultMessages.entrySet()) {
            if (!messages.contains(entry.getKey())) {
                messages.set(entry.getKey(), entry.getValue());
                addedMessages++;
                plugin.getLogger().warning("Mensagem ausente adicionada automaticamente: " + entry.getKey());
            }
        }
        
        if (addedMessages > 0) {
            try {
                File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
                messages.save(messagesFile);
                plugin.getLogger().info("Adicionadas " + addedMessages + " mensagens ausentes ao arquivo messages.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao salvar mensagens: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Garante que uma mensagem existe no arquivo
     * 
     * @param key Chave da mensagem
     * @param defaultValue Valor padrão
     */
    public void ensureMessageExists(String key, String defaultValue) {
        if (!messages.contains(key)) {
            messages.set(key, defaultValue);
            plugin.getLogger().warning("Mensagem ausente adicionada automaticamente: " + key);
            saveMessages();
        }
    }
    
    /**
     * Obtém uma mensagem do arquivo
     * 
     * @param key Chave da mensagem
     * @return Mensagem formatada
     */
    public String getMessage(String key) {
        String message = messages.getString(key);
        if (message == null) {
            message = defaultMessages.getOrDefault(key, "&cMensagem não encontrada: " + key);
            ensureMessageExists(key, message);
        }
        return ColorUtils.colorize(message);
    }
    
    /**
     * Obtém uma mensagem do arquivo com placeholders no formato %placeholder%
     * 
     * @param path Chave da mensagem
     * @param placeholders Mapa de placeholders e seus valores
     * @return Mensagem formatada com placeholders substituídos
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getString(path);
        if (message == null) {
            message = defaultMessages.getOrDefault(path, "&cMensagem não encontrada: " + path);
            ensureMessageExists(path, message);
        }
        
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        
        return ColorUtils.colorize(message);
    }
    
    /**
     * Envia uma mensagem para um jogador com placeholders no formato %placeholder%
     * 
     * @param player Jogador
     * @param path Chave da mensagem
     * @param placeholders Mapa de placeholders e seus valores
     */
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        if (player != null && player.isOnline()) {
            String message = getMessage(path, placeholders);
            if (message != null) {
                player.sendMessage(prefix + message);
            }
        }
    }
    
    /**
     * Envia uma mensagem para o console
     * 
     * @param key Chave da mensagem
     * @param placeholders Placeholders para substituir
     */
    public void sendConsoleMessage(String key, Object... placeholders) {
        plugin.getLogger().info(getMessage(key, placeholders));
    }
    
    /**
     * Envia uma mensagem para todos os jogadores online
     * 
     * @param key Chave da mensagem
     * @param placeholders Placeholders para substituir
     */
    public void broadcastMessage(String key, Object... placeholders) {
        String message = getMessage(key, placeholders);
        plugin.getServer().broadcastMessage(message);
    }
    
    /**
     * Salva o arquivo de mensagens
     */
    public void saveMessages() {
        try {
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            YamlConfiguration.loadConfiguration(messagesFile).setDefaults(messages);
            messages.save(messagesFile);
            
            // Recarrega o arquivo para garantir que está em UTF-8
            messages = YamlConfiguration.loadConfiguration(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar arquivo de mensagens: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Recarrega as mensagens do arquivo
     */
    public void reload() {
        try {
            // Recarregar configuração de mensagens
            messages = plugin.getConfigManager().getMessagesConfig();
            
            // Validar e adicionar mensagens ausentes
            validateMessages();
            
            // Recarregar lista de mensagens
            messagesList.clear();
            for (String key : messages.getKeys(true)) {
                if (messages.isList(key)) {
                    messagesList.put(key, messages.getStringList(key));
                }
            }
            
            plugin.getLogger().info("Mensagens recarregadas com sucesso!");
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao recarregar mensagens: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Trunca uma string para o tamanho máximo do scoreboard
     * 
     * @param text Texto a ser truncado
     * @return Texto truncado
     */
    public String truncateScoreboardLine(String text) {
        if (text == null) {
            return "";
        }
        
        // Limitar a 32 caracteres
        if (text.length() > 32) {
            return text.substring(0, 32);
        }
        
        return text;
    }
    
    /**
     * Sanitiza uma string removendo caracteres Unicode problemáticos
     * 
     * @param text Texto a ser sanitizado
     * @return Texto sanitizado
     */
    public String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Permitir apenas caracteres ASCII e códigos de cor
            if (c <= 255 || c == '§') {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Obtém uma lista de mensagens pelo seu identificador
     * 
     * @param key Identificador da lista de mensagens
     * @return A lista de mensagens
     */
    public List<String> getMessageList(String key) {
        return messagesList.getOrDefault(key, null);
    }
    
    /**
     * Formata uma mensagem substituindo placeholders
     * 
     * @param message Mensagem a ser formatada
     * @param replacements Placeholders e seus valores (ex: "%player%", "Steve")
     * @return Mensagem formatada
     */
    public String formatMessage(String message, Object... replacements) {
        if (message == null) {
            return "§cMensagem nula";
        }
        
        String formattedMessage = message;
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                formattedMessage = formattedMessage.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            }
        }
        
        return ColorUtils.colorize(formattedMessage);
    }
    
    /**
     * Verifica se uma mensagem existe
     * 
     * @param key Identificador da mensagem
     * @return true se a mensagem existir, false caso contrário
     */
    public boolean hasMessage(String key) {
        return messages.contains(key);
    }
    
    /**
     * Verifica se uma lista de mensagens existe
     * 
     * @param key Identificador da lista de mensagens
     * @return true se a lista de mensagens existir, false caso contrário
     */
    public boolean hasMessageList(String key) {
        return messagesList.containsKey(key);
    }
    
    /**
     * Obtém uma mensagem da configuração com o prefixo
     * @param path Caminho da mensagem
     * @return Mensagem com prefixo
     */
    public String getMessageWithPrefix(String path) {
        return ColorUtils.colorize(prefix + getMessage(path));
    }
    
    /**
     * Envia uma mensagem para um CommandSender
     * @param sender CommandSender
     * @param path Caminho da mensagem
     */
    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessageWithPrefix(path));
    }
    
    /**
     * Envia uma mensagem para um CommandSender com placeholders
     * @param sender CommandSender
     * @param path Caminho da mensagem
     * @param placeholders Placeholders e valores
     */
    public void sendMessage(CommandSender sender, String path, Object... placeholders) {
        sender.sendMessage(getMessageWithPrefix(getFormattedMessage(path, placeholders)));
    }
    
    /**
     * Obtém uma mensagem formatada com placeholders
     * @param path Caminho da mensagem
     * @param placeholders Placeholders e valores
     * @return Mensagem formatada
     */
    public String getFormattedMessage(String path, Object... placeholders) {
        String message = getMessage(path);
        
        if (placeholders.length % 2 != 0) {
            return message;
        }
        
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            message = message.replace("%" + placeholder + "%", value);
        }
        
        return message;
    }
    
    /**
     * Obtém o prefixo do plugin
     * @return Prefixo do plugin
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * Obtém uma mensagem com tamanho limitado para uso em GUIs
     * (Evita o erro "Received string length longer than maximum allowed (N > 32)")
     * 
     * @param key Chave da mensagem
     * @param maxLength Tamanho máximo permitido (32 para nomes de itens)
     * @return A mensagem limitada ao tamanho especificado
     */
    public String getItemDisplayName(String key) {
        return getShortMessage(key, 32); // 32 é o limite do Minecraft 1.5.2 para displayName
    }
    
    /**
     * Obtém uma mensagem com tamanho limitado
     * 
     * @param key Chave da mensagem
     * @param maxLength Tamanho máximo permitido
     * @return A mensagem limitada ao tamanho especificado
     */
    public String getShortMessage(String key, int maxLength) {
        String message = getMessage(key);
        
        if (message.length() > maxLength) {
            plugin.getLogger().warning("Mensagem muito longa (" + key + ": " + message.length() + " caracteres). Truncando para " + maxLength + " caracteres.");
            return message.substring(0, maxLength);
        }
        
        return message;
    }
    
    /**
     * Obtém uma mensagem formatada e limitada para uso em GUIs
     * 
     * @param key Chave da mensagem
     * @param maxLength Tamanho máximo permitido
     * @param placeholders Placeholders e valores
     * @return A mensagem formatada e limitada
     */
    public String getFormattedItemName(String key, Object... placeholders) {
        String message = getFormattedMessage(key, placeholders);
        
        if (message.length() > 32) {
            plugin.getLogger().warning("Mensagem formatada muito longa (" + key + ": " + message.length() + " caracteres). Truncando para 32 caracteres.");
            return message.substring(0, 32);
        }
        
        return message;
    }
    
    /**
     * Obtém a configuração de mensagens para manipulação direta
     * @return Configuração de mensagens
     */
    public FileConfiguration getMessageConfig() {
        return plugin.getConfigManager().getMessagesConfig();
    }
    
    /**
     * Obtém uma mensagem do arquivo com placeholders no formato %placeholder%
     * 
     * @param key Chave da mensagem
     * @param placeholders Placeholders para substituir
     * @return Mensagem formatada
     */
    public String getMessage(String key, Object... placeholders) {
        String message = getMessage(key);
        
        if (placeholders != null && placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    String placeholder = "%" + placeholders[i] + "%";
                    String value = String.valueOf(placeholders[i + 1]);
                    message = message.replace(placeholder, value);
                }
            }
        }
        
        return message;
    }
    
    /**
     * Envia uma mensagem para um jogador
     * 
     * @param player Jogador
     * @param key Chave da mensagem
     * @param placeholders Placeholders para substituir
     */
    public void sendMessage(Player player, String key, Object... placeholders) {
        if (player != null && player.isOnline()) {
            player.sendMessage(prefix + getMessage(key, placeholders));
        }
    }
} 