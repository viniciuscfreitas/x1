package br.com.primeleague.x1.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import br.com.primeleague.x1.Main;

/**
 * Listener para formatação de mensagens de chat
 */
public class ChatListener implements Listener {
    
    private final Main plugin;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public ChatListener(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Evento para formatar mensagens de chat com tags de equipe
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Verificar se o sistema de tags de equipe está habilitado
        if (plugin.getTeamTagManager() == null || !plugin.getTeamTagManager().areTeamTagsEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Verificar se o jogador está em um duelo
        if (!plugin.getDuelManager().isInDuel(player.getName())) {
            return;
        }
        
        // Formatar a mensagem com a tag da equipe
        String message = event.getMessage();
        String formattedMessage = plugin.getTeamTagManager().formatChatMessage(player, message);
        
        if (!formattedMessage.equals(message)) {
            event.setMessage(formattedMessage);
        }
    }
} 