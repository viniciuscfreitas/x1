package br.com.primeleague.x1.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import java.lang.reflect.Constructor;

import br.com.primeleague.x1.Main;

/**
 * Utilitário para exibir efeitos visuais e sonoros
 */
public class EffectsUtils {
    
    private static Main plugin;
    
    /**
     * Inicializa a classe com o plugin
     * 
     * @param mainPlugin Instância do plugin
     */
    public static void initialize(Main mainPlugin) {
        plugin = mainPlugin;
    }
    
    /**
     * Toca um som para o jogador
     * 
     * @param player Jogador que receberá o som
     * @param soundName Nome do som no enum Sound ou definido na config
     */
    public static void playSound(Player player, String soundName) {
        playSound(player, soundName, 1.0f, 1.0f);
    }
    
    /**
     * Toca um som para o jogador com volume e pitch personalizados
     * 
     * @param player Jogador que receberá o som
     * @param soundName Nome do som no enum Sound ou definido na config
     * @param volume Volume do som (0.0 a 1.0)
     * @param pitch Tom do som (0.5 a 2.0)
     */
    public static void playSound(Player player, String soundName, float volume, float pitch) {
        if (player == null || soundName == null || soundName.isEmpty()) {
            return;
        }
        
        try {
            // Tentar reproduzir o som diretamente pelo nome
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, volume, pitch);
                return;
            } catch (IllegalArgumentException e) {
                // Ignorar, provavelmente o som está definido na config
            }
            
