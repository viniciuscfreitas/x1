package br.com.primeleague.x1.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.utils.ColorUtils;

/**
 * Gerenciador de mensagens para o plugin
 */
public class MessageManager {

    private final Main plugin;
    private Map<String, String> messages;
    private Map<String, List<String>> messagesList;
    private final String prefix;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public MessageManager(Main plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.messagesList = new HashMap<>();
        this.prefix = getConfig().getString("prefixo", "&6[X1] &r");
        loadMessages();
    }
    
    /**
     * Carrega as mensagens do arquivo de configuração
     */
    public void loadMessages() {
        messages.clear();
        messagesList.clear();
        
        FileConfiguration config = plugin.getConfigManager().getMessagesConfig();
        if (config == null) {
            plugin.getLogger().warning("Não foi possível carregar o arquivo de mensagens!");
            return;
        }
        
        // Carrega todas as mensagens do arquivo de configuração
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                messages.put(key, config.getString(key));
            } else if (config.isList(key)) {
                messagesList.put(key, config.getStringList(key));
            }
        }
        
        plugin.getLogger().info("Mensagens carregadas com sucesso!");
    }
    
    /**
     * Obtém a configuração de mensagens
     * @return Configuração de mensagens
     */
    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getMessages();
    }
    
    /**
     * Obtém uma mensagem pelo seu identificador
     * 
     * @param key Identificador da mensagem
     * @return A mensagem
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, "§cMensagem não encontrada: " + key);
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
     * Envia uma mensagem para um jogador
     * 
     * @param player Jogador
     * @param key Identificador da mensagem
     * @param replacements Placeholders e seus valores
     */
    public void sendMessage(Player player, String key, Object... replacements) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        String message = getMessage(key);
        if (message == null || message.isEmpty()) {
            return;
        }
        
        player.sendMessage(formatMessage(message, replacements));
    }
    
    /**
     * Envia uma lista de mensagens para um jogador
     * 
     * @param player Jogador
     * @param key Identificador da lista de mensagens
     * @param replacements Placeholders e seus valores
     */
    public void sendMessageList(Player player, String key, Object... replacements) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        List<String> messages = getMessageList(key);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        for (String message : messages) {
            player.sendMessage(formatMessage(message, replacements));
        }
    }
    
    /**
     * Envia uma mensagem para todos os jogadores
     * 
     * @param key Identificador da mensagem
     * @param replacements Placeholders e seus valores
     */
    public void broadcastMessage(String key, Object... replacements) {
        String message = getMessage(key);
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String formattedMessage = formatMessage(message, replacements);
        plugin.getServer().broadcastMessage(formattedMessage);
    }
    
    /**
     * Verifica se uma mensagem existe
     * 
     * @param key Identificador da mensagem
     * @return true se a mensagem existir, false caso contrário
     */
    public boolean hasMessage(String key) {
        return messages.containsKey(key);
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
} 