            // Verificar configuração para sons
            if (plugin != null) {
                String configSound = plugin.getConfigManager().getSound(soundName);
                if (configSound != null && !configSound.isEmpty()) {
                    try {
                        Sound sound = Sound.valueOf(configSound.toUpperCase());
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    } catch (IllegalArgumentException e) {
                        System.out.println("[PrimeLeagueX1] Som inválido: " + configSound);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao reproduzir som: " + e.getMessage());
        }
    }
    
    /**
     * Envia um título para o jogador
     * Esta implementação é compatível com versões mais antigas do Bukkit
     * 
     * @param player Jogador que receberá o título
     * @param title Título principal
     * @param subtitle Subtítulo
     * @param fadeIn Tempo de fade in (ticks)
     * @param stay Tempo de permanência (ticks)
     * @param fadeOut Tempo de fade out (ticks)
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        
        try {
            // Usar reflection para compatibilidade com diferentes versões
            Class<?> packetPlayOutTitleClass = getNMSClass("PacketPlayOutTitle");
            Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
            Class<?> chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
            
            if (packetPlayOutTitleClass != null && iChatBaseComponentClass != null) {
                // Enviar título
                if (title != null && !title.isEmpty()) {
                    Object chatComponent = chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                    Constructor<?> titleConstructor = packetPlayOutTitleClass.getConstructor(
                            getNMSClass("PacketPlayOutTitle$EnumTitleAction"), iChatBaseComponentClass, int.class, int.class, int.class);
                    Object packet = titleConstructor.newInstance(
                            Enum.valueOf((Class<Enum>) getNMSClass("PacketPlayOutTitle$EnumTitleAction"), "TITLE"),
                            chatComponent, fadeIn, stay, fadeOut);
                    sendPacket(player, packet);
                }
                
                // Enviar subtítulo
                if (subtitle != null && !subtitle.isEmpty()) {
                    Object chatComponent = chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle + "\"}");
                    Constructor<?> titleConstructor = packetPlayOutTitleClass.getConstructor(
                            getNMSClass("PacketPlayOutTitle$EnumTitleAction"), iChatBaseComponentClass, int.class, int.class, int.class);
                    Object packet = titleConstructor.newInstance(
                            Enum.valueOf((Class<Enum>) getNMSClass("PacketPlayOutTitle$EnumTitleAction"), "SUBTITLE"),
                            chatComponent, fadeIn, stay, fadeOut);
                    sendPacket(player, packet);
                }
            }
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao enviar título: " + e.getMessage());
            // Fallback para clientes que não suportam títulos
            player.sendMessage(title);
            if (subtitle != null && !subtitle.isEmpty()) {
                player.sendMessage(subtitle);
            }
        }
    }
    
    /**
     * Envia uma mensagem na action bar para o jogador
     * Esta implementação é compatível com versões mais antigas do Bukkit
     * 
     * @param player Jogador que receberá a mensagem
     * @param message Mensagem
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        
        try {
            Class<?> chatSerializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");
            Class<?> chatComponentClass = getNMSClass("IChatBaseComponent");
            Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
            
            if (chatSerializerClass != null && chatComponentClass != null && packetPlayOutChatClass != null) {
                // Serializar o texto
                Object chatComponent = chatSerializerClass.getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + message + "\"}");
                
                // Criar o pacote com o componente de chat e o tipo de mensagem (2 = action bar)
                Constructor<?> packetConstructor = packetPlayOutChatClass.getConstructor(chatComponentClass, byte.class);
                Object packet = packetConstructor.newInstance(chatComponent, (byte) 2);
                
                // Enviar o pacote
                sendPacket(player, packet);
            } else {
                // Fallback: enviar mensagem normal
                player.sendMessage(message);
            }
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao enviar action bar: " + e.getMessage());
            // Fallback: enviar mensagem normal
            player.sendMessage(message);
        }
    }
    
    /**
     * Obtém uma classe NMS pelo nome
     * 
     * @param nmsClassName Nome da classe NMS
     * @return Classe NMS ou null se não encontrada
     */
    private static Class<?> getNMSClass(String nmsClassName) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
            String className = "net.minecraft.server." + version + nmsClassName;
            return Class.forName(className);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao obter classe NMS " + nmsClassName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Envia um pacote para o jogador
     * 
     * @param player Jogador
     * @param packet Pacote a ser enviado
     */
    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao enviar pacote: " + e.getMessage());
        }
    }
    
    /**
     * Cria efeitos de partículas na localização
     * 
     * @param location Localização onde o efeito será exibido
     * @param effect Tipo de efeito
     * @param count Número de partículas
     */
    public static void playEffect(Location location, Effect effect, int count) {
        if (location == null || effect == null) {
            return;
        }
        
        try {
            location.getWorld().playEffect(location, effect, count);
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] Erro ao criar efeito: " + e.getMessage());
        }
    }
    
    /**
     * Cria fogos de artifício na localização do jogador vencedor
     * 
     * @param player Jogador vencedor
     * @param count Número de fogos
     */
    public static void spawnFireworks(Player player, int count) {
        if (player == null) {
            return;
        }
        
        try {
            // Efeito alternativo para versões que não suportam fogos
            player.getWorld().strikeLightningEffect(player.getLocation());
            
            // Tentar criar fogos de artifício
            for (int i = 0; i < count; i++) {
                try {
                    Location location = player.getLocation();
                    // Adicionar um pouco de aleatoriedade na posição
                    location.add(Math.random() * 2 - 1, 0, Math.random() * 2 - 1);
                    
                    Firework firework = player.getWorld().spawn(location, Firework.class);
                    FireworkMeta meta = firework.getFireworkMeta();
                    
                    // Configurar efeito aleatório
                    FireworkEffect.Type[] types = FireworkEffect.Type.values();
                    FireworkEffect.Type type = types[(int) (Math.random() * types.length)];
                    
                    // Cores aleatórias
                    Color[] colors = {
                            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, 
                            Color.AQUA, Color.FUCHSIA, Color.PURPLE, Color.ORANGE
                    };
                    Color color1 = colors[(int) (Math.random() * colors.length)];
                    Color color2 = colors[(int) (Math.random() * colors.length)];
                    
                    // Criar efeito do fogo
                    FireworkEffect effect = FireworkEffect.builder()
                            .with(type)
                            .withColor(color1)
                            .withFade(color2)
                            .trail(Math.random() > 0.5)
                            .flicker(Math.random() > 0.5)
                            .build();
                    
                    meta.addEffect(effect);
                    meta.setPower(1); // Altura pequena
                    
                    firework.setFireworkMeta(meta);
                } catch (Exception e) {
                    // Ignorar erros específicos de fogos de artifício
                    // Muitas versões não suportam todos os recursos de fogos
                }
            }
        } catch (Exception e) {
            // Ignorar erros, são apenas efeitos visuais
            System.out.println("[PrimeLeagueX1] Erro ao criar fogos: " + e.getMessage());
        }
    }
    
    /**
     * Toca som de vitória para um jogador
     * 
     * @param player Jogador
     */
    public static void playSoundVictory(Player player) {
        String soundName = plugin.getConfigManager().getSound("vitoria");
        if (soundName == null) soundName = "LEVEL_UP";
        playSound(player, soundName);
    }
    
    /**
     * Toca som de derrota para um jogador
     * 
     * @param player Jogador
     */
    public static void playSoundDefeat(Player player) {
        String soundName = plugin.getConfigManager().getSound("derrota");
        if (soundName == null) soundName = "GHAST_SCREAM";
        playSound(player, soundName);
    }
    
    /**
     * Toca som de nova rivalidade para um jogador
     * 
     * @param player Jogador
     */
    public static void playSoundNewRival(Player player) {
        // Som especial para novas rivalidades - mais dramático 
        playSound(player, "ENDERDRAGON_GROWL", 1.0f, 0.8f);
        
        // Atraso para tocar outro som em sequência
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                playSound(player, "NOTE_PLING", 1.0f, 1.5f);
            }
        }, 15L);
    }
    
    /**
     * Toca som especial para eventos importantes em rivalidades
     * 
     * @param player Jogador
     */
    public static void playSoundSpecial(Player player) {
        playSound(player, "LEVEL_UP", 1.0f, 1.2f);
        
        // Atraso para tocar outro som em sequência
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                playSound(player, "FIREWORK_LARGE_BLAST", 1.0f, 1.0f);
            }
        }, 5L);
    }
} 